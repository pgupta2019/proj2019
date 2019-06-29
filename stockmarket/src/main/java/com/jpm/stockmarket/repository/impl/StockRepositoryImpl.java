package com.jpm.stockmarket.repository.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jpm.stockmarket.exception.GBCEServiceException;
import com.jpm.stockmarket.repository.StockRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
@Slf4j
public class StockRepositoryImpl implements StockRepository {

    // in-memory stock cache
    private LoadingCache<String, Optional<Stock>> stocksCache;

    @PostConstruct
    public void initialize() {

        // initialize and override the load of CacheLoader to have control over the fetch
        stocksCache = CacheBuilder.newBuilder()
                .maximumSize(5)    // maximum 5 records can only be cached
                .build(new CacheLoader<String, Optional<Stock>>() {
                    @Override
                    public Optional<Stock> load(String symbol) throws GBCEServiceException {
                        return getStockBySymbolFromDB(symbol);
                    }
                });
    }


    /*
        method to load the data from the database. This will query the db and extract the matching Stock with the
        symbol provided

     */
    private Optional<Stock> getStockBySymbolFromDB(String symbol) throws GBCEServiceException {

        log.info("initial fetch, hence getting it from DB. Going forward it will be from cache");
        log.info("getting data for symbol={}", symbol);
        Stock stocks = EnumSet.allOf(Stock.class).stream().filter(s -> s.getSymbol().equalsIgnoreCase(symbol.toUpperCase()))
                .findFirst().
                        orElseThrow(() -> new GBCEServiceException(String.format("no stocks found for symbol=%s", symbol)));
        return Optional.of(stocks);
    }

    /**
     * this method will get the stock with provided symbol that is available in the cache. 
     * 
     * @param symbol
     * @return
     * @throws GBCEServiceException
     */
    
    @Override
    public Optional<Stock> getStockBySymbol(String symbol) throws GBCEServiceException {
        log.debug("Getting Stock details for symblol {}", symbol);
        Optional<Stock> stock = null;
        try {
            stock = stocksCache.get(symbol);
        } catch (ExecutionException e) {
            log.error("exception occurred when getting stocks for symbol={}",symbol, e);
            throw new GBCEServiceException(e);
        }

        return stock;
    }

}
