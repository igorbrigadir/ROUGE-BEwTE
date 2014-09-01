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

package tratz.parse.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tratz.parse.types.Arc;
import tratz.parse.types.Parse;
import tratz.parse.types.Sentence;
import tratz.parse.types.Token;

/**
 * Designed to read in CoNLL-X format
 *
 */
public class ConllxSentenceReader implements SentenceReader {
	
	// canonicalizing map.. this ought to be made optional
	private Map<String, String> mCanon = new HashMap<String, String>();
	
	@Override
	public Parse readSentence(BufferedReader reader) throws IOException {
		Token root = new Token("[ROOT]", 0);
		List<Token> tokens = new ArrayList<Token>();
		List<Arc> arcs = new ArrayList<Arc>();
		
		String line = null;
		Map<Integer, Token> numToToken = new HashMap<Integer, Token>();
		numToToken.put(0, root);
		List<String> lines = new ArrayList<String>();
		
		while((line = reader.readLine()) != null) {
			if(line.trim().equals("")) {
				if(tokens.size() == 0) {
					continue;
				}
				else {
					break;
				}
			}
			else {
				lines.add(line);
				String[] split = line.split("\\t+");
				int tokenNum = Integer.parseInt(split[0]);
				
				String text = canon(split[1]);
				
				Token token = new Token(text, tokenNum);
				String pos = canon(split[4]);
				String coarsePos = canon(split[3]);
				
				token.setCoarsePos(coarsePos);
				token.setPos(pos);
				
				tokens.add(token);
				numToToken.put(tokenNum, token);
			}
		}
		for(String l : lines) {
			String[] split = l.split("\\t+");
			int tokenNum = Integer.parseInt(split[0]);
			int head = Integer.parseInt(split[6]);
			String dependency = canon(split[7]);
			String pos = canon(split[4]);
			String coarsePos = canon(split[3]);
			
			
			Token t1 = numToToken.get(tokenNum);
			Token t2 = numToToken.get(head);
			
			// Why am I reseting this stuff?
			t1.setPos(pos);
			t1.setCoarsePos(coarsePos);
			
			arcs.add(new Arc(t1, t2, dependency));
		}
		
		Parse result = null;
		if(tokens.size() > 0) {
			result = new Parse(new Sentence(tokens), root, arcs);
		}
		return result;
	}
	
	private String canon(String s) {
		String canon = mCanon.get(s);
		if(canon == null) mCanon.put(s, canon = s);
		return canon;
	}
	
}