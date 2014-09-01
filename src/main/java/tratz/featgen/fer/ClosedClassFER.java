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
 * Feature Extraction Rule that has a bunch of hard-coded lexicons
 *
 */
public class ClosedClassFER extends AbstractFeatureRule {
	public final static long serialVersionUID = 1;
	
	//incidentally, conversely
	//private final static String DISCOURSE_CONNECTIVE_ADVS = "alas|meanwhile|nevertheless|nonetheless|otherwise|likewise|therefore|accordingly|consequently|furthermore|hence|however|thus|still|then|also|besides|moreover|wherefore"; // so also
	
	private final static String CORRELATIVE_CONJUNCTIVES = "either|neither|both";
	
	private final static String PERSONAL_PRONOUN = "he|she|me|i|we|us|him|'im|her|'er|they|them|'em";
	private final static String RELATIVE_PRONOUN = "that|who|which";// personally i think 'when' qualifies
	private final static String INDEFINITE_PRONOUN = "something|anything|nothing|everything"+
													 "somebody|anybody|nobody|everybody"+
													 "someone|anyone|none|everyone"+
													 
													 //"someplace|nowhere|sometime"+
													 // these don't occur with 'else' so they probably should moved elsewhere
													 "another|other|few|several|both|others|some|none|most|more|any|all";
	private final static String WHPRONOUN = "whoever|whatever|whichever|who|what|where|how|which|whose"; // seems a bit buggy to me..., should include when
	private final static String WHADVERB = "how|when|wherever|whenever|whence|whither|where|why|whereof";
	
	private final static String CURRENCY_SYMBOLS = "(A|H|HK|C|NZ)?\\$|¢|£|#|¤|¥|₠|₡|₢|₣|₤|₦|₧|₨|₩|₪|₫|€|₭|₮|₯|₰|₱|₲|₳|₴|₵";
	
	private final static String AUXILLARIES = "being|be|been|was|were|is|ai|are|am|'s|'re|do|did|does|doth|has|have|'ve|had|having|'d|hath";
	private final static String MODALS = "can|could|will|'ll|wo|would|'d|shall|shalt|should|may|might|must";
	// foreign prep 'en','cum' added
	// than, 
	// that as preposition???
	// removed 'plus, regarding, and respecting'
	private final static String PREPOSITIONS = "unto|cum|circa|aside|en|above|aboard|about|absent|across|after|against|along|alongside|amid|amidst|among|amongst|around|as|astride|at|atop|athwart|before|behind|below|beneath|beside|between|beyond|by|despite|down|during|except|pending|for|from|like|in|inside|into|minus|modulo|near|notwithstanding|of|off|on|onboard|onto|opposite|out|outside|outwith|over|pace|past|per|round|sans|than|through|thru|throughout|to|toward|towards|unlike|under|underneath|until|up|upon|upside|versus|via|with|within|without";
	//private final static String TOPIC_PREPS = "about|on|over|into|regarding|respecting|around";
	
	private final static String ARTICLE = "a|the|an";
	private final static String DEMONSTRATIVE = "this|that|these|those|which";
	private final static String PERSONAL_POSSESSIVE = "my|our|your|his|her|thy|its";
	// mine, thine, yours, his, hers, theirs, its
	private final static String UNIVERSAL_DETERMINER = "all|both";
	private final static String DISTRIBUTIVE_DETERMINER = "every|each"; // any behaves more like these two
	private final static String DEGREE_DETERMINER = "many|much|few|little";
	private final static String EXISTENTIAL_DETERMINER = "some|several|any|no";
	private final static String DISJUNCTIVE_DETERMINER = "either|neither";
	private final static String ALTERNATIVEADDITIONAL_DETERMINER = "another|other";
	
	private final static String QUANTIFIER_DETERMINER = UNIVERSAL_DETERMINER+"|"+DISTRIBUTIVE_DETERMINER+"|" + DEGREE_DETERMINER + "|" + EXISTENTIAL_DETERMINER;
	private Matcher mCoordinatingConj = Pattern.compile("minus|plus|times|and|or|but|yet|so|\\&", Pattern.CASE_INSENSITIVE).matcher("");
	// removed how, added for, added that
	private Matcher mSubordinatingConj = Pattern.compile("that|for|wherein|whereby|whereas|whereupon|wherewith|wherefore|lest|whether|than|when|where|'till|'til|till|til|until|if|unless|since|because|while|though|although|so|before|after|cos|'cos|'cause|whilst|whenever|lest", Pattern.CASE_INSENSITIVE).matcher("");
	private Matcher mCorrelativeConjunctives = Pattern.compile(CORRELATIVE_CONJUNCTIVES, Pattern.CASE_INSENSITIVE).matcher("");
	
