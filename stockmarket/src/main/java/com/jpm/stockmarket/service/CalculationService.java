package com.jpm.stockmarket.service;

import java.math.BigDecimal;

import com.jpm.stockmarket.exception.GBCEServiceException;
import org.springframework.stereotype.Service;

import com.jpm.stockmarket.model.Trade;

@Service
public interface CalculationService {

    BigDecimal calculateDividendYield(String stockSymbol, BigDecimal price) throws GBCEServiceException;

    BigDecimal calculatePERatio(String stockSymbol, BigDecimal price) throws GBCEServiceException;

    String recordTrade(Trade trade) throws GBCEServiceException;

    BigDecimal calculateVolWeightedStockPrice(String stockSymbol) throws GBCEServiceException;

    BigDecimal calculateGBCEAllShareIndex() throws GBCEServiceException;
}
