package extraction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cmaps.Concept;
import cmaps.PToken;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM;

/**
 * merges extracted concepts into single concepts if their labels match
 */
public class ConceptMerger {

	private static SnowballStemmer stemmer = new SnowballStemmer(ALGORITHM.ENGLISH);

	// merge extracted concepts
	public static void mergeConcepts(List<Concept> concepts, Map<Concept, Concept> conceptMapping,
			MergeStrategy strategy) {

		// find groups and max per group
		Map<Concept, String> lookupConceptGroup = new HashMap<Concept, String>();
		Map<String, Concept> lookupGroupMax = new HashMap<String, Concept>();

		for (Concept c : concepts) {
			String label = getLabel(strategy, c);
			lookupConceptGroup.put(c, label);
			Concept cm = lookupGroupMax.get(label);
			if (cm == null)
				lookupGroupMax.put(label, c);
			else {
				if (c.compareTo(cm) < 0) {
					lookupGroupMax.put(label, c);
				}
			}
		}

		// create mapping to max
		conceptMapping.clear();
		for (Concept c : concepts) {
			conceptMapping.put(c, lookupGroupMax.get(lookupConceptGroup.get(c)));
		}
		concepts.clear();
		concepts.addAll(lookupGroupMax.values());

	}

	private static String getLabel(MergeStrategy strategy, Concept c) {
		switch (strategy) {
		case LABEL:
			return label(c);
		case STEM:
			return stem(c);
		case STEM_SW:
			return stem_sw(c);
		default:
			return label(c);
		}
	}

	// merge criterion: same label
	public static String label(Concept c) {
		return c.name.replaceAll("[^\\p{Alnum} ]", " ").replaceAll("\\s\\s+", " ").trim();
	}

	// merge criterion: same stemmed label
	public static String stem(Concept c) {
		return stem(label(c));
	}

	public static String stem(String s) {
		String stemmed = "";
		for (String token : s.split(" ")) {
			stemmed += stemmer.stem(token) + " ";
		}
		return stemmed.trim();
	}

	// merge criterion: same label after stemming, cleaning and removal of
	// determiners
	public static String stem_sw(Concept c) throws RuntimeException {
		if (c.tokenLists.size() == 0 || c.tokenLists.get(0).isEmpty())
			throw new RuntimeException();
		String clean = "";
		for (PToken t : c.tokenLists.get(0)) {
			if (!t.pos.startsWith("D") && !t.pos.equals("POS")
					&& !Arrays.asList(tokenIgnore).contains(t.text.toLowerCase()))
				clean += stemmer.stem(t.text.toLowerCase()) + " ";
		}
		clean = clean.replaceAll("[^\\p{Alnum} ]", " ").replaceAll("\\s\\s+", " ");
		return clean.trim();
	}

	// list of determiners to remove (to handle pos-tagging errors)
	public static String[] tokenIgnore = { "many", "most", "that", "a", "an", "the", "any", "each", "every", "this",
			"that", "these", "those", "all" };

	public enum MergeStrategy {
		LABEL, STEM, STEM_SW
	}

}
