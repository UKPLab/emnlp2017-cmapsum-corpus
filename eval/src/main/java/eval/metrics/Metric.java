package eval.metrics;

import cmaps.ConceptMap;
import eval.Result;
import eval.matcher.Match;
import util.CountedSet;

/**
 * Abstract class for comparison metrics
 */
public abstract class Metric {

	protected String name;
	protected Match match;

	public Metric(Match match) {
		this.match = match;
	}

	// to be implemented by specific metric
	public abstract Result compare(ConceptMap evalMap, ConceptMap goldMap);

	// computes matches between two sets of strings
	// (counts are ignored for now)
	protected Result compareSets(Result res, CountedSet<String> eval, CountedSet<String> gold) {

		res.evalSize = eval.sum();
		res.evalMatches = 0;
		for (String se : eval) {
			for (String sg : gold) {
				if (match.isMatch(sg, se)) {
					res.evalMatches++;
					break;
				}
			}
		}

		res.goldSize = gold.sum();
		res.goldMatches = 0;
		for (String sg : gold) {
			for (String se : eval) {
				if (match.isMatch(sg, se)) {
					res.goldMatches++;
					break;
				}
			}
		}

		res.computePrecision();
		res.computeRecall();
		res.computeF1();

		return res;
	}

	// prepare result container
	protected Result createResult(ConceptMap evalMap, ConceptMap goldMap) {
		Result res = new Result();
		res.eval = evalMap;
		res.gold = goldMap;
		res.metric = this;
		return res;
	}

	// determine metric name
	public String getName() {
		String name = this.name;
		if (this.match != null)
			name += " " + this.match.getName();
		return name;
	}
}
