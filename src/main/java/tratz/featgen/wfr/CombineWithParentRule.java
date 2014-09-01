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

/**
 */
public class CombineWithParentRule extends AbstractWordFindingRule {
	
	private static final long serialVersionUID = 1L;

	@Override
	public Set<Token> getProductions(List<Token> tokenList, Parse parse, int headIndex) {
		Set<Token> results = new HashSet<Token>();

		Token tok = tokenList.get(headIndex);
		Arc headArc = parse.getHeadArcs()[tok.getIndex()];
		if(tok != null && headArc != null) {
			Token headToken = headArc.getHead();
			if(headToken != null) {
				results.add(new Token(tok.getText() + "\t" + headToken.getText(), "NN", -1));
			}
		}
		
		return results;
	}
	
}