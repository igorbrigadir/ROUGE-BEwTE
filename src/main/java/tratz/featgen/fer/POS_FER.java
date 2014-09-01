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
 * Feature extraction rule that just returns the given part-of-speech
 *
 */
public class POS_FER extends AbstractFeatureRule {
	
	private static final long serialVersionUID = 1L;

	public Set<String> getProductions(String input, String type, Set<String> productions) {
		productions.add(type == null ? "null" : type);
		return productions;
	}
	
}