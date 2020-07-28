package de.zabuza.fastcdc4j.external.chunking;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public interface Chunker {
	Iterable<Chunk> chunk(Path path) throws IOException;

	Iterable<Chunk> chunk(Stream<Path> path) throws IOException;

	Iterable<Chunk> chunk(InputStream stream) throws IOException;

	Iterable<Chunk> chunk(byte[] data);
}
