package preprocessing;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;

/**
 * Print number of token to stdout for each document in a dataset
 * Documents have to be preprocessed first
 */
public class PipelineTokenStat {

	public static final String textFolder = "data/CMapSummaries/train";
	public static final String[] textPattern = { "*/*.bin6" };

	public static void main(String[] args) throws UIMAException, IOException {

		// read text documents
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(BinaryCasReader.class,
				BinaryCasReader.PARAM_SOURCE_LOCATION, textFolder, BinaryCasReader.PARAM_PATTERNS, textPattern,
				BinaryCasReader.PARAM_LANGUAGE, "en");

		// print statistics
		AnalysisEngineDescription stat = AnalysisEngineFactory.createEngineDescription(CorpusStatWriter.class);

		// run pipeline
		SimplePipeline.runPipeline(reader, stat);
	}

}