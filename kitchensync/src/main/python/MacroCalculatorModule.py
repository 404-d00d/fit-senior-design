import requests
import json
import sys

# 🔹 USDA API Key
USDA_API_KEY = "l43si2gPPVmHMLavt4w78UjeMNK8RDahOPexf0NM"
USDA_URL = "https://api.nal.usda.gov/fdc/v1/foods/search"

def get_usda_nutrition(ingredient, amount):
    """Fetches nutritional data from USDA FoodData Central"""
    params = {"query": ingredient, "api_key": USDA_API_KEY}
    response = requests.get(USDA_URL, params=params)

    if response.status_code == 200:
        data = response.json()
        if "foods" in data and len(data["foods"]) > 0:
            food = data["foods"][0]  # Take the first result
            nutrients = {nutrient["nutrientName"]: nutrient["value"] for nutrient in food["foodNutrients"]}

            # Extract macros (values per 100g)
            calories = nutrients.get("Energy", 0)
            protein = nutrients.get("Protein", 0)
            carbs = nutrients.get("Carbohydrate, by difference", 0)
            fats = nutrients.get("Total lipid (fat)", 0)

            # Adjust based on actual amount used
            factor = amount / 100
            return {
                "ingredient": ingredient,
                "calories": round(calories * factor, 2),
                "protein": round(protein * factor, 2),
                "carbs": round(carbs * factor, 2),
                "fats": round(fats * factor, 2)
            }
    return None  # Return None if data not found

def calculate_recipe_macros(ingredients):
    """Calculates macros for a list of ingredients"""
    results = []
    for item in ingredients:
        name, amount = item["name"], item["amount"]

        # 1 Try fetching from USDA
        nutrition = get_usda_nutrition(name, amount)

        # 2 If still not found, mark as missing
        if nutrition is None:
            nutrition = {"ingredient": name, "error": "Not Found"}

        results.append(nutrition)

    return results

if __name__ == "__main__":
    # 🔹 Read JSON input from Java (via command-line args)
    input_data = sys.stdin.read()
    ingredients = json.loads(input_data)

    # 🔹 Run Calculation
    nutrition_info = calculate_recipe_macros(ingredients)

    # 🔹 Print JSON Output (for Java Integration)
    print(json.dumps(nutrition_info))
