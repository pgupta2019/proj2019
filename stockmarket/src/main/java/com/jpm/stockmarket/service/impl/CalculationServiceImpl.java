package com.jpm.stockmarket.service.impl;

import com.jpm.stockmarket.exception.GBCEServiceException;
import com.jpm.stockmarket.model.Trade;
import com.jpm.stockmarket.repository.StockRepository;
import com.jpm.stockmarket.repository.TradeRepository;
import com.jpm.stockmarket.repository.impl.Stock;
import com.jpm.stockmarket.service.CalculationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;



/**
 * @author preetigupta
 *
 */
@Slf4j
public class CalculationServiceImpl implements CalculationService {

	private StockRepository stockRepo;
	private TradeRepository tradeRepo;

	@Autowired
	public CalculationServiceImpl(StockRepository stockRepo, TradeRepository tradeRepo) {
		this.stockRepo = stockRepo;
		this.tradeRepo = tradeRepo;
	}

	/**
	 * For a given market price as input, calculates the dividend yield.
	 * Calculation formula is different depending on the stocktype.
	 * For COMMON stock type - lastDividend/marketPrice
	 * For PREFERRED stock type - lastDividend*parValue/marketPrice
	 *
	 * @param symbol
	 * @param price
	 * @return
	 * @throws GBCEServiceException
	 */
	@Override
	public BigDecimal calculateDividendYield(String symbol, BigDecimal price) throws GBCEServiceException {
		log.info("calculating dividend yield for stock={} with market-price={}", symbol, price);

		// validating the inputs
		Optional.ofNullable(symbol).orElseThrow(() -> new GBCEServiceException("stock symbol cannot be null"));
		Optional.ofNullable(price).orElseThrow(() -> new GBCEServiceException("price cannot be null"));

		// get the stocks details from database. ASSUMPTION - if stock is found all the data would be available
		// validations to ensure code is 100% reliable
		Stock selectedStock = stockRepo.getStockBySymbol(symbol).orElseThrow(() -> new GBCEServiceException("no stocks found for the given symbol=" + symbol));

		Optional.ofNullable(selectedStock.getType()).orElseThrow(() -> new GBCEServiceException("no stock type found for symbol=" + symbol));

		log.info("extracted stock is={}", selectedStock);
		BigDecimal dividendYield;
		
		// calculate dividend yield based on the stock types
		switch (selectedStock.getType()) {
		case COMMON:
			dividendYield = selectedStock.getLastDividend().divide(price);
			break;
		case PREFERRED:
			dividendYield = selectedStock.getFixedDividend().multiply(selectedStock.getParValue()).divide(price);
			break;
		default:
			// invalid scenario, which will not happen as we are dealing with ENUMS
			throw new GBCEServiceException("invalid stock type =" + selectedStock.getType());
		}

		log.info("calculated dividend-yield={} for symbol={}, with market-price={}", dividendYield, symbol, price);
		return dividendYield;
	}


	/**
	 * For a given a market price as input, calculate the price earning ratio for stocks
	 * Formula - Market Price/ Dividend
	 *
	 * @param symbol
	 * @param price
	 * @return
	 * @throws GBCEServiceException
	 */
	@Override
	public BigDecimal calculatePERatio(String symbol, BigDecimal price) throws GBCEServiceException {
		log.info("calculating price earning ratio for symbol={} with price={}", symbol, price);

		// reusing the calculateDividendYield() for validations and calculating the dividend
		BigDecimal dividend = calculateDividendYield(symbol, price);
		log.info("calculated dividend for symbol={} with price={} is {}", symbol, price, dividend);

		Optional.ofNullable(dividend).filter(d -> BigDecimal.ZERO.compareTo(d) != 0)
		.orElseThrow(() -> new GBCEServiceException("dividend cannot be null"));

		BigDecimal peRatio = dividend.divide(price);
		log.info("calculated pe-ratio={} for symbol={} and price={}", peRatio, symbol, price);

		return peRatio;
	}


	/**
	 * Calculate Volume Weighted Stock Price based on trades happened in past 15 minutes
	 * Formula - ∑i Trade Pricei × Quantityi/ ∑i Quantityi
	 *
	 * @param symbol
	 * @return
	 * @throws GBCEServiceException
	 */
	@Override
	public BigDecimal calculateVolWeightedStockPrice(String symbol) throws GBCEServiceException {
		log.info("calculating volume weighted stock price for symbol={}", symbol);

		Optional.ofNullable(symbol).orElseThrow(() -> new GBCEServiceException("symbol cannot be null"));

		// get all the recent trades for input symbol from database
		List<Trade> trades = tradeRepo.getLatestTrades(symbol);

		// throw exeption in case of no data available to process
		Optional.ofNullable(trades).filter(t -> !t.isEmpty()).orElseThrow(() -> new GBCEServiceException(
				"no data found for symbol=" + symbol + " to perform weight stock price calculation"));

		log.info("collected the trades with size={} for symbol={}", trades.size(), symbol);
		BigDecimal priceSum = BigDecimal.ZERO;
		BigInteger quantitySum = BigInteger.ZERO;

		// add all the price and sums for calculating stock price
		for (Trade t : trades) {
			priceSum = priceSum.add(t.getTradePrice().multiply(BigDecimal.valueOf(t.getShareQuantity())));
			quantitySum = quantitySum.add(BigInteger.valueOf(t.getShareQuantity()));
		}
		BigDecimal price = priceSum.divide(new BigDecimal(quantitySum), 7, 3).setScale(0, BigDecimal.ROUND_UP);
		log.info("calculation completed for stock symbol={}");
		return price;
	}

	/**
	 * all the buying and selling needs to be recorded for auditing purposes
	 *
	 * @param trade
	 * @return
	 * @throws GBCEServiceException
	 */
	@Override
	public String recordTrade(Trade trade) throws GBCEServiceException {
		log.info("recording trade={}", trade);
		Optional.ofNullable(trade).orElseThrow(() -> new GBCEServiceException("trade cannot be null"));

		// unique id - system generated
		String id = tradeRepo.recordTrade(trade);

		log.info("trade successfully registered with id={}", id);
		return id;
	}

	/**
	 * Calculate the GBCE All Share Index using the geometric mean of prices for all stocks
	 * Formula - √p1p2p3 ... pn
	 * @return
	 * @throws GBCEServiceException
	 */
	@Override
	public BigDecimal calculateGBCEAllShareIndex() throws GBCEServiceException {
		log.info("calculating shareindex for all the stocks");

		List<Trade> trades = tradeRepo.getTradesForAllStocks();
		Optional.ofNullable(trades).filter(t -> !t.isEmpty()).orElseThrow(() ->
		new GBCEServiceException("No trades found. Trades cannot be null for calculating share index"));

		// calculate the share index
		BigDecimal tradePrice = BigDecimal.ONE;
		// iterate through all the trades and multiply the prices
		for (Trade trade : trades) {
			tradePrice = tradePrice.multiply(trade.getTradePrice());
		}
		BigDecimal shareIndex = new BigDecimal(Math.pow(tradePrice.doubleValue(), 1.0 / trades.size()))
				.setScale(2, BigDecimal.ROUND_HALF_UP);
		log.info("calculated GBCE share Index= {} ",shareIndex);
		return shareIndex;
	}
}
