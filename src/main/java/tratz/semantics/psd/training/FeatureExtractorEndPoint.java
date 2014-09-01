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

package tratz.semantics.psd.training;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tratz.featgen.MultiStepFeatureGeneratorConfigParser;
import tratz.featgen.WfrEntry;
import tratz.featgen.fer.FeatureExtractionRule;
import tratz.parse.types.Arc;
import tratz.parse.types.Parse;
import tratz.parse.types.Token;
import tratz.runpipe.Annotation;
import tratz.runpipe.InitializationException;
import tratz.runpipe.ProcessException;
import tratz.runpipe.TextDocument;
import tratz.runpipe.annotations.Sentence;
import tratz.runpipe.impl.EndPointImpl;
import tratz.runpipe.util.RunpipeUtils;

/**
 * Used to produce features for preposition sense disambiguation training & testing
 *
 */
public class FeatureExtractorEndPoint extends EndPointImpl {
	
	public final static String PARAM_OUTPUT_DIR = "OutputDir";
	public final static String PARAM_COMPLEX_RULE_LIST = "ComplexRuleList";
	public final static String PARAM_FEATURE_EXTRACTION_LIST = "FeatureExtractionRuleList";
	public final static String PARAM_COMBO_RULE_LIST = "ComboRuleList";
	// For the original TPP annotations
	public final static String PARAM_TRUTH_DIR = "TruthDir";
	// For the new annotations
	public final static String PARAM_OVERRIDE_MAP_FILE = "OverrideMap";
	public final static String PARAM_APPEND = "Append";
	
	private WfrEntry[] mComplexRules;
	private File mOutDir;
	private Map<String, FeatureExtractionRule> mFeatureRuleMap;
	private Map<String, String> mTruthMap;
	private boolean mAppend;
	private boolean mOverridden;
	
	@Override
	public void initialize(Map<String, String> params) throws InitializationException {
		mOutDir = new File(params.get(PARAM_OUTPUT_DIR));
		String featureExtractionRuleList = params.get(PARAM_FEATURE_EXTRACTION_LIST);
		String complexRuleListFile = params.get(PARAM_COMPLEX_RULE_LIST);
		String comboRuleList = params.get(PARAM_COMBO_RULE_LIST);
		String truthDir = params.get(PARAM_TRUTH_DIR);
		mAppend = Boolean.parseBoolean(params.get(PARAM_APPEND));
		
		try {
			MultiStepFeatureGeneratorConfigParser configReader = new MultiStepFeatureGeneratorConfigParser();
			mFeatureRuleMap = configReader.readFeatureExtractionRuleMap(featureExtractionRuleList);
			mComplexRules = configReader.readComplexRules(complexRuleListFile, mFeatureRuleMap);
			readTruthMap(new File(truthDir), mTruthMap = new HashMap<String, String>(), null);
			
			String overrideMap = params.get(PARAM_OVERRIDE_MAP_FILE);
			if(overrideMap != null) {
					readNewTruthMap(new File(overrideMap), mTruthMap = new HashMap<String, String>());
					mOverridden = true;
			}
		}
		catch(Exception ioe) {
			throw new InitializationException(ioe);
		}
	}
	
