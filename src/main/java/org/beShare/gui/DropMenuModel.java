package org.beShare.gui;

import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

/**
 * Defines a model for a DropMenu, which extends ListModel, ListSelectionModel,
 * and exposes some additional behavior specific to drop menus.
 */
public interface DropMenuModel<E> extends ListModel<E>, ListSelectionModel {

	public int getMaxSize();

	public void setMaxSize(int maxSize);

	public E getSelectedItem();

	public String elementToString(final E obj);

	public E elementFromString(final String obj);
}
