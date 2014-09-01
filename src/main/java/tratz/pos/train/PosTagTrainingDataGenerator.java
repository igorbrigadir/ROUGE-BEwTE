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

package tratz.pos.train;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;


import tratz.cmdline.CommandLineOptions;
import tratz.cmdline.CommandLineOptionsParser;
import tratz.cmdline.ParsedCommandLine;
import tratz.jwni.WordNet;
import tratz.parse.io.SentenceReader;
import tratz.parse.types.Parse;
import tratz.parse.types.Sentence;
import tratz.parse.types.Token;
import tratz.pos.featgen.PosFeatureGenerator;

/**
 * Used to generate training data for the part-of-speech tagger
 *
 */
public class PosTagTrainingDataGenerator {
	
	public final static String OPT_INPUT_FILES = "input",
								OPT_SENTENCE_READER = "sentencereader",
								OPT_OUTPUT_FILE = "output",
								OPT_FEAT_GEN = "featuregenerator",
								OPT_WN_DIR = "wndir";
	
	private static CommandLineOptions createOptions() {
		CommandLineOptions options = new CommandLineOptions();
		options.addOption(OPT_INPUT_FILES, "file(s)", "the input files for training");
		options.addOption(OPT_SENTENCE_READER, "classname", "name of the sentence reader class (must implement " + tratz.parse.io.SentenceReader.class.getName() + ")");
		options.addOption(OPT_OUTPUT_FILE, "file", "the output file");
		options.addOption(OPT_FEAT_GEN, "classname", "name of the feature generation class (must implement " + tratz.pos.featgen.PosFeatureGenerator.class.getName() + ")");
		options.addOption(OPT_WN_DIR, "file", "the dictionary (dict) directory of WordNet");
		return options;
	}
	
	
	public static void main(String[] args) throws Exception {
		ParsedCommandLine cmdLine = new CommandLineOptionsParser().parseOptions(createOptions(), args);
		
		String dataFiles = cmdLine.getStringValue(OPT_INPUT_FILES);
		String sentenceReaderClass = cmdLine.getStringValue(OPT_SENTENCE_READER);
		String outputFile = cmdLine.getStringValue(OPT_OUTPUT_FILE);
		String featureGeneratorClass = cmdLine.getStringValue(OPT_FEAT_GEN);
		String wordNetDir = cmdLine.getStringValue(OPT_WN_DIR);
		
		new WordNet(new File(wordNetDir));
		
		System.err.println("Input files: " + dataFiles);
		
		PosFeatureGenerator featGen = (PosFeatureGenerator)Class.forName(featureGeneratorClass).newInstance();
		
		SentenceReader sentenceReader = (SentenceReader)Class.forName(sentenceReaderClass).newInstance();
		
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(outputFile))));
		for(String dataFile : dataFiles.split(File.pathSeparator)) {
			System.err.println("Reading from: " + dataFile);
			BufferedReader reader = new BufferedReader(new FileReader(dataFile));
			
			int snum = 0;
			Parse parse = null;
			while((parse = sentenceReader.readSentence(reader)) != null) {
				List<Token> tokens = parse.getSentence().getTokens();
				int numTokens = tokens.size();
				for(int i = 0; i < numTokens; i++) {
					Token t = tokens.get(i);
					Set<String> feats = featGen.getFeats(tokens, i);
					
					// FORMAT: ID\30CLASS\30FEAT1\30FEAT2\30...
					writer.print("-1");
					writer.print('\30');
					writer.print(t.getPos());
					writer.print('\30');
					for(String feat : feats) {
						writer.print(feat);
						writer.print('\30');
					}
					writer.println();
				}
				if(snum % 100 == 0) System.err.println(snum);
				snum++;
			}
			reader.close();
		}
		writer.close();
	}
	
	
}