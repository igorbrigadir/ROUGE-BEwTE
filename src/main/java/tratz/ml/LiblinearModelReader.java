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

package tratz.ml;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class LiblinearModelReader {
	
	public static LinearClassificationModel readLiblinearModel(String svmModelFile, String alphabetFile) throws Exception {
		LinearClassificationModel decisionModel = null;
		InputStream is = new BufferedInputStream(new FileInputStream(alphabetFile), 1000000);
		if(alphabetFile.endsWith(".gz")) {
			is = new GZIPInputStream(is);
		}
		ObjectInputStream ois = new ObjectInputStream(is);
		FeatureDictionary featureDictionary = (FeatureDictionary)ois.readObject();
		ClassDictionary classDictionary = (ClassDictionary)ois.readObject();
		ois.close();
		
		System.err.println("Loading: " + svmModelFile);
		InputStream fis = new BufferedInputStream(new FileInputStream(new File(svmModelFile)));
		if(svmModelFile.endsWith(".gz")) {
			fis = new GZIPInputStream(fis);
		}
		
		Pattern whitespaceSplitter = Pattern.compile("\\s+");
		BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
		String line = null;
		String solverType = reader.readLine();// not needed
		String numberOfClasses = reader.readLine();
		String labelList = reader.readLine(); 
		
		String numberOfFeatures = reader.readLine();
		String biasLine = reader.readLine(); // not currently needed
		String wLine = reader.readLine(); // just filler
		
		final int numFeats = Integer.parseInt(whitespaceSplitter.split(numberOfFeatures)[1]);
		/*System.err.println("SolverType: " + solverType);
		System.err.println("NumberOfClasses: " + numberOfClasses);
		System.err.println("LabelList: " + labelList);
		System.err.println("NumberOfFeatures: " + numberOfFeatures);
		System.err.println("BiasLine: " + biasLine);
		System.err.println("wLine: " + wLine);*/
		
		final int numLabels = Integer.parseInt(whitespaceSplitter.split(numberOfClasses)[1]);
		int[] labelOrder = new int[numLabels];
		String[] labels = whitespaceSplitter.split(labelList);
		for(int i = 1; i < labels.length; i++) { // first entry is 'label'
			labelOrder[i-1] = Integer.parseInt(labels[i]);
		}
		ArrayList<float[]> weights = new ArrayList<float[]>(numLabels);
		for(int i = 0; i < numLabels; i++) {
			weights.add(new float[numFeats]);
		}
		
		for(int i = 0; i < numFeats; i++) {
			line = reader.readLine();
			String[] parts = line.split("\\s+");
			if(parts.length != numLabels) {
				if(parts.length == 1 && numLabels == 2) { // this is ok.. just hack in a fix
						float[] vector = weights.get(0);
						float val = (float)Double.parseDouble(parts[0]);
						vector[i] = val;
						float[] vector2 = weights.get(1);
						vector2[i] = -val;
						//if(!parts[j].equals("0")) vector.put(i, Double.parseDouble(parts[j]));
					
				}
				else {
					throw new Exception("Number of entries (" + parts.length + ") != number of expected labels (" +numLabels+")");
				}
			}
			else {
				for(int j = 0; j < numLabels; j++) {
					float[] vector = weights.get(j);
					if(!parts[j].equals("0")) vector[i] = (float)Double.parseDouble(parts[j]);
				}
			}
		}
		reader.close();
		
		fis.close();
		
		return new LinearClassificationModel(weights, labelOrder, classDictionary, featureDictionary);
	}
	
}