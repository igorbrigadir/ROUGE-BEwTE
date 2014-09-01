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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tratz.parse.io.ConllxSentenceReader;
import tratz.parse.io.SentenceReader;
import tratz.parse.transform.VchTransformer;
import tratz.parse.types.Arc;
import tratz.parse.types.Parse;
import tratz.parse.types.Token;
import tratz.parse.util.ParseConstants;

/**
 * Code for lining up PropBank with the dependency parses.
 * It doesn't handle empty categories well.
 *  
 * Needs some cleanup. 
 *
 */
public class PropBankConverter {
	
	public static boolean INCLUDE_ALL_KIDS = false;
	public static boolean INCLUDE_PARTMOD_KID = true;
	public static boolean INCLUDE_AMOD_KID = true;
	
	public static Set<String> SKIP_DEPS = new HashSet<String>(Arrays.asList(ParseConstants.DETERMINER_DEP,
			ParseConstants.VERBAL_CHAIN_DEP,ParseConstants.UNSPECIFIED_DEP,ParseConstants.COORDINATING_CONJUNCTION_DEP,ParseConstants.APPOSITIVE_DEP,ParseConstants.ADJECTIVAL_MODIFIER_DEP,ParseConstants.PARATAXIS_DEP,ParseConstants.PRECONJ_DEP,ParseConstants.POSSESSOR_DEP,ParseConstants.PUNCTUATION_DEP,ParseConstants.RELATIVE_CLAUSE_DEP));
	
	public static class ParseNode {
		List<ParseNode> children = new ArrayList<ParseNode>();
		int index;
		int emptylessIndex;
		String type;
		String text;
		
		public ParseNode(String type) {
			this.type = type;
		}
		
		public boolean isTerminal() {
			return children.size() == 0;
		}
		
		public String toString() {
			return type;
		}
	}
	
	public static class PTBSentence {
		int overallIndex = -1;
		List<ParseNode> terminals = new ArrayList<ParseNode>();
		ParseNode root;
		public PTBSentence() {
			
		}
	}
	
	public static class IntHolder {
		public int x;
		public IntHolder(int val) {
			this.x = val;
		}
	}
	
	public static class SemArc implements Comparable<SemArc>{
		private int mChildToken;
		private int mHeadToken;
		
		private String mArgType;
		private String mPropBankFileEntry;
		private String mHeadFrame;
		public SemArc(int childToken, 
				      int headToken,
				      String argType,
				      String fullEntry,
				      String headFrame) {
			mHeadToken = headToken;
			mChildToken = childToken;
			mArgType = argType;
			mPropBankFileEntry = fullEntry;
			mHeadFrame = headFrame;
		}
		
	public int hashCode() {
			return toString().hashCode();
		}
		
		
		public boolean equals(Object a2) {
			return toString().equals(a2.toString());
		}
		
		private String s;
		public String toString() {
			if(s == null) {
				s = (mArgType+":"+mPropBankFileEntry+":"+mHeadFrame);//+":"+mChildToken+":"+mHeadToken);
			}
			return s;
		}
		
		public int compareTo(SemArc a2) {
			return toString().compareTo(a2.toString());
		}
	}
	
