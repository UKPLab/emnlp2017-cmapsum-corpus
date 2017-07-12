package cmaps;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Token
 */
public class PToken {

	public String documentId;
	public String sentId;
	public int sentIdConll;
	public int tokenId;
	public int start;
	public int end;
	public int docLength;

	public String text;
	public String pos;
	public String stem;
	public String lemma;

	public PToken(String text) {
		this.text = text;
	}

	public PToken(Token t) {

		this.text = t.getCoveredText().replaceAll("\\p{C}", " ").replaceAll("  ", " ");
		this.pos = t.getPos() != null ? t.getPos().getPosValue() : null;
		this.stem = t.getStem() != null ? t.getStem().getValue() : null;
		this.lemma = t.getLemma() != null ? t.getLemma().getValue() : null;

		DocumentMetaData meta = (DocumentMetaData) t.getCAS().getDocumentAnnotation();
		this.documentId = meta.getDocumentId();
		this.start = t.getBegin();
		this.end = t.getEnd();
		this.docLength = t.getCAS().getDocumentText().length();
	}

	public PToken(Token t, String sentKey, int tokenId, int sentId) {
		this(t);
		this.sentId = sentKey;
		this.tokenId = tokenId;
		this.sentIdConll = sentId;
	}

	@Override
	public String toString() {
		return text + "(" + pos + "/" + stem + "/" + lemma + ")";
	}

}