	private Matcher mFirstPerson = Pattern.compile("I|we|us|my|myself|our|ourselves|ours", Pattern.CASE_INSENSITIVE).matcher("");
	private Matcher mSecondPerson = Pattern.compile("you|your|yourself|yourselves|yours|thyself", Pattern.CASE_INSENSITIVE).matcher("");
	private Matcher mThirdPerson = Pattern.compile("he|she|they|it|him|'m|her|hers|them|'em|herself|'imself|himself|themselves|ourselves", Pattern.CASE_INSENSITIVE).matcher("");
	
	private Matcher mAccusative = Pattern.compile("you|thee|me|us|him|'im|her|'er|them|'em|yourself|yourselves|thyself|myself|herself|'imself|himself|themselves|ourselves|it|itself", Pattern.CASE_INSENSITIVE).matcher("");
	private Matcher mNominative = Pattern.compile("you|thou|ye|i|he|she|they|we|it", Pattern.CASE_INSENSITIVE).matcher("");
	
	private Matcher mPronouns = Pattern.compile(PERSONAL_PRONOUN+"|"+RELATIVE_PRONOUN+"|"+INDEFINITE_PRONOUN, Pattern.CASE_INSENSITIVE).matcher("");
	private Matcher mPersonalPronoun = Pattern.compile(PERSONAL_PRONOUN, Pattern.CASE_INSENSITIVE).matcher("");
	//private Matcher mImpersonalPronoun = Pattern.compile(IMPERSONAL_PRONOUN, Pattern.CASE_INSENSITIVE).matcher("");
	private Matcher mRelativePronouns = Pattern.compile(RELATIVE_PRONOUN, Pattern.CASE_INSENSITIVE).matcher("");
	private Matcher mWHPronoun = Pattern.compile(WHPRONOUN, Pattern.CASE_INSENSITIVE).matcher("");
	private Matcher mWHAdverb = Pattern.compile(WHADVERB, Pattern.CASE_INSENSITIVE).matcher("");
	
	private Matcher mPrepositions = Pattern.compile(PREPOSITIONS, Pattern.CASE_INSENSITIVE).matcher("");
	//private Matcher mTopicPrepositions = Pattern.compile(TOPIC_PREPS, Pattern.CASE_INSENSITIVE).matcher("");
	// spatial preps
	// temporal preps
	// other preps?
	
	private Matcher mDeterminer = Pattern.compile(ARTICLE+"|"+DEMONSTRATIVE+"|"+DISJUNCTIVE_DETERMINER+"|"+QUANTIFIER_DETERMINER+"|"+ALTERNATIVEADDITIONAL_DETERMINER+"|"+PERSONAL_POSSESSIVE+"|their|its|", Pattern.CASE_INSENSITIVE).matcher("");
	private Matcher mArticle = Pattern.compile(ARTICLE, Pattern.CASE_INSENSITIVE).matcher("");
	private Matcher mDemonstratives = Pattern.compile(DEMONSTRATIVE, Pattern.CASE_INSENSITIVE).matcher("");
	private Matcher mDisjunctiveDeterminer = Pattern.compile(DISJUNCTIVE_DETERMINER, Pattern.CASE_INSENSITIVE).matcher("");
	
	//private Matcher mEvaluativeDeterminer = Pattern.compile("such", Pattern.CASE_INSENSITIVE).matcher("");
	//private Matcher mExclamativeDeterminer = Pattern.compile("what", Pattern.CASE_INSENSITIVE).matcher("");
	private Matcher mAlternativeAdditive = Pattern.compile(ALTERNATIVEADDITIONAL_DETERMINER, Pattern.CASE_INSENSITIVE).matcher("");
	private Matcher mPossessiveDeterminer = Pattern.compile(PERSONAL_POSSESSIVE+"|their|its", Pattern.CASE_INSENSITIVE).matcher("");
	
