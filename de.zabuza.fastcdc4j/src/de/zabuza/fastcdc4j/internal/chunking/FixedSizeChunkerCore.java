package de.zabuza.fastcdc4j.internal.chunking;

import de.zabuza.fastcdc4j.external.chunking.IterativeStreamChunkerCore;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public final class FixedSizeChunkerCore implements IterativeStreamChunkerCore {
	private final int chunkSize;

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
