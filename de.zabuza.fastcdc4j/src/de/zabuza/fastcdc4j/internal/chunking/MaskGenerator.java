package de.zabuza.fastcdc4j.internal.chunking;

import de.zabuza.fastcdc4j.external.chunking.MaskOption;
import de.zabuza.fastcdc4j.internal.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MaskGenerator {
	private static final int MASK_SIZE_TOTAL_FAST_CDC = 48;

	private static long generateMaskFastCdc(int effectiveBits) {
		// Shuffle a mask with 'effectiveBits' 1s and fill up the rest with '0'
		// The most significant bit has to be 1 always, hence we only shuffle the rest
		List<Integer> maskBits = new ArrayList<>();
		int i = 0;
		while (i < effectiveBits - 1) {
			maskBits.add(1);
			i++;
		}
		while (i < MaskGenerator.MASK_SIZE_TOTAL_FAST_CDC - 1) {
			maskBits.add(0);
			i++;
		}
		// TODO Use seeded random
		Collections.shuffle(maskBits, new Random());

		String mask = Stream.concat(Stream.of(1), maskBits.stream())
				.map(Object::toString)
				.collect(Collectors.joining());

		return Long.parseLong(mask, 2);
	}

	private static long generateMaskNlfiedlerRust(int bits) {
		return Long.parseLong("1".repeat(bits), 2);
	}

	private static int getEffectiveBits(int expectedChunkSize) {
		return Util.log2(expectedChunkSize);
	}

	private final int expectedChunkSize;
	private final MaskOption maskOption;
	private final int normalizationLevel;

	public MaskGenerator(final MaskOption maskOption, final int normalizationLevel, final int expectedChunkSize) {
		this.maskOption = maskOption;
		this.normalizationLevel = normalizationLevel;
		this.expectedChunkSize = expectedChunkSize;
	}

	public long generateLargeMask() {
		return generateMask(normalizationLevel);
	}

	public long generateSmallMask() {
		return generateMask(-normalizationLevel);
	}

	private long generateMask(int effectiveBitOffset) {
		// TODO Implement
		int effectiveBits = getEffectiveBits(expectedChunkSize) + effectiveBitOffset;
		return switch (maskOption) {
			case FAST_CDC -> generateMaskFastCdc(effectiveBits);
			case NLFIEDLER_RUST -> generateMaskNlfiedlerRust(effectiveBits);
		};
	}
}
