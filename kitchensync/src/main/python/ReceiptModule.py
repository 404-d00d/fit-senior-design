import pytesseract
from PIL import Image
import re
import cv2
import os
import json
import sys

pytesseract.pytesseract.tesseract_cmd = r'C:\Program Files\Tesseract-OCR\tesseract.exe'

# Preprocess image for better OCR accuracy
def preprocess_image(image_path):
    img = cv2.imread(image_path)
    if img is None:
        raise FileNotFoundError(f"Unable to load image: {image_path}")

    # Resize for better OCR accuracy
    img = cv2.resize(img, (0, 0), fx=2, fy=2)

    # Convert to grayscale
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    # Apply Gaussian Blur to reduce noise
    blurred = cv2.GaussianBlur(gray, (5, 5), 0)

    # Apply adaptive thresholding for better contrast
    thresh = cv2.adaptiveThreshold(
        blurred, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 11, 2
    )

    # Apply morphological operations to clean up the image
    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (3, 3))
    cleaned = cv2.morphologyEx(thresh, cv2.MORPH_CLOSE, kernel)

    return cleaned

# Extract text from image
def extract_text(image_path):
    processed_image = preprocess_image(image_path)
    custom_config = r'--oem 3 --psm 6 -c tessedit_char_whitelist=0123456789.$ABCDEFGHIJKLMNOPQRSTUVWXYZ'
    text = pytesseract.image_to_string(processed_image, config=custom_config)
    return text

# Extract items (UPC codes)
def extract_items(ocr_text):
    item_pattern = r"(\d{8,13})"  # Extract only UPC codes (8 to 13 digits)
    items = re.findall(item_pattern, ocr_text)
    return items

# Process receipts
def process_receipt(image_path):
    response = {
        "status": "error",
        "upc_codes": []
    }

    try:
        # Extract raw text
        raw_text = extract_text(image_path)

        # Extract items (UPC codes)
        extracted_items = extract_items(raw_text)

        if extracted_items:
            response["status"] = "success"
            response["upc_codes"] = extracted_items
        else:
            response["message"] = "No UPC codes found."

    except Exception as e:
        response["message"] = str(e)

    # Print the response as JSON
    print(json.dumps(response))

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(json.dumps({
            "status": "error",
            "message": "Usage: python ReceiptModule.py <image_path>"
        }))
    else:
        image_path = sys.argv[1]
        process_receipt(image_path)
