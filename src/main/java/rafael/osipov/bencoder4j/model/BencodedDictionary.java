package rafael.osipov.bencoder4j.model;

import rafael.osipov.bencoder4j.BencodeFormatException;
import rafael.osipov.bencoder4j.CircularReferenceException;
import rafael.osipov.bencoder4j.io.BencodeStreamIterator;
import rafael.osipov.bencoder4j.io.BencodeStreamReader;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * Represents bencoded dictionary object.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Bencode">Bencode reference</a>
 * <p>
 * Please note that this implementation is not synchronized.
 * If multiple threads access a BencodedDictionary instance concurrently,
 * and at least one of the threads modifies the object structurally,
 * it must be synchronized externally.
 * </p>
 */
public final class BencodedDictionary extends BencodedObject {

    /**
     * Dictionary prefix character in serialized form.
     */
    public static final char SERIALIZED_PREFIX = 'd';

    /**
     * Dictionary body.
     */
    private final Map<BencodedByteSequence, BencodedObject> dictionary;

    /**
     * Constructs the class instance.
     */
    public BencodedDictionary() {
        this.dictionary = new TreeMap<>();
    }

    /**
     * Constructs the class instance using input stream data.
     *
     * @param bsr input stream reader.
     * @throws BencodeFormatException if there's a problem with bencoding format.
     * @throws IOException            if there's an input/output error occurred.
     */
    public BencodedDictionary(BencodeStreamReader bsr) throws BencodeFormatException, IOException {
        super();

        if (bsr.read() != SERIALIZED_PREFIX) {
            throw new BencodeFormatException(
                    "Incorrect stream position, " +
                            "expected prefix character: " + SERIALIZED_PREFIX);
        }

        this.dictionary = new TreeMap<>();

        final BencodeStreamIterator bsi = new BencodeStreamIterator(bsr);
        while (bsi.hasNext()) {

            BencodedObject bbsKeyObject = bsi.next();
            if (bbsKeyObject instanceof BencodedByteSequence) {
                BencodedByteSequence bbsKey = (BencodedByteSequence) bbsKeyObject;

                if (bsi.hasNext()) {
                    BencodedObject bencodedObject = bsi.next();

                    this.dictionary.put(bbsKey, bencodedObject);

                } else {
                    throw new BencodeFormatException(
                            "Unexpected end of the stream for dictionary. 'Key' object is present, " +
                                    "but 'value' object is not.");
                }
            } else {
                throw new BencodeFormatException("Incorrect object used as dictionary key. Expected: '"
                        + BencodedByteSequence.class.getCanonicalName() + "' but get: '"
                        + bbsKeyObject.getClass().getCanonicalName() + "'");
            }
        }

        // reading suffix
        bsr.read();
    }


    /**
     * Returns true, if the class instance contains either list or dictionary.
     *
     * @return check the method description above.
     */
    public boolean isCompositeObject() {
        return true;
    }

    /**
     * Gets object from the dictionary by key.
     *
     * @param keyObject key to be used to get the object from the dictionary.
     * @return object from the dictionary mapped to the provided keyObject.
     */
    public BencodedObject get(BencodedByteSequence keyObject) {
        return this.dictionary.get(keyObject);
    }

    /**
     * Gets object from the dictionary by key.
     *
     * @param key key to be used to get the object from the dictionary, string
     *            will be converted to BencodedByteSequence object.
     * @return object from the dictionary mapped to the provided key.
     */
    public BencodedObject get(String key) {
        return this.dictionary.get(new BencodedByteSequence(key));
    }

    /**
     * Gets collection of dictionary values.
     *
     * @return collection of dictionary values.
     */
    @Override
    protected Collection<BencodedObject> getCompositeValues() {
        return this.dictionary.values();
    }

    /**
     * Returns the iterator over dictionary keys.
     *
     * @return the iterator over dictionary keys.
     */
    public Iterator<BencodedByteSequence> getKeysIterator() {
        return this.dictionary.keySet().iterator();
    }

    /**
     * Compares the class instance with another instance of this class.
     *
     * @param obj reference to another instance of this class.
     * @return true, if instances are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BencodedDictionary that = (BencodedDictionary) obj;
        return Objects.equals(dictionary, that.dictionary);
    }

    /**
     * Calculates and returns hashcode for the class instance.
     *
     * @return see method description above.
     */
    @Override
    public int hashCode() {

        return Objects.hash(dictionary);
    }

    /**
     * Puts an object to the dictionary.
     *
     * @param keyObject      key to be used to put the object to the dictionary.
     * @param bencodedObject object, to put to the dictionary.
     */
    public void put(BencodedByteSequence keyObject, BencodedObject bencodedObject) {
        checkPutParameters(keyObject, bencodedObject);
        this.dictionary.put(keyObject, bencodedObject);
    }

    /**
     * Puts an object to the dictionary.
     *
     * @param key            key to be used to put the object to the dictionary, the string
     *                       will be converted to BencodedByteSequence instance.
     * @param bencodedObject object, to put to the dictionary.
     */
    public void put(String key, BencodedObject bencodedObject) {
        checkPutParameters(key, bencodedObject);

        final BencodedByteSequence bbsKey = new BencodedByteSequence(key);
        this.dictionary.put(bbsKey, bencodedObject);
    }

    /**
     * Returns the string representation of the class instance.
     *
     * @return see method description above.
     */
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        Iterator<BencodedByteSequence> keysIterator = this.getKeysIterator();
        while (keysIterator.hasNext()) {
            BencodedByteSequence key = keysIterator.next();
            BencodedObject value = dictionary.get(key);
            sb.append("([").append(key.toString()).append("]:[").append(value.toString()).append("])\n");
        }

        sb.append("}");

        return sb.toString();
    }

    /**
     * Writes the class instance to the output stream.
     *
     * @param os output stream instance.
     * @throws IOException                if there's an input/output error occurred.
     * @throws CircularReferenceException if there's a circular reference found upon serializing the object.
     */
    @Override
    public void writeObject(OutputStream os) throws IOException, CircularReferenceException {
        super.writeObject(os);

        os.write(SERIALIZED_PREFIX);

        Set<BencodedByteSequence> keySet = this.dictionary.keySet();
        for (BencodedByteSequence key : keySet) {
            BencodedObject value = this.dictionary.get(key);
            key.writeObject(os);
            value.writeObject(os);
        }

        os.write(SERIALIZED_SUFFIX);
    }

    /**
     * Checks parameters used for put method for this class.
     *
     * @param keyObject      key to be used to put the object to the dictionary.
     * @param bencodedObject object to be put to the dictionary.
     */
    private void checkPutParameters(Object keyObject, BencodedObject bencodedObject) {
        if (null == keyObject) {
            throw new IllegalArgumentException("'keyObject' value for BencodedDictionary cannot be null!");
        }

        if (null == bencodedObject) {
            throw new IllegalArgumentException("'bencodedObject' value for BencodedDictionary cannot be null!");
        }
    }
}