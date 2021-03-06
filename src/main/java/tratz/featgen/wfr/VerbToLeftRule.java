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
import java.util.Set;

import tratz.parse.types.Arc;
import tratz.parse.types.Parse;
import tratz.parse.types.Token;
import tratz.util.TreebankConstants;

/**
 * Word-finding rule for finding the verb to the left of the input token.
 *
 */
public class VerbToLeftRule extends AbstractWordFindingRule {
	
	private static final long serialVersionUID = 1L;

	@Override
	public Set<Token> getProductions(List<Token> tokenList, Parse parse, int headIndex) {
		Set<Token> results = new HashSet<Token>();
		for(int i = headIndex-1; i >= 0; i--) {
			Token tok = tokenList.get(i);
			String pos = tok.getPos();
			if(TreebankConstants.VERB_LABELS.contains(pos)) {
				// found the first verb to the left
				results.add(tok);
				break;
			}
		}
		
		return results;
	}
	
}