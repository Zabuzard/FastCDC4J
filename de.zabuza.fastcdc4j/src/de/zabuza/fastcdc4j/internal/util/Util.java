package de.zabuza.fastcdc4j.internal.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Collection of various utility methods of no particular topic.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public enum Util {
	;
	/**
	 * All characters available in the hexadecimal-system, as UTF-8 encoded array.
	 */
	private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.UTF_8);

	/**
	 * Creates a hexadecimal representation of the given binary data.
	 *
	 * @param bytes The binary data to convert
	 *
	 * @return Hexadecimal representation
	 */
	@SuppressWarnings("MagicNumber")
	public static String bytesToHex(final byte[] bytes) {
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
	 * @param method The method to use for hashing, must be supported by {@link MessageDigest}.
	 * @param data   The data to hash
	 *
	 * @return The computed hash
	 */
	public static byte[] hash(final String method, final byte[] data) {
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
	 * @param x The value to compute the log2 of
	 *
	 * @return The log2 of the given value
	 */
	public static int log2(int x) {
		if (x >= 0) {
			// Safe binary-only conversion without floating points
			return Integer.bitCount(Integer.highestOneBit(x) - 1);
		}
		// Adding epsilon to mitigate floating point errors, see https://stackoverflow.com/a/3305400/2411243
		return (int) (Math.log(x) / Math.log(2) + 1e-12);


	}
}
