package net.cloud.exception.authException;

import org.springframework.security.core.AuthenticationException;

public class UnauthorizedRequestException extends AuthenticationException {
    public UnauthorizedRequestException(String message) {
        super(message);
    }

    public UnauthorizedRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
