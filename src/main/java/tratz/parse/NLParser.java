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

package tratz.parse;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import tratz.parse.featgen.ParseFeatureGenerator;
import tratz.parse.ml.ParseModel;
import tratz.parse.types.Arc;
import tratz.parse.types.Parse;
import tratz.parse.types.Sentence;
import tratz.parse.types.Token;
import tratz.parse.types.TokenPointer;
import tratz.parse.util.ParseConstants;
import tratz.types.IntArrayList;

/**
 * The parsing class. Implements Goldberg and Elhadad's (2010) algorithm with Nivre-style (2009)
 * reordering to support non-projectivity.
 *
 */
public class NLParser {
	
	public static long sFeatGenTime = 0;
	public static long sDotProductTime = 0;
	
	
	private ParseModel mModel;
	private ParseFeatureGenerator mFeatGen;
	
	public NLParser(ParseModel model, ParseFeatureGenerator featGen) {
		mModel = model;
		mFeatGen = featGen;
	}
	
	public NLParser(String modelFile) throws IOException, ClassNotFoundException {
		InputStream is = new BufferedInputStream(new FileInputStream(modelFile));
		if(modelFile.endsWith(".gz")) {
			is = new GZIPInputStream(is);
		}
		ObjectInputStream ois = new ObjectInputStream(is);
		mModel = (ParseModel)ois.readObject();
		mFeatGen = (ParseFeatureGenerator)ois.readObject();
		ois.close();
	}
	
	private IntArrayList getValues(ParseModel model, Set<String> fts, IntArrayList values, boolean addFeats) {
		
		for(String f : fts) {
			int index = model.getIndex(f,addFeats);
			if(index != Integer.MIN_VALUE) {
				values.add(index);
			}
		}
		return values;
	}
	
