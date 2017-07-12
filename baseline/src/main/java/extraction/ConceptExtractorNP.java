package extraction;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import cmaps.Concept;
import cmaps.PToken;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import types.CC;

/**
 * Extracts noun phrases as candidate concepts
 */
public class ConceptExtractorNP extends ConceptExtractor {

	private Map<CC, Concept> anno2Concept;

	// constructor
	public ConceptExtractorNP() {
		this.concepts = new LinkedList<Concept>();
		this.anno2Concept = new HashMap<CC, Concept>();
	}

	// sentence-based processing
	@Override
	public void processSentence(JCas jcas, Sentence sent) {
		this.extractCandidateConcepts(jcas, sent);
	}

	// extract candidate concepts finding noun phrases in parse
	private void extractCandidateConcepts(JCas jcas, Sentence sentence) {

		// all NPs
		for (Constituent constituent : JCasUtil.selectCovered(jcas, Constituent.class, sentence)) {
			if (constituent.getConstituentType().equals("NP")) {

				Collection<Token> token = JCasUtil.selectCovered(jcas, Token.class, constituent);
				List<PToken> toks = new LinkedList<PToken>();
				boolean hasNoun = false;
				for (Token t : token) {
					if (t.getPos().getPosValue().startsWith("N"))
						hasNoun = true;
					toks.add(this.parent.getToken(t));
				}

				if (toks.size() > 0 && hasNoun) {
					String text = constituent.getCoveredText().replaceAll("\\p{C}", " ").replaceAll("  ", " ");
					Concept c = new Concept(text, toks);
					this.concepts.add(c);
					CC annotation = new CC(jcas);
					annotation.setBegin(constituent.getBegin());
					annotation.setEnd(constituent.getEnd());
					annotation.setParent(constituent);
					annotation.addToIndexes();
					this.anno2Concept.put(annotation, c);
				}
			}
		}
	}

	// return concept for concept annotation
	@Override
	public Concept getConcept(CC annotation) {
		return this.anno2Concept.get(annotation);
	}

	// return main concept for merged concept
	@Override
	public Concept getConcept(Concept c) {
		return c; // dummy, merging is done later
	}

}
