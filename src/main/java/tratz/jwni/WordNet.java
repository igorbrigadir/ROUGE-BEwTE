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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The main WordNet class. Currently it defines a global variable that can be used to access
 * WordNet. This may change in the future.
 */
public class WordNet {

	private static WordNet sInstance;
	public static WordNet getInstance() {
		return sInstance;
	}
	
	/**
	 * Map of noun sense offsets to noun <code>Sense</code>s 
	 */
	private Map<Integer, Sense> mNounToSenseMap = new HashMap<Integer, Sense>();
	
	/**
	 * Map of verb sense offsets to verb <code>Sense</code>s 
	 */
	private Map<Integer, Sense> mVerbToSenseMap = new HashMap<Integer, Sense>();
	
	/**
	 * Map of adjective sense offsets to adjective <code>Sense</code>s 
	 */
	private Map<Integer, Sense> mAdjToSenseMap = new HashMap<Integer, Sense>();
	
	/**
	 * Map of adverb sense offsets to adverb <code>Sense</code>s 
	 */
	private Map<Integer, Sense> mAdvToSenseMap = new HashMap<Integer, Sense>();
	
	private Map<String, Sense> mKeyHashToSense = new HashMap<String, Sense>();
	private Map<POS, Map<Integer, Sense>> mPosToSenseMap = new HashMap<POS, Map<Integer, Sense>>();
	
	private Map<POS, Map<String, IndexEntry>> mPosToIndexEntries = new HashMap<POS, Map<String, IndexEntry>>();
	
	private Map<Integer, String> mLexnamesMap = new HashMap<Integer, String>();
	
	private MorphoProcessor mMorpho;
	
	private final static Map<Integer, POS> INTEGER_TO_POS = new HashMap<Integer, POS>();
	private final static Map<String, POS> STRING_TO_POS = new HashMap<String, POS>();
	static {
		STRING_TO_POS.put("n", POS.NOUN);
		STRING_TO_POS.put("v", POS.VERB);
		STRING_TO_POS.put("a", POS.ADJECTIVE);
		STRING_TO_POS.put("s", POS.ADJECTIVE);
		STRING_TO_POS.put("r", POS.ADVERB);
		INTEGER_TO_POS.put(1, POS.NOUN);
		INTEGER_TO_POS.put(2, POS.VERB);
		INTEGER_TO_POS.put(3, POS.ADJECTIVE);
		INTEGER_TO_POS.put(4, POS.ADVERB);
		INTEGER_TO_POS.put(5, POS.ADJECTIVE);
	}
	
	public WordNet(String base) throws IOException {
		this(URI.create(base + File.separator+"index.sense").toURL(), 
			URI.create(base + File.separator+"data.noun").toURL(), 
			URI.create(base + File.separator+"data.verb").toURL(), 
			URI.create(base + File.separator+"data.adj").toURL(), 
			URI.create(base + File.separator+"data.adv").toURL(),
			URI.create(base + File.separator+"index.noun").toURL(), 
			URI.create(base + File.separator+"index.verb").toURL(), 
			URI.create(base + File.separator+"index.adj").toURL(),
			URI.create(base + File.separator+"index.adv").toURL(),
			URI.create(base + File.separator+"lexnames").toURL(),
			true);
		mMorpho = new MorphoProcessor(this, base);
		sInstance = this;
	}
	
	public WordNet(File base) throws IOException {
		this(base, true);
	}
	
	public WordNet(File base, boolean includeGloss) throws IOException {
		this(new File(base, "index.sense").toURI().toURL(), 
			new File(base,"data.noun").toURI().toURL(), 
			new File(base, "data.verb").toURI().toURL(), 
			new File(base, "data.adj").toURI().toURL(), 
			new File(base, "data.adv").toURI().toURL(),
			new File(base, "index.noun").toURI().toURL(), 
			new File(base, "index.verb").toURI().toURL(), 
			new File(base, "index.adj").toURI().toURL(),
			new File(base, "index.adv").toURI().toURL(),
			new File(base, "lexnames").toURI().toURL(),
			includeGloss);
		mMorpho = new MorphoProcessor(this, base);
		sInstance = this;
	}
	
