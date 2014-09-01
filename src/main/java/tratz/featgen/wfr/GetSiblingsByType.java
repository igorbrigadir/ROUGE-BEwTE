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

import tratz.parse.types.Arc;
import tratz.parse.types.Parse;
import tratz.parse.types.Token;

/**
 * Returns the siblings of a node in a dependency tree that match one of
 * perhaps multiple dependency types
 *
 */
public class GetSiblingsByType extends AbstractWordFindingRule {
	
	private static final long serialVersionUID = 1L;
	
	public final static String PARAM_DEP_TYPE = "DEP_TYPE";
	
	private Set<String> mDepType;
	
	@Override
	public void init(Map<String, String> params) {
		String deps = params.get(PARAM_DEP_TYPE);
		if(deps != null) {
			mDepType = new HashSet<String>();
			for(String dep : deps.split(",")) {
				mDepType.add(dep);
			}
		}
	}

	@Override
	public Set<Token> getProductions(List<Token> tokenList, Parse parse,  int tokenIndex) {
		Set<Token> results = new HashSet<Token>();
		Token childOfArc = tokenList.get(tokenIndex);
		Arc toHead = parse.getHeadArcs()[childOfArc.getIndex()];
		if(toHead != null && toHead.getHead() != null) {
			List<Arc> children = parse.getDependentArcLists()[toHead.getHead().getIndex()];
			for(Arc childArc : children) {
				if(mDepType == null || mDepType.contains(childArc.getDependency())) {
					results.add(childArc.getChild());
				}
			}
		}
		return results;
	}
	
}