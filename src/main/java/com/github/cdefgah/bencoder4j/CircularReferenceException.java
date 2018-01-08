package com.github.cdefgah.bencoder4j;

/**
 * The exception to be thrown if there's a circular reference found.
 */
public final class CircularReferenceException extends Exception {

    /**
     * Constructs the exception object.
     *
     * @param message problem description.
     */
    public CircularReferenceException(String message) {
        super(message);
    }
}