package util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM;

/**
 * String matching after stemming and stopword removal
 */
public class StemSWMatch {

	protected SnowballStemmer stemmer;
	protected Set<String> stopwords;

	public StemSWMatch(URL stopwordFile) {
		this.stemmer = new SnowballStemmer(ALGORITHM.ENGLISH);
		this.stopwords = new HashSet<String>();
		if (stopwordFile != null) {
			try {
				for (String word : FileUtils.readLines(new File(stopwordFile.getPath()))) {
					this.stopwords.add(word);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isMatch(String s1, String s2) {
		CharSequence stemmedS1 = stem(clean(s1));
		CharSequence stemmedS2 = stem(clean(s2));
		if (stemmedS1.length() == 0 || stemmedS2.length() == 0)
			return false;
		return stemmedS1.equals(stemmedS2);
	}

	private String clean(String s) {
		String c = s.toLowerCase().replaceAll("[^\\p{Alnum} ]", " ").replaceAll("\\s\\s+", " ");
		String[] words = c.split("\\s+");
		c = "";
		for (String w : words)
			if (!this.stopwords.contains(w))
				c += w + " ";
		return c.trim();
	}

	private String stem(String s) {
		String stemmed = "";
		for (String token : s.split(" ")) {
			stemmed += this.stemmer.stem(token) + " ";
		}
		return stemmed.trim();
	}

}
