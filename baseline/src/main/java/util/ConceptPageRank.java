package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cmaps.Concept;
import de.tudarmstadt.ukp.dkpro.keyphrases.textgraphs.util.PageRank;
import edu.stanford.nlp.util.Pair;
import no.uib.cipr.matrix.sparse.FlexCompColMatrix;

/**
 * Apply Page Rank for concepts, used with TextRankScorer
 */
public class ConceptPageRank extends PageRank {

	public void initializeFromConceptPairs(List<Pair<Concept, Concept>> pairs, boolean counted) {

		this.termMap = new HashMap<String, Integer>();
		this.termRanks = new ArrayList<TermRank>();

		// first, we have to get all terms
		int termId = 0;
		for (Pair<Concept, Concept> pair : pairs) {

			String term1 = pair.first().name;
			String term2 = pair.second().name;

			// adds term if not already in map, and increases termId
			termId = this.updateTermMap(term1, termId, pair.first().getSpan()[0], pair.first().getSpan()[1]);
			termId = this.updateTermMap(term2, termId, pair.second().getSpan()[0], pair.second().getSpan()[1]);
		}

		// now, initialize the adjacency matrix
		this.adjacentMatrix = new FlexCompColMatrix(termMap.size(), termMap.size());

		for (Pair<Concept, Concept> pair : pairs) {

			String term1 = pair.first().name;
			String term2 = pair.second().name;

			int termId1 = this.termMap.get(term1);
			int termId2 = this.termMap.get(term2);

			// undirected, weighted by count if flag is set
			if (termId1 != termId2) {
				double weight = 1.0;
				if (counted)
					weight = this.adjacentMatrix.get(termId1, termId2) + 1;
				this.adjacentMatrix.set(termId1, termId2, weight);
				this.adjacentMatrix.set(termId2, termId1, weight);
			}
		}

	}

}
