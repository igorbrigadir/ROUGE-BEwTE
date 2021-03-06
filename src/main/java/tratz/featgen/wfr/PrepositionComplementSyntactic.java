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
 * Returns the complement of a preposition
 *
 */
public class PrepositionComplementSyntactic extends AbstractWordFindingRule {
	
	private static final long serialVersionUID = 1L;

	@Override
	public Set<Token> getProductions(List<Token> tokenList, Parse parse, int tokenIndex) {
		Set<Token> results = new HashSet<Token>();
		
		Token head = tokenList.get(tokenIndex);
		if(head != null) {
			Arc garc = parse.getHeadArcs()[head.getIndex()];
			if(garc != null && ParseConstants.SUBORDINATE_CLAUSE_MARKER_DEP.equals(garc.getDependency())) {
				Token subordinatedClauseHead = garc.getHead();
				results.add(subordinatedClauseHead);
			}
			else {
				List<Arc> children = parse.getDependentArcLists()[head.getIndex()];
				if(children != null) {
					for(Arc child : children) {
						if(child.getDependency().matches("pobj|pcomp")) {
							results.add(child.getChild());
						}
					}
				}
			}
		}
		return results;
	}
	
}