package org.beShare.gui;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import java.util.prefs.Preferences;

/**
 * A DefaultListModel that encapsulates a ListSelectionModel and enforces a maximum list model size.
 */
public abstract class AbstractDropMenuModel<E> extends DefaultListModel<E> implements DropMenuModel<E> {
	private DefaultListSelectionModel selectionModel;
	private int maxSize = Integer.MAX_VALUE;

	public AbstractDropMenuModel() {
		super();
		selectionModel = new DefaultListSelectionModel();
		selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	public AbstractDropMenuModel(int maxSize) {
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
			if (getMinSelectionIndex() == 0) {
				remove(1);
			} else {
				remove(0);
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

	public void saveTo(final Preferences prefs, final String baseKeyName) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size(); i++) {
			sb.append(elementToString(get(i))).append("|");
		}
		prefs.put(baseKeyName, sb.toString());
		prefs.put(baseKeyName + "-selected", elementToString(getSelectedItem()));
	}

	public void loadFrom(final Preferences prefs, final String baseKeyName) {
		loadFrom(prefs, baseKeyName, "");
	}

	public void loadFrom(final Preferences prefs, final String baseKeyName, final String defaults) {
		String value = prefs.get(baseKeyName, defaults);
		// If the current value is empty, but the default should not be, use the defaults, since it's probably a prior
		// save, or a bug.
		if (("".equals(value) || "|".equals(value)) && !"".equals(defaults)) {
			value = defaults;
		}

		for (String item : value.split("\\|")) {
			if (!"".equals(item.trim())) {
				E element = elementFromString(item.trim());
				if (!contains(element)) {
					addElement(element);
				}
			}
		}

		String selected = prefs.get(baseKeyName + "-selected", "").trim();
		if (!"".equals(selected)) {
			ensureSelected(elementFromString(selected));
		} else if (size() > 0) {
			setSelectionInterval(0, 0);
		}
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
