package bewte;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tratz.jwni.WordNet;

import foal.map.IntObjectHashMap;

import bewte.io.BESetReader;
import bewte.io.TransformInfoReader;
import bewte.names.NameExtractor;
import bewte.transforms.BETransform;
import bewte.util.BEUtils;

public class BEXpander {

	public static final int MAX_PER_DIR = 200;
	
	public static void main(String[] args) throws Exception {
		File beDir = new File(args[0]);
		File outputDir = new File(args[1]);
		String wordNetDir = args[2];
		int startIndex = Integer.parseInt(args[3]);
		int endIndex = Integer.parseInt(args[4]);
		String referenceFilePattern = args[5];
		File transformsListFile = new File(args[6]);
		NameExtractor topicNameGenerator = (NameExtractor)Class.forName(args[7]).newInstance();
		
		new WordNet(new File(wordNetDir).getAbsoluteFile().toURI().toString());
		
		int currentDirCount = 0;
		int dirCount = 0;
		File currentDir = null;
		DecimalFormat format = new DecimalFormat();
		format.setGroupingUsed(false);
		format.setMaximumFractionDigits(0);
		format.setMinimumIntegerDigits(8);
			
		// Create transform pipeline
		IntObjectHashMap<String> bitToTransformName = new IntObjectHashMap<String>();
		List transforms = new ArrayList();
		Map<String, Integer> transformNameToBitIndex = new HashMap<String, Integer>();
		createTransformPipeline(transformsListFile, transforms, bitToTransformName, transformNameToBitIndex);
		
		List<File> befiles = new ArrayList<File>();
		BEUtils.getFiles(beDir, befiles);
		List<String> topics = BEUtils.getTopicList(befiles.toArray(new File[0]), topicNameGenerator);
		Map<String, List<File>> topicToFiles = new HashMap<String, List<File>>();
		Map<String, List<File>> topicToGoldenFiles = new HashMap<String, List<File>>();
		
		Map<File, List<BE>> fileToBes = new HashMap<File, List<BE>>();
		Map<File, List<Set<String>>> fileToLemmaSet = new HashMap<File, List<Set<String>>>();
		
		Set<String> systemsSet = new HashSet<String>();
		Map<File, String> fileToSystem = new HashMap<File, String>();
		
		Map<String, BE.BEPart> bePartMap = new HashMap<String, BE.BEPart>();
		
		Collections.sort(topics);
		
		final int numTopics = topics.size();
		if(endIndex < 0) {
			endIndex = numTopics;
		}
		System.err.println("Number of Topics: " + numTopics);
		for (int t = startIndex; t < endIndex; t++) {
			String topic = topics.get(t);
			
			List<File> filesForTopic = BEUtils.getFilesForTopic(topic, befiles, topicNameGenerator);
			List<File> goldenFiles = BEUtils.getFiles(filesForTopic, referenceFilePattern);
			
			System.err.println("Topic: " + topic + " " + (t+1) + " of " + numTopics + " with " + filesForTopic.size() + " entries inc " + goldenFiles.size() + " refs");
			topicToFiles.put(topic, filesForTopic);
			topicToGoldenFiles.put(topic, goldenFiles);
			
			for(File file : filesForTopic) {
				fileToLemmaSet.put(file, BESetReader.getLemmaSet(file));
				List<BE> beList = new ArrayList<BE>();
				List<Map<BE, List<BitSet>>> map = new ArrayList<Map<BE, List<BitSet>>>();
				BESetReader.getBeList(beList, file, null, null, null, map, bePartMap, true);
				fileToBes.put(file, beList);
				String filename = file.getName();
				String system = filename.substring(filename.lastIndexOf('.') + 1, filename.length());
				systemsSet.add(system);
				fileToSystem.put(file, system);
			}		
			
			List<File> modelFiles = topicToGoldenFiles.get(topic);
			List<File> peerFiles = topicToFiles.get(topic);
				
			final int numModels = modelFiles.size();
			
			List<BE> goldenBEsSuperList = new ArrayList<BE>();
			
			Set<String> modelLemmaSet = new HashSet<String>();	
			
			for(int m = 0; m < numModels; m++) {
				File modelFile = modelFiles.get(m);
				List<Set<String>> goldenLemmas = fileToLemmaSet.get(modelFile);
				modelLemmaSet.addAll(goldenLemmas.get(0));
				modelLemmaSet.addAll(goldenLemmas.get(1));
				modelLemmaSet.addAll(goldenLemmas.get(2));
				List<BE> truthBes = fileToBes.get(modelFile);
				goldenBEsSuperList.addAll(truthBes);
			}
			
			
			final int numPeers = peerFiles.size();
			for(int p = 0; p < numPeers; p++) {
				currentDirCount++;
				if(currentDirCount > MAX_PER_DIR || (dirCount==0 && currentDirCount==1)) {
					currentDirCount = 0;
					currentDir = new File(outputDir, format.format(dirCount));
					currentDir.mkdirs();
					dirCount++;
				}
				File peerFile = peerFiles.get(p);
				System.err.println("File: " + peerFile.getName());
				List<BE> summaryBes = fileToBes.get(peerFile);
				File outputFile = new File(currentDir, peerFile.getName());
				applyTransforms(outputFile, bitToTransformName, transformNameToBitIndex, transforms, summaryBes, goldenBEsSuperList, modelLemmaSet);
			}
			
			// Clean up memory
			List<File> files = topicToFiles.remove(topic);
			topicToGoldenFiles.remove(topic);
			for(File file : files) {
				fileToBes.remove(file);
				fileToLemmaSet.remove(file);
			}
			System.gc();
		}		
	}