	private static void readTruthMap(File truthDir, Map<String, String> truthMap, Map<String, String> senseToSrType) throws IOException {
		File[] files = truthDir.listFiles();
		for(File file : files) {
			if(file.getName().endsWith("key")) {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line = null;
				while((line = reader.readLine()) != null) {
					line = line.trim();
					String[] split = line.split("\\s+");
					
					String className = split[2];
					
					/*className = className.split("\\s+")[0];
					String origC = className;
					className = className.replace("-1", "").replace("-2", "").replace("-3","");
					className = senseToSrType.get(file.getName().substring(0, file.getName().indexOf('.')) + "." + className);*/
					
					truthMap.put(split[1],className);
				}
				reader.close();
			}
		}
	}
	
	
	public static void readNewTruthMap(File newTruthFile, Map<String, String> truthMap) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(newTruthFile));
		String line = null;
		while((line = reader.readLine()) != null) {
			line = line.trim();
			if(!line.startsWith("#")) {
				String[] parts = line.split("\\t+");
				truthMap.put(parts[0], parts[1]);
			}
		}
		reader.close();
	}
	
	@Override
	public void process(TextDocument doc) throws ProcessException {
		List<HeadAnnotation> heads = (List<HeadAnnotation>)doc.getAnnotationList(HeadAnnotation.class);
		List<tratz.runpipe.annotations.Token> tokenList = (List<tratz.runpipe.annotations.Token>)doc.getAnnotationList(tratz.runpipe.annotations.Token.class);
		
		if(!mOutDir.exists()) {
			mOutDir.mkdirs();
		}
		
		PrintWriter writer = null;
		try {
			String uri = doc.getUri();
			String inputFilename = uri.substring(uri.lastIndexOf('/')+1);
			String preposition = inputFilename.substring(3, inputFilename.indexOf('.'));
			
			File newFile = new File(mOutDir, preposition);
			System.err.println("New file: " + newFile.getName());

			writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(newFile, mAppend)));
			List<Sentence> sentences = (List)doc.getAnnotationList(Sentence.class);
			if(sentences != null) {
				String originalString = null;
				String sentenceText = null;
				String parseString = null;
				HeadAnnotation head = null;
				
				for(Sentence sentence : sentences) {
					
					List<tratz.runpipe.annotations.Token> containedList = RunpipeUtils.getSublist(sentence, tokenList);
					
					for(HeadAnnotation headAnnot : heads) {
						if(headAnnot.getStart() >= sentence.getStart() && headAnnot.getEnd() <= sentence.getEnd()) {
							head = headAnnot;
							
							//writer.println(sentence.getAnnotText());
					List<Set<Annotation>> annotSets = new ArrayList<Set<Annotation>>();
					
					Map<WfrEntry, List<Set<tratz.parse.types.Token>>> ruleToAnnotSets = new HashMap<WfrEntry, List<Set<tratz.parse.types.Token>>>();
					
					//writer.println("#" + sentence.getAnnotText());
					//writer.println("#" + sentence.getParseString());
					sentenceText = sentence.getAnnotText();
					parseString = sentence.getParseString();
					//System.err.println(sentenceText);
					//System.err.println(parseString);
					
					int numTokens = containedList.size();
					int headIndex = -1;
					for(int i = 0; i < numTokens; i++) {
						if(headAnnot.getEnd() == containedList.get(i).getEnd()) {
							headIndex = i;
						}
					}
					
					List<tratz.parse.types.Token> parserTokens = new ArrayList<tratz.parse.types.Token>();
					for(int i = 0; i < containedList.size(); i++) {
						tratz.runpipe.annotations.Token token = containedList.get(i);
						parserTokens.add(new Token(token.getAnnotText(), token.getPos(), i+1));
					}
					
					List<tratz.parse.types.Arc> arcs = new ArrayList<tratz.parse.types.Arc>();
					for(int i = 0; i < containedList.size(); i++) {
						Token t = parserTokens.get(i);
						tratz.runpipe.annotations.Token tok = containedList.get(i);
						tratz.runpipe.annotations.Token.Arc headTokParent = tok.getParentArc();
						if(headTokParent != null) {
							tratz.runpipe.annotations.Token headTok = headTokParent.getHead();
							if(headTok != null) {
								String depend = headTokParent.getDependency();
								int parentIndex = containedList.indexOf(headTok);
								Token normalHead = parserTokens.get(parentIndex);
								arcs.add(new Arc(t, normalHead, depend));
							}
						}
					}
					
					tratz.parse.types.Sentence parsedSentence = new tratz.parse.types.Sentence(parserTokens);
					// Potentially bad to be using null for root
					Parse parse = new Parse(parsedSentence, null, arcs);
					for(WfrEntry rule : mComplexRules) {
						Set<tratz.parse.types.Token> annotations = 
							rule.getWfrRule().getProductions(parserTokens, parse, headIndex);
						if(annotations != null && annotations.size() > 0) {
							List<Set<tratz.parse.types.Token>> list = new ArrayList<Set<tratz.parse.types.Token>>();
							list.add(annotations);
							ruleToAnnotSets.put(rule, list);
						}
					}					
										
					if(head != null) {
						String sense = head.getSenseId();
						if(mTruthMap.containsKey(headAnnot.getId())) {
							sense = mTruthMap.get(headAnnot.getId());
						

							if(sense.trim().equals("")) {
								System.err.println("Warning: Unexpected whitespace-only or empty string class name for instance: " + headAnnot.getId());
							}
						
						}
						if(sense != null && !sense.equals("-1") && (!mOverridden || mTruthMap.containsKey(headAnnot.getId()))) { // Some bad data points. Ignore these here to get the proper number of data points								
							writeFeatures(writer, originalString, sentenceText, parseString, head, sense, ruleToAnnotSets, sentence, tokenList);
						}
					}
					else {
						System.err.println("ERROR: head not found");
					}
				}
					}
				}//
			}
		}
		catch(Exception e) {
			throw new ProcessException(e);
		}
		writer.close();
	}
	
	private void writeFeatures(PrintWriter writer, 
								   String originalString, 
								   String sentenceText, 
								   String parseString, 
								   HeadAnnotation headAnnot,
								   String sense,
								   Map<WfrEntry, List<Set<tratz.parse.types.Token>>> ruleToAnnotSets,
								   Sentence sentence,
								   List<tratz.runpipe.annotations.Token> tokens) throws Exception {
		writer.print(headAnnot.getId()+"\30" + sense+"\30"); // sense
		List<WfrEntry> allRules = new ArrayList<WfrEntry>();
		allRules.addAll(Arrays.asList(mComplexRules));
		
		List<tratz.runpipe.annotations.Token> tokenList = RunpipeUtils.getSublist(sentence, tokens);
		
		boolean foundHead = false;
		for(tratz.runpipe.annotations.Token t : tokenList) {
			if(t.getStart() == headAnnot.getStart() && t.getEnd() == headAnnot.getEnd()) {
				foundHead = true;
			}
		}
		
		if(!foundHead) {
			System.err.println("ERROR: !Failed to find head for: " + headAnnot.getId());
		}
		
		Set<String> feats = new HashSet<String>();
		for(WfrEntry rule : allRules) {
			
			List<Set<Token>> annotSetList = ruleToAnnotSets.get(rule);
			if(annotSetList != null && annotSetList.size() > 0) {
				//System.err.println("Printing for rule: " + rule);
				for(int i = 0; i < annotSetList.size(); i++) {
					List<Token> annots = new ArrayList<Token>(annotSetList.get(i));
					int numAnnots = annots.size();
					//writer.print(rule.mId);
					for(int j = 0; j < numAnnots; j++) {
						Token annot = annots.get(j);
						
						String annotText = annot.getText();
						String type = annot.getPos();
						if(type == null) {
							System.err.println("Null type for: " + annotText);
						}
						// Strip off ending periods
						String[] parts = annotText.split("\\s+");
						String lastPart = parts[parts.length-1];
						int periodIndex = lastPart.indexOf('.');
						if(periodIndex == lastPart.length()-1 && periodIndex != 0) {
							annotText = annotText.substring(0, periodIndex);
						}
						List<FeatureExtractionRule> fers = rule.getFERs();
						for(FeatureExtractionRule fer : fers) {
							Set<String> productions = fer.getProductions(annotText, type);
							//System.err.println(fer.getClass().getName());
							if(productions != null) {
								for(String production : productions) {
									production = production.replace("\30", "*c*");
									//feats.add((rule.mPrefix+":"+fer.getPrefix() +":"+tindex+":"+production));//.toLowerCase() for Dirk
									feats.add((rule.getPrefix()+":"+fer.getPrefix() +":"+production).toLowerCase());//.toLowerCase()
									//writer.print((rule.mPrefix + ":" + fer.getPrefix() + ":" + production +"\30").toLowerCase());
								}
							}
						}
					}
				}
			}
		}
		List<String> featList = new ArrayList<String>(feats);
		Collections.sort(featList);
		final int numFeats = featList.size();
		for(int i = 0; i < numFeats; i++) {
			String feati = featList.get(i);
			writer.print(feati);
			writer.print("\30");
		}
		writer.println();
	}
	
	@Override
	public void batchFinished() {

	}

	
}