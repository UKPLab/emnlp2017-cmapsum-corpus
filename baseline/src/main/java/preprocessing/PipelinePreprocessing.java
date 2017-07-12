package preprocessing;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasWriter;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.snowball.SnowballStemmer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser.DependenciesMode;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;

/**
 * Preprocessing Pipeline
 * 
 * Applies the following preprocessing tools to all input documents and stores
 * resulting annotations in binary UIMA CAS files for further processing.
 * 
 * - Tokenization and Sentence Splitting
 * - Part of Speech Tagging
 * - Lemmatization
 * - Stemming
 * - Constituency Parsing
 */
public class PipelinePreprocessing {

	// dataset to be processed
	public static final String textFolder = "data/CMapSummaries/train";
	public static final String[] textPattern = { "*/*.txt" };

	public static void main(String[] args) throws UIMAException, IOException {

		// read text documents
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class,
				TextReader.PARAM_SOURCE_LOCATION, textFolder, TextReader.PARAM_PATTERNS, textPattern,
				TextReader.PARAM_LANGUAGE, "en");

		// preprocess documents
		String[] quoteBegin = { "“", "‘" };
		List<String> quoteBeginList = Arrays.asList(quoteBegin);
		String[] quoteEnd = { "”", "’" };
		List<String> quoteEndList = Arrays.asList(quoteEnd);

		AnalysisEngineDescription segmenter = AnalysisEngineFactory.createEngineDescription(StanfordSegmenter.class);
		AnalysisEngineDescription pos = AnalysisEngineFactory.createEngineDescription(StanfordPosTagger.class,
				StanfordPosTagger.PARAM_QUOTE_BEGIN, quoteBeginList, StanfordPosTagger.PARAM_QUOTE_END, quoteEndList);
		AnalysisEngineDescription lemmatizer = AnalysisEngineFactory.createEngineDescription(StanfordLemmatizer.class);
		AnalysisEngineDescription stemmer = AnalysisEngineFactory.createEngineDescription(SnowballStemmer.class,
				SnowballStemmer.PARAM_LOWER_CASE, true);
		AnalysisEngineDescription parser = AnalysisEngineFactory.createEngineDescription(StanfordParser.class,
				StanfordParser.PARAM_MODEL_LOCATION, "lib/englishRNN.ser", StanfordParser.PARAM_MODE,
				DependenciesMode.CC_PROPAGATED, StanfordPosTagger.PARAM_QUOTE_BEGIN, quoteBeginList,
				StanfordPosTagger.PARAM_QUOTE_END, quoteEndList);

		// write annotated data to file
		AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(BinaryCasWriter.class,
				BinaryCasWriter.PARAM_TARGET_LOCATION, textFolder, BinaryCasWriter.PARAM_STRIP_EXTENSION, false,
				BinaryCasWriter.PARAM_FILENAME_EXTENSION, ".bin6", BinaryCasWriter.PARAM_OVERWRITE, true);

		// print statistics
		AnalysisEngineDescription stat = AnalysisEngineFactory.createEngineDescription(CorpusStatWriter.class);

		// run pipeline
		SimplePipeline.runPipeline(reader, segmenter, pos, lemmatizer, stemmer, parser, writer, stat);
	}

}