package de.zabuza.fastcdc4j.internal.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * Collection of various utility methods of no particular topic.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public enum Util {
	;
	/**
	 * Small delta to counter floating point issues when rounding down would accidentally round down a full integer to
	 * much.
	 */
	private static final double FLOATING_DELTA = 1.0e-12;
	/**
	 * All characters available in the hexadecimal-system, as UTF-8 encoded array.
	 */
	private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.UTF_8);

	/**
	 * Creates a hexadecimal representation of the given binary data.
	 *
	 * @param bytes The binary data to convert, not null
	 *
	 * @return Hexadecimal representation
	 */
	@SuppressWarnings("MagicNumber")
	public static String bytesToHex(final byte[] bytes) {
		Objects.requireNonNull(bytes);
		// See https://stackoverflow.com/a/9855338/2411243
		//noinspection MultiplyOrDivideByPowerOfTwo
		final byte[] hexChars = new byte[bytes.length * 2];
		//noinspection ArrayLengthInLoopCondition
		for (int j = 0; j < bytes.length; j++) {
			final int v = bytes[j] & 0xFF;
			//noinspection MultiplyOrDivideByPowerOfTwo
			hexChars[j * 2] = Util.HEX_ARRAY[v >>> 4];
			//noinspection MultiplyOrDivideByPowerOfTwo
			hexChars[j * 2 + 1] = Util.HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars, StandardCharsets.UTF_8);
	}

	/**
	 * Hashes the given data using the given method.
	 *
	 * @param method The method to use for hashing, must be supported by {@link MessageDigest}, not null
	 * @param data   The data to hash, not null
	 *
	 * @return The computed hash
	 */
	public static byte[] hash(final String method, final byte[] data) {
		Objects.requireNonNull(method);
		Objects.requireNonNull(data);
		try {
			return MessageDigest.getInstance(method)
					.digest(data);
		} catch (final NoSuchAlgorithmException e) {
			throw new IllegalStateException("Hash method must be supported", e);
		}
	}

	/**
	 * Computes the logarithm to the base 2 of the given value.
	 *
	 * @param x The value to compute the log2 of, must be positive and not zero
	 *
	 * @return The log2 of the given value
	 */
	public static int log2(final int x) {
		Validations.requirePositiveNonZero(x, "Value");
		// Safe binary-only conversion without floating points
		return Integer.bitCount(Integer.highestOneBit(x) - 1);
	}
}
