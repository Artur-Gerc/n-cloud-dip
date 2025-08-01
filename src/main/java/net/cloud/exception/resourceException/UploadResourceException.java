package net.cloud.exception.resourceException;

public class UploadResourceException extends RuntimeException {
    public UploadResourceException(String message) {
        super(message);
    }

    public UploadResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public UploadResourceException(Throwable cause) {
        super(cause);
    }
}
