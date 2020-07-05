package com.filecluster.exception;

public class InvalidNameException extends RuntimeException {

	private static final long serialVersionUID = 3824272835582236878L;

	public InvalidNameException(String message) {
		super(message);		
	}

}
