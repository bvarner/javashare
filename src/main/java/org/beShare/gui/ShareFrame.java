/* Change Log:
	2-28.2002 - 1.0 - Initial Version
	12.19.2002 - 1.3 - Added private sizing and centering functions.
						These ensure that the full window is visible on-screen when the program starts.
						Even if the previously saved bounds were out of the current resolution.
*/
package org.beShare.gui;

import blv.swing.AboutDialog;
import org.beShare.Application;
import org.beShare.network.JavaShareTransceiver;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

/**
 * ShareFrame is a parent window of a connection to a BeShare (MUSCLE) server.
 * It renders the state maintained by the JavaShareTransceiver and provides a GUI.
 *
 * @author Bryan Varner
 */
public class ShareFrame extends JFrame {

	private final static String JAVASHARE_COMMAND = "JavaShareCommand";
	private JavaShareTransceiver transceiver;
	private ChatMessagingPanel chatterPanel;


	public ShareFrame(final JavaShareTransceiver transceiver) {
		super("JavaShare " + Application.BUILD_VERSION);
		this.transceiver = transceiver;

		ImageIcon JavaShareIcon = new ImageIcon(getClass().getClassLoader().getResource("Images/BeShare.gif"));
		this.setIconImage(JavaShareIcon.getImage());

		JPanel mainPanel = new JPanel(new BorderLayout());
		chatterPanel = new ChatMessagingPanel(transceiver);

		// Setup a panel with the server, nickname, status DropMenus.
		JPanel connectPanel = new JPanel();
		connectPanel.setLayout(new BoxLayout(connectPanel, BoxLayout.X_AXIS));
		connectPanel.add(new DropMenu<String>("Server:", 20, transceiver.getServerModel(), new StringItemFactory()));
		connectPanel.add(Box.createHorizontalStrut(6));
		connectPanel.add(new DropMenu<String>("Name:", 10, transceiver.getNameModel(), new StringItemFactory()));
		connectPanel.add(Box.createHorizontalStrut(6));
		connectPanel.add(new DropMenu<String>("Status:", 10, transceiver.getStatusModel(), new StringItemFactory()));


		TransferPanel transPan = new TransferPanel(transceiver);

		JSplitPane queryChatSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		queryChatSplit.add(transPan);
		queryChatSplit.add(chatterPanel);

		chatterPanel.chatDoc.addSystemMessage("Welcome to JavaShare!\n" +
				                                      "Type /help for a list of commands.");

		mainPanel.add(connectPanel, BorderLayout.NORTH);
		mainPanel.add(queryChatSplit);
		this.setContentPane(mainPanel);

		this.setJMenuBar(new MenuBar());
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		// TODO: monitor the connection status in the Transceiver...

		// TODO: Refactor The majority of AppPanel into this class.
	}


	private class MenuBar extends JMenuBar {
		MenuBar() {
			super();

			int editOptsMask = ActionEvent.CTRL_MASK;
			int platformMask = ActionEvent.ALT_MASK;
			if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
				platformMask = ActionEvent.META_MASK;
				editOptsMask = ActionEvent.META_MASK;
			}

			JMenu file = new JMenu("File");
			file.setMnemonic(KeyEvent.VK_F);

