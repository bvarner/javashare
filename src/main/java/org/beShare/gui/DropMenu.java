package org.beShare.gui;

import javax.swing.BorderFactory;
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
import java.awt.Dimension;
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

	/**
	 * Creates the DropMenu and assigns a Label to the button.
	 *
	 * @param label    The Label for the DropMenu button.
	 * @param textSize The size of the text field.
	 * @param model    The DropMenuModel
	 */
	public DropMenu(final String label, int textSize, final DropMenuModel<T> model) {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder());
		this.model = model;

		button = new JButton(label);
		button.putClientProperty("JButton.buttonType", "square");
		button.setHorizontalTextPosition(SwingConstants.LEADING);
		button.setIcon(new ImageIcon(getClass().getClassLoader().getResource("Images/DownArrow.gif")));
		button.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				createMenu().show(e.getComponent(), button.getX(), button.getY() + button.getHeight());
			}
		});

		text = new JTextField("", textSize);

		if (model.getSelectedItem() != null) {
			text.setText(model.elementToString(model.getSelectedItem()));
		}

		text.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String currentValue = DropMenu.this.text.getText();
				for (int i = 0; i < model.getSize(); i++) {
					if (caseInsensitive && model.elementToString(model.getElementAt(i)).equalsIgnoreCase(currentValue)) {
						if (model.getMinSelectionIndex() != i) {
							model.setSelectionInterval(i, i);
						}
						return;
					} else if (model.elementToString(model.getElementAt(i)).equals(currentValue)) {
						if (model.getMinSelectionIndex() != i) {
							model.setSelectionInterval(i, i);
						}
						return;
					}
				}

				// If we couldn't find the item, and it's not empty.
				if (model instanceof AbstractDropMenuModel && !"".equals(currentValue.trim())) {
					// Create and add the item.

					T newItem = model.elementFromString(currentValue);
					((AbstractDropMenuModel<T>) model).ensureSelected(newItem);
				} else {
					// If we can't create it or locate it, reset the text (force input to entries)
					DropMenu.this.text.setText(model.elementToString(model.getSelectedItem()));
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

		this.add(button);
		this.add(text);

		this.model.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				text.setText(model.elementToString(model.getSelectedItem()));
			}
		});
	}

	@Override
	public void addNotify() {
		super.addNotify();

		// If we don't have a selected item yet, select the first item in the list before we get added.
		if (model.isSelectionEmpty() && model.getSize() > 0) {
			model.setSelectionInterval(0, 0);
		}
	}

	private JPopupMenu createMenu() {
		JPopupMenu menu = new JPopupMenu();
		for (int i = 0; i < model.getSize(); i++) {
			final int itemIndex = i;
			JMenuItem item = new JMenuItem(model.elementToString(model.getElementAt(itemIndex)));
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

	@Override
	public Dimension getPreferredSize() {
		Dimension buttonSize = button.getPreferredSize();
		Dimension textSize = text.getPreferredSize();
		return new Dimension(buttonSize.width + textSize.width, Math.max(textSize.height, buttonSize.height));
	}

	/**
	 * Gets the drop menu model in use by the DropMenu
	 *
	 * @return
	 */
	public DropMenuModel<T> getModel() {
		return model;
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
