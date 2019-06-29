package com.jpm.stockmarket.repository;

import com.jpm.stockmarket.exception.GBCEServiceException;
import com.jpm.stockmarket.model.Trade;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository {

    List<Trade> getTrades(String stockSymbol);

    String recordTrade(Trade trade) throws GBCEServiceException;

    List<Trade> getLatestTrades(String symbol) throws GBCEServiceException;

    List<Trade> getTradesForAllStocks() throws GBCEServiceException;

}
