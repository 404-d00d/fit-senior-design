import pytesseract
from PIL import Image
import re
import cv2
import os

pytesseract.pytesseract.tesseract_cmd = r'C:\Program Files\Tesseract-OCR\tesseract.exe'


def preprocess_image(image_path):
    # Load the image
    img = cv2.imread(image_path)

    image = cv2.resize(img, (0, 0), fx=3, fy=3)

    # Convert the image to grayscale
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    # Apply GaussianBlur to remove noise
    blurred = cv2.GaussianBlur(gray, (3, 3), 0)

    # Use thresholding to make text clearer
    thresh = cv2.threshold(blurred, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)[1]

    # Morph open to remove noise and invert image
    # Morph open to remove noise and invert image
    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (3, 3))
    opening = cv2.morphologyEx(thresh, cv2.MORPH_OPEN, kernel, iterations=1)
    invert = 255 - opening

    # dilated = cv2.dilate(thresh, kernel, iterations=1)

    # Increase size for better OCR accuracy
    # resized = cv2.resize(dilated, None, fx=3, fy=3, interpolation=cv2.INTER_LINEAR)

    processed_image_path = 'processed_image.png'

    # Save the preprocessed image
    cv2.imwrite(processed_image_path, invert)

    return processed_image_path


def clean_ocr_text(ocr_text):
    clean_text = re.sub(r'[^\x00-\x7F]+', ' ', ocr_text)
    clean_text = re.sub(r'\s+', ' ', clean_text).strip()
    return clean_text


# Function to extract text from image using Tesseract OCR
def extract_text_from_image(image_path):
    processed_image_path = preprocess_image(image_path)
    custom_config = r'--oem 3 --psm 6'
    text = pytesseract.image_to_string(Image.open(processed_image_path), config=custom_config)
    cleaned_text = clean_ocr_text(text)
    return cleaned_text


# Walmart: Extracting product id and price
def extract_walmart_items(ocr_text):
    # Adjusted pattern to only capture id and price
    walmart_pattern = r"(\d{10,12})[A-Z]*\s+[^\d]*?\s*(\d+\s*\.\s*\d{2})"
    items = []

    walmart_matches = re.findall(walmart_pattern, ocr_text, re.DOTALL)
    for match in walmart_matches:
        item_id, price = match
        items.append((item_id.strip(), price.strip()))  # Return as tuple (id, price)

    return items


# Target: Extracting product id and price
def extract_target_items(ocr_text):
    # Adjusted pattern to only capture id and price
    target_pattern = r"(\d{9,12})\s+[^\d]*?\s*(\d+\s*\.\s*\d{2})"
    items = []

    target_matches = re.findall(target_pattern, ocr_text, re.DOTALL)
    for match in target_matches:
        item_id, price = match
        items.append((item_id.strip(), price.strip()))  # Return as tuple (id, price)

    return items


# Main function to process receipt image and determine store
def process_receipt(image_path, store):
    # Extract text using Tesseract
    ocr_text = extract_text_from_image(image_path)

    print(ocr_text)

    if store.lower() == "walmart":
        return extract_walmart_items(ocr_text)
    elif store.lower() == "target":
        return extract_target_items(ocr_text)
    else:
        print("Unsupported store format.")
        return []


def basic_process(image_path):
    # Use Tesseract to extract text from the image
    custom_config = r'--oem 3 --psm 6'
    text = pytesseract.image_to_string(Image.open(image_path), config=custom_config)
    print(text)


if __name__ == "__main__":
    # Define the relative path to the resources folder
    resources_folder = os.path.join(os.path.dirname(__file__), '')
    # Path to a specific receipt image in the resources folder
    # receipt_image_path = os.path.join(resources_folder, 'Target.jpg')

    # process_receipt(receipt_image_path)
    # basic_process(receipt_image_path)

    walmart_receipt_path = os.path.join(resources_folder, 'test.jpg')
    walmart_items = process_receipt(walmart_receipt_path, 'Walmart')
    print("Walmart Items:", walmart_items)
    print(len(walmart_items))

    target_receipt_path = os.path.join(resources_folder, 'test4.png')
    target_items = process_receipt(target_receipt_path, 'Target')
    print("Target Items:", target_items)
    print(len(target_items))
