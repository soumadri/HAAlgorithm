package com.filecluster.exception;

public class TooManyFailedHostException extends RuntimeException {
	
	private static final long serialVersionUID = -7936074571917335578L;

	public TooManyFailedHostException(String message) {
		super(message);
	}

}
