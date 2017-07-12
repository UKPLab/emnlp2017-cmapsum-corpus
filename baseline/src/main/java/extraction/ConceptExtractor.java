package extraction;

import java.util.List;

import cmaps.Concept;
import types.CC;

/**
 * Base for concept extractor implementations
 */
public abstract class ConceptExtractor extends CmmComponent {

	protected List<Concept> concepts;

	// return extracted concepts
	public List<Concept> getConcepts() {
		return this.concepts;
	}

	// return main concept for merged concept
	public Concept getConcept(Concept c) {
		return null;
	}

	// return concept for concept annotation
	public Concept getConcept(CC annotation) {
		return null;
	}

}
