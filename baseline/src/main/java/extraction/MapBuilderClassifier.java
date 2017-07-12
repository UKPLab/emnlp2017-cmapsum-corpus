package extraction;

import baseline.src.main.java.extraction.ConceptMerger.MergeStrategy;
import baseline.src.main.java.extraction.ConceptRanker.RankingStrategy;
import baseline.src.main.java.util.ClassifierUtils;

/**
 * Builds a concept map from extracted concepts and relations
 * 
 * 1. merges concepts with same label
 * 2. scores concepts with classifier
 * 3. selects best relation for each concept
 * 4. finds high-scoring connected component <= max_concepts
 * 
 */
public class MapBuilderClassifier extends MapBuilder {

	private static String modelName = "model/cmapsum_train_rf.model";

	private File documentLocation;
	private RandomForest model;

	public MapBuilderClassifier() {
		super();
	}

	@Override
	public void processSentence(JCas jcas, Sentence sent) {
		if (documentLocation == null) {
			DocumentMetaData meta = (DocumentMetaData) jcas.getDocumentAnnotationFs();
			try {
				File docFile = new File(new URL(meta.getDocumentUri()).getPath().replace("props", "baseline"));
				this.documentLocation = docFile.getParentFile();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
	};

	@Override
	@Override
	public void processCollection() {

		this.init(this.parent);

		File loc = new File(this.parent.getTargetLocation());
		String cluster = loc.getName();

		// prepare concepts
		this.concepts = this.ce.getConcepts();
		int sizeInit = this.concepts.size();

		// merge concepts
		ConceptRanker.rankConcepts(this.concepts, RankingStrategy.CF, false, this.parent, "");
		ConceptMerger.mergeConcepts(this.concepts, conceptMapping, MergeStrategy.STEM_SW);
		Map<Concept, Set<Concept>> groupLookup = ClassifierUtils.buildConceptGroupingLookup(concepts, conceptMapping);

		this.parent.log(this, sizeInit + " concepts, " + concepts.size() + " after merging");

		// prepare features
		ConceptDict cd = parent.getComponent(ConceptDict.class);
		TextRankScorer tr = parent.getComponent(TextRankScorer.class);
		tr.compute();

		String topicFile = documentLocation.getParent() + "/topics.tsv";
		String clusterSizeFile = documentLocation.getParent() + "/cluster_size.txt";
		ClassifierUtils util = new ClassifierUtils(clusterSizeFile, topicFile);

		Instances data = util.createEmptyDataset("ConceptSelectionPredict");
		Map<Concept, Instance> instances = new HashMap<Concept, Instance>();
		for (Concept c : this.concepts) {
			Instance instance = util.createInstance(c, false, cd, tr, cluster, groupLookup);
			instance.setDataset(data);
			instances.put(c, instance);
		}

		// load model
		try {
			model = (RandomForest) (new ObjectInputStream(new FileInputStream(modelName))).readObject();
		} catch (Exception e) {
			System.err.println(e);
			System.exit(1);
		}

		// predict
		for (Concept c : this.concepts) {
			try {
				Instance i = instances.get(c);
				double[] pred = model.distributionForInstance(i);
				c.weight = pred[1]; // probability for positive classification
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Collections.sort(this.concepts);

		// update relations and select best
		this.updateRelations();
		int relSize = this.le.propositions.size();
		this.selectBestRelations();
		this.parent.log(this, "propositions: " + relSize + ", after selection: " + this.le.propositions.size());

		// build concept map
		this.buildMapIteratively();
	}

	// build map by removing lowest concepts and follow biggest connected parts
	protected void buildMapIteratively() {

		this.parent.log(this, "building map");

		List<Set<Concept>> components = this.findConnectedComponents(this.concepts, false);
		int biggest = components.get(0).size();
		while (this.concepts.size() > this.parent.getMaxConcepts()) {

			// remove too small components
			for (Set<Concept> component : components) {
				if (component.size() < this.parent.getMaxConcepts() && component.size() < biggest) {
					this.concepts.removeAll(component);
				}
			}

			// remove weakest concept
			this.concepts.remove(this.concepts.size() - 1);

			// find new components
			components = this.findConnectedComponents(this.concepts, false);
			biggest = components.get(0).size();
		}

		// build map
		this.map.addConcept(this.concepts);
		for (Concept c : this.concepts) {
			for (Proposition p : this.le.getPropositions(c)) {
				if (this.concepts.contains(p.sourceConcept) && this.concepts.contains(p.targetConcept))
					this.map.addProposition(p);
			}
		}

	}

	// add all concepts
	protected void buildMapConceptsOnly() {
		for (int i = 0; i < this.parent.getMaxConcepts(); i++) {
			Concept c = this.concepts.get(i);
			this.map.addConcept(c);
			for (Proposition p : this.le.getPropositions(c)) {
				this.map.addProposition(p);
			}
		}
	}

	// check if map reached size limit
	protected boolean isFinished() {
		int[] size = this.map.size();
		return size[0] >= this.parent.getMaxConcepts() || size[1] >= this.parent.getMaxLinks();
	}

	// find connected components
	private List<Set<Concept>> findConnectedComponents(List<Concept> concepts, boolean printLog) {

		List<Set<Concept>> components = new ArrayList<Set<Concept>>();
		Set<Concept> notVisited = new HashSet<Concept>(concepts);

		while (!notVisited.isEmpty()) {

			Concept first = notVisited.iterator().next();
			notVisited.remove(first);
			Queue<Concept> neighbourQueue = new LinkedList<Concept>();
			neighbourQueue.add(first);
			Set<Concept> component = new HashSet<Concept>();

			while (!neighbourQueue.isEmpty()) {
				Concept c = neighbourQueue.poll();
				component.add(c);
				for (Proposition p : this.le.getPropositions(c)) {
					if (notVisited.contains(p.sourceConcept)) {
						neighbourQueue.add(p.sourceConcept);
						notVisited.remove(p.sourceConcept);
					}
					if (notVisited.contains(p.targetConcept)) {
						neighbourQueue.add(p.targetConcept);
						notVisited.remove(p.targetConcept);
					}
				}
			}
			components.add(component);
		}

		if (printLog) {
			this.parent.log(this, components.size() + " connected components found");
			DescriptiveStatistics lengthStat = new DescriptiveStatistics();
			DescriptiveStatistics weightStat = new DescriptiveStatistics();
			for (Set<Concept> component : components) {
				lengthStat.addValue(component.size());
				double weightSum = 0;
				for (Concept c : component)
					weightSum += c.weight;
				weightStat.addValue(weightSum);
			}
			System.out.println("component size");
			System.out.println(lengthStat);
			System.out.println("component weight");
			System.out.println(weightStat);
		}

		Collections.sort(components, new Comparator<Set<Concept>>() {
			public int compare(Set<Concept> o1, Set<Concept> o2) {
				return o2.size() - o1.size();
			}
		});
		return components;
	}

	// map relations to merged concepts, remove if concepts were filtered out
	protected void updateRelations() {

		List<Proposition> remove = new LinkedList<Proposition>();
		this.le.nodeMap.clear();
		for (Proposition p : this.le.propositions) {
			p.sourceConcept = this.getConcept(p.sourceConcept);
			p.targetConcept = this.getConcept(p.targetConcept);
			if (p.sourceConcept == null || p.targetConcept == null || p.sourceConcept == p.targetConcept) {
				remove.add(p);
			} else {
				Set<Proposition> cPs = this.le.nodeMap.get(p.sourceConcept);
				if (cPs == null) {
					cPs = new HashSet<Proposition>();
					this.le.nodeMap.put(p.sourceConcept, cPs);
				}
				cPs.add(p);
				Set<Proposition> cPt = this.le.nodeMap.get(p.targetConcept);
				if (cPt == null) {
					cPt = new HashSet<Proposition>();
					this.le.nodeMap.put(p.targetConcept, cPt);
				}
				cPt.add(p);
				p.weight = p.sourceConcept.weight + p.targetConcept.weight;
			}
		}
		Set<Proposition> all = new HashSet<Proposition>();
		for (Set<Proposition> props : this.le.nodeMap.values()) {
			all.addAll(props);
		}
		this.le.propositions.clear();
		this.le.propositions.addAll(all);

	}

	// for all pairs of concepts, select only one relation
	protected void selectBestRelations() {

		for (Concept c1 : this.concepts) {

			// collect all neighbours
			Set<Concept> neighbours = new HashSet<Concept>();
			for (Proposition p : this.le.getPropositions(c1)) {
				neighbours.add(p.sourceConcept);
				neighbours.add(p.targetConcept);
			}
			neighbours.remove(c1);

			// for each pair
			for (Concept c2 : neighbours) {

				// collect all relations
				List<Proposition> props = new ArrayList<Proposition>();
				for (Proposition p : this.le.getPropositions(c1))
					if ((p.sourceConcept == c1 && p.targetConcept == c2)
							|| (p.sourceConcept == c2 && p.targetConcept == c1))
						props.add(p);

				// keep best only
				if (props.size() > 1) {
					Collections.sort(props, new Comparator<Proposition>() {
						public int compare(Proposition o1, Proposition o2) {
							return o1.linkingWord.length() - o2.linkingWord.length();
						}
					});
					for (Proposition p : props.subList(1, props.size())) {
						this.le.propositions.remove(p);
						this.le.nodeMap.get(c1).remove(p);
						this.le.nodeMap.get(c2).remove(p);
					}
				}
			}

		}
	}

}