package com.jpm.stockmarket.repository.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.jpm.stockmarket.exception.GBCEServiceException;
import com.jpm.stockmarket.model.Trade;
import com.jpm.stockmarket.repository.TradeRepository;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class TradeRepositoryImpl implements TradeRepository {

    // should be configurable, ideally something like zookeeper
    public static final Long inMemoryExpirationValue = 2000l;

    private static List<Trade> trades = new ArrayList<Trade>();

    @Getter
    // represents in memory cache
    private LoadingCache<String, List<Trade>> inMemoryTradeCache;


    /**
     * this will be creating all the caches for holding the trade data
     */
    @PostConstruct
    public void setup() {

        inMemoryTradeCache = CacheBuilder.newBuilder()
                .expireAfterWrite(inMemoryExpirationValue, TimeUnit.MILLISECONDS)
                .removalListener((RemovalListener<String, List<Trade>>) notification -> {
                    log.info("data is getting evicted={}", notification.getKey());
                    Optional<Trade> first = trades.stream()
                            .filter(t -> t.getStockSymbol().equalsIgnoreCase(notification.getKey())).findFirst();
                    if(first.isPresent()) {
                        first.get().setEvicted(true);
                    }
                })
                // building a cache loader to load the data
                .build(new CacheLoader<String, List<Trade>>() {

                    @Override
                    public List<Trade> load(String symbol) {
                        return getTrades(symbol);
                    }
                });
    }

    /**
     * get all the trades based on the input symbol
     * mock database implementation where data is fetched from db and this would have even the evicted data from cache
     * This method is one level above getLatestTrades()
     *
     * @param stockSymbol
     * @return
     */
    @Override
    public List<Trade> getTrades(String stockSymbol) {
        log.info("getting trades from database as not found in cache");
        return trades.stream()
                .filter(e -> e.getStockSymbol().equals(stockSymbol)
                // only the active trades needs to be fetched having status isEvicted set to false
                        && ! e.isEvicted()).collect(Collectors.toList());

    }


    /**
     * capture every trade in both primary and secondary cache
     *
     * @param trade
     * @return
     */
    @Override
    public String recordTrade(Trade trade) {
        log.debug("Adding trade for Symbol={}", trade.getStockSymbol());
       
        // using random number to generate ID , in real scenario this would be the ID returned after inserting data into DB
        String id = UUID.randomUUID().toString();
        trade.setId(id);
        log.info("trade is recorded for id={}", id);
        List<Trade> cachedTrades = inMemoryTradeCache.getIfPresent(trade.getStockSymbol());
        if (cachedTrades == null) {
            cachedTrades = new ArrayList<>();
        }
        cachedTrades.add(trade);
        // adding in cache
        inMemoryTradeCache.put(trade.getStockSymbol(), cachedTrades);
        //adding in database
        trades.add(trade);
        return id;
    }


    /**
     * this method will get all the trades that are available in the cache. As cache is assigned with an
     * expiration interval, data will be purged after writing and only the available ones at that given time would
     * be retrieved
     *
     * @param symbol
     * @return
     * @throws GBCEServiceException
     */
    @Override
    public List<Trade> getLatestTrades(String symbol) throws GBCEServiceException {
        log.debug("Getting trades in last minute ");

        List<Trade> tradeList;
        try {
            tradeList = inMemoryTradeCache.get(symbol);
        } catch (Exception e) {
            log.error("exception occurred when retrieving data from cache={}", e.getMessage(), e);
            throw new GBCEServiceException(e);
        }
        log.info("total trades extracted={}", tradeList.size());
        return tradeList;

    }

    /**
     *
     */
    @Override
    public List<Trade> getTradesForAllStocks() {
        log.debug("Getting trades for all stocks with no time limit");
        log.info("total trades extracted={}", trades.size());
        return trades;
    }
}