	public Parse parseSentence(Sentence sentence) {
		Parse returnValue = null;
		List<Token> tokens = sentence.getTokens();
		final int numTokens = tokens.size();
		if(numTokens >= 1) {
			
		List[] currentArcs = new List[numTokens+1];
		int creationOrderIndex = 1;
		double[] scores = new double[mModel.getActions().size()];
		int[] indices = new int[mModel.getActions().size()];
		
		TokenPointer first = null;
		// Create data structure
		TokenPointer[] tokenToPtr = new TokenPointer[numTokens+1];
		TokenPointer prev = null;
		for(int i = 0; i < numTokens; i++) {
			Token t = tokens.get(i);
			TokenPointer ptr = new TokenPointer(t, null, prev);
			if(i == 0) {
				first = ptr;
			}
			tokenToPtr[t.getIndex()] = ptr;
			if(prev != null) {
				prev.next = ptr;
			}
			prev = ptr;
		}
		
		IntArrayList[] featureCache = new IntArrayList[numTokens+1];
		ArrayList[] actionCache = new ArrayList[numTokens+1];
		boolean[] actionListStale = new boolean[numTokens+1];
		for(int i = 0; i < numTokens+1; i++) {
			actionListStale[i] = true;
		}
		
		// Reused item (less memory [de]allocation)
		final Set<String> ftSet = new HashSet<String>();
		
		while(first != null && first.next != null) {
			TokenPointer ptr = first;
			while(ptr != null) {
				if(actionListStale[ptr.tok.getIndex()]) {
					Token token = ptr.tok;
					ArrayList<ParseAction> actions = actionCache[token.getIndex()];
					
					IntArrayList features = featureCache[token.getIndex()];
					
					long sfeat = System.nanoTime();
					mFeatGen.genFeats(ftSet, mModel, tokens, tokenToPtr[token.getIndex()], currentArcs);
					if(features == null) {
						featureCache[token.getIndex()] = features = new IntArrayList(ftSet.size());
					}
					features.clear();
					//IntArrayList values = new IntArrayList(fts.size());
					featureCache[token.getIndex()] = features = getValues(mModel, ftSet, features, false);
					ftSet.clear();
					sFeatGenTime += System.nanoTime()-sfeat;
					//featTime += System.nanoTime()-start;
					
				
					final List<String> actionNames = mModel.getActions(ptr.tok, ptr.next == null ? null : ptr.next.tok, null);
					//if(actions == null) {
					if(actions == null) actionCache[token.getIndex()]= actions = new ArrayList<ParseAction>();
					int numActions = actionNames.size();
					int currentSize = actions.size();
					for(int i = currentSize-1; i >= numActions; i--) {
						actions.remove(i); // remove additional actions only
					}
					currentSize = actions.size();
					for(int i = currentSize; i < numActions; i++) {
						actions.add(new ParseAction(token, ptr, actionNames.get(i), 0d)); // fill up space as necessary
					}
					currentSize = actions.size();
					
					long start = System.nanoTime();
					mModel.score(actionNames, features, indices, scores);
					sDotProductTime += System.nanoTime()-start;
					for(int i = 0; i < numActions; i++) {
						String action = actionNames.get(i);
						ParseAction oldAction = actions.get(i);
						oldAction.actionName = action;
						oldAction.score = scores[i];
					}
					actionListStale[ptr.tok.getIndex()] = false;
				}
				ptr = ptr.next;
			}
			
			ParseAction bestAction = getBestAction(first, actionCache, tokenToPtr, currentArcs);
			if(bestAction != null) {
				first = performAction(tokens, first, tokenToPtr, bestAction, actionListStale, featureCache, currentArcs, creationOrderIndex, mFeatGen.getContextWidth());
				if(!bestAction.actionName.startsWith("SWAP")) {
					creationOrderIndex++;
				}
			}
			else {
				// Something bad happened, let's print this out...
				for(Token tok : tokens){
					System.err.print(tok.getText()+"/" + tok.getPos() + " ");
				}
				System.err.println();
				TokenPointer tptr = first;
				while(tptr != null) {
					System.err.print(tptr.tok.getText()+"/"+tptr.tok.getPos()+" ");
					
					ArrayList<ParseAction> actions = actionCache[tptr.tok.getIndex()];
					if(actions != null) {
						System.err.print("**");
						for(ParseAction act : actions) {
							System.err.print(act.actionName+ "_"+act.token.getText()+ "_"+isValid(act, tokenToPtr, currentArcs)+" ");
						}
						System.err.print("**");
					}
					tptr = tptr.next;
				}
				System.err.println();
				
				throw new RuntimeException("Bizarre. No valid action found. Unsupported part-of-speech perhaps?");
			}
		}
		
			Token root = new Token("[ROOT]", 0);
			List<Arc> sentenceArcs = new ArrayList<Arc>();
			for(List<Arc> arcs : currentArcs) {
				if(arcs != null) {
					sentenceArcs.addAll(arcs);
				}
			}
			sentenceArcs.add(new Arc(first.tok, root, ParseConstants.ROOT_DEP));
			returnValue = new Parse(sentence, root, sentenceArcs);
		}
		return returnValue; 
	}
	
	public ParseAction getBestAction(TokenPointer first, 
									List[] actionCache, 
									TokenPointer[] tokenToPtr, 
									List[] currentArcs) {
		ParseAction bestAction = null;
		double bestActionScore = -99999999;
		TokenPointer ptr = first;
		
		while(ptr != null) {
			for(ParseAction action : (List<ParseAction>)actionCache[ptr.tok.getIndex()]) {
				//System.err.println(action.actionName + " " + action.score);
				if(action.score > bestActionScore && isValid(action, tokenToPtr, currentArcs)) {						
					bestActionScore = action.score;
					bestAction = action;
				}
			}
			ptr = ptr.next;
		}
		return bestAction;	
	}
	
