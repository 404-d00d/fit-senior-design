import os
import sys
from pyzbar.pyzbar import decode
from PIL import Image
import openfoodfacts
import json

api = openfoodfacts.API(user_agent="MyAwesomeApp/1.0")

def decode_barcode(image_path):
    response = {
        "status": "error",
        "results": []
    }
    try:
        if not os.path.exists(image_path):
            response["results"].append({
                "status": "error",
                "message": f"The file path '{image_path}' does not exist."
            })
            print(json.dumps(response))
            return

        # Open the image file
        image = Image.open(image_path)

        # Decode the image
        decoded_objects = decode(image)

        if not decoded_objects:
            response["results"].append({
                "status": "error",
                "message": "No barcode found."
            })
            print(json.dumps(response))
            return

        # Iterate over each detected barcode
        for obj in decoded_objects:
            barcode = obj.data.decode("utf-8")
            product_info = api.product.get(barcode, fields=["code", "product_name", "quantity"])

            if product_info and "product_name" in product_info:
                product_name = product_info.get("product_name", "Unknown product")
                quantity_info = product_info.get("quantity", "1 unit")
                response["results"].append({
                    "status": "success",
                    "barcode": barcode,
                    "product_name": product_name,
                    "quantity": quantity_info
                })
            else:
                response["results"].append({
                    "status": "not_found",
                    "barcode": barcode,
                    "message": "Product not found."
                })

        response["status"] = "success"

    except Exception as e:
        response["results"].append({
            "status": "error",
            "message": str(e)
        })

    # Print the JSON response
    print(json.dumps(response))


# Entry point when the script is run
if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(json.dumps({
            "status": "error",
            "message": "Usage: python BarcodeModule.py <image_path>"
        }))
    else:
        image_path = sys.argv[1]
        decode_barcode(image_path)
