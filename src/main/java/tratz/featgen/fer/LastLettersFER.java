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

/**
 * Returns the 2 and 3 last letters of the input token.
 * Also checks if the token ends with 's' (which should be moved to the AffixFER in the future)
 *
 */
public class LastLettersFER extends AbstractFeatureRule {
	
	private static final long serialVersionUID = 1L;

	public Set<String> getProductions(String term, String type, Set<String> productions) {
		int termLength = term.length();
		if(term.length() > 2) {
			productions.add(term.substring(termLength-2));
		}
		if(term.length() > 3) {
			productions.add(term.substring(termLength-3));
		}
		if(term.endsWith("s") || term.endsWith("S")) {
			productions.add("s");
		}
		return productions;
	}
	
}