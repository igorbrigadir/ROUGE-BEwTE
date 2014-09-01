package foal.set;

import java.io.Serializable;

import foal.list.LongArrayList;
import foal.map.HashFunctions;
import foal.map.LongIntHashMap;

/**
 * Set for containing a collection of <code>long</code>s
 * @see java.util.Set
 */
public class LongSet implements Serializable {
	public final static long serialVersionUID = 1;
	
	private LongIntHashMap mMap;
	
	public LongSet() {
		mMap = new LongIntHashMap();
	}
	
	public LongSet(int initialCapacity) {
		mMap = new LongIntHashMap(initialCapacity);
	}
	
	public LongSet(LongSet otherSet) {
		mMap = new LongIntHashMap(otherSet.size());
		for(long x : otherSet.toArray()) {
			add(x);
		}
	}
	
	public LongSet(long[] values) {
		mMap = new LongIntHashMap(values.length);
		for(long x : values) {
			add(x);
		}
	}
	
	public LongSet(LongArrayList longList) {
		this(longList.elements());
	}
	
	public void add(long x) {
		mMap.put(x, 1);
	}
	
	public void addAll(long[] array) {
		for(long x : array) {
			mMap.put(x, 1);
		}
	}
	
	public void addAll(LongArrayList longList) {
		addAll(longList.elements());
	}
	
	public void addAll(LongSet longSet) {
		addAll(longSet.toArrayQuick());
	}
	
	public void clear() {
		mMap.clear();
	}
	
	public boolean contains(long x) {
		return mMap.containsKey(x);
	}
	
	public boolean containsAll(long[] array) {
		boolean containsAll = true;
		for(long x : array) {
			if(!contains(x)) {
				containsAll = false;
				break;
			}
		}
		return containsAll;
	}
	
	public boolean containsAll(LongSet longSet) {
		return containsAll(longSet.toArrayQuick());
	}
	
	public boolean containsAll(LongArrayList longList) {
		return containsAll(longList.elements());
	}
	
	public boolean equals(Object obj) {
		return (obj == this) || (((IntSet)obj).size() == this.size() && ((LongSet)obj).containsAll(this));
	}
	
	public int hashCode() {
		int sumOfHashes = 0;
		for(long x : mMap.keys().elements()) {
			sumOfHashes += HashFunctions.hash(x);
		}
		return sumOfHashes;
	}
	
	public boolean remove(long x) {
		return mMap.removeKey(x);
	}
	
	public boolean removeAll(long[] longArray) {
		boolean setChanged = false;
		for(long x : longArray) {
			setChanged |= mMap.removeKey(x);
		}
		return setChanged;
	}
	
	public boolean removeAll(LongSet longSet) {
		return removeAll(longSet.toArrayQuick());
	}
	
	public boolean removeAll(LongArrayList longList) {
		return removeAll(longList.elements());
	}
	
	public boolean retainAll(LongSet longSet) {
		boolean setChanged = false;
		long[] elements = mMap.keys().elements();
		long[] keysCopy = new long[size()];
		System.arraycopy(elements, 0, keysCopy, 0, size());
		for(long x : keysCopy) {
			if(!longSet.contains(x)) {
				remove(x);
				setChanged = true;
			}
		}
		return setChanged;
	}
	
	public boolean retainAll(long[] longArray) {
		return retainAll(new LongSet(longArray));
	}
	
	public boolean retainAll(LongArrayList longList) {
		return retainAll(new LongSet(longList));
	}
	
	public int size() {
		return mMap.size();
	}
	
	public long[] toArray() {
		long[] elements = mMap.keys().elements();
		long[] keysCopy = new long[size()];
		System.arraycopy(elements, 0, keysCopy, 0, size());
		return keysCopy;
	}
	
	public long[] toArrayQuick() {
		return mMap.keys().elements();
	}
}