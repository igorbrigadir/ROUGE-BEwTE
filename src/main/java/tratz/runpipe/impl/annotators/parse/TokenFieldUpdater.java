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

package tratz.runpipe.impl.annotators.parse;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;


import tratz.jwni.IndexEntry;
import tratz.jwni.POS;
import tratz.jwni.WordNet;
import tratz.runpipe.InitializationException;
import tratz.runpipe.ProcessException;
import tratz.runpipe.TextDocument;
import tratz.runpipe.annotations.Token;
import tratz.runpipe.impl.AnnotatorImpl;
import tratz.util.TreebankConstants;

public class TokenFieldUpdater extends AnnotatorImpl implements TreebankConstants {
	
	public static final String PARAM_WORDNET_DIR = "WordNetDir";
	
	public void initialize(Map<String, String> params) throws InitializationException {
		String wordNetPath = params.get(PARAM_WORDNET_DIR);
		if(WordNet.getInstance() == null) {
			try {
				String path = new File(wordNetPath).getAbsolutePath();
				new WordNet(new File(path).getAbsoluteFile().toURI().toString());
			}
			catch(IOException ioe) {
				throw new InitializationException(ioe);
			}
		}
	}
	
	public void process(TextDocument doc) throws ProcessException {
		List<Token> tokens = (List)doc.getAnnotationList(Token.class);
		if(tokens != null) {
			for(Token token : tokens) {
				updateField(token);
			}
		}
	}
	
	private void updateField(Token tok) {
		String xPos = tok.getPos();
		POS jpos = getPosForType(xPos);
		if(jpos != null) {
			IndexEntry ie = null;
			try {
				for(POS speechPart : new POS[]{jpos, POS.NOUN, POS.VERB, POS.ADJECTIVE}) {
					if(ie == null) {
						ie = WordNet.getInstance().lookupIndexEntry(speechPart, tok.getAnnotText().replace(' ', '_'));
					}
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			if(ie != null) {
				tok.setLemma(ie.getLemma());
			}
			else {
				tok.setLemma(tok.getAnnotText().toLowerCase());
			}
		}
		else {
			tok.setLemma(tok.getAnnotText().toLowerCase());
		}
	}
	
	protected POS getPosForType(String type) {
		POS pos = null;
		if(TreebankConstants.NOUN_LABELS.contains(type)) {
			pos = POS.NOUN;
		}
		else if(TreebankConstants.VERB_LABELS.contains(type)) {
			pos = POS.VERB;
		}
		else if(TreebankConstants.ADJ_LABELS.contains(type)) {
			pos = POS.ADJECTIVE;
		}
		else if(TreebankConstants.ADV_LABELS.contains(type)) {
			pos = POS.ADVERB;
		}
		return pos;
	}
	
}