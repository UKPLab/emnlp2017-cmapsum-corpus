package extraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import cmaps.Concept;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.keyphrases.textgraphs.util.PageRank.TermRank;
import edu.stanford.nlp.util.Pair;
import types.CC;
import util.ConceptPageRank;

/**
 * component that computes text rank scores for concepts based on co-occurrence
 */
public class TextRankScorer extends CmmComponent {

	public static final int windowSize = 3;
	public static final boolean counted = true;

	private Queue<Concept> lastConcepts;
	private List<Pair<Concept, Concept>> pairs;
	private ConceptPageRank textRank;
	private Map<String, Double> scores;
	private boolean computed;

	public TextRankScorer() {
		this.lastConcepts = new LinkedList<Concept>();
		this.pairs = new ArrayList<Pair<Concept, Concept>>();
		this.textRank = new ConceptPageRank();
		this.scores = new HashMap<String, Double>();
		this.computed = false;
	}

	// returns score for a concept
	public double getConceptWeight(Concept c) {
		Double s = this.scores.get(c.name);
		if (s == null) {
			System.err.println("Missing concept: " + c);
			// filtering -> might remove all neighbours
			return 0;
		} else
			return s;
	}

	// run text rank on the resulting graph to get scores
	public void compute() {
		if (!computed) {

			MapBuilder mb = this.parent.getComponent(MapBuilder.class);
			ArrayList<Pair<Concept, Concept>> mappedPairs = new ArrayList<Pair<Concept, Concept>>();
			for (Pair<Concept, Concept> pair : pairs) {
				Concept c1 = mb.getConcept(pair.first());
				Concept c2 = mb.getConcept(pair.second());
				if (c1 != null && c2 != null)
					mappedPairs.add(new Pair<Concept, Concept>(c1, c2));
			}

			this.textRank.initializeFromConceptPairs(mappedPairs, counted);
			this.textRank.run();
			List<TermRank> termRanks = this.textRank.getTermRanks();
			for (TermRank termRank : termRanks) {
				this.scores.put(termRank.getStringRepresentation(), termRank.getScore());
			}

			computed = true;
		}
	}

	// collect pairs of co-occurring concept candidates
	@Override
	public void processSentence(JCas jcas, Sentence sent) {

		for (CC ca : JCasUtil.selectCovered(jcas, CC.class, sent)) {
			Concept c = this.parent.getComponent(ConceptExtractor.class).getConcept(ca);
			if (c != null) {
				for (Concept cn : this.lastConcepts) {
					this.pairs.add(new Pair<Concept, Concept>(cn, c));
				}
				this.lastConcepts.offer(c);
				if (this.lastConcepts.size() > windowSize)
					this.lastConcepts.poll();
			}
		}

	}

	@Override
	public boolean delaySentenceProcessing() {
		return true;
	}

}
