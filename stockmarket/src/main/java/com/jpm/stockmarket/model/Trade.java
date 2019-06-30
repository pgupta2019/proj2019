package com.jpm.stockmarket.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@ToString
public class Trade {

	@Getter @Setter
	private String id;
	@Getter @Setter
	private String stockSymbol;
	@Getter @Setter
	private LocalDateTime timeStamp;
	@Getter @Setter
	private long shareQuantity;
	@Getter @Setter
	private TradeIndicator indicator;
	@Getter @Setter
	private BigDecimal tradePrice;
	@Getter @Setter
	private boolean isEvicted;
	
	
	public Trade(String stockSymbol, LocalDateTime timeStamp, long shareQuantity, TradeIndicator indicator,
			BigDecimal tradePrice) {
		super();
		this.stockSymbol = stockSymbol;
		this.timeStamp = timeStamp;
		this.shareQuantity = shareQuantity;
		this.indicator = indicator;
		this.tradePrice = tradePrice;
	}

	public Trade() {
		super();
		// TODO Auto-generated constructor stub
	}
	

	
	
	
}
