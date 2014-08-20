package org.beShare.gui;

import org.beShare.network.JavaShareTransceiver;

import javax.swing.JDialog;
import java.awt.Frame;

/**
 * A simple wrapper for private chats.
 */
public class PrivateDialog extends JDialog {

	/**
	 * Creates a new PrivateDialog!
	 *
	 * @param owner
	 * @param transceiver
	 * @param sessionIds
	 */
	public PrivateDialog(Frame owner, JavaShareTransceiver transceiver, String[] sessionIds) {
		super(owner, "Private Chat with: ", false);
		setContentPane(new ChatMessagingPanel(transceiver, sessionIds));
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}
}
