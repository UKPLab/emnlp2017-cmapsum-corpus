package extraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import cmaps.Concept;
import cmaps.PToken;
import cmaps.Proposition;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import types.CC;

/**
 * extracts verbs between concepts in a sentence as potential relations
 */
public class RelationExtractorVerb extends RelationExtractor {

	// constructor
	public RelationExtractorVerb() {
		this.propositions = new LinkedList<Proposition>();
		this.nodeMap = new HashMap<Concept, Set<Proposition>>();
	}

	// sentence-based processing
	@Override
	public void processSentence(JCas jcas, Sentence sent) {
		this.findVerbs(jcas, sent);
	}

	// final collection-based processing
	@Override
	public void processCollection() {
		int size = this.propositions.size();
		this.mapLinksToMergedConcepts();
		this.parent.log(this, size + " links, " + this.propositions.size() + " after mapping");
	}

	// find linking words for pairs of concepts
	private void findVerbs(JCas jcas, Sentence sentence) {

		List<CC> concepts = JCasUtil.selectCovered(jcas, CC.class, sentence);
		if (concepts.size() >= 2) {
			for (CC c1 : concepts) {
				for (CC c2 : concepts) {
					if (c1 != c2 && c1.getEnd() < c2.getBegin()) {

						List<PToken> tokens = new ArrayList<PToken>();
						boolean hasVerb = false;
						for (Token t : JCasUtil.selectCovered(Token.class, sentence)) {
							if (t.getBegin() > c1.getEnd() && t.getEnd() < c2.getBegin()) {
								tokens.add(this.parent.getToken(t));
								if (t.getPos().getPosValue().startsWith("V"))
									hasVerb = true;
							}
						}

						if (tokens.size() > 0 && tokens.size() < 10 && hasVerb)
							this.addLink(c1, c2, tokens);
					}
				}
			}
		}
	}

	// add extracted link
	private void addLink(CC sourceConcept, CC targetConcept, List<PToken> tokens) {

		String linkingWord = "";
		for (PToken token : tokens)
			linkingWord += token.text.toLowerCase() + " ";
		linkingWord = linkingWord.replaceAll("\\p{C}", " ").replaceAll("  ", " ");

		Concept source = parent.getComponent(ConceptExtractor.class).getConcept(sourceConcept);
		Concept target = parent.getComponent(ConceptExtractor.class).getConcept(targetConcept);
		Proposition p = new Proposition(source, target, linkingWord, tokens);
		this.propositions.add(p);
	}

}
