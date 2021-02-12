package io.github.zabuzard.fastcdc4j.internal.chunking;

import io.github.zabuzard.fastcdc4j.external.chunking.Chunk;
import io.github.zabuzard.fastcdc4j.internal.util.Util;
import io.github.zabuzard.fastcdc4j.internal.util.Validations;
import io.github.zabuzard.fastcdc4j.external.chunking.Chunker;

import java.util.Objects;

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
	 * A binary hash representation of the contained data. Using the algorithm specified during construction by the
	 * {@link Chunker}.
	 */
	private final byte[] hash;
	/**
	 * A hexadecimal hash representation of the contained data. Using the algorithm specified during construction by the
	 * {@link Chunker}.
	 */
	private final String hexHash;
	/**
	 * The offset of this chunk, with respect to its source data stream.
	 */
	private final long offset;

	/**
	 * Creates a new simple chunk.
	 * <p>
	 * The {@link #getHexHash()} is cached and will be generated upon construction based on the given hash.
	 *
	 * @param data   The data contained in this chunk, not null and not empty
	 * @param offset The offset of this chunk, with respect to its source data stream, must be positive
	 * @param hash   A binary hash representation of the contained data. Using the algorithm specified during
	 *               construction by the {@link Chunker}. Not null and not empty.
	 */
	public SimpleChunk(final byte[] data, final long offset, final byte[] hash) {
		Objects.requireNonNull(data);
		Validations.require(data.length > 0, "Data must not be empty");
		Objects.requireNonNull(hash);
		Validations.require(hash.length > 0, "Hash must not be empty");
		//noinspection AssignmentOrReturnOfFieldWithMutableType
		this.data = data;
		this.offset = Validations.requirePositive(offset, "Offset");
		//noinspection AssignmentOrReturnOfFieldWithMutableType
		this.hash = hash;
		hexHash = Util.bytesToHex(hash);
	}

	@Override
	public byte[] getData() {
		//noinspection AssignmentOrReturnOfFieldWithMutableType
		return data;
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

	@Override
	public int getLength() {
		return data.length;
	}

	@Override
	public long getOffset() {
		return offset;
	}
}
