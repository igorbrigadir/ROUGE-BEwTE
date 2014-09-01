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

package tratz.parse.train;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import tratz.cmdline.CommandLineOptions;
import tratz.cmdline.CommandLineOptionsParser;
import tratz.cmdline.ParsedCommandLine;
import tratz.parse.featgen.ParseFeatureGenerator;
import tratz.parse.ml.FinalizedParseModel;
import tratz.parse.ml.TrainablePerceptron;

/**
 * Script for 'finalizing' a parsing model. It is used to create a
 * <code>FinalizedParseModel</code> out of a <code>TrainablePerceptron</code>.
 * A <code>FinalizedParseModel</code> is faster and requires less memory than
 * a <code>TrainablePerceptron</code>.
 * 
 * Running this script can require a lot of memory.
 *
 */
public class FinalizeParseModel {
	
	public final static String OPT_INPUT_FILE = "infile",
							   OPT_OUTPUT_FILE = "outfile",
							   OPT_PERCENTAGE_TO_KEEP = "keeppercent";
	
	public final static double DEFAULT_PERCENTAGE_TO_KEEP = 1.0;
	
	private static CommandLineOptions createOptions() {
		CommandLineOptions cmdOptions = new CommandLineOptions();
		cmdOptions.addOption(OPT_INPUT_FILE, "file", "input model file");
		cmdOptions.addOption(OPT_OUTPUT_FILE, "file", "output model file (gzipped file)");
		cmdOptions.addOption(OPT_PERCENTAGE_TO_KEEP, "double", "percentage of nonzero features to keep (1.0 = 100%)");
		return cmdOptions;
	}
	
	
	public static void main(String[] args) throws Exception {
		ParsedCommandLine cmdLine = new CommandLineOptionsParser().parseOptions(createOptions(), args);
		
		File inputModel = new File(cmdLine.getStringValue(OPT_INPUT_FILE));
		File outputModel = new File(cmdLine.getStringValue(OPT_OUTPUT_FILE));
		double percentageToKeep = cmdLine.getDoubleValue(OPT_PERCENTAGE_TO_KEEP, DEFAULT_PERCENTAGE_TO_KEEP);
		
		assert(percentageToKeep > 0 && percentageToKeep <= 1.0);
		
		System.err.print("Loading old model...");
		ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(inputModel),1000000)));
		TrainablePerceptron sap = (TrainablePerceptron)ois.readObject();
		ParseFeatureGenerator efg = (ParseFeatureGenerator)ois.readObject();
		ois.close();
		
		System.err.println("Done");
		
		System.err.print("Creating finalized model...");
		FinalizedParseModel fsap = sap.createFinal(percentageToKeep);
		System.err.println("Done");
		sap = null;
		System.gc();
		System.err.print("Writing finalized model...");
		ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(outputModel),1000000)));
		oos.writeObject(fsap);
		oos.writeObject(efg);
		oos.close();
		System.err.println("Done");
		
	}
	
}
