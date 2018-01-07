package rafael.osipov.bencoder4j;

/**
 * The exception to be thrown if there is a problem with bencoding format.
 */
public class BencodeFormatException extends Exception {

    /**
     * Constructs the exception object.
     * @param message the problem description.
     */
    public BencodeFormatException(String message) {
        super(message);
    }
}
