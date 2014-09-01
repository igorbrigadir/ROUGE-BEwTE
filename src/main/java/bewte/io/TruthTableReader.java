package bewte.io;

import java.io.File;
import java.util.Map;

public interface TruthTableReader {
	public Map<String, Map<String, Double>> readTruthTableFile(File truthTableFile) throws Exception;
}
	