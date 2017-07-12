package extraction;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import cmaps.Concept;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import util.CountedSet;

/**
 * dictionary counting occurrences of extracted concepts
 */
public class ConceptDict extends CmmComponent {

	private CountedSet<String> concepts;
	private double length;

	// constructor
	public ConceptDict() {
		this.concepts = new CountedSet<String>();
	}

	@Override
	public void processSentence(JCas jcas, Sentence sent) {
		this.length += JCasUtil.selectCovered(Token.class, sent).size();
	};

	// build dictionary
	@Override
	public void processCollection() {
		ConceptExtractor ce = this.parent.getComponent(ConceptExtractor.class);
		for (Concept c : ce.getConcepts()) {
			this.concepts.add(c.name);
		}
	}

	// returns occurrences for a given concept
	public int getConceptCount(Concept concept) {
		return this.concepts.getCount(concept.name);
	}

	// returns occurrences for a given concept relative to cluster size
	public double getRelCount_Token(Concept concept) {
		return this.concepts.getCount(concept.name) / this.length;
	}

	// returns occurrences for a given concept relative to the total number of
	// concepts
	public double getRelCount_Concepts(Concept concept) {
		return this.concepts.getCount(concept.name) / this.concepts.sum();
	}

}
