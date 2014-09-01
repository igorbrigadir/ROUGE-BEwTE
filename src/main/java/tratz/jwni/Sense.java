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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a WordNet synonym set.
 */
public class Sense {
	
	public static class Key {
		private String mLemma;
		private byte mLexId;
		private POS mPos;
		private Sense mSense;
		private int mTagCount;
		private String mSenseKey;
		private int[] mFrames = new int[0];
		private WordNet mWordNet;
		
		public Key(WordNet wordNet, String lemma, byte lexid, POS pos) {
			mWordNet = wordNet;
			mLemma = lemma;
			mLexId = lexid;
			mPos = pos;
		}
		
		public void addFrame(int frame) {
			int[] newFrameArray = new int[mFrames.length+1];
			System.arraycopy(mFrames, 0, newFrameArray, 0, mFrames.length);
			newFrameArray[mFrames.length] = frame;
			mFrames = newFrameArray;
		}
		
		public int[] getFrames() {
			return mFrames;
		}
		
		public String getLemma() {
			return mLemma;
		}
		
		public POS getPOS() {
			return mPos;
		}
		
		public byte getLexId() {
			return mLexId;
		}
		
		public String getLexFileName() {
			return mWordNet.getLexName(mSense.getLexFileNum());
		}
		
		public Sense getSense() {
			return mSense; 
		}
		
		public String toString() {
			return mSenseKey;
		}
		
		public int hashCode() {
			return toString().hashCode();
		}
		
		public int getTagCount() {
			return mTagCount;
		}
		
		void setSenseKey(String senseKey) {
			mSenseKey = senseKey;
		}
		
		void setTagCount(int tagCount) {
			mTagCount = tagCount;
		}
		
	}
	
	private int mOffset;
	private String mGloss;
	private String[] mGlossTerms;
	private byte mLexFileNum;
	private POS mPos;
	private Key[] mKeys;
	private LexPointer[][] mLexPointers;
	private Pointer[] mSemPointers;

	public Sense(int offset, String gloss, String[] glossTerms, byte lexfilenum, POS pos,
			Key[] keys, LexPointer[][] lexPointers, Pointer[] semPointers) {
		mOffset = offset;
		mGloss = gloss;
		mGlossTerms = glossTerms;
		mLexFileNum = lexfilenum;
		mPos = pos;
		mKeys = keys;
		for(Key key : keys) {
			key.mSense = this;
		}
		mLexPointers = lexPointers;
		mSemPointers = semPointers;
		
	}
	
	public int getOffset() {
		return mOffset;
	}
	
	public String getGloss() {
		return mGloss;
	}
	
	public String[] getGlossTerms() {
		return mGlossTerms;
	}
	
	public byte getLexFileNum() {
		return mLexFileNum;
	}
	
	public POS getPOS() {
		return mPos;
	}
	
	public Key[] getKeys() {
		return mKeys;
	}
	
	public Pointer[][] getLexPointers() {
		return mLexPointers;
	}
	
	public int getKeyIndex(String keyString) {
		for(int i = 0; i < mKeys.length; i++) {
			if(keyString.compareToIgnoreCase(mKeys[i].getLemma()) == 0) {
				return i;
			}
		}
		return -1;
	}
	
	public Pointer[] getPointers(PointerType ... pointerType) {
		Set<PointerType> ptrTypes = new HashSet<PointerType>(Arrays.asList(pointerType));
		List<Pointer> pointers = new ArrayList<Pointer>();
		for(LexPointer[] lptrs : mLexPointers) {
			for(LexPointer lptr : lptrs) {
				if(ptrTypes.contains(lptr.mPointerType)) {
					pointers.add(lptr);
				}
			}
		}
		for(Pointer ptr : mSemPointers) {
			if(ptrTypes.contains(ptr.mPointerType)) {
				pointers.add(ptr);
			}
		}
		return pointers.toArray(new Pointer[pointers.size()]);
	}
	
	public LexPointer[][] getLexPointers(PointerType ... pointerTypes) {
		Set<PointerType> pTypes = new HashSet<PointerType>(Arrays.asList(pointerTypes));
		List<LexPointer>[] pointers = new List[mLexPointers.length];
		for(int i = 0; i < mLexPointers.length; i++) {
			pointers[i] = new ArrayList<LexPointer>();
			for(int j = 0; j < mLexPointers[i].length; j++) {
				if(pTypes.contains(mLexPointers[i][j].getPointerType())) {
					pointers[i].add(mLexPointers[i][j]);
				}
			}
		}
		LexPointer[][] lPointers = new LexPointer[mLexPointers.length][];
		for(int i = 0; i < mLexPointers.length; i++) {
			lPointers[i] = pointers[i].toArray(new LexPointer[pointers[i].size()]);
		}
		return lPointers;
	}
	
	public Pointer[] getSemPointers() {
		return mSemPointers;
	}
	
	public Pointer[] getSemPointers(PointerType ... pointerTypes) {
		List<Pointer> pointers = new ArrayList<Pointer>();
		for(Pointer p : mSemPointers) {
			for(PointerType pType : pointerTypes) {
				if(p.mPointerType == pType) {
					pointers.add(p);
				}
			}
		}
		return pointers.toArray(new Pointer[pointers.size()]);
	}
	
	public String toString() {
		return Arrays.toString(getKeys()) + "\t" + getPOS() + "\t" + getOffset() + "\t" + getLexFileNum() + "\t" + getGloss();
	}
}