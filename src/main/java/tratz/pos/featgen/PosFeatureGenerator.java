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

package tratz.pos.featgen;

import java.util.List;
import java.util.Set;

import tratz.parse.types.Token;

/**
 * Interface for part-of-speech feature generation
 */
public interface PosFeatureGenerator {
	
	public void init();
	public Set<String> getFeats(List<Token> tokens, int index);
	
}