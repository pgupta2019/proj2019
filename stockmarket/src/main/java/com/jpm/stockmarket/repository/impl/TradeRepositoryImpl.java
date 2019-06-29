package com.jpm.stockmarket.repository.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.jpm.stockmarket.exception.GBCEServiceException;
import com.jpm.stockmarket.model.Trade;
import com.jpm.stockmarket.repository.TradeRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class TradeRepositoryImpl implements TradeRepository {

    // should be configurable, ideally something like zookeeper
    public static final Long inMemoryExpirationValue = 2000l;

    // IDEAL approach - there should be one more cache representing database (secondary level),
    // when a record is getting expired/evicted from this cache,
    // it should be pushed into the database cache for auditing and history
    private static List<Trade> trades = new ArrayList<Trade>();
    
    @Getter
    // represents inmemory cache
    private LoadingCache<String, List<Trade>> inMemoryTradeCache;


    /**
     * this will be creating all the caches for holding the trade data
     */
    @PostConstruct
    public void setup() {

    	inMemoryTradeCache = CacheBuilder.newBuilder()
    			.expireAfterWrite(inMemoryExpirationValue, TimeUnit.MILLISECONDS)

    			// a call should be made to database cache which will store the data getting evicted
    			.removalListener((RemovalListener<String, List<Trade>>) notification -> {
    				
    				// storing the data that is getting evicted
    				trades.addAll(notification.getValue());
    				log.info("data is getting evicted={}", notification.getKey());
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
        return trades.stream()
                .filter(e -> e.getStockSymbol().equals(stockSymbol)).collect(Collectors.toList());

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
        // in real scenario this would be the ID returned after inserting data into DB
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
     * this method will get all the trades for provided stock that are available in the cache. A
     *
     * @param symbol
     * @return
     * @throws GBCEServiceException
     */
    @Override
    public List<Trade> getLatestTrades(String symbol) throws GBCEServiceException {
        log.debug("Getting trades in last minute ");

        List<Trade> tradeList = null;
        try {
            tradeList = inMemoryTradeCache.get(symbol);
        } catch (ExecutionException e) {
            log.error("exception occurred when retrieving data from cache={}", e.getMessage(), e);
            throw new GBCEServiceException(e);
        }
        log.info("total trades extracted={}", tradeList.size());
        return tradeList;

    }

    /**
     * This method will get all the trades for all stocks available in the cache. 
     * 
     * 
     */
    @Override
    public List<Trade> getTradesForAllStocks() {
        log.debug("Getting trades for all stocks from in-memory cache");
        List<Trade> tradeList = inMemoryTradeCache.asMap().values().stream().collect(ArrayList::new, List::addAll, List::addAll);
        log.info("total trades extracted={}", tradeList.size());
        return tradeList;
    }
}