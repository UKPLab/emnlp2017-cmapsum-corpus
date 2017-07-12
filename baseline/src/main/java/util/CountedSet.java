package util;

/**
 * Counted set with utility functions
 */
public class CountedSet<T> extends opennlp.tools.util.CountedSet<T> {

	public double sum() {
		int sum = 0;
		for (T o : this) {
			sum += this.getCount(o);
		}
		return sum;
	}

	public void setAllCounts(int c) {
		for (T o : this) {
			this.setCount(o, c);
		}
	}

	@Override
	public String toString() {
		String out = "[";
		for (T o : this) {
			out += o + ":" + this.getCount(o) + ", ";
		}
		if (out.length() > 2) {
			out = out.substring(0, out.length() - 2);
		}
		return out + "]";
	}

}
