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
import tratz.jwni.IndexEntry;
import tratz.jwni.POS;
import tratz.jwni.WordNet;
import tratz.parse.io.ConllxSentenceReader;
import tratz.parse.transform.VchTransformer;
import tratz.parse.types.Arc;
import tratz.parse.types.Parse;
import tratz.parse.types.Token;

public class FeatureExtractionRoutinePredicates {
	
	public static Set<String> POS_TO_INCLUDE = new HashSet<String>(Arrays.asList("JJ","VBN","VBD","VB","VBP","VBZ","VBG"));
	
	
	public final static String OPT_INPUT_FILES = "input",
			OPT_OUTPUT_DIR = "outputdir",
			OPT_AUTOPARSED_FILES = "autoparsedfiles",
			OPT_WORDNET_DIR = "wndir", OPT_WFR_RULES = "wfr",
			OPT_FEATURE_RULES = "fer", OPT_COMBO_RULES = "comborules",
			//OPT_USE_AUTO_PARSE = "useautoparsed",
			OPT_IS_FOR_TESTING = "isfortesting", 
			OPT_TRUTH_FILE = "truthfile",
			OPT_PROPBANK_FRAMES_FILES = "propbankframes";

	private static CommandLineOptions createOptions() {
		CommandLineOptions cmdOpts = new CommandLineOptions();
		cmdOpts.addOption(OPT_INPUT_FILES, "file(s)",
				"file(s) containing the srl info");
		cmdOpts.addOption(OPT_OUTPUT_DIR, "file", "directory for output files");
		cmdOpts
				.addOption(OPT_AUTOPARSED_FILES, "file(s)",
						"file(s) containing versions of the files parsed using an automatic parser");
		cmdOpts.addOption(OPT_WORDNET_DIR, "file",
				"the dictionary (dict) directory of WordNet");
		cmdOpts.addOption(OPT_WFR_RULES, "file",
				"the file containing the word-finding rules");
		cmdOpts.addOption(OPT_FEATURE_RULES, "file",
				"the file containing the feature extraction rules");
		cmdOpts.addOption(OPT_COMBO_RULES, "file",
				"the file containing the combination rules");
		//cmdOpts
				//.addOption(
//						OPT_USE_AUTO_PARSE,
						//"boolean",
//						"indicates if the automatically parsed files should be used for feature generation");
		cmdOpts.addOption(OPT_IS_FOR_TESTING, "boolean",
				"indicates if the features being generated are for testing");
		cmdOpts
				.addOption(
						OPT_TRUTH_FILE,
						"file",
						"file that will contain two columns (1) the list of ids (2) the correct assignments");
		cmdOpts.addOption(OPT_PROPBANK_FRAMES_FILES, "file",
				"directory containg the PropBank frame files");
		return cmdOpts;
	}

