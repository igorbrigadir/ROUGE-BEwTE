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

import tratz.jwni.IndexEntry;
import tratz.jwni.LexPointer;
import tratz.jwni.POS;
import tratz.jwni.PointerType;
import tratz.jwni.Sense;
import tratz.jwni.WordNet;


/**
 * Feature Extraction Rule that returns the list of sentence frames for a given
 * word in WordNet
 *
 */
public class FramesFER extends AbstractWordNetFER {
	
	private static final long serialVersionUID = 1L;

	@Override
	public Set<String> generateFeatures(String term, String type, Set<String> productions) {
		POS pos = getPosForType(type);
		if(pos != null) {
		IndexEntry ie = WordNet.getInstance().lookupIndexEntry(pos, term);
		if(ie != null) {
			Sense[] senses = ie.getSenses();
			for(int s = 0; s < senses.length && s < mMaxSenseNum; s++) {
				if(pos == POS.NOUN) {
				LexPointer[][] lps = senses[s].getLexPointers(PointerType.DERIVED_FORM);
				int index = senses[s].getKeyIndex(term);
				if(index > -1) {
					for(int i = 0; i < lps[index].length; i++) {
						Sense targetSense = lps[index][i].getTargetSense();
						for(Sense.Key key : targetSense.getKeys()) {
							int[] frames = key.getFrames();
							if(frames != null) {
								for(int j = 0; j < frames.length; j++) {
									productions.add(Integer.toString(frames[j]));
								}
							}
						}
					}
				}
				}
				else {
					for(Sense.Key key : senses[s].getKeys()) {
						int[] frames = key.getFrames();
						if(frames != null) {
							final int numFrames = frames.length;
							for(int j = 0; j < numFrames; j++) {
								productions.add(Integer.toString(frames[j]));
							}
						}
					}
				}
			}
		}		
		}
		return productions;
	}
	
}