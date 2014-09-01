package foal.set;

import java.io.Serializable;

import foal.list.DoubleArrayList;
import foal.map.DoubleIntHashMap;
import foal.map.HashFunctions;

/**
 * Set for containing a collection of doubles
 * @see java.util.Set
 */
public class DoubleSet implements Serializable {
	public final static long serialVersionUID = 1;
	
	private DoubleIntHashMap mMap;
	
	public DoubleSet() {
		mMap = new DoubleIntHashMap();
	}
	
	public DoubleSet(int initialCapacity) {
		mMap = new DoubleIntHashMap(initialCapacity);
	}
	
	public DoubleSet(DoubleSet otherSet) {
		mMap = new DoubleIntHashMap(otherSet.size());
		for(double x : otherSet.toArray()) {
			add(x);
		}
	}
	
	public DoubleSet(double[] values) {
		mMap = new DoubleIntHashMap(values.length);
		for(double x : values) {
			add(x);
		}
	}
	
	public DoubleSet(DoubleArrayList doubleList) {
		this(doubleList.elements());
	}
	
	public void add(double x) {
		mMap.put(x, 1);
	}
	
	public void addAll(double[] array) {
		for(double x : array) {
			mMap.put(x, 1);
		}
	}
	
	public void addAll(DoubleSet doubleSet) {
		addAll(doubleSet.mMap.keys().elements());
	}
	
	public void addAll(DoubleArrayList list) {
		addAll(list.elements());
	}
	
	public void clear() {
		mMap.clear();
	}
	
	public boolean contains(double x) {
		return mMap.containsKey(x);
	}
	
	public boolean containsAll(double[] array) {
		boolean containsAll = true;
		for(double x : array) {
			if(!contains(x)) {
				containsAll = false;
				break;
			}
		}
		return containsAll;
	}
	
	public boolean containsAll(DoubleSet doubleSet) {
		return containsAll(doubleSet.toArrayQuick());
	}
	
	public boolean containsAll(DoubleArrayList list) {
		return containsAll(list.elements());
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof DoubleSet && ((obj == this) || (((DoubleSet)obj).size() == this.size() && ((DoubleSet)obj).containsAll(this)));
	}
	
	@Override
	public int hashCode() {
		int sumOfHashes = 0;
		for(double x : mMap.keys().elements()) {
			sumOfHashes += HashFunctions.hash(x);
		}
		return sumOfHashes;
	}
	
	public boolean remove(double x) {
		return mMap.removeKey(x);
	}
	
	public boolean removeAll(double[] array) {
		boolean setChanged = false;
		for(double x : array) {
			setChanged |= mMap.removeKey(x);
		}
		return setChanged;
	}
	
	public boolean removeAll(DoubleSet doubleSet) {
		return removeAll(doubleSet.toArray());
	}
	
	public boolean removeAll(DoubleArrayList list) {
		return removeAll(list.elements());
	}
	
	public boolean retainAll(DoubleArrayList list) {
		return retainAll(new DoubleSet(list.elements()));
	}
	
	public boolean retainAll(double[] array) {
		return retainAll(new DoubleSet(array));
	}
	
	public boolean retainAll(DoubleSet doubleSet) {
		boolean setChanged = false;
		double[] elements = mMap.keys().elements();
		double[] keysCopy = new double[size()];
		System.arraycopy(elements, 0, keysCopy, 0, size());
		for(double x : keysCopy) {
			if(!doubleSet.contains(x)) {
				remove(x);
				setChanged = true;
			}
		}
		return setChanged;
	}
	
	public int size() {
		return mMap.size();
	}
	
	public double[] toArray() {
		double[] elements = mMap.keys().elements();
		double[] keysCopy = new double[size()];
		System.arraycopy(elements, 0, keysCopy, 0, size());
		return keysCopy;
	}
	
	public double[] toArrayQuick() {
		return mMap.keys().elements();
	}
}