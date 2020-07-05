package com.filecluster.exception;

public class HostNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -8250691804339372594L;

	public HostNotFoundException(String message) {
		super(message);
	}

}
