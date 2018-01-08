package com.github.cdefgah.bencoder4j.model;

import com.github.cdefgah.bencoder4j.BencodeFormatException;
import com.github.cdefgah.bencoder4j.io.BencodeStreamReader;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Represents bencoded byte sequence object (also known as "byte string").
 *
 * @see <a href="https://en.wikipedia.org/wiki/Bencode">Bencode reference</a>
 */
public final class BencodedByteSequence extends BencodedObject implements Comparable<BencodedByteSequence> {

    /**
     * Character used to separate byte sequence length from the byte sequence body.
     */
    private static final char DELIMITER = ':';

    /**
     * Byte sequence body.
     */
    private final byte[] sequence;

    /**
     * Reads the input stream and constructs the class instance.
     *
     * @param bsr stream reader instance.
     * @throws IOException            if there's an input/output error occurred.
     * @throws BencodeFormatException if there's an error related to bencoding format.
     */
    public BencodedByteSequence(BencodeStreamReader bsr) throws IOException, BencodeFormatException {

        final String sequenceLengthStrValue = bsr.readCharSequence(DELIMITER);
        if (sequenceLengthStrValue.length() == 0) {
            throw new BencodeFormatException("BencodedByteSequence length part is not present in the stream");
        }

        try {
            final int sequenceLength = Integer.parseInt(sequenceLengthStrValue);
            this.sequence = new byte[sequenceLength];
            final int bytesRead = bsr.readByteSequence(this.sequence);
            if (bytesRead != sequenceLength) {
                throw new BencodeFormatException("Unexpected end of the byte sequence stream");
            }
        } catch (NumberFormatException nfe) {
            throw new BencodeFormatException("BencodedByteSequence length cannot be converted to a numeric value");
        }
    }

    /**
     * Constructs class instance.
     *
     * @param sequence byte array to be used as source for byte sequence.
     */
    public BencodedByteSequence(byte[] sequence) {
        if ((sequence != null) && (sequence.length > 0)) {
            this.sequence = new byte[sequence.length];
            System.arraycopy(sequence, 0, this.sequence, 0, sequence.length);
        } else {
            this.sequence = new byte[0];
        }
    }

    /**
     * Constructs class instance.
     *
     * @param string initial string to be used as source for byte sequence.
     */
    public BencodedByteSequence(String string) {
        if ((string != null) && (string.length() > 0)) {
            this.sequence = string.getBytes(StandardCharsets.UTF_8);
        } else {
            this.sequence = new byte[0];
        }
    }

    /**
     * Compares the current object with another instance of this class.
     *
     * @param anotherObject reference to another instance of this class.
     * @return 1 if this object is greater than another object, -1 if it is less
     * than another object, and 0 if they are equal.
     */
    @Override
    public int compareTo(BencodedByteSequence anotherObject) {
        return this.toUTF8String().compareTo(anotherObject.toUTF8String());
    }

    /**
     * Returns the safe copy of byte sequence body of the class instance.
     *
     * @return see method description above.
     */
    public byte[] getByteSequence() {
        final byte[] byteSequence = new byte[this.sequence.length];
        System.arraycopy(this.sequence, 0, byteSequence, 0, this.sequence.length);
        return byteSequence;
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
        BencodedByteSequence that = (BencodedByteSequence) obj;
        return Arrays.equals(sequence, that.sequence);
    }

    /**
     * Calculates and returns hashcode for the class instance.
     *
     * @return see method description above.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(sequence);
    }

    /**
     * Gets the length of the sequence byte array.
     *
     * @return see method description above.
     */
    public int length() {
        return this.sequence.length;
    }

    /**
     * Returns the string representation of the class instance.
     *
     * @return see method description above.
     */
    @Override
    public String toString() {
        return Arrays.toString(sequence);
    }

    /**
     * Returns the utf-8 string composed using the byte sequence.
     *
     * @return see method description above.
     */
    public String toUTF8String() {
        return new String(this.sequence, StandardCharsets.UTF_8);
    }

    /**
     * Writes the class instance to the output stream.
     *
     * @param os output stream instance.
     * @throws IOException if there's an input/output error occurred.
     */
    @Override
    public void writeObject(OutputStream os) throws IOException {

        final char[] sequenceLengthCharArray = String.valueOf(this.sequence.length).toCharArray();
        for (char oneChar : sequenceLengthCharArray) {
            os.write(oneChar);
        }

        os.write(DELIMITER);
        os.write(this.sequence);
    }
}