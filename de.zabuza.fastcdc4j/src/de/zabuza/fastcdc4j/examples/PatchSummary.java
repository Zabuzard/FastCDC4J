package de.zabuza.fastcdc4j.examples;

import de.zabuza.fastcdc4j.external.chunking.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class PatchSummary {
	/**
	 * Starts the application.
	 *
	 * @param args Not supported
	 */
	public static void main(final String[] args) {
		if (args.length != 2) {
			throw new IllegalArgumentException(
					"Expected two arguments path1 and path2, where path1 denotes the path to the previous version and path2 the path to the current version.");
		}
		Path previousBuild = Path.of(args[0]);
		Path currentBuild = Path.of(args[1]);

		// Analyze builds
		Chunker chunker = new ChunkerBuilder().setChunkerOption(ChunkerOption.NLFIEDLER_RUST).setHashTableOption(
				HashTableOption.NLFIEDLER_RUST).build();
		List<ChunkMetadata> previousChunks = new ArrayList<>();
		chunker.chunk(previousBuild)
				.forEach(chunk -> previousChunks.add(new ChunkMetadata(chunk)));
		BuildSummary previousBuildSummary = new BuildSummary(previousChunks);

		List<ChunkMetadata> currentChunks = new ArrayList<>();
		chunker.chunk(currentBuild)
				.forEach(chunk -> currentChunks.add(new ChunkMetadata(chunk)));
		BuildSummary currentBuildSummary = new BuildSummary(currentChunks);

		// Summarize patch
		PatchSummary summary = new PatchSummary(previousBuildSummary, currentBuildSummary);
		System.out.printf("Summary for patching from previous (%s) to current (%s):%n", previousBuild, currentBuild);
		System.out.printf("%-20s %10d%n", "Patch size (byte):", summary.getPatchSize());
		System.out.printf("%-20s %10d%n", "Chunks to add:", summary.getChunksToAdd()
				.size());
		System.out.printf("%-20s %10d%n", "Chunks to remove:", summary.getChunksToRemove()
				.size());
		System.out.printf("%-20s %10d%n", "Chunks to move:", summary.getChunksToMove()
				.size());
		System.out.printf("%-20s %10d%n", "Untouched chunks:", summary.getUntouchedChunks()
				.size());
	}
	private final BuildSummary previousBuildSummary;
	private final BuildSummary currentBuildSummary;
	private final List<ChunkMetadata> chunksToRemove = new ArrayList<>();
	private final List<ChunkMetadata> chunksToAdd = new ArrayList<>();
	private final List<ChunkMetadata> chunksToMove = new ArrayList<>();
	private final List<ChunkMetadata> untouchedChunks = new ArrayList<>();
	private int patchSize;
	public PatchSummary(BuildSummary previousBuildSummary, BuildSummary currentBuildSummary) {
		this.previousBuildSummary = previousBuildSummary;
		this.currentBuildSummary = currentBuildSummary;
		computePatch();
	}

	public List<ChunkMetadata> getChunksToRemove() {
		return chunksToRemove;
	}

	public List<ChunkMetadata> getChunksToAdd() {
		return chunksToAdd;
	}

	public List<ChunkMetadata> getChunksToMove() {
		return chunksToMove;
	}

	public List<ChunkMetadata> getUntouchedChunks() {
		return untouchedChunks;
	}

	public int getPatchSize() {
		return patchSize;
	}

	private void computePatch() {
		// Chunks to remove
		previousBuildSummary.getChunks()
				.filter(Predicate.not(currentBuildSummary::containsChunk))
				.forEach(chunksToRemove::add);
		// Chunks to add
		currentBuildSummary.getChunks()
				.filter(Predicate.not(previousBuildSummary::containsChunk))
				.forEach(chunksToAdd::add);
		// Chunks to move
		currentBuildSummary.getChunks()
				.filter(previousBuildSummary::containsChunk)
				.filter(currentChunk -> previousBuildSummary.getChunk(currentChunk.hexHash).offset
						!= currentChunk.offset)
				.forEach(chunksToMove::add);
		// Untouched chunks
		currentBuildSummary.getChunks()
				.filter(previousBuildSummary::containsChunk)
				.filter(currentChunk -> previousBuildSummary.getChunk(currentChunk.hexHash).offset
						== currentChunk.offset)
				.forEach(untouchedChunks::add);

		patchSize = chunksToAdd.stream()
				.mapToInt(ChunkMetadata::getLength)
				.sum();
	}

	private static class ChunkMetadata {
		private final String hexHash;
		private final long offset;
		private final int length;

		public ChunkMetadata(Chunk chunk) {
			hexHash = chunk.getHexHash();
			offset = chunk.getOffset();
			length = chunk.getLength();
		}

		public String getHexHash() {
			return hexHash;
		}

		public long getOffset() {
			return offset;
		}

		public int getLength() {
			return length;
		}
	}

	private static class BuildSummary {
		private final Map<String, ChunkMetadata> hashToChunk = new HashMap<>();

		public BuildSummary(Iterable<ChunkMetadata> chunks) {
			chunks.forEach(chunk -> {
				if (hashToChunk.containsKey(chunk.hexHash)) {
					return;
				}
				hashToChunk.put(chunk.hexHash, chunk);
			});
		}

		public Stream<ChunkMetadata> getChunks() {
			return hashToChunk.values()
					.stream();
		}

		public boolean containsChunk(ChunkMetadata chunk) {
			return hashToChunk.containsKey(chunk.getHexHash());
		}

		public ChunkMetadata getChunk(String hash) {
			return hashToChunk.get(hash);
		}
	}
}
