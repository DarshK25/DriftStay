package com.driftstay.common.util;

import lombok.experimental.UtilityClass;

import java.security.SecureRandom;
import java.time.Instant;

/**
 * ULID (Universally Unique Lexicographically Sortable Identifier) generator.
 * Replaces AtomicLong-based reference generation to prevent collision on server restart.
 *
 * Format: 26-character Crockford Base32 encoded string
 * - First 10 chars: timestamp (millisecond precision)
 * - Last 16 chars: random
 *
 * Example: 01ARZ3NDEKTSV4RRFFQ69G5FAV
 */
@UtilityClass
public class UlidGenerator {

    private static final char[] ENCODING = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generate a ULID string.
     */
    public static String generate() {
        return generate(Instant.now().toEpochMilli());
    }

    /**
     * Generate a ULID string with a specific timestamp.
     */
    public static String generate(long timestamp) {
        char[] buffer = new char[26];

        // Encode timestamp (10 chars)
        encodeTimestamp(buffer, timestamp);

        // Encode random (16 chars, enough for crypto-grade randomness)
        byte[] random = new byte[10];
        RANDOM.nextBytes(random);
        encodeRandom(buffer, random);

        return new String(buffer);
    }

    /**
     * Generate a shorter public ID (16 chars) for non-primary identifiers.
     */
    public static String generateShort() {
        char[] buffer = new char[16];
        long timestamp = Instant.now().toEpochMilli();

        // First 6 chars from timestamp
        buffer[0] = ENCODING[(int) ((timestamp >>> 45) & 0x1F)];
        buffer[1] = ENCODING[(int) ((timestamp >>> 40) & 0x1F)];
        buffer[2] = ENCODING[(int) ((timestamp >>> 35) & 0x1F)];
        buffer[3] = ENCODING[(int) ((timestamp >>> 30) & 0x1F)];
        buffer[4] = ENCODING[(int) ((timestamp >>> 25) & 0x1F)];
        buffer[5] = ENCODING[(int) ((timestamp >>> 20) & 0x1F)];

        // Remaining 10 chars from random
        byte[] random = new byte[7];
        RANDOM.nextBytes(random);
        for (int i = 0; i < 10; i++) {
            int byteIndex = (i * 8) / 7;
            int bitOffset = (i * 8) % 7;
            int value;
            if (bitOffset <= 2) {
                value = (random[byteIndex] >>> (2 - bitOffset)) & 0x1F;
            } else {
                int upper = (random[byteIndex] << (bitOffset - 2)) & 0xFF;
                int lower = (byteIndex + 1 < random.length ? (random[byteIndex + 1] & 0xFF) >>> (9 - bitOffset) : 0);
                value = (upper | lower) & 0x1F;
            }
            buffer[6 + i] = ENCODING[value];
        }

        return new String(buffer);
    }

    // ========================================================================
    // PRIVATE HELPERS
    // ========================================================================

    private static void encodeTimestamp(char[] buffer, long timestamp) {
        buffer[0] = ENCODING[(int) ((timestamp >>> 45) & 0x1F)];
        buffer[1] = ENCODING[(int) ((timestamp >>> 40) & 0x1F)];
        buffer[2] = ENCODING[(int) ((timestamp >>> 35) & 0x1F)];
        buffer[3] = ENCODING[(int) ((timestamp >>> 30) & 0x1F)];
        buffer[4] = ENCODING[(int) ((timestamp >>> 25) & 0x1F)];
        buffer[5] = ENCODING[(int) ((timestamp >>> 20) & 0x1F)];
        buffer[6] = ENCODING[(int) ((timestamp >>> 15) & 0x1F)];
        buffer[7] = ENCODING[(int) ((timestamp >>> 10) & 0x1F)];
        buffer[8] = ENCODING[(int) ((timestamp >>> 5) & 0x1F)];
        buffer[9] = ENCODING[(int) (timestamp & 0x1F)];
    }

    private static void encodeRandom(char[] buffer, byte[] random) {
        buffer[10] = ENCODING[(random[0] & 0x1F)];
        buffer[11] = ENCODING[((random[0] >>> 5) | ((random[1] & 0x03) << 3)) & 0x1F];
        buffer[12] = ENCODING[((random[1] >>> 2) & 0x1F)];
        buffer[13] = ENCODING[((random[1] >>> 7) | ((random[2] & 0x0F) << 1)) & 0x1F];
        buffer[14] = ENCODING[((random[2] >>> 4) | ((random[3] & 0x01) << 4)) & 0x1F];
        buffer[15] = ENCODING[((random[3] >>> 1) & 0x1F)];
        buffer[16] = ENCODING[((random[3] >>> 6) | ((random[4] & 0x07) << 2)) & 0x1F];
        buffer[17] = ENCODING[((random[4] >>> 3) & 0x1F)];
        buffer[18] = ENCODING[(random[4] >>> 8 | ((random[5] & 0x1F) << 0)) & 0x1F];
        buffer[19] = ENCODING[((random[5] >>> 5) | ((random[6] & 0x03) << 3)) & 0x1F];
        buffer[20] = ENCODING[((random[6] >>> 2) & 0x1F)];
        buffer[21] = ENCODING[(random[6] >>> 7) & 0x1F];
        buffer[22] = ENCODING[(random[7] & 0x1F)];
        buffer[23] = ENCODING[((random[7] >>> 5) | ((random[8] & 0x03) << 3)) & 0x1F];
        buffer[24] = ENCODING[((random[8] >>> 2) & 0x1F)];
        buffer[25] = ENCODING[((random[8] >>> 7) | ((random[9] & 0x0F) << 1)) & 0x1F];
    }
}
