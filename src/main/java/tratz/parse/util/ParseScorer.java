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

package tratz.parse.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tratz.cmdline.CommandLineOptions;
import tratz.cmdline.CommandLineOptionsParser;
import tratz.cmdline.ParsedCommandLine;
import tratz.parse.io.ConllxSentenceReader;
import tratz.parse.io.SentenceReader;
import tratz.parse.types.Arc;
import tratz.parse.types.Parse;
import tratz.parse.types.Token;
import tratz.parse.util.NLParserUtils;

/**
 * Object for calculating LAS (labeled attachment score), UAS (unlabeled attachment score),
 * perfect sentence matches, and non-projective arc attachment recall. 
 *
 */
public class ParseScorer {
	
	public final static String PUNCTUATION_REGEX = "\\(|\\)|:|;|,|\\.|''|``|/";
	
	public static class LASresults {
		public int numCorrectArcsLabeled;
		public int numIncorrectArcsLabeled;
		public int numCorrectArcsUnlabeled;
		public int numIncorrectArcsUnlabeled;
		
		public int numNonProjectiveCorrectArcsLabeled;
		public int numNonProjectiveIncorrectArcsLabeled;
		public int numNonProjectiveCorrectArcsUnlabeled;
		public int numNonProjectiveIncorrectArcsUnlabeled;
		
		public int numNonProjective;
		
		public boolean rootCorrect;
	}
	
	
	private static void tagNonProjectiveArcs(Arc arc, 
			List[] tokenToChildren, 
			Set<Arc> visited, 
			Set<Arc> nonProjective,
			String ignoreString) {
		
		// check if it is nonprojective
		if(!arc.getChild().getPos().matches(ignoreString)) {
			int childIndex = arc.getChild().getIndex();
			int headIndex = arc.getHead().getIndex();
			for(Arc prevArc : visited) {
				int prevChildIndex = prevArc.getChild().getIndex();
				int prevHeadIndex = prevArc.getHead().getIndex();
				// Check if child index falls between the child and head indices of the other arc
				if((childIndex > prevChildIndex && childIndex < prevHeadIndex)
					|| (childIndex < prevChildIndex && childIndex > prevHeadIndex)) {
					// Check if head index falls outside the child and head indices of the other arc 
					if((headIndex > prevChildIndex && headIndex > prevHeadIndex) ||
						(headIndex < prevChildIndex && headIndex < prevHeadIndex)) {
						// Cross arc found; consider to be non-projective
						nonProjective.add(arc);
						break;
					}
				}
			}
			visited.add(arc); 
		}
		final Token arcChild = arc.getChild();
		List<Arc> children = tokenToChildren[arcChild.getIndex()];
		if(children != null) {
			// Need to order based upon absolute distance from head
			Collections.sort(children, new Comparator<Arc>() {
				public int compare(Arc a1, Arc a2) {
					int diff1 = Math.abs(a1.getChild().getIndex()-arcChild.getIndex());
					int diff2 = Math.abs(a2.getChild().getIndex()-arcChild.getIndex());
					return diff1-diff2;
				}
			});
			for(Arc child : children) {
				tagNonProjectiveArcs(child, tokenToChildren, visited, nonProjective, ignoreString);
			}
		}
	}
	
	public static LASresults calc(int numTokens,
								  Parse goldParse,
								  Parse predictedParse,
								  String ignoreString) {
		LASresults results = new LASresults();
		
		Arc[] goldTokenToHead = goldParse.getHeadArcs();
		List[] goldTokenToChildren = goldParse.getDependentArcLists();
		
		Arc[] predictedTokenToHead = predictedParse.getHeadArcs();
		
		
		// Find root arc
		Arc rootArc = null;
		for(int i = 1; i < goldTokenToHead.length; i++) {
			//System.err.println(goldTokenToHead[i].getDependency());
			if(goldTokenToHead[i].getHead().getIndex() == 0) {
				rootArc = goldTokenToHead[i];
			}
		}
		
		// Compile list of arcs considered non-projective
		Set<Arc> visited = new HashSet<Arc>();
		Set<Arc> nonProjective = new HashSet<Arc>();
		tagNonProjectiveArcs(rootArc, goldTokenToChildren, visited, nonProjective, ignoreString);
		
		for(int i = 1; i < goldTokenToHead.length; i++) {
			Arc predicted = predictedTokenToHead[i];
			Arc gold = goldTokenToHead[i];
			
			boolean match1 = gold.getChild().getPos().matches(ignoreString);
			boolean match2 = gold.getChild().getPos().matches("\\(|\\)|:|;|,|\\.|''|``|/");
			if(match1 != match2) {
				System.err.println(gold.getChild() + " " + gold.getChild().getPos());
			}
			// For ignoring punctuation
			if(gold.getChild().getPos().matches(ignoreString)) {
				continue;
			}
			
			boolean isNonProjective = nonProjective.contains(goldTokenToHead[i]); 
			
			if(isNonProjective) {
				results.numNonProjective++;
			}
			if(predicted.getHead().getIndex() == gold.getHead().getIndex()) {
				results.numCorrectArcsUnlabeled++;
				if(isNonProjective) {
					results.numNonProjectiveCorrectArcsUnlabeled++;
				}
				if(predicted.getHead().getIndex() == 0) {
					results.rootCorrect = true;
				}
				if(predicted.getDependency().equals(gold.getDependency())) {
					results.numCorrectArcsLabeled++;
					if(isNonProjective) {
						results.numNonProjectiveCorrectArcsLabeled++;
					}
				}
				else {
					if(predicted.getHead().getIndex()==0) {
						System.err.println("Diff: " + predicted.getDependency() + " vs " + gold.getDependency());
					}
					results.numIncorrectArcsLabeled++;
					if(isNonProjective) {
						results.numNonProjectiveIncorrectArcsLabeled++;
					}
				}
			}
			else {
				results.numIncorrectArcsUnlabeled++;
				results.numIncorrectArcsLabeled++;
				if(isNonProjective) {
					results.numNonProjectiveIncorrectArcsLabeled++;
					results.numNonProjectiveIncorrectArcsUnlabeled++;
				}
			}
		}
		
		return results;
	}
	
