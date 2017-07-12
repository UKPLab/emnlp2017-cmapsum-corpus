package util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;

import cmaps.Concept;
import cmaps.PToken;
import extraction.ConceptDict;
import extraction.TextRankScorer;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * utilities functions to create WEKA instances for classifying potential
 * concepts into important and non-important ones
 */
public class ClassifierUtils {

	private FastVector labels;
	private static Stopwords sw = new Stopwords();
	private Map<String, Integer> clusterSize;
	private HashMap<String, String[]> topics;

	// active features
	private static boolean[] active = { true, true, true, true, true, true, true, true, true, true, true, true, true,
			true, true, true, true, true, true, true, false };

	// constructor
	public ClassifierUtils(String clusterSizeFile, String topicsFile) {

		this.labels = new FastVector();
		this.labels.addElement("incorrect"); // not in summary
		this.labels.addElement("correct"); // in summary

		try {
			List<String> lines = FileUtils.readLines(new File(topicsFile), Charsets.UTF_8);
			this.topics = new HashMap<String, String[]>();
			for (String line : lines) {
				String[] cols = line.split("\t");
				String[] token = cols[1].split("\\s+");
				this.topics.put(cols[0], token);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			List<String> lines = FileUtils.readLines(new File(clusterSizeFile), Charsets.UTF_8);
			this.clusterSize = new HashMap<String, Integer>();
			for (String line : lines) {
				String[] cols = line.split("\t");
				this.clusterSize.put(cols[0], Integer.parseInt(cols[1]));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// creates an empty dataset with attributes defined
	public Instances createEmptyDataset(String name) {

		FastVector atts = new FastVector();
		if (active[0])
			atts.addElement(new Attribute("freq_abs"));
		if (active[1])
			atts.addElement(new Attribute("freq_relc"));
		if (active[2])
			atts.addElement(new Attribute("freq_relt"));
		if (active[3])
			atts.addElement(new Attribute("textrank"));

		if (active[4])
			atts.addElement(new Attribute("pos_max"));
		if (active[5])
			atts.addElement(new Attribute("pos_min"));
		if (active[6])
			atts.addElement(new Attribute("pos_spread"));
		if (active[7])
			atts.addElement(new Attribute("pos_3sent"));

		if (active[8])
			atts.addElement(new Attribute("topic_match_abs"));
		if (active[9])
			atts.addElement(new Attribute("topic_match_rel"));

		if (active[10])
			atts.addElement(new Attribute("length_tok"));
		if (active[11])
			atts.addElement(new Attribute("length_char"));
		if (active[12])
			atts.addElement(new Attribute("stopwords"));

		if (active[13])
			atts.addElement(new Attribute("noun_count_abs"));
		if (active[14])
			atts.addElement(new Attribute("noun_count_rel"));
		if (active[15])
			atts.addElement(new Attribute("noun_last"));
		if (active[16])
			atts.addElement(new Attribute("adj_count_abs"));
		if (active[17])
			atts.addElement(new Attribute("adj_count_rel"));
		if (active[18])
			atts.addElement(new Attribute("punct"));

		if (active[19])
			atts.addElement(new Attribute("doc_freq"));
		if (active[20])
			atts.addElement(new Attribute("keyphraseness"));

		atts.addElement(new Attribute("label", this.labels));

		Instances data = new Instances(name, atts, 0);
		data.setClassIndex(data.numAttributes() - 1);

		return data;
	}

	// creates a single instances with all features for a potential concept
	public Instance createInstance(Concept c, boolean isCorrect, ConceptDict cd, TextRankScorer tr, String cluster,
			Map<Concept, Set<Concept>> groupLookup) {

		List<Double> vals = new ArrayList<Double>();

		// freq
		if (active[0])
			vals.add((double) cd.getConceptCount(c));
		if (active[1])
			vals.add(cd.getRelCount_Concepts(c));
		if (active[2])
			vals.add(cd.getRelCount_Token(c));
		if (active[3])
			vals.add(tr.getConceptWeight(c));

		// position
		List<Double> positions = new ArrayList<Double>();
		if (active[4] || active[5] || active[6]) {
			for (Concept co : groupLookup.get(c)) {
				PToken firstToken = co.tokenLists.get(0).get(0);
				positions.add((firstToken.docLength - firstToken.start) / (double) firstToken.docLength);
			}
		}
		if (active[4])
			vals.add(Collections.max(positions));
		if (active[5])
			vals.add(Collections.min(positions));
		if (active[6])
			vals.add(Collections.max(positions) - Collections.min(positions));
		if (active[7]) {
			if (Collections.max(positions) >= ((2420 - 3 * 20) / 2420.0))
				vals.add(1.0);
			else
				vals.add(0.0);
		}

		// topic match
		if (active[8])
			vals.add(this.getTopicOverlap(c, cluster, false));
		if (active[9])
			vals.add(this.getTopicOverlap(c, cluster, true));

		// name
		if (active[10])
			vals.add((double) c.tokenLists.get(0).size());
		if (active[11])
			vals.add((double) c.name.length());
		if (active[12]) {
			double frac = 0;
			for (PToken t : c.tokenLists.get(0)) {
				if (sw.isSW(t.text.toLowerCase().trim()))
					frac += 1;
			}
			vals.add(frac / c.tokenLists.get(0).size());
		}

		// part of speech
		CountedSet<String> pos = new CountedSet<String>();
		for (PToken t : c.tokenLists.get(0))
			pos.add(t.pos.substring(0, 1));

		if (active[13])
			vals.add((double) pos.getCount("N"));
		if (active[14])
			vals.add(pos.getCount("N") / pos.sum());
		if (active[15]) {
			PToken last = c.tokenLists.get(0).get(c.tokenLists.get(0).size() - 1);
			vals.add(last.pos.startsWith("N") ? 1.0 : 0.0);
		}
		if (active[16])
			vals.add((double) pos.getCount("J"));
		if (active[17])
			vals.add(pos.getCount("J") / pos.sum());
		if (active[18])
			vals.add(c.name.matches(".*[^A-Za-z0-9 ]+.*") ? 1.0 : 0.0);

		// doc freq
		if (active[19]) {
			Set<String> docs = new HashSet<String>();
			for (Concept co : groupLookup.get(c)) {
				PToken firstToken = co.tokenLists.get(0).get(0);
				docs.add(firstToken.documentId);
			}
			vals.add(docs.size() / getClusterSize(cluster));
		}

		// label
		vals.add(isCorrect ? (double) labels.indexOf("correct") : (double) labels.indexOf("incorrect"));

		double[] dvals = new double[vals.size()];
		for (int i = 0; i < dvals.length; i++)
			dvals[i] = vals.get(i);
		return new Instance(1.0, dvals);
	}

	// build lookup table for grouped concepts
	public static Map<Concept, Set<Concept>> buildConceptGroupingLookup(List<Concept> concepts,
			Map<Concept, Concept> conceptMapping) {
		Map<Concept, Set<Concept>> lookup = new HashMap<Concept, Set<Concept>>();
		for (Concept c : concepts)
			lookup.put(c, new HashSet<Concept>());
		for (Entry<Concept, Concept> entry : conceptMapping.entrySet())
			lookup.get(entry.getValue()).add(entry.getKey());
		return lookup;
	}

	// read size of topic clusters from file
	public double getClusterSize(String cluster) {
		return clusterSize.get(cluster);
	}

	// calculate topic-relatedness for concept as token overlap
	public double getTopicOverlap(Concept concept, String cluster, boolean relative) {

		String[] topicToken = topics.get(cluster);

		int common = 0;
		for (String tg : topicToken) {
			for (PToken t : concept.tokenLists.get(0)) {
				if (tg.toLowerCase().equals(t.text.toLowerCase())) {
					common += 1;
					break;
				}
			}
		}

		if (relative)
			return common / (double) topicToken.length;
		else
			return common;
	}

}