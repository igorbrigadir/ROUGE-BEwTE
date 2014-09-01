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

package tratz.semantics.srl;

import java.io.Serializable;
import java.util.List;

import tratz.jwni.WordNet;
import tratz.parse.types.Parse;
import tratz.parse.types.Token;
import tratz.parse.util.NLParserUtils;
import tratz.semantics.ModelNameLookup;

public class SrlPredicatesModelNameLookup implements ModelNameLookup, Serializable {
	
	private static final long serialVersionUID = 1L;

	@Override
	public String determineModelName(String arcType, List<Token> tokens, Parse parse, int wordIndex) {
		String modelName = null;
		Token token = tokens.get(wordIndex);
		if(FeatureExtractionRoutinePredicates.POS_TO_INCLUDE.contains(token.getPos())) {
			modelName = NLParserUtils.getLemma(token, WordNet.getInstance()); 
		}
		return modelName;
	}
	
}