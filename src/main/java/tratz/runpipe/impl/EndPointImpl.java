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

package tratz.runpipe.impl;

import java.io.IOException;
import java.util.Map;

import tratz.runpipe.EndPoint;
import tratz.runpipe.InitializationException;
import tratz.runpipe.ProcessException;
import tratz.runpipe.TextDocument;

/**
 * Implementation of EndPoint class
 */
public class EndPointImpl implements EndPoint {
	
	@Override
	public void initialize(Map<String, String> params) throws InitializationException {
		
	}
	
	@Override
	public void process(TextDocument doc) throws ProcessException {
		
	}
	
	@Override
	public void batchFinished() throws IOException {
		
	}
	
}