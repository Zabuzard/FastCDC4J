package de.zabuza.fastcdc4j.examples;

import de.zabuza.fastcdc4j.external.chunking.Chunker;
import de.zabuza.fastcdc4j.external.chunking.ChunkerBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class offering a {@link #main(String[])} method that compares builds in a given folder with each other and creates
 * files with patch sizes.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
@SuppressWarnings({ "UseOfSystemOutOrSystemErr", "MagicNumber" })
enum PatchBenchmark {
	;

	/**
	 * Starts the application.
	 *
	 * @param args One arguments, the path to the builds to benchmark.
	 *
	 * @throws IOException If an IOException occurred
	 */
	public static void main(final String[] args) throws IOException {
		if (args.length != 1) {
			throw new IllegalArgumentException(
					"Expected one arguments denoting the path to the folder containing the builds to benchmark.");
		}

		final Path basePath = Path.of(args[0]);
		final List<String> builds;
		try (final Stream<Path> stream = Files.list(basePath)) {
			builds = stream.filter(Files::isDirectory)
					.map(Path::getFileName)
					.map(Path::toString)
					.sorted()
					.collect(Collectors.toList());
		}

		final List<Map.Entry<String, String>> buildsToCompare = new ArrayList<>();
		for (int i = 0; i < builds.size() - 1; i++) {
			final String previous = builds.get(i);
			final String current = builds.get(i + 1);

			buildsToCompare.add(Map.entry(previous, current));
		}

		System.out.printf("Comparing %d patch scenarios%n", buildsToCompare.size());

		final Collection<String> patchDataLines = new ArrayList<>();
		patchDataLines.add("patch,name,fsc2mb,fastcdc2mb,fastcdc8kb");
		//		patchDataLines.add("patch,name,rtpal262kb");
		final Collection<String> buildDataLines = new ArrayList<>();
		buildDataLines.add("version,name,size");
		int i = 1;
		for (final Map.Entry<String, String> comparison : buildsToCompare) {
			//noinspection HardcodedFileSeparator
			System.out.printf("====================== %d / %d patch scenarios ======================%n", i,
					buildsToCompare.size());

			final Path previousBuild = basePath.resolve(comparison.getKey());
			final Path currentBuild = basePath.resolve(comparison.getValue());

			final Map<String, Chunker> descriptionToChunker = new LinkedHashMap<>();
			descriptionToChunker.put("fsc2mb", new ChunkerBuilder().fsc()
					.setExpectedChunkSize(2 * 1024 * 1024)
					.build());
			descriptionToChunker.put("fastcdc2mb", new ChunkerBuilder().fastCdc()
					.setExpectedChunkSize(2 * 1024 * 1024)
					.build());
			descriptionToChunker.put("fastcdc8kb", new ChunkerBuilder().fastCdc()
					.setExpectedChunkSize(8 * 1024)
					.build());
			//			descriptionToChunker.put("rtpal262kb", new RtpalChunker());

			System.out.printf("Summary for patching from previous (%s) to current (%s):%n", comparison.getKey(),
					comparison.getValue());
			System.out.println();

			final Collection<Long> patchSizes = new ArrayList<>();
			final AtomicLong previousBuildSize = new AtomicLong();
			final AtomicLong currentBuildSize = new AtomicLong();
			descriptionToChunker.forEach((description, chunker) -> {
				final PatchSummary summary = PatchSummary.computePatchSummary(chunker, previousBuild, currentBuild);

				patchSizes.add(summary.getPatchSize());

				previousBuildSize.set(summary.getPreviousBuildSummary()
						.getTotalSize());
				currentBuildSize.set(summary.getCurrentBuildSummary()
						.getTotalSize());
			});

			final StringJoiner patchSizesJoiner = new StringJoiner(",");
			patchSizesJoiner.add(String.valueOf(i + 1));
			patchSizesJoiner.add(previousBuild.getFileName() + "-" + currentBuild.getFileName());
			patchSizes.forEach(size -> patchSizesJoiner.add(String.valueOf(size)));
			patchDataLines.add(patchSizesJoiner.toString());

			if (i == 1) {
				final StringJoiner buildDataJoiner = new StringJoiner(",");
				buildDataJoiner.add("1");
				buildDataJoiner.add(previousBuild.getFileName()
						.toString());
				buildDataJoiner.add(String.valueOf(previousBuildSize.get()));
				buildDataLines.add(buildDataJoiner.toString());
			}

			final StringJoiner buildDataJoiner = new StringJoiner(",");
			buildDataJoiner.add(String.valueOf(i + 1));
			buildDataJoiner.add(currentBuild.getFileName()
					.toString());
			buildDataJoiner.add(String.valueOf(currentBuildSize.get()));
			buildDataLines.add(buildDataJoiner.toString());

			i++;

			final Path buildDataPath = Path.of("benchmark_build_data.csv");
			final Path patchDataPath = Path.of("benchmark_patch_data.csv");
			Files.write(buildDataPath, buildDataLines);
			Files.write(patchDataPath, patchDataLines);
			System.out.println("Updated benchmark build data: " + buildDataPath);
			System.out.println("Updated benchmark patch data: " + patchDataPath);
		}
	}
}