package bewte.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import foal.list.IntArrayList;
import foal.map.IntDoubleHashMap;
import foal.map.IntIntHashMap;

import bewte.transforms.BETransform;

public class TransformInfoReader {
	
	public static double[] readTransformCoeffs(File filename, IntIntHashMap bitIndexToWeightIndex, Map<String, Integer> transformNameToBitIndex) throws IOException {
		Map<String, Integer> weightNames = new HashMap<String, Integer>();
		IntDoubleHashMap weightToValue = new IntDoubleHashMap();
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line = null;
		int weightIndex = -1;
		while((line = reader.readLine()) != null) {
			line = line.trim();
			if(!line.equals("") && !line.startsWith("#") && !line.startsWith("@")) {
				String[] split = line.split("=");
				if(split.length == 1) {
					String[] mapping = line.split("\\t+");
					int weight = -1;
					if(!weightNames.keySet().contains(mapping[1])) {
						weightIndex++;
						weight = weightIndex;
						weightNames.put(mapping[1], weight);
					}
					else {
						weight = weightNames.get(mapping[1]);
					}
					Integer index = transformNameToBitIndex.get(mapping[0]);
					if(index == null) {
						System.err.println("Warning: no index for: " + mapping[0]);
					}
					else {
						bitIndexToWeightIndex.put(index, weight);
					}
				}
				else {
					System.err.println(split[0]);
					int index = weightNames.get(split[0]);
					double value = Double.parseDouble(split[1]);
					weightToValue.put(index, value);					
				}
			}
		}
		reader.close();
		int numWeights = weightToValue.size();
		double[] weights = new double[numWeights];
		IntArrayList keys = weightToValue.keys();
		for(int i = 0; i < keys.size(); i++) {
			int key = keys.get(i);
			weights[key] = weightToValue.get(key);
		}
		return weights;
	}
	
	public static void readTransformPipeline(File transformListSource, List transformList, boolean initialize) throws Exception {
		
		BufferedReader reader = new BufferedReader(new FileReader(transformListSource));
		String line = null;
		
		final int DEFAULT = 0;
		final int DEFS = 1;
		final int PIPE = 2;
		
		int state = DEFAULT;
		
		Map<String, BETransform> nameToTransform = new LinkedHashMap<String, BETransform>();
		
		while((line = reader.readLine()) != null) {
			line = line.trim();
			if(!line.startsWith("#") && !line.equals("")) {
				if(line.equals("@startDefs")) {
					state = DEFS;
				}
				else if(line.equals("@startPipe")) {
					state = PIPE;
				}
				else {
					switch(state) {
						case DEFS:
							String[] split = line.split("\\t+");
							String transformName = split[0];
							String transformClass = split[1];
							Map<String, String> params = new HashMap<String, String>();
							for(int i = 2; i < split.length; i++) {
								String[] paramValPair = split[i].split("=");
								String param = paramValPair[0];
								String value = paramValPair[1];
								params.put(param, value);
							}
							BETransform transform = (BETransform)Class.forName(transformClass).newInstance();
							//if(initialize) {
								transform.setName(transformName);
								transform.initialize(params);
							//}
							nameToTransform.put(transformName, transform);
						break;
						
						case PIPE:
							String[] pieces = line.split("\\t+");
							if(pieces.length == 1) {
								BETransform t = nameToTransform.get(pieces[0]);
								transformList.add(t);
							}
							else {
								List<BETransform> layer = new ArrayList<BETransform>();
								for(String piece : pieces) {
									BETransform t = nameToTransform.get(piece);
									if(t == null) {
										System.err.println("EEK: " + piece);
									}
									layer.add(t);
								}
								transformList.add(layer);
							}
						break;
					}
				}
			}
		}
		reader.close();		
		
	}
	
}