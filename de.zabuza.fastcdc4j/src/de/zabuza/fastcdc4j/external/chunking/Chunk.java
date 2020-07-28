package de.zabuza.fastcdc4j.external.chunking;

/**
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public interface Chunk {
	byte[] getData();

	int getOffset();

	int getLength();

	byte[] getHash();

	String getHexHash();
}
