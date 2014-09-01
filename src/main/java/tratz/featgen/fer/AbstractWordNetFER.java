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
import tratz.jwni.LexPointer;
import tratz.jwni.POS;
import tratz.jwni.PointerType;
import tratz.jwni.Sense;
import tratz.jwni.WordNet;
import tratz.util.TreebankConstants;

/**
 * Abstract feature extraction rule for WordNet-based feature extraction rules.
 */
abstract public class AbstractWordNetFER extends AbstractFeatureRule {
	
	private static final long serialVersionUID = 1L;
	
	public final static String PARAM_DEFAULT_POS = "DefaultPos";
	public final static String PARAM_OVERRIDE_POS = "OverridePos";
	public final static String PARAM_MAX_SENSE_NUM = "MaxSenseNum";
	public final static String PARAM_DERIVED_POS = "DerivedInputPos";
	
	protected String mDefaultPos;
	protected String mOverridePos;
	protected int mMaxSenseNum = Integer.MAX_VALUE;
	protected POS mDerivedInputPos;
	
	@Override
	public void init(Map<String, String> params) throws InitException {
		mDefaultPos = params.get(PARAM_DEFAULT_POS);
		mOverridePos = params.get(PARAM_OVERRIDE_POS);
		String mDerivedPosString = params.get(PARAM_DERIVED_POS);
		if(mDerivedPosString != null) {
			mDerivedInputPos = getPosForType(mDerivedPosString);
			if(mDerivedInputPos == null) {
				throw new InitException("Invalid derived pos type: " + mDerivedPosString);
			}
		}
		String maxSenseString = params.get(PARAM_MAX_SENSE_NUM);
		if(maxSenseString != null) {
			mMaxSenseNum = Integer.parseInt(maxSenseString);
		}
	}
	
	
	final public Set<String> getProductions(String input, String type, Set<String> productions) {
		if(mOverridePos != null) {
			type = mOverridePos;
		}
		else if(type == null) {
			type = mDefaultPos;
		}
		if(mDerivedInputPos != null) {
			input = getDerived(input, mDerivedInputPos, getPosForType(type));
		}
		return input == null ? productions : generateFeatures(input, type, productions);
	}
	
	abstract public Set<String> generateFeatures(String input, String type, Set<String> productions);
	
	protected POS getPosForType(String type) {
		POS pos = null;
		if(TreebankConstants.NOUN_LABELS.contains(type)) {
			pos = POS.NOUN;
		}
		else if(TreebankConstants.VERB_LABELS.contains(type)) {
			pos = POS.VERB;
		}
		else if(TreebankConstants.ADJ_LABELS.contains(type)) {
			pos = POS.ADJECTIVE;
		}
		else if(TreebankConstants.ADV_LABELS.contains(type)) {
			pos = POS.ADVERB;
		}
		return pos;
	}
	
	protected static String getDerived(String s, POS pos, POS target) {
		IndexEntry ie = WordNet.getInstance().lookupIndexEntry(pos, s);
		Sense.Key result = null;
		if(ie != null) {
			outer:
			for(Sense sense : ie.getSenses()) {
				LexPointer[][] lps = sense.getLexPointers(PointerType.DERIVED_FORM);
				int index = sense.getKeyIndex(s);
				if(index > -1) {
					for(int i = 0; i < lps[index].length; i++) {
						if(lps[index][i].getTargetSense().getPOS() == target) {
							Sense tSense = lps[index][i].getTargetSense();
							result = tSense.getKeys()[lps[index][i].getTargetWordIndex()];
							if(result != null) {
								break outer;
							}
						}
					}
				}
			}
		}
		String lemma = result == null ? null : result.getLemma();
		return lemma;
	}
	
	
}