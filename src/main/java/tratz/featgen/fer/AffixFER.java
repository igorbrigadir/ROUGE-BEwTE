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

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Feature extraction rule that identifies a bunch of different affixes
 *
 */
public class AffixFER extends AbstractFeatureRule {
	
	private static final long serialVersionUID = 1L;
	
	private final static String DEVERBAL_AGENT_SUFFIX = "[a-zA-Z]*(ant|ee|er|ar|or)s?";
	private final static String DENOMINAL_AGENT_SUFFIX = "[a-zA-Z]*(ian|ist|ster|yst)s?";
	private final static String DENOMINAL_SUFFIX = "[a-zA-Z]*(age|dom|hood|ism|ship|ry|ocracy|ese|ite)s?";
	private final static String DEADJECTIVAL_SUFFIX = "[a-zA-Z]*(ce|cy|ty|ness)";
	private final static String DEVERBAL_SUFFIX = "[a-zA-Z]*(al|ce|ion|ment|ure|ing)s?";
	
	private final static String TIMEORDER_PREFIX = "(pre|post|fore|ex|re|mid)[a-zA-Z]+";
	private final static String DEGREE_PREFIX = "(arch|super|out|sur|sub|over|under|hyper|ultra|mini)[a-zA-Z]+";
	private final static String LOC_PREFIX = "(super|sub|inter|trans|intra|endo|exo|ecto|ent|ante|circum|cata|peri|para|fore|mid|opistho|supra)[a-zA-Z]+";
	private final static String NUM_PREFIX = "(ennea|uni|mono|bi|di|tri|multi|poly|quad|penta|hexa|hepta|octo|octa|novem|noven|dec|dodec|milli|peta|centi|tetra|singul)[a-zA-Z]+";
	
	private final static String FRONT_BACK_SIDE = "(back|front|side).*|.*(back|front|side)";
	private final static String NEGATIVE_PREFIX = "(un|non|in|im|dis|dys|ab|a|anti|contr|counter).+";
	private final static String COLOR_PREFIX = "(cupro|chlor|chrys|erythro|leuk|flav|lute|melan|polio|xanth|cyan|purp|cirrho|aureo|fulvi|viridi|prasini|glauc|amaur|cinere)[a-zA-Z]+";
	
	private final static String CAUSE_TO_BECOME_SUFFIX = "[a-zA-Z]+(en|ize|ise|ate|fy)";
	private final static String PARTICIPLE_SUFFIX = "[a-zA-Z]+(en|ed|ing)";
	private final static String VOWEL_SUFFIX = "[a-zA-Z]+[aeiouy]";
	private final static String STARTS_WITH_A = "[aA][a-zA-Z]+";
	
	private final static String BODY_PREFIX = "(thyro|pyel|pylor|sangui|syndesmo|syring|laryng|phleb|pulmo|rhin|reni|reno|ptero|phyll|oro|oto|enter|dent|adren|arteri|blephar|cephal|brachi|bronch|cervic|arth|cardi|caud|chondr|cost|crani|cuti|cyst|dactyl|derm|encephal|gastr|gnath|hepat|kerat|myo|nephr|neuro|ocul|odont|ophthalm|oste|ped|pell|pleur|pneum|pod|soma|stome|thorac|trache)[a-zA-Z]+";
	private final static String DISEASE_SUFFIX = "[a-zA-Z].*(centesis|cele|itis|lepsy|oma|phobia|lagnia|phonia|plegia|asis|derma|lalia|malacia|megaly|melia|metropia|odynia|onychia|opsia|opia|osmia|pathy|penia|phasia|phrenia|thymia)";
	
	private final static String HAVING_SUFFIX = "[a-zA-Z]+(ous|ful|y|ive|ant|ent|ose)";
	private final static String ABLE_SUFFIX = "[a-zA-Z]+(ble)";
	private final static String RELATEDTO_SUFFIX = "[a-zA-Z]+(al|il|ic)";
	private final static String EN_SUFFIX = "[a-zA-Z]+(en)";
	private final static String ESCENT_SUFFIX = "[a-zA-Z]+(escent)";
	private final static String OID_SUFFIX = "[a-zA-Z]+(oid)";
	private final static String SUPERLATIVE_SUFFIX = "[A-Za-z]+(est|most)";
	private final static String ABSENCE_SUFFIX = "[A-Za-z]+less";
	private final static String LIKE_SUFFIX = "[A-Za-z]+(ical|ish|ory)";
	
	private transient Matcher mDeverbalAgent,
							  mDenominalAgent,
							  mDenominalSuffix,
							  mDeadjectivalSuffix,
							  mDeverbalSuffix,
							  mTimeOrderPrefix,
							  mDegPrefix,
							  mLocPrefix,
							  mNumPrefix,
							  mFrontBackSide,
							  mNegativePrefix,
							  mColorPrefix,
							  mCauseToBecomeSuffix,
							  mParticipleSuffix,
							  mVowelSuffix,
							  mStartsWithA,
							  mBodySuffix,
							  mDiseaseSuffix,
							  mHavingSuffix,
							  mAbleSuffix,
							  mRelatedToSuffix,
							  mEnSuffix,
							  mEscentSuffix,
							  mOidSuffix,
							  mSuperlativeSuffix,
							  mAbsenceSuffix,
							  mLikeSuffix;		
	
