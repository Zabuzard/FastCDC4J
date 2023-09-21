package io.github.zabuzard.fastcdc4j.internal.chunking;

import io.github.zabuzard.fastcdc4j.external.chunking.IterativeStreamChunkerCore;
import io.github.zabuzard.fastcdc4j.internal.util.Validations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * Implementation of an iterative stream chunker core that chunks according to a modified FastCDC algorithm (by Nathan
 * Fiedler (<a href="https://github.com/nlfiedler/fastcdc-rs">source</a>)).
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class NlfiedlerRustChunkerCore implements IterativeStreamChunkerCore {
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
	 * The maximal size for a single chunk, in bytes.
	 */
	private final int maxSize;
	/**
	 * The minimal size for a single chunk, in bytes.
	 */
	private final int minSize;

	/**
	 * Creates a new core.
	 *
	 * @param expectedSize The expected size for a single chunk, in bytes, must be positive
	 * @param minSize      The minimal size for a single chunk, in bytes, must be positive and less equals expected
	 *                     size
	 * @param maxSize      The maximal size for a single chunk, in bytes, must be positive and greater equals expected
	 *                     size
	 * @param gear         The hash table, also known as {@code gear} used as noise to improve the splitting behavior
	 *                     for relatively similar content, must have a length of exactly 256, one hash per byte value
	 * @param maskSmall    Mask for the fingerprint that is used for smaller windows, to decrease the likelihood of a
	 *                     split
	 * @param maskLarge    Mask for the fingerprint that is used for bigger windows, to increase the likelihood of a
	 *                     split
	 */
	@SuppressWarnings("ConstructorWithTooManyParameters")
	public NlfiedlerRustChunkerCore(final int expectedSize, final int minSize, final int maxSize, final long[] gear,
			final long maskSmall, final long maskLarge) {
		Validations.require(minSize <= expectedSize, "Min size must be less equals expected size");
		Validations.require(maxSize >= expectedSize, "Max size must be greater equals expected size");
		Objects.requireNonNull(gear);
		//noinspection MagicNumber
		Validations.require(gear.length == 256,
				"Gear must have a length of 256, one hash per byte value, was: " + gear.length);

		this.expectedSize = Validations.requirePositive(expectedSize, "Expected size");
		this.minSize = Validations.requirePositive(minSize, "Min size");
		this.maxSize = Validations.requirePositive(maxSize, "Max size");
		this.gear = gear.clone();
		this.maskSmall = maskSmall;
		this.maskLarge = maskLarge;
	}

	@Override
	public byte[] readNextChunk(final InputStream stream, final long size, final long currentOffset) {
		Objects.requireNonNull(stream);
		Validations.requirePositiveNonZero(size, "Size");
		Validations.requirePositive(currentOffset, "Current offset");
		Validations.require(currentOffset < size, "Current offset must be less than size");

		try (final ByteArrayOutputStream dataBuffer = new ByteArrayOutputStream()) {
			int normalSize = expectedSize;
			//noinspection StandardVariableNames
			long n = size - currentOffset;
			if (n <= 0) {
				throw new IllegalArgumentException(
						"Attempting to read the next chunk but out of available bytes, as indicated by size");
			}
			if (n <= minSize) {
				return stream.readNBytes((int) n);
			}
			if (n >= maxSize) {
				n = maxSize;
			} else if (n <= normalSize) {
				normalSize = (int) n;
			}

			long fingerprint = 0;
			int i = minSize;
			dataBuffer.write(stream.readNBytes(i - 1));

			//noinspection ForLoopWithMissingComponent
			for (; i < normalSize; i++) {
				final int data = stream.read();
				if (data == -1) {
					throw new IllegalStateException(
							"Attempting to read a byte from the stream but the stream has ended");
				}
				dataBuffer.write(data);
				fingerprint = (fingerprint >> 1) + gear[data];
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
				fingerprint = (fingerprint >> 1) + gear[data];
				if ((fingerprint & maskLarge) == 0) {
					return dataBuffer.toByteArray();
				}
			}

			final int data = stream.read();
			if (data == -1) {
				throw new IllegalStateException("Attempting to read a byte from the stream but the stream has ended");
			}
			dataBuffer.write(data);

			return dataBuffer.toByteArray();
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
