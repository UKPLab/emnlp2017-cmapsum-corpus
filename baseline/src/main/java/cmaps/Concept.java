package cmaps;

import java.util.LinkedList;
import java.util.List;

/**
 * Concept in a concept map
 */
public class Concept implements Comparable<Concept> {

	public String name;
	public List<List<PToken>> tokenLists;
	public double weight;

	public Concept(String name, List<PToken> tokens) {
		this.name = name.toLowerCase().trim();
		this.tokenLists = new LinkedList<List<PToken>>();
		this.tokenLists.add(tokens);
	}

	public Concept(String name) {
		this(name, new LinkedList<PToken>());
	}

	public Concept(List<PToken> tokens) {
		this("", tokens);
		for (PToken token : tokens) {
			this.name += token.text + " ";
		}
		this.name = this.name.trim().toLowerCase();
	}

	public int[] getSpan() {
		int[] span = { Integer.MAX_VALUE, -1 };
		for (PToken t : this.tokenLists.get(0)) {
			if (t.start < span[0])
				span[0] = t.start;
			if (t.end > span[1])
				span[1] = t.end;
		}
		return span;
	}

	@Override
	public String toString() {
		return this.name + " (" + this.weight + ")";
	}

	public int compareTo(Concept o) {
		int comp = Double.compare(o.weight, this.weight);
		if (comp != 0)
			return comp;
		else
			return Integer.compare(this.name.length(), o.name.length());
	}

}