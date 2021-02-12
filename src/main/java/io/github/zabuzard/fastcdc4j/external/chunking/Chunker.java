package io.github.zabuzard.fastcdc4j.external.chunking;

import io.github.zabuzard.fastcdc4j.internal.util.FlatIterator;
import io.github.zabuzard.fastcdc4j.internal.util.Validations;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Interface for algorithms that are able to chunk data streams for data deduplication.
 * <p>
 * Use {@link ChunkerBuilder} for convenient construction of instances.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
@SuppressWarnings("InterfaceMayBeAnnotatedFunctional")
public interface Chunker {
	/**
	 * Chunks the given stream into chunks. The stream is consumed and populates the resulting iterable lazily as it is
	 * consumed.
	 * <p>
	 * Chunks own their bytes, so it is preferable to process them directly and avoid first collecting all of them.
	 *
	 * @param stream The data stream to chunk, not null
	 * @param size   The amount of bytes available in the stream that are subject to be chunked, the stream must offer
	 *               at least that many bytes. Must be positive and not zero.
	 *
	 * @return The chunks of the stream, lazily populated
	 */
	Iterable<Chunk> chunk(InputStream stream, long size);

	/**
	 * Chunks all given regular files into chunks. The stream is consumed and populates the resulting iterable lazily as
	 * it is consumed.
	 * <p>
	 * Chunks own their bytes, so it is preferable to process them directly and avoid first collecting all of them.
	 * <p>
	 * The stream is consumed sequential, files are not processed parallel.
	 *
	 * @param paths Stream of files to process, only regular files are chunked, not null
	 *
	 * @return The chunks of the stream, lazily populated
	 */
	default Iterable<Chunk> chunk(final Stream<? extends Path> paths) {
		Objects.requireNonNull(paths);
		return () -> new FlatIterator<>(paths.filter(Files::isRegularFile)
				.iterator(), path -> chunk(path).iterator());
	}

	/**
	 * Chunks the given data into chunks. The data is consumed and populates the resulting iterable lazily as it is
	 * consumed.
	 *
	 * @param data The data to chunk, not null and not empty
	 *
	 * @return The chunks of the stream, lazily populated
	 */
	default Iterable<Chunk> chunk(final byte[] data) {
		Objects.requireNonNull(data);
		Validations.require(data.length > 0, "Data must not be empty");
		return chunk(new ByteArrayInputStream(data), data.length);
	}

	/**
	 * Chunks the data available at the given path. The path must either be a regular file or a directory. In case of a
	 * directory, the method recursively traverses the directory and lazily collects all regular files.
	 * <p>
	 * The stream is consumed and populates the resulting iterable lazily as it is consumed.
	 * <p>
	 * Chunks own their bytes, so it is preferable to process them directly and avoid first collecting all of them.
	 * <p>
	 * The stream is consumed sequential, files are not processed parallel.
	 *
	 * @param path Either a regular file or a directory to traverse, only regular files are processed, not null
	 *
	 * @return The chunks of the stream, lazily populated
	 */
	default Iterable<Chunk> chunk(final Path path) {
		Objects.requireNonNull(path);
		try {
			if (Files.isDirectory(path)) {
				return chunk(Files.walk(path));
			}
			if (Files.isRegularFile(path)) {
				return chunk(new BufferedInputStream(Files.newInputStream(path)), Files.size(path));
			}
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
		throw new IllegalArgumentException("Only existing regular files or directories are supported");
	}
}
