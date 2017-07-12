package eval.matcher;

/**
 * exact string match
 */
public class ExactMatch extends Match {

	public ExactMatch() {
		this.name = "Exact Match";
	}

	@Override
	public boolean isMatch(String s1, String s2) {
		return clean(s1).equals(clean(s2));
	}

	protected String clean(String s) {
		String c = s.replace("[^\\p{Alnum}]", "").replaceAll("\\s\\s+", " ");
		return c;
	}

}
