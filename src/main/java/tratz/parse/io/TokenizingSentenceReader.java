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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tratz.parse.types.Arc;
import tratz.parse.types.Parse;
import tratz.parse.types.Sentence;
import tratz.parse.types.Token;

public class TokenizingSentenceReader implements SentenceReader {
	private final static String HIGH_DOUBLE_QUOTES;
	private final static String HIGH_SINGLE_QUOTES;
	
	static {
		// 145-148 are ANSI quotes, 132 is double low quotes
		// 8216-8223 quotes are supported by HTML (?Unicode?), 8242-8247 are multiple "primes"
		int[] doubleQuoteInts = new int[]{132,  171,187,147,148,8220,8221,8222,8223, 8243,8244,8246,8247};
		int[] singleQuoteInts = new int[]{145,146, 8216,8217,8218,8219, 8242,8245};
		// 8218 (looks like a comma, but maybe its a lower quote) ‚
		// 8220 & 8221 “”
		// 8216 & 8217 => ‘ ’
		StringBuilder buf = new StringBuilder();
		for(int i : doubleQuoteInts) {
			buf.append((char)i).append("|");
		}
		buf.setLength(buf.length()-1);
		HIGH_DOUBLE_QUOTES = buf.toString();
		buf = new StringBuilder();
		for(int i : singleQuoteInts) {
			buf.append((char)i).append("|");
		}
		buf.setLength(buf.length()-1);
		HIGH_SINGLE_QUOTES = buf.toString();
	}
	
	private Matcher mDoubleQuoteMatcher = Pattern.compile(HIGH_DOUBLE_QUOTES).matcher("");
	private Matcher mSingleQuoteMatcher = Pattern.compile(HIGH_SINGLE_QUOTES).matcher("");
	
	@Override
	public Parse readSentence(BufferedReader reader) throws IOException {
		Token root = new Token("[ROOT]", 0);
		List<Token> tokens = new ArrayList<Token>();
		List<Arc> arcs = new ArrayList<Arc>();
		Map<Integer, Token> numToToken = new HashMap<Integer, Token>();
		numToToken.put(0, root);
		String line = null;
		boolean done = false;
		while(!done && (line = reader.readLine()) != null) {
			done = true;
			line = mDoubleQuoteMatcher.reset(line).replaceAll("\"");
			line = mSingleQuoteMatcher.reset(line).replaceAll("'");
			
			String[] split = line.split("\\s+");
			
			int tokenNum = 0;
			for(int i = 0; i < split.length; i++) {
				String tokenString = split[i];
				if(tokenString.length() > 0) {
					tokenNum++;
					Token token = new Token(tokenString, tokenNum);
					tokens.add(token);
					numToToken.put(tokenNum, token);
				}
			}	
		}
		Parse result = null;
		if(line != null) {
			result = new Parse(new Sentence(tokens), root, arcs);
		}
		return result;
	}
	
}