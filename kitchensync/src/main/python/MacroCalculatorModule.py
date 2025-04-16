import requests
import json
import sys
import os
import re

# 🔹 USDA API Key
USDA_API_KEY = "l43si2gPPVmHMLavt4w78UjeMNK8RDahOPexf0NM"
USDA_URL = "https://api.nal.usda.gov/fdc/v1/foods/search"

UNIT_CONVERSIONS = {
    "g": 1,
    "gram": 1,
    "grams": 1,
    "kg": 1000,
    "lb": 453.592,
    "lbs": 453.592,
    "cup": 240,
    "cups": 240,
    "tbsp": 15,
    "tablespoon": 15,
    "tablespoons": 15,
    "tsp": 5,
    "teaspoon": 5,
    "teaspoons": 5,
    "oz": 28.35,
    "taste": 0,
    "to taste": 0,
    "pinch": 0,
    "n/a": 0,
    "": 0
}

# Words that indicate the item is not raw produce
NOT_RAW_CUES = ["juice", "oil", "paste", "sauce", "canned", "dried", "roasted", "powder", "frozen"]

def maybe_append_raw(main_part):
    """
    If the string doesn't contain any known 'not-raw' cues
    and is only 1 or 2 words, append 'raw' for better USDA results.
    """
    if any(cue in main_part for cue in NOT_RAW_CUES):
        return main_part
    
    if len(main_part.split()) <= 2:  # e.g. "onion", "strawberries", etc.
        return main_part + " raw"
    
    return main_part

def clean_ingredient_name(ingredient_str):
    """
    Attempts to parse out parentheses, remove trailing descriptors after commas,
    and possibly append 'raw' if there's no known cue that it's not raw.
    """
    # Lowercase and strip
    raw_name_lower = ingredient_str.lower().strip()

    # Remove parenthetical text
    raw_name_lower = re.sub(r"\([^)]*\)", "", raw_name_lower).strip()  # remove (...) groups

    # Remove 'thinly sliced', etc. from end. We'll keep first chunk up to a comma.
    if "," in raw_name_lower:
        # Example: "chicken thighs, boneless, skinless" => "chicken thighs"
        # But skip if it explicitly says "boneless skinless" in the front
        # if you want to keep that detail. Otherwise you can remove them here.
        raw_name_lower = raw_name_lower.split(",")[0].strip()

    # Possibly add "raw" if it’s a short produce-like item.
    raw_name_lower = maybe_append_raw(raw_name_lower)
    
    return raw_name_lower

def parse_raw_ingredient_text(line):
    """
    Given a line like "1 tablespoon vegetable oil, or more to taste",
    this tries to parse out:
       quantity: float
       unit: string
       name: string (cleaned)
    If it fails, returns quantity=0, unit="n/a", name=line for fallback.
    """
    # Basic approach: split by spaces, look for the first token that is a number or fraction, 
    # then the next token that might be a unit, and the rest is the name.
    # e.g. "1 tablespoon vegetable oil..."
    # This is an approximation and can be improved with more advanced text parsing.

    # 1) Find the numeric portion (including fraction)
    match = re.match(r'^(\d+\s*\d*\/?\d*)\s+(.*)$', line.strip().lower())
    quantity_str = "0"
    rest = line.strip().lower()
    if match:
        quantity_str = match.group(1).strip()  # e.g. "1", or "1 1/2"
        rest = match.group(2).strip()          # e.g. "tablespoon vegetable oil, or more to taste"

    quantity_val = parse_amount(quantity_str)

    # 2) Extract possible unit from the start of 'rest'
    #    e.g. "tablespoon vegetable oil" => unit="tablespoon", then the name is "vegetable oil..."
    #    We only consider known units from UNIT_CONVERSIONS.
    tokens = rest.split()
    unit = ""
    name_tokens = []
    if tokens:
        # We might have multiple tokens for the unit (e.g. "tablespoons" is recognized, but "tbsp" is also recognized).
        # We'll scan up to the first token that doesn't match a known unit
        # or if we pass 2 tokens and still see a known unit.
        i = 0
        while i < len(tokens):
            # If we have an exact match to the unit conversions key, treat that as the unit
            candidate = re.sub(r'[^\w]', '', tokens[i])  # remove punctuation
            if candidate in UNIT_CONVERSIONS:
                unit = candidate
                i += 1
                break
            else:
                break

        # The rest is name
        name_tokens = tokens[i:]

    # Join name tokens back together
    raw_name = ' '.join(name_tokens)
    # Remove trailing fluff like "or more to taste", "or as needed", etc. 
    # Just do a simple cut at " or " or " to taste".
    raw_name = re.split(r'\bor\b|\bto taste\b|\bas needed\b', raw_name)[0].strip(",. ")

    if not raw_name:
        # fallback
        raw_name = rest

    # Final cleaning for USDA query
    cleaned_name = clean_ingredient_name(raw_name)
    return quantity_val, unit, cleaned_name

def parse_amount(amount_str):
    """
    Converts fraction-like strings "1/2", "1 1/2", etc. into floats.
    """
    amount_str = amount_str.strip()

    # Special cases
    if any(keyword in amount_str for keyword in ["taste", "pinch", "some", "n/a"]):
        return 0

    # "1 1/2" => "1.5"
    # If it has a space, split them and add up
    if " " in amount_str:
        parts = amount_str.split()
        total = 0
        for p in parts:
            total += parse_amount(p)
        return total

    # If it has a fraction slash
    if "/" in amount_str:
        try:
            num, denom = amount_str.split("/")
            return float(num) / float(denom)
        except:
            pass

    # Else fallback to float cast
    try:
        return float(amount_str)
    except:
        return 0

