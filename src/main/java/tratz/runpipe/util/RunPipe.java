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

package tratz.runpipe.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tratz.runpipe.Annotator;
import tratz.runpipe.CorpusReader;
import tratz.runpipe.EndPoint;
import tratz.runpipe.Pipe;
import tratz.runpipe.TextDocumentReader;

/**
 * Basic script for running a pipeline
 *
 */
public class RunPipe {
	
	public final static String ANNOTATOR = "-annotator";
	public final static String CORPUS_READER = "-corpusreader";
	public final static String DOC_READER = "-documentreader";
	public final static String END_POINT = "-endpoint";
	
	private final static Set<String> KNOWN_ARG_TYPES = new HashSet<String>(Arrays.asList(new String[]{CORPUS_READER, DOC_READER,ANNOTATOR, END_POINT}));
	
	public static void main(String[] args) throws Exception {
		Pipe pipe = new Pipe();
		
		// Names of pipe components
		String corpusReaderClassName = null;
		String documentReaderClassName = null;
		List<String> annotatorClassNameList = new ArrayList<String>();
		List<String> endPointClassNameList = new ArrayList<String>();
		
		// Parameter holders
		Map<String, String> corpusReaderParams = new HashMap<String,String>();
		Map<String, String> initializerParams = new HashMap<String,String>();		
		List<Map<String, String>> annotatorParams = new ArrayList<Map<String,String>>();
		List<Map<String, String>> endPointParams = new ArrayList<Map<String,String>>();
		
		// Interpret the arguments
		for(int argIndex = 0; argIndex < args.length;) {
			final String arg = args[argIndex];
			Map<String, String> params = null;
			if(arg.equals(ANNOTATOR)) {
				annotatorClassNameList.add(args[++argIndex]);
				annotatorParams.add(params = new HashMap<String, String>());
			}
			else if(arg.equals(CORPUS_READER)) {
				corpusReaderClassName = args[++argIndex];
				params = corpusReaderParams;
			}
			else if(arg.equals(DOC_READER)) {
				documentReaderClassName = args[++argIndex];
				params = initializerParams;
			}
			else if(arg.equals(END_POINT)) {
				endPointClassNameList.add(args[++argIndex]);
				endPointParams.add(params = new HashMap<String, String>());
			}
			else {
				throw new IllegalArgumentException("Unknown arg type: " + args[argIndex]);
			}
			argIndex = populateArgumentMap(++argIndex, args, params);
		}
		
		
		// Build up the pipeline
		CorpusReader corpusReader = (CorpusReader)Class.forName(corpusReaderClassName).newInstance();
		pipe.setCorpusReader(corpusReader, corpusReaderParams);
		if(documentReaderClassName != null) {
			TextDocumentReader docReader = (TextDocumentReader)Class.forName(documentReaderClassName).newInstance();
			pipe.setDocumentReader(docReader, initializerParams);
		}
		
		final int numAnnotators = annotatorClassNameList.size();
		for(int i = 0; i < numAnnotators; i++) {
			Annotator annotator = (Annotator)Class.forName(annotatorClassNameList.get(i)).newInstance();
			pipe.addAnnotator(annotator, annotatorParams.get(i));
		}
		
		final int numEndPoints = endPointClassNameList.size();
		for(int i = 0; i < numEndPoints; i++) {
			EndPoint epu = (EndPoint)Class.forName(endPointClassNameList.get(i)).newInstance();
			pipe.addEndProcessingUnit(epu, endPointParams.get(i));
		}

		// Run the pipeline
		pipe.go();
	}
	
	private static int populateArgumentMap(int currentIndex, String[] args, Map<String, String> paramMap) {
		for(String arg = args[currentIndex]; 
			currentIndex < args.length && !KNOWN_ARG_TYPES.contains(arg = args[currentIndex]);
			currentIndex++) {
			int equalsIndex = arg.indexOf('=');
			if(equalsIndex == -1) {
				throw new IllegalArgumentException("parameter setting: " + arg + " lacks an equals sign ('=')");
			}
			paramMap.put(arg.substring(0, equalsIndex), arg.substring(equalsIndex+1));
		}
		return currentIndex;
	}
	
}