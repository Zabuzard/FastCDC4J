package de.zabuza.fastcdc4j.internal.chunking.fsc;

import de.zabuza.fastcdc4j.external.chunking.Chunk;
import de.zabuza.fastcdc4j.external.chunking.Chunker;
import de.zabuza.fastcdc4j.internal.chunking.SimpleChunk;
import de.zabuza.fastcdc4j.internal.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Iterator;

/**
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class FixedSizeChunker implements Chunker {
	// TODO Make configurable
	private static final int CHUNK_SIZE = 1 * 1024 * 1024;

	@Override
	public Iterable<Chunk> chunk(final InputStream stream, long size) {
		return new ChunkerIterable(stream, size);
	}

	private static class ChunkerIterable implements Iterable<Chunk>, Iterator<Chunk> {
		private final InputStream stream;
		private final long size;
		private long currentOffset;

		public ChunkerIterable(InputStream stream, long size) {
			this.stream = stream;
			this.size = size;
		}

		@Override
		public Iterator<Chunk> iterator() {
			return this;
		}

		@Override
		public boolean hasNext() {
			return currentOffset < size;
		}

		@Override
		public Chunk next() {
			// Read up to CHUNK_SIZE many bytes
			int length = currentOffset + CHUNK_SIZE <= size ? CHUNK_SIZE : (int) (size - currentOffset);

			byte[] data;
			try {
				data = stream.readNBytes(length);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}

			// TODO Make hash method configurable
			Chunk chunk = new SimpleChunk(data, currentOffset, Util.hashSha1(data));

			currentOffset += length;
			return chunk;
		}
	}
}
