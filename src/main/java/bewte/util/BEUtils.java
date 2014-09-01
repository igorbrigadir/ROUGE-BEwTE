package bewte.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bewte.names.NameExtractor;

public class BEUtils {
	
	public static List<String> sortScores(Set<String> systemsSet, final Map<String, Double> systemToScore) {
		List<String> keys = new ArrayList<String>(systemsSet);
		Collections.sort(keys, new Comparator<String>() {
			public int compare(String i1, String i2) {
				double score1 = systemToScore.get(i1);
				double score2 = systemToScore.get(i2);
				if(Double.isNaN(score1)) {
					score1 = 0;
				}
				if(Double.isNaN(score2)) {
					score2 = 0;
				}
				
				if(score1 > score2) {
					return -1;
				}
				else if(score2 > score1) {
					return 1;
				}
				else {
					return 0;
				}
			}
		});
		return keys;
	}
	
	public static void getFiles(File dir, List<File> fileList, String pattern) {
		for(File file : dir.listFiles()) {
			if(file.isDirectory()) {
				getFiles(file, fileList, pattern);
			}
			else {
				if(file.getName().matches(pattern)) {
					fileList.add(file);
				}
			}
		}
	}
	
	public static void getFiles(File dir, List<File> fileList) {
		getFiles(dir, fileList, ".*");
	}
	
	public static List<File> getFiles(List<File> files, String pattern) {
		List<File> truthFiles = new ArrayList<File>();
		for (File file : files) {
			if (file.getName().matches(pattern)) {
				truthFiles.add(file);
			}
		}
		return truthFiles;
	}

	public static List<File> getFilesForTopic(String topic, List<File> files, NameExtractor topicNameGen) {
		List<File> filesForTopic = new ArrayList<File>();
		for (File file : files) {
			if (topic.equals(topicNameGen.getTopicName(file))) {
				filesForTopic.add(file);
			}
		}
		return filesForTopic;
	}

	public static List<String> getTopicList(Collection<File> files, NameExtractor gen) {
		return getTopicList(files.toArray(new File[files.size()]), gen);
	}
	
	public static List<String> getTopicList(File[] files, NameExtractor gen) {
		Set<String> topicList = new LinkedHashSet<String>();
		for (File file : files) {
			topicList.add(gen.getTopicName(file));
		}
		return new ArrayList<String>(topicList);
	}
	
}