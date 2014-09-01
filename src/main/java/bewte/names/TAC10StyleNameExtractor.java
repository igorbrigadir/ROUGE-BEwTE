package bewte.names;

import java.io.File;

public class TAC10StyleNameExtractor implements NameExtractor {
	
	/**
	 * Topic name ends at first period
	 */
	public String getTopicName(File file) {
		String filename = file.getName();
		return filename.substring(0, filename.lastIndexOf('.'));
	}
	
	/**
	 * System name starts after last period
	 */
	public String getSystemName(File file) {
		String filename = file.getName();
		return filename.substring(filename.lastIndexOf('.')+1);
	}
}