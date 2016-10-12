package com.ft.universalpublishing.documentstore.exception;

/**
 * Created by peter.clark on 11/10/2016.
 */
public class ErrorMessage {

  private final String message;

  public ErrorMessage(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
