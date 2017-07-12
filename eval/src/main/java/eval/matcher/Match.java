package eval.matcher;

/**
 * Abstract base class for a match
 */
public abstract class Match {

	protected String name;

	public String getName() {
		return this.name;
	}

	/**
	 * determines if two elements match
	 * 
	 * @param s1
	 *            element 1
	 * @param s2
	 *            element 2
	 * @return true, if they match, false otherwise
	 */
	public abstract boolean isMatch(String s1, String s2);

}
