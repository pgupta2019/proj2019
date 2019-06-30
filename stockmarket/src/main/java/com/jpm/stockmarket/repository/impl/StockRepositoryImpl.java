package com.jpm.stockmarket.repository.impl;

import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jpm.stockmarket.exception.GBCEServiceException;
import com.jpm.stockmarket.model.Stock;
import com.jpm.stockmarket.repository.StockRepository;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class StockRepositoryImpl implements StockRepository {

    // in-memory stock cache
    private LoadingCache<String, Optional<Stock>> StockCache;

    @PostConstruct
    public void initialize() {

        // initialize and override the load of CacheLoader to have control over the fetch
        StockCache = CacheBuilder.newBuilder()
                .maximumSize(5)    // maximum 5 records can only be cached
                .build(new CacheLoader<String, Optional<Stock>>() {
                    @Override
                    public Optional<Stock> load(String symbol) throws GBCEServiceException {
                        return getStockBySymbolFromDB(symbol);
                    }
                });
    }


    /*
        method to load the data from the database. This will query the db and extract the matching Stock with 
        symbol provided

     */
    private Optional<Stock> getStockBySymbolFromDB(String symbol) throws GBCEServiceException {

        log.info("initial fetch, hence getting it from DB. Going forward it will be from cache");
        log.info("getting data for symbol={}", symbol);
        Stock Stock = EnumSet.allOf(Stock.class).stream().filter(s -> s.getSymbol().equalsIgnoreCase(symbol.toUpperCase()))
                .findFirst().
                        orElseThrow(() -> new GBCEServiceException(String.format("no Stock found for symbol=%s", symbol)));
        return Optional.of(Stock);
    }


    @Override
    public Optional<Stock> getStockBySymbol(String symbol) throws GBCEServiceException {
        log.debug("Getting Stock details for symblol {}", symbol);
        Optional<Stock> stock = null;
        try {
            stock = StockCache.get(symbol);
        } catch (ExecutionException e) {
            log.error("exception occurred when getting Stock for symbol={}",symbol, e);
            throw new GBCEServiceException(e);
        }

        return stock;
    }

}
