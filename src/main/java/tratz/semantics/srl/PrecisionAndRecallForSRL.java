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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import tratz.cmdline.CommandLineOptions;
import tratz.cmdline.CommandLineOptionsParser;
import tratz.cmdline.ParsedCommandLine;

public class PrecisionAndRecallForSRL {
	
	public final static String OPT_PREDICTIONS_FILES = "predictions",
							   OPT_INSTANCE_FILES = "truthfile";
	
	private static CommandLineOptions createOptions() {
		CommandLineOptions cmdOpts = new CommandLineOptions();
		cmdOpts.addOption(OPT_PREDICTIONS_FILES, "file", "directory containing the predictions of the classifiers");
		cmdOpts.addOption(OPT_INSTANCE_FILES, "file", "file containing the instances being classified");
		return cmdOpts;
	}
	
	public static class IntHolder {
		int val;
		public IntHolder(int val) {
			this.val = val;
		}
	}
	
	public static void main(String[] args) throws Exception {
		ParsedCommandLine cmdLine = new CommandLineOptionsParser().parseOptions(createOptions(), args);
		
		
		File outputFiles = new File(cmdLine.getStringValue(OPT_PREDICTIONS_FILES));
		File truthFile = new File(cmdLine.getStringValue(OPT_INSTANCE_FILES));
		
		Map<String, String> labelToClassTruth = new HashMap<String, String>();
		BufferedReader reader = new BufferedReader(new FileReader(truthFile));
		String line = null;
		while((line = reader.readLine()) != null) {
			String[] split = line.split("\\t+");
			labelToClassTruth.put(split[0], split[1]);
		}
		reader.close();
		
		
		Map<String, String> labelToPredicted = new HashMap<String, String>();
		for(File outputFile : outputFiles.listFiles()) {
			reader = new BufferedReader(new FileReader(outputFile));
			while((line = reader.readLine()) != null) {
				String[] split = line.split("\\s+");
				labelToPredicted.put(split[1],split[2]);
			}
			reader.close();
		}
		
		System.err.println("Label to predicted size: " + labelToPredicted.size());
		System.err.println("Label to class truth size: " + labelToClassTruth.size());
		
		// For holding totals for calculating recall
		Map<String, IntHolder> classToNumOutThereTRUTH = new HashMap<String, IntHolder>();
		// For holding totals for calculating precision
		Map<String, IntHolder> classToNumPredicted = new HashMap<String, IntHolder>();
		// For holding num correct for calculating precision and recall
		Map<String, IntHolder> classToNumberOfCorrectPredicitions = new HashMap<String, IntHolder>();
		
		for(String label : labelToPredicted.keySet()) {
			String prediction = labelToPredicted.get(label);
			String truth = labelToClassTruth.get(label);
			
			IntHolder numPredicted = classToNumPredicted.get(prediction);
			if(numPredicted == null) classToNumPredicted.put(prediction, numPredicted = new IntHolder(0));
			numPredicted.val++;
			
			if(truth == null) {
				System.err.println("Null truth: " + label);
			}
			if(prediction.equals(truth)) {
				IntHolder numCorrect = classToNumberOfCorrectPredicitions.get(prediction);
				if(numCorrect == null) classToNumberOfCorrectPredicitions.put(prediction, numCorrect = new IntHolder(0));
				numCorrect.val++;
			}
			else {
				System.err.println(prediction + " vs " + truth + " for " + label);
			}
		}
		
		for(String label : labelToClassTruth.keySet()) {
			String truth = labelToClassTruth.get(label);
			IntHolder numOutThere = classToNumOutThereTRUTH.get(truth);
			if(numOutThere == null) classToNumOutThereTRUTH.put(truth, numOutThere = new IntHolder(0));
			numOutThere.val++;
		}
		
		Set<String> clazzes = new HashSet<String>(classToNumOutThereTRUTH.keySet());
		clazzes.addAll(classToNumPredicted.keySet());
		for(String clazzType : clazzes) {
			IntHolder numOutThereH = classToNumOutThereTRUTH.get(clazzType);
			IntHolder numCorrectH = classToNumberOfCorrectPredicitions.get(clazzType);
			IntHolder numPredictedH = classToNumPredicted.get(clazzType);
			double numOutThere = numOutThereH == null ? 0 : numOutThereH.val;
			double numCorrect = numCorrectH == null ? 0 : numCorrectH.val;
			double numPredicted = numPredictedH == null ? 0 : numPredictedH.val;
			
			double precision = numCorrect/numPredicted;
			double recall = numCorrect/numOutThere;
			
			System.err.println(clazzType+"\t"+numCorrect + "\t"+numPredicted+"\t"+numOutThere +"\t"+precision+"\t"+recall);
		}
		
	}
	
}