package bewte;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import foal.list.DoubleArrayList;
import foal.map.IntIntHashMap;
import foal.map.IntObjectHashMap;
import bewte.BE.BEPart;
import bewte.endanalysis.AbstractEndAnalyzer;
import bewte.endanalysis.EndAnalyzer;
import bewte.io.BESetReader;
import bewte.io.TransformInfoReader;
import bewte.names.NameExtractor;
import bewte.scoring.TallyFunction;
import bewte.transforms.BETransform;
import bewte.util.BEUtils;

/**
 * The BEwT-E main evaluation program
 *  
 * Runs on the extracted BEs (or their transformed versions) to calculate BEwT-E scores
 */
public class BEwT_E {
	
	private class CmdArgs {
		public final static int BE_DIRS_INDEX = 0;
		public final static int SYSTEM_LEVEL_OUTPUT_FILE_INDEX = 1;
		public final static int SUMMARY_LEVEL_OUTPUT_FILE_INDEX = 2;
		public final static int TALLY_FUNCTION_INDEX = 3;
		public final static int INCLUDE_DUPLICATE_BES_INDEX = 4;
		public final static int BE_EXTRACTION_RULE_LIST_INDEX = 5;
		public final static int TRANSFORM_LIST_INDEX = 6;
		public final static int TRANSFORM_COEFFS_INDEX = 7;
		public final static int REFERENCE_SYSTEMS_PATTERN_INDEX = 8;
		public final static int END_PROCESSOR_CONFIG_INDEX = 9;
		public final static int TOPIC_NAME_GENERATOR_INDEX = 10;
		public final static int FILE_PATTERN_INDEX = 11;
		
		String[] beDirs;
		File systemLevelOutputFile;
		File summaryLevelOutputFile;
		TallyFunction tallyFunction;
		boolean includeDuplicateBEs;
		File beExtractionRuleList;
		File transformList;
		File transformCoeffs;
		String refSystemsPattern;
		File endProcessorConfig;
		NameExtractor topicNameGenerator;
		String filePattern;
		
		public CmdArgs init(String[] args) throws Exception {
			// Directory containing BE files
			beDirs = args[BE_DIRS_INDEX].split(File.pathSeparator);
			// File to write system level output to
			systemLevelOutputFile = new File(args[SYSTEM_LEVEL_OUTPUT_FILE_INDEX]);
			// File to write summary level output to
			summaryLevelOutputFile = new File(args[SUMMARY_LEVEL_OUTPUT_FILE_INDEX]);
			// Tally function for weighting BEs based upon occurrence in references
			tallyFunction = (TallyFunction)Class.forName(args[TALLY_FUNCTION_INDEX]).newInstance();
			// Determines if duplicate BEs are included in calculations
			includeDuplicateBEs = Boolean.parseBoolean(args[INCLUDE_DUPLICATE_BES_INDEX]);
			// List of BE extraction rules
			beExtractionRuleList = new File(args[BE_EXTRACTION_RULE_LIST_INDEX]);
			// List of BE transformations
			transformList = new File(args[TRANSFORM_LIST_INDEX]);
			// List of BE transformation coefficients
			transformCoeffs = new File(args[TRANSFORM_COEFFS_INDEX]);
			// Regex for matching reference system names
			refSystemsPattern = args[REFERENCE_SYSTEMS_PATTERN_INDEX];
			// File of EndAnalyzer information
			endProcessorConfig = new File(args[END_PROCESSOR_CONFIG_INDEX]);
			// Class name of class that extracts topic and system names from filenames
			topicNameGenerator = (NameExtractor)Class.forName(args[TOPIC_NAME_GENERATOR_INDEX]).newInstance();
			// Regex pattern for limiting which BE files are included
			filePattern = args[FILE_PATTERN_INDEX];
			
			// for convenience, this method returns a reference to the object
			return this;
		}
	}
	
