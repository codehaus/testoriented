package org.dyndns.opendemogroup.todd;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.IStructuredSelection;

public class SimpleStructuredSelection implements IStructuredSelection {

	private List _Contents;
	
	public SimpleStructuredSelection() {
		_Contents = new ArrayList();
	}
	
	public SimpleStructuredSelection ( IJavaElement element ) {
		_Contents = new ArrayList();
		_Contents.add(element);
	}
	
	/**
	 * @see java.util.List#add(Object)
	 */
	public boolean add ( Object o ) {
		return _Contents.add(o);
	}
	
	/**
	 * @see org.eclipse.jface.viewers.IStructuredSelection#getFirstElement()
	 */
	public Object getFirstElement() {
		if (isEmpty()) {
			return null;
		}
		return _Contents.get(0);
	}

	/**
	 * @see org.eclipse.jface.viewers.IStructuredSelection#iterator()
	 */
	public Iterator iterator() {
		return _Contents.iterator();
	}

	/**
	 * @see org.eclipse.jface.viewers.IStructuredSelection#size()
	 */
	public int size() {
		return _Contents.size();
	}

	/**
	 * @see org.eclipse.jface.viewers.IStructuredSelection#toArray()
	 */
	public Object[] toArray() {
		return _Contents.toArray();
	}

	/**
	 * @see org.eclipse.jface.viewers.IStructuredSelection#toList()
	 */
	public List toList() {
		return _Contents;
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelection#isEmpty()
	 */
	public boolean isEmpty() {
		boolean result = (0 == _Contents.size());
		return result;
	}

}
