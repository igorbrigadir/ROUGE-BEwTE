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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import tratz.jwni.WordNet;
import tratz.ml.ClassScoreTuple;
import tratz.parse.transform.VchTransformer;
import tratz.parse.types.Arc;
import tratz.parse.types.Parse;
import tratz.parse.types.Sentence;
import tratz.parse.types.Token;
import tratz.parse.util.ParseConstants;
import tratz.pos.PosTagger;
import tratz.semantics.ClassificationBundle;

public class FullSystemWrapper {
	
	public final static String[] POSSESSIVE_DEPENDENCY_RELATIONS = new String[]{ParseConstants.POSSESSOR_DEP};
	public final static String[] NOUN_COMPOUND_DEPENDENCY_RELATIONS = new String[]{ParseConstants.NOUN_COMPOUND_DEP};
	public final static String[] PREPOSITION_DEPENDENCY_RELATIONS = new String[]{ParseConstants.PREP_MOD_DEP, ParseConstants.AGENT_DEP, ParseConstants.COPULAR_COMPLEMENT_DEP};
	
	private NLParser mParser;
	private PosTagger mPosTagger;
	private ClassificationBundle mPsdWrapper = null,
	 							 mNnWrapper = null,
	 							 mPossWrapper = null,
	 							 mSrlArgsWrapper = null,
	 							 mSrlPredicatesWrapper = null;
	
	public static class FullSystemResult implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		private Parse mParse;
		private Parse mSrlParse;
		
		public FullSystemResult(Parse parse, Parse srlParse) {
			mParse = parse;
			mSrlParse = srlParse;
		}
		
		public Parse getParse() {
			return mParse;
		}
		
