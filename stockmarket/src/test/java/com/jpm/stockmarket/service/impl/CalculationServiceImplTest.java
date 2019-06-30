package com.jpm.stockmarket.service.impl;

import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.jpm.stockmarket.exception.GBCEServiceException;
import com.jpm.stockmarket.model.Stock;
import com.jpm.stockmarket.model.Trade;
import com.jpm.stockmarket.model.TradeIndicator;
import com.jpm.stockmarket.repository.StockRepository;
import com.jpm.stockmarket.repository.TradeRepository;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CalculationServiceImplTest.class)
public class CalculationServiceImplTest {

    private CalculationServiceImpl underTest = null;
    private StockRepository mockedStockRepository = null;
    private TradeRepository mockedTradeRepository = null;

    @Before
    public void setup() {
        mockedStockRepository = mock(StockRepository.class);
        mockedTradeRepository = mock(TradeRepository.class);
        underTest = new CalculationServiceImpl(mockedStockRepository, mockedTradeRepository);
    }

    @Test(expected = GBCEServiceException.class)
    public void calculateDividendYield_with_noStockType_failure() throws Exception {
        Stock stock = Stock.ALE;
        stock.setType(Stock.StockTypes.OTHER);
        when(mockedStockRepository.getStockBySymbol(any())).thenReturn(Optional.of(stock));

        underTest.calculateDividendYield("Test", BigDecimal.ONE);
    }

    @Test
    public void calculateDividendYield_for_commonType_success() throws Exception {
        when(mockedStockRepository.getStockBySymbol(any())).thenReturn(Optional.of(Stock.ALE));

        BigDecimal response = underTest.calculateDividendYield("Test", BigDecimal.ONE);
        assertThat(response, CoreMatchers.notNullValue());
        assertThat(response.doubleValue(), CoreMatchers.is(23.00));
    }


    @Test
    public void calculateDividendYield_for_preferredType_success() throws Exception {

        when(mockedStockRepository.getStockBySymbol(any())).thenReturn(Optional.of(Stock.GIN));

        BigDecimal response = underTest.calculateDividendYield("Test", BigDecimal.ONE);
        assertThat(response, CoreMatchers.notNullValue());
        assertThat(response.doubleValue(), CoreMatchers.is(2.00));
    }

    @Test(expected = GBCEServiceException.class)
    public void calculateDividendYield_null_inputs_exception() throws Exception {
        underTest.calculateDividendYield(null, null);

    }

    @Test(expected = GBCEServiceException.class)
    public void calculateDividendYield_null_symbol_exception() throws Exception {

        underTest.calculateDividendYield(null, BigDecimal.ONE);
    }

    @Test(expected = GBCEServiceException.class)
    public void calculateDividendYield_null_price_exception() throws Exception {

        underTest.calculateDividendYield("test", null);

    }

    @Test(expected = GBCEServiceException.class)
    public void calculatePERatio_nullDividend_exception() throws GBCEServiceException {
        Stock testStock = Stock.GIN;
        testStock.setFixedDividend(BigDecimal.ZERO);
        testStock.setParValue(BigDecimal.ZERO);

        when(mockedStockRepository.getStockBySymbol(any())).thenReturn(Optional.of(testStock));
        underTest.calculatePERatio("Test", BigDecimal.ONE);
    }

    @Test
    public void calculatePERatio_success() throws GBCEServiceException {
        when(mockedStockRepository.getStockBySymbol(any())).thenReturn(Optional.of(Stock.GIN));

        BigDecimal response = underTest.calculatePERatio("Test", BigDecimal.ONE);
        assertThat(response, CoreMatchers.notNullValue());
        assertThat(response.doubleValue(), CoreMatchers.is(2.00));
    }

