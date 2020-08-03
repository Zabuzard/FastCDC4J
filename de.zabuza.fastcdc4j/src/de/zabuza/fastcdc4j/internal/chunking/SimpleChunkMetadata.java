package de.zabuza.fastcdc4j.internal.chunking;

import de.zabuza.fastcdc4j.external.chunking.ChunkMetadata;

/**
 * Implementation of a simple chunk metadata, wrapping given data.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class SimpleChunkMetadata implements ChunkMetadata {
	/**
	 * The offset of this chunk, with respect to its source data stream.
	 */
	private final long offset;
	/**
	 * The length of this chunk, i.e. the amount of contained data.
	 */
	private final int length;
	/**
	 * A binary hash representation of the contained data. Using the algorithm specified during construction by the
	 * {@link de.zabuza.fastcdc4j.external.chunking.Chunker}.
	 */
	private final byte[] hash;
	/**
	 * A hexadecimal hash representation of the contained data. Using the algorithm specified during construction by the
	 * {@link de.zabuza.fastcdc4j.external.chunking.Chunker}.
	 */
	private final String hexHash;

	/**
	 * Creates a new simple chunk.
	 *
	 * @param offset  The offset of this chunk, with respect to its source data stream
	 * @param length  The length of this chunk, i.e. the amount of contained data
	 * @param hash    A binary hash representation of the contained data. Using the algorithm specified during
	 *                construction by the {@link de.zabuza.fastcdc4j.external.chunking.Chunker}.
	 * @param hexHash A hexadecimal hash representation of the contained data. Using the algorithm specified during
	 *                construction by the {@link de.zabuza.fastcdc4j.external.chunking.Chunker}.
	 */
	public SimpleChunkMetadata(final long offset, final int length, final byte[] hash, final String hexHash) {
		this.offset = offset;
		this.length = length;
		//noinspection AssignmentOrReturnOfFieldWithMutableType
		this.hash = hash;
		this.hexHash = hexHash;
	}

	@Override
	public long getOffset() {
		return offset;
	}

	@Override
	public int getLength() {
		return length;
	}

	@Override
	public byte[] getHash() {
		//noinspection AssignmentOrReturnOfFieldWithMutableType
		return hash;
	}

	@Override
	public String getHexHash() {
		return hexHash;
	}
}
