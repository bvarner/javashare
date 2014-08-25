package org.beShare.gui;

import blv.swing.AboutDialog;
import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent;
import com.apple.eawt.PreferencesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
import org.beShare.Application;
import org.beShare.network.JavaShareTransceiver;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

/**
 * Pulled out into it's own class.
 */
class MenuBar extends JMenuBar {
	private final static String JAVASHARE_COMMAND = "JavaShareCommand";
	private final static String JAVASHARE_WINDOW = "JavaShareWindow";

	private static boolean osXInstalled = false;

	private JFrame owner;
	private JavaShareTransceiver transceiver;
	private ChatMessagingPanel chatterPanel;

	MenuBar(final JFrame owner, final JavaShareTransceiver transceiver, final ChatMessagingPanel chatterPanel) {
		super();

		this.owner = owner;
		this.transceiver = transceiver;
		this.chatterPanel = chatterPanel;

		int editOptsMask = ActionEvent.CTRL_MASK;
		int platformMask = ActionEvent.ALT_MASK;

		boolean macOS = false;
		if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
			macOS = true;
			if (!osXInstalled) {
				osXInstalled = true;
				platformMask = ActionEvent.META_MASK;
				editOptsMask = ActionEvent.META_MASK;

				try {
					com.apple.eawt.Application application = com.apple.eawt.Application.getApplication();
					application.setPreferencesHandler(new PreferencesHandler() {
						@Override
						public void handlePreferences(AppEvent.PreferencesEvent preferencesEvent) {
							doPrefs();
						}
					});
					application.setAboutHandler(new AboutHandler() {
						@Override
						public void handleAbout(AppEvent.AboutEvent aboutEvent) {
							doAbout();
						}
					});
					application.setQuitHandler(new QuitHandler() {
						@Override
						public void handleQuitRequestWith(AppEvent.QuitEvent quitEvent, QuitResponse quitResponse) {
							quitResponse.cancelQuit();
							for (int i = Application.FRAMES.size() - 1; i >= 0; i--) {
								Frame f = Application.FRAMES.get(i);
								f.dispatchEvent(new WindowEvent(f, WindowEvent.WINDOW_CLOSING));
							}
						}
					});
				} catch (Exception ex) {
				} catch (Error error) {
				}
			}
		}

		JMenu file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);

		file.add(new CommandAction("Connect", "/CONNECT", KeyStroke.getKeyStroke(KeyEvent.VK_N, platformMask)));
		file.add(new CommandAction("Disconnect", "/DISCONNECT", KeyStroke.getKeyStroke(KeyEvent.VK_B, platformMask + ActionEvent.SHIFT_MASK)));
		file.addSeparator();
		file.add(new CommandAction("Open Private Chat Window", "/PRIV", KeyEvent.VK_C));
		file.add(new CommandAction("Clear Chat Log", "/CLEAR", KeyStroke.getKeyStroke(KeyEvent.VK_L, platformMask)));
		if (!macOS) {
			file.addSeparator();
			file.add(new AbstractGUIAction("About JavaShare", KeyEvent.VK_A, KeyStroke.getKeyStroke(KeyEvent.VK_F11, platformMask)) {
				@Override
				public void actionPerformed(ActionEvent e) {
					doAbout();
				}
			});

			file.addSeparator();
			file.add(new AbstractGUIAction("Quit", KeyEvent.VK_Q, KeyStroke.getKeyStroke(KeyEvent.VK_Q, platformMask)) {
				@Override
				public void actionPerformed(ActionEvent e) {
					for (int i = Application.FRAMES.size() - 1; i >= 0; i--) {
						Frame f = Application.FRAMES.get(i);
						f.dispatchEvent(new WindowEvent(f, WindowEvent.WINDOW_CLOSING));
					}
				}
			});
		}

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
		if (!macOS) {
			edit.addSeparator();
			edit.add(new AbstractGUIAction("Preferences", KeyEvent.VK_P, KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, platformMask)) {
				@Override
				public void actionPerformed(ActionEvent e) {
					doPrefs();
				}
			});
		}

		final JMenu window = new JMenu("Window");
		window.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent e) {
				window.removeAll();
				for (Frame frame : Application.FRAMES) {
					window.add(new JCheckBoxMenuItem(new FrameAction(frame)));
				}
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuCanceled(MenuEvent e) {
				System.out.println("CANCELED");
			}
		});

		this.add(file);
		this.add(edit);
		this.add(window);
	}

	private void doAbout() {
		String[] aboutText = {"JavaShare",
		                      "Version " + Application.VERSION,
		                      "",
		                      "Code Contributions by:",
		                      "     bvarner (Bryan Varner)",
		                      "     waddlesplash (Augustin Cavalier)",
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
				new AboutDialog(owner, "About JavaShare", true, aboutText, new ImageIcon(this.getClass().getClassLoader().getResource("Images/BeShare.gif")), 2, 20);
		dialog.pack();
		dialog.setVisible(true);
	}

	private void doPrefs() {
		PrefsFrame preferences = new PrefsFrame(owner, transceiver.getPreferences());
		preferences.pack();
		preferences.setVisible(true);
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
			transceiver.command(getValue(JAVASHARE_COMMAND).toString(), chatterPanel.chatDoc);
		}
	}

	private class FrameAction extends AbstractAction {
		FrameAction(final Frame frame) {
			super(frame.getTitle());
			putValue(Action.SELECTED_KEY, frame.isFocused());
			putValue(JAVASHARE_WINDOW, frame);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			((Window) getValue(JAVASHARE_WINDOW)).setVisible(true);
		}
	}
}
