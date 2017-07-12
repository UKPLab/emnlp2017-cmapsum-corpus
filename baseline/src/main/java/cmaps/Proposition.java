package cmaps;

import java.util.LinkedList;
import java.util.List;

/**
 * Proposition (Concept + Relation + Concept) in a concept map
 */
public class Proposition {

	public Concept sourceConcept;
	public Concept targetConcept;
	public String linkingWord;
	public List<PToken> linkingWordToken;
	public double weight;

	public Proposition(Concept source, Concept target, String link, List<PToken> linkingWordToken) {
		this.sourceConcept = source;
		this.linkingWord = link.toLowerCase().trim();
		this.targetConcept = target;
		this.linkingWordToken = linkingWordToken;
		this.weight = source.weight + target.weight;
	}

	public Proposition(Concept source, Concept target, String link) {
		this(source, target, link, new LinkedList<PToken>());
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Proposition))
			return false;
		else {
			Proposition pO = (Proposition) o;
			return this.sourceConcept == pO.sourceConcept && this.targetConcept == pO.targetConcept
					&& this.linkingWord.equals(pO.linkingWord);
		}
	}

	@Override
	public int hashCode() {
		int hash = 1;
		hash = hash * 31 + this.sourceConcept.name.hashCode();
		hash = hash * 31 + this.targetConcept.name.hashCode();
		hash = hash * 31 + this.linkingWord.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		return this.sourceConcept + " - " + this.linkingWord + " - " + this.targetConcept;
	}

}