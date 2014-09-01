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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import tratz.cmdline.CommandLineOptions;
import tratz.cmdline.CommandLineOptionsParser;
import tratz.cmdline.ParsedCommandLine;

/**
 * Ranks the features used by a collection of instances according to a feature selection metric (e.g., Chi Squared)
 *
 */
public class FeatureSelection {
	
	// Command line options
	public final static String OPT_INPUT_DIR = "instances",
							   OPT_OUTPUT_DIR = "output",
							   OPT_MIN_FEATURE_FREQUENCY = "minfreq",
							   OPT_NUMTHREADS = "numthreads",
							   OPT_SELECTIONMETRIC = "metric";
	// Default option values
	public final static int DEFAULT_NUMTHREADS = 1;
	public final static int DEFAULT_MINCOUNT = 0;
	public final static String DEFAULT_SELECTION_METRIC_CLASS = ChiSquared.class.getName();
	
	
	private static CommandLineOptions createOptions() {
		CommandLineOptions cmdOptions = new CommandLineOptions();
		cmdOptions.addOption(OPT_INPUT_DIR, "file", "input file/directory containing instances file");
		cmdOptions.addOption(OPT_OUTPUT_DIR, "file", "output file/directory for the feature ranking files");
		cmdOptions.addOption(OPT_MIN_FEATURE_FREQUENCY, "integer", "minimum feature frequency");
		cmdOptions.addOption(OPT_SELECTIONMETRIC, "classname", "name of the feature selection class");
		cmdOptions.addOption(OPT_NUMTHREADS, "numthreads", "number of threads to use (only applicable for directories)");
		return cmdOptions;
	}
	
	private static class IntHolder {
		int val;
		public IntHolder(int val) {
			this.val = val;
		}
	}
	
	public static interface SelectionMetric {
		void calculateCountsAndScores(File infile, 
				Map<String, Integer> classToIndex,
				int[] classCounts,
				Set<Integer> keepHashes,
				Map<String, Object> featToCounts,
				Map<String, Double> featToScore) throws IOException;
	}
	
