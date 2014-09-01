package bewte.names;

import java.io.File;

public class StopAtFirstPeriodNameExtractor implements NameExtractor {
	
	public String getTopicName(File file) {
		String filename = file.getName();
		return filename.substring(0, filename.indexOf('.'));
	}
	
	public String getSystemName(File file) {
		String filename = file.getName();
		return filename.substring(filename.lastIndexOf('.')+1);
	}
}