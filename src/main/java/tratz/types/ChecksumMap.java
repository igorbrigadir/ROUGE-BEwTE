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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;

/**
 * Limited functionality map. Values are integers. Doesn't keep a copy of the keys, but instead keeps
 * a checksum. It will only currently work properly for Strings as keys. 
 */
public class ChecksumMap<K> implements Externalizable {
	
	public static final long serialVersionUID = 1L;

	public final static int DEFAULT_NOT_FOUND_VALUE = Integer.MIN_VALUE;
	public final static double DEFAULT_LOAD_FACTOR = .75;
	public final static int DEFAULT_START_SIZE = 32;

	private static class Node implements Serializable {
		public final static long serialVersionUID = 1L;
		public int checksum;
		public int val;
		public Node next;
		public int hash;

		public Node(int hash, int checksum, int value, Node next) {
			this.hash = hash;
			this.checksum = checksum;
			this.val = value;
			this.next = next;
		}
	}

	public interface HashFunction {
		public int hash(Object key);
	}

	public static class PassthroughHashFunction implements HashFunction,
			Serializable {
		public static final long serialVersionUID = 1L;

		public int hash(Object key) {
			return key.hashCode();
		}
	}

	// designed specifically for Strings
	// conceivably, it could interact poorly with other hash functions...
	public static class DefaultChecksumFunction implements HashFunction,
			Serializable {
		public static final long serialVersionUID = 1L;

		public int hash(Object key) {
			String s = (String)key;
			int h = 0;
			int len = s.length();
			for(int i = len - 1; i >= 0; i--) {
				h = 31 * h + s.charAt(i);
			}
			return h;
		}
	}

	protected int mNotFoundValue = DEFAULT_NOT_FOUND_VALUE;

	protected Node[] mRows;
	protected HashFunction mHashFunction;
	protected HashFunction mChecksumFunction;
	protected int mNumEntries;
	protected int mGrowThreshold;
	protected double mLoadFactor;

	public ChecksumMap() {
		this(new PassthroughHashFunction(), new DefaultChecksumFunction());
	}

