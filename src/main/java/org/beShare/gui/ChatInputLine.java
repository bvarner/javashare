package org.beShare.gui;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Keymap;
import java.awt.event.KeyEvent;

/**
 * ChatInputLine - An extended JTextField that claims to manage the focus,
 * and removes the tab key from it's keymap. This allows VK_TAB to be sent
 * to any registered keylisteners.
 */
public class ChatInputLine extends JTextField {
	/**
	 * Constructor that takes a column size.
	 *
	 * @param size The number of columns display.
	 */
	public ChatInputLine(int size) {
		super(size);
		removeTabBinding();
	}

	/**
	 * Removes the Tab Key Binding
	 */
	private void removeTabBinding() {
		// Remove the binding for the TAB Key.
		KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
		Keymap noTabMap = getKeymap();

		noTabMap.removeKeyStrokeBinding(tab);
		noTabMap.addActionForKeyStroke(tab, new DefaultEditorKit.DefaultKeyTypedAction());
		addKeymap("NoTabbingMap", noTabMap);
	}

	/**
	 * Return true if there is text in the field, false if not.
	 */
	public boolean isManagingFocus() {
		try {
			return !"".equals(getText());
		} catch (Exception e) {
			return true;
		}
	}

	/**
	 * Updates the Look and feel.
	 *
	 * @overrides JTextField.updateUI();
	 */
	public void updateUI() {
		super.updateUI();
		removeTabBinding();
	}
}
