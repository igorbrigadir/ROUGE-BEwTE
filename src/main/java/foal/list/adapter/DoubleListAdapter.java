/*
Copyright � 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
*/
package foal.list.adapter;

import foal.list.AbstractDoubleList;
/**
 * Adapter that permits an {@link foal.list.AbstractDoubleList} to be viewed and treated as a JDK 1.2 {@link java.util.AbstractList}.
 * Makes the contained list compatible with the JDK 1.2 Collections Framework.
 * <p>
 * Any attempt to pass elements other than <tt>java.lang.Number</tt> to setter methods will throw a <tt>java.lang.ClassCastException</tt>.
 * <tt>java.lang.Number.doubleValue()</tt> is used to convert objects into primitive values which are then stored in the backing templated list.
 * Getter methods return <tt>java.lang.Double</tt> objects.
 */
public class DoubleListAdapter extends java.util.AbstractList<Double> implements java.util.List<Double> {
	protected AbstractDoubleList content;
/**
 * Constructs a list backed by the specified content list.
 */
public DoubleListAdapter(AbstractDoubleList content) {
	this.content = content;
}
/**
 * Inserts the specified element at the specified position in this list
 * (optional operation).  Shifts the element currently at that position
 * (if any) and any subsequent elements to the right (adds one to their
 * indices).<p>
 *
 * @param index index at which the specified element is to be inserted.
 * @param element element to be inserted.
 * 
 * @throws ClassCastException if the class of the specified element
 * 		  prevents it from being added to this list.
 * @throws IllegalArgumentException if some aspect of the specified
 *		  element prevents it from being added to this list.
 * @throws IndexOutOfBoundsException index is out of range (<tt>index &lt;
 *		  0 || index &gt; size()</tt>).
 */
public void add(int index, Double element) {
	content.beforeInsert(index,value(element));
	modCount++;
}
/**
 * Returns the element at the specified position in this list.
 *
 * @param index index of element to return.
 * 
 * @return the element at the specified position in this list.
 * @throws IndexOutOfBoundsException if the given index is out of range
 * 		  (<tt>index &lt; 0 || index &gt;= size()</tt>).
 */
public Double get(int index) {
	return object(content.get(index));
}
/**
 * Transforms an element of a primitive data type to an object. 
 */
protected static Double object(double element) {
	return Double.valueOf(element);
}
/**
 * Removes the element at the specified position in this list (optional
 * operation).  Shifts any subsequent elements to the left (subtracts one
 * from their indices).  Returns the element that was removed from the
 * list.<p>
 *
 * @param index the index of the element to remove.
 * @return the element previously at the specified position.
 * 
 * @throws IndexOutOfBoundsException if the specified index is out of
 * 		  range (<tt>index &lt; 0 || index &gt;= size()</tt>).
 */
public Double remove(int index) {
	Double old = get(index);
	content.remove(index);
	modCount++;
	return old;
}
/**
 * Replaces the element at the specified position in this list with the
 * specified element (optional operation). <p>
 *
 * @param index index of element to replace.
 * @param element element to be stored at the specified position.
 * @return the element previously at the specified position.
 * 
 * @throws ClassCastException if the class of the specified element
 * 		  prevents it from being added to this list.
 * @throws IllegalArgumentException if some aspect of the specified
 *		  element prevents it from being added to this list.
 * 
 * @throws IndexOutOfBoundsException if the specified index is out of
 *            range (<tt>index &lt; 0 || index &gt;= size()</tt>).
 */

public Double set(int index, Double element) {
	Double old = get(index);
	content.set(index,value(element));
	return old;
}
/**
 * Returns the number of elements in this list.
 *
 * @return  the number of elements in this list.
 */
public int size() {
	return content.size();
}
/**
 * Transforms an object element to a primitive data type. 
 */
protected static double value(Number element) {
	return element.doubleValue();
}
}
