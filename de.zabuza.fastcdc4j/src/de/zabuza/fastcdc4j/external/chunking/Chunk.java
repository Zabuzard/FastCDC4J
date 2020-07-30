package de.zabuza.fastcdc4j.external.chunking;

/**
 * Interface representing chunked data as created by a {@link Chunker}.
 * <p>
 * Chunks own their data, hence it is preferable to keep their lifetime short and collect necessary information as soon
 * as possible.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public interface Chunk {
	/**
	 * Gets the data contained in this chunk.
	 *
	 * @return The contained data
	 */
	byte[] getData();

	/**
	 * Gets the offset of this chunk, with respect to its source data stream.
	 *
	 * @return The offset
	 */
	long getOffset();

	/**
	 * The length of this chunk, i.e. the amount of contained data.
	 *
	 * @return Gets the length
	 */
	int getLength();

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
}