	public static void main(String[] args) throws Exception {
		new BEwT_E().evaluate(args);
	}
	
	public void evaluate(String[] args) throws Exception {
		// Read the params
		CmdArgs params = new CmdArgs().init(args);
		
		// Create the end analysis object (not needed typically)
		EndAnalyzer endAnalyzerObject = AbstractEndAnalyzer.createEnder(params.endProcessorConfig);
		
		// Read BE extraction rule to rule weights
		IntIntHashMap ruleToWeightIndex = readRuleToWeightIndex(params.beExtractionRuleList);
		final int numRules = ruleToWeightIndex.size();
		
		List<String> topics = new ArrayList<String>(); // Why a list and not a set???
		List<File> allFiles = new ArrayList<File>();
		for(String dir : params.beDirs) {
			File fileDir = new File(dir);
			// Collect all BE files under this directory and its subdirectories
			List<File> files = new ArrayList<File>(); 
			BEUtils.getFiles(fileDir, files, params.filePattern);
			allFiles.addAll(files);
			
			// Read list of topics
			List<String> topicsForDir = BEUtils.getTopicList(files, params.topicNameGenerator);
			for(String topicName : topicsForDir) {
				String topic = /*prefix +*/ topicName;
				topics.add(topic);
			}
		}
		Collections.sort(topics);
		
		// Read transformation list and transformation weights
		List transforms = new ArrayList();
		TransformInfoReader.readTransformPipeline(params.transformList, transforms, false);
		IntObjectHashMap<String> bitToTransformName = new IntObjectHashMap<String>();
		Map<String, Integer> transformNameToBitIndex = new HashMap<String, Integer>();
		BEXpander.createBitMaps(transforms, bitToTransformName, transformNameToBitIndex);
		IntIntHashMap bitIndexToWeightIndex = new IntIntHashMap();
		double[] transformWeights = TransformInfoReader.readTransformCoeffs(params.transformCoeffs, bitIndexToWeightIndex, transformNameToBitIndex);
	
		// BE extraction rule weights (currently all 1.0)
		final double[] ruleWeights = new double[numRules];
		Arrays.fill(ruleWeights, 1.0);
		/*BufferedReader reader = new BufferedReader(new FileReader("conf/rules/weights06.txt"));
		String rl = null;
		int ri = 0;
		while((rl = reader.readLine()) != null) {
			ruleWeights[ri++] = Double.parseDouble(rl);
		}
		reader.close();*/
		
		Map<String, List<File>> topicToFiles = new HashMap<String, List<File>>();
		Map<String, List<File>> topicToGoldenFiles = new HashMap<String, List<File>>();
		
		Map<File, List<BE>> fileToBEs = new HashMap<File, List<BE>>();
		Map<File, List<Map<BE, List<BitSet>>>> fileToBEXs = new HashMap<File, List<Map<BE, List<BitSet>>>>();
		
		Map<File, String> fileToSystem = new HashMap<File, String>();
		
		Map<String, BEPart> bePartCanonicalMap = new HashMap<String, BEPart>();
		for(String topic : topics) {
			System.out.println("Loading topic data for topic: " + topic + " ...");
			List<File> filesForTopic = BEUtils.getFilesForTopic(topic, allFiles, params.topicNameGenerator);
			List<File> referenceFiles = BEUtils.getFiles(filesForTopic, ".*" + params.refSystemsPattern);
			System.out.println("... finished. Topic has " + filesForTopic.size() + " files including " + referenceFiles.size() + " references");
			topicToFiles.put(topic, filesForTopic);
			topicToGoldenFiles.put(topic, referenceFiles);
			
			for(File file : filesForTopic) {
				List<BE> beList = new ArrayList<BE>();
				List<Map<BE, List<BitSet>>> beToTransformedBes = new ArrayList<Map<BE, List<BitSet>>>();
				BESetReader.getBeList(beList, file, new HashSet<String>(), ruleToWeightIndex, transformNameToBitIndex, beToTransformedBes, bePartCanonicalMap, params.includeDuplicateBEs);
				fileToBEs.put(file, beList);
				fileToBEXs.put(file, beToTransformedBes);
				fileToSystem.put(file, params.topicNameGenerator.getSystemName(file));
			}
		}
		bePartCanonicalMap.clear();
		System.gc();
		
		
		boolean loopAgain = false;
		int iteration = 1;
		do {
			System.out.println("Iteration: " + iteration);
			
			loopAgain = false;
			long iterationStart = System.nanoTime();
			
			Map<String, Double> systemToScore = new HashMap<String, Double>();
			Map<String, Map<String, Double>> topicToSystemToScore = new HashMap<String, Map<String,Double>>();
			// Main evaluation
			mainEvaluation(systemToScore, topicToSystemToScore, topics, allFiles, topicToFiles, topicToGoldenFiles, fileToSystem, fileToBEs, ruleWeights, params.tallyFunction, params.refSystemsPattern, bitIndexToWeightIndex, transformWeights, fileToBEXs);
			
			// Write out system level scores
			if(iteration == 1) System.out.println("Writing system level scores to: " + params.systemLevelOutputFile.getAbsolutePath());
			writeSystemLevelScores(params.systemLevelOutputFile, systemToScore, topicToSystemToScore);
			
			// Write out summary level scores
			if(iteration == 1) System.out.println("Writing summary level scores to: " + params.summaryLevelOutputFile.getAbsolutePath());
			writeSummaryLevelScores(params.summaryLevelOutputFile, systemToScore, topicToSystemToScore, topics);
			
			// Do something more (optional) and decide whether to loop again
			loopAgain = endAnalyzerObject.doSomething(topics, systemToScore, topicToSystemToScore, ruleWeights, transformWeights, transformNameToBitIndex, ruleToWeightIndex, bitIndexToWeightIndex);
			if(iteration == 1) System.out.println("Iteration: " + iteration + " seconds: " + ((System.nanoTime() - iterationStart)/1000000.0));
			
			iteration++;
		}
		// Typically there is no reason to loop again. This is included for optimization (or other) purposes.
		while(loopAgain);
	}
	
