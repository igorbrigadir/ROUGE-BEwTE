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
import tratz.jwni.Pointer;
import tratz.jwni.PointerType;
import tratz.jwni.Sense;
import tratz.jwni.WordNet;
import tratz.jwni.Sense.Key;


/**
 * Feature extraction rule that returns WordNet hypernyms
 *
 */
public class AllHypernymsFER extends AbstractWordNetFER {
	
	private static final long serialVersionUID = 1L;

	public Set<String> generateFeatures(String input, String type, Set<String> productions) {
		if(mOverridePos != null) {
			type = mOverridePos;
		}
		else if(type == null) {
			type = mDefaultPos;
		}
		POS pos = getPosForType(type);
		if(pos != null) {
			IndexEntry entry = WordNet.getInstance().getMorpho().lookupIndexEntry(pos, input, true);
			if(entry != null) {
				//Sense sense = entry.getSenses()[0];
				for(Sense sense : entry.getSenses()) {
				Pointer[] hyperPointers = sense.getSemPointers(PointerType.HYPERNYM, PointerType.INSTANCE_HYPERNYM);
				for(Pointer p : hyperPointers) {
					addHypernyms(productions, p.getTargetSense(), 0);
				}
				}
			}
		}
		return productions;
	}
	
	private void addHypernyms(Set<String> productions, Sense sense, int level) {
		if(level < 15) {
			Pointer[] hyperPointers = sense.getSemPointers(PointerType.HYPERNYM, PointerType.INSTANCE_HYPERNYM);
			for(Key key : sense.getKeys()) {
				productions.add(key.getLemma());
			}
			for(Pointer p : hyperPointers) {
				addHypernyms(productions, p.getTargetSense(), level+1);
			}
		}
	}
	
}