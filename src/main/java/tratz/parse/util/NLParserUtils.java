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

package tratz.parse.util;

import java.util.HashMap;
import java.util.Map;

import tratz.jwni.IndexEntry;
import tratz.jwni.POS;
import tratz.jwni.WordNet;
import tratz.parse.types.Token;

public class NLParserUtils {
	
	public final static String UNUSUAL_DOUBLE_QUOTES;
	public final static String UNUSUAL_SINGLE_QUOTES;
	
	static {
		// 145-148 are ANSI quotes, 132 is double low quotes
		// 	8216-8223 quotes are supported by HTML (?Unicode?), 8242-8247 are multiple "primes"
		int[] doubleQuoteInts = new int[]{132,  171,187,147,148,8220,8221,8222,8223, 8243,8244,8246,8247};
		int[] singleQuoteInts = new int[]{145,146, 8216,8217,8218,8219, 8242,8245};
		// 8218 (looks like a comma, but maybe its a lower quote) ‚
		// 	8220 & 8221 “”
		// 8216 & 8217 => ‘ ’
		StringBuilder buf = new StringBuilder();
		for(int i : doubleQuoteInts) {
			buf.append((char)i).append("|");
		}
		buf.setLength(buf.length()-1);
		UNUSUAL_DOUBLE_QUOTES = buf.toString();
		buf = new StringBuilder();
		for(int i : singleQuoteInts) {
			buf.append((char)i).append("|");
		}
		buf.setLength(buf.length()-1);
		UNUSUAL_SINGLE_QUOTES = buf.toString();
	}
	
	private static Map<String, POS> stringToPos = new HashMap<String, POS>();
	static {
		for(String s : new String[]{"VB","VBP","VBZ","VBD","VBN", "VBG"}) {
			stringToPos.put(s, POS.VERB);
		}
		for(String s: new String[]{"NN","NNS"}) {
			stringToPos.put(s, POS.NOUN);
		}
		for(String s: new String[]{"JJ","JJR","JJS"}) {
			stringToPos.put(s, POS.ADJECTIVE);
		}
		for(String s: new String[]{"RB","RBR","RBS"}) {
			stringToPos.put(s, POS.ADVERB);
		}
	}
	
	public static String getLemma(Token t, WordNet wn) {
		POS pos = stringToPos.get(t.getPos());
		String lemma = t.getText();
		if(pos != null) {
			IndexEntry ie = wn.lookupIndexEntry(pos, lemma);
			if(ie != null) {
				lemma = ie.getLemma();
			}
			else {
				lemma = lemma.toLowerCase();
			}
		}
		else if(!t.getPos().startsWith("NN")) {
			lemma = lemma.toLowerCase();
		}
		return lemma;
	}
	
}