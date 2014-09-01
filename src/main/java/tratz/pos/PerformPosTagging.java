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

package tratz.pos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;


import tratz.cmdline.CommandLineOptions;
import tratz.cmdline.CommandLineOptionsParser;
import tratz.cmdline.ParsedCommandLine;
import tratz.jwni.WordNet;
import tratz.ml.ClassScoreTuple;
import tratz.ml.LinearClassificationModel;
import tratz.parse.io.SentenceReader;
import tratz.parse.io.SentenceWriter;
import tratz.parse.types.Parse;
import tratz.parse.types.Sentence;
import tratz.parse.types.Token;
import tratz.pos.featgen.PosFeatureGenerator;

/**
 * A script for performing POS-tagging
 *
 */
public class PerformPosTagging {
	
	private static class IntCounter {
		int val;
		public IntCounter(int x) {
			this.val = x;
		}
	}
	
	public final static String OPT_MODEL_FILE = "model",
							   OPT_INPUT_FILE = "input",
							   OPT_SENTENCE_READER = "sreader",
							   OPT_SENTENCE_WRITER = "swriter",
							   OPT_SENTENCE_WRITER_OPTIONS = "writeropts",
							   OPT_WORDNET_DIR = "wndir",
							   OPT_CONFUSION_FILE = "confusion";
	
	public static CommandLineOptions createOptions() {
		CommandLineOptions opts = new CommandLineOptions();
		opts.addOption(OPT_MODEL_FILE, "file", "file containing the POS-tagging model");
		opts.addOption(OPT_INPUT_FILE, "file", "the input file");
		opts.addOption(OPT_SENTENCE_READER, "classname", "name of the class for reading in the sentences (should implement " + tratz.parse.io.SentenceReader.class.getName() + ")");
		opts.addOption(OPT_SENTENCE_WRITER, "classname", "name of the class for writing out the tagged sentences (should implement " + tratz.parse.io.SentenceWriter.class.getName() + ")");
		opts.addOption(OPT_SENTENCE_WRITER_OPTIONS, "string", "the map of key-value pairs for the sentence writer (separated by semicolons if multiple) (e.g., output=xyz.txt)");
		opts.addOption(OPT_WORDNET_DIR, "file", "the dictionary (dict) directory of WordNet");
		opts.addOption(OPT_CONFUSION_FILE, "file", "name of the file to write the confusion matrix to (optional)");
		return opts;
	}
	
