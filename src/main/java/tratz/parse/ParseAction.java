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

import tratz.parse.types.Token;
import tratz.parse.types.TokenPointer;

/**
 * Structure for holding information about a parsing action
 * including the token involved and the score of the action.
 */
public class ParseAction implements Comparable<ParseAction> {
	
	public final Token token;
	public final TokenPointer tpr;
	public String actionName;
	public double score;
	public ParseAction(Token t1, TokenPointer tpr, String action, double score) {
		this.token = t1;
		this.actionName = action;
		this.score = score;
		this.tpr = tpr;
	}
	
	public int compareTo(ParseAction pa) {
		if(score > pa.score) {
			return -1;
		}
		else if (score < pa.score) {
			return 1;
		}
		else if(actionName.compareTo(pa.actionName) != 0) {
			return actionName.compareTo(pa.actionName);
		}
		else if(token.getIndex() != pa.token.getIndex()) {
			return token.getIndex()-pa.token.getIndex();
		}
		else return pa.hashCode()-this.hashCode();
	}
	
}