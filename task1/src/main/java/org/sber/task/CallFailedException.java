package org.sber.task;

public class CallFailedException extends RuntimeException {
    public CallFailedException() {
    }

    public CallFailedException(String message) {
        super(message);
    }

    public CallFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public CallFailedException(Throwable cause) {
        super(cause);
    }
}
