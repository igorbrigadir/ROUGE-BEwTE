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

package tratz.featgen.wfr;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tratz.parse.types.Parse;
import tratz.parse.types.Token;

/**
 * Word-finding rule that finds a word to the right of the input token.
 *
 */
public class WordToRightRule extends AbstractWordFindingRule {
	
	private static final long serialVersionUID = 1L;
	
	public final static String PARAM_HOW_FAR_TO_RIGHT = "HowFarToRight";
	
	private int mHowFarToRight;
	
	public WordToRightRule(int wordToRight) {
		mHowFarToRight = wordToRight;
	}
	
	public WordToRightRule() {
		this(1);
	}
	
	@Override
	public void init(Map<String, String> params) {
		String howFarToRight = params.get(PARAM_HOW_FAR_TO_RIGHT);
		if(howFarToRight != null) {
			mHowFarToRight = Integer.parseInt(howFarToRight);
		}
	}
	
	@Override
	public Set<Token> getProductions(List<Token> tokenList, Parse parse,  int headIndex) {
		Set<Token> results = new HashSet<Token>();
		if(headIndex > -1 && headIndex < tokenList.size()-mHowFarToRight) {
			Token tok = tokenList.get(headIndex+mHowFarToRight);
			results.add(tok);
		}
		return results;
	}
	
}