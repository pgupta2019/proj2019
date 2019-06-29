package com.jpm.stockmarket.exception;

/**
 * @author preetigupta
 *
 */
public class GBCEServiceException extends Exception{

	private static final long serialVersionUID = 3572960376610804788L;

	public GBCEServiceException(String message) {
		super(message);
	}

	public GBCEServiceException(Exception e) {
		super(e);
	}

}
