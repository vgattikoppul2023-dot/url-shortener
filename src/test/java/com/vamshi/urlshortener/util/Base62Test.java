package com.vamshi.urlshortener.util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class Base62Test {

    @Test
    void encodeThenDecodeReturnsOriginalId() {
        for (long id : new long[]{0, 1, 61, 62, 12345, 9_999_999, Long.MAX_VALUE / 2}) {
            assertEquals(id, Base62.decode(Base62.encode(id)));
        }
    }

    @Test
    void codesAreAtLeastSevenCharacters() {
        assertTrue(Base62.encode(1).length() >= 7);
    }

    @Test
    void codesAreUnique() {
        Set<String> seen = new HashSet<>();
        for (long id = 1; id <= 100_000; id++) {
            assertTrue(seen.add(Base62.encode(id)), "duplicate code for id " + id);
        }
    }

    @Test
    void rejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> Base62.encode(-1));
        assertThrows(IllegalArgumentException.class, () -> Base62.decode("abc$"));
    }
}
