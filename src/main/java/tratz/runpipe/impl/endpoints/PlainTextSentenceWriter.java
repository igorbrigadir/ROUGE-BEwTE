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

package tratz.runpipe.impl.endpoints;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import tratz.runpipe.EndPoint;
import tratz.runpipe.InitializationException;
import tratz.runpipe.ProcessException;
import tratz.runpipe.TextDocument;
import tratz.runpipe.annotations.Sentence;

public class PlainTextSentenceWriter implements EndPoint, Serializable {
	
	private static final long serialVersionUID = 1L;
	private PrintWriter mWriter;
	private String PARAM_OUTPUTFILENAME = "outputFilename";
	
	public void initialize(Map<String, String> args) throws InitializationException {
		try {
			mWriter = new PrintWriter(new FileWriter(args.get(PARAM_OUTPUTFILENAME)));
		}
		catch(IOException ioe) {
			throw new InitializationException(ioe);
		}
	}
	
	public void process(TextDocument doc) throws ProcessException {
		List<Sentence> sList = (List)doc.getAnnotationList(Sentence.class);
		if(sList != null) {
			for(Sentence s : sList) {
				mWriter.println(s.getAnnotText().replace('\n', ' '));
			}
		}
	}
	
	public void batchFinished() {
		mWriter.close();
	}
	
}