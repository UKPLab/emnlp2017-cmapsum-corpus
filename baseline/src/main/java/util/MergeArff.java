package util;

/**
 * Merge several arff-files with the same attributes into a single file
 */
public class MergeArff {

	/**
	 * folder containing topics with preprocessed documents
	 */
	public static final String dataFolder = "data/CMapSummaries/train";
	/**
	 * name of generated arff-file per topic
	 */
	public static final String fileName = "training_data";
	/**
	 * name of new merged file
	 */
	public static final String fileNameMerged = "training_data";

	public static void main(String[] args) throws Exception {

		Instances all = null;

		// iterate over topics
		File folder = new File(dataFolder);
		for (File clusterFolder : folder.listFiles()) {
			if (clusterFolder.isDirectory()) {

				System.out.println(clusterFolder.getName());
				BufferedReader reader = new BufferedReader(new FileReader(clusterFolder + "/" + fileName + ".arff"));
				Instances data = new Instances(reader);

				if (all == null)
					all = data;
				else {
					for (int i = 0; i < data.numInstances(); i++)
						all.add(data.instance(i));
				}

			}
		}

		System.out.println();
		System.out.println(all.toSummaryString());
		System.out.println(all.attributeStats(all.numAttributes() - 1));

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(dataFolder + "/" + fileNameMerged + ".arff"));
			writer.write(all.toString());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
