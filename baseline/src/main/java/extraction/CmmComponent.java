package extraction;

import org.apache.uima.jcas.JCas;

import cmaps.ConceptMap;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import pipeline.ConceptMapMining;

/**
 * Abstract base for all concept map mining components
 */
public abstract class CmmComponent {

	protected ConceptMapMining parent;

	public void setParent(ConceptMapMining parent) {
		this.parent = parent;
	}

	public ConceptMap getConceptMap() {
		return null;
	}

	public void processSentence(JCas jcas, Sentence sent) {
		// default: no action
	}

	public boolean delaySentenceProcessing() {
		return false;
	}

	public void processCollection() {
		// default: no action
	}

}
