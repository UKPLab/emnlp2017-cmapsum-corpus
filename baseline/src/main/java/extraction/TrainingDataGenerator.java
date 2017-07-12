package extraction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import org.apache.uima.jcas.JCas;

import cmaps.Concept;
import cmaps.ConceptMap;
import cmaps.io.ConceptMapReader;
import cmaps.io.Format;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import extraction.ConceptMerger.MergeStrategy;
import extraction.ConceptRanker.RankingStrategy;
import pipeline.ConceptMapMining;
import util.ClassifierUtils;
import util.StemSWMatch;
import weka.core.Instance;
import weka.core.Instances;

/**
 * creates training data files for WEKA for each topic
 */
public class TrainingDataGenerator extends MapBuilder {

	private File documentLocation;

	public TrainingDataGenerator() {
		super();
	}

	@Override
	public void init(ConceptMapMining parent) {
		super.init(parent);
		this.map = null;
	}

	@Override
	public void processSentence(JCas jcas, Sentence sent) {
		if (documentLocation == null) {
			try {
				DocumentMetaData meta = (DocumentMetaData) jcas.getDocumentAnnotationFs();
				File docFile = new File(new URL(meta.getDocumentUri()).getPath().replace("props", "baseline"));
				this.documentLocation = docFile.getParentFile();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
	};

	@Override
	public void processCollection() {

		File loc = new File(this.parent.getTargetLocation());
		String cluster = loc.getName();

		// prepare concepts
		concepts = this.parent.getComponent(ConceptExtractor.class).getConcepts();

		ConceptRanker.rankConcepts(concepts, RankingStrategy.CF, false, this.parent, "");
		ConceptMerger.mergeConcepts(concepts, conceptMapping, MergeStrategy.STEM_SW);
		Map<Concept, Set<Concept>> groupLookup = ClassifierUtils.buildConceptGroupingLookup(concepts, conceptMapping);
		this.parent.log(this, "concepts: " + this.concepts.size());

		ConceptDict cd = parent.getComponent(ConceptDict.class);
		TextRankScorer tr = parent.getComponent(TextRankScorer.class);
		tr.compute();

		// load gold data
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".cmap");
			}
		};
		File goldFile = new File(documentLocation.listFiles(filter)[0].getPath());
		ConceptMap mapGold = ConceptMapReader.readFromFile(goldFile, Format.TSV);

		// create data
		this.parent.log(this, "computing features");

		URL sw = getClass().getResource("lists/stopwords_en_eval.txt");
		StemSWMatch match = new StemSWMatch(sw);
		int matched = 0;

		String topicFile = documentLocation.getParent() + "/topics.tsv";
		String clusterSizeFile = documentLocation.getParent() + "/cluster_size.txt";
		ClassifierUtils util = new ClassifierUtils(clusterSizeFile, topicFile);

		Instances data = util.createEmptyDataset("ConceptSelectionTrain");
		for (Concept c : concepts) {

			// label
			boolean isGold = false;
			for (Concept cg : mapGold.getConcepts()) {
				if (match.isMatch(cg.name, c.name)) {
					isGold = true;
					matched++;
					break;
				}
			}

			Instance instance = util.createInstance(c, isGold, cd, tr, cluster, groupLookup);
			data.add(instance);
		}

		try {
			BufferedWriter writer = new BufferedWriter(
					new FileWriter(parent.getTargetLocation() + "/" + parent.getName() + ".arff"));
			writer.write(data.toString());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.parent.log(this,
				"arff-file created: " + data.numInstances() + ", " + data.numAttributes() + ", " + data.numClasses());
		this.parent.log(this, "positive instances: " + matched);
	}

}