package rafael.osipov.bencoder4j.model;

import org.junit.jupiter.api.Test;
import rafael.osipov.bencoder4j.BencodeFormatException;
import rafael.osipov.bencoder4j.io.BencodeStreamReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class BencodedIntegerTest {

    @Test
    void signed64BitIntValuesAreSupported() {
        BencodedInteger bintWithMaxLongValue = new BencodedInteger(Long.MAX_VALUE);
        BencodedInteger bintWithMinLongValue = new BencodedInteger(Long.MIN_VALUE);

        assertAll("Validating BencodedInteger to support signed long numbers",
                () -> assertEquals(Long.MAX_VALUE, bintWithMaxLongValue.getValue(),
                        "Storing and retrieving Long.MAX_VALUE does not work, " +
                                "hence the BencodedInteger class does not support signed long values " +
                                "as it should according to the specification"),
                () -> assertEquals(Long.MIN_VALUE, bintWithMinLongValue.getValue(),
                        "Storing and retrieving Long.MIN_VALUE does not work, " +
                                "hence the BencodedInteger class does not support signed long values " +
                                "as it should according to the specification")
        );
    }

    @Test
    void objectsConformToTheMainComparableRules() {
        BencodedInteger bintWithMaxValue1 = new BencodedInteger(Long.MAX_VALUE);
        BencodedInteger bintWithMaxValue2 = new BencodedInteger(Long.MAX_VALUE);

        BencodedInteger bintWithMinValue = new BencodedInteger(Long.MIN_VALUE);

        assertAll("Ensuring the conformance to the compareTo contract",
                () -> assertEquals(0, bintWithMaxValue1.compareTo(bintWithMaxValue2),
                        "Equal comparable objects should return 0 on compareTo() call"),
                () -> assertTrue(bintWithMaxValue1.compareTo(bintWithMinValue) > 0,
                        "Calling compareTo() of a bigger comparable object " +
                                "with a smaller comparable object as an argument should return a positive int value"),
                () -> assertTrue(bintWithMinValue.compareTo(bintWithMaxValue1) < 0,
                        "Calling compareTo() of a smaller comparable object " +
                                "with a bigger comparable object as an argument should return a negative int value")
        );
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void compareWithNull() {
       assertThrows(NullPointerException.class, () -> {
         BencodedInteger bi = new BencodedInteger(Long.MIN_VALUE);
         bi.compareTo(null);
       }, "The class does not conform to the Oracle recommendation for compareTo() " +
                                "when there is a null argument passed," +
                                "this method should throw NullPointerException, but it does not");
    }

    @Test
    void compareInBothDirections() {
        BencodedInteger bint1 = new BencodedInteger(Long.MAX_VALUE);
        BencodedInteger bint2 = new BencodedInteger(Long.MIN_VALUE);

        // checking for: sgn(x.compareTo(y)) == -sgn(y.compareTo(x))
        assertEquals(Integer.signum(bint1.compareTo(bint2)),
                   -1 * Integer.signum(bint2.compareTo(bint1)),
                "compareTo() method of comparable objects should work in both directions");
    }

    @Test
    void compareToIsTransitive() {
        BencodedInteger x = new BencodedInteger(Long.MAX_VALUE);
        BencodedInteger y = new BencodedInteger(0L);
        BencodedInteger z = new BencodedInteger(Long.MIN_VALUE);

        assertAll("Ensuring that: (x.compareTo(y)>0 && y.compareTo(z)>0) implies x.compareTo(z)>0",
                () -> assertTrue(x.compareTo(y) > 0),
                () -> assertTrue(y.compareTo(z) > 0),
                () -> assertTrue(x.compareTo(z) > 0)
        );
    }

    @Test
    void compareToContractAdditionalRuleIsFulfilled() {
        BencodedInteger x = new BencodedInteger(Long.MIN_VALUE);
        BencodedInteger y = new BencodedInteger(Long.MIN_VALUE);
        BencodedInteger z = new BencodedInteger(Long.MAX_VALUE);

        assertAll("x.compareTo(y)==0 implies that sgn(x.compareTo(z)) == sgn(y.compareTo(z)), for all z," +
                        "(x.compareTo(y)==0) == (x.equals(y))",

                () -> assertEquals( 0, x.compareTo(y)),
                () -> assertEquals(Integer.signum(x.compareTo(z)), Integer.signum(y.compareTo(z))),
                () -> assertEquals(x, y)
        );
    }

    @Test
    void equalsIsSymmetric() {
        // for a and b, if a.equals(b), then b.equals(a);
        BencodedInteger bint1 = new BencodedInteger(Long.MAX_VALUE);
        BencodedInteger bint2 = new BencodedInteger(Long.MAX_VALUE);

        assertAll("Equals is symmetric",
                () -> assertEquals(bint1, bint2),
                () -> assertEquals(bint2, bint1)
        );
    }

    @SuppressWarnings("EqualsWithItself")
    @Test
    void equalsIsReflexive() {
        // when a!=null, a.equals(a);
        BencodedInteger bint1 = new BencodedInteger(Long.MAX_VALUE);

        assertTrue(bint1.equals(bint1));
    }

    @Test
    void equalsIsTransitive() {
        // if a.equals(b), and b.equals(c), then a.equals(c);

        BencodedInteger bint1 = new BencodedInteger(Long.MAX_VALUE);
        BencodedInteger bint2 = new BencodedInteger(Long.MAX_VALUE);
        BencodedInteger bint3 = new BencodedInteger(Long.MAX_VALUE);

        assertAll("Equals is transitive",
                () -> assertEquals(bint1, bint2),
                () -> assertEquals(bint2, bint3),
                () -> assertEquals(bint1, bint3)
        );
    }

    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    @Test
    void equalsReturnsFalseForDifferentObjectClass() {
        // if we're comparing two objects of different classes
        // equals should return false

        String someStringObject = "1234567890";
        BencodedInteger bint1 = new BencodedInteger(1234567890L);

        assertFalse(bint1.equals(someStringObject));
    }

    @Test
    void equalsReturnsFalseForObjectsWithDifferentValues() {
        // if we're comparing two objects of the same class,
        // but with different values,
        // equals should return false

        BencodedInteger bint1 = new BencodedInteger(Long.MAX_VALUE);
        BencodedInteger bint2 = new BencodedInteger(Long.MIN_VALUE);

        assertFalse(bint1.equals(bint2));
    }

    @Test
    void hashCodesAreEqualForEqualObjects() {
        // if a.equals(b), then a.hashCode() == b.hashCode();

        BencodedInteger bint1 = new BencodedInteger(Long.MAX_VALUE);
        BencodedInteger bint2 = new BencodedInteger(Long.MAX_VALUE);

        assertAll("Hash codes are equal for equal objects",
                () -> assertEquals(bint1, bint2),
                () -> assertEquals(bint1.hashCode(), bint2.hashCode())
        );
    }


    @SuppressWarnings("ObjectEqualsNull")
    @Test
    void equalsToNullReturnsFalse() {
        // a.equals(null) should return false
        BencodedInteger bint1 = new BencodedInteger(Long.MAX_VALUE);

        assertFalse(bint1.equals(null));
    }

    @Test
    void toStringReturnsCorrectValue() {
        String expectedStringValue = String.valueOf(Long.MIN_VALUE);
        BencodedInteger bint1 = new BencodedInteger(Long.MIN_VALUE);

        assertEquals(expectedStringValue, bint1.toString());
    }

    @Test
    void getValueWorksCorrectly() {
        BencodedInteger bint1 = new BencodedInteger(Long.MAX_VALUE);

        assertEquals(Long.MAX_VALUE, bint1.getValue());
    }

    @Test
    void instantiationFromStreamWorksCorrectly() throws IOException, BencodeFormatException {
        long referenceValue = Long.MIN_VALUE;
        String streamContents = "i" + String.valueOf(referenceValue) + "e";
        InputStream is = new ByteArrayInputStream(streamContents.getBytes(StandardCharsets.UTF_8));
        BencodeStreamReader bsr = new BencodeStreamReader(is);

        BencodedInteger bint = new BencodedInteger(bsr);
        assertEquals(referenceValue, bint.getValue());
    }

    @Test
    void loadingFromStreamValueWithLeadingZero() {

        BencodeFormatException exception = assertThrows(BencodeFormatException.class, () -> {
            // consciously incorrect form of serialized bencoded integer value
            String streamContents = "i03e";

            InputStream is =
                    new ByteArrayInputStream(streamContents.getBytes(StandardCharsets.UTF_8));
            BencodeStreamReader bsr = new BencodeStreamReader(is);

            new BencodedInteger(bsr);
        }, "Stream reader behaves incorrectly on serialized numeric " +
                                                                        "value with leading zero");

        assertEquals("Incorrect character sequence for the value",
                exception.getMessage(),
                "Unexpected message in correctly thrown exception");
    }


    @Test
    void loadingFromStreamValueWithNegativeZero() {

        BencodeFormatException exception = assertThrows(BencodeFormatException.class, () -> {
            // consciously incorrect form of serialized bencoded integer value
            String streamContents = "i-0e";

            InputStream is =
                    new ByteArrayInputStream(streamContents.getBytes(StandardCharsets.UTF_8));
            BencodeStreamReader bsr = new BencodeStreamReader(is);

            new BencodedInteger(bsr);
        }, "Stream reader behaves incorrectly on " +
                                                    "serialized numeric value with negative zero");

        assertEquals("Incorrect character sequence for the value",
                exception.getMessage(),
                "Unexpected message in correctly thrown exception");
    }

    @Test
    void loadingFromStreamValueWithIncorrectPrefix() {

        BencodeFormatException exception =
            assertThrows(BencodeFormatException.class, () -> {
                // consciously incorrect form of serialized bencoded integer value
                String streamContents = "x3e";

                InputStream is =
                        new ByteArrayInputStream(streamContents.getBytes(StandardCharsets.UTF_8));
                BencodeStreamReader bsr = new BencodeStreamReader(is);

                new BencodedInteger(bsr);
            }, "Stream reader behaves incorrectly on serialized numeric value " +
                                                                           "with incorrect prefix");

        assertEquals("Incorrect stream position, expected prefix character: i",
                                            exception.getMessage(),
                "Unexpected message in correctly thrown exception");
    }

    @Test
    void loadingFromStreamValueWithIncorrectSuffix() {

        BencodeFormatException exception =
                assertThrows(BencodeFormatException.class, () -> {
                    // consciously incorrect form of serialized bencoded integer value
                    String streamContents = "i3f";

                    InputStream is =
                          new ByteArrayInputStream(streamContents.getBytes(StandardCharsets.UTF_8));
                    BencodeStreamReader bsr = new BencodeStreamReader(is);

                    new BencodedInteger(bsr);
                }, "Stream reader behaves incorrectly on serialized numeric value " +
                                                                           "with incorrect suffix");

        assertEquals("Stop symbol: 'e' was not reached",
                    exception.getMessage(),
                    "Unexpected message in correctly thrown exception");
    }


    @Test
    void loadingFromStreamValueWithMissingSuffix() {

        BencodeFormatException exception =
                assertThrows(BencodeFormatException.class, () -> {
                    // consciously incorrect form of serialized bencoded integer value
                    String streamContents = "i3";

                    InputStream is =
                            new ByteArrayInputStream(streamContents.getBytes(StandardCharsets.UTF_8));
                    BencodeStreamReader bsr = new BencodeStreamReader(is);

                    new BencodedInteger(bsr);
                }, "Stream reader behaves incorrectly on serialized numeric value " +
                        "without suffix");

        assertEquals("Stop symbol: 'e' was not reached",
                exception.getMessage(),
                "Unexpected message in correctly thrown exception");
    }

    @Test
    void loadingFromStreamZeroValue() throws IOException, BencodeFormatException {
        String streamContents = "i0e";

        InputStream is = new ByteArrayInputStream(streamContents.getBytes(StandardCharsets.UTF_8));
        BencodeStreamReader bsr = new BencodeStreamReader(is);

        BencodedInteger bint = new BencodedInteger(bsr);
        assertEquals(0L, bint.getValue());
    }

    @Test
    void writeObjectWorksCorrectly() throws IOException {
        long referenceNumericValue = Long.MIN_VALUE;
        String expectedSerializedForm = BencodedInteger.SERIALIZED_PREFIX +
                                                    String.valueOf(referenceNumericValue) +
                                                                BencodedInteger.SERIALIZED_SUFFIX;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        BencodedInteger bint = new BencodedInteger(referenceNumericValue);
        bint.writeObject(baos);

        String serializedForm = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        assertEquals(expectedSerializedForm, serializedForm);
    }

    @Test
    void testNonCompositeState() {
        BencodedInteger bint = new BencodedInteger(Long.MAX_VALUE);

        assertAll("Ensuring the correct composite object state",
                () -> assertFalse(bint.isCompositeObject()),
                () -> assertEquals(0, bint.getCompositeValues().size())
        );
    }
}