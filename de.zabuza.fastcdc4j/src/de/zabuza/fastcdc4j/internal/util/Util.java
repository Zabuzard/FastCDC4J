package de.zabuza.fastcdc4j.internal.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Collection of various utility methods of no particular topic.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class Util {
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
	public static String bytesToHex(byte[] bytes) {
		// See https://stackoverflow.com/a/9855338/2411243
		byte[] hexChars = new byte[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
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
	public static byte[] hash(String method, byte[] data) {
		try {
			return MessageDigest.getInstance(method)
					.digest(data);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("Hash method must be supported", e);
		}
	}
}
