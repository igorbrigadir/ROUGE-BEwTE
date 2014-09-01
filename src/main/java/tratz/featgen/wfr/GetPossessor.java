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

import tratz.jwni.IndexEntry;
import tratz.jwni.POS;
import tratz.jwni.WordNet;
import tratz.parse.types.Arc;
import tratz.parse.types.Parse;
import tratz.parse.types.Token;
import tratz.parse.util.ParseConstants;

/**
 * Looks up the possessor of the given word
 *
 */
public class GetPossessor extends AbstractWordFindingRule {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public void init(Map<String, String> params) {
		
	}

	@Override
	public Set<Token> getProductions(List<Token> tokenList, Parse parse,  int tokIndex) {
		Set<Token> results = new HashSet<Token>();
		Token proposedPossessee = tokenList.get(tokIndex);
		
		List<Arc> children = parse.getDependentArcLists()[proposedPossessee.getIndex()];
		if(children != null) {
			for(Arc childArc : children) {
				if(childArc.getDependency().equals(ParseConstants.POSSESSOR_DEP)) {
					Token modifier = childArc.getChild();
					String pl = modifier.getText();
					if(modifier.getText().toLowerCase().matches("(corp|co|plc|inc|ag|ltd|llc)\\.?")) {
						pl = "corporation";
					}
					else if(modifier.getPos().startsWith("NNP")) {
						IndexEntry ie = WordNet.getInstance().lookupIndexEntry(POS.NOUN, pl);
						if(ie == null) {
							StringBuilder nounMods = new StringBuilder();
							List<Arc> arcs = parse.getDependentArcLists()[modifier.getIndex()];
							if(arcs != null) {
							for(Arc a : arcs) {
								if(a.getDependency().equals(ParseConstants.NOUN_COMPOUND_DEP)) {
									nounMods.append(a.getChild().getText()+" ");
								}
							}
							if(nounMods.length() > 0) {
								String newLeftString = nounMods.toString() + pl;
								ie = WordNet.getInstance().lookupIndexEntry(POS.NOUN, newLeftString);
								if(ie != null) {
									pl = ie.getLemma();
								}
							}
							}
						}
					}
					
					String pos = modifier.getPos();
					if(pl.toLowerCase().equals("her") || 
							pl.toLowerCase().equals("his") || 
							pl.toLowerCase().equals("my") || 
							pl.toLowerCase().equals("your")) {
						pl = "person";
						pos = "NN";
					}
					
					results.add(new Token(pl, pos, modifier.getIndex()));
					break;
				}
			}
		}
		return results;
	}
	
}