package de.zabuza.fastcdc4j.internal.chunking;

import de.zabuza.fastcdc4j.external.chunking.IterativeStreamChunkerCore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public final class FastCdcChunkerCore implements IterativeStreamChunkerCore {
	private static final long MASK_L = 0b1101_10010_00000_00000_00011_0101_0011_0000_0000_0000_0000L;
			// TODO Make dependent on given expected size
	private static final long MASK_S = 0b11_0101_1001_0000_0111_0000_0011_0101_0011_0000_0000_0000_0000L;
			// TODO Make dependent on given expected size
	private static final int MAX_SIZE = 64 * 1_024; // TODO Make dependent on given expected size
	private static final int MIN_SIZE = 2 * 1_024; // TODO Make dependent on given expected size

	private final int expectedSize;
	private final long[] gear;

	public FastCdcChunkerCore(int expectedSize, long[] gear) {
		this.expectedSize = expectedSize;
		this.gear = gear;
	}

	@Override
	public byte[] readNextChunk(final InputStream stream, final long size, final long currentOffset) {
		try {
			int normalSize = expectedSize;
			long n = size - currentOffset;
			if (n <= 0) {
				throw new IllegalArgumentException();
			}
			if (n <= MIN_SIZE) {
				return stream.readNBytes((int) n);
			}
			if (n >= MAX_SIZE) {
				n = MAX_SIZE;
			} else if (n <= normalSize) {
				normalSize = (int) n;
			}

			ByteArrayOutputStream dataBuffer = new ByteArrayOutputStream();
			long fingerprint = 0;
			int i = MIN_SIZE;
			dataBuffer.write(stream.readNBytes(i));

			for (; i < normalSize; i++) {
				int data = stream.read();
				if (data == -1) {
					throw new IllegalStateException();
				}
				dataBuffer.write(data);
				fingerprint = (fingerprint << 1) + gear[data];
				if ((fingerprint & MASK_S) == 0) {
					return dataBuffer.toByteArray();
				}
			}
			for (; i < n; i++) {
				int data = stream.read();
				if (data == -1) {
					throw new IllegalStateException();
				}
				dataBuffer.write(data);
				fingerprint = (fingerprint << 1) + gear[data];
				if ((fingerprint & MASK_L) == 0) {
					return dataBuffer.toByteArray();
				}
			}

			return dataBuffer.toByteArray();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
