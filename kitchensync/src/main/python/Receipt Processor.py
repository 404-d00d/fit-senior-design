import pytesseract
from PIL import Image
import re
import cv2
import os

pytesseract.pytesseract.tesseract_cmd = r'C:\Program Files\Tesseract-OCR\tesseract.exe'

# Pre-process image for OCR
def preprocess_image(image_path):
    img = cv2.imread(image_path)
    img_resized = cv2.resize(img, (0, 0), fx=3, fy=3)
    gray = cv2.cvtColor(img_resized, cv2.COLOR_BGR2GRAY)
    blurred = cv2.GaussianBlur(gray, (3, 3), 0)
    thresh = cv2.threshold(blurred, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)[1]
    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (3, 3))
    opening = cv2.morphologyEx(thresh, cv2.MORPH_OPEN, kernel, iterations=1)
    invert = 255 - opening
    processed_image_path = 'processed_image.png'
    cv2.imwrite(processed_image_path, invert)
    return processed_image_path

# Extract text from image using Tesseract OCR
def extract_text_from_image(image_path):
    processed_image_path = preprocess_image(image_path)
    custom_config = r'--oem 3 --psm 6'
    text = pytesseract.image_to_string(Image.open(processed_image_path), config=custom_config)
    return text

# Main function to process receipt
def process_receipt(image_path, store):
    ocr_text = extract_text_from_image(image_path)
    items = []

    if store.lower() == "walmart":
        walmart_pattern = r"(\d{10,12})[A-Z]*\s+[^\d]*?\s*(\d+\s*\.\s*\d{2})"
        walmart_matches = re.findall(walmart_pattern, ocr_text, re.DOTALL)
        items = [(match[0].strip(), match[1].strip()) for match in walmart_matches]

    elif store.lower() == "target":
        target_pattern = r"(\d{9,12})\s+[^\d]*?\s*(\d+\s*\.\s*\d{2})"
        target_matches = re.findall(target_pattern, ocr_text, re.DOTALL)
        items = [(match[0].strip(), match[1].strip()) for match in target_matches]

    else:
        print("Unsupported store format.")

    return items

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python receipt_processor.py <image_path> <store>")
    else:
        image_path = sys.argv[1]
        store = sys.argv[2]
        items = process_receipt(image_path, store)
        if items:
            for item in items:
                print(f'Product ID: {item[0]}, Price: {item[1]}')
        else:
            print("No items found.")
