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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import tratz.cmdline.CommandLineOptions;
import tratz.cmdline.CommandLineOptionsParser;
import tratz.cmdline.ParsedCommandLine;
import tratz.parse.NLParser;
import tratz.parse.featgen.ParseFeatureGenerator;
import tratz.parse.io.SentenceReader;
import tratz.parse.ml.ParseModel;
import tratz.parse.ml.TrainablePerceptron;
import tratz.parse.train.PerSentenceTrainer.TrainingResult;
import tratz.parse.types.Arc;
import tratz.parse.types.Parse;
import tratz.parse.types.Token;
import tratz.parse.util.ParseScorer;

/**
 * OnlineParserTrainer
 * For training the parser using an online training strategy (e.g., average perceptron)
 */
public class OnlineParserTrainer {

	public final static String DEFAULT_PARSE_MODEL_CLASSNAME = TrainablePerceptron.class.getName();
	public final static String DEFAULT_FEATURE_GENERATOR_CLASSNAME = tratz.parse.featgen.DefaultEnParseFeatureGenerator.class.getName();
	public final static String DEFAULT_SENTENCE_READER = tratz.parse.io.ConllxSentenceReader.class.getName();
	public final static String DEFAULT_TRAINING_CLASSNAME = tratz.parse.train.StandardPerSentenceTrainer.class.getName();
	public final static int DEFAULT_NUMBER_OF_ITERATIONS = 50;
	public final static String DEFAULT_OUTPUT_PREFIX = "parseModel";
	public final static String DEFAULT_SAVE_ITERATIONS = "new_best";
	public final static int DEFAULT_FIRST_SAVE_ITERATION = 10;
	
	public final static String OPT_INFILES = "infiles",
							   OPT_DEVFILES = "devfile",
							   OPT_OUTFILES = "out",
							   OPT_READER = "sentencereader",
							   OPT_ITERATIONS = "iterations",
							   OPT_FEATGEN = "featgen",
							   OPT_TRAININGCLASS = "trainingclass",
							   OPT_MODELCLASS = "modelclass",
							   OPT_BASEMODEL = "basemodel",
							   OPT_LOG = "log",
							   OPT_FIRST_SAVE = "firstsaveiteration",
							   OPT_ITERATIONS_TO_SAVE = "saveiterations";
	
	private static CommandLineOptions createCommandLineOptions() {
		CommandLineOptions cmdOpts = new CommandLineOptions();
		cmdOpts.addOption(OPT_INFILES,		"file(s)",		"training file(s) (required)");
		cmdOpts.addOption(OPT_DEVFILES,	"file", 		"development file for measuring accuracy (optional)");
		cmdOpts.addOption(OPT_OUTFILES, 	"file", 		"base output file name (default: "+DEFAULT_OUTPUT_PREFIX+")");
		
		cmdOpts.addOption(OPT_READER, 		"class_name",	"class name of sentence reader (implements "+tratz.parse.io.SentenceReader.class.getName() + ") (default:"+DEFAULT_SENTENCE_READER+")");
		
		cmdOpts.addOption(OPT_ITERATIONS, 	"integer", 		"number of iterations (default: "+DEFAULT_NUMBER_OF_ITERATIONS+")");
		
		cmdOpts.addOption(OPT_FEATGEN, 		"class_name",	"class name of feature generator (implements " + tratz.parse.featgen.ParseFeatureGenerator.class.getName() + ") (required)");
		cmdOpts.addOption(OPT_TRAININGCLASS,		"class_name", 	"file name iterative training class (default:"+DEFAULT_TRAINING_CLASSNAME + " )");
		cmdOpts.addOption(OPT_MODELCLASS, 		"class_name", 	"class name of model class (implements " + tratz.parse.ml.ParseModel.class.getName() + ") (default: "+tratz.parse.ml.TrainablePerceptron.class.getName()+")");
		
		cmdOpts.addOption(OPT_BASEMODEL, 		"file", 		"file name of model to start with (optional) (won't always work)");
		
		cmdOpts.addOption(OPT_LOG, 					"file",			"log file name (optional)");
		cmdOpts.addOption(OPT_FIRST_SAVE,	"integer", 		"first iteration to save after (default:"+DEFAULT_FIRST_SAVE_ITERATION+")");
		cmdOpts.addOption(OPT_ITERATIONS_TO_SAVE, 		"all,new_best",	"iterations to save model (last iteration always saved) (default:"+DEFAULT_SAVE_ITERATIONS+")");
		return cmdOpts;
	}
	
