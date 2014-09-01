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

package tratz.semantics.nn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import tratz.cmdline.CommandLineOptions;
import tratz.cmdline.CommandLineOptionsParser;
import tratz.cmdline.ParsedCommandLine;
import tratz.featgen.MultiStepFeatureGenerator;
import tratz.jwni.WordNet;
import tratz.parse.types.Arc;
import tratz.parse.types.Parse;
import tratz.parse.types.Sentence;
import tratz.parse.types.Token;
import tratz.parse.util.ParseConstants;

public class NnFeatureExtractionRoutine {
	
	public final static String PARAM_INPUT_DIR = "indir";
	public final static String PARAM_OUTPUT_FILE = "outfile";
	public final static String PARAM_WORDNET_DIR = "wndir";
	
	public final static String PARAM_WFR_RULES_FILE = "wfr";
	public final static String PARAM_FER_RULES_FILE = "fer";
	public final static String PARAM_COMBO_RULES_FILE = "comborules";
	
	private static CommandLineOptions createOptions() {
		CommandLineOptions opts = new CommandLineOptions();
		opts.addOption(PARAM_INPUT_DIR, "file", "location of input directory");
		opts.addOption(PARAM_OUTPUT_FILE, "file", "location of output file");
		opts.addOption(PARAM_WORDNET_DIR, "file", "location of WordNet's dictionary (dict) dir");
		
		opts.addOption(PARAM_WFR_RULES_FILE, "file", "location of the file containing the word-finding rules");
		opts.addOption(PARAM_FER_RULES_FILE, "file", "location of the file containing the feature-extraction rules");
		opts.addOption(PARAM_COMBO_RULES_FILE, "file", "location of the file containing the combination rules");

		return opts;
	}
	
	public static void main(String[] args) throws Exception {
		
		ParsedCommandLine cmdLine = new CommandLineOptionsParser().parseOptions(createOptions(), args);
		
		File inDir = new File(cmdLine.getStringValue(PARAM_INPUT_DIR));
		File outFile = new File(cmdLine.getStringValue(PARAM_OUTPUT_FILE));
		File wnDir = new File(cmdLine.getStringValue(PARAM_WORDNET_DIR));
		
		String wfrRulesFile = cmdLine.getStringValue(PARAM_WFR_RULES_FILE);
		String ferRulesFile = cmdLine.getStringValue(PARAM_FER_RULES_FILE);
		String comboRulesFile = cmdLine.getStringValue(PARAM_COMBO_RULES_FILE);
		
		// Make parent directory if it doesn't exist
		if(!outFile.getParentFile().exists()) {
			outFile.getParentFile().mkdirs();
		}
		new WordNet(wnDir);
		PrintWriter featureWriter = new PrintWriter(new FileWriter(outFile));
		
		MultiStepFeatureGenerator nnFeatGen = new MultiStepFeatureGenerator(wfrRulesFile, ferRulesFile, comboRulesFile);
		
		Set<String> instanceSet = new HashSet<String>();
		File[] files = inDir.listFiles();
		for(File f : files) {
			String fname = f.getName();
			if(fname.matches("[A-Z].*")) {
				int periodIndex = f.getName().indexOf('.');
				String className = f.getName().substring(0, periodIndex == -1 ? f.getName().length() : periodIndex);
				BufferedReader reader = new BufferedReader(new FileReader(f));
				String line = null;
				while((line = reader.readLine()) != null) {
					line = line.trim();
					if(!line.equals("") && !line.startsWith("#") && !line.startsWith("//")) {
						//lookingFor.remove(line);
						
						String[] split = line.split("\\t+");
						String pl = null, pr = null;
						pl = new LinkedList<String>(Arrays.asList(split[0]
							                                                .split("\\s+"))).getLast();
						pr = new LinkedList<String>(Arrays.asList(split[1]
							                                                .split("\\s+"))).getLast();
						if(instanceSet.contains(pl + "\t" + pr)) {
							System.err.println("Duplicate: " + pl + "\t" + pr + "\t" + f.getName());
						}
						else {
							featureWriter.print(pl+"_"+pr+"\30"+className+"\30");
							instanceSet.add(pl + "\t" + pr);
							
							List<Token> tokens = new ArrayList<Token>();
							int wordIndex = 0; // the dependent word
							
							Token leftToken = new Token(pl, "NN", 1);
							Token rightToken = new Token(pr, "NN", 2);
							tokens.add(leftToken);
							tokens.add(rightToken);
							Arc nnLink = new Arc(leftToken, rightToken, ParseConstants.NOUN_COMPOUND_DEP);
							List<Arc> arcs = new ArrayList<Arc>();
							arcs.add(nnLink);
							Parse parse = new Parse(new Sentence(tokens), null, arcs);
							Set<String> features = nnFeatGen.generateFeatures(tokens, parse, wordIndex);
							for(String feat : features) {
								featureWriter.print(feat);
								featureWriter.print("\30");
							}
							featureWriter.println();
						}
					}
				}
				reader.close();
			}
		}
		featureWriter.close();
		
	}
	
}