package de.zabuza.fastcdc4j.examples;

import de.zabuza.fastcdc4j.external.chunking.Chunk;
import de.zabuza.fastcdc4j.external.chunking.ChunkerBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Class offering a {@link #main(String[])} method that chunks a given build and populates a local chunk cache.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
@SuppressWarnings({ "UseOfSystemOutOrSystemErr", "ClassIndependentOfModule", "ClassOnlyUsedInOneModule" })
enum LocalChunkCache {
	;

	/**
	 * Starts the application.
	 *
	 * @param args Two arguments, the path to the build and the path to the local chunk cache
	 */
	public static void main(final String[] args) throws IOException {
		if (args.length != 2) {
			throw new IllegalArgumentException(
					"Expected two arguments buildPath and cachePath, where buildPath denotes the path to the build and cachePath the path to the local chunk cache.");
		}
		final Path buildPath = Path.of(args[0]);
		final Path cachePath = Path.of(args[1]);

		int cachedChunks = 0;
		int uncachedChunks = 0;

		final var chunker = new ChunkerBuilder().build();
		final var chunks = chunker.chunk(buildPath);
		for (final Chunk chunk : chunks) {
			final Path chunkPath = cachePath.resolve(chunk.getHexHash());
			if (Files.exists(chunkPath)) {
				cachedChunks++;
			} else {
				Files.write(chunkPath, chunk.getData());
				uncachedChunks++;
			}
		}

		System.out.printf("%d cached chunks, %d uncached chunks%n", cachedChunks, uncachedChunks);
	}
}