	private void mainEvaluation(
			// Map for holding overall system level scores
			Map<String, Double> systemToScore,
			// Map for holding summary level scores
			Map<String, Map<String, Double>> topicToSystemToScore,
			List<String> topics, 
			List<File> allFiles, 
			Map<String, List<File>> topicToFiles, 
			Map<String, List<File>> topicToReferenceFiles,
			Map<File, String> fileToSystem, 
			Map<File, List<BE>> fileToBEs, 
			double[] ruleWeights, 
			TallyFunction tallyFunction, 
			String refSystemsPattern, 
			//IntIntHashMap ruleToWeightIndex,
			IntIntHashMap bitIndexToWeightIndex, 
			double[] transformWeights,
			Map<File, List<Map<BE, List<BitSet>>>> fileToBEXs) throws IOException {
		
		Map<String, Integer> systemToScoreCount = new HashMap<String, Integer>();
		Map<String, Double> systemToUnnormalizedOverallScore = new HashMap<String, Double>();
		
		final int numTopics = topics.size();
		// Process each topic separately
		for (int t = 0; t < numTopics; t++) {		
			String topic = topics.get(t);
			
			List<File> referenceFiles = topicToReferenceFiles.get(topic);
			List<File> peerFiles = topicToFiles.get(topic);
			
			System.out.println("Processing topic: " + topic + " " + peerFiles.size() + " Peer Files." + referenceFiles.size() + " References");
			
			Map<String, Double> systemToScoreForTopic = new HashMap<String, Double>();
			topicToSystemToScore.put(topic, systemToScoreForTopic);		
			if(referenceFiles.size() == 0) {
				System.err.println("WARNING: No models found for topic: " + topic);
			}
			else {
				// Score each summary
				for(File peerFile : peerFiles) {
					System.out.println("Peer: " + peerFile.toString() );
					String system =  fileToSystem.get(peerFile);
				
					double score = 0.0;
				
					// NOTE: Consider create objects that perform the calc score
					if(referenceFiles.size() == 1 || tallyFunction instanceof TallyFunction.BinaryTallyFunction) {
						System.out.println("Fast score calc... " );
						score = calculateScoreFast(peerFile, referenceFiles, fileToBEs,	fileToBEXs, tallyFunction, ruleWeights, transformWeights, bitIndexToWeightIndex);
					
					}
					else {
						System.out.println("Score calc... " );
						score = calculateScore(peerFile, referenceFiles, fileToBEs,	fileToBEXs,	tallyFunction, ruleWeights, transformWeights, bitIndexToWeightIndex);
					}

					// Place the score into the map
					systemToScoreForTopic.put(system, score);
					Double prevScore = systemToUnnormalizedOverallScore.get(system);
					systemToUnnormalizedOverallScore.put(system, (prevScore == null ? 0d : prevScore) + score);
					Integer oldCount = systemToScoreCount.get(system);
					systemToScoreCount.put(system, (oldCount == null ? 0 : oldCount) + 1);
					System.out.println("...done" );
				}
			}
		}
		// Normalize system scores (divide by number of summaries)
		for(String system : systemToUnnormalizedOverallScore.keySet()) {
			Integer count = systemToScoreCount.get(system);
			systemToScore.put(system, systemToUnnormalizedOverallScore.get(system)/(count == null ? 0 : count));
		}
	}
	
