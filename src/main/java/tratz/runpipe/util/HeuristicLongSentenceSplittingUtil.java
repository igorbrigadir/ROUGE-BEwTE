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

package tratz.runpipe.util;

import java.util.ArrayList;
import java.util.List;

import tratz.runpipe.TextDocument;
import tratz.runpipe.annotations.Sentence;

public class HeuristicLongSentenceSplittingUtil {
	
	public static List<Sentence> splitSentences(TextDocument doc, List<Sentence> sentences) {
		List<Sentence> newSentences = new ArrayList<Sentence>();
		int textLength = doc.getText().length();
		for(Sentence s : sentences) {
			String sText = s.getAnnotText();
			
			// First try to split based upon likely sentence boundary (perhaps missed by whichever Sentence splitter was used)
			// NOTE: Regex here looks like it could be improved
			String[] split = sText.split("([^.?!][^.?!])([.?!;])([^.?!0-9][^.?!0-9])");
			if(split.length > 1) {
				int start = 0;
				for(int i = 0; i < split.length; i++) {
					String part = split[i];
					Sentence newSentence = new Sentence(doc, i == 0 ? s.getStart() : s.getStart()+start-2, Math.min(textLength, s.getStart()+start+part.length()+3));
					newSentence.setSentenceNum(s.getSentenceNum());
					// IF clause fixes a bug that introduced empty sentences
					if(!newSentence.getAnnotText().trim().equals("")) {
						newSentences.add(newSentence);
						doc.addAnnotation(newSentence);
					}
					start = start+part.length()+5;
				}
				doc.removeAnnotation(s);		
			}
			else {
				// If no likely sentence boundary found, split as close to center of sentence as possible
				int midSpace = sText.indexOf(" ", sText.length()/2);
				if(midSpace == -1 || midSpace == sText.length()-1) {
					midSpace = sText.lastIndexOf(" ", sText.length()/2);
				}
				if(midSpace > 1 && midSpace < sText.length()-1) {
					Sentence newSentence1 = new Sentence(doc, s.getStart(), s.getStart()+midSpace);
					Sentence newSentence2 = new Sentence(doc, s.getStart()+midSpace+1, s.getEnd());
					newSentence1.setSentenceNum(s.getSentenceNum());
					newSentence2.setSentenceNum(s.getSentenceNum());
					// IF clause fixes a bug that introduced empty sentences
					if(!newSentence1.getAnnotText().trim().equals("")) {
						newSentences.add(newSentence1);
						doc.addAnnotation(newSentence1);
					}
					// IF clause fixes a bug that introduced empty sentences
					if(!newSentence2.getAnnotText().trim().equals("")) {
						newSentences.add(newSentence2);
						doc.addAnnotation(newSentence2);
					}
				
					doc.removeAnnotation(s);
				}
			}				
		}
		return newSentences;
	}
	
}