	public static void main(String[] args) throws Exception {
		ParsedCommandLine commandLine = new CommandLineOptionsParser().parseOptions(createCommandLineOptions(), args);	
		
		//cmdOpts.addOption("constrain", true, "part-of-speech constraints for link types");
		
		// REQUIRED
		String trainingFiles = commandLine.getStringValue(OPT_INFILES);
		String develFile = commandLine.getStringValue(OPT_DEVFILES);
		String outputModels = commandLine.getStringValue(OPT_OUTFILES);
		
		// OPTIONAL, WITH DEFAULTS
		String sentenceReaderClass = commandLine.getStringValue(OPT_READER, DEFAULT_SENTENCE_READER);
		String featGenClass = commandLine.getStringValue(OPT_FEATGEN, DEFAULT_FEATURE_GENERATOR_CLASSNAME);
		String modelType = commandLine.getStringValue(OPT_MODELCLASS, DEFAULT_PARSE_MODEL_CLASSNAME);
		String perSentenceTrainingClass =  commandLine.getStringValue(OPT_TRAININGCLASS, DEFAULT_TRAINING_CLASSNAME);
		int numIterations = commandLine.getIntegerValue(OPT_ITERATIONS, DEFAULT_NUMBER_OF_ITERATIONS);
		int firstSaveIteration = commandLine.getIntegerValue(OPT_FIRST_SAVE, DEFAULT_FIRST_SAVE_ITERATION);
		
		// OPTIONAL, NO DEFAULTS
		String baseModel = commandLine.getStringValue(OPT_BASEMODEL);
		String logFile = commandLine.getStringValue(OPT_LOG);
		
		
		System.err.println("Training Files: " + trainingFiles);
		SentenceReader sentenceReader = (SentenceReader)Class.forName(sentenceReaderClass).newInstance();
		PerSentenceTrainer trainer = (PerSentenceTrainer)Class.forName(perSentenceTrainingClass).newInstance();
		
		PrintWriter log = null;
		if(logFile != null) {
			log = new PrintWriter(new FileWriter(logFile));
		}
		
		// Read in the sentences and create the list of possible parse actions
		System.err.println("Reading: " + trainingFiles);
		List<Parse> parses = new ArrayList<Parse>();
		
		readSentences(sentenceReader, trainingFiles, parses);
		
		Set<String> parseActionList = createActionsList(parses);
		sentenceReader = null;
		
		ParseModel parseModel = (ParseModel)Class.forName(modelType).getConstructor(List.class).newInstance(new ArrayList<String>(parseActionList));
		ParseFeatureGenerator featGen = (ParseFeatureGenerator)Class.forName(featGenClass).newInstance();
		
		if(baseModel != null) {
			InputStream is = new BufferedInputStream(new FileInputStream(baseModel));
			if(baseModel.endsWith(".gz")) is = new GZIPInputStream(is);
			ObjectInputStream ois = new ObjectInputStream(is);
			parseModel = (ParseModel)ois.readObject();
			featGen = (ParseFeatureGenerator)ois.readObject();
			ois.close();
		}
		
		double bestUnlabeledAcc = 0;
		double bestLabeledAcc = 0;
		
		Random rng = new Random(1);
		for(int iter = 0; iter < numIterations; iter++) {
			int i = 0;
			int totalInvalids = 0;
			
			// Shuffle the sentences randomly (important)
			Collections.shuffle(parses, rng);
			for(int sentenceIndex = 0; sentenceIndex < parses.size(); sentenceIndex++) {
				Parse parse = parses.get(sentenceIndex);
				
				Arc[] tokenToHead = parse.getHeadArcs();
				List[] finalTokenToChildren = parse.getDependentArcLists();
				
				int[] projectiveIndices = new int[parse.getSentence().getTokens().size()+1];
				ProjectivityHandler.traverse(parse.getRoot(), finalTokenToChildren, projectiveIndices);
				Token[] tokenToSubcomponent = ProjectivityHandler.findSubcomponents(parse.getSentence().getTokens(), finalTokenToChildren);
				
				TrainingResult result = trainer.train(parse.getSentence().getTokens(), finalTokenToChildren, tokenToHead, parseModel, featGen, tokenToSubcomponent, projectiveIndices);
				if(result.fatalError || result.maxUpdatesExceeded) {
					System.err.println("Removing sentence: " + result.fatalError + " " + result.maxUpdatesExceeded);
					parses.remove(sentenceIndex);
					sentenceIndex--;
				}
				
				totalInvalids += Math.abs(result.numUpdatesMade);
				if(i%100 == 0) {
					if(i%1000 == 0) {
						System.gc();
					}
					System.err.println("s: " + i);
				}
				i++;
			}
			
			double[] scores = parseFiles((SentenceReader)Class.forName(sentenceReaderClass).newInstance(), parseModel, featGen, develFile);
			
			System.err.println("Current scores: " + scores[0] + " " + scores[1]);
			if(log != null) {
				log.println(iter + "\t" + totalInvalids + "\t" + scores[0] + "\t" + scores[1]);
				log.flush();
			}
			
			if(iter >= firstSaveIteration && (scores[0] >= bestUnlabeledAcc || scores[1] >= bestLabeledAcc)) {
				if(scores[0] > bestUnlabeledAcc) {
					bestUnlabeledAcc = scores[0];
				}
				if (scores[1] > bestLabeledAcc) {
					bestLabeledAcc = scores[1];
				}
				System.err.println("Writing model");
				saveModel(outputModels+iter+".gz", parseModel, featGen);
			}
		}
		
		// Save final model
		saveModel(outputModels+".final.gz", parseModel, featGen);
		
		if(log != null) log.close();
	}
	
