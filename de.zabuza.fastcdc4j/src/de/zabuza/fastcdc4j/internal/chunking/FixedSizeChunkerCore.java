package de.zabuza.fastcdc4j.internal.chunking;

import de.zabuza.fastcdc4j.external.chunking.IterativeStreamChunkerCore;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

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
	 * @param chunkSize The fixed chunk size to use for splitting
	 */
	public FixedSizeChunkerCore(int chunkSize) {
		this.chunkSize = chunkSize;
	}

	@Override
	public byte[] readNextChunk(final InputStream stream, final long size, final long currentOffset) {
		// Read up to CHUNK_SIZE many bytes
		int length = currentOffset + chunkSize <= size ? chunkSize : (int) (size - currentOffset);

		try {
			return stream.readNBytes(length);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