	/*public static LASresults calc(List<Token> goldTokens, List[] parseTree, List<Arc> correctArcs, String ignoreString) {
		LASresults results = new LASresults();
		
		// Figure out where the non-projective arcs are, if any
		List[] tokenToChildren = NLParserUtils.buildTokenToChildren(goldTokens.size(), correctArcs);
		Arc[] goldTokenToHead = NLParserUtils.buildTokenToHeadArcs(goldTokens, tokenToChildren);
		
		Arc rootArc = null;
		for(int i = 1; i < goldTokenToHead.length; i++) {
			Arc arc = goldTokenToHead[i];
			if(arc == null || arc.getHead() == null || arc.getHead().getIndex() == 0) {
				// hack in a root arc if it is missing....
				if(arc == null) {
					goldTokenToHead[i] = rootArc = new Arc(goldTokens.get(i-1), new Token("[ROOT]", 0), "ROOT");
					tokenToChildren[0] = Arrays.asList(new Arc[]{goldTokenToHead[i]});
				}
				else {
					rootArc = arc;
				}
				break;
			}
		}
		
		Set<Arc> visited = new HashSet<Arc>();
		Set<Arc> nonProjective = new HashSet<Arc>();
		tagNonProjectiveArcs(rootArc, tokenToChildren, visited, nonProjective, ignoreString);
		
		for(Token t : goldTokens) {
			List<Arc> arcs = parseTree[t.getIndex()];
			if(arcs != null) {
				for(Arc arc : arcs) {
					Token sourceToken = arc.getChild();
					int sourceTokenIndex = sourceToken.getIndex();
					Token goldToken = goldTokens.get(sourceTokenIndex);
					if(goldToken.getPos().matches(ignoreString)) {
						continue;
					}
					boolean correct = false;
					boolean correctUnlabeled = false;
					for(Arc arc2 : correctArcs) {
						if(arc2.getChild().getIndex() == arc.getChild().getIndex() && arc2.getHead().getIndex() == arc.getHead().getIndex()) {
							correctUnlabeled = true;
							if(arc.getHead().getIndex() == 0) {
								results.numRootCorrect = true;
							}
							if(arc.getDependency().equals(arc2.getDependency())) {
								correct = true;
								break;
							}
						}
					}
					
					Arc goldArc = goldTokenToHead[arc.getChild().getIndex()];
					boolean isNonProjective = nonProjective.contains(goldArc);
					
					if(correct) {
						results.numCorrectArcsLabeled++;
						if(isNonProjective) {
							results.numNonProjectiveCorrectArcsLabeled++;
						}
					}
					else {
						results.numIncorrectArcsLabeled++;
						if(isNonProjective) {
							results.numNonProjectiveIncorrectArcsLabeled++;
						}
					}
					if(correctUnlabeled) {
						results.numCorrectArcsUnlabeled++;
						if(isNonProjective) {
							results.numNonProjectiveCorrectArcsUnlabeled++;
							//System.err.println(arc.getChild().getIndex() + " " + goldArc.getDependency() + " " + arc.getChild().getText() + " " + arc.getHead().getIndex());
						}
					}
					else {
						results.numIncorrectArcsUnlabeled++;
						if(isNonProjective) {
							results.numNonProjectiveIncorrectArcsUnlabeled++;
						}
					}
					if(isNonProjective) {
						results.numNonProjective++;
					}
				}
			}
		}
		
		return results;
	}*/	
	
	public final static String OPT_TRUTH_FILE = "g",
							   OPT_SYSTEM_FILE = "s",
							   OPT_PUNCTUATION = "p";
	
