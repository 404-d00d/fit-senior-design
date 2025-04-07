import requests
import json
import sys
import os

# 🔹 USDA API Key
USDA_API_KEY = "l43si2gPPVmHMLavt4w78UjeMNK8RDahOPexf0NM"
USDA_URL = "https://api.nal.usda.gov/fdc/v1/foods/search"

UNIT_CONVERSIONS = {
    "g": 1,
    "kg": 1000,
    "lb": 453.592,
    "cup": 240,
    "tbsp": 15,
    "tsp": 5,
    "oz": 28.35,
    "taste": 0,
    "to taste": 0,
    "pinch": 0,
    "n/a": 0,
    "": 0
}

def get_usda_nutrition(ingredient, quantity, unit):
    """Fetch nutritional data from USDA FoodData Central"""
    grams = get_grams(quantity, unit)
    if grams == 0:
        return {"ingredient": ingredient, "calories": 0, "protein": 0, "carbs": 0, "fats": 0}

    params = {"query": ingredient, "api_key": USDA_API_KEY}
    response = requests.get(USDA_URL, params=params)

    if response.status_code == 200:
        data = response.json()
        if "foods" in data and len(data["foods"]) > 0:
            food = data["foods"][0]
            nutrients = {n["nutrientName"]: n["value"] for n in food["foodNutrients"]}

            calories = nutrients.get("Energy", 0)
            protein = nutrients.get("Protein", 0)
            carbs = nutrients.get("Carbohydrate, by difference", 0)
            fats = nutrients.get("Total lipid (fat)", 0)

            factor = grams / 100
            return {
                "ingredient": ingredient,
                "calories": round(calories * factor, 2),
                "protein": round(protein * factor, 2),
                "carbs": round(carbs * factor, 2),
                "fats": round(fats * factor, 2)
            }

    return {"ingredient": ingredient, "error": "Not Found"}

def calculate_recipe_macros(ingredients):
    """Calculates macros for a list of ingredients"""
    results = []
    for item in ingredients:
        name = item.get("name")
        amount = item.get("quantity", 0)
        unit = item.get("unit", "g")

        # Pass raw values to get_usda_nutrition, not pre-converted
        nutrition = get_usda_nutrition(name, amount, unit)
        if nutrition is None:
            nutrition = {"ingredient": name, "error": "Not Found"}

        results.append(nutrition)
    return results


def parse_amount(amount_str):
    amount_str = amount_str.strip().lower()

    # Special case for "to taste", "some", etc.
    if any(word in amount_str for word in ["taste", "pinch", "some", "n/a"]):
        return 0

    try:
        # Handle mixed numbers (e.g., "1 1/2")
        if " " in amount_str:
            parts = amount_str.split(" ")
            return parse_amount(parts[0]) + parse_amount(parts[1])

        # Handle fractions (e.g., "1/2")
        if "/" in amount_str:
            num, denom = amount_str.split("/")
            return float(num) / float(denom)

        return float(amount_str)
    except:
        print(f"Could not parse amount: {amount_str}")
        return 0

def summarize_macros(results):
    """Sums up the macro values from each ingredient"""
    total = {"calories": 0, "protein": 0, "carbs": 0, "fats": 0}
    for item in results:
        if "error" in item:
            continue
        total["calories"] += item["calories"]
        total["protein"] += item["protein"]
        total["carbs"] += item["carbs"]
        total["fats"] += item["fats"]
    return total

def load_needed_ingredients(filepath="lists.json"):
    """Loads 'Needed Ingredients' from lists.json"""
    if not os.path.exists(filepath):
        print(f"File not found: {filepath}")
        return []
    with open(filepath, "r") as f:
        data = json.load(f)
        return data.get("Needed Ingredients", [])
    
def main():
    ingredients = load_needed_ingredients()
    print(f"🔍 Found {len(ingredients)} needed ingredients...\n")

    nutrition_info = calculate_recipe_macros(ingredients)
    total_macros = summarize_macros(nutrition_info)

    print("📊 Individual Ingredient Nutrition:")
    for item in nutrition_info:
        print(json.dumps(item, indent=2))

    print("\n🍽️ Total Meal Nutrition:")
    print(json.dumps(total_macros, indent=2))

def get_grams(quantity, unit):
    unit = unit.strip().lower()
    return quantity * UNIT_CONVERSIONS.get(unit, 0)
    


if __name__ == "__main__":
    # Allow command line usage OR integration from Java
    if sys.stdin.isatty():
        main()
    else:
        # Handle input from Java STDIN
        input_data = sys.stdin.read()
        ingredients = json.loads(input_data)
        nutrition_info = calculate_recipe_macros(ingredients)
        print(json.dumps(nutrition_info))
