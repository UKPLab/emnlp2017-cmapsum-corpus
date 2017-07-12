package eval;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import cmaps.ConceptMap;
import cmaps.Proposition;
import io.ConceptMapReader;
import io.Format;

/**
 * Check if concept maps in a given folder are well-formed
 * 
 * Implemented checks:
 * - map has to be connected
 * - only one relation between a pair of concepts
 * 
 * Usage:
 * java CheckConceptMaps <map-folder>
 * 
 * The folder has to contain maps as *.cmap files
 */
public class CheckConceptMaps {

	public static void main(String[] args) throws IOException {

		// parameters
		if (args.length < 1) {
			System.err.println("arguments missing!");
			System.err.println("Usage: java CheckConceptMaps <map-folder> [<map-name>]");
			System.exit(0);
		}

		File folder = new File(args[0]);
		String[] ext = { "cmap" };

		String name = ".*";
		if (args.length == 2)
			name = args[1];

		boolean hasError = false;
		for (File mapFile : FileUtils.listFiles(folder, ext, true)) {
			if (mapFile.getName().matches(name)) {

				System.out.println(mapFile);

				ConceptMap map = ConceptMapReader.readFromFile(mapFile, Format.TSV);
				System.out.println("concepts: \t" + map.getConcepts().size());
				System.out.println("relations: \t" + map.getProps().size());

				boolean connected = map.isConnected();
				System.out.println("connected: \t" + connected);
				if (!connected)
					hasError = true;

				Proposition dup = map.hasDoubleEdge();
				if (dup != null) {
					System.out.println(
							"duplicate relation: \t" + dup.sourceConcept.name + " <-> " + dup.targetConcept.name);
					hasError = true;
				}
			}
		}

		if (hasError)
			System.exit(4);
		else
			System.exit(0);

	}

}
