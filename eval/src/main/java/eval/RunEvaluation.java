package eval;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import cmaps.ConceptMap;
import eval.matcher.StemSWMatch;
import eval.metrics.Metric;
import eval.metrics.PropositionMatchMetric;
import io.ConceptMapReader;
import io.Format;

/**
 * Run an evaluation that compares generated and reference concept maps on a
 * dataset with several topics
 * 
 * Usage:
 * java RunEvaluation <gold-folder> <system-folder>
 * 
 * Both folders have to contain subfolders for each topic and the corresponding
 * concept maps as *.cmap files
 */
public class RunEvaluation {

	public static void main(String[] args) throws IOException {

		// parameters
		if (args.length < 2) {
			System.err.println("arguments missing!");
			System.err.println("Usage: java RunEvaluation <gold-folder> <system-folder> [<map-name>]");
			System.exit(0);
		}

		String goldFolderName = args[0];
		String mapFolderName = args[1];

		String name = ".*";
		if (args.length == 3)
			name = args[2];

		// run evaluation per topic
		List<Evaluation> evaluations = runEval(goldFolderName, mapFolderName, name);

		// compute averages
		List<Evaluation> avgResults = Evaluation.getAvgResults(evaluations, false);
		for (Evaluation eval : avgResults) {
			System.out.println(eval.printResults());
		}

	}

	// run evaluations for all topics
	public static List<Evaluation> runEval(String goldFolderName, String mapFolderName, String namePattern) {

		// set up metrics
		InputStream sw = RunEvaluation.class.getResourceAsStream("/lists/stopwords_en_eval.txt");
		Metric metric = new PropositionMatchMetric(new StemSWMatch(sw), true);
		Metric[] metrics = { metric };

		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".cmap");
			}
		};

		System.out.println("Computing evaluation metrics");
		List<Evaluation> evaluations = new LinkedList<Evaluation>();

		// iterate over document cluster
		File goldFolder = new File(goldFolderName);
		for (File clusterFolder : goldFolder.listFiles()) {
			if (clusterFolder.isDirectory()) {

				// load maps
				File[] files = clusterFolder.listFiles(filter);
				if (files.length == 0) {
					System.out.println(clusterFolder.getName() + ": no reference concept map found");
					System.exit(4);
				}
				File goldFile = new File(clusterFolder.listFiles(filter)[0].getPath());
				ConceptMap mapGold = ConceptMapReader.readFromFile(goldFile, Format.TSV);

				List<ConceptMap> maps = new LinkedList<ConceptMap>();
				File mFolder = new File(mapFolderName + "/" + clusterFolder.getName());
				for (File file : mFolder.listFiles(filter)) {
					if (file.getName().matches(namePattern)) {
						ConceptMap map = ConceptMapReader.readFromFile(file, Format.TSV);
						maps.add(map);
					}
				}
				if (maps.size() == 0) {
					System.out.println(clusterFolder.getName() + ": no system concept maps found");
					System.exit(4);
				}

				// evaluate
				Evaluation eval = new Evaluation(clusterFolder.getName(), mapGold);
				eval.addConceptMaps(maps);
				eval.addMetrics(metrics);
				eval.run();
				evaluations.add(eval);

				System.out.println(eval.printResults());
			}
		}
		return evaluations;
	}

	// save to file
	public static void saveToFile(String mapFolder, List<Evaluation> evaluations) {

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		File resultFile = new File(mapFolder + "/eval-" + df.format(new Date()) + ".csv");
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile));
			for (Evaluation eval : evaluations) {
				writer.write(eval.getCsv());
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
