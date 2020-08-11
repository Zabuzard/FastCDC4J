package de.zabuza.fastcdc4j.examples;

import de.zabuza.fastcdc4j.external.chunking.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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
		//		descriptionToChunker.put("FastCDC 8KB NlFiedlerRust",
		//				new ChunkerBuilder().setChunkerOption(ChunkerOption.FAST_CDC)
		//						.setHashTableOption(HashTableOption.NLFIEDLER_RUST)
		//						.build());
		//		descriptionToChunker.put("NlFiedlerRust 8KB RTPal",
		//				new ChunkerBuilder().setChunkerOption(ChunkerOption.NLFIEDLER_RUST)
		//						.setHashTableOption(HashTableOption.RTPAL)
		//						.build());
		descriptionToChunker.put("FSC 1MB", new ChunkerBuilder().setChunkerOption(ChunkerOption.FIXED_SIZE_CHUNKING)
				.setExpectedChunkSize(1024 * 1024)
				.build());
		descriptionToChunker.put("FastCDC 1MB RTPal", new ChunkerBuilder().setChunkerOption(ChunkerOption.FAST_CDC)
				.build());
		descriptionToChunker.put("NlFiedlerRust 1MB NlFiedlerRust",
				new ChunkerBuilder().setChunkerOption(ChunkerOption.NLFIEDLER_RUST)
						.setHashTableOption(HashTableOption.NLFIEDLER_RUST)
						.setExpectedChunkSize(1024 * 1024)
						.build());
		//		descriptionToChunker.put("FastCDC 1MB NlFiedlerRust",
		//				new ChunkerBuilder().setChunkerOption(ChunkerOption.FAST_CDC)
		//						.setHashTableOption(HashTableOption.NLFIEDLER_RUST)
		//						.setExpectedChunkSize(1024 * 1024)
		//						.build());
		//		descriptionToChunker.put("NlFiedlerRust 1MB RTPal",
		//				new ChunkerBuilder().setChunkerOption(ChunkerOption.NLFIEDLER_RUST)
		//						.setHashTableOption(HashTableOption.RTPAL)
		//						.setExpectedChunkSize(1024 * 1024)
		//						.build());

		System.out.printf("Summary for patching from previous (%s) to current (%s):%n", previousBuild, currentBuild);
		System.out.println();
		descriptionToChunker.forEach(
				(description, chunker) -> PatchSummary.executePatchSummary(description, chunker, previousBuild,
						currentBuild));
	}

	private static String bytesToReadable(final long bytes) {
		if (bytes < 1_000) {
			return bytes + " B";
		}

		final double kiloBytes = bytes / 1_000.0;
		if (kiloBytes < 1_000) {
			return String.format("%.2f", kiloBytes) + " KB";
		}

		final double megaBytes = kiloBytes / 1_000.0;
		if (megaBytes < 1_000) {
			return String.format("%.2f", megaBytes) + " MB";
		}

		final double gigaBytes = megaBytes / 1_000.0;
		if (gigaBytes < 1_000) {
			return String.format("%.2f", gigaBytes) + " GB";
		}
		return "";
	}

	private static void chunkPath(final Chunker chunker, final Path path, final Consumer<? super Chunk> chunkAction) {
		try {
			final List<Path> files = Files.walk(path)
					.filter(Files::isRegularFile)
					.collect(Collectors.toList());

			final long totalBytes = files.stream()
					.mapToLong(file -> {
						try {
							return Files.size(file);
						} catch (final IOException e) {
							throw new UncheckedIOException(e);
						}
					})
					.sum();
			final AtomicLong processedBytesTotal = new AtomicLong(0);
			final AtomicLong processedBytesSincePrint = new AtomicLong(0);
			final AtomicLong timeStart = new AtomicLong(System.nanoTime());
			final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
			final long nanosPerSecond = 1_000_000_000L;
			final Runnable statPrinter = () -> {
				final AtomicLong timeEnd = new AtomicLong(System.nanoTime());
				final long timeDiff = timeEnd.get() - timeStart.get();
				if (timeDiff < nanosPerSecond) {
					return;
				}
				timeStart.set(timeEnd.get());
				final long bytesPerSecond = processedBytesSincePrint.get() / (timeDiff / nanosPerSecond);
				final long bytesLeft = totalBytes - processedBytesTotal.get();
				final long secondsLeft = bytesLeft / (bytesPerSecond == 0 ? 1 : bytesPerSecond);

				System.out.printf("\t%12s/s, %12s ETC, %12s processed, %12s total\r",
						PatchSummary.bytesToReadable(bytesPerSecond), PatchSummary.secondsToReadable(secondsLeft),
						PatchSummary.bytesToReadable(processedBytesTotal.get()),
						PatchSummary.bytesToReadable(totalBytes));

				processedBytesSincePrint.set(0);
			};
			final var statPrintTask = service.scheduleAtFixedRate(statPrinter, 0, 1, TimeUnit.SECONDS);

			files.parallelStream()
					.filter(Files::isRegularFile)
					.map(chunker::chunk)
					.forEach(chunks -> chunks.forEach(chunk -> {
						processedBytesTotal.addAndGet(chunk.getLength());
						processedBytesSincePrint.addAndGet(chunk.getLength());

						chunkAction.accept(chunk);
					}));
			statPrintTask.cancel(false);
			service.shutdown();
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static void executePatchSummary(final String description, final Chunker chunker, final Path previousBuild,
			final Path currentBuild) {
		final List<ChunkMetadata> previousChunks = Collections.synchronizedList(new ArrayList<>());
		PatchSummary.chunkPath(chunker, previousBuild, chunk -> previousChunks.add(chunk.toChunkMetadata()));
		final BuildSummary previousBuildSummary = new BuildSummary(previousChunks);

		final List<ChunkMetadata> currentChunks = Collections.synchronizedList(new ArrayList<>());
		PatchSummary.chunkPath(chunker, currentBuild, chunk -> currentChunks.add(chunk.toChunkMetadata()));
		final BuildSummary currentBuildSummary = new BuildSummary(currentChunks);

		final PatchSummary summary = new PatchSummary(previousBuildSummary, currentBuildSummary);
		System.out.println("==== " + description);
		System.out.printf("%-25s %12s total size, %12d total chunks, %12s unique size, %12d unique chunks%n",
				"Build summary previous:", PatchSummary.bytesToReadable(previousBuildSummary.getTotalSize()),
				previousBuildSummary.getTotalChunksCount(),
				PatchSummary.bytesToReadable(previousBuildSummary.getTotalUniqueSize()),
				previousBuildSummary.getUniqueChunksCount());
		System.out.printf("%-25s %12s total size, %12d total chunks, %12s unique size, %12d unique chunks%n",
				"Build summary current:", PatchSummary.bytesToReadable(currentBuildSummary.getTotalSize()),
				currentBuildSummary.getTotalChunksCount(),
				PatchSummary.bytesToReadable(currentBuildSummary.getTotalUniqueSize()),
				currentBuildSummary.getUniqueChunksCount());
		System.out.printf("%-25s %12s average chunk size, %12.2f%% deduplication ratio%n", "Build metrics previous:",
				PatchSummary.bytesToReadable(previousBuildSummary.getAverageChunkSize()),
				previousBuildSummary.getDeduplicationRatio());
		System.out.printf("%-25s %12s average chunk size, %12.2f%% deduplication ratio%n", "Build metrics current:",
				PatchSummary.bytesToReadable(currentBuildSummary.getAverageChunkSize()),
				currentBuildSummary.getDeduplicationRatio());
		System.out.printf("%-25s %12s%n", "Patch size:", PatchSummary.bytesToReadable(summary.getPatchSize()));
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

	private static String secondsToReadable(final long seconds) {
		final StringBuilder sb = new StringBuilder();
		boolean entered = false;
		final Duration time = Duration.ofSeconds(seconds);

		final long days = time.toDays();
		if (days != 0) {
			sb.append(days)
					.append("d ");
			entered = true;
		}

		final int hours = time.toHoursPart();
		if (hours != 0 || entered) {
			sb.append(hours)
					.append("h ");
			entered = true;
		}

		final int minutes = time.toMinutesPart();
		if (minutes != 0 || entered) {
			sb.append(minutes)
					.append("m ");
		}

		sb.append(time.toSecondsPart())
				.append("s");
		return sb.toString();
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
