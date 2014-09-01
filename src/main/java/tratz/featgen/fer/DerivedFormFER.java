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
 * Feature Extraction Rule for returning a list of 'derivationally-related' terms
 * according to WordNet.
 *
 */
public class DerivedFormFER extends AbstractWordNetFER {
	
	private static final long serialVersionUID = 1L;

	public Set<String> generateFeatures(String input, String type, Set<String> productions) {
		POS pos = getPosForType(type);
		if(pos != null) {
			IndexEntry entry = WordNet.getInstance().getMorpho().lookupIndexEntry(pos, input, true);
			if(entry != null) {
				for(Sense sense : entry.getSenses()) {
					Pointer[] derForms = sense.getPointers(PointerType.DERIVED_FORM);
					for(Pointer ptr : derForms) {
						for(Key key : ptr.getTargetSense().getKeys()) {
							productions.add(key.getLemma());
						}				
					}
				}
			}
		}
		return productions;
	}
	
}