	public static Map<PTBSentence, List<SemArc>> readPropBankSemanticArcs(File file, 
			Map<String, List<PTBSentence>> fileToSentences) throws IOException {
		Map<PTBSentence, List<SemArc>> semArcsMap = new HashMap<PTBSentence, List<SemArc>>();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		while((line = reader.readLine()) != null) {
			String[] split = line.split("\\s+");
			String f = split[0];
			f = f.substring(f.lastIndexOf('/')+1);
			List<PTBSentence> sentences = fileToSentences.get(f);
			if(sentences == null) {
				System.err.println("No sentences were read for: " + f + " so we are skipping a PropBank entry for it");
				continue;
			}
			PTBSentence sentence = sentences.get(Integer.parseInt(split[1]));
			List<SemArc> semArcs = semArcsMap.get(sentence);
			if(semArcs == null) {
				semArcsMap.put(sentence, semArcs = new ArrayList<SemArc>());
			}
			int headTokenIndex = Integer.parseInt(split[2]);
			ParseNode headNode = sentence.terminals.get(headTokenIndex);
			String annotator = split[3];
			String frame = split[4];
			String flags = split[5];
			for(int i = 6; i < split.length; i++) {
				String arg = split[i];
				int dashIndex = split[i].indexOf('-');
				String type = split[i].substring(dashIndex+1);
				String tokens = split[i].substring(0, dashIndex);
				String[] starSplits = tokens.split("\\*");
				for(String starSplit : starSplits) {
					for(String commaSplit : starSplit.split(",")) {
						String[] colonSplit = commaSplit.split(":");
						int tokenIndex = Integer.parseInt(colonSplit[0]);
						int depth = Integer.parseInt(colonSplit[1]);
						
						ParseNode childNode = sentence.terminals.get(tokenIndex);
						if(!childNode.type.equals("-NONE-") || (childNode.emptylessIndex > headNode.emptylessIndex && !tokens.contains("*"))) {
							SemArc semArc = new SemArc(childNode.emptylessIndex, headNode.emptylessIndex, type, arg, frame);
							semArcs.add(semArc);
						}
					}
				}
			}
		}
		reader.close();
		return semArcsMap;
	}
	
	
	public static void traverse(List<ParseNode> allTerminals,
								ParseNode node, 
								IntHolder fullCounter, 
								IntHolder nonEmptyCounter) {
		if(node.isTerminal()) {
			
			node.index = fullCounter.x;
			node.emptylessIndex = nonEmptyCounter.x;
			fullCounter.x++;
			allTerminals.add(node);
			if(!node.type.equals("-NONE-")) {
				nonEmptyCounter.x++;
				
			}
		}
		else {
			for(ParseNode child : node.children) {
				traverse(allTerminals, child, fullCounter, nonEmptyCounter);
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		File ptbMrgWsjDir = new File(args[0]);
		String propbankDatafile = args[1];
		String autoTaggedParseFiles = args[2];
		String outFile = args[3];
		
		File outputFile = new File(outFile);
		File outputFileParentDirectory = outputFile.getParentFile();
		if(!outputFileParentDirectory.exists()) {
			System.err.println("Attempting to create directory: " + outputFileParentDirectory.getAbsolutePath());
			outputFileParentDirectory.mkdirs();
		}
		
		System.err.println("Reading MRG files from: " + ptbMrgWsjDir.getAbsolutePath());
		List<File> files = collectFiles(new ArrayList<File>(), ptbMrgWsjDir);
		Map<String, List<PTBSentence>> fileToSentences = new HashMap<String, List<PTBSentence>>();
		int sentenceIndex = 0;
		List<PTBSentence> allConstituentSentences = new ArrayList<PTBSentence>();
		for(File file : files) {
			List<PTBSentence> sentences = readPtbSentences(file);
			for(PTBSentence sentence : sentences) {
				sentence.overallIndex = sentenceIndex;
				traverse(sentence.terminals, sentence.root, new IntHolder(0), new IntHolder(0));
				sentenceIndex++;
				allConstituentSentences.add(sentence);
			}
			fileToSentences.put(file.getName(), sentences);
		}
		Map<PTBSentence, List<SemArc>> sentenceToArcs = 
			readPropBankSemanticArcs(new File(propbankDatafile), fileToSentences);
		List<Parse> allSentences = new ArrayList<Parse>();
		ConllxSentenceReader sreader = new ConllxSentenceReader();
		System.err.println(autoTaggedParseFiles);
		for(String autoTaggedFile : autoTaggedParseFiles.split(File.pathSeparator)) {
			System.err.println("Reading from: " + autoTaggedFile);
			allSentences.addAll(readDependencySentences(sreader, autoTaggedFile));	
		}
		
		VchTransformer vchTransformer = new VchTransformer();
		
		int numUnattached = 0;
		
		PrintWriter writer = new PrintWriter(new FileWriter(outFile));
		int numDependencySentences = allSentences.size();
		for(int i = 0; i < numDependencySentences; i++) {
			Parse parse = allSentences.get(i);
			//List<Arc> arcs = sentence.getArcs();
			PTBSentence constituentSentence = allConstituentSentences.get(i);
			List<SemArc> semArcs = sentenceToArcs.get(constituentSentence);
			List<Token> tokens = parse.getSentence().getTokens();
			List[] tokenToArcs = parse.getDependentArcLists();
			Arc[] tokenToHead = parse.getHeadArcs();
			vchTransformer.performTransformation(parse);
			
			Map<Token, Set<Token>> tokenToTokenPlusVch = new HashMap<Token, Set<Token>>();
			for(Token t : tokens) {
				Set<Token> vch = new HashSet<Token>();
				tokenToTokenPlusVch.put(t, vch);
				vch.add(t);
				addAllVch(vch, t, tokenToHead);
			}
			
			List<SemArc> unattached = new ArrayList<SemArc>();
			Map<Token, Set<SemArc>> tokenToSemArcs = new HashMap<Token, Set<SemArc>>(); 
			for(int t = 0; t < tokens.size(); t++) {
				Token tok = tokens.get(t);

				if(semArcs != null) {
					for(SemArc sarc : semArcs) {
						
						Arc arcHead = tokenToHead[tok.getIndex()];
						if(sarc.mChildToken == t && !sarc.mArgType.endsWith("INVERTED")) {
							Set<SemArc> tokensSemArcs = tokenToSemArcs.get(tok);
							if(tokensSemArcs == null) {
								tokenToSemArcs.put(tok, tokensSemArcs = new HashSet<SemArc>());
							}
							if(sarc.mArgType.contains("rel")) {
								tokensSemArcs.add(sarc);
								continue;
							}
							// need to figure out what to do with this
							// Case 1: it is a partmod/amod
							boolean isDependent = false;
							Token headToken = tokens.get(sarc.mHeadToken);
							Set<Token> vchCluster = tokenToTokenPlusVch.get(headToken);
							// Need to check kids as well if we are trying to do everything
							if(INCLUDE_ALL_KIDS || INCLUDE_AMOD_KID || INCLUDE_PARTMOD_KID) {
							List<Arc> kids = tokenToArcs[tok.getIndex()];
							if(kids != null) {
								for(Arc kid : kids) {
									if(INCLUDE_ALL_KIDS || kid.getDependency().equals(ParseConstants.ADJECTIVAL_MODIFIER_DEP) && INCLUDE_AMOD_KID || kid.getDependency().equals(ParseConstants.PARTICIPLE_MODIFIER_DEP) && INCLUDE_PARTMOD_KID) {
										if(kid.getChild().getIndex() == sarc.mHeadToken+1) {
											isDependent = true;
											if(kid.getDependency().matches("amod|partmod")) {
												sarc.mArgType = sarc.mArgType+"-INVERTED";
												int oldHead = sarc.mHeadToken;
												sarc.mHeadToken = sarc.mChildToken;
												sarc.mChildToken = oldHead;
												Token child = tokens.get(sarc.mChildToken);
												Set<SemArc> childsemarcs = tokenToSemArcs.get(child);
												if(childsemarcs == null) {
													tokenToSemArcs.put(child, childsemarcs = new HashSet<SemArc>());
												}
												childsemarcs.add(sarc);
											}
											else {
												tokensSemArcs.add(sarc);
											}
										}
									}
								}
								}
							}
							boolean foundTrueHead = false;
							if(!isDependent) {
								
								// Case 2: percolate up to head, if the text matches it may be a preposition attachment parse issue...
								outer:
								while(arcHead != null ) {
									Token head = arcHead.getHead();
									if(vchCluster.contains(head)) {
										Set<SemArc> headSemArcs = tokenToSemArcs.get(arcHead.getChild());
										if(headSemArcs == null) {
											tokenToSemArcs.put(arcHead.getChild(), headSemArcs = new HashSet<SemArc>());
										}
										headSemArcs.add(sarc);
										sarc.mHeadToken = head.getIndex()-1;
										foundTrueHead = true;
										break;
									}
									else  {
										// Need to check kids as well...
										if(INCLUDE_ALL_KIDS || INCLUDE_AMOD_KID || INCLUDE_PARTMOD_KID) {
										List<Arc> kids2 = tokenToArcs[head.getIndex()];
										if(kids2 != null) {
											for(Arc kid : kids2) {
												if(INCLUDE_ALL_KIDS || INCLUDE_AMOD_KID && kid.getDependency().equals(ParseConstants.ADJECTIVAL_MODIFIER_DEP) || INCLUDE_PARTMOD_KID && kid.getDependency().equals(ParseConstants.PARTICIPLE_MODIFIER_DEP)) {
													if(kid.getChild().getIndex() == sarc.mHeadToken+1) {
														isDependent = true;
														if(kid.getDependency().matches(ParseConstants.ADJECTIVAL_MODIFIER_DEP +"|" + ParseConstants.PARTICIPLE_MODIFIER_DEP)) {
															sarc.mArgType = sarc.mArgType+"-INVERTED";
															int oldHead = sarc.mHeadToken;
															//sarc.mHeadToken = sarc.mChildToken;
															sarc.mChildToken = oldHead;
															sarc.mHeadToken = head.getIndex()-1;
															Token child = tokens.get(sarc.mChildToken);
															Set<SemArc> childsemarcs = tokenToSemArcs.get(child);//hild);
															if(childsemarcs == null) {
																tokenToSemArcs.put(child, childsemarcs = new HashSet<SemArc>());
															}
															childsemarcs.add(sarc);
														}
														else {
															Set<SemArc> headSemArcs = tokenToSemArcs.get(head);
															if(headSemArcs == null) {
																tokenToSemArcs.put(head, headSemArcs = new HashSet<SemArc>());
															}
															//	sarc.mHeadToken = head.getIndex()-1;
															headSemArcs.add(sarc);
														}
														break outer;
													}
												}
											}
										}
										}
										
										// Don't go up any further if we just tried a subject link headed by an infinitive 'VB', likely a control verb issue (or perhaps communicative)
										/*if(arcHead.getDependency().contains("subj")
												&& arcHead.getHead().getPos().equals("VB")
												&& !hasAux(arcHead.getHead(), tokenToArcs)
												&& sarc.mHeadFrame.matches("((name|convince|encourage|urge|lead|prompt|force|train|lobby|pressure|order|require)\\..*)||((make|allow|drive)\\.02)||(get\\.04)")) {
											break;
										}*/
										
									}
									if(!sarc.mArgType.endsWith(arcHead.getChild().getText().toLowerCase())) {
										arcHead = tokenToHead[head.getIndex()];	
									}
									else {
										break;
									}
									
								}
							}
							if(!isDependent && !foundTrueHead) {
								// Drat...
								sarc.mArgType = sarc.mArgType+"UNATTACHED";
								numUnattached++;
								//tokensSemArcs.add(sarc);
							}
						}
					}
				}
			}
			
			for(int t = 0; t < tokens.size(); t++) {
				Token tok = tokens.get(t);
				Arc arcHead = tokenToHead[tok.getIndex()];
				Set<SemArc> ars = tokenToSemArcs.get(tok);
				String semType = null;
				String dep = (arcHead == null ? "ROOT" : arcHead.getDependency());
				if(semType != null) {
					dep = semType;
				}
				writer.print(tok.getIndex()+"\t"+tok.getText()+"\t_\t"+tok.getPos()+"\t" + tok.getPos() + "\t_\t"+(arcHead == null ? "0" : arcHead.getHead().getIndex()) + "\t" + dep+"\t_\t_\t");
				
				if(ars != null) {
					List<SemArc> arsList = new ArrayList<SemArc>(ars);
					Collections.sort(arsList);
					
					Map<String, SemArc> predicateToSemArc = new HashMap<String, SemArc>();
					List<SemArc> arcsToRemove = new ArrayList<SemArc>();
					for(SemArc arc : arsList) {
						if(arc.mArgType.startsWith("ARG")) {
							SemArc currentArc = predicateToSemArc.get(arc.mHeadFrame);
							if(currentArc != null) {
								/*if(currentArc.mArgType.startsWith("ARG1")) {
									arcsToRemove.add(currentArc);
									predicateToSemArc.put(arc.mHeadFrame, arc);
								}
								else {
									arcsToRemove.add(arc);
								}*/
							}
							else {
								predicateToSemArc.put(arc.mHeadFrame, arc);
							}
						}
					}
					arsList.removeAll(arcsToRemove);
					
					SemArc lastArc = null;
					for(SemArc sarc : arsList) {
						//writer.print(arc.mArgType)
						writer.print(sarc.mArgType+"->"+(sarc.mHeadToken+1)+"|"+sarc.mPropBankFileEntry+"|"+sarc.mHeadFrame+"\t");
						lastArc = sarc;
					}
				}
				/*if(semArcs != null) {
					for(SemArc sarc : semArcs) {
						if(sarc.mChildToken == t) {
							writer.print(sarc.mArgType+"->"+(sarc.mHeadToken+1)+"|"+sarc.mPropBankFileEntry+"\t");
						}
					}
				}*/
				writer.println();
			}
			writer.println();
			
		}
		writer.close();
		
		System.err.println("Number unattached: " + numUnattached + " (most failures to attach are due to empty category nodes)");
	}
	
	private static void addAllVch(Set<Token> vchSet, Token t, Arc[] tokenToHead) {
		Arc headArc = tokenToHead[t.getIndex()];
		if(headArc != null && headArc.getDependency().equals(ParseConstants.VERBAL_CHAIN_DEP)) {
			vchSet.add(headArc.getHead());
			addAllVch(vchSet, headArc.getHead(), tokenToHead);
		}
	}
	
	public static List<Parse> readDependencySentences(SentenceReader sreader, String inputFile) throws IOException {
		List<Parse> parses = new ArrayList<Parse>();
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		Parse parse = null;
		while((parse = sreader.readSentence(reader)) != null) {
			parses.add(parse);
		}
		reader.close();
		return parses;
	}
	
	public static void printParse(ParseNode node, String indent) {
		System.err.println(indent+node.type+(node.text == null ? "":"_"+node.text));
		for(ParseNode child : node.children) {
			printParse(child, indent+"   ");
		}
	}
	
	public static List<PTBSentence> readPtbSentences
	(File file) throws IOException {
		List<PTBSentence> sentences = new ArrayList<PTBSentence>();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		PTBSentence sentence = null;
		while((sentence = readPtbSentence(reader)) != null) {
			sentences.add(sentence);
		}
		reader.close();
		return sentences;
	}
	
	public static PTBSentence readPtbSentence(BufferedReader reader) throws IOException {
		PTBSentence sentence = null;
		List stack = new ArrayList();
		int CARRIAGE_RETURN = (int)'\n';
		int LINE_FEED = (int)'\r';
		int nextCharacter;
		StringBuilder buf = new StringBuilder();
		while((nextCharacter = reader.read()) != -1) {
			if(nextCharacter == CARRIAGE_RETURN || nextCharacter == LINE_FEED) {
				continue;
			}
			else {
				if(nextCharacter == '(') {
					stack.add("(");
					if(stack.size() == 1) {
						stack.add("-ROOT-");
					}
				}
				else if(nextCharacter == ')') {
					if(buf.length() > 0) {
						stack.add(buf.toString());
						buf.setLength(0);
					}
					int previousOpenParenIndex = -1;
					for(int i = stack.size()-1; i >= 0; i--) {
						Object token = stack.get(i);
						if("(".equals(token)) {
							previousOpenParenIndex = i;
							break;
						}
					}
					String type = (String)stack.get(previousOpenParenIndex+1);
					ParseNode node = new ParseNode(type);
					for(int i = previousOpenParenIndex+2; i < stack.size(); i++) {
						Object o = stack.get(i);
						if(o instanceof ParseNode) {
							node.children.add((ParseNode)o);
						}
						else {
							node.text = (String)o;
						}
					}
					int stackSize = stack.size();
					for(int i = stackSize-1; i >= stackSize-(stackSize-previousOpenParenIndex); i--) {
						stack.remove(i);
					}
					stack.add(node);
					if(stack.size() == 1) {
						sentence = new PTBSentence();
						sentence.root = node;
						break;
					}
				}
				else if(nextCharacter == ' ') {
					if(buf.length() == 0) {
						continue;
					}
					else {
						stack.add(buf.toString());
						buf.setLength(0);
					}
				}
				else {
					buf.append((char)nextCharacter);
				}
				
			}
		}
		reader.read();
		return sentence;
	}
	
	public static List<File> collectFiles(List<File> files, File dir) {
		File[] filesInDir = dir.listFiles();
		Arrays.sort(filesInDir);
		for(File f : filesInDir) {
			if(f.isDirectory()) {
				if(f.getName().equals("00") || f.getName().equals("01")) {
					System.err.println("Hard-coded to skip: " + f.getName());
				}
				/*else if(!f.getName().equals("22")) {
					System.err.println("Only considering 22");
				}*/
				else {
					collectFiles(files, f);
				}
			}
			else {
			//	if(f.getName().equals("wsj_2200.mrg")) {
					files.add(f);
			//	}
			}
		}
		return files;
	}
	
	public static boolean hasAux(Token token, List[] arcLists) {
		boolean hasAux = false;
		List<Arc> arcList = arcLists[token.getIndex()];
		if(arcList != null) {
			for(Arc arc : arcList) {
				if(arc.getDependency().startsWith(ParseConstants.AUXILLARY_DEP)) {
					hasAux = true;
				}
			}
		}
		return hasAux;
	}
	
	public static boolean hasNoArgArcs(Set<SemArc> ars) {
		if(ars == null) {
			return true;
		}
		else {
			boolean hasNoArgArcs = true;
			for(SemArc arc : ars) {
				if(arc.mArgType.startsWith("ARG")) {
					hasNoArgArcs = false;
					break;
				}
			}
			return hasNoArgArcs;
		}
	}
	
	
	public static boolean hasDep(String dep, String markText, List<Arc> children) {
		if(children == null) {
			return false;
		}
		else {
			boolean hasNoArgArcs = false;
			for(Arc arc : children) {
				if(arc.getDependency().equals(dep) && (markText == null || arc.getChild().getText().toLowerCase().equals(markText))) {
					hasNoArgArcs = true;
					break;
				}
			}
			return hasNoArgArcs;
		}
	}
	
}