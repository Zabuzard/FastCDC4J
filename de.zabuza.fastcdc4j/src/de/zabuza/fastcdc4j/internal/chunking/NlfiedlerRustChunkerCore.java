package de.zabuza.fastcdc4j.internal.chunking;

import de.zabuza.fastcdc4j.external.chunking.IterativeStreamChunkerCore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * Implementation of an iterative stream chunker core that chunks according to a modified FastCDC algorithm (by Nathan
 * Fiedler (<a href="https://github.com/nlfiedler/fastcdc-rs">source</a>)).
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class NlfiedlerRustChunkerCore implements IterativeStreamChunkerCore {
	// TODO Make dependent on given expected size
	/**
	 * Mask for the fingerprint that is used for bigger windows, to increase the likelyhood of a split.
	 */
	private static final long MASK_L = 0b1111_1111_1111L;
	// TODO Make dependent on given expected size
	/**
	 * Mask for the fingerprint that is used for smaller windows, to decrease the likelyhood of a split.
	 */
	private static final long MASK_S = 0b11_1111_1111_1111L;
	/**
	 * Maximal size for a single chunk, in bytes.
	 */
	private static final int MAX_SIZE = 64 * 1024; // TODO Make dependent on given expected size
	/**
	 * Minimal size for a single chunk, in bytes.
	 */
	private static final int MIN_SIZE = 2 * 1024; // TODO Make dependent on given expected size

	/**
	 * The expected average size for a single chunk, in bytes.
	 */
	private final int expectedSize;
	/**
	 * The hash table, also known as {@code gear} used as noise to improve the splitting behavior for relatively similar
	 * content.
	 */
	private final long[] gear;

	/**
	 * Creates a new core.
	 *
	 * @param expectedSize The expected size for a single chunk, in bytes
	 * @param gear         The hash table, also known as {@code gear} used as noise to improve the splitting behavior
	 *                     for relatively similar content
	 */
	public NlfiedlerRustChunkerCore(int expectedSize, long[] gear) {
		this.expectedSize = expectedSize;
		this.gear = gear;
	}

	@Override
	public byte[] readNextChunk(final InputStream stream, final long size, final long currentOffset) {
		try {
			int normalSize = expectedSize;
			long n = size - currentOffset;
			if (n <= 0) {
				throw new IllegalArgumentException();
			}
			if (n <= MIN_SIZE) {
				return stream.readNBytes((int) n);
			}
			if (n >= MAX_SIZE) {
				n = MAX_SIZE;
			} else if (n <= normalSize) {
				normalSize = (int) n;
			}

			ByteArrayOutputStream dataBuffer = new ByteArrayOutputStream();
			long fingerprint = 0;
			int i = MIN_SIZE;
			dataBuffer.write(stream.readNBytes(i));

			for (; i < normalSize; i++) {
				int data = stream.read();
				if (data == -1) {
					throw new IllegalStateException();
				}
				dataBuffer.write(data);
				fingerprint = (fingerprint >> 1) + gear[data];
				if ((fingerprint & MASK_S) == 0) {
					return dataBuffer.toByteArray();
				}
			}
			for (; i < n; i++) {
				int data = stream.read();
				if (data == -1) {
					throw new IllegalStateException();
				}
				dataBuffer.write(data);
				fingerprint = (fingerprint >> 1) + gear[data];
				if ((fingerprint & MASK_L) == 0) {
					return dataBuffer.toByteArray();
				}
			}

			return dataBuffer.toByteArray();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
