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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;



import tratz.cmdline.CommandLineOptions;
import tratz.cmdline.CommandLineOptionsParser;
import tratz.cmdline.ParsedCommandLine;
import tratz.featgen.MultiStepFeatureGenerator;
import tratz.jwni.WordNet;
import tratz.parse.io.ConllxSentenceReader;
import tratz.parse.transform.VchTransformer;
import tratz.parse.types.Arc;
import tratz.parse.types.Parse;
import tratz.parse.types.Token;
import tratz.parse.util.ParseConstants;

/**
 * Rather ugly code for extracting features for PropBank arguments and adjuncts
 *
 */
public class FeatureExtractionRoutine {
	
	public final static String OPT_INPUT_FILES = "input",
							   OPT_OUTPUT_DIR = "outputdir",
							   OPT_AUTOPARSED_FILES = "autoparsedfiles",
							   OPT_WORDNET_DIR = "wndir",
							   OPT_WFR_RULES = "wfr",
							   OPT_FEATURE_RULES = "fer",
							   OPT_COMBO_RULES = "comborules",
							   OPT_IS_FOR_TESTING = "isfortesting",
							   OPT_TRUTH_FILE = "truthfile"; // Why do I have this parameter? Such information should be recoverable from the feature files instead			
	
	private static CommandLineOptions createOptions() {
		CommandLineOptions cmdOpts = new CommandLineOptions();
		cmdOpts.addOption(OPT_INPUT_FILES, "file(s)", "file(s) containing the srl info");
		cmdOpts.addOption(OPT_OUTPUT_DIR, "file", "directory for output files");
		cmdOpts.addOption(OPT_AUTOPARSED_FILES, "file(s)", "file(s) containing versions of the files parsed using an automatic parser");
		cmdOpts.addOption(OPT_WORDNET_DIR, "file", "the dictionary (dict) directory of WordNet");
		cmdOpts.addOption(OPT_WFR_RULES, "file", "the file containing the word-finding rules");
		cmdOpts.addOption(OPT_FEATURE_RULES, "file", "the file containing the feature extraction rules");
		cmdOpts.addOption(OPT_COMBO_RULES, "file", "the file containing the combination rules");
		cmdOpts.addOption(OPT_IS_FOR_TESTING, "boolean", "indicates if the features being generated are for testing");
		cmdOpts.addOption(OPT_TRUTH_FILE, "file", "file that will contain two columns (1) the list of ids (2) the correct assignments");
		return cmdOpts;
	}
	
	
	public static Set<String> SKIP_DEPS = new HashSet<String>(Arrays.asList(ParseConstants.DETERMINER_DEP,
			ParseConstants.CLEFT_DEP,
			ParseConstants.NOUN_COMPOUND_DEP,
			ParseConstants.NUMERAL_MOD_DEP,
			ParseConstants.NUMERAL_MOD_DEP,
			ParseConstants.CONJUNCTION_DEP,
			ParseConstants.SUBORDINATE_CLAUSE_MARKER_DEP,
			ParseConstants.MEASURE_DEP,
			ParseConstants.PREP_OBJECT_DEP,
			ParseConstants.PREP_COMPLEMENT_DEP,
			ParseConstants.EXPLETIVE_THERE_DEP,
			ParseConstants.VERBAL_CHAIN_DEP,
			ParseConstants.UNSPECIFIED_DEP,
			ParseConstants.COORDINATING_CONJUNCTION_DEP,
			ParseConstants.COMBO_DEP,
			ParseConstants.COMPLEMENTIZER_DEP,
			ParseConstants.APPOSITIVE_DEP,
			ParseConstants.PARATAXIS_DEP,
			ParseConstants.PRECONJ_DEP,
			ParseConstants.PREDETERMINER_DEP,
			ParseConstants.POSSESSOR_DEP,
			ParseConstants.POSTERIOR_LOCATION_DEP,
			ParseConstants.POSSESSIVE_MARKER,
			ParseConstants.PUNCTUATION_DEP,
			ParseConstants.RELATIVE_CLAUSE_DEP,
			ParseConstants.SUFFICIENCY_COMPLEMENT_DEP,
			ParseConstants.QUANTMOD_DEP,
			ParseConstants.ROOT_DEP));
	
	
	
