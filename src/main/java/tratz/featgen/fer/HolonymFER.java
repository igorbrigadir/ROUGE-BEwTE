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

import java.util.Map;
import java.util.Set;

import tratz.featgen.InitException;
import tratz.jwni.IndexEntry;
import tratz.jwni.POS;
import tratz.jwni.Pointer;
import tratz.jwni.PointerType;
import tratz.jwni.Sense;
import tratz.jwni.WordNet;
import tratz.jwni.Sense.Key;

/**
 * Feature Extraction Rule for returning WordNet holonyms
 *
 */
public class HolonymFER extends AbstractWordNetFER {
	
	private static final long serialVersionUID = 1L;
	
	public final static String PARAM_HOLONYM_TYPE = "HolonymType";
	public final static String PARAM_INCLUDE_HYPERNYM_HOLONYMS = "IncludeHypernymHolonyms";
	
	public final static String PART_TYPE = "Part";
	public final static String MEMBER_TYPE = "Member";
	public final static String SUBSTANCE_TYPE = "Substance";
	
	private PointerType mPointerType;
	protected boolean mIncludeHypernymHolonyms;
	
	@Override
	public void init(Map<String, String> params) throws InitException {
		super.init(params);
		String holonymType = params.get(PARAM_HOLONYM_TYPE);
		if(holonymType.equals(PART_TYPE)) {
			mPointerType = PointerType.PART_HOLONYM;
		}
		else if(holonymType.equals(MEMBER_TYPE)) {
			mPointerType = PointerType.MEMBER_HOLONYM;
		}
		else if(holonymType.equals(SUBSTANCE_TYPE)) {
			mPointerType = PointerType.SUBSTANCE_HOLONYM;
		}
		String includeHypernymHolonymsString = params.get(PARAM_INCLUDE_HYPERNYM_HOLONYMS);
		if(includeHypernymHolonymsString != null) {
			mIncludeHypernymHolonyms = Boolean.parseBoolean(includeHypernymHolonymsString);
		}
	}
	
	@Override
	public Set<String> generateFeatures(String input, String type, Set<String> productions) {
		POS pos = getPosForType(type);
		if(pos == POS.NOUN) {
			IndexEntry entry = WordNet.getInstance().lookupIndexEntry(POS.NOUN, input);
			if(entry != null) {
				Sense[] senses = entry.getSenses();
				for(int i = 0; i < senses.length && i < mMaxSenseNum; i++) {
					addHolonyms(senses[i], productions);
					if(mIncludeHypernymHolonyms) {
						addHypernyms(productions, senses[i], 1);
					}
				}
			}
		}
		return productions;
	}
	
	private void addHypernyms(Set<String> productions, Sense sense, int level) {
		if(level < 15) {
			Pointer[] hyperPointers = sense.getPointers(PointerType.HYPERNYM);
			for(Pointer p : hyperPointers) {
				addHypernyms(productions, p.getTargetSense(), level+1);
			}
		}
	}
	
	private void addHolonyms(Sense sense, Set<String> productions) {
		Pointer[] pointers = sense.getPointers(mPointerType);
		for(Pointer p : pointers) {
			for(Key key : p.getTargetSense().getKeys()) {
				productions.add(key.getLemma());
				productions.add("has" + mPointerType);
			}
		}
	}	
}