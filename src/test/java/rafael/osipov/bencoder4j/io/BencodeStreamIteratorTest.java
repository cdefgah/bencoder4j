package rafael.osipov.bencoder4j.io;

import org.junit.jupiter.api.Test;
import rafael.osipov.bencoder4j.BencodeFormatException;
import rafael.osipov.bencoder4j.model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class BencodeStreamIteratorTest {

    @Test
    void tryingToGetNextElementOnExhaustedIterator() throws IOException {
        String streamContents = "";
        InputStream is = new ByteArrayInputStream(streamContents.getBytes(StandardCharsets.UTF_8));
        BencodeStreamIterator bsi = new BencodeStreamIterator(is);

        assertFalse(bsi.hasNext());
        assertThrows(NoSuchElementException.class, bsi::next,
                            "Iterator behaves incorrectly upon getting the next element when it is exhausted");
    }


    @Test
    void tryingToGetVariousObjects() throws IOException, BencodeFormatException {
        String streamContents = "i1e3:123li2eed3:abci5ee";
        InputStream is = new ByteArrayInputStream(streamContents.getBytes(StandardCharsets.UTF_8));
        BencodeStreamIterator bsi = new BencodeStreamIterator(is);

        assertTrue(bsi.hasNext());
        BencodedObject bencodedObject1 = bsi.next();
        assertTrue(bencodedObject1 instanceof BencodedInteger);
        assertEquals(1, (((BencodedInteger)bencodedObject1).getValue()) );

        assertTrue(bsi.hasNext());
        BencodedObject bencodedObject2 = bsi.next();
        assertTrue(bencodedObject2 instanceof BencodedByteSequence);
        assertEquals("123", ((BencodedByteSequence) bencodedObject2).toUTF8String());

        assertTrue(bsi.hasNext());
        BencodedObject bencodedObject3 = bsi.next();
        assertTrue(bencodedObject3 instanceof BencodedList);

        BencodedList bencodedList = (BencodedList)bencodedObject3;
        assertEquals(1, bencodedList.size());

        BencodedInteger bencodedInteger = (BencodedInteger) bencodedList.get(0);
        assertEquals(2, bencodedInteger.getValue());

        assertTrue(bsi.hasNext());
        BencodedObject bencodedObject4 = bsi.next();
        assertTrue(bencodedObject4 instanceof BencodedDictionary);

        BencodedDictionary dictionary = (BencodedDictionary) bencodedObject4;
        Iterator<BencodedByteSequence> keysIterator = dictionary.getKeysIterator();

        assertTrue(keysIterator.hasNext());
        BencodedByteSequence key = keysIterator.next();
        assertEquals("abc", key.toUTF8String());

        assertFalse(keysIterator.hasNext());
        BencodedObject dictionaryElementValue = dictionary.get(key);
        assertTrue(dictionaryElementValue instanceof BencodedInteger);

        assertEquals(5, ((BencodedInteger)dictionaryElementValue).getValue());

        assertFalse(bsi.hasNext());
    }

    @Test
    void loadIncorrectSequenceStreamViaIterator() {
        String streamContents = "x1:abc";

        InputStream is = new ByteArrayInputStream(streamContents.getBytes(StandardCharsets.UTF_8));
        BencodeStreamIterator bsi = new BencodeStreamIterator(is);

        BencodeFormatException exception =
                assertThrows(BencodeFormatException.class, bsi::next,
                                                "BencodeStreamIterator behaves incorrectly " +
                                                                       "on stream contents with incorrect length part");

        assertEquals("Unexpected character in the stream: x",
                exception.getMessage(),
                "Unexpected message in correctly thrown exception");

    }
}