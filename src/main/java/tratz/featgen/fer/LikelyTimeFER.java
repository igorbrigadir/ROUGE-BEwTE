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

package tratz.featgen.fer;

import java.io.Serializable;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Feature extraction rule for identifying some likely time expressions. (Not particularly thorough)
 *
 */
public class LikelyTimeFER extends AbstractFeatureRule implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	protected Matcher mYearMatcher;
	protected Matcher mTimePeriods;
	protected Matcher mTimeWithColon;
	
	public Set<String> getProductions(String s, String type, Set<String> feats) {
		if(mYearMatcher == null) {
			// Numbers from 1000-2999 without commas are likely years in news text
			// because 1) no comma and more than 3 digits in length
			// numbers greater than 2299 are unlikely to be years except in science fiction
			// could probably tighten up the lower bound of 1000 by some but this could be dangerous
			// for history-related docs.
			mYearMatcher =  Pattern.compile("(1[0-9]{3}|2[0-2][0-9]{2})s?").matcher("");
			// Catch US time zones + GMT, p.m., and a.m.
			mTimePeriods = Pattern.compile("EDT|EST|MST|MDT|PST|PDT|CST|CDT|GMT|p\\.m\\.|a\\.m\\.", Pattern.CASE_INSENSITIVE).matcher("");
			mTimeWithColon = Pattern.compile("[012]?[0-9]:[0-9][0-9]").matcher("");
		}
		if(mYearMatcher.reset(s).matches()) {
			feats.add("liTime");
		}
		if(mTimePeriods.reset(s).matches()) {
			feats.add("liTime");
		}
		if(mTimeWithColon.reset(s).matches()) {
			feats.add("liTime");
		}
		return feats;
	}
	
}