import requests
from bs4 import BeautifulSoup


url="https://www.walmart.com/search?q="

headers = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36",
    "Accept-Language": "en-US,en;q=0.9",
}

product = "pepperoni"

# scrapes the url provided to get product prices and price per unit.
def getHTML(url, product):
    # feed text directly via app instead of console
    #product = input("Item to search: ")
    

    response = requests.get(url+str(product), headers=headers)

    # Check if the request was successful
    if response.status_code == 200:
        # Parse the HTML content using BeautifulSoup
        soup = BeautifulSoup(response.text, 'html.parser')

        # Extracting all titles (h1, h2, h3, etc.)
        #titles = soup.find_all(['h1', 'h2', 'h3'])
        #productList = soup.find("div", {"data-testid": "list-view"})
        #productData = productList.find_all("div", class_="flex")
        productData = soup.find_all("div", class_="flex flex-wrap w-100 flex-grow-0 flex-shrink-0 ph2 pr0-xl pl4-xl mt0-xl")
        #productData = soup.find_all("div", {"data-testid": "itemContainer"})

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


        for y in range(len(titles)):
            print(titles[y].get_text())
            print(prices[y])
            print(pricePerUnit[y].get_text())
            print()


        # Print the extracted titles
        # for title in titles:
        #     print(title.get_text(strip=True))
    else:
        print("Failed to retrieve the webpage")

getHTML(url, product)