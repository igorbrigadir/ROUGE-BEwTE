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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tratz.parse.NLParser;
import tratz.parse.ParseAction;
import tratz.parse.featgen.ParseFeatureGenerator;
import tratz.parse.ml.ParseModel;
import tratz.parse.types.Arc;
import tratz.parse.types.Token;
import tratz.parse.types.TokenPointer;
import tratz.types.IntArrayList;

/**
 * The standard <code>PerSentenceTrainer</code>.
 * This code is not particularly easy to read. TODO: fix this
 *
 */
public class StandardPerSentenceTrainer implements PerSentenceTrainer {
	
	public final static double DEFAULT_MAX_UPDATE = 0.1;
	public final static int DEFAULT_MAX_ITERATIONS = 10;
	
	private double mMaxUpdate;
	private int mMaxIterations;
	
	public StandardPerSentenceTrainer() {
		mMaxUpdate = DEFAULT_MAX_UPDATE;
        mMaxIterations = DEFAULT_MAX_ITERATIONS;
	}
	
	public IntArrayList getValues(ParseModel model, Set<String> fts, boolean addFeats) {
		IntArrayList values = new IntArrayList(fts.size());
		for(String f : fts) {
			int index = model.getIndex(f,addFeats);
			if(index != Integer.MIN_VALUE) {
				values.add(index);
			}
		}
		return values;
	}
	
	public static boolean hasAllItsDependents(Token topOfStack, List<Arc> arcListFull, List<Arc> arcListWorking) {
		int numFull = arcListFull == null ? 0 : arcListFull.size();
		int numWorking = arcListWorking == null ? 0 : arcListWorking.size();
		return numFull == numWorking;
	}
	
	private PenaltyFunction mPenaltyFunction = new DefaultPenaltyFunction();
	
