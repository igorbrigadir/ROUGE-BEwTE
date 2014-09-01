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

package tratz.parse.featgen;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import tratz.parse.ml.ParseModel;
import tratz.parse.types.Token;
import tratz.parse.types.TokenPointer;

/**
 * The interface for a parse feature generator (generates features for parsing)
 *
 */
public interface ParseFeatureGenerator extends Serializable {
	
	public void genFeats(Set<String> fts, ParseModel model, List<Token> tokens, TokenPointer ptr, List[] currentArcs);
	public int getContextWidth();
}