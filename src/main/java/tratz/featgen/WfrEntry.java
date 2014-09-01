package tratz.featgen;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import tratz.featgen.fer.FeatureExtractionRule;
import tratz.featgen.wfr.WordFindingRule;

public class WfrEntry implements Serializable {
		
		private static final long serialVersionUID = 1L;
		private WordFindingRule mRule;
		
		private String mName;
		private String mPrefix;
		
		private List<FeatureExtractionRule> mFERs;
		
		public String getName() {return mName;}
		public String getPrefix() {return mPrefix;}
		public List<FeatureExtractionRule> getFERs() {return mFERs;}
		
		public WfrEntry(String name, 
				           String prefix, 
				           List<FeatureExtractionRule> fers, 
				           String className,
				           Map<String, String> params) throws Exception {
			mName = name;
			mPrefix = prefix;
			mFERs = fers;
			mRule = (WordFindingRule)Class.forName(className).newInstance();
			mRule.init(params);
		}
		
		public WfrEntry(String name, 
		           String prefix, 
		           List<FeatureExtractionRule> fers, 
		           WordFindingRule wfr) {
			mName = name;
			mPrefix = prefix;
			mFERs = fers;
			mRule = wfr;
		}
		
		public WordFindingRule getWfrRule() {
			return mRule;
		}
	}
