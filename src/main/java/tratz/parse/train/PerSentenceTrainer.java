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

package tratz.parse.train;

import java.util.List;

import tratz.parse.featgen.ParseFeatureGenerator;
import tratz.parse.ml.ParseModel;
import tratz.parse.types.Arc;
import tratz.parse.types.Token;

/**
 * This class defines the interface for learning from a single sentence.
 * The implementation of the <code>train</code> method is responsible for performing
 * any necessary updates. This interface way well be altered in the future.
 *
 */
public interface PerSentenceTrainer {
	
	public static final class TrainingResult {
		public final boolean maxUpdatesExceeded;
		public final boolean fatalError;
		public final int numUpdatesMade;
		
		public TrainingResult(boolean maxUpdatesExceeded, boolean fatalError, int numUpdatesMade) {
			this.maxUpdatesExceeded = maxUpdatesExceeded;
			this.fatalError = fatalError;
			this.numUpdatesMade = numUpdatesMade;
		}
	}
	
	/**
	 * 
	 * @param tokens - The tokens to be parsed 
	 * @param goldArcs - The <code>Arc</code>s in the gold parse (an array of <code>List</code>s index by <code>Token</code> index)
	 * @param goldTokenToHead - array of <code>Arc</code>s; at position i, the <code>Arc</code>'s child is the <code>Token</code> at index i
	 * @param w - the parse model being trained
	 * @param featGen - the feature generator being used
	 * @param tokenToSubcomponentHead - a mapping from tokens to projective subcomponent heads
	 * @param projectiveIndices - index indices the order in which each <code>Token</code> is visited in a left-to-right depth-first-search of the parse tree  
	 * @return a <code>TrainingResult</code>
	 * @throws Exception
	 */
	public TrainingResult train(
	         List<Token> tokens, 
	         List[] goldArcs, 
	         Arc[] goldTokenToHead, 
	         ParseModel w, 
	         ParseFeatureGenerator featGen, 
	         Token[] tokenToSubcomponentHead, 
	         int[] projectiveIndices) throws Exception;
	
}