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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tratz.parse.types.Arc;
import tratz.parse.types.Parse;
import tratz.parse.types.Sentence;
import tratz.parse.types.Token;

public class SentenceReaderForSRL {
	
private Map<String, String> mCanon = new HashMap<String, String>();

// Return Sentence & semantic arcs
	public Object[] readSentence(BufferedReader reader) throws IOException {
		Map<Arc, SemanticArc> arcToSemarc = new HashMap<Arc, SemanticArc>();
		
		Token root = new Token("[ROOT]", 0);
		List<Token> tokens = new ArrayList<Token>();
		List<Arc> arcs = new ArrayList<Arc>();
		
		String line = null;
		Map<Integer, Token> numToToken = new HashMap<Integer, Token>();
		numToToken.put(0, root);
		List<String> lines = new ArrayList<String>();
		
		while((line = reader.readLine()) != null) {
			if(line.trim().equals("")) {
				if(tokens.size() == 0) {
					continue;
				}
				else {
					break;
				}
			}
			else {
				lines.add(line);
				String[] split = line.split("\\t+");
				int tokenNum = Integer.parseInt(split[0]);
				
				String text = canon(split[1]); // was 5
				
				Token token = new Token(text, tokenNum);
				String pos = canon(split[4]); // split[7]
				//if(pos.equals("_")) {
					//pos = split[7];
				//}
				
				token.setPos(pos);
				
				tokens.add(token);
				numToToken.put(tokenNum, token);
			}
		}
		for(String l : lines) {
			String[] split = l.split("\\t+");
			int tokenNum = Integer.parseInt(split[0]);
			int head = Integer.parseInt(split[6]);
			String dependency = canon(split[7]);
			String truePos = canon(split[4]);//was 3
			Token t1 = numToToken.get(tokenNum);
			Token t2 = numToToken.get(head);
			
			
			Arc syntacticArc = new Arc(t1, t2, dependency);
			arcs.add(syntacticArc);
			
			if(split.length > 10) {
				for(int i = 10; i < split.length; i++) {
					String semArc = split[i];
					String[] parts = semArc.split("\\|");
					if(parts[0].startsWith("rel")) {
						t1.setLexSense(parts[2]);
					}
					else {
						String preArrow = parts[0].substring(0, parts[0].indexOf("->"));
						String predicate = parts[parts.length-1];
						int dashIndex = preArrow.indexOf("-");
						String semType = null;
						if(dashIndex != -1) {
							String posDash = preArrow.substring(dashIndex+1);
							if(posDash.equals(posDash.toUpperCase())) { // && !posDash.equals("REC")) {
								// Keep the whole thing
								semType = preArrow;
							}
							else {
								// Keep only the beginning: this strips preposition tags off (e.g., ARG1-into becomes ARG1) and also 'REC' tags
								semType = preArrow.substring(0, dashIndex);
							}
						}
						else {
							semType = preArrow;
						}
						SemanticArc newSemanticArc = new SemanticArc(semType, semArc, predicate);
						SemanticArc oldSemanticArc = arcToSemarc.get(syntacticArc);
						if(oldSemanticArc != null) {
							if(!newSemanticArc.getType().equals(oldSemanticArc.getType())) {
								//System.err.println("Blarg!: " + sArc.getFullForm() + " " + old.getFullForm() + " " + arc.getDependency() + " " + arc.getChild().getText());
								String argCharNew = ""+newSemanticArc.getType().charAt(3);
								String argCharOld = ""+oldSemanticArc.getType().charAt(3);
								int argNumNew = -1;
								int argNumOld = -2;
								try {
									argNumNew = Integer.parseInt(argCharNew);
								}
								catch(NumberFormatException nfe){}
								try {
									argNumOld = Integer.parseInt(argCharOld);
								}
								catch(NumberFormatException nfe) {}
								//System.err.println("Multiple: " + oldSemanticArc.getPredicate()+":"+oldSemanticArc.getType()+" vs " + newSemanticArc.getPredicate()+":"+newSemanticArc.getType());
								if(dependency.endsWith("subj") && argNumNew != -1) {
									if(argNumOld > argNumNew) {
										arcToSemarc.put(syntacticArc, newSemanticArc);
									}
								}
								else {
									if(dependency.matches("advcl|advmod|tmod|purpcl|aux|auxpass|neg")) {
										// should prefer ARGMs in this case
										if(argNumNew == -1) {
											arcToSemarc.put(syntacticArc, newSemanticArc);
										}
										else {
											if(argNumNew > argNumOld) {
												arcToSemarc.put(syntacticArc, newSemanticArc);
											}
											else {
												// leave it alone; no reason to prefer one over the other	
											}
										}
									}
									else {
										if(argNumNew > argNumOld) {
											arcToSemarc.put(syntacticArc, newSemanticArc);
										}
									}
								}
								
							}
							
							//else {
								//System.err.println("Wlarg!: " + sArc.getFullForm() + " " + old.getFullForm() + " " + arc.getDependency() + " " + arc.getChild().getText());	
							//}
						}
						else {
							arcToSemarc.put(syntacticArc, newSemanticArc);
						}
					}
				}
				
			}
		}
		Object[] result = new Object[2];
		if(tokens.size() > 0) {
			Sentence outputSentence = new Sentence(tokens);
			result[0] = new Parse(outputSentence, root, arcs);
			result[1] = arcToSemarc;
		}
		else {
			result = null;
		}
		return result;
	}
	
	private String canon(String s) {
		String canon = mCanon.get(s);
		if(canon == null) mCanon.put(s, canon = s);
		return canon;
	}
	
}