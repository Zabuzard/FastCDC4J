package de.zabuza.fastcdc4j.external.chunking;

import de.zabuza.fastcdc4j.internal.chunking.*;

/**
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class ChunkerBuilder {
	private ChunkerOption chunkerOption = ChunkerOption.FAST_CDC;
	private Chunker chunker;
	private IterativeStreamChunkerCore core;
	private String hashMethod = "SHA-1";
	private int expectedChunkSize = 8 * 1_024;
	private HashTableOption hashTableOption = HashTableOption.RTPAL;
	private long[] hashTable;

	public Chunker build() {
		if (chunker != null) {
			return chunker;
		}

		long[] hashTableToUse = hashTable != null ? hashTable : switch (hashTableOption) {
			case RTPAL -> HashTables.getRtpal();
			case NLFIEDLER_RUST -> HashTables.getNlfiedlerRust();
		};

		IterativeStreamChunkerCore coreToUse = core != null ? core : switch (chunkerOption) {
			case FAST_CDC -> new FastCdcChunkerCore(expectedChunkSize, hashTableToUse);
			case NLFIEDLER_RUST -> new NlfiedlerRustChunkerCore(expectedChunkSize, hashTableToUse);
			case FIXED_SIZE_CHUNKING -> new FixedSizeChunkerCore(expectedChunkSize);
		};
		return new IterativeStreamChunker(coreToUse, hashMethod);
	}

	public ChunkerBuilder setChunkerOption(final ChunkerOption chunkerOption) {
		this.chunkerOption = chunkerOption;
		return this;
	}

	public ChunkerBuilder setChunker(final Chunker chunker) {
		this.chunker = chunker;
		return this;
	}

	public ChunkerBuilder setCore(final IterativeStreamChunkerCore core) {
		this.core = core;
		return this;
	}

	public ChunkerBuilder setHashMethod(final String hashMethod) {
		this.hashMethod = hashMethod;
		return this;
	}

	public ChunkerBuilder setExpectedChunkSize(final int expectedChunkSize) {
		this.expectedChunkSize = expectedChunkSize;
		return this;
	}

	public ChunkerBuilder setHashTableOption(final HashTableOption hashTableOption) {
		this.hashTableOption = hashTableOption;
		return this;
	}

	public ChunkerBuilder setHashTable(final long[] hashTable) {
		this.hashTable = hashTable;
		return this;
	}
}
