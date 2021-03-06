package com.github.cdefgah.bencoder4j.model;

import com.github.cdefgah.bencoder4j.BencodeFormatException;
import com.github.cdefgah.bencoder4j.io.BencodeStreamReader;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * This class represents b-encoded integer.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Bencode">Bencode reference</a>
 */
public final class BencodedInteger extends BencodedObject implements Comparable<BencodedInteger> {

    /**
     * Number prefix character in serialized form.
     */
    public static final char SERIALIZED_PREFIX = 'i';

    /**
     * Numeric representation.
     */
    private final long value;

    /**
     * Class constructor, used to de-serialize the class instance from
     * InputStream instance.
     *
     * @param bsr stream reader to be used for deserialization purposes.
     * @throws IOException            if there's an I/O error occurred upon the processing.
     * @throws BencodeFormatException if there are b-encoding format errors found upon the
     *                                processing.
     */
    public BencodedInteger(BencodeStreamReader bsr) throws IOException, BencodeFormatException {
        super();

        if (bsr.read() != SERIALIZED_PREFIX) {
            throw new BencodeFormatException(
                    "Incorrect stream position, " +
                            "expected prefix character: " + SERIALIZED_PREFIX);
        }

        final String charSequenceBody = bsr.readCharSequence(SERIALIZED_SUFFIX);

        try {
            this.value = Long.parseLong(charSequenceBody);
            String checkString = String.valueOf(this.value);
            if (!charSequenceBody.equals(checkString)) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException nfe) {
            throw new BencodeFormatException("Incorrect character sequence for the value");
        }
    }

    /**
     * Class constructor.
     *
     * @param value value to initialize the class instance.
     */
    public BencodedInteger(long value) {
        super();
        this.value = value;
    }

    /**
     * Returns a BencodedInteger object holding the value of the specified String.
     *
     * The argument is interpreted as representing a signed decimal long,
     * exactly as if the argument were given to the Long.parseLong(java.lang.String) method.
     *
     * @param string the string to be parsed to long value and then used to build a BencodedInteger instance.
     *
     * @return a BencodedInteger object holding the long integer value represented by the string argument.
     *
     * @throws NumberFormatException If the string cannot be parsed as a long.
     */
    public static BencodedInteger valueOf(String string) throws NumberFormatException {
        return new BencodedInteger(Long.valueOf(string));
    }


    /**
     * Returns a BencodedInteger object holding the value extracted from the specified String
     * when parsed with the radix given by the second argument.
     *
     * The first argument is interpreted as representing a signed long in the radix specified by the second argument,
     * exactly as if the arguments were given to the Long.parseLong(java.lang.String, int) method.
     *
     * @param string the string to be parsed to long value and then used to build a BencodedInteger instance.
     *
     * @param radix the radix to be used in interpreting string.
     *
     * @return a BencodedInteger object holding the long integer value
     * represented by the string argument in the specified radix.
     *
     * @throws NumberFormatException If the String does not contain a parsable long.
     */
    public static BencodedInteger valueOf(String string, int radix) throws NumberFormatException {
        return new BencodedInteger(Long.valueOf(string, radix));
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
        BencodedInteger that = (BencodedInteger) obj;
        return value == that.value;
    }

    /**
     * Calculates and returns hashcode for the class instance.
     *
     * @return see method description above.
     */
    @Override
    public int hashCode() {

        return Objects.hash(value);
    }


    /**
     * Returns the stored numeric value as long integer.
     *
     * @return the stored numeric value as long integer.
     */
    public long getValue() {
        return this.value;
    }

    /**
     * Returns the string representation of the class instance.
     *
     * @return the string representation of the class instance.
     */
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Writes the class instance to the output stream.
     *
     * @param os output stream instance.
     * @throws IOException if there's an input/output error occurred.
     */
    @Override
    public void writeObject(OutputStream os) throws IOException {

        os.write(SERIALIZED_PREFIX);

        final char[] contentsCharArray = String.valueOf(this.value).toCharArray();
        for (char oneChar : contentsCharArray) {
            os.write(oneChar);
        }
        os.write(SERIALIZED_SUFFIX);
    }

    /**
     * Compares this object with the specified object for order. Returns a negative integer, zero,
     * or a positive integer as this object is less than, equal to, or greater than the specified object.
     *
     * @param anotherObject the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less than,
     * equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(BencodedInteger anotherObject) {
        return Long.compare(this.getValue(), anotherObject.getValue());
    }
}