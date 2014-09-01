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

package tratz.parse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import tratz.cmdline.CommandLineOptions;
import tratz.cmdline.CommandLineOptionsParser;
import tratz.cmdline.ParsedCommandLine;
import tratz.parse.FullSystemWrapper.FullSystemResult;
import tratz.parse.io.SentenceReader;
import tratz.parse.io.SentenceWriter;
import tratz.parse.types.Parse;
import tratz.parse.types.Sentence;
import tratz.parse.types.Token;

/**
 * 
 */
public class ParsingScript {
	
	public final static String OPT_POS_MODEL = "posmodel",
							   OPT_PARSE_MODEL = "parsemodel",
								
								OPT_POSSESSIVES_MODEL = "possmodel",
								OPT_NOUN_COMPOUND_MODEL = "nnmodel",
								OPT_PREPOSITIONS_MODEL = "psdmodel",
								OPT_SRL_ARGS_MODEL = "srlargsmodel",
								OPT_SRL_PREDICATES_MODEL = "srlpredmodel",
								
								OPT_SENTENCE_READER = "sreader",
								OPT_SENTENCE_WRITER = "swriter",
								OPT_SENTENCE_WRITER_OPTIONS = "writeroptions",
								OPT_INPUT = "input",
								
								OPT_WORDNET_DIR = "wndir";
								//OPT_VCH_CONVERT = "convertvch";
	
	public final static Boolean DEFAULT_VCH_CONVERT = Boolean.FALSE;
	public final static String DEFAULT_SENTENCE_READER_CLASS = tratz.parse.io.ConllxSentenceReader.class.getName();
	
	private static CommandLineOptions createOptions() {
		CommandLineOptions cmdOpts = new CommandLineOptions();
		cmdOpts.addOption(OPT_POS_MODEL, "file", "part-of-speech tagging model file");
		cmdOpts.addOption(OPT_PARSE_MODEL, "file", "parser model file");
		
		cmdOpts.addOption(OPT_NOUN_COMPOUND_MODEL, "file", "noun compound interpretation model file");
		cmdOpts.addOption(OPT_PREPOSITIONS_MODEL, "file", "preposition disambiguation models file");
		cmdOpts.addOption(OPT_POSSESSIVES_MODEL, "file", "possessives interpretation model file");
		cmdOpts.addOption(OPT_SRL_PREDICATES_MODEL, "file", "semantic role labeling model file");
		cmdOpts.addOption(OPT_SRL_ARGS_MODEL, "file", "semantic role labeling model file");
		
		cmdOpts.addOption(OPT_SENTENCE_READER, "classname", "sentence reader class name (implements " + tratz.parse.io.SentenceReader.class.getName() + " )");
		cmdOpts.addOption(OPT_SENTENCE_WRITER, "classname", "sentence writer class name (implements " + tratz.parse.io.SentenceWriter.class.getName() + " )");
		cmdOpts.addOption(OPT_SENTENCE_WRITER_OPTIONS, "string", "string of colon-separated arguments for the writer");
		cmdOpts.addOption(OPT_WORDNET_DIR, "file", "WordNet dictionary (dict) directory");
		//cmdOpts.addOption(OPT_VCH_CONVERT, "boolean", "convert vch->{aux,auxpass}, *subj->{*subj,*subjpass}");
		cmdOpts.addOption(OPT_INPUT, "file", "input file/directory (if left out, standard input will be used instead)");
		
		return cmdOpts;
	}
	
