package preprocessing;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * print number of token to stdout for a processed document
 */
public class CorpusStatWriter extends JCasConsumer_ImplBase {

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {

		int count = JCasUtil.select(aJCas, Token.class).size();

		DocumentMetaData meta = (DocumentMetaData) aJCas.getCas().getDocumentAnnotation();

		System.out.println(meta.getDocumentId() + "\t" + count);

	}

}
