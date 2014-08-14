/*	Change Log:
		1.0.1 - 3.09.2002 - Made addItem(Object) Case-insensitive.
		1.1   - 6.04.2002 - Added removeItem(Object) Case-insensitive, of course.
		2.0   - 1.15.2003 - Added maxSize - size limits to the list.
*/
package org.beShare.gui;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>DropMenu - A class that constructs a BeShare style Button/Menu combo.
 * <p/>
 * <p>Creates a JPanel, resizes the JPanel to match that of the visible button.
 * <p>When you add items to the vector, They are added to the menu.
 * <p/>
 * <p>The interface is similar to that of a JComboBox, but is more limited.
 *
 * @author Bryan Varner
 */

public class DropMenu<T> extends JPanel {
	private JButton button;
	private JPopupMenu menu;
	private List<T> dataList;

	private int selected = -1;
	private int maxSize = 50;

	private List<ActionListener> actionListeners = new ArrayList<>();

	/**
	 * Default Constructor
	 * Default DropMenus have a maximum number of 50 elements in the menu.
	 */
	public DropMenu() {
		this("");
	}

	/**
	 * Creates the DropMenu and assigns a Label to the button.
	 *
	 * @param label The Label for the DropMenu button.
	 */
	public DropMenu(final String label) {
		this(label, 50);
	}

	/**
	 * Creates the DropMenu and assigns a Label to the button.
	 *
	 * @param label The Label for the DropMenu button.
	 * @param max   The maximum number of items in the menu
	 */
	public DropMenu(final String label, final int max) {
		this(label, new ArrayList<T>(), max);
	}

	/**
	 * Creates a new Drop Menu with a label and contents.
	 *
	 * @param label   The Label for the DropMenu button.
	 * @param content A vector containing the contents of the menu.
	 *                Contents are listed by the toString() method.
	 */
	public DropMenu(final String label, List<T> content, final int maxSize) {
		super();
		this.maxSize = maxSize;
		this.dataList = content;

		this.setLayout(new GridLayout(1, 1, 0, 0));
		button = new JButton(label);
		button.setHorizontalTextPosition(SwingConstants.LEADING);
		button.setIcon(new ImageIcon(getClass().getClassLoader().getResource("Images/DownArrow.gif")));
		button.setMargin(new Insets(2, 2, 2, 2));
		MouseListener popupListener = new PopupListener();
		button.addMouseListener(popupListener);

		menu = new JPopupMenu();
		this.add(button);

		for (T item : dataList) {
			addItem(item);
		}
	}

	/**
	 * Gets the index of the currently selected item.
	 *
	 * @return an <code>int</code> which is the index of the selected item.
	 */
	public int getSelectedIndex() {
		return selected;
	}

	/**
	 * Sets the currently selected item.
	 *
	 * @param s The item to select
	 */
	public void setSelectedIndex(int s) {
		selected = s;
	}

	/**
	 * Adds an item to the List
	 * <p/>
	 * This method retrieves the String to display by invoking the
	 * <code>toString()</code> method on the Object <code>o</code>.
	 * <p/>
	 * All items must be unique. Duplicates will not be added. - This is Case
	 * In-senstive
	 *
	 * @param o An Object to be added to the list.
	 * @return <code>true</code> if the object is added, <code>false</code> if not.
	 */
	public boolean addItem(T o) {
		// Filter through the list looking for duplicates.
		if (dataList.contains(o)) {
			return false;
		}

		// make sure we're within size.
		if (dataList.size() >= maxSize) {
			removeItem(dataList.get(0));
		}

		dataList.add(o);
		JMenuItem tempItem = new JMenuItem(o.toString());
		tempItem.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// Transmute the incoming Item ActionEvent to originate from us.
					ActionEvent dropMenuEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, e.getActionCommand());
					for (ActionListener listener : actionListeners) {
						listener.actionPerformed(dropMenuEvent);
					}
				}
			}
		);
		menu.add(tempItem);
		return true;
	}

	/**
	 * Removes an <code>Object</code> from the menu.
	 *
	 * @param o The Object to be removed.
	 * @return <code>true</code> if the object is found, <code>false<code> if not.
	 */
	public boolean removeItem(T o) {
		int index = dataList.indexOf(o);
		if (index > -1) {
			dataList.remove(index);
			menu.remove(index);
		}
		return index > -1;
	}

	/**
	 * Gets item at index <code>i
	 *
	 * @param i The index of the object to return.
	 * @return The Object at index <code>i</code>
	 */
	public T getItemAt(int i) {
		return dataList.get(i);
	}

	/**
	 * Gets the Object which is currently selected.
	 *
	 * @return An Object of the selected item.
	 */
	public T getSelectedItem() {
		return dataList.get(selected);
	}

	/**
	 * Gets the total number of items in the list
	 *
	 * @return the number of items in the list.
	 */
	public int getItemCount() {
		return dataList.size();
	}

	/**
	 * Gets the current text label of the button.
	 *
	 * @return the Text label of the Menu button.
	 */
	public String getText() {
		return button.getText();
	}

	/**
	 * Sets the visible text of the Menu button.
	 *
	 * @param label The String for the Button to display
	 */
	public void setText(String label) {
		button.setText(label);
	}

	/**
	 * Registers an <code>ActionListener</code> for this object.
	 *
	 * @param a The <code>ActionListener</code> that messages from this object
	 *          will be sent to.
	 */
	public void addActionListener(ActionListener a) {
		actionListeners.add(a);
	}

	/**
	 * Removes an ActionListener.
	 *
	 * @param a
	 */
	public void removeActionListener(ActionListener a) {
		actionListeners.remove(a);
	}

	/**
	 * Gets the preferred size of the object by getting the preferred size of
	 * the contained button object.
	 *
	 * @return the preferred Dimension of this object.
	 * @overrides Component.getPreferredSize()
	 */
	public Dimension getPreferredSize() {
		return button.getPreferredSize();
	}

	/**
	 * Gets the Maximum Size for this object, by getting the maximum size of
	 * the contained button.
	 *
	 * @return the Maximum Dimensions of this object.
	 * @overrides Component.getMaximumSize();
	 */
	public Dimension getMaximumSize() {
		return button.getMaximumSize();
	}

	/**
	 * Gets the Minimum size for this object, by getting the minimum size of
	 * the contained button.
	 *
	 * @return The minimum Dimension of this object.
	 * @overrides Component.getMinimumSize();
	 */
	public Dimension getMinimumSize() {
		return button.getMinimumSize();
	}

	/**
	 * Returns the vector containing the list of items in the menu.
	 */
	public List<T> getItems() {
		return dataList;
	}

	/**
	 * Returns the items in the vector as an array of Strings.
	 */
	public String[] getStringItems() {
		String[] stringList = new String[dataList.size()];
		for (int x = 0; x < dataList.size(); x++) {
			stringList[x] = dataList.get(x).toString();
		}
		return stringList;
	}

	/**
	 * updates the Look and Feel for this component.
	 */
	public void updateUI() {
		try {
			SwingUtilities.updateComponentTreeUI(menu);
			super.updateUI();
		} catch (NullPointerException npe) {
		}
	}

	/**
	 * Inner Class to implement a MouseAdapter
	 */
	class PopupListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			menu.show(e.getComponent(), button.getX(), button.getY() + button.getHeight());
		}

		public void mouseReleased(MouseEvent e) {
			menu.show(e.getComponent(), button.getX(), button.getY() + button.getHeight());
		}
	}
}
