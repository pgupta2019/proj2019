package com.jpm.stockmarket.repository.impl;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author preetigupta
 */

public enum Stock {

    TEA("TEA", StockTypes.COMMON, BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(100)),
    POP("POP", StockTypes.COMMON, BigDecimal.valueOf(8), BigDecimal.valueOf(0), BigDecimal.valueOf(100)),
    ALE("ALE", StockTypes.COMMON, BigDecimal.valueOf(23), BigDecimal.valueOf(0), BigDecimal.valueOf(60)),
    GIN("GIN", StockTypes.PREFERRED, BigDecimal.valueOf(8), BigDecimal.valueOf(0.02), BigDecimal.valueOf(100)),
    JOE("JOE", StockTypes.COMMON, BigDecimal.valueOf(13), BigDecimal.valueOf(0), BigDecimal.valueOf(250));


    @Getter @Setter
    private String symbol;

    @Getter @Setter
    private StockTypes type;

    @Getter @Setter
    private BigDecimal lastDividend;
    @Getter @Setter
    private BigDecimal fixedDividend;
    @Getter @Setter
    private BigDecimal parValue;

    Stock(String symbol, StockTypes type, BigDecimal lastDividend, BigDecimal fixedDividend, BigDecimal parValue) {
        this.symbol = symbol;
        this.type = type;
        this.lastDividend = lastDividend;
        this.fixedDividend = fixedDividend;
        this.parValue = parValue;

    }

    public enum StockTypes {

        COMMON, PREFERRED, OTHER
    }
}
