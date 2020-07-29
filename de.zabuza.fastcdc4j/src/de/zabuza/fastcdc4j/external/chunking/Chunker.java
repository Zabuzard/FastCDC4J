package de.zabuza.fastcdc4j.external.chunking;

import de.zabuza.fastcdc4j.internal.util.FlatIterator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public interface Chunker {
	Iterable<Chunk> chunk(InputStream stream, long size);

	default Iterable<Chunk> chunk(Stream<Path> paths) {
		return () -> new FlatIterator<>(paths.filter(Files::isRegularFile)
				.iterator(), path -> chunk(path).iterator());
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
				return chunk(new BufferedInputStream(Files.newInputStream(path)), Files.size(path));
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		throw new IllegalArgumentException("Only existing regular files or directories are supported");
	}
}
