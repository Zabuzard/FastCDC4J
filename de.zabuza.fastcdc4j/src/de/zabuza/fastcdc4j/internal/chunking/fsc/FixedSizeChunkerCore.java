package de.zabuza.fastcdc4j.internal.chunking.fsc;

import de.zabuza.fastcdc4j.external.chunking.IterativeStreamChunkerCore;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public final class FixedSizeChunkerCore implements IterativeStreamChunkerCore {
	// TODO Make configurable
	private static final int CHUNK_SIZE = 1 * 1024 * 1024;

	@Override
	public byte[] readNextChunk(final InputStream stream, final long size, final long currentOffset) {
		// Read up to CHUNK_SIZE many bytes
		int length = currentOffset + CHUNK_SIZE <= size ? CHUNK_SIZE : (int) (size - currentOffset);

		try {
			return stream.readNBytes(length);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
