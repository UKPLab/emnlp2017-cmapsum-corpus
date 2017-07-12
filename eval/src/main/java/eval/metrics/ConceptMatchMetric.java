package eval.metrics;

import cmaps.Concept;
import cmaps.ConceptMap;
import eval.Result;
import eval.matcher.Match;
import util.CountedSet;

/**
 * Compares concepts of two concept maps
 */
public class ConceptMatchMetric extends Metric {

	public ConceptMatchMetric(Match match) {
		super(match);
		this.name = "Concept";
	}

	@Override
	public Result compare(ConceptMap evalMap, ConceptMap goldMap) {

		CountedSet<String> evalConcepts = new CountedSet<String>();
		for (Concept c : evalMap.getConcepts()) {
			evalConcepts.add(c.name.toLowerCase());
		}
		evalConcepts.setAllCounts(1);

		CountedSet<String> goldConcepts = new CountedSet<String>();
		for (Concept c : goldMap.getConcepts()) {
			goldConcepts.add(c.name.toLowerCase());
		}
		goldConcepts.setAllCounts(1);

		Result res = this.createResult(evalMap, goldMap);
		return this.compareSets(res, evalConcepts, goldConcepts);
	}

}