	public static void main(String[] args) throws Exception {
		ParsedCommandLine cmdLine = new CommandLineOptionsParser().parseOptions(createOptions(), args);
		
		String modelFile = cmdLine.getStringValue(OPT_MODEL_FILE);
		String inputFile = cmdLine.getStringValue(OPT_INPUT_FILE);
		String sentenceReaderClass = cmdLine.getStringValue(OPT_SENTENCE_READER);
		String wnDir = cmdLine.getStringValue(OPT_WORDNET_DIR);
		String confusionFile = cmdLine.getStringValue(OPT_CONFUSION_FILE);
		String sentenceWriterClass = cmdLine.getStringValue(OPT_SENTENCE_WRITER);
		String sentenceWriterParams = cmdLine.getStringValue(OPT_SENTENCE_WRITER_OPTIONS);
		
		WordNet wn = new WordNet(new File(wnDir));
		
		SentenceWriter sentenceWriter = (SentenceWriter)Class.forName(sentenceWriterClass).newInstance();
		Map<String, String> params = new HashMap<String, String>();
		if(sentenceWriterParams != null) {
			String[] keyValuePairs = sentenceWriterParams.split(";");
			for(String keyValuePair : keyValuePairs) {
				String[] pair = keyValuePair.split("=");
				params.put(pair[0], pair[1]);
			}
		}
		sentenceWriter.initialize(params);
		
		SentenceReader sentenceReader = (SentenceReader)Class.forName(sentenceReaderClass).newInstance();
		
		System.err.print("Reading POS-tagging model " + new File(modelFile).getAbsolutePath() + " ...");
		long startTime = System.nanoTime();
		ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(modelFile)));
		LinearClassificationModel decisionModule = (LinearClassificationModel)ois.readObject();
		PosFeatureGenerator featureGenerator = (PosFeatureGenerator)ois.readObject();
		ois.close();
		System.err.println("Done (Took: " + (System.nanoTime()-startTime)/Math.pow(10.0, 6) + " seconds");
		
		startTime = System.nanoTime();
		System.err.println("Input file: " + new File(inputFile).getAbsolutePath());
		BufferedReader reader = new BufferedReader(new FileReader(new File(inputFile)));
		
		int numCorrect = 0;
		int numWrong = 0;
		
		System.err.println("Begin POS-tagging...");
		Map<String, Map<String, IntCounter>> confusion = new HashMap<String, Map<String, IntCounter>>();
		int totalSentences = 0;
		Parse parse = null;
		for(int sno = 0; (parse = sentenceReader.readSentence(reader)) != null; sno++) {
			Sentence sentence = parse.getSentence();
			List<Token> tokens = sentence.getTokens();
			
			// Store off the original POS tags to calculate confusion matrix (for cases where
			// the input file contains the gold POS tags)
			String[] originalPos = null;
			if(confusionFile != null) {
				originalPos = new String[tokens.size()+1];
				for(Token t : tokens) {
					originalPos[t.getIndex()] = t.getPos();
				}
			}

			final int numTokens = tokens.size();
			for(int j = 0; j < numTokens; j++) {// numTokens; j++) {
				Set<String> feats = featureGenerator.getFeats(tokens, j);
				ClassScoreTuple[] classRankings = decisionModule.getDecision(feats);
				Token t = tokens.get(j);
				String predictedPos = classRankings[0].clazz; 
				t.setPos(predictedPos);
				
				// Record confusion matrix information
				if(confusionFile != null) {
					Map<String, IntCounter> row = confusion.get(originalPos[t.getIndex()]);
					if(row == null) {
						confusion.put(originalPos[t.getIndex()], row = new HashMap<String, IntCounter>());
					}
				
					IntCounter count = row.get(predictedPos);
					if(count == null) {
						row.put(predictedPos, count = new IntCounter(0));
					}
					count.val++;
					if(predictedPos.equals(originalPos[t.getIndex()])) {
						numCorrect++;
					}
					else {
						numWrong++;
					}
				}
			}
			 
			sentenceWriter.appendSentence(sentence, parse);
			if(sno % 100 == 0) {
				System.err.println(sno);
			}
			totalSentences++;
		}
		
		if(confusionFile != null) {
			File confusionMatrixFile = new File(confusionFile);
			System.err.println("Writing confusion matrix to: " + confusionMatrixFile.getAbsolutePath());
			writeConfusionMatrix(confusionMatrixFile, confusion);
		}
		
		System.err.println("Time: " + (System.nanoTime()-startTime)/Math.pow(10, 9));
		System.err.println("Number correct: " + numCorrect);
		System.err.println("NUmber wrong: " + numWrong);
		System.err.println("Accuracy: " + (numCorrect)/((double)numCorrect+numWrong));
		System.err.println("Tokens/second: " + (numCorrect+numWrong)/((System.nanoTime()-startTime)/Math.pow(10,9)));
		
		reader.close();
		sentenceWriter.close();
	}
	
	private static void writeConfusionMatrix(File outputFile, Map<String, Map<String, IntCounter>> confusionMatrix) throws IOException {
		List<String> keys = new ArrayList<String>(confusionMatrix.keySet());
		final int numKeys = keys.size();
		Collections.sort(keys);
		PrintWriter conf = new PrintWriter(outputFile);
		for(String key : keys) {
			conf.print("\t");
			conf.print(key);
		}
		conf.println();
		for(int i = 0; i < numKeys; i++) {
			conf.print(keys.get(i));
			Map<String, IntCounter> counts = confusionMatrix.get(keys.get(i));
			for(int j = 0; j < numKeys; j++) {
				if(counts == null) {
					conf.print("\t0");
				}
				else {
					IntCounter count = counts.get(keys.get(j));
					conf.print("\t");
					conf.print(count == null ? "0" : count.val);
				}
			}
			conf.println("\t"+keys.get(i));
		}
		for(String key : keys) {
			conf.print("\t");
			conf.print(key);
		}
		conf.println();
		conf.close();
	}
	
}