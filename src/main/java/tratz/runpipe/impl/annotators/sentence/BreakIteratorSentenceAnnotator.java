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

package tratz.runpipe.impl.annotators.sentence;

import java.text.BreakIterator;
import java.util.List;
import java.util.Map;

import tratz.runpipe.TextDocument;
import tratz.runpipe.annotations.Sentence;
import tratz.runpipe.impl.AnnotatorImpl;

/**
 * Annotator that uses the java BreakIterator to split sentences
 */
public class BreakIteratorSentenceAnnotator extends AnnotatorImpl {
	public final static String ONLY_WHEN_NECESSARY = "ONLY_WHEN_NECESSARY";	
	private boolean mOnlyWhenNecessary;
	
	public void initialize(Map<String, String> params) {
		String result = params.get(ONLY_WHEN_NECESSARY);
		if(result != null) {
			mOnlyWhenNecessary = Boolean.parseBoolean(result);
		}
	}
	
	public void process(TextDocument doc) {
		List sentences = doc.getAnnotationList(Sentence.class);
		if(!mOnlyWhenNecessary || sentences == null) {
			BreakIterator sentenceIterator = BreakIterator.getSentenceInstance();
			sentenceIterator.setText(doc.getText());
	
			int start = sentenceIterator.first();
			for (int end = sentenceIterator.next(); end != BreakIterator.DONE; end = sentenceIterator.next()) {
				doc.addAnnotation(new Sentence(doc, start, end));
				start = end;
			}
		}
	 }
	
}