	private Matcher mQuantifierDeterminer = Pattern.compile(QUANTIFIER_DETERMINER, Pattern.CASE_INSENSITIVE).matcher("");
	private Matcher mDistributiveDeterminer = Pattern.compile(DISTRIBUTIVE_DETERMINER, Pattern.CASE_INSENSITIVE).matcher("");
	private Matcher mUniversalDeterminer = Pattern.compile(UNIVERSAL_DETERMINER, Pattern.CASE_INSENSITIVE).matcher("");
	private Matcher mExistentialDeterminer = Pattern.compile(EXISTENTIAL_DETERMINER, Pattern.CASE_INSENSITIVE).matcher("");
	private Matcher mDegreeDeterminer = Pattern.compile(DEGREE_DETERMINER, Pattern.CASE_INSENSITIVE).matcher("");
	
	// Modals
	private Matcher mModal = Pattern.compile(MODALS, Pattern.CASE_INSENSITIVE).matcher("");
	private Matcher mAux = Pattern.compile(AUXILLARIES, Pattern.CASE_INSENSITIVE).matcher("");
	// Personal Possessives
	private Matcher mPersonalPossessive = Pattern.compile(PERSONAL_POSSESSIVE, Pattern.CASE_INSENSITIVE).matcher("");
	
	// Discourse Connectives
	//private Matcher mDiscourseConnectives = Pattern.compile(DISCOURSE_CONNECTIVE_ADVS, Pattern.CASE_INSENSITIVE).matcher("");

	private Matcher mCurrencySymbol = Pattern.compile(CURRENCY_SYMBOLS).matcher("");
	
	//private Matcher mSentenceEnders = Pattern.compile("\\!|\\.|\\?").matcher("");
	
	public ClosedClassFER() {
		
	}
	
	private final static boolean match(String text, Matcher matcher) {
		matcher.reset(text);
		return matcher.matches();
	}
	
	@Override
	public Set<String> getProductions(String input, String type, Set<String> fs) {
		synchronized(this) {
		if(match(input, mPrepositions)) fs.add("IN");
		
		//if(match(input, mWHAdverb)) fs.add("WRB");
		if(match(input, mCurrencySymbol)) fs.add("$");
		//if(match(input, mSentenceEnders)) fs.add(".!?");
		if(match(input, mModal)) fs.add("MOD");
		if(match(input, mAux)) fs.add("AUX");
		//if(match(input, mCorrelativeConjunctives)) fs.add("corr");
		//if(match(input, mDiscourseConnectiveAdv)) fs.add("dca");
		
		//if(match(input, mFirstPerson)) fs.add("1pos");
		//else if(match(input, mSecondPerson)) fs.add("2pos");
		//else if(match(input, mThirdPerson)) fs.add("3pos");
		
		//if(match(input, mCoordinatingConj)) fs.add("cc");
		
		if(match(input, mSubordinatingConj)) fs.add("sc");
		if(match(input, mAccusative)) fs.add("acc");
		else if(match(input, mNominative)) fs.add("nom");
		
		
		//else 
			if(match(input, mPronouns)) {
			//features.add("pron");
			if(match(input, mRelativePronouns)) {
				//fs.add("rprn");
			}
			else if(match(input, mPersonalPronoun)) {
				fs.add("pprn");
			}
			/*else if(match(input, mImpersonalPronoun)) {
				features.add("iprn");
			}*/
			else if(match(input, mWHPronoun)) {
				//fs.add("wprn");
			}
		}
		if(match(input, mPersonalPossessive)) fs.add("pposs");
		
		
		//if(match(input, mDiscourseConnectives)) fs.add("dc");
		/*if(match(input, mDeterminer)) {
			//features.add("det");
			if(match(input, mArticle)) {
				fs.add("art");
			}
		//else
			if(match(input, mDemonstratives)) fs.add("demo");
			else if(match(input, mDisjunctiveDeterminer)) fs.add("disj");
			else if(match(input, mAlternativeAdditive)) fs.add("aa");
			else if(match(input, mPossessiveDeterminer)) fs.add("pdet");
			else if(match(input, mQuantifierDeterminer)) {
				fs.add("qdet");
				if(match(input, mUniversalDeterminer)) fs.add("univ");
				else if(match(input, mDistributiveDeterminer)) fs.add("dist");	
				else if(match(input, mExistentialDeterminer)) fs.add("exis");
				else if(match(input, mDegreeDeterminer)) fs.add("deg");
			}
		}*/
		if(input.equalsIgnoreCase("n't") || input.equalsIgnoreCase("not")) {
			fs.add("not");
		}
		}
		return fs;
	}
}