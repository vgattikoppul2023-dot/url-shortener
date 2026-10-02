package com.vamshi.urlshortener.util;

/**
 * Encodes numeric database IDs into short, URL-safe base62 codes and back.
 * An offset keeps early codes from being trivially short (e.g. "1", "2").
 */
public final class Base62 {

    private static final String ALPHABET =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = ALPHABET.length();
    private static final long OFFSET = 56_800_235_584L; // 62^6 -> codes start at 7 chars

    private Base62() {}

    public static String encode(long id) {
        if (id < 0) throw new IllegalArgumentException("id must be non-negative");
        long value = id + OFFSET;
        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            sb.append(ALPHABET.charAt((int) (value % BASE)));
            value /= BASE;
        }
        return sb.reverse().toString();
    }

    public static long decode(String code) {
        if (code == null || code.isEmpty()) throw new IllegalArgumentException("code is empty");
        long value = 0;
        for (char c : code.toCharArray()) {
            int idx = ALPHABET.indexOf(c);
            if (idx < 0) throw new IllegalArgumentException("invalid character: " + c);
            value = value * BASE + idx;
        }
        return value - OFFSET;
    }
}
