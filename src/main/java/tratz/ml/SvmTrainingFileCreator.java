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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import tratz.cmdline.CommandLineOptions;
import tratz.cmdline.CommandLineOptionsParser;
import tratz.cmdline.ParsedCommandLine;
import tratz.types.IntArrayList;

/**
 * Creates a svmlight/LIBSVM-style training file.
 */
public class SvmTrainingFileCreator {

	public final static String OPT_MAX_NUM_FEATURES = "maxfeatures",
							   OPT_SELECTED_FEATURES = "featurerankings",
							   OPT_INSTANCES = "instances",
							   OPT_SVM_TRAINING = "outfiles",
							   OPT_ALPHABET = "alphabets",
							   OPT_NUM_THREADS = "numthreads",
							   OPT_NULL_OVERRIDE = "nullclass";
	
	public final static int DEFAULT_NUM_THREADS = 1;
	
	private static CommandLineOptions createOptions() {
		CommandLineOptions cmdOpts = new CommandLineOptions();
		cmdOpts.addOption(OPT_MAX_NUM_FEATURES, 	"integer", "maximum number of features to use");
		cmdOpts.addOption(OPT_INSTANCES, 		"file", "name of the file/directory containing the instances with their raw features");
		cmdOpts.addOption(OPT_SELECTED_FEATURES, 	"file", "name of the file/directory containing the ranked features");
		cmdOpts.addOption(OPT_SVM_TRAINING, 		"file", "name of the output file/directory (input file for SVM trainer)");
		cmdOpts.addOption(OPT_ALPHABET, 	"file", "name of the output file/directory for the alphabet (Java map objects for feature and class labels)");
		cmdOpts.addOption(OPT_NUM_THREADS, 	"integer", "number of threads (not relevant in single file case)");
		cmdOpts.addOption(OPT_NULL_OVERRIDE, "string", "name of the class to be treated as 'null'");
		return cmdOpts;
	}
	
	public static void main(String[] args) throws Exception {
		ParsedCommandLine cmdLine = new CommandLineOptionsParser().parseOptions(createOptions(), args);
		
		final int maxNumOfFeatures = cmdLine.getIntegerValue(OPT_MAX_NUM_FEATURES);
		final File featurerankings = new File(cmdLine.getStringValue(OPT_SELECTED_FEATURES));
		final File instances = new File(cmdLine.getStringValue(OPT_INSTANCES));
		final File svmInput = new File(cmdLine.getStringValue(OPT_SVM_TRAINING));
		final File alphabet = new File(cmdLine.getStringValue(OPT_ALPHABET));
		
		final String nullOverrideClass = cmdLine.getStringValue(OPT_NULL_OVERRIDE);
		
		if(!instances.isDirectory()) {
			SvmTrainingFileCreator.createSvmFile(instances, svmInput, alphabet, featurerankings, maxNumOfFeatures, nullOverrideClass);
		}
		else {
			final int numThreads = cmdLine.getIntegerValue(OPT_NUM_THREADS, DEFAULT_NUM_THREADS);
			
			System.err.println("Maximum number of features: " + maxNumOfFeatures);
			System.err.println();
		
			System.err.println("Instances dir: " + instances.getAbsolutePath());
			File[] files = instances.listFiles();
			System.err.println("Num of files: " + files.length);
			Arrays.sort(files);
		
			final Vector<File> fileVector = new Vector<File>(Arrays.asList(files));
		
			for(int i = 0; i < numThreads; i++) {
				new Thread() {
					public void run() {
						File f = null;
						while((f=getNext(fileVector))!=null) {
							File svmInputFile = new File(svmInput, f.getName());
							File selectedFeaturesFile = new File(featurerankings, f.getName());
							File alphabetFile = new File(alphabet, f.getName());
							try {
								createSvmFile(f, svmInputFile, alphabetFile, selectedFeaturesFile, maxNumOfFeatures, nullOverrideClass);
							}
							catch(IOException ioe) {
								System.err.println("Error with: " + f.getName());
								ioe.printStackTrace();
							}	
						}
					}
				}.start();
			}
		}
	}
	
	public static File getNext(Vector<File> files) {
		File result = null;
		try {
			result = files.remove(0);
		}
		catch(ArrayIndexOutOfBoundsException aiobe) {
			// ignore it
		}
		return result;
	}
	
	public static void createSvmFile(File f, File svmInputFile, File alphabetFile, File selectedFeaturesFile, int numFeatures, String nullOverrideClass) throws IOException {
		System.err.print("Reading features...");
		Set<String> featureSet = readFilterSet(selectedFeaturesFile, numFeatures);
		FeatureDictionary dict = new FeatureDictionary();
		for(String feat : featureSet) {
			dict.lookupIndex(feat, true);
		}
		System.err.println("Done");
	
		ClassDictionary labelAlphabet = new ClassDictionary();
	
		System.err.println("Reading from: " + f.getName());
		svmInputFile.getAbsoluteFile().getParentFile().mkdirs();
		PrintWriter dataWriter = new PrintWriter(new FileWriter(svmInputFile));
	
		InputStream is = new BufferedInputStream(new FileInputStream(f));
		if(f.getName().endsWith(".gz")) {
			is = new GZIPInputStream(is);
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	
		int lineNo = 0;
		String line = null;
		while((line = reader.readLine()) != null) {
			lineNo++;
			line = line.trim();
			if(!line.equals("") && !line.startsWith("#")) {
				String[] split = line.split("\30");
				if(split.length <= 1) {
					System.err.println("Skipping training example: bad line?: " + line);
					continue;
				}
				String clazz = split[1];
				if(clazz.equals(nullOverrideClass)) {
					clazz = null;
				}
				int labelIndex = labelAlphabet.lookupIndex(clazz, true);
			
				IntArrayList data = new IntArrayList();
				for(int x = 2 ; x < split.length; x++) {
					String featureKey = split[x];
					if(featureSet.contains(featureKey)) {
						data.add(dict.lookupIndex(featureKey, true));
					}
				}
				data.sort();
				
				dataWriter.print(labelIndex + " ");
				final int numFeats = data.size();
				for(int i = 0; i < numFeats; i++) {
					dataWriter.print((data.get(i)+1)+":1 ");
				}
				dataWriter.println("# ");
				if(lineNo % 100000 == 0) {
					System.err.println(lineNo);
				}
			}
		}
		reader.close();
	
		dataWriter.close();

		alphabetFile.getAbsoluteFile().getParentFile().mkdirs();
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(alphabetFile));
		oos.writeObject(dict);
		oos.writeObject(labelAlphabet);
		oos.close();
	}
	
	
	private static Set<String> readFilterSet(File inputFile, int maxNumOfFeatures) throws IOException {
		Set<String> filterSet = new HashSet<String>();
		BufferedReader freader = new BufferedReader(new FileReader(inputFile));
		String line = null;
		// Skip class distributions
		freader.readLine();
		freader.readLine();
		int numFeats = 0;
		while((line = freader.readLine()) != null) {
			String[] split = line.split("\\t+");
			filterSet.add(split[0]);
			numFeats++;
			if(numFeats == maxNumOfFeatures) {
				break;
			}
		}
		freader.close();
		return filterSet;
	}

}