import openfoodfacts
import sys

# Initialize Open Food Facts API
api = openfoodfacts.API(user_agent="KitchenSync/1.0")

def get_product_details(upc_code):
    try:
        # Fetch product information from Open Food Facts
        product_info = api.product.get(upc_code, fields=["code", "product_name"])

        if product_info and "product_name" in product_info:
            return {
                "code": product_info.get("code"),
                "product_name": product_info.get("product_name")
            }
        else:
            return None

    except Exception as e:
        print(f"Error: {str(e)}")
        return None

# Entry point when the script is run
if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python get_product_details.py <upc_code>")
    else:
        upc_code = sys.argv[1]
        product = get_product_details(upc_code)
        if product:
            print(f'Product found: {product["product_name"]}')
        else:
            print("No product information found, please enter manually.")
