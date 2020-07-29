package de.zabuza.fastcdc4j.internal.chunking;

import de.zabuza.fastcdc4j.external.chunking.Chunk;
import de.zabuza.fastcdc4j.internal.util.Util;

/**
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class SimpleChunk implements Chunk {
	private final byte[] data;
	private final long offset;
	private final byte[] hash;
	private final String hexHash;

	public SimpleChunk(final byte[] data, final long offset, final byte[] hash) {
		this.data = data;
		this.offset = offset;
		this.hash = hash;
		hexHash = Util.bytesToHex(hash);
	}

	@Override
	public byte[] getData() {
		return data;
	}

	@Override
	public long getOffset() {
		return offset;
	}

	@Override
	public int getLength() {
		return data.length;
	}

	@Override
	public byte[] getHash() {
		return hash;
	}

	@Override
	public String getHexHash() {
		return hexHash;
	}
}
