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
import java.util.Map;

/**
 * Interface defining the methods of a end component in a pipeline
 */
public interface EndPoint {
	
	public void initialize(Map<String, String> params) throws InitializationException;
	public void process(TextDocument document) throws ProcessException;
	
	/**
	 * Called once every TextDocument in the batch has been processed 
	 */
	public void batchFinished() throws IOException;
	
}