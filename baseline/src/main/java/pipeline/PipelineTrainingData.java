package pipeline;

/**
 * Generate training data to train a classifier that determines
 * summary-worthy concepts among a set of candidates
 */
public class PipelineTrainingData {

	/**
	 * folder containing topics with preprocessed documents
	 */
	public static final String dataFolder = "data/CMapSummaries/train";
	/**
	 * name of generated arff-file
	 */
	public static final String fileName = "training_data";

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
				String[] pipeline = { "ConceptDict", "TextRankScorer", "ConceptExtractorNP", "TrainingDataGenerator" };

				AnalysisEngineDescription cmm = AnalysisEngineFactory.createEngineDescription(ConceptMapMining.class,
						ConceptMapMining.PARAM_TARGET_LOCATION, docLocation, ConceptMapMining.PARAM_COMPONENTS,
						pipeline, ConceptMapMining.PARAM_NAME, fileName);

				// run pipeline
				SimplePipeline.runPipeline(reader, cmm);
			}
		}

	}

}