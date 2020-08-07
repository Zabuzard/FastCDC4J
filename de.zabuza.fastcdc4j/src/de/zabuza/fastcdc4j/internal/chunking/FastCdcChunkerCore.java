package de.zabuza.fastcdc4j.internal.chunking;

import de.zabuza.fastcdc4j.external.chunking.IterativeStreamChunkerCore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * Implementation of an iterative stream chunker core that chunks according to the FastCDC algorithm (by Wen Xia et al.
 * (<a href="https://www.usenix.org/system/files/conference/atc16/atc16-paper-xia.pdf">publication</a>)).
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class FastCdcChunkerCore implements IterativeStreamChunkerCore {
	/**
	 * Maximal size for a single chunk, in bytes.
	 */
	@SuppressWarnings("MultiplyOrDivideByPowerOfTwo")
	private static final int MAX_SIZE = 64 * 1_024; // TODO Make dependent on given expected size
	/**
	 * Minimal size for a single chunk, in bytes.
	 */
	@SuppressWarnings("MultiplyOrDivideByPowerOfTwo")
	private static final int MIN_SIZE = 2 * 1_024; // TODO Make dependent on given expected size
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
	 * Mask for the fingerprint that is used for bigger windows, to increase the likelihood of a split.
	 */
	private final long maskLarge;
	/**
	 * Mask for the fingerprint that is used for smaller windows, to decrease the likelihood of a split.
	 */
	private final long maskSmall;

	/**
	 * Creates a new core.
	 *
	 * @param expectedSize The expected size for a single chunk, in bytes
	 * @param gear         The hash table, also known as {@code gear} used as noise to improve the splitting behavior
	 *                     for relatively similar content
	 * @param maskSmall    Mask for the fingerprint that is used for smaller windows, to decrease the likelihood of a
	 *                     split
	 * @param maskLarge    Mask for the fingerprint that is used for bigger windows, to increase the likelihood of a
	 *                     split
	 */
	public FastCdcChunkerCore(final int expectedSize, final long[] gear, final long maskSmall, final long maskLarge) {
		this.expectedSize = expectedSize;
		this.gear = gear.clone();
		this.maskSmall = maskSmall;
		this.maskLarge = maskLarge;
	}

	@Override
	public byte[] readNextChunk(final InputStream stream, final long size, final long currentOffset) {
		try (final ByteArrayOutputStream dataBuffer = new ByteArrayOutputStream()) {
			int normalSize = expectedSize;
			//noinspection StandardVariableNames
			long n = size - currentOffset;
			if (n <= 0) {
				throw new IllegalArgumentException(
						"Attempting to read the next chunk but out of available bytes, as indicated by size");
			}
			if (n <= FastCdcChunkerCore.MIN_SIZE) {
				return stream.readNBytes((int) n);
			}
			if (n >= FastCdcChunkerCore.MAX_SIZE) {
				n = FastCdcChunkerCore.MAX_SIZE;
			} else if (n <= normalSize) {
				normalSize = (int) n;
			}

			long fingerprint = 0;
			int i = FastCdcChunkerCore.MIN_SIZE;
			dataBuffer.write(stream.readNBytes(i));

			//noinspection ForLoopWithMissingComponent
			for (; i < normalSize; i++) {
				final int data = stream.read();
				if (data == -1) {
					throw new IllegalStateException(
							"Attempting to read a byte from the stream but the stream has ended");
				}
				dataBuffer.write(data);
				fingerprint = (fingerprint << 1) + gear[data];
				if ((fingerprint & maskSmall) == 0) {
					return dataBuffer.toByteArray();
				}
			}
			//noinspection ForLoopWithMissingComponent
			for (; i < n; i++) {
				final int data = stream.read();
				if (data == -1) {
					throw new IllegalStateException(
							"Attempting to read a byte from the stream but the stream has ended");
				}
				dataBuffer.write(data);
				fingerprint = (fingerprint << 1) + gear[data];
				if ((fingerprint & maskLarge) == 0) {
					return dataBuffer.toByteArray();
				}
			}

			return dataBuffer.toByteArray();

		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