	public static void main(String[] args) throws Exception {
		
		ParsedCommandLine cmdLine = new CommandLineOptionsParser().parseOptions(createOptions(), args);
		
		String inputFiles = cmdLine.getStringValue(OPT_INPUT_FILES);
		File outputDir = new File(cmdLine.getStringValue(OPT_OUTPUT_DIR));
		String autoparsedFiles = cmdLine.getStringValue(OPT_AUTOPARSED_FILES);
		String wordNetDir = cmdLine.getStringValue(OPT_WORDNET_DIR);
		String wfrListFile = cmdLine.getStringValue(OPT_WFR_RULES);
		String featRulesFile = cmdLine.getStringValue(OPT_FEATURE_RULES);
		String comboFile = cmdLine.getStringValue(OPT_COMBO_RULES);
		boolean generateFeaturesUsingAutoParse = autoparsedFiles != null;
		if(generateFeaturesUsingAutoParse ) System.err.println("Generating features using automatic parse output");
		boolean isForTesting = cmdLine.getBooleanValue(OPT_IS_FOR_TESTING);
		File truthFile = new File(cmdLine.getStringValue(OPT_TRUTH_FILE));
		
		new WordNet(new File(wordNetDir));
		
		VchTransformer vchTransformer = new VchTransformer();
		
		MultiStepFeatureGenerator featGen = new MultiStepFeatureGenerator(wfrListFile, featRulesFile, comboFile);
		
		// reader for auto-parsed data
		ConllxSentenceReader conllxReader = new ConllxSentenceReader();
		
		PrintWriter truthWriter = new PrintWriter(new FileWriter(truthFile));
		// Map for holding the writers; one writer per dependency type
		Map<String, PrintWriter> dependencyTypeToWriter = new HashMap<String, PrintWriter>();
		
		String[] inFiles =  inputFiles.split(File.pathSeparator);
		String[] autoparsed = null;
		if(autoparsedFiles != null) {
			autoparsed = autoparsedFiles.split(File.pathSeparator);
		}
		for(int f = 0; f < inFiles.length; f++) {
			String inFile = inFiles[f];
			String autoFile = null;
			if(autoparsed != null) {
				autoFile = autoparsed[f];
			}
			
		BufferedReader goldReader = new BufferedReader(new FileReader(inFile));
		BufferedReader autoParseReader = null;
		if(autoFile != null) {
			autoParseReader = new BufferedReader(new FileReader(autoFile));
		}
		Object[] readResult = null;
		SentenceReaderForSRL srlReader = new SentenceReaderForSRL();
		
		
		int lineno = 0;
		int snum = 0;
		while((readResult = srlReader.readSentence(goldReader)) != null && readResult[0] != null) {
			snum++;
			lineno++;
			Parse goldParse = (Parse)readResult[0];
			Parse autoParse = null;
			if(autoParseReader != null) {
				autoParse = conllxReader.readSentence(autoParseReader);
			}
			
			if(autoParse != null) {
				vchTransformer.performTransformation(autoParse);
			}
			
			Arc[] tokenToHead = goldParse.getHeadArcs();
			
			Map<Arc, SemanticArc> semArcs = (Map<Arc, SemanticArc>)readResult[1];
			List<Token> tokens = goldParse.getSentence().getTokens();
			List<Token> tokensAUTO = autoParse != null ? autoParse.getSentence().getTokens() : null;
			final int numTokens = tokens.size();
			
			for(int tokNum = 1; tokNum < numTokens+1; tokNum++) {
				lineno++;
				
				Arc a = tokenToHead[tokNum];
				Arc arcAUTO = autoParse != null ? autoParse.getHeadArcs()[tokNum] : null;
				
				if(a == null && arcAUTO == null) continue;
				
				SemanticArc semArc = semArcs.get(a);
					String fullForm = null;
					String clazz;
					if(semArc != null) {
						fullForm = semArc.getFullForm();
						clazz = semArc.getType();
					}
					else {
						fullForm = "NA";
						clazz = "NA";
					}
					
					
					String label = lineno+"_"+snum + "_"+(a == null ? "" : a.getHead().getIndex()+"_"+a.getChild().getIndex());
					String dep = a == null ? null : a.getDependency();
					String depAUTO = arcAUTO == null ? null : arcAUTO.getDependency();
					
					// Fix for pseudopreps; should eventually fix this in the parse conversion (SHOULD BE FIXED NOW!)
					if(dep != null && dep.equals(ParseConstants.PREP_OBJECT_DEP) && a.getHead().getPos().startsWith("V")) {
						dep = ParseConstants.DIRECT_OBJECT_DEP;
					}
					if(depAUTO != null && depAUTO.equals(ParseConstants.PREP_OBJECT_DEP) && arcAUTO.getHead().getPos().startsWith("V")) {
						depAUTO = ParseConstants.DIRECT_OBJECT_DEP;
					}
					
					if(a != null) {
						if(a!=null&&!SKIP_DEPS.contains(dep) || arcAUTO!=null&&!SKIP_DEPS.contains(depAUTO)) {
							truthWriter.println(label+"\t"+clazz);
						}
					}
					if((!generateFeaturesUsingAutoParse && a != null && !SKIP_DEPS.contains(dep))
							|| (generateFeaturesUsingAutoParse && arcAUTO != null && !SKIP_DEPS.contains(depAUTO))) {
						// Determine if gold and auto parse trees connect the same pair of words together
						boolean arcMismatch = a==null||arcAUTO==null||!(a.getChild().getIndex() == arcAUTO.getChild().getIndex() && (a.getHead()==null?0:a.getHead().getIndex()) == (arcAUTO.getHead()==null?0:arcAUTO.getHead().getIndex()));
						if(!generateFeaturesUsingAutoParse // Generate if using gold
								||
								(arcMismatch && isForTesting) // Generate for testing using auto EVEN IF there is arc mismatch
								|| !arcMismatch) // Generate for training using auto ONLY IF there is NOT arc mismatch
						{
							String depWriterName;
							if(generateFeaturesUsingAutoParse && !SKIP_DEPS.contains(depAUTO)) {
								depWriterName = depAUTO;
							}
							else {
								depWriterName = dep;
							}
							if(depWriterName.equals(ParseConstants.PREP_MOD_DEP)) {
								depWriterName  = "prep_"+tokens.get(tokNum-1).getText().toLowerCase();
							}
							if(depWriterName.equals(ParseConstants.PARTICIPLE_MODIFIER_DEP)) {
								depWriterName = "partmod_"+(generateFeaturesUsingAutoParse?tokensAUTO.get(tokNum-1).getPos():tokens.get(tokNum-1).getPos());
							}
							
							if(generateFeaturesUsingAutoParse) {
								label =  lineno+"_"+snum + "_"+arcAUTO.getHead().getIndex()+"_"+arcAUTO.getChild().getIndex();//+"_"+arcAUTO.getHead().getText()+"_"+arcAUTO.getChild().getText();
							}
							
							if(generateFeaturesUsingAutoParse && arcMismatch) {
								clazz = "DUMBY";
								if(!isForTesting) {
									// We tested at the beginning for this, but let's test again here because this is the critical point
									System.err.println("Can't use auto parsed data with vs-gold-mismatches for training");
								}
							}
								
								PrintWriter writer = dependencyTypeToWriter.get(depWriterName);
								if(writer == null) {
									outputDir.mkdirs();
									dependencyTypeToWriter.put(depWriterName, writer = new PrintWriter(new FileWriter(new File(outputDir, depWriterName))));
								}
								// 	write the instance id and class label
								
								writer.print(label + "\30"+clazz+"\30");
								
								
								// generate features
								Set<String> feats = null;
								feats = featGen.generateFeatures(
										generateFeaturesUsingAutoParse ? autoParse.getSentence().getTokens() : goldParse.getSentence().getTokens(), 
												generateFeaturesUsingAutoParse ? autoParse : goldParse, 
																tokNum-1);
								// 	write the features
								List<String> featList = new ArrayList<String>(feats);
								Collections.sort(featList);
								int numFeats = featList.size();
								for(int i = 0; i < numFeats; i++) {
									String feat = featList.get(i);
									writer.print(feat);
									writer.print('\30');
								}
					
								writer.println();
						}
					}
			}
		}
		System.err.println("Number of sentences processed: " + snum);
			goldReader.close();
			if(autoParseReader != null) autoParseReader.close();
			truthWriter.close();
		}
		
		// Close out the writers
		for(PrintWriter writer : dependencyTypeToWriter.values()) {
			writer.close();
		}
	}
	
}