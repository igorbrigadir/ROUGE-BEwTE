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

public class LexPointer extends Pointer {
	
	private byte mTargetWord;
	private byte mSourceWord;
	
	public LexPointer(WordNet wordNet, int targetSynsetOffset, PointerType pointerType, POS targetPos, byte sourceWord, byte targetWord) {
		super(wordNet, targetSynsetOffset, pointerType, targetPos);
		mTargetWord = targetWord;
	}
	
	public byte getTargetWordIndex() {
		return mTargetWord;
	}
	
	public byte getSourceWordIndex() {
		return mSourceWord;
	}
}