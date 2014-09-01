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

package tratz.semantics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import tratz.cmdline.CommandLineOptions;
import tratz.cmdline.CommandLineOptionsParser;
import tratz.cmdline.ParsedCommandLine;
import tratz.featgen.MultiStepFeatureGenerator;
import tratz.ml.LiblinearModelReader;
import tratz.ml.LinearClassificationModel;


public class BundleModelsWithFeatureGenerator {
	
	
	public final static String OPT_ALPHABET_DIR_PRED = "alphabets",
							   OPT_MODEL_DIR_PRED = "models",
							   OPT_TRIM_FACTOR_PRED = "trimfactor",
							   OPT_WORD_FINDING_RULE_PRED = "wfr",
							   OPT_FER_PRED = "fer",
							   OPT_COMBO_PRED = "comborules",
							   OPT_MODEL_NAME_LOOKUP = "modellookup",
							   
							   OPT_OUTPUT_FILE = "outputfile";
	
	public static CommandLineOptions createOptions() {
		CommandLineOptions opts = new CommandLineOptions();
		
		opts.addOption(OPT_ALPHABET_DIR_PRED, "file", "file/directory containing alphabet file(s)");
		opts.addOption(OPT_MODEL_DIR_PRED, "file", "file/directory containing model file(s)");
		opts.addOption(OPT_TRIM_FACTOR_PRED, "double", "factor to trim model file(s) by");
		opts.addOption(OPT_WORD_FINDING_RULE_PRED, "file", "file containing the word-finding rules for feature generation");
		opts.addOption(OPT_FER_PRED, "file", "file containing the feature extraction rules for feature generation");
		opts.addOption(OPT_COMBO_PRED, "file", "file containing the combination rules for feature generation");
		opts.addOption(OPT_MODEL_NAME_LOOKUP, "string", "name of the class implementing " + ModelNameLookup.class.getCanonicalName() + " for looking up the model name");
		
		opts.addOption(OPT_OUTPUT_FILE, "file", "file to contain the GZipped wrapper file");
		return opts;
	}
	
	public static void main(String[] args) throws Exception {
		ParsedCommandLine cmdLine = new CommandLineOptionsParser().parseOptions(createOptions(), args);
		
		String alphabets = cmdLine.getStringValue(OPT_ALPHABET_DIR_PRED);
		String models = cmdLine.getStringValue(OPT_MODEL_DIR_PRED);
		double trimFactor = cmdLine.getDoubleValue(OPT_TRIM_FACTOR_PRED);
		String wordFindingRules = cmdLine.getStringValue(OPT_WORD_FINDING_RULE_PRED);
		String ferRules = cmdLine.getStringValue(OPT_FER_PRED);
		String comboRules = cmdLine.getStringValue(OPT_COMBO_PRED);
		String modelLookupClassName = cmdLine.getStringValue(OPT_MODEL_NAME_LOOKUP);
		
		String outputFile = cmdLine.getStringValue(OPT_OUTPUT_FILE);
		
		MultiStepFeatureGenerator featureGenerator = new MultiStepFeatureGenerator(wordFindingRules, ferRules, comboRules);
		
		LinearClassificationModel singleModel = null;
		Map<String, LinearClassificationModel> modelMap = null;
		ClassificationBundle bundle = null;
		ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(outputFile)));
		File modelsFile = new File(models);
		// TODO add some checking to make sure that everything is / isn't a directory
		if(modelsFile.isDirectory()) {
			ModelNameLookup modelNameLookup = (ModelNameLookup)Class.forName(modelLookupClassName).newInstance();
			modelMap = createModels(new File(models).listFiles(), alphabets, trimFactor);
			bundle = new ClassificationBundle(featureGenerator, modelMap, modelNameLookup);
		}
		else {
			File alphabetFile = new File(alphabets);
			LinearClassificationModel model = LiblinearModelReader.readLiblinearModel(modelsFile.getAbsolutePath(), alphabetFile.getAbsolutePath());
			singleModel = model.createTrimmedModel((float)trimFactor);
			bundle = new ClassificationBundle(featureGenerator, singleModel);
		}
		
		System.err.println("Writing: " + outputFile);
		out.writeObject(bundle);
		out.close();
	}
	
	private static Map<String, LinearClassificationModel> createModels(File[] files, String alphabetDir, double trimFactor) throws Exception {
		Map<String, LinearClassificationModel> predicateModels = new HashMap<String, LinearClassificationModel>();
		for(File modelFile : files) {
			System.err.println("Processing: " + modelFile.getAbsolutePath());
			String predicateType = modelFile.getName().substring(0, modelFile.getName().indexOf(".model"));
			File alphabetFile = new File(alphabetDir, predicateType);
			LinearClassificationModel model = LiblinearModelReader.readLiblinearModel(modelFile.getAbsolutePath(), alphabetFile.getAbsolutePath());
			LinearClassificationModel trimmedModel = model.createTrimmedModel((float)trimFactor);
			predicateModels.put(predicateType, trimmedModel);
		}
		return predicateModels;
	}
	
}