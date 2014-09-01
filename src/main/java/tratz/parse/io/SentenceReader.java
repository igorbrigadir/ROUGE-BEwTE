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

package tratz.parse.io;

import java.io.BufferedReader;
import java.io.IOException;

import tratz.parse.types.Parse;

public interface SentenceReader {
	
	/**
	 * 
	 * @param reader
	 * @return A SentenceAndParse object containing a Sentence and, perhaps, its Parse
	 * @throws IOException
	 */
	public Parse readSentence(BufferedReader reader) throws IOException;
}