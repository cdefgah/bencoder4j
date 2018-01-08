package com.github.cdefgah.bencoder4j.model;

import com.github.cdefgah.bencoder4j.CircularReferenceException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;

/**
 * Abstract parent object for all bencoded objects.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Bencode">Bencode reference</a>
 */
public abstract class BencodedObject {

    /**
     * Suffix for number, list and dictionary objects in serialized form.
     */
    public static final char SERIALIZED_SUFFIX = 'e';


    /**
     * Returns true, if the class instance contains either list or dictionary.
     *
     * @return check the method description above.
     */
    public boolean isCompositeObject() {
        return false;
    }

    /**
     * Returns the collection of composite object values,
     * used to check circular references.
     * <p>
     * If your class contains composite (list, map) values, override this method to return true,
     *
     * @return see the method description above.
     */
    protected Collection<BencodedObject> getCompositeValues() {
        return Collections.emptyList(); // by default, for non-composite objects
    }


    /**
     * Writes the class instance to the output stream, we're calling this method only from composite class instances,
     * i.e from BencodedList and BencodedDictionary. Calling from BencodedInteger and from BencodedByteSequence does
     * not make sense, because the circular reference verification method always returns false for
     * non-composite classes.
     *
     * @param os output stream instance.
     * @throws IOException                if there's an input/output error occurred.
     * @throws CircularReferenceException if there's a circular reference detected upon writing to the stream.
     */
    protected void writeObject(OutputStream os) throws IOException, CircularReferenceException {

        // we're passing null to isCircularReferenceDetected() method, because we have no parent nodes list
        // when we call this method. But we'll have this parent nodes list on subsequent recursive calls
        // and we'll provide a non-null parameter in this case.
        if (this.isCircularReferenceDetected(null)) {
            throw new CircularReferenceException(
                    "Upon writing to stream, circular reference found in " + this.getClass().getCanonicalName());
        }
    }


    /**
     * Checks if a circular reference detected, and returns true if it is.
     *
     * @param parentNodes parent nodes map to track circular references, specify null for the top first call.
     * @return true if a circular reference is detected.
     */
    private boolean isCircularReferenceDetected(IdentityHashMap<BencodedObject, String> parentNodes) {
        boolean foundCircularReference = false;

        if (parentNodes == null) {
            parentNodes = new IdentityHashMap<>();
        }

        parentNodes.put(this, "");

        final Collection<BencodedObject> values = getCompositeValues();
        for (BencodedObject bencodedObject : values) {
            if (bencodedObject.isCompositeObject()) {

                foundCircularReference = parentNodes.containsKey(bencodedObject)
                        || bencodedObject.isCircularReferenceDetected(parentNodes);
                if (foundCircularReference) {
                    break;
                }
            }
        }

        parentNodes.remove(this);

        return foundCircularReference;
    }
}