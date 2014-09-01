/*
 * Copyright 2011 University of Southern California 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0 
 *      
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package tratz.runpipe.impl.annotators.tokenize;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import tratz.runpipe.InitializationException;
import tratz.runpipe.ProcessException;
import tratz.runpipe.TextDocument;
import tratz.runpipe.annotations.Sentence;
import tratz.runpipe.annotations.Token;
import tratz.runpipe.impl.AnnotatorImpl;

public class WhitespaceTokenizer extends AnnotatorImpl {
	
	@Override
	public void initialize(Map<String, String> args) throws InitializationException {
		
	}
	
	@Override
	public void process(TextDocument doc) throws ProcessException {
		List<Sentence> allSentences = (List<Sentence>)doc.getAnnotationList(Sentence.class);
		if(allSentences != null) {
			Pattern whitespacePattern = Pattern.compile("\\s+");
			for(Sentence sentence : allSentences) {
				String text = sentence.getAnnotText();
				String[] split = whitespacePattern.split(text);
				int sentenceStart = sentence.getStart();
				int index = 0;
				for(String token : split) {
					int tokenIndex = text.indexOf(token, index);
					if(!token.trim().equals("")) {
						Token newToken = new Token(doc, sentenceStart+tokenIndex,sentenceStart+tokenIndex+token.length());
						doc.addAnnotation(newToken);
					}
					index = tokenIndex + token.length();
				}
			}
		}
	}
	
}