package extraction;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cmaps.Concept;
import cmaps.Proposition;

/**
 * Base class for relation extraction components
 */
public abstract class RelationExtractor extends CmmComponent {

	protected List<Proposition> propositions;
	protected Map<Concept, Set<Proposition>> nodeMap;

	// return extracted propositions
	public List<Proposition> getPropositions() {
		return this.propositions;
	}

	// return propositions for given concept
	public Set<Proposition> getPropositions(Concept c) {
		Set<Proposition> props = this.nodeMap.get(c);
		if (props == null) {
			return new HashSet<Proposition>();
		} else {
			return props;
		}
	}

	// map link candidates to final concepts
	protected void mapLinksToMergedConcepts() {
		ConceptExtractor ce = this.parent.getComponent(ConceptExtractor.class);
		List<Proposition> remove = new LinkedList<Proposition>();
		for (Proposition p : this.propositions) {
			p.sourceConcept = ce.getConcept(p.sourceConcept);
			p.targetConcept = ce.getConcept(p.targetConcept);
			if (p.sourceConcept == p.targetConcept) {
				remove.add(p);
			} else {
				Set<Proposition> cP = this.nodeMap.get(p.sourceConcept);
				if (cP == null) {
					cP = new HashSet<Proposition>();
					this.nodeMap.put(p.sourceConcept, cP);
				}
				cP.add(p);
				p.weight = p.sourceConcept.weight + p.targetConcept.weight;
			}
		}
		this.propositions.clear();
		for (Set<Proposition> props : this.nodeMap.values()) {
			this.propositions.addAll(props);
		}
	}
}
