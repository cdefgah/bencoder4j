package rafael.osipov.bencoder4j.model;

import rafael.osipov.bencoder4j.BencodeFormatException;
import rafael.osipov.bencoder4j.CircularReferenceException;
import rafael.osipov.bencoder4j.io.BencodeStreamIterator;
import rafael.osipov.bencoder4j.io.BencodeStreamReader;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * Represents a list of BencodedObjects.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Bencode">Bencode reference</a>
 * <p>
 * Please note that this implementation is not synchronized.
 * If multiple threads access a BencodedList instance concurrently, and at least one of the threads
 * modifies the list structurally, it must be synchronized externally.
 * </p>
 */
public final class BencodedList extends BencodedObject implements Iterable<BencodedObject> {

    /**
     * List prefix character in serialized form.
     */
    public static final char SERIALIZED_PREFIX = 'l';

    /**
     * The list body.
     */
    private final List<BencodedObject> listContents;

    /**
     * Constructs the class instance.
     */
    public BencodedList() {
        super();
        this.listContents = new ArrayList<>();
    }

    /**
     * Constructs the class instance using stream reader.
     *
     * @param bsr reference to the bencoder4j stream reader
     * @throws IOException            if there's an input/output error occurred.
     * @throws BencodeFormatException if there's an error in bencoding format.
     */
    public BencodedList(BencodeStreamReader bsr)
            throws IOException, BencodeFormatException {
        super();

        if (bsr.read() != SERIALIZED_PREFIX) {
            throw new BencodeFormatException(
                    "Incorrect stream position, " +
                            "expected prefix character: " + SERIALIZED_PREFIX);
        }

        this.listContents = new ArrayList<>();

        final BencodeStreamIterator bsi = new BencodeStreamIterator(bsr);
        while (bsi.hasNext()) {
            listContents.add(bsi.next());
        }

        // reading suffix
        bsr.read();
    }

    /**
     * Returns true, if the class instance contains either list or dictionary.
     * @return check the method description above.
     */
    public boolean isCompositeObject() {
        return true;
    }

    /**
     * Writes the class instance to the output stream.
     *
     * @param os output stream instance.
     * @throws IOException                if there's an input/output error occurred.
     * @throws CircularReferenceException if there's a circular reference found
     *                                    upon serializing the object.
     */
    @Override
    public void writeObject(OutputStream os) throws IOException, CircularReferenceException {
        super.writeObject(os);

        os.write(SERIALIZED_PREFIX);
        for (BencodedObject bencodedObject : listContents) {
            bencodedObject.writeObject(os);
        }
        os.write(SERIALIZED_SUFFIX);
    }

    /**
     * Adds an object to the list.
     *
     * @param bo the object to be added.
     */
    public void add(BencodedObject bo) {
        checkObjectToBeAdded(bo);
        this.listContents.add(bo);
    }

    /**
     * Adds an object to the list using index.
     *
     * @param index index to be used to add the object.
     * @param bo    the object to be added.
     */
    public void add(int index, BencodedObject bo) {
        checkObjectToBeAdded(bo);
        this.listContents.add(index, bo);
    }


    /**
     * Gets a bencoded object from the list by provided index.
     *
     * @param index item index in the list.
     * @return the reference to bencoded object from the list.
     */
    public BencodedObject get(int index) {
        checkListIndex(index);
        return listContents.get(index);
    }


    /**
     * Removes the object from the list.
     *
     * @param index index of the object to be removed.
     * @return reference to the removed object.
     */
    public BencodedObject remove(int index) {
        checkListIndex(index);
        return listContents.remove(index);
    }

    /**
     * Removes the object from the list.
     *
     * @param bencodedObject object to be removed.
     * @return true if object has been removed from the list.
     */
    public boolean remove(BencodedObject bencodedObject) {
        return listContents.remove(bencodedObject);
    }

    /**
     * Clears the list contents.
     */
    public void clear() {
        listContents.clear();
    }


    /**
     * Returns true if the list contains the specified object.
     * @param object element whose presence in this list is to be tested.
     * @return true, if element has been found.
     */
    public boolean contains(BencodedObject object) {
        return listContents.contains(object);
    }

    /**
     * Returns the size of the list.
     *
     * @return the size of the list.
     */
    public int size() {
        return listContents.size();
    }

    /**
     * Returns the string representation of the class instance.
     *
     * @return see method description above.
     */
    @Override
    public String toString() {
        return Arrays.toString(listContents.toArray());
    }

    /**
     * Returns the iterator over the list contents.
     *
     * @return the iterator over the list contents.
     */
    @Override
    public Iterator<BencodedObject> iterator() {
        return listContents.iterator();
    }

    /**
     * Gets collection of list values.
     *
     * @return collection of list values.
     */
    @Override
    protected Collection<BencodedObject> getCompositeValues() {
        return listContents;
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
        BencodedList that = (BencodedList) obj;
        return Objects.equals(listContents, that.listContents);
    }

    /**
     * Calculates and returns hashcode for the class instance.
     *
     * @return see method description above.
     */
    @Override
    public int hashCode() {
        return Objects.hash(listContents);
    }


    /**
     * Checks the provided list index on correctness.
     *
     * @param index index to be checked.
     */
    private void checkListIndex(int index) {
        if (index < 0 || index >= listContents.size()) {
            throw new IllegalArgumentException("Incorrect index value: " + index +
                    " for collection with size: " + listContents.size());
        }
    }

    /**
     * Checks the object to be added to the list.
     *
     * @param bo the object to be added.
     */
    private void checkObjectToBeAdded(BencodedObject bo) {
        if (bo == null) {
            throw new IllegalArgumentException("Null objects are not allowed for BencodedList");
        }
    }
}