	public static void main(String[] args) throws Exception {
		
		ParsedCommandLine cmdLine = new CommandLineOptionsParser().parseOptions(createOptions(), args);
		
		// input filename
		String inputFilename = cmdLine.getStringValue(OPT_INPUT);
		// sentence reader
		String sentenceReaderClass = cmdLine.getStringValue(OPT_SENTENCE_READER, DEFAULT_SENTENCE_READER_CLASS);
		// Sentence writer
		String sentenceWriterClass = cmdLine.getStringValue(OPT_SENTENCE_WRITER);
		SentenceReader sentenceReader = (SentenceReader)Class.forName(sentenceReaderClass).newInstance();
		SentenceWriter sentenceWriter = (SentenceWriter)Class.forName(sentenceWriterClass).newInstance();
		String writerArgs = cmdLine.getStringValue(OPT_SENTENCE_WRITER_OPTIONS);
		Map<String, String> writerArgsMap = new HashMap<String, String>();
		if(writerArgs != null) {
			String[] writerArgArray = writerArgs.split(":");
			for(int i = 0; i < writerArgArray.length; i++) {
				String[] parts = writerArgArray[i].split("=");
				writerArgsMap.put(parts[0], parts[1]);
			}
		}
		sentenceWriter.initialize(writerArgsMap);
		
		// POS-tagging model file
		String posModelFile = cmdLine.getStringValue(OPT_POS_MODEL);
		// 'dict' directory of WordNet
		String wnDir = cmdLine.getStringValue(OPT_WORDNET_DIR);
		// parsing model file
		String parseModelFile = cmdLine.getStringValue(OPT_PARSE_MODEL);
		// possessives model file 
		String possessivesModelFile = cmdLine.getStringValue(OPT_POSSESSIVES_MODEL);
		// noun compound model file
		String nounCompoundModelFile = cmdLine.getStringValue(OPT_NOUN_COMPOUND_MODEL);
		// preposition model file
		String prepositionModelFile = cmdLine.getStringValue(OPT_PREPOSITIONS_MODEL);
		// srl model file
		String srlArgsModelFile = cmdLine.getStringValue(OPT_SRL_ARGS_MODEL);
		String srlPredicatesModelFile = cmdLine.getStringValue(OPT_SRL_PREDICATES_MODEL);
		// Do vch conversion? TODO: might as well change this to do conversion to 'Stanford Basic'
		//boolean doVchConversion = cmdLine.getBooleanValue(OPT_VCH_CONVERT, DEFAULT_VCH_CONVERT);

		FullSystemWrapper fullSystemWrapper = new FullSystemWrapper(prepositionModelFile, nounCompoundModelFile, possessivesModelFile, srlArgsModelFile, srlPredicatesModelFile, posModelFile, parseModelFile, wnDir);
		
		// START THE PARSING
		System.err.println("Beginning sentence processing:");
		long parseStart = System.currentTimeMillis();
		int totalSentences = 0;
		int totalTokens = 0;		
		BufferedReader reader = null;
		if(inputFilename != null) {
			reader = new BufferedReader(new FileReader(inputFilename), 1000000);	
		}
		else {
			reader = new BufferedReader(new InputStreamReader(System.in));
		}
		
		// PROCESS EACH SENTENCE
		Parse emptyOrOldParse = null;
		while((emptyOrOldParse = sentenceReader.readSentence(reader)) != null) {
			Sentence sentence = emptyOrOldParse.getSentence();
			
			//for(Token t : sentence.getTokens()) {
				//System.err.print(t.getText()+" ");
			//}
			//System.err.println();
			
			List<Token> tokens = sentence.getTokens(); 
			FullSystemResult result = fullSystemWrapper.process(sentence, tokens.size() > 0 && tokens.get(0).getPos() == null,
					true, true, true, true, true);
			
			// Output sentence
			sentenceWriter.appendSentence(sentence, 
										  result.getParse(), 
										  result.getSrlParse() == null ? null : result.getSrlParse().getHeadArcs());
			
			// Keep track of some counts
			totalTokens += sentence.getTokens().size();
			totalSentences++;
			if(totalSentences % 100 == 0) {
				System.err.println(totalSentences);
			}
		}			
		reader.close();
		sentenceWriter.close();
		
		// PRINT SUMMARY STATISTICS
		long totalTime = (System.currentTimeMillis()-parseStart);
		System.err.println("Time: " + totalTime/1000.0 +" seconds");
		System.err.println("Total sentences: " + totalSentences);
		System.err.println("Total tokens: " + totalTokens);
	}

}