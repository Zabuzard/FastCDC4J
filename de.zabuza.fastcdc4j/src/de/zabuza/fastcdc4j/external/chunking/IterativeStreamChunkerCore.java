package de.zabuza.fastcdc4j.external.chunking;

import java.io.InputStream;

/**
 * Interface for the core algorithm used by a chunker that iteratively processes the stream.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
@FunctionalInterface
public interface IterativeStreamChunkerCore {
	/**
	 * Reads the next chunk from the given data stream. The stream is consumed by exactly the length of the provided
	 * chunk. The stream is safe to be read byte by byte, it provides buffering methods if necessary.
	 *
	 * @param stream        The data stream to chunk, not null
	 * @param size          Remaining data available in the stream that are subject to be chunked, must be positive and
	 *                      not zero
	 * @param currentOffset Current offset in the given stream, in regards to its original start, must be positive and
	 *                      less than size
	 *
	 * @return The chunk that was read
	 */
	byte[] readNextChunk(InputStream stream, long size, long currentOffset);
}
