package com.bruteforce.ila.common.exception;

/**
 * Thrown when the AI provider answers, but not in a form the app can use
 * (for example, malformed JSON). Shown to the user as a 502 with a clear message.
 */
public class AiResponseException extends RuntimeException {

    public AiResponseException(String message) {
        super(message);
    }

    public AiResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
