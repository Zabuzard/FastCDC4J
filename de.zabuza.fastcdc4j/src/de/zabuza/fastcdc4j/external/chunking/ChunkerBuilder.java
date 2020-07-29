package de.zabuza.fastcdc4j.external.chunking;

import de.zabuza.fastcdc4j.internal.chunking.IterativeStreamChunker;
import de.zabuza.fastcdc4j.internal.chunking.fastcdc.FastCdcChunkerCore;
import de.zabuza.fastcdc4j.internal.chunking.fsc.FixedSizeChunkerCore;

/**
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class ChunkerBuilder {
	private ChunkerAlgorithm algorithm = ChunkerAlgorithm.FAST_CDC;
	private Chunker chunker;
	private IterativeStreamChunkerCore core;

	public Chunker build() {
		if (chunker != null) {
			return chunker;
		}

		IterativeStreamChunkerCore coreToUse = core != null ? core : switch (algorithm) {
			case FAST_CDC -> new FastCdcChunkerCore();
			case FIXED_SIZE_CHUNKING -> new FixedSizeChunkerCore();
			default -> throw new AssertionError();
		};
		return new IterativeStreamChunker(coreToUse);
	}

	public ChunkerBuilder setAlgorithm(final ChunkerAlgorithm algorithm) {
		this.algorithm = algorithm;
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
}
