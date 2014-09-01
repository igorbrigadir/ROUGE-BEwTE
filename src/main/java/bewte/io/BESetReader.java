package bewte.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import foal.map.IntIntHashMap;

import bewte.BE;
import bewte.BEConstants;
import bewte.BE.BEPart;

public class BESetReader {
	
	public static List<Set<String>> getLemmaSet(File file) throws IOException {
		List<Set<String>> listOfLemmaSets = Arrays.asList((Set<String>)new HashSet<String>(), (Set<String>)new HashSet<String>(), (Set<String>)new HashSet<String>());
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		while((line = reader.readLine()) != null) {
			if(line.startsWith("@lemmas")) {
				line = line.toLowerCase();
				String[] split = line.split("\\t+");
				Set<String> lemmaSet = null;
				if(line.startsWith("@lemmas-right")) {
					lemmaSet = listOfLemmaSets.get(2);	
				}
				else if(line.startsWith("@lemmas-left")) {
					lemmaSet = listOfLemmaSets.get(0);
				}
				else if(line.startsWith("@lemmas-center")) {
					lemmaSet = listOfLemmaSets.get(1);
				}
				for(int i = 1; i < split.length; i++) {
					lemmaSet.add(split[i]);
				}
			}
		}
		reader.close();
		return listOfLemmaSets;
	}
	
	public static void getBeList(List<BE> bes, 
								 File file, 
								 Set<String> rulesToIgnore, 
								 IntIntHashMap ruleToAlpha,
								 Map<String, Integer> transformNameToBitIndex,
								 List<Map<BE, List<BitSet>>> beToTransforms,
								 Map<String, BEPart> bePartCanonicalMap,
								 boolean multiplesAllowed) throws IOException {
		IntIntHashMap beToCount = new IntIntHashMap();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		Map<BE, List<BitSet>> prevBeMap = null;
		boolean previousBeSkipped = false;
		char splitChar = BEConstants.BE_SEPARATOR_CHAR;
		String splitString = ""+splitChar;//"\\"+splitChar;
		while ((line = reader.readLine()) != null) {
			if (!line.trim().equals("") && !line.startsWith("#") && !line.startsWith("@lemmas")) {
				
				boolean tabStart = line.startsWith("\t");
				String[] transforms = null;
				String beString = null;
				if(tabStart) {
					String[] tabSplit = line.split("\\t+");
					transforms = tabSplit[2].split(":");
					beString = tabSplit[1].trim();
				}
				else {
					beString = line.trim();
				}
				int beStart = beString.indexOf(splitChar);
				if(beStart <= 0) {
					System.err.println(beString);
				}
				String rule = beString.substring(0, beStart);
				if(rule.startsWith("r")) {
					rule = rule.substring(1);
				}
				if(rulesToIgnore == null || !rulesToIgnore.contains(rule)) {
					//System.err.println(beString);
					String[] beParts = beString.substring(beStart+1).split(splitString);
					List<BE.BEPart> parts = new ArrayList<BE.BEPart>();
					for(int i = 0; i < beParts.length; i+=2) {
						String bePartString = beParts[i] + splitString + beParts[i+1];
						BE.BEPart part = bePartCanonicalMap.get(bePartString);
						if(part == null) {
							bePartCanonicalMap.put(bePartString, part = new BE.BEPart(beParts[i], beParts[i+1]));
						}
						parts.add(part);
					}
					
					int ruleNum = Integer.parseInt(rule);
					BE be = new BE(parts, ruleNum, ruleToAlpha != null ? ruleToAlpha.get(ruleNum) : -1);
					int id = be.getEquivalentId();//.getUniqueId();
					if(tabStart) {
						if(!previousBeSkipped) {
							List<BitSet> bitSets = prevBeMap.get(be);
							BitSet newBitSet = createBitSet(transforms, transformNameToBitIndex);
							boolean isSuperSet = false;
							if(bitSets == null) {
								prevBeMap.put(be, bitSets = new ArrayList<BitSet>());
							}
							else {
								List<BitSet> bitSetsToRemove = new ArrayList<BitSet>();
								for(BitSet bsPrior : bitSets) {
									if(newBitSet.equals(bsPrior)) {
										bitSetsToRemove.add(bsPrior);
									}
									else {
										BitSet superSet = new BitSet();
										superSet.or(newBitSet);
										superSet.or(bsPrior);
										if(superSet.equals(bsPrior)) {
											bitSetsToRemove.add(bsPrior);
										}
										else if(superSet.equals(newBitSet)) {
											isSuperSet = true;
											break;
										}
									}
								}
								if(!isSuperSet) {
									bitSets.removeAll(bitSetsToRemove);
								}
							}
							if(!isSuperSet) {
								bitSets.add(newBitSet);
							}
						}
					}
					else {
						int count = beToCount.get(id);
						if(count > 0) {
							beToCount.put(id, count+1);
							if(multiplesAllowed) {
								previousBeSkipped = false;
								bes.add(be);
								beToTransforms.add(prevBeMap = new HashMap<BE, List<BitSet>>());	
							}
							else {
								previousBeSkipped = true;	
							}							
						}
						else {
							previousBeSkipped = false;
							beToCount.put(id, 1);
							bes.add(be);
							beToTransforms.add(prevBeMap = new HashMap<BE, List<BitSet>>());
						}
					}
				}
			}
		}
		reader.close();
	}
	
	private static BitSet createBitSet(String[] transformNames, Map<String, Integer> transformNameToBitIndex) {
		BitSet bs = new BitSet();
		for(String s : transformNames) {
			bs.set(transformNameToBitIndex.get(s));
		}
		return bs;
	}
	
	
	
}