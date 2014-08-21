/* Change Log:
	2-28.2002 - 1.0 - Initial Version
	12.19.2002 - 1.3 - Added private sizing and centering functions.
						These ensure that the full window is visible on-screen when the program starts.
						Even if the previously saved bounds were out of the current resolution.
*/
package org.beShare.gui;

import org.beShare.Application;
import org.beShare.network.JavaShareTransceiver;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Color;

/**
 * ShareFrame is a parent window of a connection to a BeShare (MUSCLE) server.
 * It renders the state maintained by the JavaShareTransceiver and provides a GUI.
 *
 * @author Bryan Varner
 */
public class ShareFrame extends JFrame {

	private JavaShareTransceiver transceiver;
	private ChatMessagingPanel chatterPanel;

	public ShareFrame(final JavaShareTransceiver transceiver) {
		super("JavaShare " + Application.VERSION);
		this.transceiver = transceiver;

		ImageIcon JavaShareIcon = new ImageIcon(getClass().getClassLoader().getResource("Images/BeShare.gif"));
		this.setIconImage(JavaShareIcon.getImage());

		chatterPanel = new ChatMessagingPanel(transceiver);

		// Setup a panel with the server, nickname, status DropMenus.
		JPanel connectPanel = new JPanel();
		connectPanel.setLayout(new BoxLayout(connectPanel, BoxLayout.X_AXIS));
		connectPanel.setBorder(BorderFactory.createEmptyBorder());
		connectPanel.add(new DropMenu<String>("Server:", 20, transceiver.getServerModel(), new StringItemFactory()));
		connectPanel.add(Box.createHorizontalStrut(5));
		connectPanel.add(new DropMenu<String>("Name:", 10, transceiver.getNameModel(), new StringItemFactory()));
		connectPanel.add(Box.createHorizontalStrut(5));
		connectPanel.add(new DropMenu<String>("Status:", 10, transceiver.getStatusModel(), new StringItemFactory()));

		TransferPanel transPan = new TransferPanel(transceiver);

		JSplitPane queryChatSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		queryChatSplit.add(transPan);
		queryChatSplit.add(chatterPanel);
		queryChatSplit.setBorder(BorderFactory.createEmptyBorder());

		chatterPanel.chatDoc.addSystemMessage("Welcome to JavaShare!\n" +
				                              "Type /help for a list of commands.");

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(connectPanel, BorderLayout.NORTH);
		mainPanel.add(queryChatSplit);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		this.setContentPane(mainPanel);

		this.setJMenuBar(new MenuBar(this, transceiver, chatterPanel));
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		// TODO: monitor the connection status in the Transceiver...
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