	public TrainingResult train(
			         List<Token> sentence, 
			         List[] goldArcs, 
			         Arc[] goldTokenToHead, 
			         ParseModel w, 
			         ParseFeatureGenerator featGen, 
			         Token[] tokenToSubcomponentHead, 
			         int[] projectiveIndices) throws Exception {
		boolean maxIterationsReached = false, fatalError = false;
        
		// holder for action indices
		int[] indicesHolder = new int[w.getActions().size()];
		// holder for action scores
		double[] scores = new double[w.getActions().size()];
		
		// used for parameter averaging
		w.incrementCount();
		
		
		final int numTokens = sentence.size();
		int numInvalids = 0;
		List[] currentArcs = new List[numTokens+1];
		
		// Create the linked-list data structure as well as the action-is-stale holder
		TokenPointer first = null;
		TokenPointer[] tokenToPtr = new TokenPointer[numTokens+1];
		boolean[] actionListStale = new boolean[numTokens+1];
		TokenPointer prev = null;
		for(int i = 0; i < numTokens; i++) {
			Token t = sentence.get(i);
			TokenPointer ptr = new TokenPointer(t, null, prev);
			if(first == null) first = ptr;
			tokenToPtr[t.getIndex()] = ptr;
			if(prev != null) {
				prev.next = ptr;
			}
			prev = ptr;
			actionListStale[i] = true;
		}
		actionListStale[numTokens] = true; // indices start at 1 for non-Root tokens
		
		// Hold the mapping from tokens to features (features as an IntArrayList of feature indices)
		IntArrayList[] featureCache = new IntArrayList[numTokens+1];
		
		// Hold the possible parse actions for a given token given its current context
		Map<Token, List<ParseAction>> actionCache = new HashMap<Token, List<ParseAction>>();
		// Set for collecting features. Created out here to allow for reused of object
		Set<String> feats = new HashSet<String>();
		
		// Enter the training loop for this sentence
		int numIterations = 0;
		while(first.next != null) {			
			numIterations++;
			// Calculate weights for all the actions
			ParseAction highestScoredValidAction = null;
			
			ParseAction highestScoredInvalidAction = null;
			ParseAction lowestScoredValidAction = null;
			double lowestScoredValidActionScore = Double.POSITIVE_INFINITY;
			
			double highestScoredInvalidActionScore = Double.NEGATIVE_INFINITY;
			double highestScoredValidActionScore = Double.NEGATIVE_INFINITY;
			
			// Not currently using this, but it might be useful sometime
			List<ParseAction> invalidActions = new ArrayList<ParseAction>();
			//List<ParseAction> validActions = new ArrayList<ParseAction>();
			
			TokenPointer ptr = first;
			while(ptr != null) {
				Token token = ptr.tok;
				
				
				List<ParseAction> actions = actionCache.get(token);
				// First check if the actions need to be updated
				if(actionListStale[token.getIndex()]) {
					actions = null; // TODO: in the future it might make sense to reuse the same List
					
					// Generate the features and get their indices
					featGen.genFeats(feats, w, sentence, ptr, currentArcs);
					IntArrayList values = getValues(w, feats, true);
					feats.clear();
					IntArrayList tokenFeatures = featureCache[token.getIndex()]; 
					featureCache[token.getIndex()] = tokenFeatures = values;
					
					// Get the list of possible actions
					List<String> actionNames = w.getActions(token, ptr.next == null ? null : ptr.next.tok, goldTokenToHead);
					// Score the actions
					w.scoreIntermediate(actionNames, tokenFeatures, indicesHolder, scores);
					
					if(actions == null) { // this will always be null (for now)
						actionCache.put(token, actions = new ArrayList<ParseAction>());
						final int numActions = actionNames.size();
						for(int i = 0; i < numActions; i++) {
							actions.add(new ParseAction(token, ptr, actionNames.get(i), scores[i]));
						}
					}
					else {
						final int numActions = actionNames.size();
						for(int i = 0; i < numActions; i++) {
							ParseAction action = actions.get(i);
							action.score = scores[i];
						}
					}
					// Ok, we've updated the actions for this token. No longer stale.
					actionListStale[token.getIndex()] = false;
				}
				
				// Time to evaluate the actions... now this is the UGLY part
				for(ParseAction action : actions) {
					
					
					TokenPointer tmpPtr = null;
					int tokenIndex = action.token.getIndex();
					double penalty = mPenaltyFunction.calculatePenalty(tokenToPtr[tokenIndex], action, goldTokenToHead, goldArcs, currentArcs, tokenToSubcomponentHead, projectiveIndices);
					if(penalty > 0 ||
							// a valid swap is invalidated by an earlier valid non-SWAP if the SWAP is not simple (has all its dependents and token+/-2 is its head)
						(
							lowestScoredValidAction != null &&
							(
								(action.actionName.equals("SWAPRIGHT") && 
									!lowestScoredValidAction.actionName.equals("SWAPRIGHT")
									&&!((tmpPtr = tokenToPtr[tokenIndex].next) != null &&
										(tmpPtr = tmpPtr.next) != null &&
										hasAllItsDependentsAndIsAMatch(action.token, tmpPtr.tok, goldTokenToHead[tokenIndex], goldTokenToHead, goldArcs[tokenIndex], currentArcs[tokenIndex])
										)
								)
								|| 
								(action.actionName.equals("SWAPLEFT") && 
									!lowestScoredValidAction.actionName.equals("SWAPLEFT")
									&&!((tmpPtr = tokenToPtr[tokenIndex].prev) != null &&
										(tmpPtr = tmpPtr.prev) != null &&
										hasAllItsDependentsAndIsAMatch(action.token, tmpPtr.tok, goldTokenToHead[tokenIndex], goldTokenToHead, goldArcs[tokenIndex], currentArcs[tokenIndex])
										)
								)
							)
						 )
						    
					) {
						// If we got here, the action must be invalid
						invalidActions.add(action);
						if(action.score > highestScoredInvalidActionScore) {
							highestScoredInvalidAction = action;
							highestScoredInvalidActionScore = action.score;
						}
						
					}
					else {
						// If we get here, the action must be valid
						
						// Ok. Now let's check if the previous best action was some sort of SWAP, because we may want to invalidate it (SWAPs have low priority--weird things happen otherwise)
						if(lowestScoredValidAction != null) {
							tokenIndex = lowestScoredValidAction.token.getIndex();
							if(!action.actionName.startsWith("SWAP") &&
									(
											(lowestScoredValidAction.actionName.equals("SWAPRIGHT")
												&& !((tmpPtr = tokenToPtr[tokenIndex].next) != null &&
													(tmpPtr = tmpPtr.next) != null &&
													hasAllItsDependentsAndIsAMatch(lowestScoredValidAction.token, tmpPtr.tok, goldTokenToHead[tokenIndex], goldTokenToHead, goldArcs[tokenIndex], currentArcs[tokenIndex])
													)
											)
									   ||
									   		(lowestScoredValidAction.actionName.equals("SWAPLEFT")
									   			&& !((tmpPtr = tokenToPtr[tokenIndex].prev) != null &&
									   				(tmpPtr = tmpPtr.prev) != null &&
									   				hasAllItsDependentsAndIsAMatch(lowestScoredValidAction.token, tmpPtr.tok, goldTokenToHead[tokenIndex], goldTokenToHead, goldArcs[tokenIndex], currentArcs[tokenIndex])
									   				)
									   		)
									)
							  ) {
							
								invalidActions.add(lowestScoredValidAction);
								// no longer consider this swap to be valid because (it is non-simple AND) we found a new, non-SWAP valid action
								lowestScoredValidActionScore = Double.POSITIVE_INFINITY;
								highestScoredValidActionScore = Double.NEGATIVE_INFINITY;
								// probably don't need to include the penalty here.. but I could be mistaken
								if(lowestScoredValidAction.score > highestScoredInvalidActionScore) {
									highestScoredInvalidAction = lowestScoredValidAction;
									highestScoredInvalidActionScore = lowestScoredValidAction.score;
									lowestScoredValidAction = null;
									highestScoredValidAction = null;
								}
							}
						}
						
						// Alright, time to update the lowest/highest scored action holders
						if(action.score < lowestScoredValidActionScore || lowestScoredValidActionScore == Double.MAX_VALUE) {
							lowestScoredValidAction = action;
							lowestScoredValidActionScore = action.score;
						}
						if(action.score > highestScoredValidActionScore || Double.isInfinite(highestScoredValidActionScore)) {
							highestScoredValidAction = action;
							highestScoredValidActionScore = action.score;
						}
					}
				} // end for(ParseAction action : actions)
			
				// Moving on to the next token
				ptr = ptr.next;
			}
			
			if(lowestScoredValidAction == null) {
				// We should not get in here... if we do, something bad happened
				System.err.println("ERROR: No Valid Action Found! Do cycles or multi-headed tokens exist? Moving on to next sentence...");
				TokenPointer ptrZ = first;
				while(ptrZ != null && ptrZ.tok != null) {
					IntArrayList tokenFeatures = featureCache[ptrZ.tok.getIndex()];
					if(tokenFeatures == null) {
						//featureCache[ptrZ.tok.getIndex()] = tokenFeatures = featGen.genFeats(w, sentence, tokenLevelFeats, ptrZ, currentArcs, true);
					}
					List<String> actionNames2 = w.getActions(ptrZ.tok, ptrZ.next == null ? null : ptrZ.next.tok, goldTokenToHead);
				
					final int numActions = actionNames2.size();
					System.err.print(ptrZ.tok.getText() + " " + ptrZ.tok.getPos() + " ");
					w.scoreIntermediate(actionNames2, tokenFeatures, indicesHolder, scores);
					for(int i = 0; i < numActions; i++) {
						String actionName = actionNames2.get(i);
						System.err.print(actionName + ":"+scores[i]+", ");
					}
					System.err.println();
					ptrZ = ptrZ.next;	
				}
				fatalError=true;
				break;
			}

			// Determine if we should perform an action or if we should update the weights
			if(highestScoredInvalidActionScore < lowestScoredValidAction.score || numIterations > mMaxIterations) {
				// Chosen action is valid, so let's take it
				if(numIterations > mMaxIterations) {
					// check to see if we've exceeded the max updates-in-a-row threshold for this sentence
					maxIterationsReached = true;
				}
				numIterations = 0;
				// Perform the action, conceivably this could alter the pointer to the front of the linked list
				first = NLParser.performAction(sentence, first, tokenToPtr, highestScoredValidAction, actionListStale, featureCache, currentArcs,-1,featGen.getContextWidth());
			}
			else {
				// Chosen action is invalid
				numInvalids++;
				// So, let's update the model
				performUpdate(lowestScoredValidAction, highestScoredInvalidAction, first, actionListStale, featureCache, w);
			}
		}
		return new TrainingResult(maxIterationsReached, fatalError, numInvalids);
	}
	
