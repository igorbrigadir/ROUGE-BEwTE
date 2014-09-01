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
import tratz.parse.util.ParseConstants;

/**
 * Returns the syntactic governor of a word. If a subordinating conjunction is the input
 * token, it returns the head of the governing clause.
 *
 */
public class GetTokensBetweenWordAndPossessor extends AbstractWordFindingRule {
	
	private static final long serialVersionUID = 1L;

	@Override
	public Set<Token> getProductions(List<Token> tokenList, Parse parse, int tokIndex) {
		Set<Token> results = new HashSet<Token>();
		
		Token tok = tokenList.get(tokIndex);
		List<Arc> children = parse.getDependentArcLists()[tok.getIndex()];
		if(children != null) {
			for(Arc childArc : children) {
				if(childArc.getDependency().equals(ParseConstants.POSSESSOR_DEP)) {
					Token possessor = childArc.getChild();
					int possessorIndex = possessor.getIndex();
					int init, last;
					if(possessorIndex < tokIndex) {
						init = possessorIndex;
						last = tok.getIndex()-1;
					}
					else {
						// possessor should always be on the left in English...
						init = tok.getIndex();
						last = possessorIndex-1;
					}
					for(int i = init; i < last; i++) {
						results.add(tokenList.get(i));
					}
					break;// found the possessor
				}
			}
		}
		return results;
	}
	
}