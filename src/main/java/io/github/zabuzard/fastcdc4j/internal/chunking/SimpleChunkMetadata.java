package io.github.zabuzard.fastcdc4j.internal.chunking;

import io.github.zabuzard.fastcdc4j.external.chunking.ChunkMetadata;
import io.github.zabuzard.fastcdc4j.internal.util.Validations;
import io.github.zabuzard.fastcdc4j.external.chunking.Chunker;

import java.util.Objects;

/**
 * Implementation of a simple chunk metadata, wrapping given data.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class SimpleChunkMetadata implements ChunkMetadata {
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
	 * The length of this chunk, i.e. the amount of contained data.
	 */
	private final int length;
	/**
	 * The offset of this chunk, with respect to its source data stream.
	 */
	private final long offset;

	/**
	 * Creates a new simple chunk.
	 *
	 * @param offset  The offset of this chunk, with respect to its source data stream, must be positive
	 * @param length  The length of this chunk, i.e. the amount of contained data, must be positive and not zero
	 * @param hash    A binary hash representation of the contained data. Using the algorithm specified during
	 *                construction by the {@link Chunker}. Not null and not
	 *                empty.
	 * @param hexHash A hexadecimal hash representation of the contained data. Using the algorithm specified during
	 *                construction by the {@link Chunker}. Not null and not
	 *                empty.
	 */
	public SimpleChunkMetadata(final long offset, final int length, final byte[] hash, final String hexHash) {
		Objects.requireNonNull(hash);
		Validations.require(hash.length > 0, "Hash must not be empty");
		Objects.requireNonNull(hexHash);
		Validations.require(!hexHash.isEmpty(), "Hex hash must not be empty");
		this.offset = Validations.requirePositive(offset, "Offset");
		this.length = Validations.requirePositiveNonZero(length, "Length");
		//noinspection AssignmentOrReturnOfFieldWithMutableType
		this.hash = hash;
		this.hexHash = hexHash;
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
		return length;
	}

	@Override
	public long getOffset() {
		return offset;
	}
}
