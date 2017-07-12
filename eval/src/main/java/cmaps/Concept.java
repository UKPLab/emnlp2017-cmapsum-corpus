package cmaps;

/**
 * concept in a concept map
 */
public class Concept {

	public String name;

	public Concept(String name) {
		this.name = name.toLowerCase().trim();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Concept))
			return false;
		else
			return this.name.equals(((Concept) o).name);

	};

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public String toString() {
		return this.name;
	}

}