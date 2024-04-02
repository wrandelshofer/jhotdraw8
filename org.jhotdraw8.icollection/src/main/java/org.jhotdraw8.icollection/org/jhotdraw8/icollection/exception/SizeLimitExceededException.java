package org.jhotdraw8.icollection.exception;

import java.io.Serial;

/**
 * This exception is thrown when a method exceeds a size limit.
 */
public class SizeLimitExceededException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 0L;
    /**
     * Constructs an instance without a message.
     */
    public SizeLimitExceededException() {
    }

    /**
     * Constructs an instance with the specified error message.
     *
     * @param message the error message
     */
    public SizeLimitExceededException(String message) {
        super(message);
    }

    /**
     * Constructs an instance with the specified error message
     * and cause.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public SizeLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an instance with the specified underlying cause.
     *
     * @param cause the underlying cause
     */
    public SizeLimitExceededException(Throwable cause) {
        super(cause);
    }
}
