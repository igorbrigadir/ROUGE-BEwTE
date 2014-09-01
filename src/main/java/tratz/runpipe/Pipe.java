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

package tratz.runpipe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *	Defines a pipeline of components for reading in and processing text
 */
public class Pipe {
	
	private Map<String, String> mCorpusParams;
	private Map<String, String> mDocParams;
	private Map<Annotator, Map<String, String>> mAnnotatorParams = new HashMap<Annotator, Map<String, String>>();
	private Map<EndPoint, Map<String, String>> mEndUnitParams = new HashMap<EndPoint, Map<String, String>>();
	
	private CorpusReader mCorpusReader;
	private TextDocumentReader mDocumentReader;
	private List<Annotator> mAnnotators = new LinkedList<Annotator>();
	private List<EndPoint> mEndUnits = new LinkedList<EndPoint>();
	
	public Pipe() {
		
	}
	
	public void go() throws IOException, DocumentReadException, InitializationException, ProcessException {
		if(mDocumentReader != null) {
			System.err.println("Initializing document reader");
			mDocumentReader.initialize(mDocParams);
			mCorpusReader.setDocumentReader(mDocumentReader);
		}

		System.err.println("Initializing corpus reader");
		mCorpusReader.initialize(mCorpusParams);
		for(Annotator annotator : mAnnotators) {
			System.err.println("Initializing annotator");
			annotator.initialize(mAnnotatorParams.get(annotator));
		}
		
		for(EndPoint unit : mEndUnits) {
			System.err.println("Initializing end unit");
			unit.initialize(mEndUnitParams.get(unit));
		}
		
		while(mCorpusReader.hasNext()) {
			TextDocument doc = mCorpusReader.getNext();
			
			for(Annotator annotator : mAnnotators) {
				annotator.process(doc);
			}
			
			for(EndPoint unit : mEndUnits) {
				unit.process(doc);
			}
		}
		System.err.println("Annotator processing complete");
		for(EndPoint unit : mEndUnits) {
			unit.batchFinished();
		}
		System.err.println("End point processing complete");
	}
	
	public void setCorpusReader(CorpusReader corpusReader, Map<String, String> params) {
		mCorpusReader = corpusReader;
		mCorpusParams = params;
	}
	
	public CorpusReader getCorpusReader() {
		return mCorpusReader;
	}
	
	public void setDocumentReader(TextDocumentReader docReader, Map<String, String> params) {
		mDocumentReader = docReader;
		mDocParams = params;
	}
	
	public TextDocumentReader getDocumentReader() {
		return mDocumentReader;
	}
	
	public List<Annotator> getAnnotators() {
		return new ArrayList<Annotator>(mAnnotators);
	}
	
	public List<EndPoint> getEndProcessingUnits() {
		return new ArrayList<EndPoint>(mEndUnits);
	}
	
	public void addAnnotator(Annotator annotator, Map<String, String> params) {
		mAnnotators.add(annotator);
		mAnnotatorParams.put(annotator, params);
	}
	
	public void removeAnnotator(Annotator annotator) {
		mAnnotators.remove(annotator);
		mAnnotatorParams.put(annotator, null);
	}
	
	public void addEndProcessingUnit(EndPoint unit, Map<String, String> params) {
		mEndUnits.add(unit);
		mEndUnitParams.put(unit, params);
	}
	
	public void removeEndProcessingUnit(EndPoint unit) {
		mEndUnits.remove(unit);
		mEndUnitParams.put(unit, null);
	}
	
}