	private static void saveModel(String filename, ParseModel parseModel, ParseFeatureGenerator featGen) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(filename), 1000000)));
		oos.writeObject(parseModel);
		oos.writeObject(featGen);
		oos.close();
	}
	
	private static void readSentences(SentenceReader sreader, 
												String inputFiles,
												List<Parse> parses) throws IOException {
		for(String file : inputFiles.split(File.pathSeparator)) {
			BufferedReader reader = new BufferedReader(new FileReader(new File(file)));
			// Read each sentence
			Parse parse = null;
			while(((parse = sreader.readSentence(reader)) != null)) {
				parses.add(parse);
			}
			reader.close();
		}
	}
	
	private static Set<String> createActionsList(List<Parse> parses) throws IOException {
		Set<String> actionsList = new HashSet<String>();
		for(Parse parse : parses) {
			for(Arc arc : parse.getArcs()) {
				String dep = arc.getDependency();
				actionsList.add(dep+"l");
				actionsList.add(dep+"r");
			}
		}
		actionsList.add("SWAPLEFT");
		actionsList.add("SWAPRIGHT");
		return actionsList;
	}
	
	private static double[] parseFiles(SentenceReader sentenceReader, ParseModel model, ParseFeatureGenerator featGen, String inputFiles) throws Exception {
		NLParser parser = new NLParser(model, featGen);
		System.err.println("Parsing development data...");
		int laCorrect = 0, laWrong = 0, unlCorrect = 0, unlWrong = 0;
		int exactMatch = 0;
		int totalSentences = 0;
		
		for(String inputFile : inputFiles.split(File.pathSeparator)) {
			BufferedReader reader = new BufferedReader(new FileReader(new File(inputFile)));
			
			Parse goldParse = null;
			int sentenceNumber = 0;
			while((goldParse = sentenceReader.readSentence(reader)) != null) {
				if(goldParse==null)break;
				Parse predictedParse = parser.parseSentence(goldParse.getSentence());
				
				ParseScorer.LASresults results = ParseScorer.calc(goldParse.getSentence().getTokens().size(), goldParse, predictedParse, ParseScorer.PUNCTUATION_REGEX);
				
				laCorrect += results.numCorrectArcsLabeled;
				laWrong += results.numIncorrectArcsLabeled;
				unlCorrect += results.numCorrectArcsUnlabeled;
				unlWrong += results.numIncorrectArcsUnlabeled;
				double totalArcs = results.numCorrectArcsLabeled+results.numIncorrectArcsLabeled;
				
				if(sentenceNumber % 100 == 0) {
					System.err.println(sentenceNumber);
				}
				sentenceNumber++;
				
				if(results.numCorrectArcsLabeled/totalArcs >= .9999) {
					exactMatch++;
				}
				totalSentences++;
			}
		}
		return new double[]{unlCorrect/((double)unlCorrect+unlWrong), laCorrect/((double)laCorrect+laWrong)};
	}
	
}