package de.zabuza.fastcdc4j.internal.chunking;

import de.zabuza.fastcdc4j.external.chunking.Chunk;
import de.zabuza.fastcdc4j.external.chunking.Chunker;
import de.zabuza.fastcdc4j.external.chunking.IterativeStreamChunkerCore;
import de.zabuza.fastcdc4j.internal.util.Util;

import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implementation of a chunker that iteratively chunks the stream by using a given {@link IterativeStreamChunkerCore} as
 * core for the chunking behavior.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class IterativeStreamChunker implements Chunker {
	/**
	 * The core to use for chunking.
	 */
	private final IterativeStreamChunkerCore core;
	/**
	 * The method to use for hashing the data of a chunk.
	 */
	private final String hashMethod;

	/**
	 * Creates a new chunker.
	 *
	 * @param core       The core to use for chunking.
	 * @param hashMethod The hash method to use for hashing the data of a chunk, has to be supported and accepted by
	 *                   {@link java.security.MessageDigest}
	 */
	public IterativeStreamChunker(final IterativeStreamChunkerCore core, final String hashMethod) {
		this.core = core;
		this.hashMethod = hashMethod;
	}

	@Override
	public Iterable<Chunk> chunk(final InputStream stream, final long size) {
		return () -> new ChunkerIterator(stream, size, core, hashMethod);
	}

	/**
	 * Iterator that generates chunks on the fly, as requested. Using the given core for the chunking behavior.
	 *
	 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
	 */
	private static final class ChunkerIterator implements Iterator<Chunk> {
		/**
		 * The core to use for chunking.
		 */
		private final IterativeStreamChunkerCore core;
		/**
		 * The method to use for hashing the data of a chunk.
		 */
		private final String hashMethod;
		/**
		 * The amount of bytes available in the stream that are subject to be chunked.
		 */
		private final long size;
		/**
		 * The data stream to chunk.
		 */
		private final InputStream stream;
		/**
		 * The current offset in the data stream, marking the beginning of the next chunk.
		 */
		private long currentOffset;

		/**
		 * @param stream     The data stream to chunk
		 * @param size       The amount of bytes available in the stream that are subject to be chunked, the stream must
		 *                   offer at least that many bytes
		 * @param core       The core to use for chunking.
		 * @param hashMethod The hash method to use for hashing the data of a chunk, has to be supported and accepted by
		 *                   {@link java.security.MessageDigest}
		 */
		private ChunkerIterator(final InputStream stream, final long size, final IterativeStreamChunkerCore core,
				final String hashMethod) {
			this.stream = stream;
			this.size = size;
			this.core = core;
			this.hashMethod = hashMethod;
		}

		@Override
		public boolean hasNext() {
			return currentOffset < size;
		}

		@Override
		public Chunk next() {
			if (!hasNext()) {
				throw new NoSuchElementException("The data stream has ended, can not generate another chunk");
			}

			final byte[] data = core.readNextChunk(stream, size, currentOffset);

			final Chunk chunk = new SimpleChunk(data, currentOffset, Util.hash(hashMethod, data));

			currentOffset += data.length;
			return chunk;
		}
	}
}
