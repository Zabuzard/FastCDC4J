package de.zabuza.fastcdc4j.internal.chunking;

import de.zabuza.fastcdc4j.external.chunking.Chunk;
import de.zabuza.fastcdc4j.external.chunking.Chunker;
import de.zabuza.fastcdc4j.external.chunking.IterativeStreamChunkerCore;
import de.zabuza.fastcdc4j.internal.util.Util;

import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

public final class IterativeStreamChunker implements Chunker {
	private final IterativeStreamChunkerCore core;

	public IterativeStreamChunker(IterativeStreamChunkerCore core) {
		this.core = core;
	}

	@Override
	public Iterable<Chunk> chunk(final InputStream stream, long size) {
		return () -> new ChunkerIterator(stream, size);
	}

	private class ChunkerIterator implements Iterator<Chunk> {
		private final long size;
		private final InputStream stream;
		private long currentOffset;

		public ChunkerIterator(InputStream stream, long size) {
			this.stream = stream;
			this.size = size;
		}

		@Override
		public boolean hasNext() {
			return currentOffset < size;
		}

		@Override
		public Chunk next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			byte[] data = core.readNextChunk(stream, size, currentOffset);

			// TODO Make hash method configurable
			Chunk chunk = new SimpleChunk(data, currentOffset, Util.hashSha1(data));

			currentOffset += data.length;
			return chunk;
		}
	}
}
