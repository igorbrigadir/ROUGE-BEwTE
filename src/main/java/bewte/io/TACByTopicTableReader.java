package bewte.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class TACByTopicTableReader implements TruthTableReader {
	
	public Map<String, Map<String, Double>> readTruthTableFile(File truthTableFile) throws Exception {
		Map<String, Map<String, Double>> result = new HashMap<String, Map<String,Double>>();
		BufferedReader reader = new BufferedReader(new FileReader(truthTableFile));
		String line = null;
		for(int i = 0; i < 6; i++) {
			reader.readLine();// header junk
		}
		while((line = reader.readLine()) != null) {
			line = line.trim();
			if(!line.equals("")) {
				String[] split = line.split("\\s+");
				String topic = split[0];
				String system = split[1];
				Double score = null;
				if(system.matches("[A-Z]+")) {
					score = Double.parseDouble(split[6]);
				}
				else {
					score = Double.parseDouble(split[8]);
				}
				if(!result.containsKey(topic)) {
					result.put(topic, new HashMap<String, Double>());
				}
				result.get(topic).put(system, score);
			}
		}
		reader.close();
		return result;
	}
}