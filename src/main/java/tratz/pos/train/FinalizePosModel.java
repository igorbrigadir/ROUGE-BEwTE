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

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;

import tratz.cmdline.CommandLineOptions;
import tratz.cmdline.CommandLineOptionsParser;
import tratz.cmdline.ParsedCommandLine;
import tratz.ml.LiblinearModelReader;
import tratz.ml.LinearClassificationModel;
import tratz.pos.featgen.PosFeatureGenerator;


/**
 * Reads in a model created by LIBLINEAR along with an alphabet,
 * trims the size of the model (drops features with 0 weights and can reduce the number of nonzero features),
 * and then saves out a gzipped serialized version of the model to disk.
 * 
 */
public class FinalizePosModel {
	
	public final static String OPT_ALPHABET_FILE = "alphabet",
								OPT_MODEL_FILE = "inputmodel",
								OPT_TRIM_FACTOR = "trimfactor",
								OPT_OUTPUT_FILE = "output",
								OPT_FEATURE_GENERATOR_CLASS = "featgenclass";
	
	public static CommandLineOptions createOptions() {
		CommandLineOptions options = new CommandLineOptions();
		options.addOption(OPT_ALPHABET_FILE, "file", "file containing the feature and class alphabets");
		options.addOption(OPT_MODEL_FILE, "file", "file containing the output of the LIBLINEAR trainer");
		options.addOption(OPT_TRIM_FACTOR, "double", "factor by which to reduce the size of the model (should be no greater than 1.0)");
		options.addOption(OPT_OUTPUT_FILE, "file", "file for the new gzipped, serialized model");
		options.addOption(OPT_FEATURE_GENERATOR_CLASS, "string", "class name of the feature generation class");
		return options;
	}
	
	public static void main(String[] args) throws Exception {
		ParsedCommandLine cmdLine = new CommandLineOptionsParser().parseOptions(createOptions(), args);
		
		String alphabetFile = cmdLine.getStringValue(OPT_ALPHABET_FILE);
		String modelFile = cmdLine.getStringValue(OPT_MODEL_FILE);
		double trimFactor = cmdLine.getDoubleValue(OPT_TRIM_FACTOR);
		String outputFile = cmdLine.getStringValue(OPT_OUTPUT_FILE);
		String featureGenerator = cmdLine.getStringValue(OPT_FEATURE_GENERATOR_CLASS);
		
		// Create a feature generator for saving out to disk
		PosFeatureGenerator featGen = (PosFeatureGenerator)Class.forName(featureGenerator).newInstance();
		
		// Read in the model
		LinearClassificationModel originalModel = LiblinearModelReader.readLiblinearModel(modelFile, alphabetFile);
		
		// Shrink the model as necessary
		LinearClassificationModel shrunkenModel = originalModel.createTrimmedModel((float)trimFactor);
		
		// Write out the model along with an instance of the feature generator
		ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(outputFile)));
		out.writeObject(shrunkenModel);
		out.writeObject(featGen);
		out.close();
	}
	
}