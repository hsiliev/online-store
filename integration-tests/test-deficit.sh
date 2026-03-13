#!/bin/bash

# Load URLs
source .envrc

# IDs: 1 for chocolate, 2 for banana

echo -e "\nStock 3 chocolates and 4 bananas in the Store"
curl -s -X POST "$STORE_URL/stock" -H "Content-Type: application/json" -d '{"productId": 1, "productName": "chocolate", "quantity": 3}'
curl -s -X POST "$STORE_URL/stock" -H "Content-Type: application/json" -d '{"productId": 2, "productName": "banana", "quantity": 4}'
echo ""

echo -e "\nOrder 1 chocolate and 1 banana"
curl -s -X POST "$SHOP_URL/order" -H "Content-Type: application/json" -d '[{"productId": 1, "quantity": 1}, {"productId": 2, "quantity": 1}]'
echo ""

echo -e "\nCheck the stock is 2 chocolates and 3 bananas in the Store"
STOCK=$(curl -s "$STORE_URL/stock")
echo "$STOCK"
if [[ "$STOCK" == *"\"id\":1,\"name\":\"chocolate\",\"quantity\":2"* ]] && [[ "$STOCK" == *"\"id\":2,\"name\":\"banana\",\"quantity\":3"* ]]; then
    echo ""
else
    echo "Stock check FAILED"
    exit 1
fi

echo -e "\nOrder 10 chocolates"
curl -s -X POST "$SHOP_URL/order" -H "Content-Type: application/json" -d '[{"productId": 1, "quantity": 10}]'
echo ""

echo -e "\nOrder 10 chocolates and 10 bananas"
curl -s -X POST "$SHOP_URL/order" -H "Content-Type: application/json" -d '[{"productId": 1, "quantity": 10}, {"productId": 2, "quantity": 10}]'
echo ""

echo -e "\nCheck that demand is 18 chocolates and 7 bananas"
DEMAND=$(curl -s "$STORE_URL/demand")
echo "$DEMAND"
if [[ "$DEMAND" == *"\"productId\":1,\"quantityInDemand\":18"* ]] && [[ "$DEMAND" == *"\"productId\":2,\"quantityInDemand\":7"* ]]; then
    echo ""
else
    echo "Demand check FAILED"
    exit 1
fi

echo -e "\nStock 10 chocolates and 5 bananas in the Store"
curl -s -X POST "$STORE_URL/stock" -H "Content-Type: application/json" -d '{"productId": 1, "quantity": 10}'
curl -s -X POST "$STORE_URL/stock" -H "Content-Type: application/json" -d '{"productId": 2, "quantity": 5}'
echo ""

sleep 2

echo -e "\nCheck we have 2 chocolates and 8 bananas in the Store"
STOCK=$(curl -s "$STORE_URL/stock")
echo "$STOCK"
if [[ "$STOCK" == *"\"id\":1,\"name\":\"chocolate\",\"quantity\":2"* ]] && [[ "$STOCK" == *"\"id\":2,\"name\":\"banana\",\"quantity\":8"* ]]; then
    echo ""
else
    echo "Stock check FAILED"
    exit 1
fi

echo -e "\nCheck that demand is 8 chocolates and 2 bananas"
DEMAND=$(curl -s "$STORE_URL/demand")
echo "$DEMAND"
if [[ "$DEMAND" == *"\"productId\":1,\"quantityInDemand\":8"* ]] && [[ "$DEMAND" == *"\"productId\":2,\"quantityInDemand\":2"* ]]; then
    echo ""
else
    echo "Demand check FAILED"
    exit 1
fi

echo -e "\nStock another batch of 10 chocolates and 5 bananas in the Store"
curl -s -X POST "$STORE_URL/stock" -H "Content-Type: application/json" -d '{"productId": 1, "quantity": 10}'
curl -s -X POST "$STORE_URL/stock" -H "Content-Type: application/json" -d '{"productId": 2, "quantity": 5}'
echo ""

sleep 2

echo -e "\nCheck that no demand exists"
DEMAND=$(curl -s "$STORE_URL/demand")
echo "$DEMAND"
if [[ "$DEMAND" == "[]" ]]; then
    echo ""
else
    echo "Demand check FAILED"
    exit 1
fi

echo -e "\nCheck the stock is 2 chocolates and 3 bananas in the Store"
STOCK=$(curl -s "$STORE_URL/stock")
echo "$STOCK"
if [[ "$STOCK" == *"\"id\":1,\"name\":\"chocolate\",\"quantity\":2"* ]] && [[ "$STOCK" == *"\"id\":2,\"name\":\"banana\",\"quantity\":3"* ]]; then
    echo ""
else
    echo "Stock check FAILED"
    exit 1
fi

echo -e "\nAll tests PASSED"
