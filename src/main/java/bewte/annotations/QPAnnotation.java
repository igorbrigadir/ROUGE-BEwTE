package bewte.annotations;

import tratz.runpipe.TextDocument;
import tratz.runpipe.impl.AnnotationImpl;


public class QPAnnotation extends AnnotationImpl {
	
	public static final long serialVersionUID = 1;
	
	public QPAnnotation(TextDocument doc, int start, int end) {
		super(doc, start, end);
	}
}