	/**
	 * Update the model parameters
	 */
	private void performUpdate(ParseAction lowestScoredValidAction, 
							   ParseAction maxInvalidAction,
							   TokenPointer first,
							   boolean[] actionListStale,
							   IntArrayList[] featureCache,
							   ParseModel w) {
		ParseAction goodAction = lowestScoredValidAction;
		ParseAction badAction = maxInvalidAction;
		
		// Everything becomes stale because the model weights are changing
		for(TokenPointer tptr = first; tptr != null; tptr = tptr.next) {
			actionListStale[tptr.tok.getIndex()] = true;
		}
		
		// Hmm... Actually, would it make more sense to find the intersection of these and take the size of that?
		double denominator = featureCache[badAction.token.getIndex()].size()+featureCache[goodAction.token.getIndex()].size();
		
		// uniform penalty of 1
		double change = 1;
		// magnitude of error is typically around .0055-.0035 
		double update = Math.min(mMaxUpdate, (badAction.score-goodAction.score+change)/denominator);		
		
		// update feature vector weights
		w.update(badAction.actionName, featureCache[badAction.token.getIndex()], -update);
		w.update(goodAction.actionName, featureCache[goodAction.token.getIndex()], update);
	}
	
	private boolean hasAllItsDependentsAndIsAMatch(Token tokenToMove,
												   Token newNeighbor,
												   Arc goldHeadArc, 
												   Arc[] goldTokenToHead, 
												   List<Arc> goldArcs, 
												   List<Arc> currentArcs) {
		return goldHeadArc != null &&
		goldHeadArc.getHead() == newNeighbor &&
		hasAllItsDependents(tokenToMove, goldArcs, currentArcs);
	}
	
}