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

package tratz.semantics.poss;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
import tratz.parse.io.SentenceReader;
import tratz.parse.types.Parse;
import tratz.parse.types.Token;

public class FeatureExtractionRoutine {
	
	public static class PossessiveInstance {
		int id;
		String datasource;
		String token1Text;
		String token2Text;
		int token1Index;
		int token2Index;
		int sentenceId;
		String clazz;
		public PossessiveInstance(int id, String datasource, String token1Text, String token2Text, int token1Index, int token2Index, int sentenceId, String clazz) {
			this.id = id;
			this.datasource = datasource;
			this.token1Text = token1Text;
			this.token2Text = token2Text;
			this.token1Index = token1Index;
			this.token2Index = token2Index;
			this.sentenceId = sentenceId;
			this.clazz = clazz;
		}
		
		public int hashCode() {
			return id + datasource.hashCode();
		}
		
		public boolean equals(Object other) {
			if(!(other instanceof PossessiveInstance)) {
				return false;
			}
			else {
				PossessiveInstance i = (PossessiveInstance)other;
				return datasource.equals(i.datasource) && token1Index==i.token1Index && token2Index==i.token2Index && sentenceId==i.sentenceId;
			}
		}
	}
		
		
	
	private boolean mInvertIds;
	private Set<String> mIds;
	private MultiStepFeatureGenerator mFeatGen;
	private Map<String, List<Parse>> mDatasourceToParses;
	private PrintWriter mFeatureWriter;
	public FeatureExtractionRoutine(boolean invertIds, 
			Set<String> ids, 
			MultiStepFeatureGenerator featGen,
			Map<String, List<Parse>> datasourceToParses,
			PrintWriter featureWriter) {
		mInvertIds = invertIds;
		mIds = ids;
		mFeatGen = featGen;
		mDatasourceToParses = datasourceToParses;
		mFeatureWriter = featureWriter;
	}
	
	public final static String OPT_INPUT_DIRECTORY = "inputdir",
								OPT_OUTPUT_FILE = "outputfile",
								OPT_IDS_FILE = "ids",
								OPT_INVERT_IDS = "invertids",
								OPT_WORDNET_DIR = "wndir",
								OPT_WFR_FILE = "wfr",
								OPT_FER_FILE = "fer",
								OPT_COMBO_FILE = "comborules";
	
	public static CommandLineOptions createOptions() {
		CommandLineOptions opts = new CommandLineOptions();
		opts.addOption(OPT_INPUT_DIRECTORY, "file", "directory containing the input files");
		opts.addOption(OPT_OUTPUT_FILE, "file", "the output file");
		opts.addOption(OPT_IDS_FILE, "file", "the file containing the list of ids of interest");
		opts.addOption(OPT_WORDNET_DIR, "file", "the dictionary directory (dict) of WordNet");
		opts.addOption(OPT_WFR_FILE, "file", "the file contaning the word-finding rules");
		opts.addOption(OPT_FER_FILE, "file", "the file contaning the feature extraction rules");
		opts.addOption(OPT_COMBO_FILE, "file", "the file contaning the combination feature rules");
		return opts;
	}
	
	
	public static void main(String[] args) throws Exception {
		ParsedCommandLine cmdLine = new CommandLineOptionsParser().parseOptions(createOptions(), args);
		
		String inputDirectory = cmdLine.getStringValue(OPT_INPUT_DIRECTORY);
		String outputFileString = cmdLine.getStringValue(OPT_OUTPUT_FILE);
		String idsFile = cmdLine.getStringValue(OPT_IDS_FILE);
		boolean invertIds = Boolean.parseBoolean(cmdLine.getStringValue(OPT_INVERT_IDS));
		File wnDir = new File(cmdLine.getStringValue(OPT_WORDNET_DIR));
		String wfrFile = cmdLine.getStringValue(OPT_WFR_FILE);
		String featGenFile = cmdLine.getStringValue(OPT_FER_FILE);
		String comboRulesFile = cmdLine.getStringValue(OPT_COMBO_FILE);
		
		File outputFile = new File(outputFileString);
		
		outputFile.getParentFile().mkdirs();
		
		System.err.print("Reading WordNet...");
		new WordNet(wnDir);
		System.err.println("Done.");
		
		Set<String> idsToGeneratorFor = readIds(idsFile);
		
		Map<String, List<Parse>> datasourceToSentences = new HashMap<String, List<Parse>>();
		System.err.print("Reading WSJ...");

		List<Parse> parses = new ArrayList<Parse>();
		readSentences("AutoparsedPtbFull.conllX", new ConllxSentenceReader(), parses);
		datasourceToSentences.put("PTB_WSJ", parses);
		System.err.println("Done.");
		System.err.print("Reading Jungle Book...");
		List<Parse> parses2 = new ArrayList<Parse>();
		readSentences("JBrebuilt.conllX", new ConllxSentenceReader(), parses2);
		datasourceToSentences.put("JB", parses2);
		System.err.println("Done.");
		System.err.print("Reading HDFRE...");
		List<Parse> parses3 = new ArrayList<Parse>();
		readSentences("HDrebuilt.conllX", new ConllxSentenceReader(), parses3);
		datasourceToSentences.put("HDFRE", parses3);
		
		System.err.print("Reading Wiktionary...");
		
		MultiStepFeatureGenerator featGen = new MultiStepFeatureGenerator(wfrFile, featGenFile, comboRulesFile);
		
		PrintWriter featureWriter = new PrintWriter(new FileWriter(outputFile));
		FeatureExtractionRoutine routine = new FeatureExtractionRoutine(invertIds, idsToGeneratorFor, featGen, datasourceToSentences, featureWriter);
		File[] files = new File(inputDirectory).listFiles();
		Set<PossessiveInstance> allInstances = new HashSet<PossessiveInstance>();
		for(File f : files) {
			// startsWith("input") is hacky and should be removed someday
			if(f.getName().endsWith(".csv")) {
				System.err.println("Generating features for file: " + f.getName());
				String clazz = f.getName().substring(0, f.getName().indexOf(".")).toUpperCase();
				BufferedReader reader = new BufferedReader(new FileReader(f));
				String line = null;
				while((line = reader.readLine()) != null) {
					line = line.trim();
					if(!line.equals("")) {
						String[] split = line.split("\\t+");
						for(int i = 0; i < split.length; i++) {
							split[i] = removeQuotes(split[i]);
						}
						int id = Integer.parseInt(split[0]);
						String datasource = split[1];
						String token1Text = split[2];
						String token2Text = split[3];
						int token1Index = Integer.parseInt(split[4])-1;
						int token2Index = Integer.parseInt(split[5])-1;
						if(token1Index == -2 || token2Index == -2) {
							System.err.println("Skipping");
							continue;
						}
						int sentenceId = Integer.parseInt(split[6]);
						String sentenceText = split[7];
						PossessiveInstance instance = new PossessiveInstance(id, datasource, token1Text, token2Text, token1Index, token2Index, sentenceId, clazz);
						if(allInstances.contains(instance)) {
							System.err.println("Duplicate! : " + instance.token1Text + "\t" + instance.token2Text);
						}
						allInstances.add(instance);
						routine.doSomething(instance);
						
					}
				}
				reader.close();
			}
		}
		featureWriter.close();
		
	}
	
