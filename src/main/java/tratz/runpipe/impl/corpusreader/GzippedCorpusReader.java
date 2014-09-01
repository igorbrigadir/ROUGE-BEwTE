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

package tratz.runpipe.impl.corpusreader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.zip.GZIPInputStream;

import tratz.runpipe.DocumentReadException;
import tratz.runpipe.TextDocument;
import tratz.runpipe.impl.TextDocumentImpl;

/**
 * Reads in all files under a given directory, decompressing them. 
 * Optionally (default: true) includes subdirectories.
 * Optionally (default: none) can filter filenames against a regular expression.
 */
public class GzippedCorpusReader extends DirectoryCorpusReader {
	
	@Override
	public TextDocument getNext() throws DocumentReadException, IOException {
		File file = mFiles.get(mCurrentFileIndex++);
		System.err.println("Reading file: " + mCurrentFileIndex + " of " + mFiles.size() + " total: " + file.getName());
		ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(file)));
		TextDocument doc = null;
		try {
			doc = (TextDocument)ois.readObject();
		}
		catch(Exception e) {
			throw new DocumentReadException(e);
		}
		finally {
			ois.close();	
		}
		return doc;
	}
	
	
}