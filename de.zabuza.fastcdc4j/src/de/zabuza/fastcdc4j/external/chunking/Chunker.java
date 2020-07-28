package de.zabuza.fastcdc4j.external.chunking;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public interface Chunker {
	Iterable<Chunk> chunk(InputStream stream, long size);

	default Iterable<Chunk> chunk(Stream<Path> path) {
		// TODO Implement
		return null;
	}

	default Iterable<Chunk> chunk(final byte[] data) {
		return chunk(new ByteArrayInputStream(data), data.length);
	}

	default Iterable<Chunk> chunk(final Path path) {
		try {
			if (Files.isDirectory(path)) {
				return chunk(Files.walk(path));
			}
			if (Files.isRegularFile(path)) {
				return chunk(Files.newInputStream(path), Files.size(path));
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		throw new IllegalArgumentException("Only regular file or directory are supported");
	}
}
