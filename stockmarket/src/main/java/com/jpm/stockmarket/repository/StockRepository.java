package com.jpm.stockmarket.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.jpm.stockmarket.exception.GBCEServiceException;
import com.jpm.stockmarket.model.Stock;

@Repository
public interface StockRepository {

	Optional<Stock> getStockBySymbol(String symbol) throws GBCEServiceException;
	
	
}
