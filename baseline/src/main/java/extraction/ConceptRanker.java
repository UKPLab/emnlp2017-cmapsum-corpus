package extraction;

import java.util.List;
import java.util.Random;

import cmaps.Concept;
import cmaps.PToken;
import pipeline.ConceptMapMining;

/**
 * assign weights to concepts with different strategies
 */
public class ConceptRanker {

	private static Random rand;

	// assign weights to concepts
	public static void rankConcepts(List<Concept> concepts, RankingStrategy strategy, boolean group,
			ConceptMapMining parent, String cluster) {
		if (strategy == RankingStrategy.RANDOM)
			rand = new Random();
		if (strategy == RankingStrategy.TEXTRANK && group) {
			parent.getComponent(TextRankScorer.class).compute();
		}
		for (Concept c : concepts) {
			rankConcept(c, strategy, group, parent, cluster);
		}
	}

	// assign weight according to strategy
	private static void rankConcept(Concept c, RankingStrategy strategy, boolean group, ConceptMapMining parent,
			String cluster) {

		if (strategy == null) {
			c.weight = 0;
			return;
		}

		switch (strategy) {

		case RANDOM:
			// random weight from 0 to 1
			if (!group)
				c.weight = rand.nextDouble();
			break;

		case POS:
			// position in document, 1 for start, 0 for end
			if (!group) {
				c.weight = 0;
				for (PToken t : c.tokenLists.get(0)) {
					double pos = (t.docLength - t.start) / (float) t.docLength;
					if (pos > c.weight)
						c.weight = pos;
				}
			}
			break;

		case CF:
			// occurrences of concept
			if (!group) {
				ConceptDict cd = parent.getComponent(ConceptDict.class);
				c.weight = cd.getConceptCount(c);
			} else {
				MapBuilder mb = parent.getComponent(MapBuilder.class);
				double w = 0;
				for (Concept co : mb.getAllConcepts()) {
					Concept parConcept = mb.getConcept(co);
					if (parConcept == c)
						w += 1;
				}
				c.weight = w;
			}
			break;

		case TEXTRANK:
			// TextRank (Mihalcea 2004)
			if (group) {
				TextRankScorer tr = parent.getComponent(TextRankScorer.class);
				c.weight = tr.getConceptWeight(c);
			} else {
				rankConcept(c, RankingStrategy.CF, group, parent, cluster);
			}
			break;

		default:
			c.weight = 0;
		}

	}

	public enum RankingStrategy {
		RANDOM, POS, CF, TEXTRANK
	}

}