	private final boolean matches(Matcher matcher, String s) {
		return matcher.reset(s).matches();
	}
	
	private void reinit() {
		if(mDeverbalAgent == null) {
			mDeverbalAgent = Pattern.compile(DEVERBAL_AGENT_SUFFIX, Pattern.CASE_INSENSITIVE).matcher("");
			mDenominalAgent = Pattern.compile(DENOMINAL_AGENT_SUFFIX, Pattern.CASE_INSENSITIVE).matcher("");
			mDenominalSuffix = Pattern.compile(DENOMINAL_SUFFIX, Pattern.CASE_INSENSITIVE).matcher("");
			mDeadjectivalSuffix = Pattern.compile(DEADJECTIVAL_SUFFIX, Pattern.CASE_INSENSITIVE).matcher("");
			mDeverbalSuffix = Pattern.compile(DEVERBAL_SUFFIX, Pattern.CASE_INSENSITIVE).matcher("");
			mTimeOrderPrefix = Pattern.compile(TIMEORDER_PREFIX, Pattern.CASE_INSENSITIVE).matcher("");
			mDegPrefix = Pattern.compile(DEGREE_PREFIX, Pattern.CASE_INSENSITIVE).matcher("");
			mLocPrefix = Pattern.compile(LOC_PREFIX, Pattern.CASE_INSENSITIVE).matcher("");
			mNumPrefix = Pattern.compile(NUM_PREFIX, Pattern.CASE_INSENSITIVE).matcher("");
			mFrontBackSide = Pattern.compile(FRONT_BACK_SIDE, Pattern.CASE_INSENSITIVE).matcher("");
			mNegativePrefix = Pattern.compile(NEGATIVE_PREFIX, Pattern.CASE_INSENSITIVE).matcher("");
			mColorPrefix = Pattern.compile(COLOR_PREFIX, Pattern.CASE_INSENSITIVE).matcher("");
			mCauseToBecomeSuffix = Pattern.compile(CAUSE_TO_BECOME_SUFFIX, Pattern.CASE_INSENSITIVE).matcher("");
			mParticipleSuffix = Pattern.compile(PARTICIPLE_SUFFIX, Pattern.CASE_INSENSITIVE).matcher("");
			mVowelSuffix = Pattern.compile(VOWEL_SUFFIX, Pattern.CASE_INSENSITIVE).matcher("");
			mStartsWithA = Pattern.compile(STARTS_WITH_A, Pattern.CASE_INSENSITIVE).matcher("");
			mBodySuffix = Pattern.compile(BODY_PREFIX, Pattern.CASE_INSENSITIVE).matcher("");
			mDiseaseSuffix = Pattern.compile(DISEASE_SUFFIX, Pattern.CASE_INSENSITIVE).matcher("");
			mHavingSuffix = Pattern.compile(HAVING_SUFFIX, Pattern.CASE_INSENSITIVE).matcher("");
			mAbleSuffix = Pattern.compile(ABLE_SUFFIX, Pattern.CASE_INSENSITIVE).matcher("");
			mRelatedToSuffix = Pattern.compile(RELATEDTO_SUFFIX, Pattern.CASE_INSENSITIVE).matcher("");
			mEnSuffix = Pattern.compile(EN_SUFFIX, Pattern.CASE_INSENSITIVE).matcher("");
			mEscentSuffix = Pattern.compile(ESCENT_SUFFIX, Pattern.CASE_INSENSITIVE).matcher("");
			mOidSuffix = Pattern.compile(OID_SUFFIX, Pattern.CASE_INSENSITIVE).matcher("");
			mSuperlativeSuffix = Pattern.compile(SUPERLATIVE_SUFFIX, Pattern.CASE_INSENSITIVE).matcher("");
			mAbsenceSuffix = Pattern.compile(ABSENCE_SUFFIX, Pattern.CASE_INSENSITIVE).matcher("");
			mLikeSuffix = Pattern.compile(LIKE_SUFFIX, Pattern.CASE_INSENSITIVE).matcher("");
		}
	}
	
