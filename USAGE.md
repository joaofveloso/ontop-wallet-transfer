# Wallet Management

This application allows users to manage their wallet and transfer money to recipients. The application is built with Java and Spring Boot.

## Usage

1. To use the application, first navigate to http://localhost:8080/swagger-ui/index.html to interact with the API through the Swagger-UI interface. Alternatively, you can use REST calls directly.

2. To obtain a token that contains your client ID, use the following REST call:

```
curl -X 'POST'
'http://localhost:8080/login/2'
-H 'accept: /'
-d ''
```

3. Copy the token that is returned in the response, and use it to authorize subsequent requests.

4. To create a new recipient, use the following REST call, replacing the example values with the recipient information:

```
curl -X 'POST'
'http://localhost:8080/recipients'
-H 'accept: /'
-H 'Authorization: Bearer <TOKEN>'
-H 'Content-Type: application/json'
-d '{
"name": "John",
"surname": "John",
"routingNumber": "123456789",
"identificationNumber": "123-45-6789",
"accountNumber": "987654321",
"bankName": "Bank of Ontop"
}'
```

5. Note the ID that is returned in the response header's `Location` field for the newly created recipient. You will use this ID in the next step.

6. To transfer money to a recipient, use the following REST call, replacing the example values with the recipient ID and the amount to transfer:


```
curl -X 'POST'
'http://localhost:8080/transactions'
-H 'accept: /'
-H 'Authorization: Bearer <TOKEN>'
-H 'Content-Type: application/json'
-d '{
"recipientId": "<RECIPIENT_ID>",
"amount": 1000
}'
```

7. To retrieve the transaction you just created, use the following REST call, replacing the example transaction ID with the one returned in the previous step:


```
curl -X 'GET'
'http://localhost:8080/transactions/<TRANSACTION_ID>'
-H 'accept: /'
-H 'Authorization: Bearer <TOKEN>'
```


That's it! You can now use this application to manage your wallet and transfer money to recipients.


List of all available endpoints
### Recipients
Get a list of all recipients created for the client
```
curl -X 'GET' \
  'http://localhost:8080/recipients?page=0&size=20' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer <TOKEN>'
```
Create a new recipient
```
curl -X 'POST' \
  'http://localhost:8080/recipients' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer <TOKEN>' \
  -H 'Content-Type: application/json' \
  -d '{
  "name": "John",
  "surname": "John",
  "routingNumber": "123456789",
  "identificationNumber": "123-45-6789",
  "accountNumber": "987654321",
  "bankName": "Bank of Ontop"
}'
```
Find recipient by id
```
curl -X 'GET' \
  'http://localhost:8080/recipients/d' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer <TOKEN>'
```
### Transactions
List all transaction for client
```
curl -X 'GET' \
  'http://localhost:8080/transactions?page=0&size=20' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer <TOKEN>'
```
Create a new transaction
```
curl -X 'POST' \
  'http://localhost:8080/transactions' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer <TOKEN>' \
  -H 'Content-Type: application/json' \
  -d '{
  "recipientId": "de5b950e-274e-459a-8d9c-668f05d75ae6",
  "amount": 1000
}'
```
Get transaction by ID
```
curl -X 'GET' \
  'http://localhost:8080/transactions/f329fc05-e2ee-4708-a11f-b9176fe65796' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer <TOKEN>'
```
### Token controller
Create a new token
```
curl -X 'POST' \
  'http://localhost:8080/login/2' \
  -H 'accept: */*' \
  -d ''
```
## Diagrams
The following diagrams represent the application data flow and state

![State Diagram](./img/State%20Diagram.png)
![Data Diagram](./img/Data%20Diagram.png)
