package de.zabuza.fastcdc4j.examples;

import de.zabuza.fastcdc4j.external.chunking.Chunk;
import de.zabuza.fastcdc4j.external.chunking.Chunker;
import de.zabuza.fastcdc4j.external.chunking.ChunkerBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class CompareFiles {
	/**
	 * Starts the application.
	 *
	 * @param args Not supported
	 */
	public static void main(final String[] args) {
		Path first = Path.of("C:\\Users\\dtischner\\Desktop\\common.dat");
		Path second = Path.of("C:\\Users\\dtischner\\Desktop\\common_prepend.dat");

		Chunker chunker = new ChunkerBuilder().build();

		Iterable<Chunk> chunks1 = chunker.chunk(first);
		Iterable<Chunk> chunks2 = chunker.chunk(second);

		System.out.println("File1 stuff: ");
		List<String> hashes1 = new ArrayList<>();
		for (Chunk chunk : chunks1) {
			hashes1.add(chunk.getHexHash());
			System.out.printf("offset: %d\tlength: %d%n", chunk.getOffset(), chunk.getLength());
		}
		Set<String> uniqueHashes1 = new HashSet<>(hashes1);

		System.out.println();
		System.out.println("File2 stuff: ");
		List<String> hashes2 = new ArrayList<>();
		for (Chunk chunk : chunks2) {
			hashes2.add(chunk.getHexHash());
			System.out.printf("offset: %d\tlength: %d%n", chunk.getOffset(), chunk.getLength());
		}
		Set<String> uniqueHashes2 = new HashSet<>(hashes2);

		System.out.println();
		System.out.printf("File 1 chunks: %d total, %d unique, %d duplicate%n", hashes1.size(), uniqueHashes1.size(),
				hashes1.size() - uniqueHashes1.size());
		System.out.printf("File 2 chunks: %d total, %d unique, %d duplicate%n", hashes2.size(), uniqueHashes2.size(),
				hashes2.size() - uniqueHashes2.size());

		// Diff file1 to file2
		List<String> file1ToFile2Unknown = new ArrayList<>();
		for (String hash : uniqueHashes1) {
			if (!uniqueHashes2.contains(hash)) {
				file1ToFile2Unknown.add(hash);
			}
		}
		// Diff file2 to file1
		List<String> file2ToFile1Unknown = new ArrayList<>();
		for (String hash : uniqueHashes2) {
			if (!uniqueHashes1.contains(hash)) {
				file2ToFile1Unknown.add(hash);
			}
		}

		System.out.printf("File1 -> File2: %d unknown%n", file1ToFile2Unknown.size());
		System.out.printf("File2 -> File1: %d unknown%n", file2ToFile1Unknown.size());
	}
}
