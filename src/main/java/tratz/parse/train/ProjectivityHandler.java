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
import java.util.List;

import tratz.parse.types.Arc;
import tratz.parse.types.Token;

public class ProjectivityHandler {
	
	private static class IntHolderX {
		int x;
	}
	public static void traverse(Token t, List[] tokenToArcs, int[] projectiveIndices) {
		traverse(t, tokenToArcs, new IntHolderX(), projectiveIndices);
	}
	private static void traverse(Token t, List[] tokenToArcs, IntHolderX val, int[] projectiveIndices) {
		List<Arc> arcs = tokenToArcs[t.getIndex()];
		List<Arc> preArcs = new ArrayList<Arc>();
		List<Arc> postArcs = new ArrayList<Arc>();
		if(arcs != null) {
			for(Arc arc : arcs) {
				if(arc.getChild().getIndex() < t.getIndex()) {
					preArcs.add(arc);
				}
				else {
					postArcs.add(arc);
				}
			}
		}
		for(Arc arc : preArcs) {
			traverse(arc.getChild(), tokenToArcs, val, projectiveIndices);
		}
		val.x++;
		//t.setProjectiveIndex(val.x);
		projectiveIndices[t.getIndex()] = val.x;
		for(Arc arc : postArcs) {
			traverse(arc.getChild(), tokenToArcs, val, projectiveIndices);
		}
	}
	
	public static boolean hasAllItsDependents(Token topOfStack, List<Arc> arcListFull, List<Arc> arcListWorking) {
		int numFull = arcListFull == null ? 0 : arcListFull.size();
		int numWorking = arcListWorking == null ? 0 : arcListWorking.size();
		return numFull == numWorking;
	}
	
	public static Arc getDependency(Token t1, Token t2, List<Arc> arcList) {
		Arc dep = null;
		if(arcList != null) {
			for(Arc arc : arcList) {
				if(arc.getChild() == t1 && arc.getHead() == t2 ) {
					dep = arc;//arc.getDependency();
					break;
				}
			}
		}
		return dep;
	}
	
	public static Token[] findSubcomponents(List<Token> tokens, List[] finalTokenToChildren) {
		Token[] subcomponentHeadMappings = new Token[tokens.size()+1];
		// Create the stack and buffer
		List<Token> stack = new ArrayList<Token>();
		List<Token> buffer = new ArrayList<Token>();
		for(Token token : tokens) {
			buffer.add(token);
		}
		
		Token topOfStack = null;
		Token nextToTopOfStack = null;
		int numOps = 0;
		List[] workingTokenToArcs = new List[tokens.size()+1];
		stack.add(buffer.remove(0));
		while(buffer.size() > 0 || stack.size() > 1) {
			numOps++;
			if(numOps > 10000) {
				System.err.println("ERROR: 10000+ operations!!! for sentence");
				break;
			}
			topOfStack = stack.get(stack.size()-1);
			nextToTopOfStack = stack.size() >= 2 ? stack.get(stack.size()-2) : null;
			Arc arc = null;
			// Check if we need to link the left to the right
			arc = getDependency(nextToTopOfStack, topOfStack, finalTokenToChildren[topOfStack.getIndex()]);
			//System.err.println(arc != null ? arc.getDependency() : null);
			if(nextToTopOfStack != null && arc!=null && hasAllItsDependents(nextToTopOfStack, finalTokenToChildren[nextToTopOfStack.getIndex()], workingTokenToArcs[nextToTopOfStack.getIndex()])) {
				stack.remove(nextToTopOfStack);
				List<Arc> workingArcs = workingTokenToArcs[topOfStack.getIndex()];
				if(workingArcs == null) {
					workingTokenToArcs[topOfStack.getIndex()] = workingArcs = new ArrayList<Arc>();
				}
				workingArcs.add(arc);
			}
			// Check if we need to link the right to the left
			else if(nextToTopOfStack != null && (arc = getDependency(topOfStack, nextToTopOfStack, finalTokenToChildren[nextToTopOfStack.getIndex()]))!=null && hasAllItsDependents(topOfStack, finalTokenToChildren[topOfStack.getIndex()], workingTokenToArcs[topOfStack.getIndex()])) {
				stack.remove(topOfStack);
				List<Arc> workingArcs = workingTokenToArcs[nextToTopOfStack.getIndex()];
				if(workingArcs == null) {
					workingTokenToArcs[nextToTopOfStack.getIndex()] = workingArcs = new ArrayList<Arc>();
				}
				workingArcs.add(arc);
			}
		// Need to shift
			else {
				if(buffer.size() == 0) {
					// We must be finished
					break;
				}
				//shift top of buffer to stack
				Token topOfBuffer = buffer.remove(0);
				stack.add(topOfBuffer);
			}
		}
		for(Token t : stack) {
			setSubcomponentHead(t, t, workingTokenToArcs, subcomponentHeadMappings);
		}
		return subcomponentHeadMappings;
	}
	
	private static void setSubcomponentHead(Token t, Token head, List[] workingTokenToArcs, Token[] tokenToSubcomponentHead) {
		tokenToSubcomponentHead[t.getIndex()] = head;
		List<Arc> arcs = workingTokenToArcs[t.getIndex()];
		if(arcs != null) {
			for(Arc arc : arcs) {
				setSubcomponentHead(arc.getChild(), head, workingTokenToArcs, tokenToSubcomponentHead);
			}
		}
	}
	
}