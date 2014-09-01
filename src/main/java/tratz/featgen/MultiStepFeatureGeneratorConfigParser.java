/*
 * Copyright 2011 University of Southern California 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0 
 *      
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package tratz.featgen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tratz.featgen.CombinationEntry.ComboType;
import tratz.featgen.fer.FeatureExtractionRule;


public class MultiStepFeatureGeneratorConfigParser {
	
	public Map<String, FeatureExtractionRule> readFeatureExtractionRuleMap(String featureExtractionRuleList) throws Exception {
		Map<String, FeatureExtractionRule> ferRules = new HashMap<String, FeatureExtractionRule>();
		BufferedReader reader = new BufferedReader(new FileReader(featureExtractionRuleList));
		String line = null;
		while((line = reader.readLine()) != null) {
			if(!line.trim().equals("") && !line.startsWith("#")) {
				String[] split = line.split("\\t+");
				String id = split[0];
				String prefix = split[1];
				FeatureExtractionRule fer = (FeatureExtractionRule)Class.forName(split[2]).newInstance();
				fer.setPrefix(prefix);
				ferRules.put(id, fer);
				Map<String, String> params = new HashMap<String, String>();
				for(int i = 3; i < split.length; i++) {
					int equalsIndex = split[i].indexOf("=");
					String key = split[i].substring(0, equalsIndex);
					String value = split[i].substring(equalsIndex+1);
					params.put(key, value);
				}
				fer.init(params);
			}
		}
		reader.close();
		return ferRules;
	}
	
	public WfrEntry[] readComplexRules(String complexRuleListFile, Map<String, FeatureExtractionRule> ferMap) throws ConfigurationParseException, IOException {
		Set<String> ruleNamesSet = new HashSet<String>();
		List<WfrEntry> ruleList = new ArrayList<WfrEntry>();
		BufferedReader reader = new BufferedReader(new FileReader(complexRuleListFile));
		String line = null;
		while((line = reader.readLine()) != null) {
			line = line.trim();
			if(!line.equals("") && !line.startsWith("#")) {
				String[] split = line.split("\\t+");
				String prefix = split[0];
				String name = split[1];
				String wfrClassName = split[3];
				if(ruleNamesSet.contains(name)) {
					throw new ConfigurationParseException("Duplicate word-finding rule entry name: " + name);
				}
				else {
					ruleNamesSet.add(name);
				}
				List<String> ferNames = Arrays.asList(split[2].split(","));
				List<FeatureExtractionRule> fers = new ArrayList<FeatureExtractionRule>();
				for(String ferName : ferNames) {
					FeatureExtractionRule fer = ferMap.get(ferName);
					if(fer != null) {
						fers.add(fer);
					}
				}
				Map<String, String> params = new HashMap<String, String>();
				for(int i = 4; i < split.length; i++) {
					String paramValueSetting = split[i];
					int equalsIndex = paramValueSetting.indexOf("=");
					String param = paramValueSetting.substring(0, equalsIndex);
					String value = paramValueSetting.substring(equalsIndex+1);
					params.put(param, value);
				}			
				WfrEntry newRule = null;
				try {
					newRule = new WfrEntry(name,prefix,fers,wfrClassName,params);
				}
				catch(Exception e) {
					throw new ConfigurationParseException(e);
				}
				ruleList.add(newRule);
			}
		}
		reader.close();
		return ruleList.toArray(new WfrEntry[0]);
	}
	
	public CombinationEntry[] readCombinationEntries(File inputFile) throws IOException, ConfigurationParseException {
		List<CombinationEntry> entries = new ArrayList<CombinationEntry>();
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		String line = null;
		while((line = reader.readLine()) != null) {
			line = line.trim();
			if(!line.startsWith("#") && !line.equals("")) {
				String[] split = line.split("\\t+");
				String comboTypeString = split[0];
				CombinationEntry.ComboType comboType;
				if(comboTypeString.equals("*CROSS*")) {
					comboType = ComboType.CROSS_PRODUCT;
				}
				else if(comboTypeString.equals("*UNION*")) {
					comboType = ComboType.UNION;
				}
				else if(comboTypeString.equals("*INTERSECT*")) {
					comboType = ComboType.INTERSECTION;
				}
				else {
					throw new ConfigurationParseException("Unexpected combination type: " + comboTypeString + " specified.");
				}
				String newPrefix = split[1];
				String wfrPrefix1 = split[2];
				String ferPrefix1 = split[3];
				String wfrPrefix2 = split[4];
				String ferPrefix2 = split[5];
				
				String fullPrefix1 = wfrPrefix1+":"+ferPrefix1+":";
				String fullPrefix2 = wfrPrefix2+":"+ferPrefix2+":";
				
				entries.add(new CombinationEntry(comboType, newPrefix, fullPrefix1, fullPrefix2));
			}
		}
		reader.close();
		return entries.toArray(new CombinationEntry[entries.size()]);
	}
}