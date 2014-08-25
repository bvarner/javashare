package org.beShare.gui;

import org.beShare.data.FilteredUserDataModel;
import org.beShare.gui.swingAddons.TableSorter;
import org.beShare.network.JavaShareTransceiver;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Stack;

/**
 * ChatMessagingPanel - This panel is responsible to firing off new
 * ChatMessages from the local user, and for the adding of text to the chat
 * log.
 *
 * @author Bryan Varner
 */
public class ChatMessagingPanel extends JPanel {
	private static Color SCROLL_LOCKED = new Color(205, 255, 255);
	JavaShareTransceiver transceiver;
	// Private Chat panel and Session / Name Text Box
	JPanel chatWithPanel;
	JTextField chatWithSessions;
	// Chat Log and Input Box
	JTextPane chatLog;
	Color defaultBackground;
	ChatInputLine chatInput = new ChatInputLine(25);
	ChatDocument chatDoc;
	// User table
	JTable userTable;
	Stack recentLines;
	int lineIndex;
	JScrollPane logScrollPane;
	Timer scrollTimer;
	int scrollUpAdjustments;
	int previousScrollPosition;
	private WindowAdapter windowListener = new WindowAdapter() {
		@Override
		public void windowActivated(WindowEvent e) {
			chatInput.requestFocus();
		}
	};

	public ChatMessagingPanel(final JavaShareTransceiver transceiver) {
		this(transceiver, null);
	}

