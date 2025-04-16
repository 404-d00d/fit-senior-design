import sys
import json
import cv2
import numpy as np
import re
import pytesseract
from PIL import Image
import easyocr

# If Tesseract isn't in your PATH, specify its full path if you still want to compare:
pytesseract.pytesseract.tesseract_cmd = r'C:\Program Files\Tesseract-OCR\tesseract.exe'

def deskew_image(image):
    """
    Attempt to deskew the image using its largest text contours or edges.
    1) Convert to grayscale
    2) Threshold
    3) Find the largest contour or use Hough lines
    4) Estimate skew angle
    5) Rotate to correct
    """
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    # Simple threshold to find text regions
    thresh = cv2.threshold(gray, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)[1]

    # Get coordinates of all non-zero pixels
    coords = np.column_stack(np.where(thresh > 0))
    # Use cv2.minAreaRect() to find a bounding box that might define skew angle
    angle = cv2.minAreaRect(coords)[-1]

    # The 'minAreaRect' angle can be from -90 to 0; we fix it if needed:
    if angle < -45:
        angle = -(90 + angle)
    else:
        angle = -angle

    # Rotate the image to deskew
    (h, w) = image.shape[:2]
    center = (w // 2, h // 2)
    M = cv2.getRotationMatrix2D(center, angle, 1.0)
    rotated = cv2.warpAffine(
        image, M, (w, h), flags=cv2.INTER_CUBIC, borderMode=cv2.BORDER_REPLICATE
    )
    return rotated


def advanced_preprocess_image(image_path):
    """
    1) Load image
    2) Deskew it
    3) Optionally apply additional threshold / morphological ops
    Return the processed image (as an OpenCV array).
    """
    img = cv2.imread(image_path)
    if img is None:
        raise FileNotFoundError(f"Unable to load image: {image_path}")

    # Deskew the image
    deskewed = deskew_image(img)

    # Convert to grayscale
    gray = cv2.cvtColor(deskewed, cv2.COLOR_BGR2GRAY)

    # Slight Gaussian blur
    blurred = cv2.GaussianBlur(gray, (3, 3), 0)

    # You could do adaptive threshold or keep it gray for EasyOCR.
    # EasyOCR can handle grayscale or color images directly.
    # But if you want to keep Tesseract in the loop, we can threshold.
    # Let's skip threshold for EasyOCR (since it does its own).
    return blurred

def easyocr_extract_text(image_path, language_list=['en']):
    """
    Use EasyOCR to extract text. Return a single string or list of lines.

    :param language_list: e.g. ['en'] for English; add more if receipts have multiple languages.
    """
    # Initialize the EasyOCR Reader once. In a production setting,
    # you might keep this Reader object in memory for multiple calls.
    reader = easyocr.Reader(language_list, gpu=False)  # gpu=True if you have CUDA set up

    processed_img = advanced_preprocess_image(image_path)

    # Convert to RGB for EasyOCR if needed (it can handle grayscale, but let's ensure).
    processed_img_rgb = cv2.cvtColor(processed_img, cv2.COLOR_GRAY2RGB)

    # Actually perform OCR
    results = reader.readtext(processed_img_rgb, detail=1)  
    # results is a list of tuples: [(bbox, text, confidence), (bbox, text, confidence), ...]

    # Let's build a single text block or list of lines
    lines = []
    for (bbox, text, conf) in results:
        lines.append(text)

    # Optionally return lines joined by newline
    return "\n".join(lines)


def parse_line_for_details(line):
    """
    Return { 'item_name': ..., 'upc': ..., 'price': ... }
    """
    # UPC: 8-13 digits
    upc_match = re.search(r"\b(\d{8,13})\b", line)
    upc = upc_match.group(1) if upc_match else None

    # Price: naive pattern: $1.23 or 1.23
    price_match = re.search(r"\$?\d+\.\d{1,2}", line)
    price = price_match.group(0) if price_match else None

    temp_line = line
    if upc:
        temp_line = temp_line.replace(upc, "")
    if price:
        temp_line = temp_line.replace(price, "")

    # Remaining text is item name (roughly)
    item_name = temp_line.strip(" \t-._:;")

    return {
        "item_name": item_name,
        "upc": upc,
        "price": price
    }

def process_receipt(image_path):
    """
    Use advanced preprocessing + EasyOCR to read text,
    parse each line, and collect item_name, upc, price.
    
    Return a list of dicts for each line: 
      [{ "item_name": ..., "upc": ..., "price": ... }, ...]
    """
    lines_data = []
    try:
        # Extract text (joined by newline)
        raw_text = easyocr_extract_text(image_path)
        # Split into lines
        lines = raw_text.splitlines()

        for line in lines:
            # Parse for details
            details = parse_line_for_details(line)
            lines_data.append(details)

    except Exception as e:
        print(f"ERROR in process_receipt: {e}")
    
    return lines_data

if __name__ == "__main__":
    if sys.stdin.isatty():
        image_path = r"src\main\resources\org\javafx\Resources\Test Receipts\Target.jpg"
        items = process_receipt(image_path)

        print("----- RECOGNIZED LINES -----")
        for i, item in enumerate(items, start=1):
            print(f"{i}. Name: {item['item_name'] or 'N/A'}, "
                f"UPC: {item['upc'] or 'N/A'}, "
                f"Price: {item['price'] or 'N/A'}")


    else:
        image_path = sys.argv[1]
        process_receipt(image_path)
