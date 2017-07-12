package eval.metrics;

import cmaps.ConceptMap;
import cmaps.Proposition;
import eval.Result;
import eval.matcher.Match;
import util.CountedSet;

/**
 * Compares propositions of two concept maps
 * 
 * Proposition (labeled): sourceConceptLabel relationLabel targetConceptLabel
 * Proposition (unlabeled): sourceConceptLabel targetConceptLabel
 */
public class PropositionMatchMetric extends Metric {

	protected boolean labeled;

	public PropositionMatchMetric(Match match, boolean labeled) {
		super(match);
		this.labeled = labeled;
		String label = this.labeled ? "(labeled)" : "(unlabeled)";
		this.name = "Proposition " + label;
	}

	@Override
	public Result compare(ConceptMap evalMap, ConceptMap goldMap) {

		CountedSet<String> evalProps = this.getPropositionStringSet(evalMap);
		evalProps.setAllCounts(1);
		CountedSet<String> goldProps = this.getPropositionStringSet(goldMap);
		goldProps.setAllCounts(1);

		Result res = this.createResult(evalMap, goldMap);
		return this.compareSets(res, evalProps, goldProps);
	}

	protected CountedSet<String> getPropositionStringSet(ConceptMap map) {
		CountedSet<String> set = new CountedSet<String>();
		for (Proposition p : map.getProps()) {
			set.add(getPropositionString(p));
		}
		return set;
	}

	protected String getPropositionString(Proposition p) {
		String s = p.sourceConcept.name.toLowerCase() + " ";
		if (labeled && p.relationPhrase.trim().length() > 0)
			s += p.relationPhrase.toLowerCase() + " ";
		s += p.targetConcept.name.toLowerCase();
		return s;
	}
}