	public static void main(String[] args) throws Exception {
		ParsedCommandLine cmdLine = new CommandLineOptionsParser().parseOptions(createOptions(), args);
		
		final File instances = new File(cmdLine.getStringValue(OPT_INPUT_DIR));
		final File output = new File(cmdLine.getStringValue(OPT_OUTPUT_DIR));
		final int minFeatFrequency = cmdLine.getIntegerValue(OPT_MIN_FEATURE_FREQUENCY, DEFAULT_MINCOUNT);
		final String selectionMetricClass = cmdLine.getStringValue(OPT_SELECTIONMETRIC, DEFAULT_SELECTION_METRIC_CLASS);
		final SelectionMetric metric = (SelectionMetric)Class.forName(selectionMetricClass).newInstance();
		
		
		System.err.println("Instances: " + instances.getAbsolutePath());
		if(instances.isFile()) {
			if(output.exists() && !output.isFile()) {
				System.err.println("Error: " + OPT_OUTPUT_DIR + " must specify a directory if " + OPT_INPUT_DIR + " specifies a directory");
				System.exit(-1);
			}
			tratz.ml.FeatureSelection.processFile(instances, output, minFeatFrequency, FeatureSelection.ChiSquared.class.newInstance());
		}
		else {
			int numThreads = cmdLine.getIntegerValue(OPT_NUMTHREADS, DEFAULT_NUMTHREADS);
		
			final Vector<File> inputFilesVector = new Vector<File>(Arrays.asList(instances.listFiles()));
		
			for(int i = 0; i < numThreads; i++) {
				new Thread() {
					public void run() {
						File infile = null;
						while((infile = getNext(inputFilesVector)) != null) {
							File outfile = new File(output, infile.getName());
							try {
								processFile(infile, outfile, minFeatFrequency, metric);
							}
							catch(IOException ioe) {
								System.err.println("Error with file: " + infile.getName());
								ioe.printStackTrace();
							}
							catch(Exception e) {
								System.err.println("Error experienced during: " + infile.getName());
								e.printStackTrace();
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
	
	public static void processFile(File infile, 
								   File outfile, 
								   int minCount,
								   SelectionMetric metric)
			throws IOException {
		System.err.println("Processing: " + infile.getName());
		long startTime = System.currentTimeMillis();
		InputStream inStream = new FileInputStream(infile);
		if(infile.getName().endsWith(".gz")) {
			inStream = new GZIPInputStream(inStream);
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new BufferedInputStream(inStream)));

		String line = null;

		Map<Integer, IntHolder> hashToCount = new HashMap<Integer, IntHolder>();
		Map<String, IntHolder> classToCount = new HashMap<String, IntHolder>();

		System.err.println("Reading and counting...");
		int l = 0;
		while((line = reader.readLine()) != null) {
			String[] split = line.split("\30");
			if(split.length == 1)
				continue;
			IntHolder valHolder = classToCount.get(split[1]);
			if(valHolder == null)
				valHolder = new IntHolder(0);
			valHolder.val++;
			classToCount.put(split[1], valHolder);
			for(int i = 2; i < split.length; i++) {
				int hash = split[i].hashCode();
				IntHolder holder = hashToCount.get(hash);
				if(holder == null) {
					hashToCount.put(hash, holder = new IntHolder(0));
				}
				holder.val++;
			}
			l++;
			if(l % 100000 == 0) {
				System.err.println(l + " " + hashToCount.size());
			}
		}
		reader.close();
		System.err.println("Number of examples: " + l);

		System.err.println("Keeping only hashes of features meeting the frequency threshold...");
		Set<Integer> hashes = hashToCount.keySet();
		final int numHashes = hashes.size();
		Set<Integer> keepHashes = new HashSet<Integer>();
		for(Integer hash : hashes) {
			IntHolder count = hashToCount.get(hash);
			if(count.val >= minCount) {
				keepHashes.add(hash);
			}
		}
		hashToCount.clear();
		hashToCount = null;

		outfile.getAbsoluteFile().getParentFile().mkdirs();
		PrintWriter writer = new PrintWriter(outfile);

		Map<String, Integer> classToIndex = new HashMap<String, Integer>();
		Map<Integer, String> indexToClass = new HashMap<Integer, String>();
		int newIndex = 0;
		for(String s : classToCount.keySet()) {
			classToIndex.put(s, newIndex);
			indexToClass.put(newIndex, s);
			newIndex++;
			writer.print(s);
			writer.print('\t');
		}
		writer.println();
		final int numClasses = classToIndex.size();
		int[] classCounts = new int[numClasses];
		for(int i = 0; i < numClasses; i++) {
			String clazz = indexToClass.get(i);
			IntHolder count = classToCount.get(clazz);
			classCounts[i] = count == null ? 0 : count.val;
			writer.print(count.val);
			writer.print('\t');
		}
		writer.println();

		
		final Map<String, Object> featToCounts = new HashMap<String, Object>();
		final Map<String, Double> featToScore = new HashMap<String, Double>();
		
		metric.calculateCountsAndScores(infile, classToIndex, classCounts, keepHashes, featToCounts, featToScore);

		List<String> featureList = new ArrayList<String>(featToScore.keySet());

		Collections.sort(featureList, new Comparator<String>() {
			public int compare(String s1, String s2) {
				double score1 = featToScore.get(s1);
				double score2 = featToScore.get(s2);
				if(score1 > score2) {
					return -1;
				}
				else if(score1 < score2) {
					return 1;
				}
				else {
					return s1.compareTo(s2);
				}
			}
		});

		System.err.println("Writing counts");
		int z = 0;
		for(String s : featureList) {
			writer.print(s);
			writer.print('\t');
			writer.print(featToScore.get(s));
			Object counts = featToCounts.get(s);
			for(int x = 0; x < numClasses; x++) {
				writer.print('\t');
				writer.print(Array.getInt(counts, x));
			}
			writer.println();
			z++;
			if(z % 10000 == 0) {
				writer.flush();
			}
		}

		writer.close();
		System.err.println((System.currentTimeMillis() - startTime) / 1000
				+ " seconds");
	}
	
	
	public static class ChiSquared implements SelectionMetric {
		public void calculateCountsAndScores(File infile, 
								Map<String, Integer> classToIndex,
								int[] classCounts,
								Set<Integer> keepHashes,
								Map<String, Object> featToCounts,
								Map<String, Double> featToScore) throws IOException {
		InputStream iStream = new BufferedInputStream(new FileInputStream(infile));
		if(infile.getName().endsWith(".gz")) {
			iStream = new GZIPInputStream(iStream);
		}
		String line = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(iStream));
		int l = 0;
		int numClasses = classToIndex.size();
		while((line = reader.readLine()) != null) {
			String[] split = line.split("\30");
			if(split.length == 1)
				continue;
			int classIndex = classToIndex.get(split[1]);
			for(int i = 2; i < split.length; i++) {
				int hash = split[i].hashCode();
				if(keepHashes.contains(hash)) {
					Object counts = featToCounts.get(split[i]);
					if(counts == null) {
						featToCounts.put(split[i],
								counts = new byte[numClasses]);
					}
					// int[] counts =
					if(counts instanceof byte[]) {
						byte[] countsAsBytes = (byte[])counts;
						if(++countsAsBytes[classIndex] == Byte.MAX_VALUE) {
							short[] shortCounts = new short[numClasses];
							featToCounts.put(split[i], shortCounts);
							for(int x = 0; x < numClasses; x++) {
								shortCounts[x] = countsAsBytes[x];
							}
						}
					}
					else if(counts instanceof short[]) {
						short[] countsAsShorts = (short[])counts;
						if(++countsAsShorts[classIndex] == Short.MAX_VALUE) {
							int[] intCounts = new int[numClasses];
							featToCounts.put(split[i], intCounts);
							for(int x = 0; x < numClasses; x++) {
								intCounts[x] = countsAsShorts[x];
							}
						}
					}
					else {
						((int[])counts)[classIndex]++;
					}

				}
			}
			l++;
			if(l % 100000 == 0) {
				System.err.println(l + "\t" + featToCounts.size());
			}
		}
		reader.close();

		double[] classPercentages = new double[numClasses];
		double total = 0;
		for(int cCount : classCounts) {
			total += cCount;
		}
		for(int i = 0; i < numClasses; i++) {
			classPercentages[i] = classCounts[i] / total;
		}

		for(String feat : featToCounts.keySet()) {
			double posTotal = 0;
			double chiSquared = 0;
			Object counts = featToCounts.get(feat);
			for(int i = 0; i < numClasses; i++) {
				posTotal += Array.getInt(counts, i);
			}
			for(int i = 0; i < numClasses; i++) {
				double observed = Array.getInt(counts, i);
				double expected = classPercentages[i] * posTotal;// classCounts[i];
				double diff = observed - expected;
				chiSquared += diff * diff / expected;
			}
			featToScore.put(feat, chiSquared);
		}
		
	}
	}
	
	public static class DummyMetric implements SelectionMetric {
		public void calculateCountsAndScores(File infile, 
								Map<String, Integer> classToIndex,
								int[] classCounts,
								Set<Integer> keepHashes,
								Map<String, Object> featToCounts,
								Map<String, Double> featToScore) throws IOException {
		InputStream iStream = new BufferedInputStream(new FileInputStream(infile));
		if(infile.getName().endsWith(".gz")) {
			iStream = new GZIPInputStream(iStream);
		}
		String line = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(iStream));
		int l = 0;
		int numClasses = classToIndex.size();
		while((line = reader.readLine()) != null) {
			String[] split = line.split("\30");
			if(split.length == 1)
				continue;
			for(int i = 2; i < split.length; i++) {
				int hash = split[i].hashCode();
				
				if(keepHashes.contains(hash)) {
					if(!featToScore.containsKey(split[i])) {
						featToScore.put(split[i], 1.0);
						featToCounts.put(split[i], new int[numClasses]);
					}
				}
			}
			l++;
			if(l % 100000 == 0) {
				System.err.println(l + "\t" + featToCounts.size());
			}
		}
		reader.close();

		for(String feat : featToCounts.keySet()) {
			featToScore.put(feat, 1.0);
		}
		
	}
	}
}