/*	Change Log:
		1.0.1 - 3.09.2002 - Made addItem(Object) Case-insensitive.
		1.1   - 6.04.2002 - Added removeItem(Object) Case-insensitive, of course.
		2.0   - 1.15.2003 - Added maxSize - size limits to the list.
*/
package org.beShare.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

/**
 * <p>DropMenu - A class that constructs a BeShare style Button/Menu combo.
 * <p/>
 * <p>Creates a JPanel, resizes the JPanel to match that of the visible button.
 * <p>When you add items to the vector, They are added to the menu.
 * <p/>
 * <p>The interface is similar to that of a JComboBox, but is more limited.
 * <p/>
 * <p>Class Started: 2-08-2002
 * <p>Last Update: 6-04-2002
 *
 * @author Bryan Varner
 * @version 2.0
 */

public class DropMenu extends JPanel implements ActionListener {
	private JButton button;
	private JPopupMenu menu;
	private Vector dataList;
	private int selected = -1;
	private int maxSize = 50;
	private ActionListener actionTarget;

	/**
	 * Default Constructor
	 * Default DropMenus have a maximum number of 50 elements in the menu.
	 */
	public DropMenu() {
		super();
		this.setLayout(new GridLayout(1, 1, 0, 0));
		button = new JButton();
		button.setHorizontalTextPosition(SwingConstants.LEADING);
		button.setIcon(AppPanel.loadImage("Images/DownArrow.gif", this));
		button.setMargin(new Insets(2, 2, 2, 2));
		MouseListener popupListener = new PopupListener();
		button.addMouseListener(popupListener);

		menu = new JPopupMenu();

		dataList = new Vector();
		actionTarget = null;

		this.add(button);
	}

	/**
	 * Creates the DropMenu and assigns a Label to the button.
	 *
	 * @param label The Label for the DropMenu button.
	 */
	public DropMenu(String label) {
		this();
		button.setText(label);
	}

	/**
	 * Creates the DropMenu and assigns a Label to the button.
	 *
	 * @param label The Label for the DropMenu button.
	 * @param max   The maximum number of items in the menu
	 */
	public DropMenu(String label, int max) {
		this(label);
		maxSize = max;
	}

	/**
	 * Creates a new Drop Menu with a label and contents.
	 *
	 * @param label   The Label for the DropMenu button.
	 * @param content A vector containing the contents of the menu.
	 *                Contents are listed by the toString() method.
	 */
	public DropMenu(String label, Vector content, int max) {
		this(label, max);
		dataList = content;
		for (int x = 0; x < dataList.size() && x < max; x++) {
			JMenuItem tempItem = new JMenuItem(dataList.elementAt(x).toString());
			tempItem.addActionListener(this);
			menu.add(tempItem);
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
	public boolean addItem(Object o) {
		// Filter through the list looking for duplicates.
		for (int x = 0; x < dataList.size(); x++) {
			if (dataList.elementAt(x).toString().toUpperCase().equals(
					                                                         o.toString().toUpperCase())) {
				return false;
			}
		}
		// make sure we're within size.
		if (dataList.size() >= maxSize) {
			removeItem(dataList.elementAt(0));
		}
		dataList.addElement(o);
		JMenuItem tempItem = new JMenuItem(o.toString());
		tempItem.addActionListener(this);
		menu.add(tempItem);
		return true;
	}

	/**
	 * Removes an <code>Object</code> from the menu.
	 *
	 * @param o The Object to be removed.
	 * @return <code>true</code> if the object is found, <code>false<code> if not.
	 */
	public boolean removeItem(Object o) {
		boolean found = false;
		for (int x = 0; x < dataList.size(); x++) {
			if (dataList.elementAt(x).toString().toUpperCase().equals(
					                                                         o.toString().toUpperCase())) {
				dataList.removeElementAt(x);
				menu.remove(x);
				found = true;
			}
		}
		return found;
	}

	/**
	 * Gets item at index <code>i
	 *
	 * @param i The index of the object to return.
	 * @return The Object at index <code>i</code>
	 */
	public Object getItemAt(int i) {
		return dataList.elementAt(i);
	}

	/**
	 * Gets the Object which is currently selected.
	 *
	 * @return An Object of the selected item.
	 */
	public Object getSelectedItem() {
		return dataList.elementAt(selected);
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
		actionTarget = a;
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
	public Vector getItems() {
		return dataList;
	}

	/**
	 * Returns the items in the vector as an array of Strings.
	 */
	public String[] getStringItems() {
		String[] stringList = new String[dataList.size()];
		for (int x = 0; x < dataList.size(); x++) {
			stringList[x] = dataList.elementAt(x).toString();
		}
		return stringList;
	}

	/**
	 * Implements an ActionListener interface for receiving messages.
	 */
	public void actionPerformed(ActionEvent e) {
		for (int x = 0; x < dataList.size(); x++) {
			if (e.getActionCommand().equals(dataList.elementAt(x).toString())) {
				selected = x;
				actionTarget.actionPerformed(new ActionEvent(this
						                                            , ActionEvent.ACTION_PERFORMED, dataList.elementAt(x).toString()));
				return;
			}
		}
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
			menu.show(e.getComponent(), button.getX(), button.getY()
					                                           + button.getHeight());
		}

		public void mouseReleased(MouseEvent e) {
			menu.show(e.getComponent(), button.getX(), button.getY()
					                                           + button.getHeight());
		}
	}
}
