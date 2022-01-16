package com.assignment.car.charging.store.exceptions;

/**
 * Exception for charging sessions errors.
 *
 * @author <a href="mailto:lexbaev@gmail.com">Aliaksei Lizunou</a>
 */
public class ChargingSessionException extends Exception {

  private String message;

  private Throwable exception;

  public ChargingSessionException(String message, int status) {
    this.message = getJsonMessage(message, status);
  }

  public ChargingSessionException(String message, int status, Throwable exception) {
    this.message = getJsonMessage(message, status);
    this.exception = exception;
  }

  @Override
  public String getMessage() {
    return message;
  }

  public Throwable getException() {
    return exception;
  }

  /**
   * Simple Json formatted exception message.
   *
   * @param message
   * @param status
   * @return
   */
  private String getJsonMessage(String message, int status) {
    return "{ \"message\": \"" + message + "\",\n\"status\": " + status + " }";
  }
}
