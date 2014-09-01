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
import tratz.jwni.WordNet;


public class IsLexicalizedFER extends AbstractFeatureRule {
	
	private static final long serialVersionUID = 1L;

	public Set<String> getProductions(String input, String type, Set<String> productions) {
		String parts[] = input.split("\\s+");
		if(parts.length == 2) {
			String pl = parts[0];
			String pr = parts[1];
				IndexEntry e1=null;
				if(((WordNet.getInstance().lookupIndexEntry(POS.NOUN, pl+pr)) != null)
						|| ((e1=WordNet.getInstance().lookupIndexEntry(POS.NOUN, pl+" "+pr)) != null && e1.getLemma().contains("_"))) {
					productions.add("islexicalized");
			}
		}
		return productions;
	}
	
}