def get_grams(quantity, unit):
    unit = unit.lower().strip()
    return quantity * UNIT_CONVERSIONS.get(unit, 0)

def score_match(description):
    """
    We define a small scoring function so that items containing these
    preferred keywords appear on top.
    """
    desc = description.lower()
    preferred_keywords = ["raw", "cooked", "sliced", "chopped", "plain",
                          "boneless", "skinless", "fresh", "all", "usda"]
    return sum(1 for word in preferred_keywords if word in desc)

def pick_best_food(foods):
    """
    Filter out 'Branded' items, then pick the top-scoring item among the remainder.
    If everything is branded, pick the top-scoring item among all.
    """
    if not foods:
        return None
    non_branded = [f for f in foods if f.get("dataType", "") != "Branded"]
    if not non_branded:
        non_branded = foods
    sorted_foods = sorted(non_branded, key=lambda f: -score_match(f["description"]))
    return sorted_foods[0]

def get_usda_nutrition(full_ingredient_text):
    """
    Overall wrapper to parse the ingredient line into (quantity, unit, name),
    search USDA for the best match, compute macros, and return them.
    """
    quantity, unit, name = parse_raw_ingredient_text(full_ingredient_text)
    grams = get_grams(quantity, unit)

    # If there's no sensible weight (like "to taste" or "pinch"), return zero macros
    if grams <= 0:
        return {
            "ingredient": full_ingredient_text,
            "calories": 0,
            "protein": 0,
            "carbs": 0,
            "fats": 0
        }

    # Query USDA
    params = {
        "api_key": USDA_API_KEY,
        "query": name,
        # Use a narrower data set to avoid random brand results. 
        # E.g. "Survey (FNDDS)", "Foundation", "SR Legacy"
        "dataType": ["Survey (FNDDS)", "Foundation", "SR Legacy"],
        "pageSize": 10
    }
    response = requests.get(USDA_URL, params=params)

    if response.status_code == 200:
        data = response.json()
        foods = data.get("foods", [])
        if foods:
            # Pick best match
            best = pick_best_food(foods)
            if best:
                nutrients = {}
                for n in best.get("foodNutrients", []):
                    # Example keys: "Protein", "Energy", "Carbohydrate, by difference", "Total lipid (fat)"
                    nutrients[n["nutrientName"]] = n["value"]

                cals = nutrients.get("Energy", 0)
                protein = nutrients.get("Protein", 0)
                carbs = nutrients.get("Carbohydrate, by difference", 0)
                fat = nutrients.get("Total lipid (fat)", 0)

                factor = grams / 100.0
                return {
                    "ingredient": full_ingredient_text,
                    "calories": round(cals * factor, 2),
                    "protein": round(protein * factor, 2),
                    "carbs": round(carbs * factor, 2),
                    "fats": round(fat * factor, 2)
                }
    # Fallback
    return {
        "ingredient": full_ingredient_text,
        "calories": 0,
        "protein": 0,
        "carbs": 0,
        "fats": 0
    }

def calculate_recipe_macros(ingredient_list):
    """
    Takes a list of strings, each describing an ingredient (e.g. '1 cup chopped onions'),
    returns a list of dicts with nutrient info.
    """
    results = []
    for line in ingredient_list:
        info = get_usda_nutrition(line)
        results.append(info)
    return results

def summarize_macros(results):
    """Aggregate the macros from all ingredients into a single total."""
    total = {"calories": 0, "protein": 0, "carbs": 0, "fats": 0}
    for item in results:
        total["calories"] += item["calories"]
        total["protein"] += item["protein"]
        total["carbs"] += item["carbs"]
        total["fats"] += item["fats"]
    # Round them or keep as is
    total = {k: round(v, 2) for k, v in total.items()}
    return total

def main():
    # Example usage:
    ingredients = [
    "2 tsp Chili Powder",
    "1 tsp Ground Cumin",
    "1/2 tsp Dired Oregano",
    "To Taste tsp Salt",
    "1 1/2 lb boneless, skinless chicken thighs",
    "1 tbsp canola oil",
    "12 mini flour tortillas, warmed",
    "1 cup pico de gallo, homemade or store-bought",
    "1  avocado, halved, peeled, seeded and diced",
    "1 cup chopped fresh cilantro leaves",
    "1 lime, cut into wedges"
    ]

    nutrition_info = calculate_recipe_macros(ingredients)
    total_macros = summarize_macros(nutrition_info)

    print("📊 Individual Ingredient Nutrition:")
    for item in nutrition_info:
        print(json.dumps(item, indent=2))

    print("\n🍽️ Total Meal Nutrition:")
    print(json.dumps(total_macros, indent=2))

if __name__ == "__main__":
    if sys.stdin.isatty():
        # Run the example
        main()
    else:
        input_data = sys.stdin.read()
        ingredient_list = json.loads(input_data)  # Expecting a list of strings
        nutrition_info = calculate_recipe_macros(ingredient_list)
        print(json.dumps(nutrition_info))
