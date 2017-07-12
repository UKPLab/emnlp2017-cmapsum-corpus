package pipeline;

/**
 * Generate summary concept maps for document clusters
 */
public class Pipeline {

	/**
	 * folder containing topics with preprocessed documents
	 */
	public static final String dataFolder = "data/CMapSummaries/test";
	/**
	 * folder in which the maps should be placed
	 */
	public static final String mapFolder = "data/CMapSummaries/test_system";
	/**
	 * file name of the generated map
	 */
	public static final String mapName = "baseline";
	/**
	 * size restriction of final concept map
	 */
	public static int maxConcepts = 25;

	public static final String textPattern = "*.txt.bin6";

	public static void main(String[] args) throws UIMAException, IOException {

		// iterate over topics
		File folder = new File(dataFolder);
		for (File clusterFolder : folder.listFiles()) {
			if (clusterFolder.isDirectory()) {

				System.out.println("------------------------------------------------------------");
				System.out.println(clusterFolder.getName());
				System.out.println("------------------------------------------------------------");

				// read preprocessed documents
				String docLocation = dataFolder + "/" + clusterFolder.getName();

				CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
						BinaryCasReader.class, BinaryCasReader.PARAM_SOURCE_LOCATION, docLocation,
						BinaryCasReader.PARAM_PATTERNS, textPattern, BinaryCasReader.PARAM_LANGUAGE, "en");

				// configure concept mapping pipeline
				String[] pipeline = { "ConceptDict", "TextRankScorer", "ConceptExtractorNP", "RelationExtractorVerb",
						"MapBuilderClassifier" };

				String targetLocation = mapFolder + "/" + clusterFolder.getName();

				AnalysisEngineDescription cmm = AnalysisEngineFactory.createEngineDescription(ConceptMapMining.class,
						ConceptMapMining.PARAM_TARGET_LOCATION, targetLocation, ConceptMapMining.PARAM_MAX_CONCEPTS,
						maxConcepts, ConceptMapMining.PARAM_COMPONENTS, pipeline, ConceptMapMining.PARAM_NAME, mapName);

				// run pipeline
				SimplePipeline.runPipeline(reader, cmm);
			}
		}

	}

}