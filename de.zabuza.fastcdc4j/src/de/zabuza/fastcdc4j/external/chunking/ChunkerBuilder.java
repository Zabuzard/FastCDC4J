package de.zabuza.fastcdc4j.external.chunking;

import de.zabuza.fastcdc4j.internal.chunking.fsc.FixedSizeChunker;

/**
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class ChunkerBuilder {
	// TODO Choose appropriate algorithm
	private ChunkerAlgorithm algorithm = ChunkerAlgorithm.FIXED_SIZE_CHUNKING;

	public ChunkerBuilder setAlgorithm(final ChunkerAlgorithm algorithm) {
		this.algorithm = algorithm;
		return this;
	}

	public Chunker build() {
		// TODO Implement
		return switch (algorithm) {
			case FAST_CDC -> null;
			case FIXED_SIZE_CHUNKING -> new FixedSizeChunker();
			default -> throw new AssertionError();
		};
	}
}
