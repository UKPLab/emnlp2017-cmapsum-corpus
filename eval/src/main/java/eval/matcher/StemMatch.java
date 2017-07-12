package eval.matcher;

import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM;

/**
 * exact string match after stemming of each token
 */
public class StemMatch extends ExactMatch {

	private SnowballStemmer stemmer;

	public StemMatch() {
		super();
		this.name = "Stem Match";
		this.stemmer = new SnowballStemmer(ALGORITHM.ENGLISH);
	}

	@Override
	public boolean isMatch(String s1, String s2) {
		CharSequence stemmedS1 = stem(clean(s1));
		CharSequence stemmedS2 = stem(clean(s2));
		return stemmedS1.equals(stemmedS2);
	}

	protected String stem(String s) {
		String stemmed = "";
		for (String token : s.split(" ")) {
			stemmed += this.stemmer.stem(token) + " ";
		}
		return stemmed.trim();
	}

}
