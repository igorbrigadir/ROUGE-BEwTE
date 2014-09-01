package bewte.names;

import java.io.File;

public class StopAtLastPlusSignNameExtractor implements NameExtractor {
	
	public String getTopicName(File file) {
		String filename = file.getName();
		return filename.substring(0, filename.lastIndexOf('+'));
	}
	
	public String getSystemName(File file) {
		String filename = file.getName();
		String[] split = filename.split("\\+");
		return split[0] + "+" + split[3];//filename.substring(filename.lastIndexOf('+')+1);
	}
}