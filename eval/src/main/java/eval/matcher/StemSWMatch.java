package eval.matcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/**
 * exact string match after stemming of each token, ignoring certain tokens
 */
public class StemSWMatch extends StemMatch {

	protected Set<String> stopwords;

	public StemSWMatch(InputStream sw) {
		super();
		this.name = "Stem SW Match";
		this.stopwords = new HashSet<String>();
		if (sw != null) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(sw));
				String word = null;
				while ((word = reader.readLine()) != null) {
					this.stopwords.add(word);
				}
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean isMatch(String s1, String s2) {
		CharSequence stemmedS1 = stem(filter(clean(s1)));
		CharSequence stemmedS2 = stem(filter(clean(s2)));
		if (stemmedS1.length() == 0 || stemmedS2.length() == 0)
			return false;
		return stemmedS1.equals(stemmedS2);
	}

	protected String filter(String s) {
		String[] words = s.split("\\s+");
		String f = "";
		for (String w : words)
			if (!this.stopwords.contains(w))
				f += w + " ";
		return f.trim();
	}

}