	private static List createTransformPipeline(File transformFile, List transforms, IntObjectHashMap<String> bitToTransformName,  
			Map<String, Integer> transformNameToBitIndex) throws Exception {
		TransformInfoReader.readTransformPipeline(transformFile, transforms, true);
		createBitMaps(transforms, bitToTransformName, transformNameToBitIndex);
		return transforms;
	}
	
	public static void createBitMaps(List transforms, IntObjectHashMap<String> bitToTransformName, Map<String, Integer> transformToBitIndex) {
		int numTransforms = 0;
		for(Object tObj : transforms) {
			if(tObj instanceof BETransform) {
				transformToBitIndex.put(((BETransform)tObj).getName(), numTransforms);
				bitToTransformName.put(numTransforms, ((BETransform)tObj).getName());
				//System.err.println(numTransforms + " -> " + ((BETransform)tObj).getName());
				numTransforms++;
				
			}
			else {
				for(BETransform transform : (List<BETransform>)tObj) {
					transformToBitIndex.put(transform.getName(), numTransforms);
					bitToTransformName.put(numTransforms, transform.getName());
					//System.err.println(numTransforms + " -> " + transform.getName());
					numTransforms++;
				}
			}
		}
	}

	
	private static void applyTransforms(File outputFile, IntObjectHashMap<String> bitToTransformName, Map<String, Integer> transformNameToBitIndex, List transforms, List<BE> summaryBes, List<BE> truthBes, Set<String> modelLemmaSet) throws Exception {
		int numTransforms = 0;
	
		for(Object tObj : transforms) {
			if(tObj instanceof BETransform) {
				((BETransform)tObj).reinitialize(summaryBes, truthBes, modelLemmaSet);
				numTransforms++;
			}
			else {
				List<BETransform> tList = (List<BETransform>)tObj;
				for(BETransform transform : tList) {
					transform.reinitialize(summaryBes, truthBes, modelLemmaSet);
					numTransforms++;
				}
			}
		}
		Set<String> modelStrings = new HashSet<String>();
		Set<String> modelPartStrings = new HashSet<String>();
		for(BE be : truthBes) {
			modelStrings.add(be.toTextString().toLowerCase());
			for(BE.BEPart part : be.getParts()) {
				modelPartStrings.add(part.text);
				modelPartStrings.add(part.text.toLowerCase());
			}
		}
		
		BitSet bs = new BitSet(numTransforms);
		PrintWriter writer = new PrintWriter(new FileWriter(outputFile));
		for(BE be : summaryBes) {
			writer.println(be.getRule() + BEConstants.BE_SEPARATOR_STRING + be.toString());
			Map<String, List<BitSet>> newBeToTransformSet = new HashMap<String, List<BitSet>>();
			String beString = be.toString().toLowerCase();
			applyTransforms(writer, bitToTransformName, 0, transformNameToBitIndex, transforms, numTransforms, be, beString, be, bs, newBeToTransformSet, modelStrings, modelPartStrings);
		}
		writer.close();
		
	}
	
