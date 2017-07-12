package io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import cmaps.ConceptMap;
import cmaps.Proposition;

/**
 * writes a concept map to a file
 */
public class ConceptMapWriter {

	public static boolean writeToFile(ConceptMap map, File file, Format format) {
		switch (format) {
		case TSV:
			return toTsv(map, file);
		default:
			return false;
		}
	}

	private static boolean toTsv(ConceptMap map, File file) {
		StringBuilder res = new StringBuilder();
		for (Proposition p : map.getProps()) {
			res.append(p.sourceConcept.name + "\t" + p.relationPhrase + "\t" + p.targetConcept.name + "\n");
		}
		return write(res.toString(), file);
	}

	private static boolean write(String content, File file) {
		try {
			Files.write(file.toPath(), content.getBytes());
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

}
