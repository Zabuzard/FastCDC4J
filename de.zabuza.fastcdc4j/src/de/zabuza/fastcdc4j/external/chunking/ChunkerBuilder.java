package de.zabuza.fastcdc4j.external.chunking;

import de.zabuza.fastcdc4j.internal.chunking.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Builder for convenient construction of {@link Chunker} instances.
 * <p>
 * The builder offers highly customizable content-defined-chunking algorithms. Offered algorithms are:
 * <ul>
 *     <li>{@code FastCDC (original)} - Wen Xia et al. (<a href="https://www.usenix.org/system/files/conference/atc16/atc16-paper-xia.pdf">publication</a>)</li>
 *     <li>{@code FastCDC Rust} - Nathan Fiedler (<a href="https://github.com/nlfiedler/fastcdc-rs">source</a>),slightly modified version of the original algorithm</li>
 *     <li>{@code Fixed-Size-Chunking (FSC)} - Baseline, chunks the data stream every x-th byte, without interpreting the content</li>
 * </ul>
 * It is also possible to add custom algorithms by simply implementing {@link Chunker}.
 * A custom algorithm can be set by using {@link #setChunker(Chunker)} for full control or {@link #setChunkerCore(IterativeStreamChunkerCore)}
 * for a simplified interface.
 * {@link #setChunkerOption(ChunkerOption)} can be used to choose from the predefined algorithms.
 * <p>
 * The algorithms will try to strive for an expected chunk size given by {@link #setExpectedChunkSize(int)}.
 * <p>
 * Most of the algorithms internally use a hash table as source for predicted noise to steer the algorithm, a custom
 * table can be provided by {@link #setHashTable(long[])}.
 * Alternatively, {@link #setHashTableOption(HashTableOption)} can be used to choose from predefined tables.
 * <p>
 * After a chunk has been read, a hash is generated based on its content. The algorithm used for this process can be
 * set by {@link #setHashMethod(String)}, it has to be supported and accepted by {@link java.security.MessageDigest}.
 * <p>
 * Finally, a chunker using the selected properties can be created using {@link #build()}.
 * <p>
 * The <b>default configuration</b> of the builder is:
 * <ul>
 *     <li>{@link ChunkerOption#FAST_CDC}</li>
 *     <li>{@code 8 * 1024}</li>
 *     <li>{@link HashTableOption#RTPAL}</li>
 *     <li>{@code SHA-1}</li>
 * </ul>
 * The methods {@link #fastCdc()}, {@link #nlFiedlerRust()} and {@link #fsc()} can be used to get a configuration
 * that uses the given algorithms as originally proposed.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class ChunkerBuilder {
	/**
	 * The default hash method to use by all chunkers.
	 */
	private static final String DEFAULT_HASH_METHOD = "SHA-1";
	/**
	 * The default expected size of chunks, in bytes, used by all chunkers.
	 */
	@SuppressWarnings("MultiplyOrDivideByPowerOfTwo")
	private static final int DEFAULT_EXPECTED_CHUNK_SIZE = 8 * 1_024;

	/**
	 * The chunker option to use.
	 */
	private ChunkerOption chunkerOption = ChunkerOption.FAST_CDC;
	/**
	 * The chunker to use. Has priority over {@link #chunkerCore} and {@link #chunkerOption}.
	 */
	private Chunker chunker;
	/**
	 * The core to use for an iterative stream chunker. Has priority over {@link #chunkerOption}.
	 */
	private IterativeStreamChunkerCore chunkerCore;
	/**
	 * The hash method to use for representing the data of chunks.
	 */
	private String hashMethod = ChunkerBuilder.DEFAULT_HASH_METHOD;
	/**
	 * The expected size of chunks, in bytes.
	 */
	private int expectedChunkSize = ChunkerBuilder.DEFAULT_EXPECTED_CHUNK_SIZE;
	/**
	 * The option to use for the hash table used by the chunker algorithm.
	 */
	private HashTableOption hashTableOption = HashTableOption.RTPAL;
	/**
	 * The hash table to use by the chunker algorithm. Has priority over {@link #hashTableOption}.
	 */
	private long[] hashTable;

	/**
	 * Builds a chunker using the set properties.
	 *
	 * @return A chunker using the set properties
	 */
	public Chunker build() {
		if (chunker != null) {
			return chunker;
		}

		final long[] hashTableToUse = hashTable != null ? hashTable : switch (hashTableOption) {
			case RTPAL -> HashTables.getRtpal();
			case NLFIEDLER_RUST -> HashTables.getNlfiedlerRust();
		};

		final IterativeStreamChunkerCore coreToUse = chunkerCore != null ? chunkerCore : switch (chunkerOption) {
			case FAST_CDC -> new FastCdcChunkerCore(expectedChunkSize, hashTableToUse);
			case NLFIEDLER_RUST -> new NlfiedlerRustChunkerCore(expectedChunkSize, hashTableToUse);
			case FIXED_SIZE_CHUNKING -> new FixedSizeChunkerCore(expectedChunkSize);
		};
		return new IterativeStreamChunker(coreToUse, hashMethod);
	}

	/**
	 * Sets the chunker option to use.
	 *
	 * @param chunkerOption The option to use
	 *
	 * @return This builder instance
	 */
	public ChunkerBuilder setChunkerOption(final ChunkerOption chunkerOption) {
		this.chunkerOption = chunkerOption;
		return this;
	}

	/**
	 * Sets the chunker to use. Has priority over {@link #setChunkerCore(IterativeStreamChunkerCore)} and {@link
	 * #setChunkerOption(ChunkerOption)}.
	 *
	 * @param chunker The chunker to use
	 *
	 * @return This builder instance
	 */
	public ChunkerBuilder setChunker(final Chunker chunker) {
		this.chunker = chunker;
		return this;
	}

	/**
	 * Sets the core to use for an iterative stream chunker. Has priority over {@link
	 * #setChunkerOption(ChunkerOption)}.
	 *
	 * @param chunkerCore The core to use
	 *
	 * @return This builder instance
	 */
	public ChunkerBuilder setChunkerCore(final IterativeStreamChunkerCore chunkerCore) {
		this.chunkerCore = chunkerCore;
		return this;
	}

	/**
	 * Sets the hash method to use for representing the data of chunks.
	 *
	 * @param hashMethod The hash method to use, has to be accepted and supported by {@link
	 *                   java.security.MessageDigest}.
	 *
	 * @return This builder instance
	 */
	public ChunkerBuilder setHashMethod(final String hashMethod) {
		try {
			MessageDigest.getInstance(hashMethod);
		} catch (final NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("The given hash method is not supported, was: " + hashMethod, e);
		}
		this.hashMethod = hashMethod;
		return this;
	}

	/**
	 * Sets the expected size of chunks, in bytes.
	 *
	 * @param expectedChunkSize The expected size of chunks, in bytes. Must be positive.
	 *
	 * @return This builder instance
	 */
	public ChunkerBuilder setExpectedChunkSize(final int expectedChunkSize) {
		if (expectedChunkSize < 0) {
			throw new IllegalArgumentException("Expected chunk size must be positive, was: " + expectedChunkSize);
		}
		this.expectedChunkSize = expectedChunkSize;
		return this;
	}

	/**
	 * Sets the option to use for the hash table used by the chunker algorithm.
	 *
	 * @param hashTableOption The option to use for the hash table
	 *
	 * @return This builder instance
	 */
	public ChunkerBuilder setHashTableOption(final HashTableOption hashTableOption) {
		this.hashTableOption = hashTableOption;
		return this;
	}

	/**
	 * Sets the hash table to use by the chunker algorithm. Has priority over {@link
	 * #setHashTableOption(HashTableOption)}.
	 *
	 * @param hashTable The hash table to use. Must have a length of exactly 256, one hash per byte value.
	 *
	 * @return This builder instance
	 */
	public ChunkerBuilder setHashTable(final long[] hashTable) {
		//noinspection MagicNumber
		if (hashTable.length != 256) {
			throw new IllegalArgumentException(
					"Hash table must have a length of 256, one hash per byte value, was: " + hashTable.length);
		}
		this.hashTable = hashTable.clone();
		return this;
	}

	/**
	 * Sets the builder to a configuration for the original FastCDC algorithm.
	 *
	 * @return This builder instance
	 */
	public ChunkerBuilder fastCdc() {
		chunkerOption = ChunkerOption.FAST_CDC;
		hashMethod = ChunkerBuilder.DEFAULT_HASH_METHOD;
		expectedChunkSize = ChunkerBuilder.DEFAULT_EXPECTED_CHUNK_SIZE;
		hashTableOption = HashTableOption.RTPAL;
		return this;
	}

	/**
	 * Sets the builder to a configuration for the modified FastCDC algorithm of Nathan Fiedlers Rust implementation.
	 *
	 * @return This builder instance
	 */
	public ChunkerBuilder nlFiedlerRust() {
		chunkerOption = ChunkerOption.NLFIEDLER_RUST;
		hashMethod = ChunkerBuilder.DEFAULT_HASH_METHOD;
		expectedChunkSize = ChunkerBuilder.DEFAULT_EXPECTED_CHUNK_SIZE;
		hashTableOption = HashTableOption.NLFIEDLER_RUST;
		return this;
	}

	/**
	 * Sets the builder to a configuration for the baseline Fixed-Size-Chunking algorithm.
	 *
	 * @return This builder instance
	 */
	public ChunkerBuilder fsc() {
		chunkerOption = ChunkerOption.FIXED_SIZE_CHUNKING;
		hashMethod = ChunkerBuilder.DEFAULT_HASH_METHOD;
		expectedChunkSize = ChunkerBuilder.DEFAULT_EXPECTED_CHUNK_SIZE;
		return this;
	}
}
