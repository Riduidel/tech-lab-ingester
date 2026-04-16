package com.zenika.tech.lab.ingester;

public abstract class TechLabIngesterException extends RuntimeException {

	public TechLabIngesterException() {
	}

	public TechLabIngesterException(String message) {
		super(message);
	}

	public TechLabIngesterException(Throwable cause) {
		super(cause);
	}

	public TechLabIngesterException(String message, Throwable cause) {
		super(message, cause);
	}

	public TechLabIngesterException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
