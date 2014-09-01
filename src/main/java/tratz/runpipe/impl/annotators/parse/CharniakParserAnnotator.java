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

package tratz.runpipe.impl.annotators.parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import tratz.jwni.WordNet;
import tratz.runpipe.Annotation;
import tratz.runpipe.Annotator;
import tratz.runpipe.InitializationException;
import tratz.runpipe.ProcessException;
import tratz.runpipe.TextDocument;
import tratz.runpipe.annotations.Sentence;
import tratz.runpipe.util.HeuristicLongSentenceSplittingUtil;

/**
 * Somewhat complicated <code>Annotator</code> for calling the Charniak (2000) parser
 *
 */
public class CharniakParserAnnotator implements Annotator {

	public final static String PROGRAM_DIRECTORY = "ProgramDirectory";
	public final static String DATA_DIRECTORY = "DataDirectory";
	public final static String WORDNET_LOCATION = "WordNetDir";
	public final static String SPLIT_FAILED_SENTENCES = "SplitFailedSentences";
	
	private String mProgramDirectoryParam;
	private String mDataDirectoryParam;
	
	private File mProgramDir;
	private File mDataDir;
	
	private boolean mSplitFailedSentences;
	
	public CharniakParserAnnotator() {
		
	}
	
	public void initialize(Map<String, String> params) throws InitializationException {
		
		try {
			// Don't forget to refactor this at some point
			new WordNet(new File(params.get(WORDNET_LOCATION)).getAbsoluteFile().toURI().toURL().toString());
		}
		catch(Exception e) {
			throw new InitializationException(e);
		}
		
		mSplitFailedSentences = Boolean.parseBoolean(params.get(SPLIT_FAILED_SENTENCES));
		mProgramDirectoryParam = params.get(PROGRAM_DIRECTORY);
		mDataDirectoryParam = params.get(DATA_DIRECTORY);
		mProgramDir = new File(mProgramDirectoryParam);
		mDataDir = new File(mDataDirectoryParam);
		if(!mProgramDir.canRead() || !mProgramDir.isDirectory()
				|| !mDataDir.canRead() || !mProgramDir.isDirectory()) {
			System.err.println(mProgramDir.getAbsolutePath() + "\t" + mProgramDir.canRead() + "\t" + mProgramDir.isDirectory());
			System.err.println(mDataDir.getAbsolutePath() + "\t" + mDataDir.canRead() + "\t" + mDataDir.isDirectory());
			throw new InitializationException();
		}
	}
	
	public void process(TextDocument doc) throws ProcessException {
		List<Sentence> sentences = (List)doc.getAnnotationList(Sentence.class);
		boolean firstPassComplete = false;
		int iteration = 0;
		if(sentences != null) {
			while(!firstPassComplete || (mSplitFailedSentences && sentences != null && sentences.size() > 1)) {
				// Parse sentences, only sentences that failed to parse are returned
				sentences = runParsing(doc.getUri(), sentences, iteration);
				if(mSplitFailedSentences && sentences != null) {
					sentences = HeuristicLongSentenceSplittingUtil.splitSentences(doc, sentences);
				}
				iteration++;
				firstPassComplete = true;
			}
		}
		else {
			System.err.println("No sentences!");
		}
	}
	
	private List<Sentence> runParsing(String docURI, List<Sentence> sentences, int iteration) throws ProcessException {
		File tempInputFile = writeInputFile(sentences);
		if(tempInputFile != null) {
		String results = runProcess(tempInputFile);
		tempInputFile.delete();
		if(results != null) {
			return readResult(docURI, sentences, results, iteration);
		}
		else {
			List<Sentence> sentencesToRedo = new ArrayList<Sentence>();
			for(Sentence sentence : sentences) {
				List<Sentence> singleSentenceList = new ArrayList<Sentence>();
				singleSentenceList.add(sentence);
				tempInputFile = writeInputFile(singleSentenceList);
				results = runProcess(tempInputFile);
				tempInputFile.delete();
				if(results != null) {
					List<Sentence> sList = readResult(docURI, singleSentenceList, results, iteration);
					if(sList != null) {
						sentencesToRedo.addAll(sList);
					}
				}
				// ELSE what???
			}
			return sentencesToRedo;
		}
		}
		else {
			return null;
		}
		
	}
	