	public static void main(String[] args) throws Exception {

		ParsedCommandLine cmdLine = new CommandLineOptionsParser()
				.parseOptions(createOptions(), args);

		String inputFiles = cmdLine.getStringValue(OPT_INPUT_FILES);
		File outputDir = new File(cmdLine.getStringValue(OPT_OUTPUT_DIR));
		String autoparsedFiles = cmdLine.getStringValue(OPT_AUTOPARSED_FILES);
		String wordNetDir = cmdLine.getStringValue(OPT_WORDNET_DIR);
		String wfrListFile = cmdLine.getStringValue(OPT_WFR_RULES);
		String featRulesFile = cmdLine.getStringValue(OPT_FEATURE_RULES);
		String comboRulesFile = cmdLine.getStringValue(OPT_COMBO_RULES);
		boolean generateUsingAutoParsed = autoparsedFiles != null; 
			//cmdLine			.getBooleanValue(OPT_USE_AUTO_PARSE);
		boolean isForTesting = cmdLine.getBooleanValue(OPT_IS_FOR_TESTING);
		File truthFile = new File(cmdLine.getStringValue(OPT_TRUTH_FILE));
		File propBankFramesDir = new File(cmdLine
				.getStringValue(OPT_PROPBANK_FRAMES_FILES));

		File[] frameFiles = propBankFramesDir.listFiles();
		Set<String> framesSet = new HashSet<String>();
		for(File f : frameFiles) {
			if(f.getName().endsWith(".xml")) {
				String frame = f.getName().substring(0,
						f.getName().indexOf(".xml"));
				framesSet.add(frame);
			}
		}

		PrintWriter truthWriter = new PrintWriter(new FileWriter(truthFile));

		new WordNet(new File(wordNetDir));

		VchTransformer vchTransformer = new VchTransformer();

		MultiStepFeatureGenerator featGen = new MultiStepFeatureGenerator(
				wfrListFile, featRulesFile, comboRulesFile);

		// reader for auto-parsed data
		ConllxSentenceReader conllxReader = new ConllxSentenceReader();

		String[] inFiles = inputFiles.split(File.pathSeparator);
		String[] autoFiles = null;
		if(autoparsedFiles != null) {
			autoFiles = autoparsedFiles.split(File.pathSeparator);
		}

		int numFiles = inFiles.length;
		// Map for holding the writers; one writer per dependency type
		Map<String, StringBuilder> dependencyTypeToWriter = new HashMap<String, StringBuilder>();

		for(int f = 0; f < numFiles; f++) {

			BufferedReader goldReader = new BufferedReader(new FileReader(
					inFiles[f]));
			BufferedReader autoParseReader = null;
			if(autoFiles != null) {
				autoParseReader = new BufferedReader(new FileReader(autoFiles[f]));
			}
			Object[] readResult = null;
			SentenceReaderForSRL srlReader = new SentenceReaderForSRL();

			int snum = 0;
			while((readResult = srlReader.readSentence(goldReader)) != null
					&& readResult[0] != null) {
				snum++;
				Parse goldParse = (Parse)readResult[0];
				Parse autoParse = null;
				if(autoParseReader != null) {
					autoParse = conllxReader.readSentence(autoParseReader);
					vchTransformer.performTransformation(autoParse);
				}

				

				Map<Arc, SemanticArc> semArcs = (Map<Arc, SemanticArc>)readResult[1];
				List<Token> tokens = goldParse.getSentence().getTokens();
				List<Token> autoTokens = autoParse == null ? null : autoParse.getSentence().getTokens();
				for(int tokNum = 1; tokNum < tokens.size(); tokNum++) {
					Token goldToken = tokens.get(tokNum - 1);
					Token autoToken = autoTokens == null ? null : autoTokens.get(tokNum - 1);
					IndexEntry entry = WordNet.getInstance().lookupIndexEntry(
							POS.VERB, goldToken.getText());

					String depWriterName;
					if(entry == null) {
						depWriterName = goldToken.getText().toLowerCase();
					}
					else {
						depWriterName = entry.getLemma();
						
					}
					
					if(depWriterName.equals("be")) {
						// TODO: Hacked in fix here for one case where 'be' is given the label fall.01
						continue;
					}
					String clazz = goldToken.getLexSense();
					String label = snum + "_" + tokNum;
					
					if(!goldToken.getPos().matches("IN|TO|RP|RB|PRP")) {
						truthWriter.println(label + "\t" + clazz);
					}
					/*if(autoToken != null && !autoToken.getPos().matches("IN|TO|RP|RB|PRP")) {
						
					}
					else {
						continue;
					}*/

					if(((goldToken.getLexSense() != null || framesSet
							.contains(depWriterName)) && POS_TO_INCLUDE
							.contains(goldToken.getPos()))
							|| (isForTesting && POS_TO_INCLUDE
									.contains(autoToken.getPos()))) {
						if(!goldToken.getPos().matches("IN|TO|RP|RB|PRP")) {

							if(depWriterName.contains(".")
									|| depWriterName.contains("/")
									|| depWriterName.contains("\\")
									|| depWriterName.contains(",")) {
								// System.err.println("Bizarre: " + entry + " "
								// + goldToken.getText().toLowerCase());
								continue;
							}

							if(!goldToken.getText().toLowerCase().startsWith(
									"" + depWriterName.charAt(0))
									&& !depWriterName.equals("be")
									&& clazz != null) {
								System.err.println("Potential mismatch: "
										+ goldToken.getText().toLowerCase()
										+ " " + depWriterName.charAt(0) + " "
										+ clazz);
							}

							StringBuilder writer = dependencyTypeToWriter
									.get(depWriterName);
							if(writer == null) {
								dependencyTypeToWriter.put(depWriterName,
										writer = new StringBuilder());
							}

							// write the instance id and class label

							writer.append(label + "\30" + clazz + "\30");

							// generate features
							Set<String> feats = null;
							feats = featGen
									.generateFeatures(
											generateUsingAutoParsed ? autoParse.getSentence()
													.getTokens()
													: goldParse.getSentence().getTokens(),
											generateUsingAutoParsed ? autoParse
													: goldParse,
											 tokNum - 1);
							// write the features
							List<String> featList = new ArrayList<String>(feats);
							Collections.sort(featList);
							int numFeats = featList.size();
							for(int i = 0; i < numFeats; i++) {
								String feat = featList.get(i);
								writer.append(feat);
								writer.append('\30');
							}

							writer.append('\n');
						}
					}
				}
			}
			goldReader.close();
			if(autoParseReader != null) autoParseReader.close();

		}
		truthWriter.close();

		// Close out the writers
		for(String key : dependencyTypeToWriter.keySet()) {
			File outfile = new File(outputDir, key);
			outfile.getAbsoluteFile().getParentFile().mkdirs();
			PrintWriter writer = new PrintWriter(new FileWriter(outfile));
			writer.println(dependencyTypeToWriter.get(key).toString());
			writer.close();
		}
	}
	
}