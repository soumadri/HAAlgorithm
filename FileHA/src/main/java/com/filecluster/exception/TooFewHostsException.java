package com.filecluster.exception;

public class TooFewHostsException extends RuntimeException {

	private static final long serialVersionUID = 6118956129297286318L;

	public TooFewHostsException(String message) {
		super(message);
	}

}
