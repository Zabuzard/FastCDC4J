package de.zabuza.fastcdc4j.external.chunking;

/**
 * Available predefined algorithms to generate masks used by certain {@link Chunker}s.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public enum MaskOption {
	/**
	 * The mask layout used in the original FastCDC algorithm.
	 */
	FAST_CDC,
	/**
	 * The mask layout used in the  modified FastCDC algorithm by Nathan Fiedlers Rust implementation.
	 */
	NLFIEDLER_RUST
}