			file.add(new CommandAction("Connect", "/CONNECT", KeyStroke.getKeyStroke(KeyEvent.VK_N, platformMask)));
			file.add(new CommandAction("Disconnect", "/DISCONNECT", KeyStroke.getKeyStroke(KeyEvent.VK_B, platformMask + ActionEvent.SHIFT_MASK)));
			file.addSeparator();
			file.add(new CommandAction("Open Private Chat Window", "/PRIV", KeyEvent.VK_C));
			file.add(new CommandAction("Clear Chat Log", "/CLEAR", KeyStroke.getKeyStroke(KeyEvent.VK_L, platformMask)));
			file.addSeparator();
			file.add(new AbstractGUIAction("About JavaShare", KeyEvent.VK_A, KeyStroke.getKeyStroke(KeyEvent.VK_F11, platformMask)) {
				@Override
				public void actionPerformed(ActionEvent e) {
					String[] aboutText = {"JavaShare",
					                      "Version " + Application.BUILD_VERSION,
					                      "",
					                      "Special Thanks to:",
					                      "     Tori Anderson",
					                      "     Jonathon Beige",
					                      "     Adam McNutt",
					                      "     Michael Paine",
					                      "     David Varner",
					                      "     Douglas Varner",
					                      "     Helmar Rudolph",
					                      "     John Slevin",
					                      "     The Wonderful BeOS Community!",
					                      "",
					                      "And especially:",
					                      "   Those who have submitted bug-reports!",
					                      "   The graphics aid of Mikko Heikkinen",
					                      "   The awesome generosity of",
					                      "      Chris Gelatt",
					                      "      Austin Brower",
					                      "      Alan Ellis",
					                      "      Silent Computing"};
					AboutDialog dialog =
							new AboutDialog(ShareFrame.this, "About JavaShare", true, aboutText, new ImageIcon(this.getClass().getClassLoader().getResource("Images/BeShare.gif")), 2, 20);
					dialog.pack();
					dialog.setVisible(true);
				}
			});
			file.addSeparator();
			file.add(new AbstractGUIAction("Quit", KeyEvent.VK_Q, KeyStroke.getKeyStroke(KeyEvent.VK_Q, platformMask)) {
				@Override
				public void actionPerformed(ActionEvent e) {
					ShareFrame.this.dispatchEvent(new WindowEvent(ShareFrame.this, WindowEvent.WINDOW_CLOSING));
				}
			});

			JMenu edit = new JMenu("Edit");
			edit.setMnemonic(KeyEvent.VK_E);

			edit.add(new AbstractGUIAction("Cut", KeyEvent.VK_C, KeyStroke.getKeyStroke(KeyEvent.VK_X, editOptsMask)) {
				@Override
				public void actionPerformed(ActionEvent e) {
					chatterPanel.cut();
				}
			});
			edit.add(new AbstractGUIAction("Copy", KeyEvent.VK_O, KeyStroke.getKeyStroke(KeyEvent.VK_C, editOptsMask)) {
				@Override
				public void actionPerformed(ActionEvent e) {
					chatterPanel.copy();
				}
			});
			edit.add(new AbstractGUIAction("Paste", KeyEvent.VK_T, KeyStroke.getKeyStroke(KeyEvent.VK_V, editOptsMask)) {
				@Override
				public void actionPerformed(ActionEvent e) {
					chatterPanel.paste();
				}
			});
			edit.addSeparator();
			edit.add(new AbstractGUIAction("Preferences", KeyEvent.VK_P, KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, platformMask)) {
				@Override
				public void actionPerformed(ActionEvent e) {
					PrefsFrame preferences = new PrefsFrame(ShareFrame.this);
					preferences.pack();
					preferences.setVisible(true);
				}
			});

			this.add(file);
			this.add(edit);
		}


		private abstract class AbstractGUIAction extends AbstractAction {
			AbstractGUIAction(final String text, final int key, final KeyStroke keystroke) {
				super(text);
				putValue(MNEMONIC_KEY, key);
				putValue(ACCELERATOR_KEY, keystroke);
			}
		}

		/**
		 * Command Action sends text commands as menus.
		 */
		private class CommandAction extends AbstractAction {
			CommandAction(final String text, final String command, final int key) {
				super(text);
				putValue(JAVASHARE_COMMAND, command);
				putValue(MNEMONIC_KEY, key);
			}

			CommandAction(final String text, final String command, final KeyStroke keystroke) {
				super(text);
				putValue(JAVASHARE_COMMAND, command);
				putValue(ACCELERATOR_KEY, keystroke);
				putValue(MNEMONIC_KEY, keystroke.getKeyCode());
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				ShareFrame.this.transceiver.command(getValue(JAVASHARE_COMMAND).toString(), ShareFrame.this.chatterPanel.chatDoc);
			}
		}
	}
}