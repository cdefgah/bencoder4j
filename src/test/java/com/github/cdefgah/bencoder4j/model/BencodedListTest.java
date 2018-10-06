package com.github.cdefgah.bencoder4j.model;

import com.github.cdefgah.bencoder4j.BencodeFormatException;
import com.github.cdefgah.bencoder4j.CircularReferenceException;
import com.github.cdefgah.bencoder4j.io.BencodeStreamReader;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class BencodedListTest {

    @Test
    void emptyListWorksProperly() {
        BencodedList bencodedList = new BencodedList();
        assertEquals(0, bencodedList.size());
        assertFalse(bencodedList.iterator().hasNext());

        Collection<BencodedObject> collection = bencodedList.getCompositeValues();

        assertAll("Validating empty iterator state and values list size",
                () -> assertFalse(collection.iterator().hasNext()),
                () -> assertEquals(0, collection.size())
        );
    }


    @Test
    void nullElementsAreNotAllowedInTheList() {
        BencodedList bencodedList = new BencodedList();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            // consciously adding null element to raise the exception
            bencodedList.add(null);

        }, "BencodedList should throw illegal argument exception on attempt to " +
                "add a null element. But it does not.");

        assertEquals("Null elements are not allowed for BencodedList",
                exception.getMessage(),
                "Unexpected message in the correctly thrown exception");

    }

    @Test
    void testIteratorWithPlainSequence() {
        BencodedInteger bint1 = new BencodedInteger(1);
        BencodedInteger bint2 = new BencodedInteger(2);
        BencodedInteger bint3 = new BencodedInteger(3);

        BencodedList bencodedList = new BencodedList();
        bencodedList.add(bint1);
        bencodedList.add(bint2);
        bencodedList.add(bint3);

        Iterator iterator = bencodedList.iterator();

        assertAll("Validating correct order of elements",
                () -> assertTrue(iterator.hasNext()),
                () -> assertEquals(bint1, iterator.next()),
                () -> assertTrue(iterator.hasNext()),
                () -> assertEquals(bint2, iterator.next()),
                () -> assertTrue(iterator.hasNext()),
                () -> assertEquals(bint3, iterator.next()),
                () -> assertFalse(iterator.hasNext())
        );
    }

    @Test
    void testIteratorWithMixedSequence() {
        BencodedInteger bint1 = new BencodedInteger(10);
        BencodedInteger bint2 = new BencodedInteger(20);
        BencodedInteger bint3 = new BencodedInteger(30);

        BencodedList bencodedList = new BencodedList();
        bencodedList.add(0, bint2);
        bencodedList.add(1, bint3);
        bencodedList.add(2, bint1);

        Iterator iterator = bencodedList.iterator();

        assertAll("Validating correct order of elements",
                () -> assertTrue(iterator.hasNext()),
                () -> assertEquals(bint2, iterator.next()),
                () -> assertTrue(iterator.hasNext()),
                () -> assertEquals(bint3, iterator.next()),
                () -> assertTrue(iterator.hasNext()),
                () -> assertEquals(bint1, iterator.next()),
                () -> assertFalse(iterator.hasNext())
        );
    }


    @Test
    void testCorrectSequenceOrdering() {
        BencodedInteger bint1 = new BencodedInteger(11);
        BencodedInteger bint2 = new BencodedInteger(22);
        BencodedByteSequence bbs1 = new BencodedByteSequence("abc");

        BencodedList bencodedList = new BencodedList();
        bencodedList.add(bint1);
        bencodedList.add(bint2);
        bencodedList.add(bbs1);

        BencodedObject bo1 = bencodedList.get(0);
        BencodedObject bo2 = bencodedList.get(1);
        BencodedObject bo3 = bencodedList.get(2);

        assertAll("Validating correct order of elements",
                () -> assertEquals(bint1, bo1),
                () -> assertEquals(bint2, bo2),
                () -> assertEquals(bbs1, bo3)
        );
    }


    @Test
    void testGettingElementByNegativeIndex() {
        BencodedInteger bint1 = new BencodedInteger(11);

        BencodedList bencodedList = new BencodedList();
        bencodedList.add(bint1);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            // consciously incorrect index (negative)
            final int index = -1;
            bencodedList.get(index);

        }, "BencodedList behaves incorrectly upon a negative index parameter");

        assertEquals("Incorrect index value: -1 for collection with size: 1",
                exception.getMessage(),
                "Unexpected message in correctly thrown exception");
    }


    @Test
    void testGettingElementByTooLargeIndex() {
        BencodedInteger bint1 = new BencodedInteger(1);
        BencodedInteger bint2 = new BencodedInteger(2);

        BencodedList bencodedList = new BencodedList();
        bencodedList.add(bint1);
        bencodedList.add(bint2);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            // consciously incorrect index (too big)
            final int index = 2;
            bencodedList.get(index);

        }, "BencodedList behaves incorrectly upon a large index parameter");

        assertEquals("Incorrect index value: 2 for collection with size: 2",
                exception.getMessage(),
                "Unexpected message in correctly thrown exception");
    }


    @Test
    void containsWorksProperly() {
        BencodedInteger bint1 = new BencodedInteger(1);
        BencodedInteger bint2 = new BencodedInteger(2);
        BencodedByteSequence bbs1 = new BencodedByteSequence("abc");

        BencodedList bencodedList = new BencodedList();

        assertAll("Validating elements which are not in the list",
                () -> assertFalse(bencodedList.contains(bint1)),
                () -> assertFalse(bencodedList.contains(bint2)),
                () -> assertFalse(bencodedList.contains(bbs1))
        );

        bencodedList.add(bint1);
        bencodedList.add(bint2);
        bencodedList.add(bbs1);

        assertAll("Validating elements which are in the list",
                () -> assertTrue(bencodedList.contains(bint1)),
                () -> assertTrue(bencodedList.contains(bint2)),
                () -> assertTrue(bencodedList.contains(bbs1))
        );
    }

    @Test
    void testRemoveByObjectReference() {
        BencodedInteger bint1 = new BencodedInteger(1);
        BencodedInteger bint2 = new BencodedInteger(2);
        BencodedByteSequence bbs1 = new BencodedByteSequence("abc");

        BencodedList bencodedList = new BencodedList();
        bencodedList.add(bint1);
        bencodedList.add(bint2);
        bencodedList.add(bbs1);

        boolean objectHasRemoved = bencodedList.remove(bint2);
        assertAll("Validating state after element removal",
                () -> assertTrue(objectHasRemoved),
                () -> assertEquals(2, bencodedList.size())
        );

        BencodedObject firstRemainingObject = bencodedList.get(0);
        BencodedObject secondRemainingObject = bencodedList.get(1);

        assertAll("Checking remaining objects",
                () -> assertEquals(bint1, firstRemainingObject),
                () -> assertEquals(bbs1, secondRemainingObject)
        );
    }


    @Test
    void testRemoveByObjectIndex() {
        BencodedInteger bint1 = new BencodedInteger(1);
        BencodedInteger bint2 = new BencodedInteger(2);
        BencodedByteSequence bbs1 = new BencodedByteSequence("abc");

        BencodedList bencodedList = new BencodedList();
        bencodedList.add(bint1);
        bencodedList.add(bint2);
        bencodedList.add(bbs1);

        BencodedObject removedObject = bencodedList.remove(0);
        assertAll("Validating state after element removal",
                () -> assertEquals(bint1, removedObject),
                () -> assertEquals(2, bencodedList.size())
        );

        BencodedObject firstRemainingObject = bencodedList.get(0);
        BencodedObject secondRemainingObject = bencodedList.get(1);

        assertAll("Checking remaining objects",
                () -> assertEquals(bint2, firstRemainingObject),
                () -> assertEquals(bbs1, secondRemainingObject)
        );
    }

    @Test
    void testRemoveElementWithNegativeIndex() {
        BencodedList bencodedList = new BencodedList();
        bencodedList.add(new BencodedInteger(1));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            // consciously incorrect index (negative)
            bencodedList.remove(-1);

        }, "BencodedList behaves incorrectly upon removing an element by negative index");

        assertEquals("Incorrect index value: -1 for collection with size: 1",
                exception.getMessage(),
                "Unexpected message in correctly thrown exception");
    }

    @Test
    void testRemoveElementWithTooLargeIndex() {
        BencodedList bencodedList = new BencodedList();
        bencodedList.add(new BencodedInteger(1));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            // consciously incorrect index (too big)
            bencodedList.remove(1);

        }, "BencodedList behaves incorrectly upon removing an element by negative index");

        assertEquals("Incorrect index value: 1 for collection with size: 1",
                exception.getMessage(),
                "Unexpected message in correctly thrown exception");
    }


    @Test
    void testClear() {
        BencodedInteger bint1 = new BencodedInteger(1);
        BencodedInteger bint2 = new BencodedInteger(2);
        BencodedByteSequence bbs1 = new BencodedByteSequence("abc");

        BencodedList bencodedList = new BencodedList();
        bencodedList.add(bint1);
        bencodedList.add(bint2);
        bencodedList.add(bbs1);

        bencodedList.clear();
        assertEquals(0, bencodedList.size());

        Iterator<BencodedObject> iterator = bencodedList.iterator();
        assertFalse(iterator.hasNext());
    }


    @Test
    void sizeReturnsCorrectValue() {
        BencodedInteger bint1 = new BencodedInteger(1);
        BencodedInteger bint2 = new BencodedInteger(2);
        BencodedByteSequence bbs1 = new BencodedByteSequence("abc");

        BencodedList bencodedList = new BencodedList();
        assertEquals(0, bencodedList.size());

        bencodedList.add(bint1);
        assertEquals(1, bencodedList.size());

        bencodedList.add(bint2);
        assertEquals(2, bencodedList.size());

        bencodedList.add(bbs1);
        assertEquals(3, bencodedList.size());
    }


    @Test
    void toStringReturnsCorrectValue() {
        BencodedInteger bint1 = new BencodedInteger(1);
        BencodedInteger bint2 = new BencodedInteger(2);
        BencodedByteSequence bbs1 = new BencodedByteSequence("abc");

        List<BencodedObject> referenceList = new ArrayList<>();
        referenceList.add(bint1);
        referenceList.add(bint2);
        referenceList.add(bbs1);

        BencodedList bencodedList = new BencodedList();
        bencodedList.add(bint1);
        bencodedList.add(bint2);
        bencodedList.add(bbs1);

        String referenceString = Arrays.toString(referenceList.toArray());

        assertEquals(referenceString, bencodedList.toString());
    }

    @Test
    void equalsIsSymmetric() {
        BencodedByteSequence bbs1 = new BencodedByteSequence("abc");
        BencodedInteger bint1 = new BencodedInteger(123);

        BencodedList bencodedList1 = new BencodedList();
        BencodedList bencodedList2 = new BencodedList();

        bencodedList1.add(bbs1);
        bencodedList1.add(bint1);

        bencodedList2.add(bbs1);
        bencodedList2.add(bint1);

        assertAll("Equals is symmetric",
                () -> assertEquals(bencodedList1, bencodedList2),
                () -> assertEquals(bencodedList2, bencodedList1)
        );
    }


    @SuppressWarnings("EqualsWithItself")
    @Test
    void equalsIsReflexive() {
        BencodedByteSequence bbs1 = new BencodedByteSequence("abc");
        BencodedInteger bint1 = new BencodedInteger(123);

        BencodedList bencodedList1 = new BencodedList();

        bencodedList1.add(bbs1);
        bencodedList1.add(bint1);

        assertEquals(bencodedList1, bencodedList1);
    }


    @Test
    void equalsIsTransitive() {
        // if a.equals(b), and b.equals(c), then a.equals(c);

        BencodedByteSequence bbs1 = new BencodedByteSequence("abc");
        BencodedInteger bint1 = new BencodedInteger(123);

        BencodedList bencodedList1 = new BencodedList();
        BencodedList bencodedList2 = new BencodedList();
        BencodedList bencodedList3 = new BencodedList();

        bencodedList1.add(bbs1);
        bencodedList1.add(bint1);

        bencodedList2.add(bbs1);
        bencodedList2.add(bint1);

        bencodedList3.add(bbs1);
        bencodedList3.add(bint1);

        assertAll("Equals is transitive",
                () -> assertEquals(bencodedList1, bencodedList2),
                () -> assertEquals(bencodedList2, bencodedList3),
                () -> assertEquals(bencodedList3, bencodedList1)
        );
    }

    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    @Test
    void equalsReturnsFalseForDifferentObjectClass() {
        // if we're comparing two objects of different classes
        // equals should return false

        BencodedByteSequence bbs1 = new BencodedByteSequence("abc");
        BencodedInteger bint1 = new BencodedInteger(123);

        BencodedList bencodedList1 = new BencodedList();
        bencodedList1.add(bbs1);
        bencodedList1.add(bint1);

        assertNotEquals(bencodedList1, bint1);
    }


    @Test
    void equalsReturnsFalseForObjectsWithDifferentValues() {
        // if we're comparing two objects of the same class,
        // but with different values,
        // equals should return false

        BencodedInteger bint1 = new BencodedInteger(Long.MAX_VALUE);
        BencodedInteger bint2 = new BencodedInteger(Long.MIN_VALUE);

        BencodedList bencodedList1 = new BencodedList();
        BencodedList bencodedList2 = new BencodedList();

        bencodedList1.add(bint1);
        bencodedList2.add(bint2);

        assertNotEquals(bencodedList1, bencodedList2);
    }


    @Test
    void hashCodesAreEqualForEqualObjects() {
        // if a.equals(b), then a.hashCode() == b.hashCode();

        BencodedInteger bint1 = new BencodedInteger(Long.MAX_VALUE);
        BencodedInteger bint2 = new BencodedInteger(Long.MAX_VALUE);

        BencodedList bencodedList1 = new BencodedList();
        BencodedList bencodedList2 = new BencodedList();

        bencodedList1.add(bint1);
        bencodedList2.add(bint2);

        assertAll("Hash codes are equal for equal objects",
                () -> assertEquals(bencodedList1, bencodedList2),
                () -> assertEquals(bencodedList1.hashCode(), bencodedList2.hashCode())
        );
    }

    @SuppressWarnings("ObjectEqualsNull")
    @Test
    void equalsToNullReturnsFalse() {
        // a.equals(null) should return false
        BencodedInteger bint1 = new BencodedInteger(Long.MAX_VALUE);
        BencodedList bencodedList1 = new BencodedList();
        bencodedList1.add(bint1);

        assertNotEquals(null, bencodedList1);
    }


    @Test
    void nonDirectCircularReferencesCanBeDetectedProperly() {

        BencodedInteger bint1 = new BencodedInteger(Long.MAX_VALUE);
        BencodedInteger bint2 = new BencodedInteger(Long.MAX_VALUE);
        BencodedInteger bint3 = new BencodedInteger(123);

        BencodedByteSequence bbs1 = new BencodedByteSequence("abc");
        BencodedByteSequence bbs2 = new BencodedByteSequence("def");
        BencodedByteSequence bbs3 = new BencodedByteSequence("ghi");

        BencodedList bencodedList1 = new BencodedList();
        BencodedList bencodedList2 = new BencodedList();
        BencodedList bencodedList3 = new BencodedList();

        bencodedList1.add(bint1);
        bencodedList1.add(bencodedList2);
        bencodedList1.add(bint2);

        bencodedList2.add(bint3);
        bencodedList2.add(bencodedList3);

        bencodedList3.add(bbs1);
        bencodedList3.add(bencodedList1);
        bencodedList3.add(bbs2);
        bencodedList3.add(bbs3);

        String expectedExceptionMessage = "Upon writing to stream, circular reference found in " +
                BencodedList.class.getCanonicalName();
        String unexpectedExceptionMessageDiagnostics = "Unexpected message in correctly thrown exception";
        String noExceptionMessage = "BencodedList with circular reference inside should throw " +
                "circular reference exception upon writing to the stream. But it does not.";

        CircularReferenceException exception1 = assertThrows(CircularReferenceException.class, () -> {
            // consciously trying to serialize a list with circular reference inside

            OutputStream os = new ByteArrayOutputStream();
            bencodedList1.writeObject(os);

        }, noExceptionMessage);

        assertEquals(expectedExceptionMessage,
                exception1.getMessage(),
                unexpectedExceptionMessageDiagnostics);

        CircularReferenceException exception2 = assertThrows(CircularReferenceException.class, () -> {
            // consciously trying to serialize a list with circular reference inside

            OutputStream os = new ByteArrayOutputStream();
            bencodedList2.writeObject(os);

        }, noExceptionMessage);

        assertEquals(expectedExceptionMessage,
                exception2.getMessage(),
                unexpectedExceptionMessageDiagnostics);


        CircularReferenceException exception3 = assertThrows(CircularReferenceException.class, () -> {
            // consciously trying to serialize a list with circular reference inside

            OutputStream os = new ByteArrayOutputStream();
            bencodedList3.writeObject(os);

        }, noExceptionMessage);

        assertEquals(expectedExceptionMessage,
                exception3.getMessage(),
                unexpectedExceptionMessageDiagnostics);
    }


    @Test
    void directCircularReferencesCanBeDetectedProperly() {

        BencodedInteger bint1 = new BencodedInteger(Long.MAX_VALUE);
        BencodedInteger bint2 = new BencodedInteger(Long.MAX_VALUE);
        BencodedInteger bint3 = new BencodedInteger(123);

        BencodedByteSequence bbs1 = new BencodedByteSequence("abc");
        BencodedByteSequence bbs2 = new BencodedByteSequence("def");
        BencodedByteSequence bbs3 = new BencodedByteSequence("ghi");

        BencodedList bencodedList = new BencodedList();
        bencodedList.add(bint1);
        bencodedList.add(bint2);
        bencodedList.add(bint3);
        bencodedList.add(bencodedList);
        bencodedList.add(bbs1);
        bencodedList.add(bbs2);
        bencodedList.add(bbs3);

        String expectedExceptionMessage = "Upon writing to stream, circular reference found in " +
                BencodedList.class.getCanonicalName();
        String unexpectedExceptionMessageDiagnostics = "Unexpected message in correctly thrown exception";
        String noExceptionMessage = "BencodedList with circular reference inside should throw " +
                "circular reference exception upon writing to the stream. But it does not.";

        CircularReferenceException exception3 = assertThrows(CircularReferenceException.class, () -> {
            // consciously trying to serialize a list with circular reference inside

            OutputStream os = new ByteArrayOutputStream();
            bencodedList.writeObject(os);

        }, noExceptionMessage);

        assertEquals(expectedExceptionMessage,
                exception3.getMessage(),
                unexpectedExceptionMessageDiagnostics);
    }


    @Test
    void writeObjectWorksCorrectly() throws IOException, CircularReferenceException {

        long firstNumber = 123;
        long secondNumber = 456;

        String serializedFirstNumber = BencodedInteger.SERIALIZED_PREFIX +
                String.valueOf(firstNumber) + BencodedInteger.SERIALIZED_SUFFIX;

        String serializedSecondNumber = BencodedInteger.SERIALIZED_PREFIX +
                String.valueOf(secondNumber) + BencodedInteger.SERIALIZED_SUFFIX;

        String expectedSerializedForm =
                BencodedList.SERIALIZED_PREFIX +
                        serializedFirstNumber +
                        serializedSecondNumber +
                        BencodedList.SERIALIZED_SUFFIX;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        BencodedInteger bint1 = new BencodedInteger(firstNumber);
        BencodedInteger bint2 = new BencodedInteger(secondNumber);
        BencodedList bencodedList = new BencodedList();
        bencodedList.add(bint1);
        bencodedList.add(bint2);

        bencodedList.writeObject(baos);

        String serializedForm = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        assertEquals(expectedSerializedForm, serializedForm);
    }


    @Test
    void loadingFromStream() throws IOException, BencodeFormatException {

        long firstNumber = 123;
        long secondNumber = 456;

        String serializedFirstNumber = BencodedInteger.SERIALIZED_PREFIX +
                String.valueOf(firstNumber) + BencodedInteger.SERIALIZED_SUFFIX;

        String serializedSecondNumber = BencodedInteger.SERIALIZED_PREFIX +
                String.valueOf(secondNumber) + BencodedInteger.SERIALIZED_SUFFIX;

        String streamContents =
                BencodedList.SERIALIZED_PREFIX +
                        serializedFirstNumber +
                        serializedSecondNumber +
                        BencodedList.SERIALIZED_SUFFIX;

        InputStream is = new ByteArrayInputStream(streamContents.getBytes(StandardCharsets.UTF_8));
        BencodeStreamReader bsr = new BencodeStreamReader(is);

        BencodedList bencodedList = new BencodedList(bsr);
        assertEquals(2, bencodedList.size());

        BencodedObject firstListElement = bencodedList.get(0);
        BencodedObject secondListElement = bencodedList.get(1);

        assertAll("Deserialized list element types",
                () -> assertTrue(firstListElement instanceof BencodedInteger),
                () -> assertTrue(secondListElement instanceof BencodedInteger));

        assertAll("Deserialized list element values",
                () -> assertEquals(firstNumber, ((BencodedInteger) firstListElement).getValue()),
                () -> assertEquals(secondNumber, ((BencodedInteger) secondListElement).getValue()));
    }


    @Test
    void loadingFromIncompleteStreamWithCorrectPrefix() {
        String incompleteStreamContents = String.valueOf(BencodedList.SERIALIZED_PREFIX);

        InputStream is = new ByteArrayInputStream(incompleteStreamContents.getBytes(StandardCharsets.UTF_8));
        BencodeStreamReader bsr = new BencodeStreamReader(is);

        BencodeFormatException exception =
                assertThrows(BencodeFormatException.class, () ->
                        new BencodedList(bsr), "BencodedList constructor behaves incorrectly on incomplete " +
                        "serialized data with correct prefix");

        assertEquals("Unexpected end of the stream",
                exception.getMessage(),
                "Unexpected message in correctly thrown exception");

    }

    @Test
    void loadingFromIncompleteStreamWithIncorrectPrefix() {
        String incompleteStreamContents = "x";

        InputStream is = new ByteArrayInputStream(incompleteStreamContents.getBytes(StandardCharsets.UTF_8));
        BencodeStreamReader bsr = new BencodeStreamReader(is);

        BencodeFormatException exception =
                assertThrows(BencodeFormatException.class, () -> new BencodedList(bsr),
                        "BencodedList constructor behaves incorrectly on incomplete serialized " +
                                "data with incorrect prefix");

        assertEquals("Incorrect stream position, expected prefix character: l",
                exception.getMessage(),
                "Unexpected message in correctly thrown exception");
    }


    @Test
    void loadingFromIncompleteStreamWhereSuffixIsNotProvided() {
        String serializedFirstNumber = BencodedInteger.SERIALIZED_PREFIX +
                String.valueOf(123) + BencodedInteger.SERIALIZED_SUFFIX;

        String streamContents = BencodedList.SERIALIZED_PREFIX + serializedFirstNumber;

        InputStream is = new ByteArrayInputStream(streamContents.getBytes(StandardCharsets.UTF_8));
        BencodeStreamReader bsr = new BencodeStreamReader(is);

        BencodeFormatException exception =
                assertThrows(BencodeFormatException.class, () ->
                                new BencodedList(bsr),
                        "BencodedList constructor behaves incorrectly on incomplete serialized " +
                                "data without list suffix");

        assertEquals("Unexpected end of the stream",
                exception.getMessage(),
                "Unexpected message in correctly thrown exception");
    }


    @Test
    void testCompositeState() {
        BencodedList bencodedList = new BencodedList();

        BencodedByteSequence bbs = new BencodedByteSequence("abc");
        BencodedInteger bint = new BencodedInteger(Long.MAX_VALUE);
        bencodedList.add(bbs);
        bencodedList.add(bint);

        assertAll("Ensuring the correct composite object state",
                () -> assertTrue(bencodedList.isCompositeObject()),
                () -> assertEquals(2, bencodedList.getCompositeValues().size())
        );
    }

    @Test
    void testConstructorWithCorrectIterableArgument() {
        List<BencodedObject> initialList = new ArrayList<>();

        BencodedInteger element1 = new BencodedInteger(1);
        BencodedInteger element2 = new BencodedInteger(2);
        BencodedByteSequence element3 = new BencodedByteSequence("abc");

        initialList.add(element1);
        initialList.add(element2);
        initialList.add(element3);

        BencodedList bencodedList = new BencodedList(initialList);

        assertAll("BencodedList(Iterable) constructor works properly with correct argument",
                () -> assertEquals(3, bencodedList.size()),
                () -> assertSame(element1, bencodedList.get(0)),
                () -> assertSame(element2, bencodedList.get(1)),
                () -> assertSame(element3, bencodedList.get(2)));
    }

    @Test
    void testConstructorWithNullIterableArgument() {

        List<BencodedObject> initialList = null;

        assertThrows(IllegalArgumentException.class, () ->
                {
                    new BencodedList(initialList);
                },
                "BencodedList(Iterable) constructor behaves incorrectly on null argument. " +
                        "Expected IllegalArgumentException.");
    }

    @Test
    void testConstructorWithIterableArgumentThatContainsNullElements() {
        List<BencodedObject> initialList = new ArrayList<>();

        BencodedInteger element1 = new BencodedInteger(123);
        BencodedInteger element2 = null;

        initialList.add(element1);
        initialList.add(element2);

        assertThrows(IllegalArgumentException.class, () ->
                {
                    new BencodedList(initialList);
                },
                "BencodedList(Iterable) constructor behaves incorrectly on non-null argument, that contains" +
                        " null elements inside. " +
                        "Expected IllegalArgumentException.");
    }

    @Test
    void testIndexOfWithNonNullArgument() {
        BencodedList bencodedList = new BencodedList();

        BencodedInteger element1 = new BencodedInteger(1);
        BencodedInteger element2 = new BencodedInteger(2);
        BencodedInteger element3 = new BencodedInteger(3);

        bencodedList.add(element1);
        bencodedList.add(element2);

        assertAll("BencodedList.indexOf() works properly with correct argument",
                () -> assertEquals(1, bencodedList.indexOf(element2)),
                () -> assertEquals(0, bencodedList.indexOf(element1)),
                () -> assertEquals(-1, bencodedList.indexOf(element3)));
    }

    @Test
    void testIndexOfWithNullArgument() {
        BencodedList bencodedList = new BencodedList();

        BencodedInteger element1 = new BencodedInteger(1);
        BencodedInteger element2 = null;

        bencodedList.add(element1);

        assertThrows(IllegalArgumentException.class, () ->
                {
                    bencodedList.indexOf(element2);
                },
                "BencodedList.indexOf() works incorrectly on null argument. " +
                        "Expected IllegalArgumentException.");
    }

    @Test
    void testLastIndexOfWithNonNullArgument() {
        BencodedList bencodedList = new BencodedList();

        BencodedInteger element1 = new BencodedInteger(1);
        BencodedInteger element2 = new BencodedInteger(2);
        BencodedInteger element3 = new BencodedInteger(3);
        BencodedInteger element4 = new BencodedInteger(4);

        bencodedList.add(element1);
        bencodedList.add(element2);
        bencodedList.add(element3);
        bencodedList.add(element1);
        bencodedList.add(element2);
        bencodedList.add(element3);
        bencodedList.add(element1);
        bencodedList.add(element2);
        bencodedList.add(element3);

        assertAll("BencodedList.lastIndexOf() works properly with correct argument",
                () -> assertEquals(7, bencodedList.lastIndexOf(element2)),
                () -> assertEquals(6, bencodedList.lastIndexOf(element1)),
                () -> assertEquals(8, bencodedList.lastIndexOf(element3)),
                () -> assertEquals(-1, bencodedList.lastIndexOf(element4)));
    }

    @Test
    void testLastIndexOfWithNullArgument() {
        BencodedList bencodedList = new BencodedList();

        BencodedInteger element1 = new BencodedInteger(1);
        BencodedInteger element2 = null;

        bencodedList.add(element1);

        assertThrows(IllegalArgumentException.class, () ->
                {
                    bencodedList.lastIndexOf(element2);
                },
                "BencodedList.lastIndexOf() works incorrectly on null argument. " +
                        "Expected IllegalArgumentException.");
    }
}