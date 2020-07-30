package de.zabuza.fastcdc4j.external.chunking;

/**
 * Available predefined chunker algorithms.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public enum ChunkerOption {
	/**
	 * The original FastCDC algorithm.
	 */
	FAST_CDC,
	/**
	 * The modified FastCDC algorithm by Nathan Fiedlers Rust implementation.
	 */
	NLFIEDLER_RUST,
	/**
	 * The baseline algorithm chunking each x-th byte.
	 */
	FIXED_SIZE_CHUNKING
}
