import pytesseract
from PIL import Image
import re
import cv2

pytesseract.pytesseract.tesseract_cmd = r'C:\Program Files\Tesseract-OCR\tesseract.exe'


def preprocess_image(image_path):
    # Load the image
    img = cv2.imread(image_path)

    # Convert the image to grayscale
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    # Apply GaussianBlur to remove noise
    blurred = cv2.GaussianBlur(gray, (5, 5), 0)

    # Use adaptive thresholding to make text clearer
    thresh = cv2.adaptiveThreshold(blurred, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 11, 2)

    # Apply dilation and erosion to remove noise
    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (1, 1))
    dilated = cv2.dilate(thresh, kernel, iterations=1)
    processed_image_path = 'processed_image.png'

    # Save the preprocessed image
    cv2.imwrite(processed_image_path, dilated)

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
    print(text)
    cleaned_text = clean_ocr_text(text)
    return cleaned_text


# Function to extract food items and prices from OCR result
def extract_items_and_prices(ocr_text):
    # Regex to match item names and prices (assumed to be in the format "item price")
    item_price_pattern = r"([a-zA-Z\s]+)\s+(\d+\.\d{2})"
    items = re.findall(item_price_pattern, ocr_text)
    return items


# Main function to process receipt image
def process_receipt(image_path):
    ocr_text = extract_text_from_image(image_path)
    items_and_prices = extract_items_and_prices(ocr_text)


if __name__ == "__main__":
    # Path to your receipt image
    receipt_image_path = 'test2.jpg'

    process_receipt(receipt_image_path)
