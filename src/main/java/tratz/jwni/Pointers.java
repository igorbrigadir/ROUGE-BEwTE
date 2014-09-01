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

import java.util.HashMap;
import java.util.Map;

/**
 * Contains a hard-coded mapping of symbols that occur in the WordNet dictionary
 * files to the corresponding JWNI <code>PointerType</code>.
 */
public class Pointers {
	
	public static final Map<String, PointerType> STRING_TO_POINTER_TYPE;
	static {
		STRING_TO_POINTER_TYPE = new HashMap<String, PointerType>();
		STRING_TO_POINTER_TYPE.put("!",PointerType.ANTONYM);
		STRING_TO_POINTER_TYPE.put("@",PointerType.HYPERNYM);
		STRING_TO_POINTER_TYPE.put("@i",PointerType.INSTANCE_HYPERNYM);
		STRING_TO_POINTER_TYPE.put("~",PointerType.HYPONYM);
		STRING_TO_POINTER_TYPE.put("~i",PointerType.INSTANCE_HYPONYM);
		STRING_TO_POINTER_TYPE.put("#m",PointerType.MEMBER_HOLONYM);
		STRING_TO_POINTER_TYPE.put("#s",PointerType.SUBSTANCE_HOLONYM);
		STRING_TO_POINTER_TYPE.put("#p",PointerType.PART_HOLONYM);
		STRING_TO_POINTER_TYPE.put("%m",PointerType.MEMBER_MERONYM);
		STRING_TO_POINTER_TYPE.put("%s",PointerType.SUBSTANCE_MERONYM);
		STRING_TO_POINTER_TYPE.put("%p",PointerType.PART_MERONYM);
		STRING_TO_POINTER_TYPE.put("=",PointerType.ATTRIBUTE);
		STRING_TO_POINTER_TYPE.put("+",PointerType.DERIVED_FORM);
		STRING_TO_POINTER_TYPE.put(";c",PointerType.DOMAIN_OF_SYNSET_TOPIC);
		STRING_TO_POINTER_TYPE.put("-c",PointerType.MEMBER_OF_THIS_DOMAIN_TOPIC);
		STRING_TO_POINTER_TYPE.put(";r",PointerType.DOMAIN_OF_SYNSET_REGION);
		STRING_TO_POINTER_TYPE.put("-r",PointerType.MEMBER_OF_THIS_DOMAIN_REGION);
		STRING_TO_POINTER_TYPE.put(";u",PointerType.DOMAIN_OF_SYNSET_USAGE);
		STRING_TO_POINTER_TYPE.put("-u",PointerType.MEMBER_OF_THIS_DOMAIN_USAGE);
		STRING_TO_POINTER_TYPE.put("*",PointerType.ENTAILMENT);
		STRING_TO_POINTER_TYPE.put(">",PointerType.CAUSE);
		STRING_TO_POINTER_TYPE.put("^",PointerType.ALSO_SEE);
		STRING_TO_POINTER_TYPE.put("$",PointerType.VERB_GROUP);
		STRING_TO_POINTER_TYPE.put("&",PointerType.SIMILAR_TO);
		STRING_TO_POINTER_TYPE.put("<",PointerType.PARTICIPLE_OF_VERB);
		STRING_TO_POINTER_TYPE.put("\\",PointerType.PERTAINYM);
	}
	
}