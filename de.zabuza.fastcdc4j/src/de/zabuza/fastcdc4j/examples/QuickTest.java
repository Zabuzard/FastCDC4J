package de.zabuza.fastcdc4j.examples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class QuickTest {
	/**
	 * Starts the application.
	 *
	 * @param args Not supported
	 */
	public static void main(final String[] args) throws IOException {
		Path first = Path.of("C:\\Users\\dtischner\\Desktop\\common2.dat");
		Path second = Path.of("C:\\Users\\dtischner\\Desktop\\common3.dat");
		byte[] data = Files.readAllBytes(first);

		byte[] otherData = new byte[data.length];
		for (int i = 3; i < data.length; i++) {
			otherData[i - 3] = data[i];
		}
		otherData[otherData.length - 3] = data[0];
		otherData[otherData.length - 2] = data[1];
		otherData[otherData.length - 1] = data[2];

		Files.write(second, otherData);
	}

}
