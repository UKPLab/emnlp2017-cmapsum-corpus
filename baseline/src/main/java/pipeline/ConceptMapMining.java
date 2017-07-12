package pipeline;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;

import cmaps.ConceptMap;
import cmaps.PToken;
import cmaps.io.ConceptMapWriter;
import cmaps.io.Format;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import extraction.CmmComponent;

/**
 * Generic concept map mining UIMA component
 */
public class ConceptMapMining extends JCasConsumer_ImplBase {

	public static final String PARAM_NAME = "name";
	@ConfigurationParameter(name = PARAM_NAME)
	private String name;

	public static final String PARAM_MAX_CONCEPTS = "maxConcepts";
	@ConfigurationParameter(name = PARAM_MAX_CONCEPTS, defaultValue = "9999999")
	private int maxConcepts;

	public static final String PARAM_MAX_LINKS = "maxLinks";
	@ConfigurationParameter(name = PARAM_MAX_LINKS, defaultValue = "9999999")
	private int maxLinks;

	public static final String PARAM_TARGET_LOCATION = "targetLocation";
	@ConfigurationParameter(name = PARAM_TARGET_LOCATION)
	private String targetLocation;

	public static final String PARAM_COMPONENTS = "componentNames";
	@ConfigurationParameter(name = PARAM_COMPONENTS)
	private String[] componentNames;

	private final String componentPackage = "extraction.";
	private List<CmmComponent> components;

	private Map<String, PToken> tokMap;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		this.tokMap = new HashMap<String, PToken>();
		this.components = new ArrayList<CmmComponent>(this.componentNames.length);
		for (String componentName : this.componentNames) {
			if (componentName != null) {
				Class<?> classDefinition;
				CmmComponent component = null;
				try {
					classDefinition = Class.forName(componentPackage + componentName);
					component = (CmmComponent) classDefinition.newInstance();
					component.setParent(this);
				} catch (Exception e) {
					e.printStackTrace();
				}
				this.components.add(component);
			}
		}
		this.log("initialized");
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		DocumentMetaData meta = (DocumentMetaData) jcas.getDocumentAnnotationFs();
		this.log("processing " + meta.getDocumentTitle());
		Collection<Sentence> sentences = JCasUtil.select(jcas, Sentence.class);
		int i = 0;
		for (Sentence sent : sentences) {
			this.createTokenForSentence(sent, i);
			List<CmmComponent> delayed = new LinkedList<CmmComponent>();
			for (CmmComponent component : this.components) {
				if (component.delaySentenceProcessing())
					delayed.add(component);
				else
					component.processSentence(jcas, sent);
			}
			for (CmmComponent component : delayed) {
				component.processSentence(jcas, sent);
			}
			i++;
		}
	}

	@Override
	public void collectionProcessComplete() {
		this.log("processing collection");
		ConceptMap map = null;
		for (CmmComponent component : this.components) {
			component.processCollection();
			map = component.getConceptMap();
		}
		if (map != null) {
			File targetFolder = new File(targetLocation);
			if (!targetFolder.exists())
				targetFolder.mkdirs();
			File file = new File(targetLocation + "/" + name + ".cmap");
			ConceptMapWriter.writeToFile(map, file, Format.TSV);
			System.out.println(map);
		}
	}

	// create an internal token object from the annotation
	// -> annotation object won't exist anymore in collection processing step
	public PToken getToken(Token token) {
		DocumentMetaData meta = (DocumentMetaData) token.getCAS().getDocumentAnnotation();
		String key = meta.getDocumentId() + "-" + token.getAddress();
		PToken tok = this.tokMap.get(key);
		if (tok == null) {
			tok = new PToken(token);
			this.tokMap.put(key, tok);
		}
		return tok;
	}

	private void createTokenForSentence(Sentence sent, int sentId) {
		DocumentMetaData meta = (DocumentMetaData) sent.getCAS().getDocumentAnnotation();
		int i = 1;
		for (Token token : JCasUtil.selectCovered(Token.class, sent)) {
			String key = meta.getDocumentId() + "-" + token.getAddress();
			PToken tok = new PToken(token, this.getSentKey(sent), i, sentId);
			this.tokMap.put(key, tok);
			i++;
		}
	}

	public <T> T getComponent(Class<T> type) {
		for (CmmComponent component : components) {
			if (type.isInstance(component))
				return type.cast(component);
		}
		return null;
	}

	public String getName() {
		return this.name;
	}

	public int getMaxConcepts() {
		return this.maxConcepts;
	}

	public int getMaxLinks() {
		return this.maxLinks;
	}

	public String getTargetLocation() {
		return this.targetLocation;
	}

	public String getSentKey(Sentence sent) {
		DocumentMetaData meta = (DocumentMetaData) sent.getCAS().getDocumentAnnotation();
		return meta.getDocumentId() + "-" + sent.getAddress();
	}

	public void log(CmmComponent component, String msg) {
		this.log(component.getClass().getSimpleName() + ": " + msg);
	}

	private void log(String msg) {
		getContext().getLogger().log(Level.INFO, "CMM " + name + ": " + msg);
	}

}
