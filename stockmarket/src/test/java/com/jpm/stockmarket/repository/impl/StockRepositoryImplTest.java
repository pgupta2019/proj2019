package com.jpm.stockmarket.repository.impl;

import com.jpm.stockmarket.exception.GBCEServiceException;
import com.jpm.stockmarket.model.Stock;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = StockRepositoryImplTest.class)

public class StockRepositoryImplTest {

	private  StockRepositoryImpl underTest;

	@Before
	public void init() {
		underTest = new StockRepositoryImpl();
	 	underTest.initialize();
	}

	@Test
	public void getStockBySymbol_success() throws GBCEServiceException {
		Optional<Stock> stock = underTest.getStockBySymbol("gin");
		assertThat(stock.isPresent(), CoreMatchers.is(true));
		assertThat(stock.get().getSymbol(), CoreMatchers.is(Stock.GIN.getSymbol()));
		assertThat(stock.get().getFixedDividend(), CoreMatchers.is(Stock.GIN.getFixedDividend()));
		assertThat(stock.get().getLastDividend(), CoreMatchers.is(Stock.GIN.getLastDividend()));
		assertThat(stock.get().getType(), CoreMatchers.is(Stock.GIN.getType()));
	}

	@Test(expected = GBCEServiceException.class)
	public void getStockBySymbol_failure() throws GBCEServiceException {
		underTest.getStockBySymbol("test");
		
	}
}
