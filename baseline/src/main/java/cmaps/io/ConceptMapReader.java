package cmaps.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cmaps.Concept;
import cmaps.ConceptMap;
import cmaps.Proposition;

/**
 * Reads a concept map from a text file
 */
public class ConceptMapReader {

	public static ConceptMap readFromFile(File file, Format format) {
		switch (format) {
		case TSV:
			return fromTsv(file);
		default:
			return null;
		}
	}

	private static ConceptMap fromTsv(File file) {
		ConceptMap map = new ConceptMap(file.getName());
		Map<String, Concept> concepts = new HashMap<String, Concept>();
		for (String line : read(file)) {
			String[] parts = line.split("\t");
			if (parts.length >= 3) {
				Concept sC = concepts.get(parts[0]);
				if (sC == null) {
					sC = new Concept(parts[0]);
					concepts.put(parts[0], sC);
					map.addConcept(sC);
				}
				Concept tC = concepts.get(parts[2]);
				if (tC == null) {
					tC = new Concept(parts[2]);
					concepts.put(parts[2], tC);
					map.addConcept(tC);
				}
				Proposition p = new Proposition(sC, tC, parts[1]);
				map.addProposition(p);
			}
		}
		return map;
	}

	private static List<String> read(File file) {
		try {
			return Files.readAllLines(file.toPath());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
