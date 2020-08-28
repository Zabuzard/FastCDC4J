package de.zabuza.fastcdc4j.external.chunking;

/**
 * Interface representing metadata of a chunk as created by a {@link Chunker}.
 * <p>
 * Unlike a {@link Chunk}, metadata does not own their data.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public interface ChunkMetadata {
	/**
	 * A binary hash representation of the contained data. Using the algorithm specified during construction by the
	 * {@link Chunker}.
	 *
	 * @return A binary hash representation
	 */
	byte[] getHash();

	/**
	 * A hexadecimal hash representation of the contained data. Using the algorithm specified during construction by the
	 * {@link Chunker}.
	 *
	 * @return A hexadecimal hash representation
	 */
	String getHexHash();

	/**
	 * The length of this chunk, i.e. the amount of contained data.
	 *
	 * @return Gets the length
	 */
	int getLength();

	/**
	 * Gets the offset of this chunk, with respect to its source data stream.
	 *
	 * @return The offset
	 */
	long getOffset();
}
