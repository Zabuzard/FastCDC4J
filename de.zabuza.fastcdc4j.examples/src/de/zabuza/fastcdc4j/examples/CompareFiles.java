package de.zabuza.fastcdc4j.examples;

import de.zabuza.fastcdc4j.external.chunking.Chunk;
import de.zabuza.fastcdc4j.external.chunking.Chunker;
import de.zabuza.fastcdc4j.external.chunking.ChunkerBuilder;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class offering a {@link #main(String[])} method that compares two given files with each other and print statistics of
 * their chunks.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
@SuppressWarnings({ "UseOfSystemOutOrSystemErr", "ClassIndependentOfModule", "ClassOnlyUsedInOneModule" })
enum CompareFiles {
	;

	/**
	 * Starts the application.
	 *
	 * @param args Two arguments, the path to the first file and the path to the second file to compare.
	 */
	public static void main(final String[] args) {
		if (args.length != 2) {
			throw new IllegalArgumentException(
					"Expected two arguments first and second, where first denotes the path to the first file and second the path to the second file to compare.");
		}
		final Path first = Path.of(args[0]);
		final Path second = Path.of(args[1]);

		final Chunker chunker = new ChunkerBuilder().build();

		final Iterable<Chunk> chunks1 = chunker.chunk(first);
		final Iterable<Chunk> chunks2 = chunker.chunk(second);

		System.out.println("File1 stuff: ");
		final List<String> hashes1 = new ArrayList<>();
		for (final Chunk chunk : chunks1) {
			hashes1.add(chunk.getHexHash());
			System.out.printf("offset: %d\tlength: %d%n", chunk.getOffset(), chunk.getLength());
		}
		final Set<String> uniqueHashes1 = new HashSet<>(hashes1);

		System.out.println();
		System.out.println("File2 stuff: ");
		final List<String> hashes2 = new ArrayList<>();
		for (final Chunk chunk : chunks2) {
			hashes2.add(chunk.getHexHash());
			System.out.printf("offset: %d\tlength: %d%n", chunk.getOffset(), chunk.getLength());
		}
		final Set<String> uniqueHashes2 = new HashSet<>(hashes2);

		System.out.println();
		System.out.printf("File 1 chunks: %d total, %d unique, %d duplicate%n", hashes1.size(), uniqueHashes1.size(),
				hashes1.size() - uniqueHashes1.size());
		System.out.printf("File 2 chunks: %d total, %d unique, %d duplicate%n", hashes2.size(), uniqueHashes2.size(),
				hashes2.size() - uniqueHashes2.size());

		// Diff file1 to file2
		final List<String> file1ToFile2Unknown = new ArrayList<>();
		for (final String hash : uniqueHashes1) {
			if (!uniqueHashes2.contains(hash)) {
				file1ToFile2Unknown.add(hash);
			}
		}
		// Diff file2 to file1
		final List<String> file2ToFile1Unknown = new ArrayList<>();
		for (final String hash : uniqueHashes2) {
			if (!uniqueHashes1.contains(hash)) {
				file2ToFile1Unknown.add(hash);
			}
		}

		System.out.printf("File1 -> File2: %d unknown%n", file1ToFile2Unknown.size());
		System.out.printf("File2 -> File1: %d unknown%n", file2ToFile1Unknown.size());
	}
}
