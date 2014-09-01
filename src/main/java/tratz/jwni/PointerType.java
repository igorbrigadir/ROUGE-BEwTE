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

package tratz.jwni;

public enum PointerType {
	ANTONYM, 
	HYPERNYM, 
	INSTANCE_HYPERNYM, 
	HYPONYM, 
	INSTANCE_HYPONYM, 
	MEMBER_HOLONYM, 
	SUBSTANCE_HOLONYM,
	PART_HOLONYM,
	MEMBER_MERONYM,
	SUBSTANCE_MERONYM,
	PART_MERONYM, 
	ATTRIBUTE, 
	DERIVED_FORM, 
	DOMAIN_OF_SYNSET_TOPIC, 
	MEMBER_OF_THIS_DOMAIN_TOPIC, 
	DOMAIN_OF_SYNSET_REGION, 
	MEMBER_OF_THIS_DOMAIN_REGION,
	DOMAIN_OF_SYNSET_USAGE,
	MEMBER_OF_THIS_DOMAIN_USAGE, 
	ENTAILMENT, 
	CAUSE, 
	ALSO_SEE, 
	VERB_GROUP, 
	SIMILAR_TO, 
	PARTICIPLE_OF_VERB, 
	PERTAINYM};