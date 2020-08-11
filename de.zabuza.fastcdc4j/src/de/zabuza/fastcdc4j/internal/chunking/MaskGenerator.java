package de.zabuza.fastcdc4j.internal.chunking;

import de.zabuza.fastcdc4j.external.chunking.MaskOption;
import de.zabuza.fastcdc4j.internal.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for generating masks that are used by {@link de.zabuza.fastcdc4j.external.chunking.Chunker}s.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class MaskGenerator {
	/**
	 * The total size of the masks used by FastCDC.
	 */
	private static final int MASK_SIZE_TOTAL_FAST_CDC = 48;

	/**
	 * Generates a mask using the techniques described in FastCDC.
	 *
	 * @param effectiveBits The amount of effective bits in the mask (1s)
	 * @param seed          The seed to use for distribution of the bits
	 *
	 * @return The generated mask
	 */
	private static long generateMaskFastCdc(final int effectiveBits, final long seed) {
		// Shuffle a mask with 'effectiveBits' 1s and fill up the rest with '0'
		// The most significant bit has to be 1 always, hence we only shuffle the rest
		final List<Integer> maskBits = new ArrayList<>();
		int i = 0;
		while (i < effectiveBits - 1) {
			maskBits.add(1);
			i++;
		}
		while (i < MaskGenerator.MASK_SIZE_TOTAL_FAST_CDC - 1) {
			maskBits.add(0);
			i++;
		}
		Collections.shuffle(maskBits, new Random(seed));

		final String mask = Stream.concat(Stream.of(1), maskBits.stream())
				.map(Object::toString)
				.collect(Collectors.joining());

		return Long.parseLong(mask, 2);
	}

	/**
	 * Generates a mask using the techniques described in NlfiedlerRust.
	 *
	 * @param bits The amount of effective bits in the mask (1s)
	 *
	 * @return The generated mask
	 */
	private static long generateMaskNlfiedlerRust(final int bits) {
		return Long.parseLong("1".repeat(bits), 2);
	}

	/**
	 * Gets the amount of effective bits to use (1s) for the given expected chunk size.
	 *
	 * @param expectedChunkSize The expected chunk size in bytes
	 *
	 * @return The amount of effective bits to use
	 */
	private static int getEffectiveBits(final int expectedChunkSize) {
		return Util.log2(expectedChunkSize);
	}

	/**
	 * The expected chunk size in bytes.
	 */
	private final int expectedChunkSize;
	/**
	 * The option describing which technique to use for mask generation.
	 */
	private final MaskOption maskOption;
	/**
	 * The normalization level to use.
	 */
	private final int normalizationLevel;
	/**
	 * The seed to use for distributing bits in the mask generation.
	 */
	private final long seed;

	/**
	 * Creates a new mask generator.
	 *
	 * @param maskOption         The option describing which technique to use for mask generation
	 * @param normalizationLevel The normalization level to use
	 * @param expectedChunkSize  The expected chunk size in bytes
	 * @param seed               The seed to use for distributing bits in the mask generation
	 */
	public MaskGenerator(final MaskOption maskOption, final int normalizationLevel, final int expectedChunkSize,
			final long seed) {
		this.maskOption = maskOption;
		this.normalizationLevel = normalizationLevel;
		this.expectedChunkSize = expectedChunkSize;
		this.seed = seed;
	}

	/**
	 * Generates a mask to be used for content larger than the expected chunk size, making chunking easier.
	 *
	 * @return The generated mask
	 */
	public long generateLargeMask() {
		return generateMask(normalizationLevel);
	}

	/**
	 * Generates a mask to be used for content smaller than the expected chunk size, making chunking harder.
	 *
	 * @return The generated mask
	 */
	public long generateSmallMask() {
		return generateMask(-normalizationLevel);
	}

	/**
	 * Generates a mask with the given offset for effective bits to the amount obtained by the expected chunk size.
	 *
	 * @param effectiveBitOffset The offset for the effective bits
	 *
	 * @return The generated mask
	 */
	private long generateMask(final int effectiveBitOffset) {
		final int effectiveBits = MaskGenerator.getEffectiveBits(expectedChunkSize) + effectiveBitOffset;
		return switch (maskOption) {
			case FAST_CDC -> MaskGenerator.generateMaskFastCdc(effectiveBits, seed);
			case NLFIEDLER_RUST -> MaskGenerator.generateMaskNlfiedlerRust(effectiveBits);
		};
	}
}
