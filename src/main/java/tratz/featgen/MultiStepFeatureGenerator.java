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

package tratz.featgen;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import tratz.featgen.CombinationEntry.ComboType;
import tratz.featgen.fer.FERProductionException;
import tratz.featgen.fer.FeatureExtractionRule;
import tratz.parse.types.Parse;
import tratz.parse.types.Token;

/**
 * Used for feature generation. TODO: Ought to be replaced with a *much* more flexible system
 */
public class MultiStepFeatureGenerator implements Serializable {
	
	private static final long serialVersionUID = 1L;

	// instrumentation, for debug/timing...
	public static Map<FeatureExtractionRule, Long> ferTimes = new HashMap<FeatureExtractionRule, Long>();
	
	protected WfrEntry[] mWfrEntries;
	protected CombinationEntry[] mComboEntries;
	
	public MultiStepFeatureGenerator() {
		
	}
	
	public MultiStepFeatureGenerator(String wfrRulesFile, 
									 String ferRulesFile,
									 String comboRulesFile) throws Exception {
		MultiStepFeatureGeneratorConfigParser configReader = new MultiStepFeatureGeneratorConfigParser();
		Map<String, FeatureExtractionRule> featureRuleMap = configReader.readFeatureExtractionRuleMap(ferRulesFile);
		mWfrEntries = configReader.readComplexRules(wfrRulesFile, featureRuleMap);
		mComboEntries = configReader.readCombinationEntries(new File(comboRulesFile));
	}
	
	public Set<String> generateFeatures(List<Token> tokens, Parse parse, int wordIndex) {
		Set<String> feats = new HashSet<String>();
		Map<String, Set<String>> fullPrefixToProductions = new HashMap<String, Set<String>>();
		Map<tratz.parse.types.Token, Map<FeatureExtractionRule, Set<String>>> tokenToFerToProductions 
		= new HashMap<Token, Map<FeatureExtractionRule, Set<String>>>();
		for(WfrEntry wfrEntry : mWfrEntries) {
			Set<tratz.parse.types.Token> wfrResults = wfrEntry.getWfrRule().getProductions(tokens, parse, wordIndex);
			if(wfrResults != null && wfrResults.size() > 0) {
				List<FeatureExtractionRule> fers = wfrEntry.getFERs();
				for(FeatureExtractionRule fer : fers) {
					long start = System.nanoTime();
					for(Token token : wfrResults) {
						try {
							Map<FeatureExtractionRule, Set<String>> ferToFeats = tokenToFerToProductions.get(token);
							Set<String> productions = null;
							if(ferToFeats == null) {
								tokenToFerToProductions.put(token, ferToFeats = new HashMap<FeatureExtractionRule, Set<String>>());
							}
							productions = ferToFeats.get(fer);
							if(productions == null) {
								productions = fer.getProductions(token
									.getText(), token.getPos());
								if(productions != null) {
									ferToFeats.put(fer, productions);
								}
							}
							if(productions != null) {
								String fullPrefix = wfrEntry.getPrefix() + ":"
								+ fer.getPrefix() + ":";
								Set<String> prod = fullPrefixToProductions.get(fullPrefix);
								if(prod == null) {
									fullPrefixToProductions.put(fullPrefix, prod = new HashSet<String>(productions.size()));
								}
								for(String production : productions) {
									// TODO Should give some option to stop the lowercasing
									prod.add(production);//.toLowerCase());
								}
							}
						}
						catch(FERProductionException ferp) {
							// Just print for now
							ferp.printStackTrace();
						}
					}
					Long time = ferTimes.get(fer);
					if(time == null) {
						ferTimes.put(fer, time = new Long(0));
					}
					ferTimes.put(fer, time.longValue()+(System.nanoTime()-start));
				}
			}
		}
		
		for(CombinationEntry entry : mComboEntries) {
			Set<String> feats1 = fullPrefixToProductions.get(entry.getFullPrefix1());
			Set<String> feats2 = fullPrefixToProductions.get(entry.getFullPrefix2());
			CombinationEntry.ComboType comboType = entry.getComboType();
			String comboPrefix = entry.getComboPrefix();
			Set<String> comboResults = null;
			if(comboType == ComboType.CROSS_PRODUCT) {
				comboResults = cross(feats1, feats2);
			}
			else if(comboType == ComboType.INTERSECTION) {
				comboResults = intersect(feats1, feats2);
			}
			else if(comboType == ComboType.UNION) {
				comboResults = union(feats1, feats2);
			}
			if(comboResults != null) {
				String prefix = comboPrefix + ":";
				for(String result : comboResults) {
					feats.add(prefix + result);
				}
			}
		}
		
		for(String fullPrefix : fullPrefixToProductions.keySet()) {
			Set<String> productions = fullPrefixToProductions.get(fullPrefix);
			for(String production : productions) {
				feats.add(fullPrefix + production);
			}
		}
		return feats;
	}
	
	protected Set<String> intersect(Set<String> s1, Set<String> s2) {
		Set<String> intersection = null;
		if(s1 != null && s2 != null) {
			intersection = new HashSet<String>(s1);
			intersection.retainAll(s2);
		}
		return intersection;
	}
	
	protected Set<String> union(Set<String> s1, Set<String> s2) {
		Set<String> union = new HashSet<String>(s1);
		union.addAll(s2);
		return union;
	}
	
	protected Set<String> cross(Set<String> s1, Set<String> s2) {
		Set<String> cross = null;
		if(s1 != null && s2 != null) {
		cross = new HashSet<String>(s1.size()*s2.size());
			for(String s1s : s1) {
				for(String s2s : s2) {
					cross.add(s1s+"⺢"+s2s);
				}
			}
		}
		return cross;
	}
}