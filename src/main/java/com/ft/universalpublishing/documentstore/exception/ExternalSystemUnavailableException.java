package com.ft.universalpublishing.documentstore.exception;

@SuppressWarnings("serial")
public class ExternalSystemUnavailableException extends RuntimeException {

    public ExternalSystemUnavailableException(String message) {
        super(message);
    }

    public ExternalSystemUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
