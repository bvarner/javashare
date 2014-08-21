package org.beShare.gui;

import org.beShare.Application;
import org.beShare.network.JavaShareTransceiver;

import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * A simple wrapper for private chats.
 */
public class PrivateFrame extends JFrame {

	/**
	 * Creates a new PrivateFrame!
	 *
	 * @param transceiver
	 * @param sessionIds
	 */
	public PrivateFrame(JavaShareTransceiver transceiver, String[] sessionIds) {
		super("Private Chat with: ");
		ChatMessagingPanel chatterPanel = new ChatMessagingPanel(transceiver, sessionIds);
		setJMenuBar(new MenuBar(this, transceiver, chatterPanel));

		setContentPane(chatterPanel);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		pack();
	}

	@Override
	public void dispose() {
		Application.FRAMES.remove(this);
		super.dispose();
	}

	@Override
	public void setVisible(boolean b) {
		if (b) {
			if (!Application.FRAMES.contains(this)) {
				Application.FRAMES.add(this);
			}
		} else {
			Application.FRAMES.remove(this);
		}

		super.setVisible(b);
	}
}
