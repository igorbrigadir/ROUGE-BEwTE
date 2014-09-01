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

package tratz.runpipe.impl.annotators.pos;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



import tratz.jwni.WordNet;
import tratz.pos.PosTagger;
import tratz.runpipe.InitializationException;
import tratz.runpipe.ProcessException;
import tratz.runpipe.TextDocument;
import tratz.runpipe.annotations.Sentence;
import tratz.runpipe.annotations.Token;
import tratz.runpipe.impl.AnnotatorImpl;
import tratz.runpipe.util.RunpipeUtils;

public class TratzPosTaggerAnnotator extends AnnotatorImpl {
	public final static String PARAM_MODEL_FILE = "ModelFile";
	public final static String PARAM_WORDNET_DIR = "WordNetDir";
	
	private PosTagger mPosTagger;
	
	public TratzPosTaggerAnnotator() {
	}
	
	@Override
	public void initialize(Map<String, String> args) throws InitializationException {
		try {
			String modelFile = args.get(PARAM_MODEL_FILE);
			mPosTagger = new PosTagger(new File(modelFile));
			String wordNetDir = args.get(PARAM_WORDNET_DIR);
			new WordNet(new File(wordNetDir));
		}
		catch(Exception ioe) {
			throw new InitializationException(ioe);
		}
	}
	
	@Override
	public void process(TextDocument doc) throws ProcessException {
		List<Sentence> allSentences = (List<Sentence>)doc.getAnnotationList(Sentence.class);
		List<Token> allTokens = (List<Token>)doc.getAnnotationList(Token.class);
		if(allSentences != null && allTokens != null) {
			try {
				for(Sentence sentence : allSentences) {
					List<Token> tokens = RunpipeUtils.getSublist(sentence, allTokens);
					
					List<tratz.parse.types.Token> gtokens = new ArrayList<tratz.parse.types.Token>(tokens.size());
					int index = 0;
					for(Token tok : tokens) {
						gtokens.add(new tratz.parse.types.Token(tok.getAnnotText(), ++index));
					}
					
					mPosTagger.posTag(gtokens);
					final int numTokens = tokens.size();
					for(int i = 0; i < numTokens; i++) {
						tokens.get(i).setPos(gtokens.get(i).getPos());
					}
				}
			}
			catch(Exception e) {
				throw new ProcessException(e);
			}
		}
	}
	
}
