package bewte.names;

import java.io.File;

/**
 * Interface for determining the topic and system names for a given <code>File</code>.
 */
public interface NameExtractor {
	// it might be nice to take some parameters so one could create a generic regex-based name extractor
	public String getTopicName(File file);
	public String getSystemName(File file);
	
}