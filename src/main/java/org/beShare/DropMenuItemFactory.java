package org.beShare;

import javax.swing.JMenuItem;

/**
 * Creates JMenuItems for the given DropMenuItemFactory.
 */
public interface DropMenuItemFactory<T> {

	public String toString(final T obj);

	public T fromString(final String obj);
}
