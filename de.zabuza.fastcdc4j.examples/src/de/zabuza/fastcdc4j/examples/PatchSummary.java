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
 * Class oferring a {@link #main(String[])} method that compares two given paths with each other and prints statistics
 * for patching one file to the other using chunks.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class PatchSummary {
	/**
	 * Starts the application.
	 *
	 * @param args Two arguments, the path to the previous build and the path to the current build to compare.
	 */
	public static void main(final String[] args) {
		if (args.length != 2) {
			throw new IllegalArgumentException(
					"Expected two arguments path1 and path2, where path1 denotes the path to the previous version and path2 the path to the current version.");
		}
		Path previousBuild = Path.of(args[0]);
		Path currentBuild = Path.of(args[1]);

		Map<String, Chunker> descriptionToChunker = new HashMap<>();
		descriptionToChunker.put("FSC 8KB", new ChunkerBuilder().setChunkerOption(ChunkerOption.FIXED_SIZE_CHUNKING)
				.build());
		descriptionToChunker.put("FastCDC 8KB RTPal", new ChunkerBuilder().setChunkerOption(ChunkerOption.FAST_CDC)
				.build());
		descriptionToChunker.put("NlFiedlerRust 8KB NlFiedlerRust",
				new ChunkerBuilder().setChunkerOption(ChunkerOption.NLFIEDLER_RUST)
						.setHashTableOption(HashTableOption.NLFIEDLER_RUST)
						.build());

		System.out.printf("Summary for patching from previous (%s) to current (%s):%n", previousBuild, currentBuild);
		System.out.println();
		descriptionToChunker.forEach(
				(description, chunker) -> executePatchSummary(description, chunker, previousBuild, currentBuild));
	}

	private static void executePatchSummary(String description, Chunker chunker, Path previousBuild,
			Path currentBuild) {
		List<ChunkMetadata> previousChunks = new ArrayList<>();
		chunker.chunk(previousBuild)
				.forEach(chunk -> previousChunks.add(new ChunkMetadata(chunk)));
		BuildSummary previousBuildSummary = new BuildSummary(previousChunks);

		List<ChunkMetadata> currentChunks = new ArrayList<>();
		chunker.chunk(currentBuild)
				.forEach(chunk -> currentChunks.add(new ChunkMetadata(chunk)));
		BuildSummary currentBuildSummary = new BuildSummary(currentChunks);

		PatchSummary summary = new PatchSummary(previousBuildSummary, currentBuildSummary);
		System.out.println("==== " + description);
		System.out.printf("%-25s %12d total size, %12d total chunks, %12d unique size, %12d unique chunks%n",
				"Build summary previous:", previousBuildSummary.getTotalSize(),
				previousBuildSummary.getTotalChunksCount(), previousBuildSummary.getTotalUniqueSize(),
				previousBuildSummary.getUniqueChunksCount());
		System.out.printf("%-25s %12d total size, %12d total chunks, %12d unique size, %12d unique chunks%n",
				"Build summary current:", currentBuildSummary.getTotalSize(), currentBuildSummary.getTotalChunksCount(),
				currentBuildSummary.getTotalUniqueSize(), currentBuildSummary.getUniqueChunksCount());
		System.out.printf("%-25s %12d average chunk size, %12.2f%% deduplication ratio%n", "Build metrics previous:",
				previousBuildSummary.getAverageChunkSize(), previousBuildSummary.getDeduplicationRatio());
		System.out.printf("%-25s %12d average chunk size, %12.2f%% deduplication ratio%n", "Build metrics current:",
				currentBuildSummary.getAverageChunkSize(), currentBuildSummary.getDeduplicationRatio());
		System.out.printf("%-25s %12d%n", "Patch size:", summary.getPatchSize());
		System.out.printf("%-25s %12d%n", "Chunks to add:", summary.getChunksToAdd()
				.size());
		System.out.printf("%-25s %12d%n", "Chunks to remove:", summary.getChunksToRemove()
				.size());
		System.out.printf("%-25s %12d%n", "Chunks to move:", summary.getChunksToMove()
				.size());
		System.out.printf("%-25s %12d%n", "Untouched chunks:", summary.getUntouchedChunks()
				.size());
		System.out.println();
	}

	private final List<ChunkMetadata> chunksToAdd = new ArrayList<>();
	private final List<ChunkMetadata> chunksToMove = new ArrayList<>();
	private final List<ChunkMetadata> chunksToRemove = new ArrayList<>();
	private final BuildSummary currentBuildSummary;
	private final BuildSummary previousBuildSummary;
	private final List<ChunkMetadata> untouchedChunks = new ArrayList<>();
	private long patchSize;

	public PatchSummary(BuildSummary previousBuildSummary, BuildSummary currentBuildSummary) {
		this.previousBuildSummary = previousBuildSummary;
		this.currentBuildSummary = currentBuildSummary;
		computePatch();
	}

	public List<ChunkMetadata> getChunksToAdd() {
		return chunksToAdd;
	}

	public List<ChunkMetadata> getChunksToMove() {
		return chunksToMove;
	}

	public List<ChunkMetadata> getChunksToRemove() {
		return chunksToRemove;
	}

	public long getPatchSize() {
		return patchSize;
	}

	public List<ChunkMetadata> getUntouchedChunks() {
		return untouchedChunks;
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
				.mapToLong(ChunkMetadata::getLength)
				.sum();
	}

	private static class BuildSummary {
		private final Map<String, ChunkMetadata> hashToChunk = new HashMap<>();
		private int totalChunksCount = 0;
		private long totalSize = 0;
		private long totalUniqueSize = 0;
		private int uniqueChunksCount = 0;

		public BuildSummary(Iterable<ChunkMetadata> chunks) {
			chunks.forEach(chunk -> {
				totalChunksCount++;
				totalSize += chunk.getLength();

				if (hashToChunk.containsKey(chunk.hexHash)) {
					return;
				}
				hashToChunk.put(chunk.hexHash, chunk);
				uniqueChunksCount++;
				totalUniqueSize += chunk.getLength();
			});
		}

		public boolean containsChunk(ChunkMetadata chunk) {
			return hashToChunk.containsKey(chunk.getHexHash());
		}

		public int getAverageChunkSize() {
			return (int) (totalSize / totalChunksCount);
		}

		public ChunkMetadata getChunk(String hash) {
			return hashToChunk.get(hash);
		}

		public Stream<ChunkMetadata> getChunks() {
			return hashToChunk.values()
					.stream();
		}

		public double getDeduplicationRatio() {
			return (double) totalUniqueSize / totalSize * 100;
		}

		public int getTotalChunksCount() {
			return totalChunksCount;
		}

		public long getTotalSize() {
			return totalSize;
		}

		public long getTotalUniqueSize() {
			return totalUniqueSize;
		}

		public int getUniqueChunksCount() {
			return uniqueChunksCount;
		}
	}

	private static class ChunkMetadata {
		private final String hexHash;
		private final int length;
		private final long offset;

		public ChunkMetadata(Chunk chunk) {
			hexHash = chunk.getHexHash();
			offset = chunk.getOffset();
			length = chunk.getLength();
		}

		public String getHexHash() {
			return hexHash;
		}

		public int getLength() {
			return length;
		}

		public long getOffset() {
			return offset;
		}
	}
}
