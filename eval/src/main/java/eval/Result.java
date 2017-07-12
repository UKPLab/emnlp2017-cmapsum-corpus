package eval;

import cmaps.ConceptMap;
import eval.metrics.Metric;

/**
 * Result of comparison of two concept maps
 */
public class Result implements Comparable<Result> {

	public ConceptMap eval;
	public ConceptMap gold;
	public Metric metric;

	public double evalMatches;
	public double evalSize;
	public double goldMatches;
	public double goldSize;

	public double precision;
	public double recall;
	public double fMeasure;

	public void computePrecision() {
		this.precision = this.evalMatches / this.evalSize;
		if (Double.isNaN(this.precision))
			this.precision = 0;
	}

	public void computeRecall() {
		this.recall = this.goldMatches / this.goldSize;
		if (Double.isNaN(this.recall))
			this.recall = 0;
	}

	public void computeF1() {
		this.fMeasure = 2 * this.precision * this.recall / (this.precision + this.recall);
		if (Double.isNaN(this.fMeasure))
			this.fMeasure = 0;
	}

	@Override
	public String toString() {
		return String.format("Pr: %.4f\tRe: %.4f\tF1: %.4f", precision, recall, fMeasure);
	}

	public int compareTo(Result o) {
		if (Double.isNaN(this.fMeasure))
			return 1;
		if (Double.isNaN(o.fMeasure))
			return -1;
		return Double.compare(o.fMeasure, this.fMeasure);
	}
}
