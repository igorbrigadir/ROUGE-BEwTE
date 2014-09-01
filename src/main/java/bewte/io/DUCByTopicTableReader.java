package bewte.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class DUCByTopicTableReader implements TruthTableReader {
	
	public Map<String, Map<String, Double>> readTruthTableFile(File truthTableFile) throws Exception {
		Map<String, Map<String, Double>> result = new HashMap<String, Map<String,Double>>();
		BufferedReader reader = new BufferedReader(new FileReader(truthTableFile));
		String line = null;
		for(int i = 0; i < 6; i++) {
			reader.readLine();// header junk
		}
		String currentTopic = null;
		Map<String, Double> currentMap = null;
		while((line = reader.readLine()) != null) {
			line = line.trim();
			if(!line.equals("")) {
				String[] split = line.split("\\t+");
				String topic = split[0];
				String system = split[3];
				Double score = Double.parseDouble(split[4]);
				if(currentTopic == null || !topic.equals(currentTopic)) {
					currentTopic = topic;
					result.put(currentTopic, currentMap = new HashMap<String, Double>());
				}
				currentMap.put(system, score);
			}
		}
		reader.close();
		return result;
	}
}