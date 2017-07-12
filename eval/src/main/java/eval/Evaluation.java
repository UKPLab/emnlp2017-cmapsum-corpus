package eval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cmaps.ConceptMap;
import eval.metrics.Metric;

/**
 * evaluation: compares several generated concept maps against a gold map
 */
public class Evaluation {

	private String name;
	private ConceptMap goldMap;
	private List<ConceptMap> maps;
	private List<Metric> metrics;
	private List<Result> results;

	public Evaluation(String name, ConceptMap goldMap) {
		this.name = name;
		this.goldMap = goldMap;
		this.maps = new ArrayList<ConceptMap>();
		this.metrics = new ArrayList<Metric>();
	}

	public void addMetrics(List<Metric> metrics) {
		this.metrics.addAll(metrics);
	}

	public void addMetrics(Metric[] metrics) {
		this.addMetrics(Arrays.asList(metrics));
	}

	public void addConceptMaps(List<ConceptMap> maps) {
		this.maps.addAll(maps);
	}

	public void addConceptMap(ConceptMap map) {
		this.maps.add(map);
	}

	@Override
	public Evaluation clone() {
		Evaluation eval = new Evaluation(this.name, null);
		eval.addMetrics(this.metrics);
		eval.results = new LinkedList<Result>();
		return eval;
	}

	public List<Result> getResults() {
		return this.results;
	}

	// compute all metrics for given maps
	public void run() {

		this.results = new LinkedList<Result>();

		for (Metric m : this.metrics) {
			for (ConceptMap map : this.maps) {
				Result res = m.compare(map, this.goldMap);
				this.results.add(res);
			}
		}
	}

	// compute average scores for data sets
	public static List<Evaluation> getAvgResults(List<Evaluation> evaluations, boolean microAvg) {

		List<Evaluation> evals = new LinkedList<Evaluation>();
		Map<String, List<Evaluation>> map = new HashMap<String, List<Evaluation>>();

		// find datasets
		for (Evaluation eval : evaluations) {
			String key = "corpus";
			List<Evaluation> keyList = map.get(key);
			if (keyList == null) {
				keyList = new LinkedList<Evaluation>();
				map.put(key, keyList);
			}
			keyList.add(eval);
		}

		// per dataset
		for (List<Evaluation> evalCluster : map.values()) {

			// create aggregate eval
			Evaluation newEval = evalCluster.get(0).clone();
			newEval.name = "corpus average";
			newEval.name += microAvg ? " (micro)" : " (macro)";

			// iterate over configs
			for (int i = 0; i < evalCluster.get(0).results.size(); i++) {
				Result avg = new Result();

				// over maps
				for (Evaluation eval : evalCluster) {
					Result mapResult = eval.results.get(i);
					avg.eval = mapResult.eval;
					avg.metric = mapResult.metric;
					avg.evalMatches += mapResult.evalMatches;
					avg.evalSize += mapResult.evalSize;
					avg.goldMatches += mapResult.goldMatches;
					avg.goldSize += mapResult.goldSize;
					avg.precision += mapResult.precision;
					avg.recall += mapResult.recall;
					avg.fMeasure += mapResult.fMeasure;
				}

				if (microAvg) {
					// micro-average (per concept/relation)
					avg.computePrecision();
					avg.computeRecall();
					avg.computeF1();
				} else {
					// macro-average (per map)
					avg.precision /= evalCluster.size();
					avg.recall /= evalCluster.size();
					avg.fMeasure /= evalCluster.size();
				}
				newEval.results.add(avg);
			}
			evals.add(newEval);
		}

		return evals;
	}

	// return csv representation of result list
	public String getCsv() {

		char del = ';';
		StringBuffer out = new StringBuffer();

		String clusterName = this.name.substring(0, Math.max(this.name.indexOf("_"), this.name.length()));
		String name = clusterName + del + this.name + del;
		for (Result res : this.results) {
			String line = name + res.metric.getName() + del;
			line += res.eval.getName() + del;
			line += String.format("%.5f", res.precision) + del;
			line += String.format("%.5f", res.recall) + del;
			line += String.format("%.5f", res.fMeasure);
			this.addLine(out, line);
		}

		return out.toString();
	}

	// return textual representation of result list
	public String printResults() {

		StringBuffer out = new StringBuffer();

		this.addLine(out, "--------- Evaluation -----------");
		this.addLine(out, this.name);
		this.addLine(out, "");

		if (this.goldMap != null) {
			this.addLine(out, "Gold Map:");
			this.addLine(out, "(" + goldMap.size()[0] + "," + goldMap.size()[1] + ")\t" + goldMap.getName());
			this.addLine(out, "");
		}

		if (this.maps != null && !this.maps.isEmpty()) {
			this.addLine(out, "Maps:");
			for (ConceptMap map : this.maps) {
				this.addLine(out, "(" + map.size()[0] + "," + map.size()[1] + ")\t" + map.getName());
			}
			this.addLine(out, "");
		}

		this.addLine(out, "---");

		String metricName = null;
		for (Result res : this.results) {
			if (!res.metric.getName().equals(metricName)) {
				this.addLine(out, "");
				this.addLine(out, res.metric.getName());
				metricName = res.metric.getName();
			}
			this.addLine(out, " " + res + "\t" + res.eval.getName());
		}
		this.addLine(out, "");
		this.addLine(out, "---");

		return out.toString();
	}

	// add line with line break
	private void addLine(StringBuffer buf, String line) {
		buf.append(line);
		buf.append(System.lineSeparator());
	}

}