		public Parse getSrlParse() {
			return mSrlParse;
		}
	}
	
	public FullSystemWrapper(String prepositionModelFile, String nounCompoundModelFile, String possessivesModelFile, String srlArgsModelFile, String srlPredicatesModelFile,
			String posModelFile, String parseModelFile, String wnDir) throws IOException, ClassNotFoundException {
		if(prepositionModelFile != null) {
			mPsdWrapper = readBundle(new File(prepositionModelFile), "preposition disambiguation models");
		}
		if(nounCompoundModelFile != null) {
			mNnWrapper = readBundle(new File(nounCompoundModelFile), "noun compound interpretation model");
		}
		if(possessivesModelFile != null) {
			mPossWrapper = readBundle(new File(possessivesModelFile), "possessives interpretation model");
		}
		if(srlArgsModelFile != null) {
			mSrlArgsWrapper = readBundle(new File(srlArgsModelFile), "semantic role labeling models (arguments and adjuncts)");
		}
		if(srlPredicatesModelFile != null) {
			mSrlPredicatesWrapper = readBundle(new File(srlPredicatesModelFile), "semantic role labeling models (predicate disambiguation)");
		}

		// LOAD WORDNET (Needed for POS-tagger and semantic disambiguation tasks)
		if(wnDir != null) {
			System.err.print("Loading WordNet...");
			new WordNet(new File(wnDir));
			System.err.println("Done");
		}

		// READ POS TAGGING MODULE
		if(posModelFile != null) {
			//long modelStartTime = System.currentTimeMillis();
			System.err.print("Loading POS-tagging model...");
			mPosTagger = readPosTagger(posModelFile);
			System.err.println("Done");
			//System.err.println("POS-tagging model loaded. Time: " +(System.currentTimeMillis()-modelStartTime)/1000.0 + " seconds.");
		}

		// READ PARSING MODULE
		System.err.print("Loading parsing model...");
		//long startTime = System.currentTimeMillis();
		mParser = new NLParser(parseModelFile);
		System.err.println("Done");
		//System.err.println("Parse model loaded. Time: " + (System.currentTimeMillis()-startTime)/1000.0 + " seconds.");
	}
	
	public FullSystemWrapper(NLParser parser,
			PosTagger posTagger,
			ClassificationBundle psdWrapper,
			ClassificationBundle nnWrapper,
			ClassificationBundle possWrapper,
			ClassificationBundle srlArgsWrapper,
			ClassificationBundle srlPredicatesWrapper) {
		mParser = parser;
		mPosTagger = posTagger;
		mNnWrapper = nnWrapper;
		mPossWrapper = possWrapper;
		mSrlArgsWrapper = srlArgsWrapper;
		mSrlPredicatesWrapper = srlPredicatesWrapper;
	}
	
	public FullSystemResult process(Sentence sentence, 
						        boolean doPosTag,
								boolean doParse, 
								boolean doPrepDisambiguation, 
								boolean doNnDisambiguation, 
								boolean doPossInterp, 
								boolean doSrl) {
		List<Token> tokens = sentence.getTokens();
		int numTokens = tokens.size();
		
		// Part-of-Speech tag if has not already been POS-tagged
		if(doPosTag) {
			// Part-of-Speech tagging
			if(mPosTagger != null) {
				mPosTagger.posTag(tokens);
			}
		}
			
		// Parse the sentence
		Parse parse = null;
		Parse srlLabeledParse = null;
		
		if(doParse) {
			parse = mParser.parseSentence(sentence);
			
			if(doPrepDisambiguation) {
				// Preposition sense disambiguation
				performSemanticAnnotation(mPsdWrapper, sentence, parse, PREPOSITION_DEPENDENCY_RELATIONS);
			}
			if(doNnDisambiguation) {
				// Noun-noun compounding interpretation
				performSemanticAnnotation(mNnWrapper, sentence, parse, NOUN_COMPOUND_DEPENDENCY_RELATIONS);
			}
			
			if(doPossInterp) {
				// Interpretation of possessives
				performSemanticAnnotation(mPossWrapper, sentence, parse, POSSESSIVE_DEPENDENCY_RELATIONS);
			}
		
			if(doSrl) {
				// Semantic role labeling
				srlLabeledParse = performSrlAnnotation(mSrlArgsWrapper, mSrlPredicatesWrapper, sentence, parse);
			}
		}
		else {
			parse = new Parse(sentence, null, null);
		}
		
		return new FullSystemResult(parse, srlLabeledParse);
	}
	
	private static void performSemanticAnnotation(ClassificationBundle classifier, 
			  Sentence sentence, 
			  Parse parse,
			  String[] dependencyTypes) {
		if(classifier != null) {
			List<Token> tokens = sentence.getTokens();
			Arc[] tokenToHeadArc = parse.getHeadArcs();
			final int numTokens = tokens.size();
			for(int i = 0; i < numTokens; i++) {
				Token tok = tokens.get(i);
				Arc arc = tokenToHeadArc[tok.getIndex()];
				if(arc != null) {
					String dep = arc.getDependency();
					for(String depType : dependencyTypes) {
						if(dep.equals(depType)) {
							Token t = arc.getChild();							
							ClassScoreTuple[] decision = classifier.getPredictions(dep, sentence.getTokens(), parse, t.getIndex()-1);
							if(decision != null && decision.length > 0) {
								t.setLexSense(decision[0].clazz);
							}
							break;
						}
					}
				}
			}
		}
	}
	
	private static Parse performSrlAnnotation(ClassificationBundle srlArgsWrapper, 
			ClassificationBundle srlPredicatesWrapper,
			Sentence sentence,
		  Parse parse) {
		Parse vchTransformedParse = null;

		List<Token> tokens = sentence.getTokens();
		int numTokens = tokens.size();

		Arc[] tokenToHead = parse.getHeadArcs();

		if(srlArgsWrapper != null && srlPredicatesWrapper != null) {
			// 	Create a transformed version of the parse
			List<Arc> arcsCopy = new ArrayList<Arc>();
			for(int t = 0; t < numTokens; t++) {
				Arc orig = tokenToHead[t];
				if(orig != null) {
					Arc newArc = new Arc(orig.getChild(), 
							orig.getHead(), 
							orig.getDependency());
					newArc.setCreationNum(orig.getCreationNum());
					arcsCopy.add(newArc);
				}
			}	
			vchTransformedParse = new Parse(parse.getSentence(), parse.getRoot(), arcsCopy);
			// 	SRL components require performing the vch transform
			new VchTransformer().performTransformation(vchTransformedParse);

			for(int t = 0; t < numTokens; t++) {
				Arc arc = vchTransformedParse.getHeadArcs()[t];
				if(arc != null) {
					ClassScoreTuple[] predictions = srlArgsWrapper.getPredictions(arc.getDependency(), tokens, vchTransformedParse, arc.getChild().getIndex()-1);
					if(predictions != null && predictions.length > 0) {
						arc.setSemanticAnnotation(predictions[0].clazz);
					}
				}
				
				Token tok = tokens.get(t);
				ClassScoreTuple[] predictions = srlPredicatesWrapper.getPredictions(null, tokens, vchTransformedParse, tok.getIndex()-1);
				if(predictions != null && predictions.length > 0) {
					tok.setLexSense(predictions[0].clazz);
				}
			}
		}
		return vchTransformedParse;
	}
	
	public static ClassificationBundle readBundle(File inputFile, String name) throws IOException, ClassNotFoundException {
		ClassificationBundle bundle;
		System.err.print("Loading "+name+"...");
		ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(inputFile)));
		bundle = (ClassificationBundle)ois.readObject();
		ois.close();
		System.err.println("Done");
		return bundle;
	}
	
	public static PosTagger readPosTagger(String posModelFile) throws IOException, ClassNotFoundException {
		return new PosTagger(new File(posModelFile)) {
			// Override the pos tagging method to make it just a little faster
			// (There is no reason to extract features and perform dot products for periods and commas (except for sanity checking))
			@Override
			public void posTag(List<Token> sentenceTokens) {
				final int numTokens = sentenceTokens.size();
				for(int j = 0; j < numTokens; j++) {
					Token t = sentenceTokens.get(j);
					String tText = t.getText();
					// It runs slightly faster if we just hard-code tags for periods and commas
					if(tText.equals(".")) {
						t.setPos(".");
					}
					else if(tText.equals(",")) {
						t.setPos(",");
					}
					else {
						Set<String> feats = mFeatureGenerator.getFeats(sentenceTokens, j);
						// Ranking of part-of-speech tags
						ClassScoreTuple[] classRankings = mDecisionModule.getDecision(feats);
						// Use highest rated tag for this token
						t.setPos(classRankings[0].clazz);
					}
				}
			}
		};
	}
	
}