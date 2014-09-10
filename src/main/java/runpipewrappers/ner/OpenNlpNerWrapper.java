package runpipewrappers.ner;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import opennlp.maxent.io.BinaryGISModelReader;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.util.Span;
import tratz.runpipe.Annotation;
import tratz.runpipe.Annotator;
import tratz.runpipe.InitializationException;
import tratz.runpipe.ProcessException;
import tratz.runpipe.TextDocument;
import tratz.runpipe.annotations.Sentence;
import tratz.runpipe.annotations.Token;
import tratz.runpipe.util.RunpipeUtils;

/**
 * OpenNLP Named Entity Recognition interfacing code
 */
public class OpenNlpNerWrapper implements Annotator {

	public final static String PARAM_MODEL_PATH = "ModelPath";
	public final static String PARAM_ANNOTATION_CLASS = "AnnotationClass";
	
	private NameFinderME mNer;
	private Constructor mAnnotationConstructor;
	
	public void initialize(Map<String, String> params) throws InitializationException {
		try {
			DataInputStream inStream = new DataInputStream(new GZIPInputStream(new FileInputStream(params.get(PARAM_MODEL_PATH))));
			mNer = new NameFinderME(new BinaryGISModelReader(inStream).getModel());
			inStream.close();
			
			Class annotationClass = Class.forName(params.get(PARAM_ANNOTATION_CLASS));
			mAnnotationConstructor = annotationClass.getConstructor(new Class[]{TextDocument.class});
			
		}
		catch(Exception e) {
			throw new InitializationException(e);
		}
	}
	
	public void process(TextDocument doc) throws ProcessException {
		List<Sentence> sentences = (List<Sentence>)doc.getAnnotationList(Sentence.class);
		List<Token> tokens = (List<Token>)doc.getAnnotationList(Token.class);
		if(tokens != null) {
			for(Annotation sentence : sentences) {
				processSentence(doc, (Sentence)sentence, tokens);
				mNer.clearAdaptiveData();
			}
		}
	}
	
	private void processSentence(TextDocument doc, Sentence sentence, List<Token> tokenList) throws ProcessException {
		List<Token> containedTokens = RunpipeUtils.getSublist(sentence, tokenList);
		final int numTokens = containedTokens.size();
		List<String> tokenStrings = new ArrayList<String>(numTokens);
		for(Annotation token : containedTokens) {
			tokenStrings.add(token.getAnnotText());
		}		
		Span[] spans = mNer.find(tokenStrings.toArray(new String[0]));
		if(spans != null) {
		for(int i = 0; i < spans.length; i++) {
			int startToken = spans[i].getStart();
			int endToken = spans[i].getEnd();
			
			int entityStart = ((Token)containedTokens.get(startToken)).getStart();
			int entityEnd = ((Token)containedTokens.get(endToken-1)).getEnd();
			
			try {
				Annotation newAnnot = (Annotation)mAnnotationConstructor.newInstance(new Object[]{doc});
				newAnnot.setStart(entityStart);
				newAnnot.setEnd(entityEnd);
				doc.addAnnotation(newAnnot);
			}
			catch(Exception e) {
				throw new ProcessException(e);
			}
		}
		}
		mNer.clearAdaptiveData();
	}
	
}