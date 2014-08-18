package org.beShare.gui;

import org.beShare.DefaultDropMenuModel;
import org.beShare.DropMenuItemFactory;
import org.beShare.DropMenuModel;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
	private JTextField text;
	private boolean caseInsensitive = true;
	private DropMenuModel<T> model;
	private DropMenuItemFactory<T> itemFactory;

	/**
	 * Creates a DropMenu that uses the itemFactory for object / item translation
	 */
	public DropMenu(final DropMenuItemFactory itemFactory) {
		this("", 20, new DefaultDropMenuModel<T>(), itemFactory);
	}

	/**
	 * Creates the DropMenu and assigns a Label to the button.
	 *
	 * @param label       The Label for the DropMenu button.
	 * @param textSize    The size of the text field.
	 * @param model       The DropMenuModel
	 * @param itemFactory to use for creating JMenuItems.
	 */
	public DropMenu(final String label, int textSize, final DropMenuModel<T> model, final DropMenuItemFactory<T> itemFactory) {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.model = model;
		this.itemFactory = itemFactory;

		button = new JButton(label);
		button.setHorizontalTextPosition(SwingConstants.LEADING);
		button.setIcon(new ImageIcon(getClass().getClassLoader().getResource("Images/DownArrow.gif")));
		button.setMargin(new Insets(2, 2, 2, 2));
		button.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				createMenu().show(e.getComponent(), button.getX(), button.getY() + button.getHeight());
			}
		});
		this.add(button);

		text = new JTextField("", textSize);
		text.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String currentValue = DropMenu.this.text.getText();
				for (int i = 0; i < model.getSize(); i++) {
					if (caseInsensitive && itemFactory.toString(model.getElementAt(i)).equalsIgnoreCase(currentValue)) {
						if (model.getMinSelectionIndex() != i) {
							model.setSelectionInterval(i, i);
						}
						return;
					} else if (itemFactory.toString(model.getElementAt(i)).equals(currentValue)) {
						if (model.getMinSelectionIndex() != i) {
							model.setSelectionInterval(i, i);
						}
						return;
					}
				}

				// If we couldn't find the item
				if (model instanceof DefaultDropMenuModel) {
					// Create and add the item.
					T newItem = itemFactory.fromString(currentValue);
					((DefaultDropMenuModel<T>) model).addElement(newItem);

					// Update the index to select.
					int i = ((DefaultDropMenuModel<T>) model).indexOf(newItem);
					model.setSelectionInterval(i, i);
				} else {
					// If we can't create it, reset the text (force input to entries)
					DropMenu.this.text.setText(itemFactory.toString(model.getSelectedItem()));
				}
			}
		});

		// If the text field loses focus, fire it's action event.
		text.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				text.postActionEvent();
			}
		});

		this.add(text);

		this.model.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				text.setText(itemFactory.toString(model.getSelectedItem()));
			}
		});
	}

	private JPopupMenu createMenu() {
		JPopupMenu menu = new JPopupMenu();
		for (int i = 0; i < model.getSize(); i++) {
			final int itemIndex = i;
			JMenuItem item = new JMenuItem(itemFactory.toString(model.getElementAt(itemIndex)));
			item.addActionListener(new ActionListener() {
				                       @Override
				                       public void actionPerformed(ActionEvent e) {
					                       model.setSelectionInterval(itemIndex, itemIndex);
				                       }
			                       }
			);
			menu.add(item);
		}
		return menu;
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
}
