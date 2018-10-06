package com.github.cdefgah.bencoder4j.io;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BencodeStreamReaderTest {

    @Test
    void operatingWithNullOrEmptySequenceWorksProperly() {
        InputStream is = new ByteArrayInputStream(new byte[0]);
        BencodeStreamReader bsr = new BencodeStreamReader(is);

        assertAll("Ensuring correct action on reading to null or empty sequence",
                () -> assertEquals(0, bsr.readByteSequence(null)),
                () -> assertEquals(0, bsr.readByteSequence(new byte[0]))
        );
    }
}