	private WordNet(URL indexURL, URL nounDataURL, URL verbDataURL, URL adjDataURL, URL advDataURL,
			URL nounIndexURL, URL verbIndexURL, URL adjIndexURL, URL advIndexURL, URL lexnamesURL, boolean includeGloss) throws IOException {
		//synset_offset  lex_filenum  ss_type  w_cnt  word  lex_id  [word  lex_id...]  p_cnt  [ptr...]  [frames...]  |   gloss
		mPosToSenseMap.put(POS.NOUN, mNounToSenseMap);
		mPosToSenseMap.put(POS.VERB, mVerbToSenseMap);
		mPosToSenseMap.put(POS.ADJECTIVE, mAdjToSenseMap);
		mPosToSenseMap.put(POS.ADVERB, mAdvToSenseMap);
		Map<String, String> canonicalStrings = new HashMap<String, String>();
		readDataBase(nounDataURL, mNounToSenseMap, includeGloss, canonicalStrings);
		readDataBase(verbDataURL, mVerbToSenseMap, includeGloss, canonicalStrings);
		readDataBase(adjDataURL, mAdjToSenseMap, includeGloss, canonicalStrings);
		readDataBase(advDataURL, mAdvToSenseMap, includeGloss, canonicalStrings);
		//lemma  pos  synset_cnt  p_cnt  [ptr_symbol...]  sense_cnt  tagsense_cnt   synset_offset  [synset_offset...] 
		mPosToIndexEntries.put(POS.NOUN, readIndex(nounIndexURL));
		mPosToIndexEntries.put(POS.VERB, readIndex(verbIndexURL));
		mPosToIndexEntries.put(POS.ADJECTIVE, readIndex(adjIndexURL));
		mPosToIndexEntries.put(POS.ADVERB, readIndex(advIndexURL));
		readSenseIndex(indexURL);
		readLexNames(lexnamesURL);
	}
	
