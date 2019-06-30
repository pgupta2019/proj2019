package com.jpm.stockmarket.repository.impl;

import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.cache.LoadingCache;
import com.jpm.stockmarket.exception.GBCEServiceException;
import com.jpm.stockmarket.model.Trade;
import com.jpm.stockmarket.model.TradeIndicator;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TradeRepositoryImplTest.class)
public class TradeRepositoryImplTest {

    private TradeRepositoryImpl underTest = null;

    @Before
    public void setup() {
        underTest = new TradeRepositoryImpl();
        underTest.setup();
    }

    @Test
    public void addSingleTrade_success() throws ExecutionException {
        Trade trade = new Trade();
        trade.setIndicator(TradeIndicator.SELL);
        trade.setShareQuantity(2);
        trade.setStockSymbol("test");
        trade.setTimeStamp(LocalDateTime.now());
        String b = underTest.recordTrade(trade);
        assertThat(b, CoreMatchers.notNullValue());

        LoadingCache<String, List<Trade>> inMemoryTradeCache = underTest.getInMemoryTradeCache();
        assertThat(inMemoryTradeCache.get("test"), CoreMatchers.notNullValue());
        assertThat(inMemoryTradeCache.get("test").size(), CoreMatchers.is(1));
        assertThat(inMemoryTradeCache.get("test").get(0).getShareQuantity(), CoreMatchers.is(2L));

    }

    @Test
    public void addMultipleTrade_success() throws ExecutionException {
        Trade trade = new Trade();
        trade.setIndicator(TradeIndicator.SELL);
        trade.setShareQuantity(2);
        trade.setStockSymbol("test");
        trade.setTimeStamp(LocalDateTime.now());

        String b = underTest.recordTrade(trade);
        assertThat(b, CoreMatchers.notNullValue());
        b = underTest.recordTrade(trade);
        assertThat(b, CoreMatchers.notNullValue());


        LoadingCache<String, List<Trade>> inMemoryTradeCache = underTest.getInMemoryTradeCache();
        assertThat(inMemoryTradeCache.get("test"), CoreMatchers.notNullValue());
        assertThat(inMemoryTradeCache.get("test").size(), CoreMatchers.is(2));
        assertThat(inMemoryTradeCache.get("test").get(0).getShareQuantity(), CoreMatchers.is(2L));

    }

    @Test
    public void getLatestTrades_success() throws InterruptedException, GBCEServiceException {
        Trade trade = new Trade();
        trade.setIndicator(TradeIndicator.BUY);
        trade.setShareQuantity(1);
        trade.setStockSymbol("test-eviction");
        trade.setTimeStamp(LocalDateTime.now());

        String b = underTest.recordTrade(trade);
        assertThat(b, CoreMatchers.notNullValue());

        List<Trade> trades = underTest.getLatestTrades("test-eviction");
        assertThat(trades, CoreMatchers.notNullValue());
        assertThat(trades.size(), CoreMatchers.is(1));

        Thread.sleep(TradeRepositoryImpl.inMemoryExpirationValue * 2);
        List<Trade> responses = underTest.getLatestTrades("test-eviction");
        assertThat(responses, CoreMatchers.notNullValue());
        assertThat(responses.size(), CoreMatchers.is(0));

    }

    @Test(expected = GBCEServiceException.class)
    public void getLatestTrades_failure() throws GBCEServiceException {
        new TradeRepositoryImpl().getLatestTrades("test");
    }

    @Test
    public void getTrades_withSymbol_success() {
        Trade trade = new Trade();
        trade.setIndicator(TradeIndicator.BUY);
        trade.setShareQuantity(1);
        trade.setStockSymbol("test-single");
        trade.setTimeStamp(LocalDateTime.now());

        String b = underTest.recordTrade(trade);
        assertThat(b, CoreMatchers.notNullValue());

        List<Trade> trades = underTest.getTrades("test-single");
        assertThat(trades, CoreMatchers.notNullValue());
        assertThat(trades.size(), CoreMatchers.is(1));
    }

    @Test
    public void getTrades_withMultipleSymbol_success() {
        Trade trade = new Trade();
        trade.setIndicator(TradeIndicator.BUY);
        trade.setShareQuantity(1);
        trade.setStockSymbol("test-multiple");
        trade.setTimeStamp(LocalDateTime.now());

        assertThat(underTest.recordTrade(trade), CoreMatchers.notNullValue());
        assertThat(underTest.getTrades("test-multiple"), CoreMatchers.notNullValue());
        assertThat(underTest.getTrades("test-multiple").size(), CoreMatchers.is(1));

        Trade trade1 = new Trade();
        trade1.setIndicator(TradeIndicator.BUY);
        trade1.setShareQuantity(1);
        trade1.setStockSymbol("test-multiple-2");
        trade1.setTimeStamp(LocalDateTime.now());

        assertThat(underTest.recordTrade(trade1), CoreMatchers.notNullValue());
        assertThat(underTest.getTrades("test-multiple-2"), CoreMatchers.notNullValue());
        assertThat(underTest.getTrades("test-multiple-2").size(), CoreMatchers.is(1));
    }

    @Test
    public void getTrades_withInvalidSymbol_success() {
        Trade trade = new Trade();
        trade.setIndicator(TradeIndicator.BUY);
        trade.setShareQuantity(1);
        trade.setStockSymbol("test-invalid");
        trade.setTimeStamp(LocalDateTime.now());

        assertThat(underTest.recordTrade(trade), CoreMatchers.notNullValue());

        assertThat(underTest.getTrades("test-invalid-eviction"), CoreMatchers.notNullValue());
        assertThat(underTest.getTrades("test-invalid-eviction").size(), CoreMatchers.is(0));
    }

    @Test
    public void getAllTades_success() {

        Trade trade1 = new Trade("trade-one", null, 1, TradeIndicator.BUY, BigDecimal.TEN);
        Trade trade2 = new Trade("trade-two", null, 1, TradeIndicator.SELL, BigDecimal.TEN);

        assertThat(underTest.recordTrade(trade1), CoreMatchers.notNullValue());
        assertThat(underTest.recordTrade(trade2), CoreMatchers.notNullValue());

        List<Trade> allStockTrades = underTest.getTradesForAllStocks();
        assertThat(allStockTrades.size(), CoreMatchers.is(2));
        assertThat(allStockTrades.get(0).getStockSymbol(), CoreMatchers.is(trade1.getStockSymbol()));
        assertThat(allStockTrades.get(1).getStockSymbol(), CoreMatchers.is(trade2.getStockSymbol()));
    }

}