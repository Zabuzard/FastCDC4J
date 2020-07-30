package de.zabuza.fastcdc4j.external.chunking;

/**
 * Available predefined hash tables used by chunker algorithms.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public enum HashTableOption {
	/**
	 * Table used by RTPal.
	 */
	RTPAL,
	/**
	 * Table used by the modified FastCDC algorithm by Nathan Fiedlers Rust implementation.
	 */
	NLFIEDLER_RUST
}
