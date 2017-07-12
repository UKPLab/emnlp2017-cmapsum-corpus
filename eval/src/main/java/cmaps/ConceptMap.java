package cmaps;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * concept map data structure
 */
public class ConceptMap {

	private String name;
	private Set<Proposition> propositions;
	private Map<Concept, Set<Proposition>> nodeMap;

	public ConceptMap(String name) {
		this.name = name;
		this.propositions = new HashSet<Proposition>();
		this.nodeMap = new HashMap<Concept, Set<Proposition>>();
	}

	public String getName() {
		return this.name;
	}

	public void addConcept(Concept c) {
		if (!this.nodeMap.containsKey(c)) {
			this.nodeMap.put(c, new HashSet<Proposition>());
		}
	}

	public void addConcept(Collection<Concept> cs) {
		for (Concept c : cs) {
			this.addConcept(c);
		}
	}

	public void addProposition(Proposition p) {
		if (this.propositions.contains(p))
			return;
		if (!this.nodeMap.containsKey(p.sourceConcept))
			throw new IllegalArgumentException("Unknown concept: " + p.sourceConcept);
		if (!this.nodeMap.containsKey(p.targetConcept))
			throw new IllegalArgumentException("Unknown concept: " + p.targetConcept);
		this.propositions.add(p);
		this.nodeMap.get(p.sourceConcept).add(p);
	}

	public void addProposition(Collection<Proposition> ps) {
		for (Proposition p : ps) {
			this.addProposition(p);
		}
	}

	public void removeProposition(Proposition p) {
		this.propositions.remove(p);
		this.nodeMap.get(p.sourceConcept).remove(p);
	}

	public void removeProposition(Collection<Proposition> ps) {
		for (Proposition p : ps) {
			this.removeProposition(p);
		}
	}

	public Set<Proposition> getProps() {
		return this.propositions;
	}

	public Set<Proposition> getPropsUndirected(Concept c) {
		Set<Proposition> props = new HashSet<Proposition>();
		for (Proposition p : this.propositions) {
			if (p.sourceConcept == c || p.targetConcept == c)
				props.add(p);
		}
		return props;
	}

	public Set<Concept> getConcepts() {
		return this.nodeMap.keySet();
	}

	public int[] size() {
		int[] size = new int[2];
		size[0] = this.nodeMap.keySet().size();
		size[1] = this.propositions.size();
		return size;
	}

	// check if the graph is connected
	public boolean isConnected() {

		Queue<Concept> queue = new LinkedList<Concept>();
		Set<Concept> visited = new HashSet<Concept>();
		queue.add(this.getConcepts().iterator().next());

		while (!queue.isEmpty()) {
			Concept c = queue.poll();
			visited.add(c);
			for (Proposition p : this.getPropsUndirected(c)) {
				if (!visited.contains(p.sourceConcept))
					queue.add(p.sourceConcept);
				if (!visited.contains(p.targetConcept))
					queue.add(p.targetConcept);
			}
		}

		return visited.size() == this.getConcepts().size();
	}

	// check if a pair of concepts has more than one edge
	public Proposition hasDoubleEdge() {
		for (Proposition p : this.propositions) {
			for (Proposition p2 : this.propositions) {
				if (p != p2 && p.sourceConcept == p2.sourceConcept && p.targetConcept == p2.targetConcept)
					return p2;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		res.append(this.name);
		res.append(" (" + this.nodeMap.keySet().size() + "," + this.propositions.size() + ")\n");
		for (Proposition p : this.propositions) {
			res.append(" " + p + "\n");
		}
		return res.toString();
	}
}