	private static double calculateScore(File peerFile, 
										 List<File> referenceFiles, 
										 Map<File, List<BE>> fileToBEs, 
										 Map<File, List<Map<BE, List<BitSet>>>> fileToBEXs,
										 TallyFunction tallyFunction,
										 double[] beRuleWeights,
										 double[] transformWeights,
										 IntIntHashMap bitIndexToWeightIndex) {
		boolean isModelSummary = referenceFiles.contains(peerFile);				
		double score = 0.0;
		if(isModelSummary) {
			
						
			final int numReferences = referenceFiles.size();
			for(int i = 0; i < numReferences; i++) {
				File modelFile = referenceFiles.get(i);
				
				List<File> modelFiles = new ArrayList<File>(referenceFiles);
				modelFiles.remove(peerFile);
				IntIntHashMap beToModelFrequency = createBEFrequencyMap(modelFiles, fileToBEs);
			
				double precision = 0.0;
				double recall = 0.0;
				if(!modelFile.equals(peerFile)) {
					BEMatcher wbg = createBEMatcher(peerFile, modelFile, fileToBEs, fileToBEXs, beToModelFrequency, beRuleWeights);
					Object[] precisionAndRecall = wbg.solve(bitIndexToWeightIndex, transformWeights, tallyFunction);
					precision = (Double)precisionAndRecall[0];
					recall = (Double)precisionAndRecall[1];
				}
				score = Math.max(recall, score);
				
				System.out.println("Reference Summary P=" + precision + " R=" + recall );
			}
		}
		else {
			// Compare summary against each reference
			double totalRecall = 0.0;
			final int numReferences = referenceFiles.size();
			for(int i = 0; i < numReferences; i++) {
				double max = 0.0;
				List<File> modelFiles = new ArrayList<File>(referenceFiles);
				modelFiles.remove(referenceFiles.get(i));
				IntIntHashMap beToModelFrequency = createBEFrequencyMap(modelFiles, fileToBEs);
				for(int j = 0; j < numReferences; j++) {
					if(i != j) {
						File modelFile = referenceFiles.get(j);
						double precision = 0.0;
						double recall = 0.0;
						BEMatcher wbg = createBEMatcher(peerFile, modelFile, fileToBEs, fileToBEXs, beToModelFrequency, beRuleWeights);
						Object[] precisionAndRecall = wbg.solve(bitIndexToWeightIndex, transformWeights, tallyFunction);
						precision = (Double)precisionAndRecall[0];
						recall = (Double)precisionAndRecall[1];
						if(recall > max) {
							max = recall;
						}
						System.out.println("System Summary P=" + precision + " R=" + recall );
					}
				}
				totalRecall += max;
			}
			score = totalRecall / numReferences;
		}
		return score;
	}