	private static void applyTransforms(PrintWriter writer, 
										IntObjectHashMap<String> bitToTransformName, 
										int tIndex, 
										Map<String, Integer> transformNameToBitIndex, 
										List transforms, 
										int numTransforms, 
										BE origBe, 
										String origBeString,
										BE be, 
										BitSet oldBitSet, 
										Map<String, List<BitSet>> newBeToTransformSet,
										Set<String> modelStrings,
										Set<String> bePartStrings) {
		int nextIndex = tIndex+1;
		if(tIndex < transforms.size()) {
			Object tObj = transforms.get(tIndex);
			List<BETransform> tList = null;
			if(tObj instanceof BETransform) {
				tList = new ArrayList<BETransform>();
				tList.add((BETransform)tObj);
			}
			else {
				tList = (List<BETransform>)tObj;
			}
			for(BETransform bet : tList) {
				BETransform t = (BETransform)bet;
				List<BE> transformedBes = t.transform(be, bePartStrings);
				if(transformedBes != null) {
					BitSet newBitSet = new BitSet(numTransforms);
					newBitSet.or(oldBitSet);
					newBitSet.set(transformNameToBitIndex.get(t.getName()));
					
					StringBuilder buf = new StringBuilder();
					for (int bit = newBitSet.nextSetBit(0); bit >= 0; bit = newBitSet.nextSetBit(bit+1)) {
						buf.append(bitToTransformName.get(bit)).append(":");
     				} 
					final String transformString = buf.toString();
					for(BE transformedBe : transformedBes) {
						String beString = transformedBe.toString().toLowerCase();
						if(!beString.equals(origBeString)) {
							List<BitSet> bitSets = newBeToTransformSet.get(beString);
							if(bitSets == null) {
								newBeToTransformSet.put(beString, bitSets = new ArrayList<BitSet>());
							}
							boolean isSuperSet = false;
							for(BitSet bsPrior : bitSets) {
								BitSet superSet = new BitSet();
								superSet.or(newBitSet);
								superSet.or(bsPrior);
								if(superSet.equals(newBitSet)) {
									isSuperSet = true;
									break;
								}
							}
							if(!isSuperSet) {
								bitSets.add(newBitSet);
							
								if(modelStrings.contains(transformedBe.toTextString().toLowerCase())) {
									String transformedBEString = transformedBe.getRule() + BEConstants.BE_SEPARATOR_STRING + transformedBe.toString();
									writer.println("\tr" + transformedBEString + "\t" + transformString);
								}
							
								applyTransforms(writer, bitToTransformName, nextIndex, transformNameToBitIndex, transforms, numTransforms, origBe, origBeString, transformedBe, newBitSet, newBeToTransformSet, modelStrings, bePartStrings);	
							}
						}
					}
				}
			}
			applyTransforms(writer, bitToTransformName, nextIndex, transformNameToBitIndex, transforms, numTransforms, origBe, origBeString, be, oldBitSet, newBeToTransformSet, modelStrings, bePartStrings);
		}
	}

}