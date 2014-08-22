package org.beShare.gui;

import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

/**
 * Created by bvarner on 8/18/14.
 */
public interface DropMenuModel<E> extends ListModel<E>, ListSelectionModel {

	public int getMaxSize();

	public void setMaxSize(int maxSize);

	public E getSelectedItem();

	public String elementToString(final E obj);

	public E elementFromString(final String obj);
}
