package bewte.annotators;

import java.util.List;
import java.util.Map;

import bewte.util.Tokenizer;

import tratz.runpipe.InitializationException;
import tratz.runpipe.ProcessException;
import tratz.runpipe.TextDocument;
import tratz.runpipe.annotations.Sentence;
import tratz.runpipe.annotations.Token;
import tratz.runpipe.impl.AnnotatorImpl;

public class RegexTokenizer extends AnnotatorImpl {
	
	private Tokenizer mTokenizer;
	@Override
	public void initialize(Map<String, String> args) throws InitializationException {
		mTokenizer = new Tokenizer();
	}
	
	@Override
	public void process(TextDocument doc) throws ProcessException {
		List<Sentence> allSentences = (List<Sentence>)doc.getAnnotationList(Sentence.class);
		if(allSentences != null) {
			for(Sentence sentence : allSentences) {
				String text = sentence.getAnnotText();
				
				String tokenizedText = mTokenizer.tokenize(text);
				String[] split = tokenizedText.split("\\s+");
				int sentenceStart = sentence.getStart();
				int index = 0;
				for(String token : split) {
					String pos = null;
					int tokenLength = token.length();
					int tokenIndex = text.indexOf(token, index);
					//System.err.println(token + ":" + tokenIndex);
					if(tokenIndex == -1) {
						if(token.equals("``") || token.equals("''")) {
							pos = token;
							tokenIndex = text.indexOf("\"", index);
							tokenLength = 1;
						}
					}
					if(tokenIndex == -1) {
						System.err.println("Bad tokenization occurred. Can't find: " + token);
						System.err.println(text);
						System.err.println(tokenizedText);
					}
					else {
						if(!token.trim().equals("")) {
							Token newToken = new Token(doc, sentenceStart+tokenIndex,sentenceStart+tokenIndex+tokenLength);
							newToken.setPos(pos);
							doc.addAnnotation(newToken);
						}
						index = tokenIndex + tokenLength;
					}
				}
			}
		}
	}
	
}