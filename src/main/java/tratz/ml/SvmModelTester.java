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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import tratz.cmdline.CommandLineOptions;
import tratz.cmdline.CommandLineOptionsParser;
import tratz.cmdline.ParsedCommandLine;
import tratz.ml.LinearClassificationModel;

/**
 * Used to run SVMs against test instances to calculate results.
 */
public class SvmModelTester {
	
	public final static String OPT_MODEL_DIR = "modeldir",
								OPT_ALPHABET_DIR = "alphabetdir",
								OPT_TEST_DIR = "testdir",
								OPT_OUTPUT_DIR = "outputdir",
								OPT_OUTPUT_FILE = "summaryfile";
	
	public static CommandLineOptions createOptions() {
		CommandLineOptions cmdOpts = new CommandLineOptions();
		cmdOpts.addOption(OPT_MODEL_DIR, "file", "directory containing the model(s)");
		cmdOpts.addOption(OPT_ALPHABET_DIR, "file", "directory containing the alphabet(s)");
		cmdOpts.addOption(OPT_TEST_DIR, "file", "directory containing the test instances");
		cmdOpts.addOption(OPT_OUTPUT_DIR, "file", "directory for the predictions");
		cmdOpts.addOption(OPT_OUTPUT_FILE, "file", "file containing the accuracy summary information");
		return cmdOpts;
	}
	
	
	public static void main(String[] args) throws Exception {
		ParsedCommandLine cmdLine = new CommandLineOptionsParser().parseOptions(createOptions(), args);
		
		File modeldir = new File(cmdLine.getStringValue(OPT_MODEL_DIR));
		File alphabetDir = new File(cmdLine.getStringValue(OPT_ALPHABET_DIR));
		File testdir = new File(cmdLine.getStringValue(OPT_TEST_DIR));
		File outputDir = new File(cmdLine.getStringValue(OPT_OUTPUT_DIR));
		File summaryFile = new File(cmdLine.getStringValue(OPT_OUTPUT_FILE, modeldir + ".txt"));
		
		outputDir.mkdirs();
		runEvaluation(modeldir, alphabetDir, testdir, outputDir, summaryFile);
	}
	
	public static void runEvaluation(File modeldir, File alphabetDir, File testdir, File outputDir, File outputFile) throws Exception {		
		PrintWriter writer = new PrintWriter(outputFile);
		System.err.println(outputFile.getAbsolutePath());
		
		double totalNumCorrect = 0.0;
		double totalNumIncorrect = 0.0;
		
		File[] inFiles = testdir.listFiles();
		Arrays.sort(inFiles);
		for(File inFile : inFiles) {
			PrintWriter outWriter = new PrintWriter(new FileWriter(new File(outputDir,inFile.getName())));
			System.err.println("Applying model to: " + inFile.getAbsolutePath());
			
			double numCorrect = 0;
			double numIncorrect = 0.0;
			String infileName = inFile.getName();
			String modelNameBase = infileName;

			File modelFile = new File(modeldir, modelNameBase+".model");
			
			if(!modelFile.exists()) {
				System.err.println(modelFile.getAbsolutePath());
				System.err.println("SKIPPING " + modelNameBase + " due to lack of model");
				continue;
			}
			File alphabetFile = new File(alphabetDir, modelNameBase);
			
			LinearClassificationModel decModule = LiblinearModelReader.readLiblinearModel(modelFile.getAbsolutePath(), alphabetFile.getAbsolutePath());
			
			InputStream iStream = new BufferedInputStream(new FileInputStream(inFile));
			if(inFile.getName().endsWith(".gz")) {
				iStream = new GZIPInputStream(iStream);
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(iStream));
			String line = null;
			while((line = reader.readLine()) != null) {
				if(!line.trim().equals("")) {
					String[] split = line.split("\30");
					String id = split[0];
					if(split.length <= 1) {
						System.err.println("Ignore instance: bad input line?: " + line);
						continue;
					}
					String trueClass = split[1];
					Set<String> features = new HashSet<String>();
					
					for(int i = 2; i < split.length; i++) {
						features.add(split[i]);
					}
					
					ClassScoreTuple[] classRanks = decModule.getDecision(features);
					String classification = classRanks == null ? null : classRanks[0].clazz;
					
					if( !trueClass.trim().equals("")) {
						outWriter.println(modelNameBase + " " + id + " " + classification + " !! " + trueClass);
						if(trueClass.equals(classification)) {
							numCorrect++;
						}
						else {
							numIncorrect++;
						}
					}
				}
			}
			reader.close();
			
			System.err.println(numCorrect + " " + numIncorrect + " " + numCorrect/(numCorrect+numIncorrect));
			writer.println(modelNameBase + "\t" + (numCorrect + numIncorrect) + "\t" + numCorrect + "\t" + numIncorrect + "\t" + numCorrect/(numCorrect+numIncorrect));
			writer.flush();
			
			totalNumCorrect += numCorrect;
			totalNumIncorrect += numIncorrect;
			outWriter.close();
		}
		
		writer.println("Overall\t" + (totalNumCorrect+totalNumIncorrect) + "\t" + totalNumCorrect + "\t" + totalNumIncorrect + "\t" + totalNumCorrect/(totalNumCorrect+totalNumIncorrect));
		System.err.println(totalNumCorrect + " " + totalNumIncorrect + " " + totalNumCorrect/(totalNumCorrect+totalNumIncorrect));
		writer.close();
	}
	
}