package org.beShare.gui;

import org.beShare.network.JavaShareTransceiver;

import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * A simple wrapper for private chats.
 * <p/>
 * Created by bvarner on 8/20/14.
 */
public class PrivateDialog extends JDialog {
	public PrivateDialog(JFrame owner, JavaShareTransceiver transceiver, String[] sessionIds) {
		super(owner, "Private Chat with: ", false);
		setContentPane(new ChatMessagingPanel(transceiver, sessionIds));
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}
}
