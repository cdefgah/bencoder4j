package com.github.cdefgah.bencoder4j.model;

import com.github.cdefgah.bencoder4j.BencodeFormatException;
import com.github.cdefgah.bencoder4j.io.BencodeStreamReader;
import org.junit.jupiter.api.Test;
import com.github.cdefgah.bencoder4j.CircularReferenceException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class BencodedDictionaryTest {

    @Test
    void emptyDictionaryWorksProperly() {
        BencodedDictionary dictionary = new BencodedDictionary();
        Iterator keysIterator = dictionary.getKeysIterator();

        assertAll("Validating empty iterator state and values list size",
                () -> assertFalse(keysIterator.hasNext()),
                () -> assertEquals(0, dictionary.getCompositeValues().size())
        );
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void nullKeysAreNotAllowedInDictionary() {
        BencodedDictionary dictionary = new BencodedDictionary();
        String diagnosticsMessage = "BencodedDictionary should throw illegal argument exception on attempt to " +
                "add an entry with null key. But it does not.";

        String expectedMessage = "'keyObject' value for BencodedDictionary cannot be null!";
        String assertFailMessage = "Unexpected message in correctly thrown exception";

        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () -> {
            // consciously adding null key to raise the exception
            BencodedByteSequence key = null;
            dictionary.put(key, new BencodedInteger(1));

        }, diagnosticsMessage);

        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> {
            // consciously adding null key to raise the exception
            String key = null;
            dictionary.put(key, new BencodedInteger(1));

        }, diagnosticsMessage);

        assertAll("Validating exception messages",
                () -> assertEquals(expectedMessage, exception1.getMessage(), assertFailMessage),
                () -> assertEquals(expectedMessage, exception2.getMessage(), assertFailMessage)
        );
    }

    @Test
    void nullValuesAreNotAllowedInDictionary() {
        BencodedDictionary dictionary = new BencodedDictionary();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            // consciously adding null value to raise the exception
            BencodedByteSequence key = new BencodedByteSequence("abc");
            dictionary.put(key, null);

        }, "BencodedDictionary should throw illegal argument exception on attempt to " +
                "add an entry with null value. But it does not.");

        assertEquals("'bencodedObject' value for BencodedDictionary cannot be null!",
                exception.getMessage(),
                "Unexpected message in correctly thrown exception");
    }


    @Test
    void ensureKeysHaveBeenSorted() {

        BencodedDictionary dictionary = new BencodedDictionary();
        BencodedInteger value1 = new BencodedInteger(1);
        BencodedInteger value2 = new BencodedInteger(2);
        BencodedInteger value3 = new BencodedInteger(3);

        BencodedByteSequence key1 = new BencodedByteSequence("xxx");
        BencodedByteSequence key2 = new BencodedByteSequence("aaa");
        BencodedByteSequence key3 = new BencodedByteSequence("sss");

        dictionary.put(key1, value1);
        dictionary.put(key2, value2);
        dictionary.put(key3, value3);

        Iterator<BencodedByteSequence> keys = dictionary.getKeysIterator();

        assertAll("Validating correct keys order",
                () -> assertEquals(key2, keys.next()),
                () -> assertEquals(key3, keys.next()),
                () -> assertEquals(key1, keys.next())
        );
    }


    @Test
    void ensurePutWorksCorrectly() {

        BencodedDictionary dictionary = new BencodedDictionary();
        BencodedInteger value1 = new BencodedInteger(10);
        BencodedInteger value2 = new BencodedInteger(20);
        BencodedInteger value3 = new BencodedInteger(30);

        BencodedByteSequence key1 = new BencodedByteSequence("xxx");
        BencodedByteSequence key2 = new BencodedByteSequence("aaa");
        BencodedByteSequence key3 = new BencodedByteSequence("sss");

        dictionary.put(key1, value1);
        dictionary.put(key2, value2);
        dictionary.put(key3, value3);

        Iterator<BencodedByteSequence> keys = dictionary.getKeysIterator();
        assertAll("Validating correct entries order",
                () -> assertEquals(value2, dictionary.get(keys.next())),
                () -> assertEquals(value3, dictionary.get(keys.next())),
                () -> assertEquals(value1, dictionary.get(keys.next()))
        );
    }


    @Test
    void toStringReturnsCorrectValue() {

        BencodedDictionary dictionary = new BencodedDictionary();
        BencodedInteger value1 = new BencodedInteger(1);
        BencodedInteger value2 = new BencodedInteger(2);
        BencodedInteger value3 = new BencodedInteger(3);

        BencodedByteSequence key1 = new BencodedByteSequence("key1");
        BencodedByteSequence key2 = new BencodedByteSequence("key2");
        BencodedByteSequence key3 = new BencodedByteSequence("key3");

        dictionary.put(key1, value1);
        dictionary.put(key2, value2);
        dictionary.put(key3, value3);

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        Iterator<BencodedByteSequence> keysIterator = dictionary.getKeysIterator();
        while (keysIterator.hasNext()) {
            BencodedByteSequence key = keysIterator.next();
            BencodedObject value = dictionary.get(key);
            sb.append("([").append(key.toString()).append("]:[").append(value.toString()).append("])\n");
        }

        sb.append("}");

        String expectedStringValue = sb.toString();
        assertEquals(expectedStringValue, dictionary.toString());
    }

    @Test
    void testCompositeState() {
        BencodedDictionary bencodedDictionary = new BencodedDictionary();

        BencodedByteSequence key1 = new BencodedByteSequence("key1");
        BencodedByteSequence key2 = new BencodedByteSequence("key2");

        BencodedInteger value1 = new BencodedInteger(Long.MAX_VALUE);
        BencodedByteSequence value2 = new BencodedByteSequence("value2");

        bencodedDictionary.put(key1, value1);
        bencodedDictionary.put(key2, value2);

        assertAll("Validating composite state",
                () -> assertTrue(bencodedDictionary.isCompositeObject()),
                () -> assertEquals(2, bencodedDictionary.getCompositeValues().size())
        );
    }

    @Test
    void serializeEmptyDictionary() throws IOException, CircularReferenceException {
        String expectedSerializedForm = String.valueOf(BencodedDictionary.SERIALIZED_PREFIX) +
                String.valueOf(BencodedDictionary.SERIALIZED_SUFFIX);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        BencodedDictionary dictionary = new BencodedDictionary();
        dictionary.writeObject(baos);

        String actualSerializedForm = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        assertEquals(expectedSerializedForm, actualSerializedForm);
    }

    @Test
    void serializePopulatedDictionary() throws IOException, CircularReferenceException {
        long value1 = 123;
        String value2 = "abc";
        String value3 = "xyz";

        String key1 = "22222";
        String key2 = "11111";
        BencodedByteSequence key3 = new BencodedByteSequence("33333");

        BencodedDictionary dictionary = new BencodedDictionary();
        dictionary.put(key1, new BencodedInteger(value1));
        dictionary.put(key2, new BencodedByteSequence(value2));
        dictionary.put(key3, new BencodedByteSequence(value3));

        String expectedSerializedForm = "d5:111113:abc5:22222i123e5:333333:xyze";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        dictionary.writeObject(baos);

        String actualSerializedForm = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        assertEquals(expectedSerializedForm, actualSerializedForm);
    }

    @Test
    void deserializeEmptyDictionary() throws IOException, BencodeFormatException {
        String streamContents = "de";
        InputStream is = new ByteArrayInputStream(streamContents.getBytes(StandardCharsets.UTF_8));
        BencodeStreamReader bsr = new BencodeStreamReader(is);

        BencodedDictionary dictionary = new BencodedDictionary(bsr);

        assertAll("Testing empty state for the dictionary",
                () -> assertEquals(0, dictionary.getCompositeValues().size()),
                () -> assertFalse(dictionary.getKeysIterator().hasNext())
        );
    }


    @Test
    void deserializePopulatedDictionary() throws IOException, BencodeFormatException {
        String streamContents = "d5:111113:abc5:22222i123e5:333333:xyze";
        InputStream is = new ByteArrayInputStream(streamContents.getBytes(StandardCharsets.UTF_8));
        BencodeStreamReader bsr = new BencodeStreamReader(is);

        BencodedDictionary dictionary = new BencodedDictionary(bsr);

        BencodedByteSequence key1 = new BencodedByteSequence("11111");
        BencodedByteSequence value1 = new BencodedByteSequence("abc");

        String key2 = "22222";
        BencodedInteger value2 = new BencodedInteger(123);

        BencodedByteSequence key3 = new BencodedByteSequence("33333");
        BencodedByteSequence value3 = new BencodedByteSequence("xyz");

        Iterator<BencodedByteSequence> keysIterator = dictionary.getKeysIterator();
        assertAll("Testing desrialized dictionary contents",
                () -> assertTrue(keysIterator.hasNext()),
                () -> assertEquals(key1, keysIterator.next()),
                () -> assertEquals(value1, dictionary.get(key1)),
                () -> assertTrue(keysIterator.hasNext()),
                () -> assertEquals(key2, keysIterator.next().toUTF8String()),
                () -> assertEquals(value2, dictionary.get(key2)),
                () -> assertTrue(keysIterator.hasNext()),
                () -> assertEquals(key3, keysIterator.next()),
                () -> assertEquals(value3, dictionary.get(key3)),
                () -> assertFalse(keysIterator.hasNext())
        );
    }

    @Test
    void testDictionaryKeysIteratorSequenceWithBencodedSequenceKeys() {
        BencodedByteSequence key1 = new BencodedByteSequence("333");
        BencodedByteSequence key2 = new BencodedByteSequence("111");
        BencodedByteSequence key3 = new BencodedByteSequence("222");

        BencodedInteger value1 = new BencodedInteger(1);
        BencodedInteger value2 = new BencodedInteger(2);
        BencodedInteger value3 = new BencodedInteger(3);

        BencodedDictionary dictionary = new BencodedDictionary();
        dictionary.put(key1, value1);
        dictionary.put(key2, value2);
        dictionary.put(key3, value3);

        Iterator<BencodedByteSequence> keysIterator = dictionary.getKeysIterator();

        assertAll("Testing dictionary keys iterator",
                () -> assertTrue(keysIterator.hasNext()),
                () -> assertEquals(key2, keysIterator.next()),
                () -> assertTrue(keysIterator.hasNext()),
                () -> assertEquals(key3, keysIterator.next()),
                () -> assertTrue(keysIterator.hasNext()),
                () -> assertEquals(key1, keysIterator.next()),
                () -> assertFalse(keysIterator.hasNext())
        );
    }


    @Test
    void testDictionaryKeysIteratorSequenceWithStringKeys() {
        String key1 = "333";
        String key2 = "111";
        String key3 = "222";

        BencodedInteger value1 = new BencodedInteger(1);
        BencodedInteger value2 = new BencodedInteger(2);
        BencodedInteger value3 = new BencodedInteger(3);

        BencodedDictionary dictionary = new BencodedDictionary();
        dictionary.put(key1, value1);
        dictionary.put(key2, value2);
        dictionary.put(key3, value3);

        Iterator<BencodedByteSequence> keysIterator = dictionary.getKeysIterator();

        assertAll("Testing dictionary keys iterator",
                () -> assertTrue(keysIterator.hasNext()),
                () -> assertEquals(key2, keysIterator.next().toUTF8String()),
                () -> assertTrue(keysIterator.hasNext()),
                () -> assertEquals(key3, keysIterator.next().toUTF8String()),
                () -> assertTrue(keysIterator.hasNext()),
                () -> assertEquals(key1, keysIterator.next().toUTF8String()),
                () -> assertFalse(keysIterator.hasNext())
        );
    }


    @Test
    void equalsIsSymmetric() {
        // for a and b, if a.equals(b), then b.equals(a);
        BencodedDictionary dictionary1 = new BencodedDictionary();
        BencodedByteSequence key1 = new BencodedByteSequence("aaa");
        BencodedByteSequence key2 = new BencodedByteSequence("bbb");
        BencodedInteger value1 = new BencodedInteger(Long.MAX_VALUE);
        BencodedInteger value2 = new BencodedInteger(Long.MAX_VALUE);

        dictionary1.put(key1, value1);
        dictionary1.put(key2, value2);

        BencodedDictionary dictionary2 = new BencodedDictionary();
        BencodedByteSequence key3 = new BencodedByteSequence("aaa");
        BencodedByteSequence key4 = new BencodedByteSequence("bbb");
        BencodedInteger value3 = new BencodedInteger(Long.MAX_VALUE);
        BencodedInteger value4 = new BencodedInteger(Long.MAX_VALUE);

        dictionary2.put(key3, value3);
        dictionary2.put(key4, value4);

        assertAll("Equals is symmetric",
                () -> assertEquals(dictionary1, dictionary2),
                () -> assertEquals(dictionary2, dictionary1)
        );
    }

    @SuppressWarnings("EqualsWithItself")
    @Test
    void equalsIsReflexive() {
        // when a!=null, a.equals(a);
        BencodedDictionary dictionary1 = new BencodedDictionary();
        BencodedByteSequence key1 = new BencodedByteSequence("aaa");
        BencodedInteger value1 = new BencodedInteger(Long.MAX_VALUE);
        dictionary1.put(key1, value1);

        assertTrue(dictionary1.equals(dictionary1));
    }


    @Test
    void equalsIsTransitive() {
        // if a.equals(b), and b.equals(c), then a.equals(c);

        BencodedDictionary dictionary1 = new BencodedDictionary();
        BencodedByteSequence key1 = new BencodedByteSequence("aaa");
        BencodedByteSequence key2 = new BencodedByteSequence("bbb");
        BencodedInteger value1 = new BencodedInteger(Long.MAX_VALUE);
        BencodedInteger value2 = new BencodedInteger(Long.MAX_VALUE);

        dictionary1.put(key1, value1);
        dictionary1.put(key2, value2);

        BencodedDictionary dictionary2 = new BencodedDictionary();
        BencodedByteSequence key3 = new BencodedByteSequence("aaa");
        BencodedByteSequence key4 = new BencodedByteSequence("bbb");
        BencodedInteger value3 = new BencodedInteger(Long.MAX_VALUE);
        BencodedInteger value4 = new BencodedInteger(Long.MAX_VALUE);

        dictionary2.put(key3, value3);
        dictionary2.put(key4, value4);

        BencodedDictionary dictionary3 = new BencodedDictionary();
        BencodedByteSequence key5 = new BencodedByteSequence("aaa");
        BencodedByteSequence key6 = new BencodedByteSequence("bbb");
        BencodedInteger value5 = new BencodedInteger(Long.MAX_VALUE);
        BencodedInteger value6 = new BencodedInteger(Long.MAX_VALUE);

        dictionary3.put(key5, value5);
        dictionary3.put(key6, value6);

        assertAll("Equals is transitive",
                () -> assertEquals(dictionary1, dictionary2),
                () -> assertEquals(dictionary2, dictionary3),
                () -> assertEquals(dictionary1, dictionary3)
        );
    }


    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    @Test
    void equalsReturnsFalseForDifferentObjectClass() {
        // if we're comparing two objects of different classes
        // equals should return false

        String someStringObject = "1234567890";
        BencodedDictionary dictionary1 = new BencodedDictionary();
        BencodedByteSequence key1 = new BencodedByteSequence("aaa");
        BencodedByteSequence key2 = new BencodedByteSequence("bbb");
        BencodedInteger value1 = new BencodedInteger(Long.MAX_VALUE);
        BencodedInteger value2 = new BencodedInteger(Long.MAX_VALUE);

        dictionary1.put(key1, value1);
        dictionary1.put(key2, value2);

        assertFalse(dictionary1.equals(someStringObject));
    }

    @Test
    void equalsReturnsFalseForObjectsWithDifferentValues() {
        // if we're comparing two objects of the same class,
        // but with different values,
        // equals should return false

        BencodedDictionary dictionary1 = new BencodedDictionary();
        BencodedByteSequence key1 = new BencodedByteSequence("aaa");
        BencodedByteSequence key2 = new BencodedByteSequence("bbb");
        BencodedInteger value1 = new BencodedInteger(Long.MAX_VALUE);
        BencodedInteger value2 = new BencodedInteger(Long.MAX_VALUE);

        dictionary1.put(key1, value1);
        dictionary1.put(key2, value2);

        BencodedDictionary dictionary2 = new BencodedDictionary();
        BencodedByteSequence key3 = new BencodedByteSequence("aaa");
        BencodedByteSequence key4 = new BencodedByteSequence("ccc");
        BencodedInteger value3 = new BencodedInteger(Long.MAX_VALUE);
        BencodedInteger value4 = new BencodedInteger(Long.MAX_VALUE);

        dictionary2.put(key3, value3);
        dictionary2.put(key4, value4);

        assertFalse(dictionary1.equals(dictionary2));
    }


    @Test
    void hashCodesAreEqualForEqualObjects() {
        // if a.equals(b), then a.hashCode() == b.hashCode();

        BencodedDictionary dictionary1 = new BencodedDictionary();
        BencodedByteSequence key1 = new BencodedByteSequence("aaa");
        BencodedByteSequence key2 = new BencodedByteSequence("bbb");
        BencodedInteger value1 = new BencodedInteger(Long.MAX_VALUE);
        BencodedInteger value2 = new BencodedInteger(Long.MAX_VALUE);

        dictionary1.put(key1, value1);
        dictionary1.put(key2, value2);

        BencodedDictionary dictionary2 = new BencodedDictionary();
        BencodedByteSequence key3 = new BencodedByteSequence("aaa");
        BencodedByteSequence key4 = new BencodedByteSequence("bbb");
        BencodedInteger value3 = new BencodedInteger(Long.MAX_VALUE);
        BencodedInteger value4 = new BencodedInteger(Long.MAX_VALUE);

        dictionary2.put(key3, value3);
        dictionary2.put(key4, value4);

        assertAll("Hash codes are equal for equal objects",
                () -> assertEquals(dictionary1, dictionary2),
                () -> assertEquals(dictionary1.hashCode(), dictionary2.hashCode())
        );
    }

    @SuppressWarnings("ObjectEqualsNull")
    @Test
    void equalsToNullReturnsFalse() {
        // a.equals(null) should return false

        BencodedDictionary dictionary1 = new BencodedDictionary();
        BencodedByteSequence key1 = new BencodedByteSequence("aaa");
        BencodedByteSequence key2 = new BencodedByteSequence("bbb");
        BencodedInteger value1 = new BencodedInteger(Long.MAX_VALUE);
        BencodedInteger value2 = new BencodedInteger(Long.MAX_VALUE);

        dictionary1.put(key1, value1);
        dictionary1.put(key2, value2);

        assertFalse(dictionary1.equals(null));
    }

    @Test
    void interceptSimplestCircularReference() {
        BencodedDictionary dictionary = new BencodedDictionary();
        BencodedByteSequence key1 = new BencodedByteSequence("aaa");
        BencodedInteger value1 = new BencodedInteger(Long.MAX_VALUE);

        BencodedByteSequence key2 = new BencodedByteSequence("bbb");

        dictionary.put(key1, value1);
        dictionary.put(key2, dictionary);

        tryAndThrowCircularReferenceException(dictionary);
    }


    @Test
    void interceptTheFirstLevelCircularReference() {
        BencodedDictionary dictionary = new BencodedDictionary();
        BencodedByteSequence key1 = new BencodedByteSequence("aaa");
        BencodedInteger value1 = new BencodedInteger(Long.MAX_VALUE);

        BencodedByteSequence key2 = new BencodedByteSequence("bbb");

        BencodedList value2 = new BencodedList();
        value2.add(dictionary);

        dictionary.put(key1, value1);
        dictionary.put(key2, value2);

        tryAndThrowCircularReferenceException(dictionary);
    }

    @Test
    void interceptMoreComplexCircularReference() {
        BencodedDictionary dictionary1 = new BencodedDictionary();
        BencodedByteSequence key1 = new BencodedByteSequence("aaa");
        BencodedInteger value1 = new BencodedInteger(Long.MAX_VALUE);
        dictionary1.put(key1, value1);

        BencodedDictionary dictionary2 = new BencodedDictionary();
        BencodedByteSequence key2 = new BencodedByteSequence("bbb");
        BencodedInteger value2 = new BencodedInteger(Long.MIN_VALUE);
        dictionary2.put(key2, value2);

        BencodedByteSequence key3 = new BencodedByteSequence("ccc");
        dictionary1.put(key3, dictionary2);

        BencodedList list = new BencodedList();
        list.add(dictionary1);

        BencodedByteSequence key4 = new BencodedByteSequence("ddd");
        dictionary2.put(key4, list);

        tryAndThrowCircularReferenceException(dictionary1);
    }


    @Test
    void loadingFromStreamValueWithMissingDictionaryKey() {

        String streamContents = "di3ee";

        InputStream is = new ByteArrayInputStream(streamContents.getBytes(StandardCharsets.UTF_8));
        BencodeStreamReader bsr = new BencodeStreamReader(is);

        BencodeFormatException exception =
                assertThrows(BencodeFormatException.class, () ->
                                new BencodedDictionary(bsr),
                        "BencodedDictionary constructor behaves incorrectly on the stream with missing key");

        assertEquals("Incorrect object used as dictionary key. Expected: '" +
                        BencodedByteSequence.class.getCanonicalName() + "' but get: '" +
                        BencodedInteger.class.getCanonicalName() + "'",
                exception.getMessage(),
                "Unexpected message in correctly thrown exception");
    }

    @Test
    void loadingFromStreamValueWithMissingDictionarySuffix() {

        String streamContents = "d3:aaai3e";

        InputStream is = new ByteArrayInputStream(streamContents.getBytes(StandardCharsets.UTF_8));
        BencodeStreamReader bsr = new BencodeStreamReader(is);

        BencodeFormatException exception =
                assertThrows(BencodeFormatException.class, () ->
                                new BencodedDictionary(bsr),
                        "BencodedDictionary constructor behaves incorrectly on the stream with missing suffix");

        assertEquals("Unexpected end of the stream",
                exception.getMessage(),
                "Unexpected message in correctly thrown exception");
    }

    @Test
    void loadingFromStreamValueWithIncorrectDictionaryPrefix() {

        String streamContents = "x2:aai3ee";

        InputStream is = new ByteArrayInputStream(streamContents.getBytes(StandardCharsets.UTF_8));
        BencodeStreamReader bsr = new BencodeStreamReader(is);

        BencodeFormatException exception =
                assertThrows(BencodeFormatException.class, () ->
                                new BencodedDictionary(bsr),
                        "BencodedDictionary constructor behaves " +
                                "incorrectly on the stream with incorrect prefix");

        assertEquals("Incorrect stream position, expected prefix character: d",
                exception.getMessage(),
                "Unexpected message in correctly thrown exception");
    }

    @Test
    void loadingFromStreamValueWithPresentKeyButMissingValue() {

        String streamContents = "d2:aae";

        InputStream is = new ByteArrayInputStream(streamContents.getBytes(StandardCharsets.UTF_8));
        BencodeStreamReader bsr = new BencodeStreamReader(is);

        BencodeFormatException exception =
                assertThrows(BencodeFormatException.class, () ->
                                new BencodedDictionary(bsr),
                        "BencodedDictionary constructor behaves incorrectly on the stream " +
                                "where dictionary key is present, but value is not");

        assertEquals("Unexpected end of the stream for dictionary. " +
                        "'Key' object is present, but 'value' object is not.",
                exception.getMessage(),
                "Unexpected message in correctly thrown exception");
    }

    /**
     * Tries to write a dictionary to the stream to emulate circular reference exception situation.
     *
     * @param dictionary dictionary to be written to output stream.
     */
    private void tryAndThrowCircularReferenceException(BencodedDictionary dictionary) {
        String expectedExceptionMessage = "Upon writing to stream, circular reference found in " +
                BencodedDictionary.class.getCanonicalName();

        String unexpectedExceptionMessageDiagnostics = "Unexpected message in correctly thrown exception";

        String noExceptionMessage = "BencodedList with circular reference inside should throw " +
                "circular reference exception upon writing to the stream. But it does not.";

        CircularReferenceException exception1 = assertThrows(CircularReferenceException.class, () -> {

            OutputStream os = new ByteArrayOutputStream();
            dictionary.writeObject(os);

        }, noExceptionMessage);

        assertEquals(expectedExceptionMessage,
                exception1.getMessage(),
                unexpectedExceptionMessageDiagnostics);
    }
}