	// faster calc if using binary tallying
	private static double calculateScoreFast(File peerFile,
			List<File> referenceFiles, Map<File, List<BE>> fileToBEs,
			Map<File, List<Map<BE, List<BitSet>>>> fileToBEXs,
			TallyFunction tallyFunction, double[] ruleWeights,
			double[] transformWeights, IntIntHashMap bitIndexToWeightIndex) {
		boolean isModelSummary = referenceFiles.contains(peerFile);
		double score = 0.0;
		if (isModelSummary) {
			final int numReferences = referenceFiles.size();
			if(numReferences == 1) {
				score = 1.0;
			}
			else {
				for (int m = 0; m < numReferences; m++) {
					File modelFile = referenceFiles.get(m);

					List<File> modelFiles = new ArrayList<File>(referenceFiles);
					modelFiles.remove(peerFile);
					IntIntHashMap beToModelFrequency = createBEFrequencyMap(
							modelFiles, fileToBEs);

					double precision = 0.0;
					double recall = 0.0;
					if (!modelFile.equals(peerFile)) {
						BEMatcher wbg = createBEMatcher(peerFile, modelFile,
							fileToBEs, fileToBEXs, beToModelFrequency,
							ruleWeights);
						Object[] precisionAndRecall = wbg.solve(
								bitIndexToWeightIndex, transformWeights,
								tallyFunction);
						precision = (Double)precisionAndRecall[0];
						recall = (Double) precisionAndRecall[1];
					} else {
						precision = 0;
						recall = 0;
					}
					score = Math.max(recall, score);
					System.out.println("Reference Summary P=" + precision + " R=" + recall );
				}
			}
		} 
		else {
			// Compare summary against each reference
			IntIntHashMap beToModelFrequency = createBEFrequencyMap(referenceFiles, fileToBEs);
			DoubleArrayList scores = new DoubleArrayList();
			final int numReferences = referenceFiles.size();
			for (int i = 0; i < numReferences; i++) {		
				File modelFile = referenceFiles.get(i);
				double precision = 0.0;
				double recall = 0.0;
				BEMatcher wbg = createBEMatcher(peerFile, modelFile,
								fileToBEs, fileToBEXs, beToModelFrequency,
								ruleWeights);
				Object[] precisionAndRecall = wbg.solve(
								bitIndexToWeightIndex, transformWeights,
								tallyFunction);
				precision = (Double)precisionAndRecall[0];
				recall = (Double) precisionAndRecall[1];
				if(i > 0) {
					if(recall > scores.get(scores.size()-1)) {
						scores.add(recall);
					}
					else {
						scores.beforeInsert(0, recall);
					}
				}
				else {
					scores.add(recall);
				}
				System.out.println("System Summary " + i + " P=" + precision + " R=" + recall );
			}
			score = scores.size() <= 1 ? scores.get(0) : ((numReferences-1)*scores.get(scores.size()-1) + scores.get(scores.size()-2)) / numReferences; 
		}
		return score;
	}

	
	/**
	 * Create object for performing the BE matching
	 */
	private static BEMatcher createBEMatcher(File peerFile, 
									   File modelFile, 
									   Map<File, List<BE>> fileToBes,
									   Map<File, List<Map<BE, List<BitSet>>>> fileToBEXs,
									   IntIntHashMap beToModelFrequency,
									   double[] ruleWeights) {
		List<BE> summaryBes = fileToBes.get(peerFile);
		List<BE> truthBes = fileToBes.get(modelFile);
		int numTruthBes = truthBes.size();						
	
		BitSet emptyBitSet = new BitSet();
		BEMatcher matcher = new BEMatcher(ruleWeights, summaryBes, truthBes, beToModelFrequency);
		int numSummaryBes = summaryBes.size();
		List<Map<BE, List<BitSet>>> BEXes = fileToBEXs.get(peerFile);
		for(int i = 0; i < numSummaryBes; i++) {
			BE summaryBE = summaryBes.get(i);
			Map<BE, List<BitSet>> bexMap = BEXes.get(i);
			for(int j = 0; j < numTruthBes; j++) {
				BE truthBE = truthBes.get(j);
				if(summaryBE.equals(truthBE)) {
					matcher.updateEdge(summaryBE, truthBE, emptyBitSet);
				}
			}
			for(BE be : bexMap.keySet()) {
				List<BitSet> bitSets = bexMap.get(be);
				for(int j = bitSets.size()-1; j>=0; j--) {
					matcher.updateEdge(summaryBE, be, bitSets.get(j));
				}
			}
		}
		return matcher;
	}
	
