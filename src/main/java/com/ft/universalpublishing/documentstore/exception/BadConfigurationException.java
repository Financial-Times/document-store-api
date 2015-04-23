package com.ft.universalpublishing.documentstore.exception;

/**
 * BadConfigurationException
 *
 * @author Simon.Gibbs
 */
public class BadConfigurationException extends RuntimeException {
    public BadConfigurationException(Exception e) {
        super(e);
    }
}
