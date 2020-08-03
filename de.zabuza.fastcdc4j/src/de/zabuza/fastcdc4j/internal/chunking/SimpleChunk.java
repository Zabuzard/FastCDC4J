package de.zabuza.fastcdc4j.internal.chunking;

import de.zabuza.fastcdc4j.external.chunking.Chunk;
import de.zabuza.fastcdc4j.internal.util.Util;

/**
 * Implementation of a simple chunk, wrapping given data.
 * <p>
 * The {@link #getHexHash()} is cached and will be generated upon construction.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class SimpleChunk implements Chunk {
	/**
	 * The data contained in this chunk.
	 */
	private final byte[] data;
	/**
	 * The offset of this chunk, with respect to its source data stream.
	 */
	private final long offset;
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
	 * <p>
	 * The {@link #getHexHash()} is cached and will be generated upon construction based on the given hash.
	 *
	 * @param data   The data contained in this chunk
	 * @param offset The offset of this chunk, with respect to its source data stream
	 * @param hash   A binary hash representation of the contained data. Using the algorithm specified during
	 *               construction by the {@link de.zabuza.fastcdc4j.external.chunking.Chunker}.
	 */
	public SimpleChunk(final byte[] data, final long offset, final byte[] hash) {
		this.data = data;
		this.offset = offset;
		this.hash = hash;
		hexHash = Util.bytesToHex(hash);
	}

	@Override
	public byte[] getData() {
		return data;
	}

	@Override
	public long getOffset() {
		return offset;
	}

	@Override
	public int getLength() {
		return data.length;
	}

	@Override
	public byte[] getHash() {
		return hash;
	}

	@Override
	public String getHexHash() {
		return hexHash;
	}
}