	@Override
	public Set<String> getProductions(String term, String type, Set<String> features) {
		
		
		synchronized(this) {
			reinit();
		// *front, *side hillside,waterfront
		boolean deverbalAgentSuffix = matches(mDeverbalAgent, term);
		boolean denomAgentSuffix = matches(mDenominalAgent, term);
		boolean denomSuffix = matches(mDenominalSuffix, term);
		boolean deadjSuffix = matches(mDeadjectivalSuffix, term);//term.matches();//ity
		boolean deverbSuffix = matches(mDeverbalSuffix, term);
		boolean timeOrOrderPrefix = matches(mTimeOrderPrefix, term);		
		boolean degreePrefix = matches(mDegPrefix, term);
		boolean locativePrefix = matches(mLocPrefix, term);
		// endo, ex, ante, circum, extra, fore, mid, retro, step, sub, super, trans, ?cata?, para, peri, supra, opistho
		boolean numberPrefix = matches(mNumPrefix, term);
		// semi, demi, hemi, sim, singul, prim, hen, haplo, tetra, eka, du, second, dy, dvi, sesqui, tessaro, 
		// ter, quad, quart, chatur, quin, penta, pancha, sexa, sen, sext, hexa, shat, spti,
		// hepta, sapta, octo, ogdo, octa, novem, noven, nona, ennea, navam
		// dec, undec, duo, dodeca, ekad, dvad, tredec, trayo, quat, chatur, sedec, sexdec,
		// septen, viginti, vicen, vigen, icosa, icosi, eicosa, centi, ducenti, milli, chili, myria, peta
		// ?haplo, hen?
		
		
		// anti, contra
		
		boolean frontBackSideAffix = matches(mFrontBackSide, term);
		boolean negativePrefix = matches(mNegativePrefix, term);
		
		
		
		boolean colorPrefix = matches(mColorPrefix, term);
		//if(colorPrefix) features.add("col:" + colorPrefix);
		
		//boolean pejorativePrefix = term.matches("(mis|mal|pseudo).+");
		//boolean conversionPrefix = term.matches("(be|en|a).+");
		//features.add("con:"+conversionPrefixLeft);
		//features.add("pej:"+pejorativePrefixLeft);
		if(negativePrefix) features.add("neg:"+negativePrefix);
		//features.add("rev:"+reversivePrefixLeft);
		
		//infra-
		
		if(timeOrOrderPrefix) features.add("to:");//+timeOrOrderPrefix);
		if(degreePrefix) features.add("deg:");// + degreePrefix);
		if(locativePrefix) features.add("loc:");//+locativePrefix);	
		if(numberPrefix) features.add("num:");//+numberPrefix);
		if(deverbalAgentSuffix) features.add("deva:");//+deverbalAgentSuffix);
		if(denomAgentSuffix) features.add("denoa:");//+denomAgentSuffix);
		if(denomAgentSuffix|deverbalAgentSuffix) features.add("age:");//+(denomAgentSuffix|deverbalAgentSuffix));
		if(denomSuffix) features.add("deno:");//+denomSuffix);
		if(deadjSuffix) features.add("dead:");//+deadjSuffix);
		if(deverbSuffix) features.add("dev:");//+deverbSuffix);
		
		if(frontBackSideAffix) features.add("fsb:");//+frontBackSideAffix);
		
		
		// verb suffixes
		boolean causeToBecomeSuffix = matches(mCauseToBecomeSuffix, term);
		if(causeToBecomeSuffix) features.add("ctb:");//+causeToBecomeSuffix);
		boolean participleSuffixes = matches(mParticipleSuffix, term);
		if(participleSuffixes) features.add("ps:");//+participleSuffixes);
		
		boolean endsInVowel = matches(mVowelSuffix, term);
		//boolean endsInS = term.endsWith("s");
		if(endsInVowel) features.add("ev:");// + endsInVowel);
		//if(endsInS) features.add("es:" + endsInS);
		boolean startsWithA = matches(mStartsWithA, term);
		if(startsWithA) features.add("sa:");// + startsWithA);
		
		doMedicalAffixes(term, features);
		doAdjectiveAffixes(term, features);
		//doNameAffixes(term, features);
		
		//boolean endsWithS = matches(mEndsWithS, term);
		//if(endsWithS) features.add("ss");
		}
		return features;
	}
	
	private void doMedicalAffixes(String term, Set<String> features) {
		boolean bodyPrefix = matches(mBodySuffix, term);
		if(bodyPrefix) features.add("bod:" + bodyPrefix);
		if(matches(mDiseaseSuffix, term)) features.add("dis");
		//if(term.matches("(thrombo|path|pero|ather|carcino|hernio)[a-zA-Z]+")) features.add("pdis");
		//if(term.matches("(iatro[a-zA-Z]*)|([a-zA-Z]*(ectomy|rrhaphy|pexy|opsy))")) features.add("op");
	}
	
	private void doAdjectiveAffixes(String term, Set<String> features) {
		// adjective suffixes
		boolean having = matches(mHavingSuffix, term);
		boolean able = matches(mAbleSuffix, term);
		boolean relatedTo = matches(mRelatedToSuffix, term); // -fic may be causing
		boolean en = matches(mEnSuffix, term);
		boolean escent = matches(mEscentSuffix, term);
		boolean oid = matches(mOidSuffix, term);
		boolean superlative = matches(mSuperlativeSuffix, term);
		boolean absence = matches(mAbsenceSuffix, term);
		boolean like = matches(mLikeSuffix, term);
		if(like) features.add("lik:");//+like);
		if(superlative) features.add("sup:");// + superlative);
		if(having) features.add("hv:");//+having);
		if(able) features.add("ble:");//+able);
		if(relatedTo) features.add("rel:");//+relatedTo);
		if(en) features.add("en:");//+en);
		if(escent) features.add("esc:");//+escent);
		if(oid) features.add("oid:");//+oid);
		if(absence) features.add("abs:");// + absence);
	}
	

}