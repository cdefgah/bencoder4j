package rafael.osipov.bencoder4j.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

import rafael.osipov.bencoder4j.BencodeFormatException;
import rafael.osipov.bencoder4j.model.*;


/**
 * Iterator to process serialized bencoded data.
 */
public class BencodeStreamIterator {

    /**
     * Reference to stream reader.
     */
    private final BencodeStreamReader bsr;

    /**
     * if isLocalMode, we're looking for `e` suffix of a List/Dictionary object,
     * otherwise we scan until EOF (end of file).
     * @see rafael.osipov.bencoder4j.model.BencodedList
     * @see rafael.osipov.bencoder4j.model.BencodedDictionary
     */
    private final boolean isLocalMode;

    /**
     * Constructs global iterator, that processes the stream until its end.
     * @param is input stream to work on.
     */
    public BencodeStreamIterator(InputStream is) {
        this.bsr = new BencodeStreamReader(is);
        this.isLocalMode = false;
    }

    /**
     * Constructs local iterator, that processes stream to read elements of List/Dictionary object.
     * @param bsr reference to bencoder4j stream reader.
     * @see rafael.osipov.bencoder4j.model.BencodedList
     * @see rafael.osipov.bencoder4j.model.BencodedDictionary
     */
    public BencodeStreamIterator(BencodeStreamReader bsr) {
        this.bsr = bsr;
        this.isLocalMode = true;
    }

    /**
     * Returns true, if the next call of next() method will return a correct object.
     * @return see method description above.
     * @throws IOException if there's an I/O exception occurred.
     */
    public boolean hasNext() throws IOException {
        final int streamByte = bsr.read();
        bsr.unread(streamByte);

        return isLocalMode ? ((char)streamByte != BencodedObject.SERIALIZED_SUFFIX) : ((byte)streamByte != -1);
    }

    /**
     * Gets the next bencoded object from the stream.
     * @return bencoded object from the stream.
     * @throws IOException if there's an input/output error occurred.
     * @throws BencodeFormatException if there's a bencoding format error occurred.
     */
    public BencodedObject next() throws IOException, BencodeFormatException {

        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        final int objectPrefix = bsr.read();
        bsr.unread(objectPrefix);

        if ((byte)objectPrefix == -1) {
            throw new BencodeFormatException("Unexpected end of the stream");
        }

        BencodedObject bencodedObject;
        switch (objectPrefix) {
        case BencodedInteger.SERIALIZED_PREFIX:
            bencodedObject = new BencodedInteger(bsr);
            break;

        case BencodedList.SERIALIZED_PREFIX:
            bencodedObject = new BencodedList(bsr);
            break;

        case BencodedDictionary.SERIALIZED_PREFIX:
            bencodedObject = new BencodedDictionary(bsr);
            break;

        default:
            if (Character.isDigit(objectPrefix)) {
                bencodedObject = new BencodedByteSequence(bsr);
            } else {
                throw new BencodeFormatException("Unexpected character in the stream: " + (char)objectPrefix);
            }

            break;
        }

        return bencodedObject;
    }
}