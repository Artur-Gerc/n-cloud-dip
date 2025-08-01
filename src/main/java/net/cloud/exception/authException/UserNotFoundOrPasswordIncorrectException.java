package net.cloud.exception.authException;

public class UserNotFoundOrPasswordIncorrectException extends RuntimeException {
  public UserNotFoundOrPasswordIncorrectException(String message) {
    super(message);
  }

  public UserNotFoundOrPasswordIncorrectException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public UserNotFoundOrPasswordIncorrectException() {
      super();
  }
}