	private void doSomething(PossessiveInstance instance) throws Exception {
		if((!mInvertIds && mIds.contains(instance.id+"\t"+instance.datasource))
				|| (mInvertIds && !mIds.contains(instance.id+"\t"+instance.datasource))
			) {
			List<Parse> parses = mDatasourceToParses.get(instance.datasource);
			
			if(instance.sentenceId >= parses.size()) {
				System.err.println("Uhoh: " + instance.id + "\t" + instance.datasource  + "\t" + instance.token1Text + "\t" + instance.token2Text);
			}
			Parse parse = parses.get(instance.sentenceId);
			List<Token> tokens = parse.getSentence().getTokens();
			
			
			/*System.err.println(instance.id + "\t" + instance.datasource + "\t" + instance.token1Text + "\t" + instance.token2Text + "\t" + instance.token1Index + "\t" + instance.token2Index + "\t");
			for(Token t : tokens) {
				System.err.print(t.getText() + " ");
			}
			System.err.println();*/
			Token pl = tokens.get(instance.token1Index);
			Token pr = tokens.get(instance.token2Index);
			
//			Set<String> features = mFeatGen.generateFeatures(sentence, pl, pr, sentences, instance.sentenceId);
			
			Set<String> features = mFeatGen.generateFeatures(tokens, parse, instance.token2Index);
			
			mFeatureWriter.print(instance.id+"_"+instance.datasource+"\30"+instance.clazz+"\30");
			for(String feat : features) {
				mFeatureWriter.print(feat+"\30");
			}
			mFeatureWriter.println();
		}
	}
	
	private static void readSentences(String inputFile, SentenceReader sreader, List<Parse> parses) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		Parse parse = null;
		while((parse = sreader.readSentence(reader)) != null) {
			parses.add(parse);
		}
		reader.close();
	}
	
	private static Set<String> readIds(String idsFile) throws Exception {
		Set<String> ids = new HashSet<String>();
		BufferedReader reader = new BufferedReader(new FileReader(idsFile));
		String line = null;
		while((line = reader.readLine()) != null) {
			ids.add(line);
		}
		reader.close();
		return ids;
	}
	
	public static String removeQuotes(String s) {
		if(s.startsWith("\"") && s.endsWith("\"")) {
			s = s.substring(1, s.length()-1);
		}
		return s;
	}
	
}