    @Test
    public void calculateVolWeightedStockPrice_singletrade_success() throws Exception {

        List<Trade> trades = Arrays.asList(new Trade(null, null, 1, TradeIndicator.BUY, BigDecimal.TEN));
        when(mockedTradeRepository.getLatestTrades(any())).thenReturn(trades);
        BigDecimal weightPrice = underTest.calculateVolWeightedStockPrice("test-symbol");
        assertThat(weightPrice, CoreMatchers.is(BigDecimal.TEN));
    }

    @Test
    public void calculateVolWeightedStockPrice_multipletrades_success() throws Exception {

        List<Trade> trades = Arrays.asList(new Trade(null, null, 1, TradeIndicator.BUY, BigDecimal.TEN),
                new Trade(null, null, 1, TradeIndicator.BUY, BigDecimal.TEN));
        when(mockedTradeRepository.getLatestTrades(any())).thenReturn(trades);
        BigDecimal weightPrice = underTest.calculateVolWeightedStockPrice("test-symbol");
        assertThat(weightPrice, CoreMatchers.is(BigDecimal.TEN));
    }

    @Test(expected = GBCEServiceException.class)
    public void calculateVolWeightedStockPrice_withNullInput_failure() throws GBCEServiceException {
        underTest.calculateVolWeightedStockPrice(null);
    }

    @Test(expected = GBCEServiceException.class)
    public void calculateVolWeightedStockPrice_withNullTrades_failure() throws GBCEServiceException {
        when(mockedTradeRepository.getLatestTrades(any())).thenReturn(null);
        underTest.calculateVolWeightedStockPrice("test-symbol");
    }

    @Test(expected = GBCEServiceException.class)
    public void calculateVolWeightedStockPrice_withEmptyTrades_failure() throws GBCEServiceException {
        when(mockedTradeRepository.getLatestTrades(any())).thenReturn(new ArrayList<>());
        underTest.calculateVolWeightedStockPrice("test-symbol");
    }

    @Test
    public void recordTrade_success() throws GBCEServiceException {
        when(mockedTradeRepository.recordTrade(any())).thenReturn("id");
        String id = underTest.recordTrade(new Trade());
        assertThat(id, CoreMatchers.is("id"));
    }

    @Test(expected = GBCEServiceException.class)
    public void recordTrade_failure() throws GBCEServiceException {
        underTest.recordTrade(null);
    }


    @Test(expected = GBCEServiceException.class)
    public void calculateGBCEAllShareIndex_nullTrades_failure() throws GBCEServiceException {
        when(mockedTradeRepository.getTradesForAllStocks()).thenReturn(null);
        underTest.calculateGBCEAllShareIndex();
    }

    @Test(expected = GBCEServiceException.class)
    public void calculateGBCEAllShareIndex_emptyTrades_failure() throws GBCEServiceException {
        when(mockedTradeRepository.getTradesForAllStocks()).thenReturn(new ArrayList<>());
        underTest.calculateGBCEAllShareIndex();
    }

    @Test
    public void calculateGBCEAllShareIndex_singleTrade_success() throws GBCEServiceException {
        List<Trade> trades = Arrays.asList(new Trade(null, null, 1, TradeIndicator.BUY, BigDecimal.TEN));
        when(mockedTradeRepository.getTradesForAllStocks()).thenReturn(trades);
        BigDecimal index = underTest.calculateGBCEAllShareIndex();

        assertThat(index.doubleValue(), CoreMatchers.is(10.00));
    }

    @Test
    public void calculateGBCEAllShareIndex_multipleTrades_success() throws GBCEServiceException {
        List<Trade> trades = Arrays.asList(new Trade(null, null, 1, TradeIndicator.BUY, BigDecimal.TEN),
                new Trade(null, null, 1, TradeIndicator.SELL, BigDecimal.TEN));
        when(mockedTradeRepository.getTradesForAllStocks()).thenReturn(trades);
        BigDecimal index = underTest.calculateGBCEAllShareIndex();

        assertThat(index.doubleValue(), CoreMatchers.is(10.00));
    }
}