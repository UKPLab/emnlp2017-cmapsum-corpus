package extraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cmaps.Concept;
import cmaps.ConceptMap;
import pipeline.ConceptMapMining;

/**
 * Base class for concept map construction
 */
public abstract class MapBuilder extends CmmComponent {

	protected ConceptMap map;
	protected Map<Concept, Concept> conceptMapping;
	protected List<Concept> concepts;

	protected ConceptExtractor ce;
	protected RelationExtractor le;

	public MapBuilder() {
		this.conceptMapping = new HashMap<Concept, Concept>();
	}

	public void init(ConceptMapMining parent) {
		this.parent = parent;
		this.ce = this.parent.getComponent(ConceptExtractor.class);
		this.le = this.parent.getComponent(RelationExtractor.class);
		this.map = new ConceptMap(this.parent.getName());
	}

	@Override
	public ConceptMap getConceptMap() {
		return this.map;
	}

	public Concept getConcept(Concept c) {
		return this.conceptMapping.get(c);
	}

	public List<Concept> getConcepts() {
		return this.concepts;
	}

	public List<Concept> getAllConcepts() {
		List<Concept> cs = new ArrayList<Concept>(conceptMapping.keySet());
		return cs;
	}

}
