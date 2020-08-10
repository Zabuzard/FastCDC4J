package de.zabuza.fastcdc4j.examples;

import de.zabuza.fastcdc4j.external.chunking.*;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Class offering a {@link #main(String[])} method that compares two given paths with each other and prints statistics
 * for patching one file to the other using chunks.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
final class PatchSummary {
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
		final Path previousBuild = Path.of(args[0]);
		final Path currentBuild = Path.of(args[1]);

		final Map<String, Chunker> descriptionToChunker = new HashMap<>();
		//		descriptionToChunker.put("FSC 8KB", new ChunkerBuilder().setChunkerOption(ChunkerOption.FIXED_SIZE_CHUNKING)
		//				.build());
		//		descriptionToChunker.put("FastCDC 8KB RTPal", new ChunkerBuilder().setChunkerOption(ChunkerOption.FAST_CDC)
		//				.build());
		//		descriptionToChunker.put("NlFiedlerRust 8KB NlFiedlerRust",
		//				new ChunkerBuilder().setChunkerOption(ChunkerOption.NLFIEDLER_RUST)
		//						.setHashTableOption(HashTableOption.NLFIEDLER_RUST)
		//						.build());
		descriptionToChunker.put("FastCDC 8KB NlFiedlerRust",
				new ChunkerBuilder().setChunkerOption(ChunkerOption.FAST_CDC)
						.setHashTableOption(HashTableOption.NLFIEDLER_RUST)
						.build());
		descriptionToChunker.put("NlFiedlerRust 8KB RTPal",
				new ChunkerBuilder().setChunkerOption(ChunkerOption.NLFIEDLER_RUST)
						.setHashTableOption(HashTableOption.RTPAL)
						.build());

		System.out.printf("Summary for patching from previous (%s) to current (%s):%n", previousBuild, currentBuild);
		System.out.println();
		descriptionToChunker.forEach(
				(description, chunker) -> PatchSummary.executePatchSummary(description, chunker, previousBuild,
						currentBuild));
	}

	private static void executePatchSummary(final String description, final Chunker chunker, final Path previousBuild,
			final Path currentBuild) {
		final List<ChunkMetadata> previousChunks = new ArrayList<>();
		chunker.chunk(previousBuild)
				.forEach(chunk -> previousChunks.add(chunk.toChunkMetadata()));
		final BuildSummary previousBuildSummary = new BuildSummary(previousChunks);

		final List<ChunkMetadata> currentChunks = new ArrayList<>();
		chunker.chunk(currentBuild)
				.forEach(chunk -> currentChunks.add(chunk.toChunkMetadata()));
		final BuildSummary currentBuildSummary = new BuildSummary(currentChunks);

		final PatchSummary summary = new PatchSummary(previousBuildSummary, currentBuildSummary);
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

	private PatchSummary(final BuildSummary previousBuildSummary, final BuildSummary currentBuildSummary) {
		this.previousBuildSummary = previousBuildSummary;
		this.currentBuildSummary = currentBuildSummary;
		computePatch();
	}

	private List<ChunkMetadata> getChunksToAdd() {
		return Collections.unmodifiableList(chunksToAdd);
	}

	private List<ChunkMetadata> getChunksToMove() {
		return Collections.unmodifiableList(chunksToMove);
	}

	private List<ChunkMetadata> getChunksToRemove() {
		return Collections.unmodifiableList(chunksToRemove);
	}

	private long getPatchSize() {
		return patchSize;
	}

	private List<ChunkMetadata> getUntouchedChunks() {
		return Collections.unmodifiableList(untouchedChunks);
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
				.filter(currentChunk -> previousBuildSummary.getChunk(currentChunk.getHexHash())
						.getOffset() != currentChunk.getOffset())
				.forEach(chunksToMove::add);
		// Untouched chunks
		currentBuildSummary.getChunks()
				.filter(previousBuildSummary::containsChunk)
				.filter(currentChunk -> previousBuildSummary.getChunk(currentChunk.getHexHash())
						.getOffset() == currentChunk.getOffset())
				.forEach(untouchedChunks::add);

		patchSize = chunksToAdd.stream()
				.mapToLong(ChunkMetadata::getLength)
				.sum();
	}

	private static final class BuildSummary {
		private final Map<String, ChunkMetadata> hashToChunk = new HashMap<>();
		private int totalChunksCount;
		private long totalSize;
		private long totalUniqueSize;
		private int uniqueChunksCount;

		private BuildSummary(final Iterable<? extends ChunkMetadata> chunks) {
			chunks.forEach(chunk -> {
				totalChunksCount++;
				totalSize += chunk.getLength();

				if (hashToChunk.containsKey(chunk.getHexHash())) {
					return;
				}
				hashToChunk.put(chunk.getHexHash(), chunk);
				uniqueChunksCount++;
				totalUniqueSize += chunk.getLength();
			});
		}

		boolean containsChunk(final ChunkMetadata chunk) {
			return hashToChunk.containsKey(chunk.getHexHash());
		}

		int getAverageChunkSize() {
			//noinspection NumericCastThatLosesPrecision
			return (int) (totalSize / totalChunksCount);
		}

		ChunkMetadata getChunk(final String hash) {
			return hashToChunk.get(hash);
		}

		Stream<ChunkMetadata> getChunks() {
			return hashToChunk.values()
					.stream();
		}

		double getDeduplicationRatio() {
			return (double) totalUniqueSize / totalSize * 100;
		}

		int getTotalChunksCount() {
			return totalChunksCount;
		}

		long getTotalSize() {
			return totalSize;
		}

		long getTotalUniqueSize() {
			return totalUniqueSize;
		}

		int getUniqueChunksCount() {
			return uniqueChunksCount;
		}
	}
}