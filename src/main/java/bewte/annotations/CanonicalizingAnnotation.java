package bewte.annotations;

import tratz.runpipe.TextDocument;
import tratz.runpipe.impl.AnnotationImpl;


abstract public class CanonicalizingAnnotation extends AnnotationImpl {
	
	public CanonicalizingAnnotation(TextDocument doc, int start, int end) {
		super(doc, start, end);
	}
	
	abstract public String getCanonicalString();
	abstract public String getCanonicalType();
}