package org.beShare.gui;

import org.beShare.DropMenuItemFactory;
import org.beShare.network.JavaShareTransceiver;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;


/**
 * @author Bryan Varner
 */
public class AppPanel extends JPanel {

	public static final String AutoUpdateURL = "http://beshare.tycomsystems.com/servers.txt";

	ChatMessagingPanel chatterPanel;

	/**
	 * Creates a new AppPanel based on the fields stored in <code>prefsMessage
	 * </code> any fields that are required but are not present are filled in
	 * with default values.
	 */
	public AppPanel(final JavaShareTransceiver transceiver) {
// Add support for the beoslaf.jar
//        try {
//            ClassLoader.getSystemClassLoader().loadClass("com.sun.java.swing.plaf.beos.BeOSLookAndFeel");
//            UIManager.installLookAndFeel(new UIManager.LookAndFeelInfo("BeOS R5", "com.sun.java.swing.plaf.beos.BeOSLookAndFeel"));
//        } catch (Exception e) {
//        }

		// First we need to check our L&F setting!
//		if (programPrefsMessage.hasField("LaF")) {
//			try {
//				UIManager.setLookAndFeel(programPrefsMessage.getString("LaF", UIManager.getCrossPlatformLookAndFeelClassName()));
//			} catch (Exception ex) {
//				System.err.println("Failed to set LookandFeel: " + ex.getMessage());
//			}
//		}

		this.setLayout(new BorderLayout());
		chatterPanel = new ChatMessagingPanel(transceiver);

		// Setup a panel with the server, nickname, status DropMenus.
		JPanel connectPanel = new JPanel();
		connectPanel.setLayout(new BoxLayout(connectPanel, BoxLayout.X_AXIS));
		connectPanel.add(new DropMenu<String>("Server:", 20, transceiver.getServerModel(), new StringItemFactory()));
		connectPanel.add(Box.createHorizontalStrut(6));
		connectPanel.add(new DropMenu<String>("Name:", 10, transceiver.getNameModel(), new StringItemFactory()));
		connectPanel.add(Box.createHorizontalStrut(6));
		connectPanel.add(new DropMenu<String>("Status:", 10, transceiver.getStatusModel(), new StringItemFactory()));
		this.add(connectPanel, BorderLayout.NORTH);


		TransferPanel transPan = new TransferPanel(transceiver);

		JSplitPane queryChatSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		queryChatSplit.add(transPan);
		queryChatSplit.add(chatterPanel);
		this.add(queryChatSplit, BorderLayout.CENTER);

		chatterPanel.chatDoc.addSystemMessage("Welcome to JavaShare!\n" +
				                                      "Type /help for a list of commands.");
	}

	private class StringItemFactory implements DropMenuItemFactory<String> {
		@Override
		public String toString(String obj) {
			return obj;
		}

		@Override
		public String fromString(String obj) {
			return obj;
		}
	}
}
