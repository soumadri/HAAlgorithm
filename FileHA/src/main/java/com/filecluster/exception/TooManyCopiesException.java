package com.filecluster.exception;

public class TooManyCopiesException extends RuntimeException {

	private static final long serialVersionUID = 95593924066062594L;

	public TooManyCopiesException(String message) {
		super(message);
	}
}
