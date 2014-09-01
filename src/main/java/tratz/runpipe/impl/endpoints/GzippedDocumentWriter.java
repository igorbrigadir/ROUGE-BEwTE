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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import tratz.runpipe.EndPoint;
import tratz.runpipe.InitializationException;
import tratz.runpipe.ProcessException;
import tratz.runpipe.TextDocument;

public class GzippedDocumentWriter implements EndPoint {
	
	public final static String PARAM_OUTPUT_DIR = "OutputDir";
	
	private File mOutputDir;
	
	public void initialize(Map<String, String> params) throws InitializationException {
		mOutputDir = new File(params.get(PARAM_OUTPUT_DIR));
		mOutputDir.mkdirs();
	}
	
	public void process(TextDocument doc) throws ProcessException {
		String uri = doc.getUri();
		String docFilename = uri.substring(uri.lastIndexOf(File.separatorChar)+1);
		String outputFilename = docFilename + ".gz";
		try {
			File outputFile = new File(mOutputDir, outputFilename);
			ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(outputFile)));
			oos.writeObject(doc);
			oos.close();
		}
		catch(IOException ioe) {
			throw new ProcessException(ioe);
		}
	}
	
	public void batchFinished() {
		// Nothing to do
	}
	
}