	private File writeInputFile(List<Sentence> sentences) throws ProcessException {
		File tempFile = null;
		PrintWriter sentencesWriter = null;
		int numWritten = 0;
		try {
			tempFile = File.createTempFile("sentences", ".txt", new File("."));
			sentencesWriter = new PrintWriter(new FileWriter(tempFile));
			//System.err.println("Input File:");
			for(Annotation sentence : sentences) {
				String sentenceText = sentence.getAnnotText().replaceAll("\\(\\(+","\\(").replaceAll("\\)\\)+","\\)");
				if(!sentenceText.trim().equals("")) {
					numWritten++;
				}
				else {
					sentenceText = "_BLANK_LINE_DUMBY_";
				}
				sentencesWriter.print("<s> " + sentenceText + " </s>\n");
				// silly sentence unlikely to ever occur.. should replace this with something that makes more sense to future readers
				sentencesWriter.print("<s> g090g </s>\n");
				
				//System.err.println("<s> " + sentenceText + " </s>");
			}
			//System.err.println();
			//System.err.println("Total of: " + sentences.size() + " sentences.");
		}
		catch(IOException ioe) {
			throw new ProcessException(ioe);
		}
		finally {
			if(sentencesWriter != null) {
				sentencesWriter.close();
			}
		}
		if(numWritten == 0) {
			tempFile.delete();
			tempFile = null;
		}
		return tempFile;
	}
	
	private String runProcess(File sentencesFile) throws ProcessException {
		Runtime runtime = Runtime.getRuntime();
		String resultsText = null;
		try {
			String part1 = /*"./" +*/ mProgramDir.getPath()+"/parseIt";
			String part2 = /*"./"+*/mDataDir.getPath()+"/";
			String part3 = sentencesFile.getPath();
			String executionString = part1 + " " + part2 + " " + part3;
			System.err.println(executionString);
			Process process = runtime.exec(executionString/*new String[] {part1, part2, part3}*/);//, null, new File("."));
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			while((line = reader.readLine()) != null) {
				builder.append(line).append("\n");
			}
			int result = process.waitFor();
			System.err.println("Process returned: " + result);
			reader.close();
			if(result == 0) {
				resultsText = builder.toString();
			}
		}
		catch(Exception e) {
			throw new ProcessException(e);			
		}

		return resultsText;
	}	
	
	private List<Sentence> readResult(String docURI, List<Sentence> sentences, String results, int iteration) throws ProcessException {
		List<Sentence> potentiallyTooLongSentences = new ArrayList<Sentence>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new StringReader(results));
			for(Sentence sentence : sentences) {
				String line = reader.readLine();
				System.err.println(line);
				if(line == null) {
					System.err.println("Oddball result. Expected junk link.");
					for(int i = 0; i < sentences.size(); i++) {
						System.err.println(sentences.get(i).getAnnotText());
						System.err.println(sentences.get(i).getParseString());
					}
				}
				if(line.equals("(S1 (NP (NNP g090g)))")) {
					// Sentence was skipped by parser
					String sentenceText = sentence.getAnnotText();
					if(sentenceText.split("\\s+").length > 40) {
						// Sentence may have failed due to length
						potentiallyTooLongSentences.add(sentence);
					}
					// Read blank line
					reader.readLine();
				}
				else {
					if(iteration > 1) {
						System.err.println("Iteration " + iteration + ": " + sentence.getAnnotText());
					}
					if(!line.equals("(S1 (NP (NNP _BLANK_LINE_DUMBY_)))")) {
						sentence.setParseString(line);	
					}
					// 	read blank line
					reader.readLine();
					// read dumby sentence
					reader.readLine();
					// read blank line
					reader.readLine();
				}
			}
		}
		catch(IOException ioe) {
			throw new ProcessException(ioe);
		}
		finally {
			if(reader != null) {
				try {
					reader.close();
				}
				catch(Exception e) {
					// Do nothing
				}
			}
		}
		return potentiallyTooLongSentences;
	}	
	
}
