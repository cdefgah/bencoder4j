package com.github.cdefgah.bencoder4j.model;

import com.github.cdefgah.bencoder4j.BencodeFormatException;
import org.junit.jupiter.api.Test;
import com.github.cdefgah.bencoder4j.io.BencodeStreamReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class BencodedByteSequenceTest {

    @Test
    void testEmptyByteSequence() {

        byte[] initialSequence = new byte[0];
        BencodedByteSequence bbs = new BencodedByteSequence(initialSequence);
        byte[] composedSequence = bbs.getByteSequence();

        assertAll("Validating state of empty object",
                () -> assertEquals(0, bbs.length()),
                () -> assertEquals(0, composedSequence.length)
        );
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testNullByteSequence() {

        byte[] initialSequence = null;
        BencodedByteSequence bbs = new BencodedByteSequence(initialSequence);
        byte[] composedSequence = bbs.getByteSequence();

        assertAll("Validating state of empty object",
                () -> assertEquals(0, bbs.length()),
                () -> assertEquals(0, composedSequence.length)
        );
    }

    @Test
    void testEmptyStringSequence() {

        String initialString = "";
        BencodedByteSequence bbs = new BencodedByteSequence(initialString);
        byte[] composedSequence = bbs.getByteSequence();

        assertAll("Validating state of empty object",
                () -> assertEquals(0, bbs.length()),
                () -> assertEquals(0, composedSequence.length)
        );
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testNullStringSequence() {

        String initialString = null;
        BencodedByteSequence bbs = new BencodedByteSequence(initialString);
        byte[] composedSequence = bbs.getByteSequence();

        assertAll("Validating state of empty object",
                () -> assertEquals(0, bbs.length()),
                () -> assertEquals(0, composedSequence.length)
        );
    }

    @Test
    void objectsConformToTheMainComparableRules() {

        BencodedByteSequence bbsWithMaxValue1 = new BencodedByteSequence("ZZZ");
        BencodedByteSequence bbsWithMaxValue2 = new BencodedByteSequence("ZZZ");
        BencodedByteSequence bbsWithMinValue = new BencodedByteSequence("AAA");

        assertAll("Validating the conformance to compareTo contract",

                () -> assertEquals(0, bbsWithMaxValue1.compareTo(bbsWithMaxValue2),
                        "Equal comparable objects should return 0 on compareTo() call"),

                () -> assertTrue(bbsWithMaxValue1.compareTo(bbsWithMinValue) > 0,
                        "Calling compareTo() of a bigger comparable object " +
                                "with a smaller comparable object as an argument, should return positive integer"),

                () -> assertTrue(bbsWithMinValue.compareTo(bbsWithMaxValue1) < 0,
                        "Calling compareTo() of a smaller comparable object " +
                                "with a bigger comparable object as an argument, should return negative integer")
        );
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void compareWithNull() {
        assertThrows(NullPointerException.class, () -> {
            BencodedByteSequence bbs = new BencodedByteSequence("AAA");
            bbs.compareTo(null);
        }, "The class does not conform to the Oracle recommendation for compareTo() " +
                "when there is a null argument passed," +
                "this method should throw NullPointerException, but it does not");
    }

    @Test
    void compareInBothDirections() {
        BencodedByteSequence bbs1 = new BencodedByteSequence("ZZZ");
        BencodedByteSequence bbs2 = new BencodedByteSequence("AAA");

        // checking for: sgn(x.compareTo(y)) == -sgn(y.compareTo(x))
        assertEquals(Integer.signum(bbs1.compareTo(bbs2)),
                -1 * Integer.signum(bbs2.compareTo(bbs1)),
                "compareTo() method of comparable objects should work in both directions");
    }

    @Test
    void compareToIsTransitive() {
        BencodedByteSequence x = new BencodedByteSequence("ZZZ");
        BencodedByteSequence y = new BencodedByteSequence("MMM");
        BencodedByteSequence z = new BencodedByteSequence("AAA");

        assertAll("Ensuring that: (x.compareTo(y)>0 && y.compareTo(z)>0) implies x.compareTo(z)>0",
                () -> assertTrue(x.compareTo(y) > 0),
                () -> assertTrue(y.compareTo(z) > 0),
                () -> assertTrue(x.compareTo(z) > 0)
        );
    }

    @Test
    void compareToContractAdditionalRuleIsFulfilled() {
        // x.compareTo(y)==0 implies that sgn(x.compareTo(z)) == sgn(y.compareTo(z)), for all z.
        // (x.compareTo(y)==0) == (x.equals(y)).

        BencodedByteSequence x = new BencodedByteSequence("AAA");
        BencodedByteSequence y = new BencodedByteSequence("AAA");
        BencodedByteSequence z = new BencodedByteSequence("ZZZ");

        assertAll("x.compareTo(y)==0 implies that sgn(x.compareTo(z)) == sgn(y.compareTo(z)), for all z, " +
                        "(x.compareTo(y)==0) == (x.equals(y)).",
                () -> assertEquals(0, x.compareTo(y)),
                () -> assertEquals(Integer.signum(x.compareTo(z)), Integer.signum(y.compareTo(z))),
                () -> assertEquals(x, y)
        );
    }


    @Test
    void getValueWorksCorrectly() {
        byte[] initialSequence = "Последовательность ABC".getBytes(StandardCharsets.UTF_8);
        BencodedByteSequence bbs = new BencodedByteSequence(initialSequence);

        byte[] assumedSafeCopy = bbs.getByteSequence();

        assertAll("Ensuring that getValue returns safe copy equal to the reference array",
                () -> assertNotSame(initialSequence, assumedSafeCopy),
                () -> assertTrue(Arrays.equals(initialSequence, assumedSafeCopy))
        );
    }


    @Test
    void equalsIsSymmetric() {
        // for a and b, if a.equals(b), then b.equals(a);
        BencodedByteSequence bbs1 = new BencodedByteSequence("Значение 123-ABC");
        BencodedByteSequence bbs2 = new BencodedByteSequence("Значение 123-ABC");

        assertAll("Ensuring that equals is symmetric",
                () -> assertEquals(bbs1, bbs2),
                () -> assertEquals(bbs2, bbs1)
        );
    }

    @SuppressWarnings("EqualsWithItself")
    @Test
    void equalsIsReflexive() {
        // when a!=null, a.equals(a);
        BencodedByteSequence bbs1 = new BencodedByteSequence("Значение 123-ABC");

        assertEquals(bbs1, bbs1);
    }

    @Test
    void equalsIsTransitive() {
        // if a.equals(b), and b.equals(c), then a.equals(c);

        BencodedByteSequence bbs1 = new BencodedByteSequence("Некое значение ABC-456");
        BencodedByteSequence bbs2 = new BencodedByteSequence("Некое значение ABC-456");
        BencodedByteSequence bbs3 = new BencodedByteSequence("Некое значение ABC-456");

        assertAll("Ensuring that equals is transitive",
                () -> assertEquals(bbs1, bbs2),
                () -> assertEquals(bbs2, bbs3),
                () -> assertEquals(bbs1, bbs3)
        );
    }

    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    @Test
    void equalsReturnsFalseForDifferentObjectClass() {
        // if we're comparing two objects of different classes
        // equals should return false

        String someStringObject = "1234567890";
        BencodedByteSequence bbs1 = new BencodedByteSequence(someStringObject);

        assertNotEquals(bbs1, someStringObject);
    }

    @Test
    void equalsReturnsFalseForObjectsWithDifferentValues() {
        // if we're comparing two objects of the same class,
        // but with different values,
        // equals should return false

        BencodedByteSequence bbs1 = new BencodedByteSequence("Некое значение ABC-456");
        BencodedByteSequence bbs2 = new BencodedByteSequence("Некое значение XYZ-789");

        assertNotEquals(bbs1, bbs2);
    }

    @Test
    void hashCodesAreEqualForEqualObjects() {
        // if a.equals(b), then a.hashCode() == b.hashCode();

        BencodedByteSequence bbs1 = new BencodedByteSequence("Некое значение ABC-456");
        BencodedByteSequence bbs2 = new BencodedByteSequence("Некое значение ABC-456");

        assertAll("Hash codes are equal for equal objects",
                () -> assertEquals(bbs1, bbs2),
                () -> assertEquals(bbs1.hashCode(), bbs2.hashCode())
        );
    }

    @SuppressWarnings("ObjectEqualsNull")
    @Test
    void equalsToNullReturnsFalse() {
        // a.equals(null) should return false
        BencodedByteSequence bbs1 = new BencodedByteSequence("Некое значение ABC-456");

        assertNotEquals(null, bbs1);
    }

    @Test
    void toStringReturnsCorrectValue() {
        String initialString = "Некое значение ABC-456";
        String expectedStringValue =
                Arrays.toString(initialString.getBytes(StandardCharsets.UTF_8));
        BencodedByteSequence bbs1 = new BencodedByteSequence(initialString);

        assertEquals(expectedStringValue, bbs1.toString());
    }

    @Test
    void toUTF8StringReturnsCorrectValue() {
        String initialString = "Некое значение ABC-456";
        BencodedByteSequence bbs1 = new BencodedByteSequence(initialString);

        assertEquals(initialString, bbs1.toUTF8String());
    }

    @Test
    void instantiationFromStreamWorksCorrectlyForLongSequence() throws
            IOException, BencodeFormatException {

        String referenceStringValue = "Некое строковое значение ABC-0123";
        deserializeSequence(referenceStringValue);
    }


    @Test
    void instantiationFromStreamWorksCorrectlyForShortSequence() throws
            IOException, BencodeFormatException {

        String referenceStringValue = "abc-1";
        deserializeSequence(referenceStringValue);
    }

    @Test
    void instantiationFromStreamWorksCorrectlyForEmptySequence() throws
            IOException, BencodeFormatException {

        String referenceStringValue = "";
        deserializeSequence(referenceStringValue);
    }

    @Test
    void instantiationFromStreamWithIncorrectSequenceLength() {

        String streamContents = "5:1234";
        InputStream is = new ByteArrayInputStream(streamContents.getBytes(StandardCharsets.UTF_8));
        BencodeStreamReader bsr = new BencodeStreamReader(is);

        BencodeFormatException exception = assertThrows(BencodeFormatException.class, () ->
                        new BencodedByteSequence(bsr),
                "BencodedByteSequence constructor behaves incorrectly on the " +
                        "stream data with incorrect sequence length");

        assertEquals("Unexpected end of the byte sequence stream",
                exception.getMessage(),
                "Unexpected message in correctly thrown exception");
    }


    @Test
    void instantiationFromStreamWithNotNumericSymbolInSequenceLength() {

        String streamContents = "5x:1234";
        InputStream is = new ByteArrayInputStream(streamContents.getBytes(StandardCharsets.UTF_8));
        BencodeStreamReader bsr = new BencodeStreamReader(is);

        BencodeFormatException exception = assertThrows(BencodeFormatException.class, () ->
                        new BencodedByteSequence(bsr),
                "BencodedByteSequence constructor behaves incorrectly on the " +
                        "stream data with incorrect sequence length");

        assertEquals("BencodedByteSequence length cannot be converted to a numeric value",
                exception.getMessage(),
                "Unexpected message in correctly thrown exception");
    }


    @Test
    void instantiationFromStreamWithNotNumericSequenceLength() {

        String streamContents = "x:1234";
        InputStream is = new ByteArrayInputStream(streamContents.getBytes(StandardCharsets.UTF_8));
        BencodeStreamReader bsr = new BencodeStreamReader(is);

        BencodeFormatException exception = assertThrows(BencodeFormatException.class, () ->
                        new BencodedByteSequence(bsr),
                "BencodedByteSequence constructor behaves incorrectly on the " +
                        "stream data with incorrect sequence length");

        assertEquals("BencodedByteSequence length cannot be converted to a numeric value",
                exception.getMessage(),
                "Unexpected message in correctly thrown exception");
    }

    @Test
    void gettingUTF8StringWorksProperly() throws IOException, BencodeFormatException {

        String referenceString = "НеКоЕ соЧеТаНиЕ СиМвОлОв 1234567890";
        String lengthPart = String.valueOf(referenceString.getBytes(StandardCharsets.UTF_8).length);

        String streamContents = lengthPart + ":" + referenceString;

        InputStream is = new ByteArrayInputStream(streamContents.getBytes(StandardCharsets.UTF_8));
        BencodeStreamReader bsr = new BencodeStreamReader(is);

        BencodedByteSequence bbs = new BencodedByteSequence(bsr);

        assertEquals(referenceString, bbs.toUTF8String());
    }


    @Test
    void serializeEmptySequence() throws IOException {

        byte[] referenceByteSequence = "".getBytes(StandardCharsets.UTF_8);
        String expectedSerializedForm = "0:";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        BencodedByteSequence bbs = new BencodedByteSequence(referenceByteSequence);
        bbs.writeObject(baos);

        String actualSerializedForm = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        assertEquals(expectedSerializedForm, actualSerializedForm);
    }


    @Test
    void serializeNonEmptySequenceBasedOnByteSequenceInit() throws IOException {

        String referenceString = "Некая последовательность символов ABC-123";
        byte[] referenceByteSequence = referenceString.getBytes(StandardCharsets.UTF_8);
        String expectedSerializedForm = String.valueOf(referenceByteSequence.length) + ":" + referenceString;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        BencodedByteSequence bbs = new BencodedByteSequence(referenceByteSequence);
        bbs.writeObject(baos);

        String actualSerializedForm = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        assertEquals(expectedSerializedForm, actualSerializedForm);
    }


    @Test
    void serializeNonEmptySequenceBasedOnStringInit() throws IOException {

        String referenceString = "Последовательность разных символов XYZ-789";
        byte[] referenceByteSequence = referenceString.getBytes(StandardCharsets.UTF_8);
        String expectedSerializedForm = String.valueOf(referenceByteSequence.length) + ":" + referenceString;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        BencodedByteSequence bbs = new BencodedByteSequence(referenceString);
        bbs.writeObject(baos);

        String actualSerializedForm = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        assertEquals(expectedSerializedForm, actualSerializedForm);
    }


    @Test
    void testNonCompositeState() {
        BencodedByteSequence bbs = new BencodedByteSequence("abc");

        assertAll("Ensuring the correct composite object state",
                () -> assertFalse(bbs.isCompositeObject()),
                () -> assertEquals(0, bbs.getCompositeValues().size())
        );
    }

    @Test
    void deserializeObjectWithMissingLengthPart() {
        final String streamContents = ":Некий набор символов abc-123";
        InputStream is = new ByteArrayInputStream(streamContents.getBytes(StandardCharsets.UTF_8));
        BencodeStreamReader bsr = new BencodeStreamReader(is);

        BencodeFormatException exception =
                assertThrows(BencodeFormatException.class, () ->
                                new BencodedByteSequence(bsr),
                        "BencodedByteSequence constructor behaves incorrectly on stream data with missing " +
                                "sequence length part");

        assertEquals("BencodedByteSequence length part is not present in the stream",
                exception.getMessage(),
                "Unexpected message in correctly thrown exception");


    }

    @Test
    void deserializeObjectFromEmptyStream() {
        final String streamContents = "";
        InputStream is = new ByteArrayInputStream(streamContents.getBytes(StandardCharsets.UTF_8));
        BencodeStreamReader bsr = new BencodeStreamReader(is);

        BencodeFormatException exception =
                assertThrows(BencodeFormatException.class, () ->
                                new BencodedByteSequence(bsr),
                        "BencodedByteSequence constructor behaves incorrectly " +
                                "on stream data with larger than correct sequence length part");


        assertEquals("Unexpected end of the stream",
                exception.getMessage(),
                "Unexpected message in correctly thrown exception");
    }


    /**
     * Deserializes particular sequence to test the class correctness.
     *
     * @param referenceStringValue string value to serialize and deserialize as byte sequence.
     * @throws IOException            if there's an i/o problem occurred.
     * @throws BencodeFormatException if there's a bencode format problem occurred.
     */
    private static void deserializeSequence(String referenceStringValue) throws
            IOException, BencodeFormatException {

        byte[] referenceByteSequence = referenceStringValue.getBytes(StandardCharsets.UTF_8);

        String streamContents = String.valueOf(referenceByteSequence.length) + ':' + referenceStringValue;

        InputStream is = new ByteArrayInputStream(streamContents.getBytes(StandardCharsets.UTF_8));
        BencodeStreamReader bsr = new BencodeStreamReader(is);

        BencodedByteSequence bbsFromStream = new BencodedByteSequence(bsr);

        BencodedByteSequence bbs1 = new BencodedByteSequence(referenceStringValue);
        BencodedByteSequence bbs2 = new BencodedByteSequence(referenceByteSequence);

        assertAll("Comparing the deserialized objects to the reference ones",
                () -> assertEquals(bbs1, bbsFromStream),
                () -> assertEquals(bbs2, bbsFromStream));

    }
}