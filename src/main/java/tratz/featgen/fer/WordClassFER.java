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

import tratz.util.TreebankConstants;

public class WordClassFER extends AbstractFeatureRule {
	
	private static final long serialVersionUID = 1L;

	@Override
	public Set<String> getProductions(String input, String type, Set<String> productions) {
		if(TreebankConstants.NOUN_LABELS.contains(type)) {
			productions.add("noun");
		}
		else if(TreebankConstants.VERB_LABELS.contains(type)) {
			productions.add("verb");
		}
		else if(TreebankConstants.ADJ_LABELS.contains(type)) {
			productions.add("adj");
		}
		else if(TreebankConstants.ADV_LABELS.contains(type)) {
			productions.add("adv");
		}
		return productions;
	}
	
}