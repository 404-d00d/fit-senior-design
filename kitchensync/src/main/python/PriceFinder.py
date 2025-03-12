import requests
from bs4 import BeautifulSoup

# get avg price of ingredients, price may change because of stores, deals, coupons, other factors, etc.


url="https://www.walmart.com/search?q="

headers = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36",
    "Accept-Language": "en-US,en;q=0.9",
}

product = "cheese"

# scrapes the url provided to get product prices and price per unit.
# returns price per oz, can multiply the unit to get price per pound.
def getWalmartHTML(url, product, page):

    # creates formatting for the url that will be scraped
    product = product.replace(" ", "+")    
    newPage = "&page="
    pageNumber = str(page)

    response = requests.get(url+str(product)+newPage+pageNumber, headers=headers)

    # Check if the request was successful
    if response.status_code == 200:
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
            print(unitPrice, "per", unit)
            avgPriceOz += unitPrice
        # price is dollars per oz
        avgPriceOz /= len(pricePerUnit)
        print(avgPriceOz, "cents per oz")
    

        print()
        for y in range(len(titles)):
            print("PRODUCT:")
            print(titles[y].get_text())
            print(prices[y][2])
            print(pricePerUnit[y].get_text())
            print()

        return avgPriceOz, "oz"


        # Print the extracted titles
        # for title in titles:
        #     print(title.get_text(strip=True))
    else:
        print("Failed to retrieve the webpage")

avgPrice, units = getWalmartHTML(url, product, 1)

print("Price for", product, "is ", avgPrice, "cents per", units)
print("Or ", avgPrice*16, "cents per lb")