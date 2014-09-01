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

package tratz.types;

import java.io.Serializable;

/**
 * A growable array of <code>short</code>s
 */
public class ShortArrayList implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private short[] mData;
	private int mLength;
	
	public ShortArrayList() {
		this(10);
	}
	
	public ShortArrayList(int size) {
		mData = new short[size];
	}
	
	public void add(short b) {
		expandIfNecessary(mData, mLength);
		mData[mLength++] = b;
	}
	
	private void expandIfNecessary(short[] data, int neededLength) {
		if(data.length == neededLength) {
			int newSize = (data.length*3)/2+1;
			short[] newData = new short[newSize];
			System.arraycopy(data, 0, newData, 0, data.length);
			mData = newData;
		}
	}
	
	public short[] getData() {
		return mData;
	}
	
	public short[] toCompactArray() {
		short[] result = new short[mLength];
		System.arraycopy(mData, 0, result, 0, mLength);
		return result;
	}
	
	public short get(int i) {
		return mData[i];
	}
	
	public void set(int i, short b) {
		mData[i] = b;
	}
	
	public int size() {
		return mLength;
	}
	
}