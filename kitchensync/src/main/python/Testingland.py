from PIL import Image
import pytesseract
import os

pytesseract.pytesseract.tesseract_cmd = r'C:\Program Files\Tesseract-OCR\tesseract.exe'

# Define the relative path to the resources folder
resources_folder = os.path.join(os.path.dirname(__file__), '../resources')

# Path to a specific receipt image in the resources folder
receipt_image_path = os.path.join(resources_folder, 'test4.png')

img = Image.open(receipt_image_path)

custom_config = r'--oem 3 --psm 6'
text = pytesseract.image_to_string(Image.open(receipt_image_path), config=custom_config)

# Use Tesseract to extract text from the image
#text = pytesseract.image_to_string(img)

# Output the extracted text
print(text)