	public ChecksumMap(HashFunction hashFunction, HashFunction checksumFunction) {
		mRows = new Node[DEFAULT_START_SIZE];
		mGrowThreshold = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_START_SIZE);
		mLoadFactor = DEFAULT_LOAD_FACTOR;
		mHashFunction = hashFunction;
		mChecksumFunction = checksumFunction;
	}

	public int put(final K key, final int value) {
		int retValue = mNotFoundValue;
		final int hash = mHashFunction.hash(key);
		final int checksum = mChecksumFunction.hash(key);
		final int row = hash & mRows.length - 1;

		// First try to find existing entry to update
		for(Node node = mRows[row]; node != null; node = node.next) {
			if(node.hash == hash && node.checksum == checksum) {
				final int oldValue = node.val;
				node.val = value;
				retValue = oldValue;
				break;
			}
		}
		// If failed to find existing entry, add a new entry
		if(retValue == mNotFoundValue) {
			addToRow(hash, checksum, value, row);
		}
		return retValue;
	}

	public int put(final int hash, final int checksum, final int value) {
		int retValue = mNotFoundValue;
		final int row = hash & mRows.length - 1;
		// First try to find existing entry to update
		for(Node node = mRows[row]; node != null; node = node.next) {
			if(node.hash == hash && node.checksum == checksum) {
				int oldValue = node.val;
				node.val = value;
				retValue = oldValue;
				break;
			}
		}
		// If failed to find existing entry, add a new entry
		if(retValue == mNotFoundValue) {
			addToRow(hash, checksum, value, row);
		}
		return retValue;
	}

	public static class TwoPartKey {
		public int hash, checksum;

		public TwoPartKey(int hash, int checksum) {
			this.hash = hash;
			this.checksum = checksum;
		}

		public int hash() {
			return hash;
		}
	}

	public Map<TwoPartKey, Integer> getKeyToIndexMap() {
		Map<TwoPartKey, Integer> map = new HashMap<TwoPartKey, Integer>();
		final int tableLength = mRows.length;
		for(int i = 0; i < tableLength; i++) {
			for(Node node = mRows[i]; node != null; node = node.next) {
				map.put(new TwoPartKey(node.hash, node.checksum), node.val);
			}
		}
		return map;
	}

	private void addToRow(final int hash, final int checksum,
			final int value, final int row) {
		mNumEntries++;
		Node currentNode = mRows[row];
		if(currentNode == null) {
			mRows[row] = new Node(hash, checksum, value, null);
		}
		else {
			while(currentNode.next != null) {
				currentNode = currentNode.next;
			}
			currentNode.next = new Node(hash, checksum, value, null);
		}
		if(mNumEntries >= mGrowThreshold) {
			doubleTableSize();
		}
	}

	public int remove(Object key) {
		final int hash = mHashFunction.hash(key);
		final int checksum = mChecksumFunction.hash(key);
		final int row = hash & mRows.length - 1;
		Node prev = mRows[row];

		Node node = prev;
		while(node != null) {
			if(node.hash == hash && node.checksum == checksum) {
				if(prev == node) {
					// First entry in row, need to update first pointer
					mRows[row] = node.next;
				}
				else {
					// Someplace else, need to fill the hole
					prev.next = node.next;
				}
				mNumEntries--;
				break;
			}
			else {
				prev = node;
				node = node.next;
			}
		}
		return node == null ? mNotFoundValue : node.val;
	}

	public int get(final K key) {
		int retValue = mNotFoundValue;
		final int hash = mHashFunction.hash(key);
		final int checksum = mChecksumFunction.hash(key);
		for(Node node = mRows[hash & mRows.length - 1]; node != null; node = node.next) {
			if(node.hash == hash && node.checksum == checksum) {
				retValue = node.val;
				break;
			}
		}
		return retValue;
	}

	public int get(final int hash, final int checksum) {
		int retValue = mNotFoundValue;
		for(Node node = mRows[hash & mRows.length - 1]; node != null; node = node.next) {
			if(node.hash == hash && node.checksum == checksum) {
				retValue = node.val;
				break;
			}
		}
		return retValue;
	}

	public boolean containsKey(K key) {
		return get(key) != mNotFoundValue;
	}

	public int size() {
		return mNumEntries;
	}

	public int getTableSize() {
		return mRows.length;
	}

	private void doubleTableSize() {
		final int newSize = mRows.length * 2;
		final Node[] newTable = new Node[newSize];
		final Node[] oldTable = mRows;
		final int newSizeMinusOne = newSize - 1;
		final int oldTableLength = oldTable.length;
		for(int i = 0; i < oldTableLength; i++) {
			Node node = oldTable[i];
			while(node != null) {
				Node nextEntry = node.next;
				int row = node.hash & newSizeMinusOne;
				node.next = newTable[row];
				newTable[row] = node;
				node = nextEntry;
			}
		}
		mRows = newTable;
		mGrowThreshold = (int)(newSize * mLoadFactor);
	}

	@Override
	public void readExternal(ObjectInput oi) throws IOException,
			ClassNotFoundException {
		mNumEntries = oi.readInt();
		mRows = new Node[mNumEntries];
		HashFunction hashFunction = (HashFunction)oi.readObject();
		HashFunction checksumFunction = (HashFunction)oi.readObject();
		mHashFunction = hashFunction;
		mChecksumFunction = checksumFunction;
		mNotFoundValue = oi.readInt();
		mLoadFactor = oi.readDouble();
		mGrowThreshold = oi.readInt();
		mRows = (Node[])oi.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput oo) throws IOException {
		oo.writeInt(mNumEntries);
		oo.writeObject(mHashFunction);
		oo.writeObject(mChecksumFunction);
		oo.writeInt(mNotFoundValue);
		oo.writeDouble(mLoadFactor);
		oo.writeInt(mGrowThreshold);
		oo.writeObject(mRows);
	}
	
}