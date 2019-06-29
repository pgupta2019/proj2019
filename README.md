
### Assignment - Super Simple Stock Market

## Requirements

Provide working source code that will:
    a.	For a given stock:
    
        i.    Calculate the dividend yield.
        ii.   Calculate the P/E Ratio.
        iii.  Record a trade, with timestamp, quantity of shares, buy or sell indicator and price.
        iv.   Calculate Stock Price based on trades recorded in past 15 minutes.

    b.	Calculate the GBCE All Share Index using the geometric mean of prices for all stocks
## Tech Stack:
* Source Java-8
* Framework  Spring Boot -2.1.6
* In Cache memory Guava-23.0
* Build Tool: Maven 
## Implementation
This design of application includes service “CalculationServiceImpl ” , In memory database layer interfaces StockRepostory  and TradeRepository 
and model - Stock and Trade.In-memory cache to keep data in cache .Followed Test Driven development  approach to test each operation and edge cases .

##### Assumption 
* To calculate “P/E Ratio”  assuming last dividend as dividend and dividend should be positive value.
* To calculate Volume weighted stock price for trades in past 15 minutes assuming minutes are configurable .
* Assuming all the data would be available if stock is present. 

##### Package Structure 
* Model classes -com.jpm.stockmarket.model
* Service Classes -com.jpm.stockmarket.Service
* Database Classes -com.jpm.stockmarket.repository
* Exception Class -com.jpm.stockmarket.exception

## How to use-

To compile and run test with below command
* mvn clean install -To compile the project 
* mvn test - To execute unit tests.

Or if using eclipse add lumbok plugin to avoid compilation error and use maven goal clean install .
