package org.beShare;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

/**
 * A DefaultListModel that encapsulates a ListSelectionModel and enforces a maximum list model size.
 */
public class DefaultDropMenuModel<E> extends DefaultListModel<E> implements DropMenuModel<E> {
	private DefaultListSelectionModel selectionModel;
	private int maxSize = Integer.MAX_VALUE;

	public DefaultDropMenuModel() {
		super();
		selectionModel = new DefaultListSelectionModel();
		selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	public DefaultDropMenuModel(int maxSize) {
		this();
		this.maxSize = maxSize;
	}

	@Override
	public int getMaxSize() {
		return maxSize;
	}

	@Override
	public void setMaxSize(int maxSize) {
		if (maxSize < 2) {
			throw new IllegalArgumentException("Maximum size must be > 1");
		}
		this.maxSize = maxSize;
	}

	/**
	 * If necessary, adds the element to the list, then sets it as the selected index.
	 *
	 * @param element
	 */
	public void ensureSelected(E element) {
		if (!contains(element)) {
			addElement(element);
		}
		int index = indexOf(element);
		setSelectionInterval(index, index);
	}

	@Override
	public void addElement(E element) {
		if (size() + 1 > maxSize) {
			E removed;
			if (getMinSelectionIndex() == 0) {
				removed = remove(1);
			} else {
				removed = remove(0);
			}
		}
		super.addElement(element);
	}

	@Override
	public void add(int index, E element) {
		super.add(index, element);
		if (index == getMinSelectionIndex()) {
			selectionModel.setSelectionInterval(index, index);
		}
	}

	/**
	 * Gets the selected object.
	 *
	 * @return
	 */
	@Override
	public E getSelectedItem() {
		if (selectionModel.isSelectionEmpty()) {
			return null;
		}
		return get(selectionModel.getMinSelectionIndex());
	}

	@Override
	public void setSelectionInterval(int index0, int index1) {
		selectionModel.setSelectionInterval(index0, index1);
	}

	@Override
	public void addSelectionInterval(int index0, int index1) {
		selectionModel.setSelectionInterval(index0, index1);
	}

	@Override
	public void removeSelectionInterval(int index0, int index1) {
		selectionModel.removeSelectionInterval(index0, index1);
	}

	@Override
	public int getMinSelectionIndex() {
		return selectionModel.getMinSelectionIndex();
	}

	@Override
	public int getMaxSelectionIndex() {
		return selectionModel.getMaxSelectionIndex();
	}

	@Override
	public boolean isSelectedIndex(int index) {
		return selectionModel.isSelectedIndex(index);
	}

	@Override
	public int getAnchorSelectionIndex() {
		return selectionModel.getAnchorSelectionIndex();
	}

	@Override
	public void setAnchorSelectionIndex(int index) {
		selectionModel.setAnchorSelectionIndex(index);
	}

	@Override
	public int getLeadSelectionIndex() {
		return selectionModel.getLeadSelectionIndex();
	}

	@Override
	public void setLeadSelectionIndex(int index) {
		selectionModel.setLeadSelectionIndex(index);
	}

	@Override
	public void clearSelection() {
		selectionModel.clearSelection();
	}

	@Override
	public boolean isSelectionEmpty() {
		return selectionModel.isSelectionEmpty();
	}

	@Override
	public void insertIndexInterval(int index, int length, boolean before) {
		selectionModel.insertIndexInterval(index, length, before);
	}

	@Override
	public void removeIndexInterval(int index0, int index1) {
		selectionModel.removeIndexInterval(index0, index1);
	}

	@Override
	public boolean getValueIsAdjusting() {
		return selectionModel.getValueIsAdjusting();
	}

	@Override
	public void setValueIsAdjusting(boolean valueIsAdjusting) {
		selectionModel.setValueIsAdjusting(valueIsAdjusting);
	}

	@Override
	public int getSelectionMode() {
		return selectionModel.getSelectionMode();
	}

	@Override
	public void setSelectionMode(int selectionMode) {
		if (selectionMode != ListSelectionModel.SINGLE_SELECTION) {
			throw new IllegalArgumentException("invalid selectionMode");
		}
		selectionModel.setSelectionMode(selectionMode);
	}

	@Override
	public void addListSelectionListener(ListSelectionListener x) {
		selectionModel.addListSelectionListener(x);
	}

	@Override
	public void removeListSelectionListener(ListSelectionListener x) {
		selectionModel.removeListSelectionListener(x);
	}
}
