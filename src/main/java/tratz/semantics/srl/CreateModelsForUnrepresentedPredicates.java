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

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import tratz.cmdline.CommandLineOptions;
import tratz.cmdline.CommandLineOptionsParser;
import tratz.cmdline.ParsedCommandLine;

/**
 * Creates models for PropBank predicates not seen in the training data
 *
 */
public class CreateModelsForUnrepresentedPredicates {
	
	public final static String OPT_PROPBANK_FRAMES_DIR = "propbankframes",
								OPT_TRAINING_FEATURES_DIR = "instances";
	
	private static CommandLineOptions createOptions() {
		CommandLineOptions cmdOpts = new CommandLineOptions();
		cmdOpts.addOption(OPT_PROPBANK_FRAMES_DIR, "file", "directory containing the PropBank frame files");
		cmdOpts.addOption(OPT_TRAINING_FEATURES_DIR, "file", "directory containing the training instance files");
		return cmdOpts;
	}
	
	public static void main(String[] args) throws Exception {
		ParsedCommandLine cmdLine = new CommandLineOptionsParser().parseOptions(createOptions(), args);
		
		File propbankFramesDir = new File(cmdLine.getStringValue(OPT_PROPBANK_FRAMES_DIR));
		File trainingFeaturesFilesDir = new File(cmdLine.getStringValue(OPT_TRAINING_FEATURES_DIR));
		
		// Build list of predicates
		Set<String> predicatesSet = new HashSet<String>();
		for(File file : propbankFramesDir.listFiles()) {
			if(file.getName().endsWith(".xml")) {
				predicatesSet.add(file.getName().substring(0, file.getName().indexOf(".")));
			}
		}
		
		// Build list of observed predicates
		Set<String> representedPredicatesSet = new HashSet<String>();
		for(File file : trainingFeaturesFilesDir.listFiles()) {
			representedPredicatesSet.add(file.getName());
		}
		
		// Remove observed predicates
		predicatesSet.removeAll(representedPredicatesSet);
		
		// Create basic models for unobserved predicates
		for(String predicate : predicatesSet) {
			PrintWriter writer = new PrintWriter(new FileWriter(new File(trainingFeaturesFilesDir, predicate)));
			writer.println("0\30"+predicate+".01\30blank");
			writer.close();
		}
	}
	
	
}