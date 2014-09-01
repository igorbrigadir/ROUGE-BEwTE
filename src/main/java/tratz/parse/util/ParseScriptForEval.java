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

package tratz.parse.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import tratz.cmdline.CommandLineOptions;
import tratz.cmdline.CommandLineOptionsParser;
import tratz.cmdline.ParsedCommandLine;
import tratz.featgen.MultiStepFeatureGenerator;
import tratz.featgen.fer.FeatureExtractionRule;
import tratz.jwni.WordNet;
import tratz.ml.ClassScoreTuple;
import tratz.parse.NLParser;
import tratz.parse.featgen.ParseFeatureGenerator;
import tratz.parse.io.SentenceReader;
import tratz.parse.io.SentenceWriter;
import tratz.parse.ml.ParseModel;
import tratz.parse.transform.VchTransformer;
import tratz.parse.types.Arc;
import tratz.parse.types.Parse;
import tratz.parse.types.Sentence;
import tratz.parse.types.Token;
import tratz.semantics.ClassificationBundle;

public class ParseScriptForEval {

	public final static String OPT_SENTENCE_READER_CLASS = "sentencereader",
							   OPT_SENTENCE_WRITER_CLASS = "sentencewriter",
							   OPT_SENTENCE_WRITER_OPTIONS = "writeroptions",
							   OPT_MODEL_FILE = "model",
							   OPT_WORDNET_DIR = "wndir",
							   OPT_INPUT_FILE = "infile";
							   
	private static CommandLineOptions createOptions() {
		CommandLineOptions cmdOptions = new CommandLineOptions();
		
		cmdOptions.addOption(OPT_SENTENCE_READER_CLASS, "classname", "the name of the sentence reading class (must implement " + tratz.parse.io.SentenceReader.class.getName() + ")");
		cmdOptions.addOption(OPT_SENTENCE_WRITER_CLASS, "classname", "the name of the sentence writing class (must implement " + tratz.parse.io.SentenceWriter.class.getName() + ")");
		cmdOptions.addOption(OPT_SENTENCE_WRITER_OPTIONS, "string", "string of colon-separated arguments for the writer");
		cmdOptions.addOption(OPT_MODEL_FILE, "file", "the name of file with the parsing model");
		cmdOptions.addOption(OPT_WORDNET_DIR, "file", "the dictionary (dict) directory of WordNet");
		cmdOptions.addOption(OPT_INPUT_FILE, "file", "the name of the input file");
		
		return cmdOptions;
	}
	
	
	public static long sPsdTime = 0;
	public static long sNnTime = 0;
	
