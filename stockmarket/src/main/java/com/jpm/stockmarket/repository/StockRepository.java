package com.jpm.stockmarket.repository;

import com.jpm.stockmarket.exception.GBCEServiceException;
import com.jpm.stockmarket.repository.impl.Stock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockRepository {

	Optional<Stock> getStockBySymbol(String symbol) throws GBCEServiceException;
	
	
}
