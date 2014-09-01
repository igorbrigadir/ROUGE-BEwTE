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

package tratz.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public interface TreebankConstants {
	
	// Sentence
	public static final String DECLARATIVE_SENTENCE = "S";
	public static final String SBAR_CLAUSE = "SBAR";
	public static final String SBARQ_QUESTION = "SBARQ";
	public static final String INVERTED_DECLARATIVE_SENTENCE = "SINV";
	public static final String INVERTED_YES_NO_QUESTION = "SQ";
	
	// Phrases
	public static final String NOUN_PHRASE = "NP";
	public static final String VERB_PHRASE = "VP";
	public static final String ADJECTIVE_PHRASE = "ADJP";
	public static final String ADVERB_PHRASE = "ADVP";
	public static final String PREPOSITIONAL_PHRASE = "PP";

	public static final String WH_NOUN_PHRASE = "WHNP";
	public static final String QUANTIFIER_PHRASE = "QP";
	
	public static final String UNKNOWN_PHRASE = "X";
	
	public static final String UNLIKE_COORDINATED_PHRASE = "UCP";
	public static final String WH_ADJECTIVE_PHRASE = "WHADJP";
	public static final String WH_ADVERB_PHRASE = "WHADVP";
	public static final String WH_PREPOSITIONAL_PHRASE = "WHPP";
	public static final String CONJUNCTION_PHRASE = "CONJP";
	public static final String INTERJECTION_PHRASE = "INTJ";
	public static final String PARENTHETICAL = "PRN";
	public static final String FRAGMENT = "FRAG";
	public static final String LIST_MARKER = "LST";
	public static final String NOT_A_CONSTITUENT = "NAC";
	public static final String NOUN_HEAD = "NX";
	public static final String REDUCED_RELATIVE_CLAUSE = "RRC";
	
	//	 Verbs
	public static final String VERB_BASE_FORM = "VB";
	public static final String VERB_PAST_TENSE = "VBD";
	public static final String VERB_GERUND_OR_PRESENT_PARTICIPLE = "VBG";
	public static final String VERB_PAST_PARTICIPLE = "VBN";
	public static final String NON_THIRD_PERSON_SINGULAR_PRESENT_VERB = "VBP";
	public static final String THIRD_PERSON_SINGULAR_PRESENT_VERB = "VBZ";
	public static final String MODAL = "MD";
	public static final String AUXILLARY = "AUX";

	// Nouns
	public static final String NOUN_SINGULAR = "NN";
	public static final String NOUN_PLURAL = "NNS";
	public static final String NOUN_PROPER_SINGULAR = "NNP";
	public static final String NOUN_PROPER_PLURAL = "NNPS";
	public static final String CARDINAL_NUMBER = "CD";
	
	//	 Adjectives
	public static final String ADJECTIVE = "JJ";
	public static final String ADJECTIVE_COMPARATIVE = "JJR";
	public static final String ADJECTIVE_SUPERLATIVE = "JJS";

	//	 Adverbs
	public static final String ADVERB = "RB";
	public static final String ADVERB_COMPARATIVE = "RBR";
	public static final String ADVERB_SUPERLATIVE = "RBS";
	public static final String WH_ADVERB = "WRB";
	
	// Pronouns
	public static final String PRONOUN_PERSONAL = "PRP";
	public static final String PRONOUN_POSSESSIVE = "PRP$";
	public static final String POSSESSIVE_WH_PRONOUN = "WP$";
	public static final String WH_PRONOUN = "WP";
	
	// Determiner
	public static final String DETERMINER = "DT";
	public static final String PREDETERMINER = "PDT";
	public static final String WH_DETERMINER = "WDT";
	
	//	 Prepositions
	public static final String TO = "TO";
	public static final String PREPOSITION = "IN";
	
	public static final String PARTICLE = "PRT";
	
	public static final String COORDINATING_CONJUCTION = "CC";
	public static final String EXISTENTIAL_THERE = "EX";
	public static final String FOREIGN_WORD = "FW";
	
	public static final String POSSESSIVE_ENDING = "POS";
	public static final String LIST_ITEM_MARKER = "LS";
	
	public static final String PARTICIPLE = "RP";
	public static final String SYMBOL = "SYM";
	
	public static final String INTERJECTION = "UH";
	
	// Punctuation
	public static final String RIGHT_CURLY_BRACE = "-RCB-";
	public static final String RIGHT_SQUARE_BRACE = "-RSB-";
	public static final String RIGHT_ROUND_BRACE = "-RRB-";
	public static final String LEFT_ROUND_BRACE = "-LRB-";
	public static final String LEFT_SQUARE_BRACE = "-LSB-";
	public static final String LEFT_CURLY_BRACE = "-LCB-";
	public static final String LEFT_DOUBLE_QUOTE = "``";
	public static final String RIGHT_DOUBLE_QUOTE = "''";
	
	// Form/function discrepancies
	public static final String ADVERBIAL = "ADV";
	public static final String NOMINAL = "NOM";
	
	// Grammatical roles
	public static final String DATIVE = "DTV";
	public static final String LOGICAL_SUBJECT = "LGS";
	public static final String PREDICATE = "PRD";
	public static final String PUT = "PUT";
	public static final String SUBJECT = "SBJ";
	public static final String TOPICALIZED = "TPC";
	public static final String VOCATIVE = "VOC";
	
	// Adverbials
	public static final String MODIFIER_NOMINAL_RIGHT = "MNR";
	public static final String BENEFACTIVE = "BNF";
	public static final String DIRECTION = "DIR";
	public static final String EXTENT = "EXT";
	public static final String LOCATIVE = "LOC";
	public static final String PURPOSE = "PRP";
	public static final String TEMPORAL = "TMP";
	
	// Misc
	public static final String CLOSELY_RELATED = "CLR";
	public static final String CLEFT = "CLF";
	public static final String HEADLINE = "HLN";
	public static final String TITLE = "TTL";
	
	
	public static final Set<String> PHRASES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
			NOUN_PHRASE,
			VERB_PHRASE,
			ADJECTIVE_PHRASE,
			PREPOSITIONAL_PHRASE,
			ADVERB_PHRASE,
			CONJUNCTION_PHRASE,
			INTERJECTION_PHRASE,
			PARTICLE,
			WH_NOUN_PHRASE,
			WH_PREPOSITIONAL_PHRASE,
			WH_ADJECTIVE_PHRASE,
			WH_ADVERB_PHRASE,
			QUANTIFIER_PHRASE,
			PARENTHETICAL,
			CONJUNCTION_PHRASE,
			UNLIKE_COORDINATED_PHRASE,
			FRAGMENT,
			INTERJECTION_PHRASE,
			NOUN_HEAD,
			NOT_A_CONSTITUENT, // not sure if this really belongs
			UNKNOWN_PHRASE,
			LIST_MARKER
	)));
	
	public static final Set<String> CLAUSES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
			DECLARATIVE_SENTENCE,
			SBAR_CLAUSE,
			SBARQ_QUESTION,
			INVERTED_DECLARATIVE_SENTENCE,
			INVERTED_YES_NO_QUESTION,
			REDUCED_RELATIVE_CLAUSE
	)));
	
	public static final Set<String> NOUN_LABELS = Collections.unmodifiableSet(new HashSet<String>(
			Arrays.asList(
					NOUN_SINGULAR,
					NOUN_PLURAL,
					NOUN_PROPER_SINGULAR,
					NOUN_PROPER_PLURAL,
					CARDINAL_NUMBER
			)));
	
	public static final Set<String> VERB_LABELS = Collections.unmodifiableSet(new HashSet<String>(
			Arrays.asList(
					VERB_BASE_FORM,
					VERB_PAST_TENSE,
					VERB_GERUND_OR_PRESENT_PARTICIPLE,
					VERB_PAST_PARTICIPLE,
					NON_THIRD_PERSON_SINGULAR_PRESENT_VERB,
					THIRD_PERSON_SINGULAR_PRESENT_VERB,
					MODAL,
					AUXILLARY
					
	)));
	
	public static final Set<String> ADJ_LABELS = Collections.unmodifiableSet(new HashSet<String>(
			Arrays.asList(
					ADJECTIVE,
					ADJECTIVE_COMPARATIVE,
					ADJECTIVE_SUPERLATIVE
			)));
	
	public static final Set<String> ADV_LABELS = Collections.unmodifiableSet(new HashSet<String>(
			Arrays.asList(
					ADVERB,
					ADVERB_COMPARATIVE,
					ADVERB_SUPERLATIVE,
					WH_ADVERB
			)));
	
	public static final Set<String> PRON_LABELS = Collections.unmodifiableSet(new HashSet<String>(
			Arrays.asList(
					PRONOUN_PERSONAL, PRONOUN_POSSESSIVE, POSSESSIVE_WH_PRONOUN, WH_PRONOUN				
			)));
	
	public static final Set<String> DET_LABELS = Collections.unmodifiableSet(new HashSet<String>(
			Arrays.asList(
					DETERMINER,
					PREDETERMINER,
					WH_DETERMINER					
			)));
	
	public static final Set<String> PREPOSITIONS = Collections.unmodifiableSet(new HashSet<String>(
			Arrays.asList(
					PREPOSITION, TO)));
			
	
	public static final Set<String> OTHER_WORDLEVEL_LABELS = Collections.unmodifiableSet(new HashSet<String>(
			Arrays.asList(
					PARTICLE,
					COORDINATING_CONJUCTION,
					EXISTENTIAL_THERE,
					FOREIGN_WORD,
					POSSESSIVE_ENDING,
					LIST_ITEM_MARKER,
					PARTICIPLE,
					SYMBOL,
					INTERJECTION			
	)));
	
	public static final Set<String> FORM_FUNCTION_DISCREPANCY = Collections.unmodifiableSet(new HashSet<String>(
			Arrays.asList(
					ADVERBIAL, NOMINAL
			)));
	
	public static final Set<String> GRAMMATICAL_ROLES = Collections.unmodifiableSet(new HashSet<String>(
			Arrays.asList(
					DATIVE, LOGICAL_SUBJECT, PREDICATE, PUT, SUBJECT, TOPICALIZED, VOCATIVE
			)));
	
	public static final Set<String> ADVERBIALS = Collections.unmodifiableSet(new HashSet<String>(
			Arrays.asList(
					MODIFIER_NOMINAL_RIGHT, BENEFACTIVE, DIRECTION, EXTENT, LOCATIVE, PURPOSE, TEMPORAL
			)));
	
	public static final Set<String> MISC_OTHER = Collections.unmodifiableSet(new HashSet<String>(
			Arrays.asList(
					CLOSELY_RELATED, CLEFT, HEADLINE, TITLE
			)));
	
	public static final Set<String> LEFT_BRACES = Collections.unmodifiableSet(new HashSet<String>(
			Arrays.asList(
					LEFT_CURLY_BRACE, LEFT_SQUARE_BRACE, LEFT_ROUND_BRACE
			)));
	
	public static final Set<String> RIGHT_BRACES = Collections.unmodifiableSet(new HashSet<String>(
			Arrays.asList(
					RIGHT_CURLY_BRACE, RIGHT_SQUARE_BRACE, RIGHT_ROUND_BRACE
			)));
	
	public static final Set<String> CLOSED_CLASS_TAGS = Collections.unmodifiableSet(new HashSet<String>(
			Arrays.asList(PREPOSITION, TO, PARTICLE, WH_ADVERB, WH_DETERMINER, WH_PRONOUN, PRONOUN_PERSONAL, PRONOUN_POSSESSIVE, EXISTENTIAL_THERE, PREDETERMINER, DETERMINER)));
	
	
}