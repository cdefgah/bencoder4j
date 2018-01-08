package rafael.osipov.bencoder4j.io;

import rafael.osipov.bencoder4j.BencodeFormatException;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * Helper class to read input stream to get bencoded objects from it.
 */
public final class BencodeStreamReader {

    /**
     * Reference to the input stream.
     */
    private final PushbackInputStream is;

    /**
     * Constructs the reader class instance.
     *
     * @param is reference to input stream.
     */
    public BencodeStreamReader(InputStream is) {
        this.is = new PushbackInputStream(is);
    }

    /**
     * Reads a byte from the stream.
     *
     * @return byte from the stream, or -1 if the end of the stream is reached.
     * @throws IOException if there's an I/O error occurred.
     */
    public int read() throws IOException {
        return this.is.read();
    }

    /**
     * Pushes the byte back to the stream.
     *
     * @param byte2Unread byte to be pushed back to the stream.
     * @throws IOException if there's an I/O error occurred.
     */
    public void unread(int byte2Unread) throws IOException {
        this.is.unread(byte2Unread);
    }

    /**
     * Reads byte sequence and returns the qty of read bytes.
     *
     * @param sequence array to be used as target place upon reading bytes from the stream.
     * @return qty of bytes read, or -1 if the end of the stream has been reached.
     * @throws IOException if there's an input/output error occurred.
     */
    public int readByteSequence(byte[] sequence) throws IOException {
        if ((sequence == null) || (sequence.length == 0)) {
            return 0;
        }

        return is.read(sequence);
    }

    /**
     * Gets character sequence until specified symbol. Useful when you need
     * to read all characters until 'e' or until ':'.
     *
     * @param stopSymbol stop symbol, not included to the result string.
     * @return string, composes from all read characters.
     * @throws IOException            if there's an input/output error occurred upon reading.
     * @throws BencodeFormatException if there's an error related to the bencoding format.
     */
    public String readCharSequence(char stopSymbol) throws IOException, BencodeFormatException {
        boolean stopSymbolIsNotReached = true;
        final StringBuilder sb = new StringBuilder();

        int intValue;
        char charValue;
        while (stopSymbolIsNotReached && ((intValue = is.read()) != -1)) {
            charValue = (char) intValue;

            stopSymbolIsNotReached = (charValue != stopSymbol);

            if (stopSymbolIsNotReached) {
                sb.append(charValue);
            }
        }

        if (stopSymbolIsNotReached) {
            final String exceptionMessage = sb.length() > 0 ? "Stop symbol: '" + stopSymbol + "' was not reached" :
                    "Unexpected end of the stream";
            throw new BencodeFormatException(exceptionMessage);
        }

        return sb.toString();
    }
}