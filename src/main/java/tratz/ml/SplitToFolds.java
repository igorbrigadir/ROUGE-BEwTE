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

package tratz.ml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Random;

import tratz.cmdline.CommandLineOptions;
import tratz.cmdline.CommandLineOptionsParser;
import tratz.cmdline.ParsedCommandLine;

/**
 * Used to split a collection of instances randomly into two sets of folds files. 
 * The first set of files each contains all folds except one. (These are used for training)
 * The second set of files contains only one. (These are used for testing)
 *
 */
public class SplitToFolds {
	
	public final static String OPT_INPUT_FILE = "infile",
							   OPT_TRAINFOLD_PREFIX = "trainfoldprefix",
							   OPT_TESTFOLD_PREFIX = "testfoldprefix",
							   OPT_NUM_FOLDS = "numfolds",
							   OPT_RANDOM_SEED = "randomseed";
	
	public final static int DEFAULT_NUM_FOLDS = 10;
	
	private static CommandLineOptions createOptions() {
		CommandLineOptions cmdOptions = new CommandLineOptions();
		cmdOptions.addOption(OPT_INPUT_FILE, "file", "file containing instances");
		cmdOptions.addOption(OPT_TRAINFOLD_PREFIX, "string", "prefix of training fold files (e.g., trainfolds/fold");
		cmdOptions.addOption(OPT_TESTFOLD_PREFIX, "string", "prefix of testing fold files (e.g., testfolds/fold");
		cmdOptions.addOption(OPT_NUM_FOLDS, "integer", "number of folds");
		cmdOptions.addOption(OPT_RANDOM_SEED, "long", "seed for random number generator");
		return cmdOptions;
	}
	
	public static void main(String[] args) throws Exception {
		
		ParsedCommandLine cmdLine = new CommandLineOptionsParser().parseOptions(createOptions(), args);
		
		File inFile = new File(cmdLine.getStringValue(OPT_INPUT_FILE));
		String trainingFoldOutprefix = cmdLine.getStringValue(OPT_TRAINFOLD_PREFIX);
		String testFoldOutprefix = cmdLine.getStringValue(OPT_TESTFOLD_PREFIX);
		int numFolds = cmdLine.getIntegerValue(OPT_NUM_FOLDS, DEFAULT_NUM_FOLDS);
		long randomSeed = cmdLine.getIntegerValue(OPT_RANDOM_SEED);
		
		File[] filesToProcess = inFile.isDirectory() ? inFile.listFiles() : new File[]{inFile};
		
		for(File fileToProcess : filesToProcess) {
			int numLines = countLines(fileToProcess);
			double numPerFold = numLines/(double)numFolds;
		
			int[] selections = new int[numLines];
			Random rng = new Random(randomSeed);
			for(int i = 0; i < numLines; i++) {
				selections[i] = rng.nextInt(numFolds);
			}
			System.err.println("Num per fold: " + numPerFold);
			BufferedReader reader = new BufferedReader( new FileReader(fileToProcess));
			String line = null;
			PrintWriter[] trainWriters = new PrintWriter[numFolds];
			PrintWriter[] testWriters = new PrintWriter[numFolds];
			for(int i = 0; i < numFolds; i++) {
				File trainFile = new File(trainingFoldOutprefix+(inFile.isDirectory()?"_"+fileToProcess.getName()+"_":"")+i);
				File testFile = new File(testFoldOutprefix+(inFile.isDirectory()?"_"+fileToProcess.getName()+"_":"")+i);
				trainFile.getAbsoluteFile().getParentFile().mkdirs();
				testFile.getAbsoluteFile().getParentFile().mkdirs();
				trainWriters[i] = new PrintWriter(new FileWriter(trainFile));
				testWriters[i] = new PrintWriter(new FileWriter(testFile));
			}
		
			int x = 0;	
			while((line = reader.readLine()) != null) {
				int selection = selections[x];
				testWriters[selection].println(line);
				for(int j = 0; j < numFolds; j++) {
					if(selection != j) {
						trainWriters[j].println(line);
					}
				}
				x++;
			}
		
			reader.close();
			for(int i = 0; i < numFolds; i++) {
				trainWriters[i].close();
				testWriters[i].close();
			}
		}
		
	}
	
	public static int countLines(File file) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		int count = 0;
		while((reader.readLine()) != null) {
			count++;
		}
		reader.close();
		return count;
	}
	
}