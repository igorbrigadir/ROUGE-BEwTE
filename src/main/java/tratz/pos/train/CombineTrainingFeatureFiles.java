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

package tratz.pos.train;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

// Just a little hacky code
public class CombineTrainingFeatureFiles {
	
	public static void main(String[] args) throws Exception {
		String inputFilePrefix = args[0];
		String outputFilePrefix = args[1];
		int numFolds = Integer.parseInt(args[2]);
		for(int i = 1; i <= numFolds; i++) {
			
			String outputFilename = outputFilePrefix + i + ".gz";
			System.err.println("Writing: " + outputFilename);
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(outputFilename),1000000))));
			for(int j = 1; j <= numFolds; j++) {
				if(i != j) {
					String inputFilename = inputFilePrefix + j + ".gz";
					System.err.println("Adding: " + inputFilename);
					BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new BufferedInputStream(new FileInputStream(inputFilename),1000000))));
					String line = null;
					while((line = reader.readLine()) != null) {
						writer.println(line);
					}
					reader.close();
				}
			}
			writer.close();
		}
		
		// The full thing
		
			String outputFilename = outputFilePrefix + "FULL.gz";
			System.err.println("Writing: " + outputFilename);
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(outputFilename))));
			for(int j = 1; j <= numFolds; j++) {
					String inputFilename = inputFilePrefix + j + ".gz";
					System.err.println("Adding: " + inputFilename);
					BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(inputFilename))));
					String line = null;
					while((line = reader.readLine()) != null) {
						writer.println(line);
					}
					reader.close();
			}
			writer.close();
		
		
	}
	
}
