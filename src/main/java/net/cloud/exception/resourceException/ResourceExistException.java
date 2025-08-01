package net.cloud.exception.resourceException;

public class ResourceExistException extends RuntimeException {
    public ResourceExistException(String message) {
        super(message);
    }

    public ResourceExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceExistException(Throwable cause) {
        super(cause);
    }
}
