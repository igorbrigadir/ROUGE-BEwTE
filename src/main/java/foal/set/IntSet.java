package foal.set;

import java.io.Serializable;

import foal.list.IntArrayList;
import foal.map.HashFunctions;
import foal.map.IntIntHashMap;

/**
 * Set for containing a collection of ints
 * @see java.util.Set
 */
public class IntSet implements Serializable {
	public final static long serialVersionUID = 1;
	
	private IntIntHashMap mMap;
	
	public IntSet() {
		mMap = new IntIntHashMap();
	}
	
	public IntSet(int initialCapacity) {
		mMap = new IntIntHashMap(initialCapacity);
	}
	
	public IntSet(IntSet otherSet) {
		mMap = new IntIntHashMap(otherSet.size());
		for(int x : otherSet.toArray()) {
			add(x);
		}
	}
	
	public IntSet(int[] values) {
		mMap = new IntIntHashMap(values.length);
		for(int x : values) {
			add(x);
		}
	}
	
	public IntSet(IntArrayList intList) {
		this(intList.elements());
	}
	
	public void add(int x) {
		mMap.put(x, 1);
	}
	
	public void addAll(int[] array) {
		for(int x : array) {
			mMap.put(x, 1);
		}
	}
	
	public void addAll(IntArrayList intList) {
		addAll(intList.elements());
	}
	
	public void addAll(IntSet intSet) {
		addAll(intSet.toArrayQuick());
	}
	
	public void clear() {
		mMap.clear();
	}
	
	public boolean contains(int x) {
		return mMap.containsKey(x);
	}
	
	public boolean containsAll(int[] array) {
		boolean containsAll = true;
		for(int x : array) {
			if(!contains(x)) {
				containsAll = false;
				break;
			}
		}
		return containsAll;
	}
	
	public boolean containsAll(IntSet intSet) {
		return containsAll(intSet.toArrayQuick());
	}
	
	public boolean containsAll(IntArrayList intList) {
		return containsAll(intList.elements());
	}
	
	public boolean equals(Object obj) {
		return (obj == this) || (((IntSet)obj).size() == this.size() && ((IntSet)obj).containsAll(this));
	}
	
	public int hashCode() {
		int sumOfHashes = 0;
		for(int x : mMap.keys().elements()) {
			sumOfHashes += HashFunctions.hash(x);
		}
		return sumOfHashes;
	}
	
	public boolean remove(int x) {
		return mMap.removeKey(x);
	}
	
	public boolean removeAll(int[] intArray) {
		boolean setChanged = false;
		for(int x : intArray) {
			setChanged |= mMap.removeKey(x);
		}
		return setChanged;
	}
	
	public boolean removeAll(IntSet intSet) {
		return removeAll(intSet.toArrayQuick());
	}
	
	public boolean removeAll(IntArrayList intList) {
		return removeAll(intList.elements());
	}
	
	public boolean retainAll(IntSet intSet) {
		boolean setChanged = false;
		int[] elements = mMap.keys().elements();
		int[] keysCopy = new int[size()];
		System.arraycopy(elements, 0, keysCopy, 0, size());
		for(int x : keysCopy) {
			if(!intSet.contains(x)) {
				remove(x);
				setChanged = true;
			}
		}
		return setChanged;
	}
	
	public boolean retainAll(int[] intArray) {
		return retainAll(new IntSet(intArray));
	}
	
	public boolean retainAll(IntArrayList intList) {
		return retainAll(new IntSet(intList));
	}
	
	public int size() {
		return mMap.size();
	}
	
	public int[] toArray() {
		int[] elements = mMap.keys().elements();
		int[] keysCopy = new int[size()];
		System.arraycopy(elements, 0, keysCopy, 0, size());
		return keysCopy;
	}
	
	public int[] toArrayQuick() {
		return mMap.keys().elements();
	}
}