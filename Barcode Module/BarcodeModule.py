
from pyzbar.pyzbar import decode
from PIL import Image
import openfoodfacts
import sys

# Initialize Open Food Facts API
api = openfoodfacts.API(user_agent="MyAwesomeApp/1.0")


def decode_barcode(image_path):
    try:
        # Open the image file
        image = Image.open(image_path)

        # Decode the image
        decoded_objects = decode(image)

        if not decoded_objects:
            print("No barcode found.")
            return

        # Iterate over each detected barcode
        for obj in decoded_objects:
            barcode = obj.data.decode("utf-8")
            print(f"Found Barcode: {barcode}")

            # Fetch product information from Open Food Facts
            product_info = api.product.get(barcode, fields=["code", "product_name"])

            if product_info and "product_name" in product_info:
                product_name = product_info.get("product_name", "Unknown product")
                print(f'Barcode: {barcode}\nProduct: {product_name}')
            else:
                print(f'Barcode: {barcode}\nProduct not found.')

    except Exception as e:
        print(f"Error: {str(e)}")


# Entry point when the script is run
if __name__ == "__main__":
    # Ensure an image path is passed as a command-line argument
    if len(sys.argv) < 2:
        print("Usage: python BarcodeModule.py <image_path>")
    else:
        image_path = sys.argv[1]  # The image path is passed as the first argument
        decode_barcode(image_path)
