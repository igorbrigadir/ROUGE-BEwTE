package bewte.names;

import java.io.File;

public class T2VStyleNameExtractor implements NameExtractor {
	
	/**
	 * Topic name ends at first dash
	 */
	public String getTopicName(File file) {
		String filename = file.getName();
		return filename.substring(0, filename.indexOf('-'));
	}
	
	/**
	 * System name starts after last dash
	 */
	public String getSystemName(File file) {
		String filename = file.getName();
		return filename.substring(filename.lastIndexOf('.')+1);
	}
}