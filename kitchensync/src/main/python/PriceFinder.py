import requests
from bs4 import BeautifulSoup
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.webdriver import Options
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import time
from webdriver_manager.chrome import ChromeDriverManager
import atexit
import sys


# get avg price of ingredients, price may change because of stores, deals, coupons, other factors, etc.




headers = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36",
    "Accept-Language": "en-US,en;q=0.9",
}

product = "orange juice"

def convertToLb(price):
    return price*16

def convertToKg(price):
    return price*16*0.4536

def convertToG(price):
    return (price/1000)*16*0.4536

# liquid densities are not exact - just an approximation
def convertToL(price):
    return 1000*(price/1.043)/29.6

def convertToMl(price):
    return (price/1.043)/29.6

def getWalmartHTMLselenium(product, page):
    url = f"https://www.walmart.com/search?q={product.replace(' ', '+')}&page={page}"

    chrome_options = Options()
    chrome_options.add_argument("--headless=new")
    chrome_options.add_argument("--disable-blink-features=AutomationControlled")
    chrome_options.add_argument("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
    driver = webdriver.Chrome(options=chrome_options)

    try:
        driver.get(url)
        time.sleep(3)  # allow JavaScript to render
        soup = BeautifulSoup(driver.page_source, 'html.parser')

        # now do your usual price extraction here...
        return soup
    finally:
        driver.quit()

# scrapes the url provided to get product prices and price per unit.
# returns price per oz, can multiply the unit to get price per pound.
def getWalmartHTML(product, page):

    url="https://www.walmart.com/search?q="

    # creates formatting for the url that will be scraped
    product = product.replace(" ", "+")    
    newPage = "&page="
    pageNumber = str(page)

    response = requests.get(url+str(product)+newPage+pageNumber, headers=headers)
    #response = requests.getWalmartHTMLselenium(product, page)

    # Check if the request was successful
    if response.status_code == 200:

        # print("==== RAW HTML SAMPLE ====")
        # print(response.text[:1000])

        # Parse the HTML content using BeautifulSoup
        soup = BeautifulSoup(response.text, 'html.parser')

        # scrape data from walmart search page url based on its html structure
        productData = soup.find_all("div", {"data-testid": "item-stack"})
        for product in productData:
            titles = product.find_all("span", {"data-automation-id": "product-title"})
            prices = product.find_all("div", {"data-automation-id": "product-price"})
            pricePerUnit = product.find_all("div", class_="gray mr1 f6 f5-l flex items-end mt1")

            for x in range(len(prices)):
                price = prices[x].get_text().strip()
                priceList = price.split("$")                 
                #print(priceList)
                prices[x] = priceList
                #print([ord(char) for char in prices[x].get_text()]) 

        avgPriceOz = 0
        for x in range(len(pricePerUnit)):
            pPU = pricePerUnit[x].get_text()
            if "¢" in pPU:
                print("CENT IS PRESENT")
                pPU = pPU.split("¢")
                unit = pPU[1][1:]
                unitPrice = float(pPU[0][:-1])
            elif "$" in pPU:
                print("DOLLAR IS PRESENT")
                pPU = pPU.split("/")
                unit = pPU[1]
                unitPrice = float(pPU[0][1:])
            # 16 oz = 1 lb, need to standardize units across all products to ounces
            if unit == "lb":
                unitPrice /= 16
                unit = "oz"
            elif unit == "fl oz":
                # for liquids roughly similar to water - again prices vary so no need to get exact values.
                unitPrice *= 1.043
                unit = "oz"
            print(unitPrice, "per", unit)
            avgPriceOz += unitPrice
        # price is dollars per oz
        if len(pricePerUnit) != 0:
            avgPriceOz /= len(pricePerUnit)
        print(avgPriceOz, "cents per oz")
    

        # print()
        # for y in range(len(pricePerUnit)):
        #     print("PRODUCT:")
        #     print(titles[y].get_text())
        #     print(prices[y][2])
        #     print(pricePerUnit[y].get_text())
        #     print()

        return avgPriceOz, "oz"


        # Print the extracted titles
        # for title in titles:
        #     print(title.get_text(strip=True))
    else:
        print("Failed to retrieve the webpage")

# avgPrice, units = getWalmartHTML(product, 1)

# print("Price for", product, "is ", avgPrice, "cents per", units)
# print("Or ", avgPrice*16, "cents per lb")

if __name__ == "__main__":
    import sys

    if len(sys.argv) < 3:
        print("Usage: python PriceFinder.py <method> <product name> [page]")
        sys.exit(1)

    method = sys.argv[1]
    product = sys.argv[2]
    page = int(sys.argv[3]) if len(sys.argv) > 3 else 1

    try:
        if method == "walmart":
            avgPrice, unit = getWalmartHTML(product, page)
            print(f"{avgPrice},{unit}")

        elif method == "toLb":
            price = float(product)
            print(convertToLb(price))

        elif method == "toKg":
            price = float(product)
            print(convertToKg(price))

        elif method == "toG":
            price = float(product)
            print(convertToG(price))

        elif method == "toL":
            price = float(product)
            print(convertToL(price))

        elif method == "toMl":
            price = float(product)
            print(convertToMl(price))

        else:
            print("Unknown method:", method)
            sys.exit(3)

    except Exception as e:
        print(f"ERROR: {str(e)}")
        sys.exit(2)
        