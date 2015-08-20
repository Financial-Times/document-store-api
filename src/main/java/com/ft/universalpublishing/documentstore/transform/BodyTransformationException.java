package com.ft.universalpublishing.documentstore.transform;

public class BodyTransformationException extends RuntimeException {
	private static final long serialVersionUID = 1682900007757661672L;

	public BodyTransformationException(final String message) {
		super(message);
	}

	public BodyTransformationException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
