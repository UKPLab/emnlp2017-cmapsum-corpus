package cmaps;

/**
 * proposition in a concept map
 */
public class Proposition {

	public Concept sourceConcept;
	public Concept targetConcept;
	public String relationPhrase;

	public Proposition(Concept source, Concept target, String link) {
		this.sourceConcept = source;
		this.relationPhrase = link.toLowerCase().trim();
		this.targetConcept = target;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Proposition))
			return false;
		else {
			Proposition pO = (Proposition) o;
			return this.sourceConcept == pO.sourceConcept && this.targetConcept == pO.targetConcept
					&& this.relationPhrase == pO.relationPhrase;
		}
	}

	@Override
	public int hashCode() {
		int hash = 1;
		hash = hash * 31 + this.sourceConcept.name.hashCode();
		hash = hash * 31 + this.targetConcept.name.hashCode();
		hash = hash * 31 + this.relationPhrase.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		return this.sourceConcept + " - " + this.relationPhrase + " - " + this.targetConcept;
	}
}