	public static void main(String[] args) throws Exception {
		ParsedCommandLine cmdLine = new CommandLineOptionsParser().parseOptions(createOptions(), args);
		
		String sentenceReaderClass = cmdLine.getStringValue(OPT_SENTENCE_READER_CLASS);
		String sentenceWriterClass = cmdLine.getStringValue(OPT_SENTENCE_WRITER_CLASS);
		String modelFile = cmdLine.getStringValue(OPT_MODEL_FILE);
		String wnDir = cmdLine.getStringValue(OPT_WORDNET_DIR);
		String inputFile = cmdLine.getStringValue(OPT_INPUT_FILE);
		
		String writerArgs = cmdLine.getStringValue(OPT_SENTENCE_WRITER_OPTIONS);
		Map<String, String> writerArgsMap = new HashMap<String, String>();
		String[] writerArgArray = writerArgs.split(":");
		SentenceReader sentenceReader = (SentenceReader)Class.forName(sentenceReaderClass).newInstance();
		SentenceWriter sentenceWriter = (SentenceWriter)Class.forName(sentenceWriterClass).newInstance();
		for(int i = 0; i < writerArgArray.length; i++) {
			String[] parts = writerArgArray[i].split("=");
			writerArgsMap.put(parts[0], parts[1]);
		}
		
		sentenceWriter.initialize(writerArgsMap);
		
		ClassificationBundle psdWrapper = null;
		ClassificationBundle nnWrapper = null;
		ClassificationBundle possWrapper = null;
		ClassificationBundle srlArgsWrapper = null;
		ClassificationBundle srlPredicatesWrapper = null;
		
		ObjectInputStream ois = null;
		/*
		ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream("psdModels.gz")));
		psdWrapper = (PsdWrapper)ois.readObject();
		ois.close();
		// Read in the noun-noun compound disambiguation model
		ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream("nnModel.gz")));
		nnWrapper = (NnWrapper)ois.readObject();
		ois.close();
		
		ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream("possessivesModel.gz")));
		possWrapper = (PossessivesWrapper)ois.readObject();
		ois.close();
		
		ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream("srlWrapper.gz")));
		srlWrapper = (SrlWrapper)ois.readObject();
		ois.close();*/
		
		System.err.print("Loading parse model...");
		long startTime = System.currentTimeMillis();
		System.err.println(modelFile);
		InputStream is = new BufferedInputStream(new FileInputStream(modelFile));
		if(modelFile.endsWith(".gz")) {
			is = new GZIPInputStream(is);
		}
		ois = new ObjectInputStream(is);
		ParseModel model = (ParseModel)ois.readObject();
		ParseFeatureGenerator featGen = (ParseFeatureGenerator)ois.readObject();
		ois.close();
		System.err.println("Model type: " + model.getClass().getName());
		
		
		System.err.println("loaded");
		System.err.println("Model loading took: " + (System.currentTimeMillis()-startTime)/1000.0 + " seconds.");
		
		System.err.print("Loading WordNet...");
		new WordNet(new File(wnDir));
		System.err.println("loaded");
		System.gc();
		System.err.println("Feature generation class: " + featGen.getClass().getName());
		
		NLParser parser = new NLParser(model, featGen);
		
		System.err.println("Start parsing...");
		
		int laCorrect = 0, laWrong = 0, unlCorrect = 0, unlWrong = 0;
		ParseScorer lasCalc = new ParseScorer();
		int exactMatch = 0;
		int totalSentences = 0;
		
		System.gc();
		
		startTime = System.currentTimeMillis();
		String[] files = inputFile.split(File.pathSeparator);
		for(String file : files) {
			BufferedReader reader = new BufferedReader(new FileReader(new File(file)));
			Map<Sentence, Integer> sentenceToIndex = new HashMap<Sentence, Integer>();
			List<Sentence> sentences = new ArrayList<Sentence>();
			int index = 0;
			while(true) {
				Parse parse = sentenceReader.readSentence(reader);
				if(parse == null) break;
					sentences.add(parse.getSentence());
					sentenceToIndex.put(parse.getSentence(), index++);
				}
			reader.close();

			int ti = 0;
			int i = -1;
			for(Sentence sentence : sentences) {
				i++;
				
				if(sentence == null) break;
				long nanoStart = System.nanoTime();
				Parse parse = null;
				try {
					parse = parser.parseSentence(sentence);
				}
				catch(Exception e) {
					e.printStackTrace();
					System.err.println("Error encountered :(");
					for(Token t : sentence.getTokens()) {
						System.err.print(t.getText() + " ");
					}
					System.exit(0);
				}
				
				
				if(i % 100 == 0) {
					System.err.println(i);
				}
				
				long psdStart = System.nanoTime();
				if(psdWrapper != null) {
					for(Arc a : parse.getArcs()) {
						String dep = a.getDependency();
						if(dep.equals(ParseConstants.PREP_MOD_DEP) || dep.equals(ParseConstants.AGENT_DEP) || (dep.equals(ParseConstants.COPULAR_COMPLEMENT_DEP) && a.getChild().getPos().equals("IN"))) {
							Token t = a.getChild();
							ClassScoreTuple[] rankings = psdWrapper.getPredictions(dep, sentence.getTokens(), parse, t.getIndex()-1);
							t.setLexSense(rankings[0].clazz);
						}
					}
				}
				sPsdTime += System.nanoTime()-psdStart;
				long nnTime = System.nanoTime();
				if(nnWrapper != null) {
					int numTokens = sentence.getTokens().size();
					for(int t = 0; t < numTokens; t++) {
						Arc arc = parse.getHeadArcs()[t];
						if(arc != null && arc.getDependency().equals(ParseConstants.NOUN_COMPOUND_DEP)) {
							ClassScoreTuple[] decision = nnWrapper.getPredictions(arc.getDependency(), parse.getSentence().getTokens(), parse, arc.getChild().getIndex()-1);
							arc.setSemanticAnnotation(decision[0].clazz);
						}
					}
				}
				sNnTime += System.nanoTime()-nnTime;
				
				if(possWrapper != null) {
					List<Sentence> sentenceDummyList = new ArrayList<Sentence>();
					sentenceDummyList.add(sentence);
					final int numTokens = sentence.getTokens().size();
					for(int t = 0; t < sentence.getTokens().size(); t++) {
						Arc arc = parse.getHeadArcs()[t];
						if(arc != null && arc.getDependency().equals(ParseConstants.POSSESSOR_DEP)) {
							ClassScoreTuple[] classification = possWrapper.getPredictions(arc.getDependency(), sentence.getTokens(), parse, arc.getHead().getIndex()-1);
							arc.setSemanticAnnotation(classification[0].clazz);
						}
					}
				}
				
				List<Token> tokens = sentence.getTokens();
				int numTokens = tokens.size();
				
				Parse parseCopy = null;
				if(srlArgsWrapper != null) {
					
					List<Arc> newArcs = new ArrayList<Arc>();
					for(int t = 0; t < numTokens; t++) {
						Arc orig = parse.getHeadArcs()[t];
						if(orig != null) {
							Arc newArc = new Arc(orig.getChild(), 
									orig.getHead(), 
									orig.getDependency());
							newArc.setCreationNum(orig.getCreationNum());
							newArcs.add(newArc);
						}
					}
					parseCopy = new Parse(sentence, parse.getRoot(), newArcs);
					
					new VchTransformer().performTransformation(parseCopy);
					for(int t = 0; t < numTokens; t++) {
						Arc arc = parseCopy.getHeadArcs()[t];
						if(arc != null) {
							ClassScoreTuple[] predictions = srlArgsWrapper.getPredictions(arc.getDependency(), tokens, parseCopy, arc.getChild().getIndex()-1);
							if(predictions != null) {
								arc.setSemanticAnnotation(predictions[0].clazz);
							}
						}
						
						Token tok = tokens.get(t);
						ClassScoreTuple[] predictions = srlPredicatesWrapper.getPredictions(arc.getDependency(), tokens, parseCopy, tok.getIndex()-1);
						if(predictions != null) {
							tok.setLexSense(predictions[0].clazz);
						}
					}
				}
				ti += sentence.getTokens().size();
				sentenceWriter.appendSentence(sentence, 
						                      parse, 
						                      parseCopy.getHeadArcs());
			}
		}
		System.err.println("Parsing time: " + (System.currentTimeMillis()-startTime)/1000.0 + " seconds");
		System.err.println("Accuracy: " + (laCorrect)/((double)laCorrect+laWrong) + " " + unlCorrect/((double)unlCorrect+unlWrong));
		System.err.println("Exact match: " + (exactMatch/(double)totalSentences));
		System.err.println("DotTime: " + (NLParser.sDotProductTime)/(1000000.0));
		System.err.println("FeatTime: " + (NLParser.sFeatGenTime)/(1000000.0));
		System.err.println("NNTime: " + (sNnTime)/(10000000.0));
		System.err.println("PsdTime: " + (sPsdTime)/(1000000.0));
		for(FeatureExtractionRule fer : MultiStepFeatureGenerator.ferTimes.keySet()) {
			System.err.println(fer.getClass().getCanonicalName()+"\t"+(MultiStepFeatureGenerator.ferTimes.get(fer))/1000000.0);
		}
		
		sentenceWriter.close();
	}
}