	/**
	 *	Read BE extraction rule to weight index mapping 
	 */
	private static IntIntHashMap readRuleToWeightIndex(File ruleFile) throws IOException {
		IntIntHashMap ruleToWeight = new IntIntHashMap();
		BufferedReader reader = new BufferedReader(new FileReader(ruleFile));
		String line = null;
		while((line = reader.readLine()) != null) {
			line = line.trim();
			if(!line.equals("") && !line.startsWith("#")) {
				String[] split = line.split("\\s+");
				int weight = Integer.parseInt(split[0]);
				int rule = Integer.parseInt(split[1].substring(1));
				ruleToWeight.put(rule, weight);
			}
		}
		reader.close();
		return ruleToWeight;
	}
	
	/**
	 * Create map containing BE->frequency mapping 
	 */
	private static IntIntHashMap createBEFrequencyMap(List<File> files, Map<File, List<BE>> fileToBEs) {
		IntIntHashMap beToFrequency = new IntIntHashMap();
		for(File file : files) {
			List<BE> beList = fileToBEs.get(file);
			IntIntHashMap beEquivIdSet = new IntIntHashMap();
			for(BE be : beList) {
				int equivId = be.getEquivalentId();
				if(beEquivIdSet.get(equivId) == 0) {
					beEquivIdSet.put(equivId, 1);
					beToFrequency.put(equivId, beToFrequency.get(equivId)+1);
				}
			}
		}
		return beToFrequency;
	}
	
	/**
	 * Writes out overall system level scores 
	 */
	private static void writeSystemLevelScores(File outputFile, Map<String, Double> systemToScore,
							Map<String, Map<String, Double>> topicToSystemToScore) throws IOException {
		PrintWriter out = new PrintWriter(outputFile);
		List<String> keys = BEUtils.sortScores(systemToScore.keySet(), systemToScore);
		for (String system : keys) {
			
				
				out.println(system + "\t" + systemToScore.get(system));
			
		}
		out.close();
	}
	
	/**
	 * Writes out summary level scores 
	 */
	private static void writeSummaryLevelScores(File outputFile, Map<String, Double> systemToScore,
		Map<String, Map<String, Double>> topicToSystemToScore, List<String> topics) throws IOException {
		PrintWriter out = new PrintWriter(outputFile);
		for(String topic : topics) {
			Map<String, Double> systemToScoreByTopic = topicToSystemToScore.get(topic);
			List<String> keys = BEUtils.sortScores(systemToScoreByTopic.keySet(), systemToScoreByTopic);
			for (String system : keys) {
				out.println(topic + "." + system + " " + systemToScoreByTopic.get(system));
			}
		}
		out.close();
	}
	
}
