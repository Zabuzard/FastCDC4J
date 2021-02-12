package io.github.zabuzard.fastcdc4j.internal.chunking;

import io.github.zabuzard.fastcdc4j.external.chunking.IterativeStreamChunkerCore;
import io.github.zabuzard.fastcdc4j.internal.util.Validations;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * Implementation of an iterative stream chunker core that splits data into chunks of equal size, known as {@code
 * Fixed-Size-Chunking}.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class FixedSizeChunkerCore implements IterativeStreamChunkerCore {
	/**
	 * The fixed chunk size to use for splitting.
	 */
	private final int chunkSize;

	/**
	 * Creates a new core.
	 *
	 * @param chunkSize The fixed chunk size to use for splitting, must be positive and not zero
	 */
	public FixedSizeChunkerCore(final int chunkSize) {
		this.chunkSize = Validations.requirePositiveNonZero(chunkSize, "Chunk size");
	}

	@Override
	public byte[] readNextChunk(final InputStream stream, final long size, final long currentOffset) {
		Objects.requireNonNull(stream);
		Validations.requirePositiveNonZero(size, "Size");
		Validations.requirePositive(currentOffset, "Current offset");
		Validations.require(currentOffset < size, "Current offset must be less than size");

		// Read up to CHUNK_SIZE many bytes
		//noinspection NumericCastThatLosesPrecision
		final int length = currentOffset + chunkSize <= size ? chunkSize : (int) (size - currentOffset);

		try {
			return stream.readNBytes(length);
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
