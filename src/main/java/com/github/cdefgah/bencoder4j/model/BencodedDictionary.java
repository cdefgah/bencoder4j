package com.github.cdefgah.bencoder4j.model;

import com.github.cdefgah.bencoder4j.BencodeFormatException;
import com.github.cdefgah.bencoder4j.CircularReferenceException;
import com.github.cdefgah.bencoder4j.io.BencodeStreamIterator;
import com.github.cdefgah.bencoder4j.io.BencodeStreamReader;

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
    private final Map<BencodedByteSequence, BencodedObject> dictionary = new TreeMap<>();

    /**
     * Constructs the class instance.
     */
    public BencodedDictionary() {
        super();
    }

    /**
     * Constructs the class instance, based on a map with bencoded objects.
     *
     * @param initialMap initial map.
     * @throws IllegalArgumentException if initialMap contains null keys or values.
     */
    public BencodedDictionary(Map<BencodedByteSequence, BencodedObject> initialMap) {
        super();

        if (initialMap == null) {
            return;
        }

        for (Map.Entry<BencodedByteSequence, BencodedObject> entry: initialMap.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
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
     * @throws IllegalArgumentException if key is null.
     */
    public BencodedObject get(BencodedByteSequence keyObject) {
        checkNullKey(keyObject);
        return this.dictionary.get(keyObject);
    }

    /**
     * Gets object from the dictionary by key.
     *
     * @param key key to be used to get the object from the dictionary, string
     *            will be converted to BencodedByteSequence object.
     * @return object from the dictionary mapped to the provided key.
     * @throws IllegalArgumentException if key is null.
     */
    public BencodedObject get(String key) {
        checkNullKey(key);
        return this.dictionary.get(new BencodedByteSequence(key));
    }

    /**
     * Gets collection of dictionary values.
     *
     * @return collection of dictionary values.
     */
    @Override
    Collection<BencodedObject> getCompositeValues() {
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
     * @throws IllegalArgumentException if either key or value is null.
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
     * @throws IllegalArgumentException if either key or value is null.
     */
    public void put(String key, BencodedObject bencodedObject) {
        checkPutParameters(key, bencodedObject);

        final BencodedByteSequence bbsKey = new BencodedByteSequence(key);
        this.dictionary.put(bbsKey, bencodedObject);
    }

    /**
     * Removes all of the mappings from this map. The map will be empty after this call returns.
     */
    public void clear() {
        this.dictionary.clear();
    }

    /**
     * Returns true if this map contains a mapping for the specified key.
     *
     * @param key the key.
     * @return true if this map contains a mapping for the specified key.
     * @throws IllegalArgumentException if key is null.
     */
    public boolean containsKey(BencodedByteSequence key) {
        checkNullKey(key);
        return this.dictionary.containsKey(key);
    }

    /**
     * Returns true if this map contains a mapping for the specified key.
     *
     * @param key the key.
     * @return true if this map contains a mapping for the specified key.
     * @throws IllegalArgumentException if key is null.
     */
    public boolean containsKey(String key) {
        checkNullKey(key);
        return this.dictionary.containsKey(new BencodedByteSequence(key));
    }

    /**
     * Returns true if this map maps one or more keys to the specified value.
     * More formally, returns true if and only if this map contains at least
     * one mapping to a value v such that (value==null ? v==null : value.equals(v)).
     * <p>
     * This operation will probably require time linear in the map size for most implementations.
     *
     * @param value value whose presence in this map is to be tested.
     * @return true if a mapping to value exists; false otherwise.
     * @throws IllegalArgumentException if value is null.
     */
    public boolean containsValue(BencodedObject value) {
        if (value == null) {
            throw new IllegalArgumentException("Null values are not allowed for BencodedDictionary instances");
        }

        return this.dictionary.containsValue(value);
    }

    /**
     * Removes the mapping for this key from this TreeMap if present.
     *
     * @param key key for which mapping should be removed.
     * @return the previous value associated with key, or null if there was no mapping for key.
     * @throws IllegalArgumentException if key is null.
     */
    public BencodedObject remove(BencodedByteSequence key) {
        checkNullKey(key);
        return this.dictionary.remove(key);
    }

    /**
     * Removes the mapping for this key from this TreeMap if present.
     *
     * @param key key for which mapping should be removed.
     * @return the previous value associated with key, or null if there was no mapping for key.
     * @throws IllegalArgumentException if key is null.
     */
    public BencodedObject remove(String key) {
        checkNullKey(key);
        return this.dictionary.remove(new BencodedByteSequence(key));
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map.
     */
    public int size() {
        return this.dictionary.size();
    }

    /**
     * Returns the iterator over dictionary values.
     *
     * @return the iterator over dictionary values.
     */
    public Iterator<BencodedObject> getValuesIterator() {
        return this.dictionary.values().iterator();
    }

    /**
     * Returns true if dictionary is empty.
     *
     * @return true if dictionary is empty.
     */
    public boolean isEmpty() {
        return this.dictionary.isEmpty();
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
     * @throws IllegalArgumentException if either of both key or value objects are nulls.
     */
    private static void checkPutParameters(Object keyObject, BencodedObject bencodedObject) {
        if (null == keyObject) {
            throw new IllegalArgumentException("'keyObject' value for BencodedDictionary cannot be null!");
        }

        if (null == bencodedObject) {
            throw new IllegalArgumentException("'bencodedObject' value for BencodedDictionary cannot be null!");
        }
    }

    /**
     * Checks if key is null. If key is not null, nothing happens, otherwise NullPointerException is being thrown.
     * @param key key to be checked.
     * @throws IllegalArgumentException if key is null.
     */
    private static void checkNullKey(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("Null keys are not allowed for BencodedDictionary instances");
        }
    }
}