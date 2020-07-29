package de.zabuza.fastcdc4j.external.chunking;

import java.io.InputStream;

@FunctionalInterface
public interface IterativeStreamChunkerCore {
	byte[] readNextChunk(InputStream stream, long size, long currentOffset);
}