	private void readLexNames(URL lexnamesURL) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(lexnamesURL.openStream()));
		String line = null;
		while((line = reader.readLine()) != null) {
			String[] split = line.trim().split("\\t+");
			if(split.length == 3) {
				mLexnamesMap.put(Integer.parseInt(split[0].trim()), split[1]);
			}
		}
		reader.close();
	}
	
	private void readSenseIndex(URL senseIndexURL) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(senseIndexURL.openStream()));
		String line = null;
		while((line = reader.readLine()) != null) {
			if(!line.trim().equals("")) {
				String[] split = line.split("\\s+");
				String wnSenseKey = split[0];
				
				int offset = Integer.parseInt(split[1]);
				int senseNum = Integer.parseInt(split[2])-1;
				int tagCount = Integer.parseInt(split[3]);
				int percentIndex = wnSenseKey.indexOf('%');
				String lemma = wnSenseKey.substring(0, percentIndex);
				int posInt = Integer.parseInt(wnSenseKey.substring(percentIndex+1, wnSenseKey.indexOf(':')));
				POS pos = INTEGER_TO_POS.get(posInt);
				Map<String, IndexEntry> lemmaToIndexEntry = mPosToIndexEntries.get(pos);
				IndexEntry iEntry = lemmaToIndexEntry.get(lemma);
				Sense sense = iEntry.getSenses()[senseNum];
				Sense.Key[] keys = sense.getKeys();
				for(Sense.Key key : keys) {
					if(key.getLemma().toLowerCase().equals(lemma)) {
						key.setTagCount(tagCount);
						key.setSenseKey(wnSenseKey);
						mKeyHashToSense.put(wnSenseKey, sense);
					}
				}
				
			}
		}
		reader.close();
	}
	
	public String getLexName(int lexId) {
		return mLexnamesMap.get(lexId);
	}
	
	private Map<String,IndexEntry> readIndex(URL indexURL) throws IOException {
		Map<String,IndexEntry> entries = new LinkedHashMap<String,IndexEntry>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(indexURL.openStream()));
		String line = null;
		while((line = reader.readLine()) != null) {
			if(line.startsWith("  ")) {
				
			}
			else {
				String[] split = line.split(" ");
				String lemma = split[0];
				POS pos = STRING_TO_POS.get(split[1]);
				int numSenses = Integer.parseInt(split[2]);
				int numPointerTypes = Integer.parseInt(split[3]);
				int[] senses = new int[numSenses];
				for(int i = 0; i < numSenses; i++) {
					senses[i] = Integer.parseInt(split[6+numPointerTypes+i]);
				}
				IndexEntry entry = new IndexEntry(this, pos, lemma, senses);
				entries.put(entry.getLemma(), entry);
			}
		}
		reader.close();
		return entries;
	}
	
	public MorphoProcessor getMorpho() {
		return mMorpho;
	}
	
	public Map<String, IndexEntry> getIndexWords(POS pos) {
		return mPosToIndexEntries.get(pos);
	}
	
	public Map<Integer, Sense> getSenses(POS pos) {
		return mPosToSenseMap.get(pos);
	}
	
	public IndexEntry getIndexEntry(POS pos, String lemma) {
		return mPosToIndexEntries.get(pos).get(lemma);
	}
	
	public IndexEntry lookupIndexEntry(POS pos, String lemma, boolean splitIfNecessary) {
		return mMorpho.lookupIndexEntry(pos, lemma, splitIfNecessary);
	}
	
	public IndexEntry lookupIndexEntry(POS pos, String lemma) {
		return lookupIndexEntry(pos, lemma, true);
	}
	
	public Sense getSense(String wnkey) {
		return mKeyHashToSense.get(wnkey);
	}

	public Sense getSenseAtOffset(POS pos, int offset) {
		return mPosToSenseMap.get(pos).get(offset);
	}
	
	private void readDataBase(URL dataURL, Map<Integer, Sense> senseMap, boolean includeGloss, Map<String, String> canonicalStrings) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(dataURL.openStream()));
		String line = null;
		while((line = reader.readLine()) != null) {
			if(line.startsWith("  ")) {
				// Skip
			}
			else {
				Sense sense = readSense(line, includeGloss, canonicalStrings);
				senseMap.put(sense.getOffset(), sense);
			}
		}
		reader.close();
	}
	
	public Sense readSense(String line, boolean includeGloss, Map<String, String> canonicalStrings) {
		int barIndex = line.indexOf('|');
		String preBar = line.substring(0, barIndex);
		String gloss = line.substring(barIndex+2);
		String[] glossTerms = null;
		if(!includeGloss) {
			gloss = null;
		}
		else {
			String[] terms = gloss.replaceAll("[^\\w]", " ").split("\\s+");
			glossTerms = new String[terms.length];
			for(int i = 0; i < terms.length; i++) {
				String term = terms[i];
				String canonicalVersion = canonicalStrings.get(term);
				if(canonicalVersion == null) {
					canonicalStrings.put(term, canonicalVersion = term);
				}
				term = canonicalVersion;
				glossTerms[i] = term;
			}
		}
		String[] split = preBar.split("\\s+");
		int offset = Integer.parseInt(split[0]);
		byte lexfilenum = Byte.parseByte(split[1]);
		POS sensePOS = STRING_TO_POS.get(split[2]);
		
		// Read in keys
		byte wordCount = Byte.parseByte(split[3], 16);
		List[] lexPointers = new List[wordCount];
		for(int i = 0; i < wordCount; i++) {
			lexPointers[i] = new LinkedList();
		}
		Sense.Key[] keys = new Sense.Key[wordCount];
		for(int i = 0; i < wordCount; i++) {
			String word = split[4+2*i];
			byte lexid = Byte.parseByte(split[4+2*i+1], 16);
			if(word.endsWith(")")) {
				if(word.endsWith("(a)") || word.endsWith("(p)")) {
					word = word.substring(0, word.length()-3);
				}
				else if(word.endsWith("(ip)")) {
					word = word.substring(0, word.length()-4);
				}
			}
			
			String canonicalVersion = canonicalStrings.get(word);
			if(canonicalVersion == null) {
				canonicalStrings.put(word, canonicalVersion = word);
			}
			word = canonicalVersion;
			
			keys[i] = new Sense.Key(WordNet.this, word, lexid, sensePOS);
		}
		
		int pointerCount = Integer.parseInt(split[4+wordCount*2]);
		//pointer_symbol  synset_offset  pos  source/target
		int firstPointer = 4+wordCount*2 + 1;
		
		List<Pointer> semPointers = new LinkedList<Pointer>();
		Set<PointerType> lexPointerTypes = new HashSet<PointerType>();
		for(int i = 0; i < pointerCount; i++) {
			String pointerSymbol = split[firstPointer + 4*i];
		    //System.err.println(pointerSymbol + "\t" + line);
			PointerType pointerType = Pointers.STRING_TO_POINTER_TYPE.get(pointerSymbol);
			int synsetOffset = Integer.parseInt(split[firstPointer + 4*i+1]);
			POS targetPOS = STRING_TO_POS.get(split[firstPointer + 4*i+2]);
			String sourceTarget = split[firstPointer + 4*i+3];
			byte source = Byte.parseByte(sourceTarget.substring(0,2), 16);
			byte target = Byte.parseByte(sourceTarget.substring(2,4), 16);
			if(source == 0) {
				if(target == 0) {
					// semantic pointer
					semPointers.add(new SemPointer(this, synsetOffset, pointerType, targetPOS));
				}
				else {
					System.err.println(this.getClass().getCanonicalName()+" says EEEEEEEEEEEEEEEEEK");
				}
			}
			else {
				source--;
				target--;
				List pointers = lexPointers[source];
				if(pointers == null) {
					lexPointers[source] = pointers = new LinkedList<Pointer>();
				}
				
				pointers.add(new LexPointer(this, synsetOffset, pointerType, targetPOS, source, target));
				lexPointerTypes.add(pointerType);
			}
		}

		// Read frames
		int frameIndex = 4+wordCount*2+4*pointerCount+1;
		if(split.length > frameIndex) {
			int numFrames = Integer.parseInt(split[frameIndex]);
			for(int i = 0; i < numFrames; i++) {
				int frameNum = Integer.parseInt(split[frameIndex+i*3+2]);
				int wordNum = Integer.parseInt(split[frameIndex+i*3+3], 16);
				if(wordNum == 0) {
					for(Sense.Key key : keys) {
						key.addFrame(frameNum);
					}
				}
				else {
					keys[wordNum-1].addFrame(frameNum);
				}
			}
		}

		LexPointer[][] lPointers = new LexPointer[wordCount][];
		for(int i = 0; i < wordCount; i++) {
			lPointers[i] = ((List<LexPointer>)lexPointers[i]).toArray(new LexPointer[0]);
		}
		
		return new Sense(offset, gloss, glossTerms, lexfilenum, sensePOS, keys, lPointers, semPointers.toArray(new SemPointer[0]));
	}
	
}