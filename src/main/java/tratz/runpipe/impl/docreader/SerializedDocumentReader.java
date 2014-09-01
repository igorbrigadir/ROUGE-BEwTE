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

package tratz.runpipe.impl.docreader;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Map;

import tratz.runpipe.DocumentReadException;
import tratz.runpipe.InitializationException;
import tratz.runpipe.TextDocument;
import tratz.runpipe.TextDocumentReader;

/**
 * Reads in serialized <code>TextDocument</code>s 
 */
public class SerializedDocumentReader implements TextDocumentReader {

	public void initialize(Map<String, String> params) throws InitializationException {
	}

	public void hydrateDocument(InputStream is, TextDocument doc) throws IOException, DocumentReadException {
		try {			
			ObjectInputStream ois = new ObjectInputStream(is);
			doc = (TextDocument)ois.readObject();
			ois.close();
		}
		catch(IOException e) {
			// Re-throw IOExceptions
			throw new IOException(e.getMessage());
		}
		catch(Exception e) {
			// All others rethrow as DocumentRead exceptions
			throw new DocumentReadException(e);
		}
	}

}