	protected boolean isValid(ParseAction action, TokenPointer[] tokenToPtr,List[] tokenToArcs) {
		TokenPointer ptr = tokenToPtr[action.token.getIndex()];
		boolean isValid = false;
		if(action.actionName.equals("SWAPRIGHT")) {
			isValid = ptr.next != null && action.token.getIndex() < ptr.next.tok.getIndex(); 
		}
		else if(action.actionName.equals("SWAPLEFT")) {
			isValid = ptr.prev != null && action.token.getIndex() > ptr.prev.tok.getIndex(); 
		}
		else if(ptr.next != null){
			isValid = true;
		}
		return isValid;
	}
	

	public static TokenPointer performAction(List<Token> sentence, 
											  TokenPointer first, 
											  TokenPointer[] tokenToPtr, 
											  ParseAction action,
											  boolean[] actionListStale,
											  IntArrayList[] tokenToFeatures,
											  List[] currentArcs,
											  int actionNum,
											  int contextWidth) {
		int widthToReset = contextWidth/2+1;
		// Clear caches as necessary
		TokenPointer clearPtr = tokenToPtr[action.token.getIndex()];
		TokenPointer save = clearPtr;
		for(int i = 0; i < widthToReset+1 && clearPtr != null; i++, clearPtr = clearPtr.next) {
			actionListStale[clearPtr.tok.getIndex()] = true;
			tokenToFeatures[clearPtr.tok.getIndex()].clear();// = null;
		}
		
		clearPtr = save.prev;
		for(int i = 0; i < widthToReset && clearPtr != null; i++,clearPtr = clearPtr.prev) {
			actionListStale[clearPtr.tok.getIndex()] = true;
			tokenToFeatures[clearPtr.tok.getIndex()].clear();// = null;
		}
		
		// perform action
		TokenPointer actionPtr = save;
		if(action.actionName.equals("SWAPRIGHT")) {
			TokenPointer nextptr = actionPtr.next;
			TokenPointer prevptr = actionPtr.prev;
			if(nextptr.next != null) {
				nextptr.next.prev = actionPtr;
			}
			actionPtr.next = nextptr.next;
			actionPtr.prev = nextptr;
			nextptr.next = actionPtr;
			nextptr.prev = prevptr;
			if(prevptr != null) {
				prevptr.next = nextptr;
			}
			
			if(actionPtr == first) {
				first = nextptr;
			}
		}
		else if(action.actionName.equals("SWAPLEFT")) {
				TokenPointer nextptr = actionPtr.next;
				TokenPointer prevptr = actionPtr.prev;
				if(prevptr.prev != null) {
					prevptr.prev.next = actionPtr;
				}
				else {
					first = actionPtr;
				}
				prevptr.next = actionPtr.next;
				actionPtr.next = prevptr;
				actionPtr.prev = prevptr.prev;
				prevptr.prev = actionPtr;
				if(nextptr != null) {
					nextptr.prev = prevptr;
				}
			}
		else {
			// Create the new arc
			String dependency = action.actionName.substring(0, action.actionName.length()-1);
			TokenPointer head = null, child = null;
			if(action.actionName.endsWith("l")) {
				head = actionPtr.next;
				child = actionPtr;
			}
			else if(action.actionName.endsWith("r")) {
				//head = actionPtr.prev;
				head = actionPtr;
				child = actionPtr.next;
			}
			else {
				throw new RuntimeException("ERROR. Unexpected action type: " + action.actionName);
			}
			List<Arc> arcs = currentArcs[head.tok.getIndex()];
			if(arcs == null) {
				currentArcs[head.tok.getIndex()] = arcs = new ArrayList<Arc>();
			}
			arcs.add(new Arc(child.tok, head.tok, dependency, actionNum));
			
			// Delete the child from the list of tokens to process
			TokenPointer childprev = child.prev;
			TokenPointer childnext = child.next;
			if(childprev != null) {
				childprev.next = childnext;
			}
			if(childnext != null) {
				childnext.prev = childprev;
			}
			if(child == first) {
				first = childnext;
			}
		}
		return first;
	}
	
}