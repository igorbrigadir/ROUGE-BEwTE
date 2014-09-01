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

package tratz.featgen.fer;

import java.util.Set;

import tratz.jwni.POS;
import tratz.jwni.WordNet;


/**
 * Feature Extraction Rule that indicates all the parts-of-speech
 * WordNet does and does not list for a given word.
 *
 */
public class WordNetPosTypesFER extends AbstractFeatureRule {
	
	public final static long serialVersionUID = 1;
	
	public Set<String> getProductions(String term, String type, Set<String> features) {
		WordNet wn = WordNet.getInstance();
		if(wn.lookupIndexEntry(POS.ADJECTIVE, term) != null) {
			features.add("ISj");
		}	
		else {
			features.add("notISj");
		}
		if(wn.lookupIndexEntry(POS.ADVERB, term) != null) {
			features.add("ISr");
		}		
		else {
			features.add("notISr");
		}
		if(wn.lookupIndexEntry(POS.VERB, term) != null) {
			features.add("ISv");
		}
		else {
			features.add("notISv");
		}
		if(wn.lookupIndexEntry(POS.NOUN, term) != null) {
			features.add("ISn");
		}
		else {
			features.add("notISn");
		}
		return features;
	}
}