	private static CommandLineOptions createOptions() {
		CommandLineOptions cmdOpts = new CommandLineOptions();
		cmdOpts.addOption(OPT_TRUTH_FILE, "file", "the gold parse file");
		cmdOpts.addOption(OPT_SYSTEM_FILE, "file", "the system predictions file");
		cmdOpts.addOption(OPT_PUNCTUATION, "boolean", "indicator if punctuation should be considered");
		return cmdOpts;
	}
	public static void main(String[] args) throws Exception {
		ParsedCommandLine cmdLine = new CommandLineOptionsParser().parseOptions(createOptions(), args);
		
		String truthFile = cmdLine.getStringValue(OPT_TRUTH_FILE);
		String inputFile = cmdLine.getStringValue(OPT_SYSTEM_FILE);
		boolean punct = cmdLine.getBooleanValue(OPT_PUNCTUATION, false);
		// Need to fix this to take any sentence reader again
		SentenceReader sentenceReader = new ConllxSentenceReader();//(SentenceReader)Class.forName(args[2]).newInstance();
		
		BufferedReader truthreader = new BufferedReader(new FileReader(truthFile));
		BufferedReader testreader = new BufferedReader(new FileReader(inputFile));
		
		int totalLCorrect = 0, totalUCorrect = 0, totalLWrong = 0, totalUWrong = 0;
		int totalLCorrectNP = 0, totalUCorrectNP = 0, totalLWrongNP = 0, totalUWrongNP = 0;
		int totalLExact = 0, totalUExact = 0, totalSentences = 0;
		int numSentencesWithNonProjective = 0;
		double totalNonProjective = 0;
		int rootCorrect = 0;
		Parse truthparse = null;

		while((truthparse = sentenceReader.readSentence(truthreader)) != null) {
			totalSentences++;
			
			Parse testparse = sentenceReader.readSentence(testreader);
			
			LASresults results = calc(truthparse.getSentence().getTokens().size(), truthparse, testparse,  punct? "":PUNCTUATION_REGEX);
			totalLCorrect += results.numCorrectArcsLabeled;
			totalUCorrect += results.numCorrectArcsUnlabeled;
			totalLWrong += results.numIncorrectArcsLabeled;
			totalUWrong += results.numIncorrectArcsUnlabeled;
			
			totalLCorrectNP += results.numNonProjectiveCorrectArcsLabeled;
			totalUCorrectNP += results.numNonProjectiveCorrectArcsUnlabeled;
			totalLWrongNP += results.numNonProjectiveIncorrectArcsLabeled;
			totalUWrongNP += results.numNonProjectiveIncorrectArcsUnlabeled;
			totalNonProjective += results.numNonProjective;
			
			if(results.numNonProjective > 0) {
				numSentencesWithNonProjective++;
			}
			
			if(results.numIncorrectArcsLabeled == 0) {
				totalLExact++;
			}
			if(results.numIncorrectArcsUnlabeled == 0) {
				totalUExact++;
			}
			
			if(results.rootCorrect) {
				rootCorrect++;
			}
		}
		truthreader.close();
		testreader.close();

		double total = totalLCorrect+totalLWrong;
		System.err.println("Input File: " + inputFile);
		System.err.println("Truth File: " + truthFile);
		System.err.println("Total arcs evaluated: " + total);
		System.err.println("Total non-projective arcs: " + totalNonProjective);
		System.err.println("Total sentences: " + totalSentences);
		System.err.println("Total non-projective sentences: " + numSentencesWithNonProjective);

		System.err.println("Non-projective arc proportion: " + (totalNonProjective/total));
		System.err.println("Non-projective sentence proportion: " + numSentencesWithNonProjective/(double)totalSentences);
		
		System.err.println("Accuracy (labeled): " + totalLCorrect/total);
		System.err.println("Accuracy (unlabeled): " + totalUCorrect/total);
		
		System.err.println("Accuracy (labeled) non-projective: " + totalLCorrectNP/(double)totalNonProjective);//(totalLCorrectNP+totalLWrongNP));//totalNonProjective
		System.err.println("Accuracy (unlabeled) non-projective: " + totalUCorrectNP/(double)totalNonProjective);//(totalLCorrectNP+totalLWrongNP));//totalNonProjective
		System.err.println("Root accuracy: " + rootCorrect/(double)totalSentences);
		//System.err.println("Accuracy: " + totalLCorrect + " " + totalUCorrect + " " + total);
	
		System.err.println("Full sentence match (labeled): " + totalLExact + " " + totalSentences + " " + totalLExact/(double)totalSentences);
		System.err.println("Full sentence match (unlabeled): " + totalUExact + " " + totalSentences + " " + totalUExact/(double)totalSentences);
		System.err.println(totalUCorrect + " " + totalUWrong + " " + (totalUCorrect+totalUWrong));
	}
	
}