	public ChatMessagingPanel(final JavaShareTransceiver transceiver, final String[] sessionIds) {
		super(new BorderLayout(5, 5));
		this.transceiver = transceiver;
		setPreferredSize(new Dimension(600, 450));

		chatDoc = new ChatDocument(new FilteredUserDataModel(this.transceiver.getUserDataModel()));

		chatWithPanel = new JPanel();
		chatWithPanel.setLayout(new BoxLayout(chatWithPanel, BoxLayout.X_AXIS));
		chatWithSessions = new JTextField("");
		if (sessionIds != null) {
			StringBuilder chatWith = new StringBuilder();
			for (String sessionId : sessionIds) {
				chatWith.append(sessionId).append(" ");
			}
			chatWithSessions.setText(chatWith.toString());
			updateUsers();
		}
		chatWithPanel.add(new JLabel("Chat With: "));
		chatWithPanel.add(chatWithSessions);
		chatWithPanel.setVisible(sessionIds != null);

		this.scrollUpAdjustments = 0;
		this.previousScrollPosition = 0;
		this.recentLines = new Stack();
		this.lineIndex = 0;

		chatInput.setRequestFocusEnabled(true);

		chatWithSessions.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateUsers();
			}
		});
		chatWithSessions.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				updateUsers();
			}
		});

		chatInput.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String chatText = chatInput.getText();
				if (chatText.length() > 0) {
					String message = "";

					transceiver.handleInput(chatInput.getText(), chatDoc);

					recentLines.push(chatText);
					lineIndex = recentLines.size();
					chatInput.setText("");
				}
			}
		});

		chatInput.addKeyListener(new KeyAdapter() {
			/**
			 * Responds to various events on the panel and implements nice UI things.
			 */
			public void keyReleased(KeyEvent e) {
				if ((e.getKeyCode() == KeyEvent.VK_KP_UP)
						    || (e.getKeyCode() == KeyEvent.VK_UP)) {
					if (lineIndex > 0 && recentLines.size() > 0) {
						lineIndex--;
						chatInput.setText((String) recentLines.elementAt(lineIndex));
					}
				} else if ((e.getKeyCode() == KeyEvent.VK_KP_DOWN) ||
						           (e.getKeyCode() == KeyEvent.VK_DOWN)) {
					if (recentLines.size() > lineIndex + 1) {
						lineIndex++;
						chatInput.setText((String) recentLines.elementAt(lineIndex));
					} else {
						lineIndex = recentLines.size();
						chatInput.setText("");
					}
				} else if ((e.getKeyCode() == KeyEvent.VK_F1) || (e.getKeyCode() == KeyEvent.VK_TAB)) {
					String completedName = "";
					int lastSpace = chatInput.getText().lastIndexOf(" ");
					if (lastSpace == -1) {
						completedName =
								chatDoc.getFilteredUserDataModel().getUserDataModel().findCompletedName(chatInput.getText().substring(0));
					} else {
						completedName =
								chatDoc.getFilteredUserDataModel().getUserDataModel().findCompletedName(chatInput.getText().substring(lastSpace + 1));
					}
					chatInput.setText(chatInput.getText().substring(0, lastSpace + 1) + completedName);
				}
			}
		});

		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
		inputPanel.add(new JLabel("Chat: "));
		inputPanel.add(chatInput);

		chatLog = new JTextPane(chatDoc);
		defaultBackground = UIManager.getColor("EditorPane.background");
		chatLog.setBackground(defaultBackground);

		chatLog.setMargin(new Insets(2, 2, 2, 2));
		chatLog.setEditable(false);
		chatLog.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				chatDoc.textClicked(chatLog.viewToModel(e.getPoint()));
			}
		});
		logScrollPane = new JScrollPane(chatLog);
		logScrollPane.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
		// Set the background color on the chatLog when we're in a range that won't auto-scroll.
		logScrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				// Check our adjuster...
				if (e.getValue() <= previousScrollPosition) {
					scrollUpAdjustments++;
				} else if (scrollUpAdjustments > 0) {
					scrollUpAdjustments--;
				} else {
					scrollUpAdjustments = 0;
				}
				// See if we're at the bottom... if we are, we'll set the adjustments to
				// 0 just for safe measure...
				Adjustable adjustable = e.getAdjustable();
				if (e.getValue() == adjustable.getMaximum() - adjustable.getVisibleAmount()) {
					scrollUpAdjustments = 0;
				}
				previousScrollPosition = e.getValue();

				Color targetColor = defaultBackground;
				if (scrollUpAdjustments >= 10) {
					targetColor = SCROLL_LOCKED;
				}

				if (!chatLog.getBackground().equals(targetColor)) {
					chatLog.setBackground(targetColor);
					// Force the entire contents to repaint
					logScrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
					chatLog.repaint();
					// Restore the fast-scroll mode.
					logScrollPane.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
				}
			}
		});

		JPanel chatPanel = new JPanel(new BorderLayout(5, 5));
		chatPanel.add(logScrollPane, BorderLayout.CENTER);
		chatPanel.add(inputPanel, BorderLayout.SOUTH);

		// User Table
		TableSorter userTableSorter = new TableSorter(chatDoc.getFilteredUserDataModel());
		userTable = new JTable(userTableSorter) {
			@Override
			public boolean getScrollableTracksViewportWidth() {
				return false;
			}
		};
		userTable.setFocusable(false);
		userTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}

			private void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					JPopupMenu userMenu = new JPopupMenu();

					int userRow = userTable.rowAtPoint(new Point(e.getX(), e.getY()));
					final String userName = userTable.getValueAt(userRow, 0).toString();

					userMenu.add(new JMenuItem(new AbstractAction("Private Chat with " + userName) {
						@Override
						public void actionPerformed(ActionEvent e) {
							transceiver.command("/priv " + userName, chatDoc);
						}
					}));
					userMenu.addSeparator();
					userMenu.add(new JMenuItem(new AbstractAction("Watch User: " + userName) {
						@Override
						public void actionPerformed(ActionEvent e) {
							transceiver.command("/watch " + userName, chatDoc);
						}
					}));
					userMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		userTableSorter.addMouseListenerToHeaderInTable(userTable);
		userTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		userTable.setPreferredScrollableViewportSize(new Dimension(150, 450));


		// Final setup for the panel
		JSplitPane chatUserSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatPanel, new JScrollPane(userTable));
		chatUserSplit.setBorder(BorderFactory.createEmptyBorder());
		chatUserSplit.setResizeWeight(0.80);

		add(chatWithPanel, BorderLayout.NORTH);
		add(chatUserSplit, BorderLayout.CENTER);

		// Set up the thread for scrolling the log. It has a delay of .01 sec.
		scrollTimer = new Timer(10, new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JScrollBar verticalScrollBar = logScrollPane.getVerticalScrollBar();
				if (scrollUpAdjustments <= 10) {
					verticalScrollBar.setValue(verticalScrollBar.getMaximum());
					scrollUpAdjustments = 0;
				}
				scrollTimer.stop();
			}
		});

		// Add the document listener to scroll the log when new text is added.
		chatDoc.addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				scrollTimer.start();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});
	}

	@Override
	public void updateUI() {
		super.updateUI();
		defaultBackground = UIManager.getColor("EditorPane.background");
	}

	@Override
	public void addNotify() {
		super.addNotify();
		transceiver.addChatDocument(chatDoc);
		SwingUtilities.getWindowAncestor(this).addWindowListener(windowListener);
	}

	@Override
	public void removeNotify() {
		transceiver.removeChatDocument(chatDoc);
		SwingUtilities.getWindowAncestor(this).removeWindowListener(windowListener);
		super.removeNotify();
	}

	/**
	 * Crazy implementation for the copy menu.
	 * This this will copy either from the chat log or the chat input.
	 */
	public void copy() {
		if (chatInput.getSelectionStart() != chatInput.getSelectionEnd()) {
			chatInput.copy();
		} else {
			chatLog.copy();
		}
	}

	/**
	 * Support for the paste menu, it pastes to the only logical place - the
	 * chat input field.
	 */
	public void paste() {
		chatInput.paste();
	}

	/**
	 * Cuts selected text in the same manner as <code>copy()</code>.
	 */
	public void cut() {
		if (chatInput.getSelectionStart() != chatInput.getSelectionEnd()) {
			chatInput.cut();
		} else {
			chatLog.copy();
		}
	}

	/**
	 * Sets the font used for the chat input and chatlog.
	 */
	public void setChatFont(Font chatFont) {
		try {
			chatLog.setFont(chatFont);
			chatInput.setFont(chatFont);
			// Invalidate Layout?
		} catch (NullPointerException npe) {
		}
		this.validate();
	}

	private void updateUsers() {
		String[] sessionsOrNames = chatWithSessions.getText().trim().split(" ");

		// Normalize the text to be all sessionIds.
		StringBuilder sessions = new StringBuilder();
		boolean prefix = false;
		for (int i = 0; i < sessionsOrNames.length; i++) {
			if (prefix) {
				sessions.append(" ");
				prefix = false;
			}

			if (sessionsOrNames[i].matches("[0-9]*")) {
				sessions.append(sessionsOrNames[i]);
				prefix = true;
			} else {
				// See if we can find a match by treating it as a userName
				String sessionId =
						chatDoc.getFilteredUserDataModel().getUserDataModel().findSessionByName(sessionsOrNames[i]);
				if (!"".equals(sessionId)) {
					sessions.append(sessionId);
					prefix = true;
				}
			}
		}

		// Set the filter.
		chatDoc.getFilteredUserDataModel().setSessionIds(sessions.toString().trim());

		// Trim the text.
		chatWithSessions.setText(chatWithSessions.getText().trim());
	}

	/**
	 * Forces a UI update.
	 */
	public void updateLafSetting() {
		SwingUtilities.updateComponentTreeUI(this.getRootPane().getParent());
	}
}
