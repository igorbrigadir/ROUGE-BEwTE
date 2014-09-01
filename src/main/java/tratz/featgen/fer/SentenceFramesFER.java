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
import tratz.jwni.POS;
import tratz.jwni.Sense;
import tratz.jwni.WordNet;
import tratz.jwni.Sense.Key;
import tratz.util.TreebankConstants;


/**
 * Feature extraction rule for returning the list of sentence frames associated
 * with a particular word (only applicable to verbs)
 *
 */
public class SentenceFramesFER extends AbstractWordNetFER {
	
	private static final long serialVersionUID = 1L;

	@Override
	public Set<String> generateFeatures(String input, String type, Set<String> productions) {
		POS pos = null;
		if(TreebankConstants.VERB_LABELS.contains(type)) {
			pos = POS.VERB;		
			IndexEntry entry = WordNet.getInstance().getMorpho().lookupIndexEntry(pos, input, true);
			if(entry != null) {
				Sense[] senses = entry.getSenses();
				for(int s = 0; s < senses.length && s < mMaxSenseNum; s++) {
					Sense sense = senses[s];
					for(Key key : sense.getKeys()) {
						int[] sentenceFrames = key.getFrames();
						int numFrames = sentenceFrames.length;
						for(int i = 0; i < numFrames; i++) {
							productions.add(Integer.toString(sentenceFrames[i]));
						}
					}
				}
			}
		}
		return productions;
	}
	
}