package org.beShare.gui;

import blv.swing.AboutDialog;
import com.meyer.muscle.message.Message;
import com.meyer.muscle.support.Rect;
import org.beShare.gui.prefPanels.JavaSharePrefListener;
import org.beShare.network.JavaShareTransceiver;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;


/**
 * AppPanel.java - JavaShare 2's main hub panel.
 *
 * @author Bryan Varner
 */
public class AppPanel extends JPanel implements ActionListener, JavaSharePrefListener {

	public static final String AutoUpdateURL = "http://beshare.tycomsystems.com/servers.txt";

	private static Map<String, ImageIcon> imageCache = Collections.synchronizedMap(new HashMap<String, ImageIcon>());

	Message programPrefsMessage;

	JSplitPane queryChatSplit;

	boolean saveUserSort;
	LocalUserPanel localUserInfo;
	JavaShareTransceiver transceiver;
	AboutDialog aboutJavaShare;

	TransferPanel transPan;

	ChatMessagingPanel chatterPanel;

	Hashtable aliasTable;
	Object menuBar;
	Vector chatMessageListenerVect;
	Vector soundTriggerListenerVect;

	Vector autoPrivVect;
	Vector watchPatVect;
	Vector ignorePatVect;
	Vector onLoginVect;

	Timer autoAwayTimer;

	Calendar calendar = Calendar.getInstance();


	/**
	 * Creates a new AppPanel based on the fields stored in <code>prefsMessage
	 * </code> any fields that are required but are not present are filled in
	 * with default values.
	 */
	public AppPanel(final JavaShareTransceiver transceiver, final Message prefsMessage) {
		this.programPrefsMessage = prefsMessage;

		// Retreive the SOCKS settings.
		String svr = programPrefsMessage.getString("socksServer", "");
		int port = programPrefsMessage.getInt("socksPort", -1);

		if (!svr.equals("") && port != -1) {
			Properties prop = System.getProperties();
			prop.put("sockProxyHost", svr);
			prop.put("sockProxyPort", "" + port);
			System.setProperties(prop);
		}

		this.transceiver = transceiver;

// Add support for the beoslaf.jar
//        try {
//            ClassLoader.getSystemClassLoader().loadClass("com.sun.java.swing.plaf.beos.BeOSLookAndFeel");
//            UIManager.installLookAndFeel(new UIManager.LookAndFeelInfo("BeOS R5", "com.sun.java.swing.plaf.beos.BeOSLookAndFeel"));
//        } catch (Exception e) {
//        }

		// First we need to check our L&F setting!
		if (programPrefsMessage.hasField("LaF")) {
			try {
				UIManager.setLookAndFeel(programPrefsMessage.getString("LaF", UIManager.getCrossPlatformLookAndFeelClassName()));
			} catch (Exception ex) {
				System.err.println("Failed to set LookandFeel: " + ex.getMessage());
			}
		}


		String[] serverList = new String[]{"beshare.tycomsystems.com"};
		String[] nickList = new String[]{"Binky"};
		String[] statusList = new String[]{"Here", "Away"};

		// Servers
		serverList = programPrefsMessage.getStrings("servers", serverList);
		transceiver.setServerName(serverList[programPrefsMessage.getInt("curServer", 0)]);

		// Nicknames
		nickList = programPrefsMessage.getStrings("nicks", nickList);
		transceiver.setLocalUserName(nickList[programPrefsMessage.getInt("curNick", 0)]);

		// Status
		statusList = programPrefsMessage.getStrings("status", statusList);
		transceiver.setLocalUserStatus(statusList[programPrefsMessage.getInt("curStatus", 0)]);

		this.localUserInfo = new LocalUserPanel(transceiver, false);
		this.localUserInfo.setHereStatus(programPrefsMessage.getInt("curStatus", 0));
		this.localUserInfo.setAwayStatus(programPrefsMessage.getInt("awayStatus", 0));

		this.setLayout(new BorderLayout());
		chatMessageListenerVect = new Vector();
		soundTriggerListenerVect = new Vector();

		// add our list data to the localUserInfo...
		for (int x = 0; x < serverList.length; x++) {
			if (!serverList[x].equals(transceiver.getServerName())) {
				localUserInfo.addServerName(serverList[x]);
			}
		}

		for (int x = 0; x < nickList.length; x++) {
			if (!nickList[x].equals(transceiver.getLocalUserName())) {
				localUserInfo.addUserName(nickList[x]);
			}
		}

		for (int x = 0; x < statusList.length; x++) {
			if (!statusList[x].equals(transceiver.getLocalUserStatus())) {
				localUserInfo.addStatus(statusList[x]);
			}
		}

		// Load the auto-priv regular expressions.
		autoPrivVect = new Vector();
		String[] strPrivs = null;
		strPrivs = programPrefsMessage.getStrings("autoPriv", strPrivs);
		if (strPrivs != null) {
			for (int x = 0; x < strPrivs.length; x++) {
				if (strPrivs[x].startsWith("(?:")) {
					strPrivs[x] = strPrivs[x].substring(3, strPrivs[x].length() - 1);
				}
//				try {
//					autoPrivVect.addElement(new RE(strPrivs[x]));
//				} catch (REException ree) {
//				}
			}
		}

		// Load the watch patterns
		watchPatVect = new Vector();
		String[] strWatch = null;
		strWatch = programPrefsMessage.getStrings("watchPat", strWatch);
		if (strWatch != null) {
			for (int x = 0; x < strWatch.length; x++) {
				if (strWatch[x].startsWith("(?:")) {
					strWatch[x] = strWatch[x].substring(3, strWatch[x].length() - 1);
				}
//				try {
//					watchPatVect.addElement(new RE(strWatch[x]));
//				} catch (REException ree) {
//				}
			}
		}

		// Load the ignore patterns
		ignorePatVect = new Vector();
		String[] strIgnore = null;
		strIgnore = programPrefsMessage.getStrings("ignorePat", strIgnore);
		if (strIgnore != null) {
			for (int x = 0; x < strIgnore.length; x++) {
				if (strIgnore[x].startsWith("(?:")) {
					strIgnore[x] = strIgnore[x].substring(3, strIgnore[x].length() - 1);
				}
//				try {
//					ignorePatVect.addElement(new RE(strIgnore[x]));
//				} catch (REException ree) {
//				}
			}
		}

		// Load the on-login commands.
		onLoginVect = new Vector();
		String[] strOnLogin = null;
		strOnLogin = programPrefsMessage.getStrings("onLogin", strOnLogin);
		if (strOnLogin != null) {
			for (int x = 0; x < strOnLogin.length; x++) {
				onLoginVect.addElement(strOnLogin[x]);
			}
		}

		// Load the alias info.
		aliasTable = new Hashtable();
		String[] keys = programPrefsMessage.getStrings("aliasKeys", null);
		String[] elements = programPrefsMessage.getStrings("aliasElements", null);
		if (keys != null && elements != null) {
			for (int x = 0; x < keys.length; x++) {
				aliasTable.put(keys[x], elements[x]);
			}
		}

		// Create the chat panel and restore the split position if one is available.
		chatterPanel = new ChatMessagingPanel(transceiver);
//		int divider = MusclePreferenceReader.getInt(programPrefsMessage, "chatDivider", -1);
//		if (divider != -1) {
//			chatterPanel.setDividerLocation(divider);
//		}

		// User Table Sorting and column widths.
//		saveUserSort = MusclePreferenceReader.getBoolean(programPrefsMessage, "userSort", false);
//		if (saveUserSort) {
//			chatterPanel.setUserListSortColumn(MusclePreferenceReader.getInt(programPrefsMessage, "sortUserTable", 0));
//		}
//		chatterPanel.setUserTableColumnWidths(MusclePreferenceReader.getInts(programPrefsMessage, "userTableColumnWidths", null));

		// Set the font.
		if (programPrefsMessage.hasField("fontName")) {
			try {
				updateChatFont(new Font(programPrefsMessage.getString("fontName"),
						                       programPrefsMessage.getInt("fontStyle"),
						                       programPrefsMessage.getInt("fontSize")));
			} catch (Exception e) {
			}
		}

		// Create the sound listener.
//		soundPackChange(MusclePreferenceReader.getString(programPrefsMessage, "soundPack", "Default"));

		menuBar = null;
		this.add(localUserInfo, BorderLayout.NORTH);

		transPan = new TransferPanel(this.transceiver);

		queryChatSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		queryChatSplit.add(transPan);
		queryChatSplit.add(chatterPanel);
		this.add(queryChatSplit, BorderLayout.CENTER);

		SwingUtilities.updateComponentTreeUI(this);

		this.transceiver.logSystemMessage("Welcome to JavaShare!\n" +
				                                  "Type /help for a list of commands.");

		this.transceiver.setServerPort(2960);
		if (programPrefsMessage.getBoolean("autoLogin", false)) {
			transceiver.connect();
		}


	}

	/**
	 * Gets an ImageIcon specified by <code>name</code> for </code>cmp</code>
	 */
	public static ImageIcon loadImage(String name, Component cmp) {
		ImageIcon img = imageCache.get(name);

		if (img == null) {
			URLClassLoader urlLoader = (URLClassLoader) cmp.getClass().getClassLoader();
			URL fileLoc = urlLoader.findResource(name);

			if (fileLoc != null) {
				img = new ImageIcon(fileLoc);
				imageCache.put(name, img);
			}
		}

		return img;
	}

	/**
	 * Retreives all prefrences, saves them in a <code>Message</code> and
	 * flattens it into a file in the current Users home directory.
	 */
	public void savePrefs() {
		String prefsFile = System.getProperty("user.home")
				                   + System.getProperty("file.separator")
				                   + ".JavaSharePrefs.dat";
		FileOutputStream fileStream = null;
		DataOutputStream prefsOutStream = null;
		try {
			fileStream = new FileOutputStream(prefsFile, false);
			prefsOutStream = new DataOutputStream(fileStream);
		} catch (FileNotFoundException fnfe) {
			// Could not open file! There's really nothing I can do about this.
		} catch (SecurityException se) {
			// SECURITY EXCEPTION! Again, nothing I can do.
		}
		// If all objects were created properly, this won't be null, so save!
		if (prefsOutStream != null) {
			// Add preferences from the user information.
			programPrefsMessage.setStrings("servers", localUserInfo.getServerList());
			if (localUserInfo.getCurrentServer() == -1) {
				programPrefsMessage.setInt("curServer", 0);
			} else {
				programPrefsMessage.setInt("curServer", localUserInfo.getCurrentServer());
			}
			programPrefsMessage.setStrings("nicks", localUserInfo.getNicksList());
			if (localUserInfo.getCurrentNick() == -1) {
				programPrefsMessage.setInt("curNick", 0);
			} else {
				programPrefsMessage.setInt("curNick", localUserInfo.getCurrentNick());
			}
			programPrefsMessage.setStrings("status", localUserInfo.getStatusList());
			if (localUserInfo.getCurrentStatus() == -1) {
				programPrefsMessage.setInt("curStatus", 0);
			} else {
				programPrefsMessage.setInt("curStatus", localUserInfo.getHereStatus());
			}
			programPrefsMessage.setBoolean("isAway", localUserInfo.isAway());
			programPrefsMessage.setInt("awayStatus", localUserInfo.getAwayStatus());
			programPrefsMessage.setStrings("autoPriv",
					                              getStringArrFromVect(autoPrivVect));
			programPrefsMessage.setStrings("watchPat",
					                              getStringArrFromVect(watchPatVect));
			programPrefsMessage.setStrings("ignorePat",
					                              getStringArrFromVect(ignorePatVect));
			programPrefsMessage.setStrings("onLogin",
					                              getStringArrFromVect(onLoginVect));
			programPrefsMessage.setStrings("aliasKeys", getKeysAsStringArr(aliasTable));
			programPrefsMessage.setStrings("aliasElements", getElementsAsStringArr(aliasTable));
			Rectangle frameBounds = ((ShareFrame) this.getRootPane().getParent()).getBounds();
			Rect fBounds = new Rect(frameBounds.x, frameBounds.y,
					                       frameBounds.width,
					                       frameBounds.height);
			programPrefsMessage.setRect("mainWindowRect", fBounds);
//			programPrefsMessage.setInt("chatDivider", chatterPanel.getDividerLocation());

			// User Table Sorting
//			programPrefsMessage.setInt("sortUserTable", chatterPanel.getUserListSortColumn());
			programPrefsMessage.setBoolean("userSort", saveUserSort);

			// User table column widths.
//			programPrefsMessage.setInts("userTableColumnWidths", chatterPanel.getUserTableColumnWidths());

			// Transfer Panel settings
			if (transPan != null) {
				programPrefsMessage.setInt("transferSplit", transPan.getDividerLocation());
			}
			try {
				programPrefsMessage.flatten(prefsOutStream);
			} catch (IOException ioe) {
				// There was an error writing the prefs to the stream (file)
				// Go Figure, nothing I can do. I believe the phrase for this
				// is 'aah, screw it.'
			}
		}
	}

	/**
	 * Updates the Font for text entry and display with all Messageing panels via their posters.
	 */
	public void updateChatFont(Font f) {
		programPrefsMessage.setString("fontName", f.getName());
		programPrefsMessage.setInt("fontSize", f.getSize());
		programPrefsMessage.setInt("fontStyle", f.getStyle());

		for (int x = 0; x < chatMessageListenerVect.size(); x++) {
//			((ChatPoster) chatMessageListenerVect.elementAt(x)).setChatFont(f);
		}
	}

	/**
	 * Takes a vector of Server Names and adds them to the server list.
	 *
	 * @param servers A vector of server names as Strings
	 */
	public void addServers(Vector servers) {
		for (int x = 0; x < servers.size(); x++) {
			if (localUserInfo.addServerName((String) servers.elementAt(x))) {
				transceiver.logSystemMessage("Added server: " + (String) servers.elementAt(x));
			}
		}
	}

	/**
	 * Takes a vector of server names and removes them from the server list.
	 *
	 * @param servers a vector of server names as Strings
	 */
	public void removeServers(Vector servers) {
		for (int x = 0; x < servers.size(); x++) {
			if (localUserInfo.removeServerName((String) servers.elementAt(x))) {
				transceiver.logSystemMessage("Removed server: " + (String) servers.elementAt(x));
			}
		}
	}

	/**
	 * Returns a <code>String</code> array from given <code>Vector</code>
	 *
	 * @param v The Vector to return strings from.
	 * @return A String array of the contents of the vectore. All conversion is
	 * done by invoking the <code>toString()</code> method.
	 */
	public String[] getStringArrFromVect(Vector v) {
		String[] stringList = new String[v.size()];
		for (int x = 0; x < v.size(); x++) {
			stringList[x] = v.elementAt(x).toString();
		}
		return stringList;
	}

	/**
	 * Returns a String array of the keys of a Hashtable.
	 *
	 * @param h The Hashtable to get the keys from.
	 * @return A String array of the <code>h</code>'s keys.
	 */
	public String[] getKeysAsStringArr(Hashtable h) {
		String[] stringList = new String[h.size()];
		Enumeration enu = h.keys();
		for (int x = 0; x < h.size(); x++) {
			stringList[x] = enu.nextElement().toString();
		}
		return stringList;
	}

	/**
	 * Returns a String array of the elements of a Hashtable.
	 *
	 * @param h The Hashtable to get the elements from.
	 * @return A String array of the <code>h</code>'s elements.
	 */
	public String[] getElementsAsStringArr(Hashtable h) {
		String[] stringList = new String[h.size()];
		Enumeration enu = h.elements();
		for (int x = 0; x < h.size(); x++) {
			stringList[x] = enu.nextElement().toString();
		}
		return stringList;
	}

	// -------------------
	// Preference Listener
	// -------------------

	/**
	 * Implementation of ActionListener
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == "menuConnect") {
			transceiver.connect();
		} else if (e.getActionCommand() == "menuDisconnect") {
			transceiver.disconnect();
			if (transPan != null) {
				transPan.clearQueryResults();
			}
			transceiver.logError("You are diconnected from the MUSCLE server.");
		} else if (e.getActionCommand() == "menuQuit") {
			Window root = SwingUtilities.getWindowAncestor(this);
			root.dispatchEvent(new WindowEvent(root, WindowEvent.WINDOW_CLOSING));
		} else if (e.getActionCommand() == "menuPrivate") {
//			PrivateFrame privFrame = new PrivateFrame(this,
//					                                         this, " ");
//			privFrame.pack();
//			privFrame.show();
		} else if (e.getActionCommand() == "menuClear") {
			chatterPanel.chatDoc.clear();
		} else if (e.getActionCommand() == "menuAbout") {
			aboutJavaShare.setVisible(true);
		} else if (e.getActionCommand() == "cut") {
			chatterPanel.cut();
		} else if (e.getActionCommand() == "copy") {
			chatterPanel.copy();
		} else if (e.getActionCommand() == "paste") {
			chatterPanel.paste();
		} else if (e.getActionCommand() == "prefs") {
//			prefsFrame.setVisible(true);
		} else if (e.getSource() == autoAwayTimer) {
			// Set the status to away and stop the timer. No need to run it.
			try {
				localUserInfo.setAwayStatus(true);
				autoAwayTimer.stop();
			} catch (NullPointerException npe) {
				autoAwayTimer.stop();
				// This happens if the auto-away is disabled.
				// It's not any type of a problem. The timer just isn't created
				// unless we need it.
			}
		}
	}

	/**
	 * AutoAway preference has Changed.
	 *
	 * @param time The new timer delay in seconds.
	 */
	public void autoAwayTimerChange(int time, int selectedIndex) {
		if (autoAwayTimer != null) {
			if (time > 0) {
				autoAwayTimer.setDelay(time);
			} else {
				autoAwayTimer.stop();
			}
		} else {
			autoAwayTimer = new Timer(time, this);
		}
		try {
			programPrefsMessage.setInt("awayTime", time);
			programPrefsMessage.setInt("awayTimeIndex", selectedIndex);
		} catch (Exception e) {
		}
	}

	/**
	 * Auto-Update server on startup has changed.
	 *
	 * @param autoUpdate <code>True</code> if we should update, <code>false</code> if we shouldn't.
	 */
	public void autoUpdateServerChange(boolean autoUpdate) {
		try {
			programPrefsMessage.setBoolean("autoUpdServers", autoUpdate);
		} catch (Exception e) {
		}
	}

	/**
	 * Firewall setting changed.
	 *
	 * @param firewalled Boolean representing the on/off status of firewalling.
	 */
	public void firewallSettingChange(boolean firewalled) {
		try {
			programPrefsMessage.setBoolean("firewalled", firewalled);
			transceiver.setFirewalled(firewalled);
			transPan.resetShareList();
		} catch (Exception e) {
		}
	}

	/**
	 * Automatic Login on startup setting changed.
	 *
	 * @param login Boolean representing the on/off status of auto-login.
	 */
	public void loginOnStartupChange(boolean login) {
		try {
			programPrefsMessage.setBoolean("autoLogin", login);
		} catch (Exception e) {
		}
	}

	/**
	 * Save User Sorting
	 *
	 * @param sort true to save the setting, false to not save it.
	 */
	public void userSortChange(boolean sort) {
		saveUserSort = sort;
	}

	/**
	 * The Bandwidth setting has changed.
	 *
	 * @param label The 'name' of the connection type. T1, Cable, etc.
	 * @param speed The Speed in bps of the connection.
	 */
	public void bandwidthChange(String label, int speed, int index) {
		try {
			programPrefsMessage.setString("uploadLabel", label);
			programPrefsMessage.setInt("uploadValue", speed);
			programPrefsMessage.setInt("uploadBw", index);
			if (transceiver != null) {
				transceiver.setUploadBandwidth(
						                              programPrefsMessage.getString("uploadLabel"),
						                              programPrefsMessage.getInt("uploadValue"));
			}
		} catch (Exception e) {
		}
	}

	/**
	 * Receives a SOCKS setting change. if <code>port</code> = -1, disable the SOCKS.
	 *
	 * @param server The name (or ip address) of the SOCKS server.
	 * @param port   The port the SOCKS server listens on. Typically 1080.
	 */
	public void socksChange(String server, int port) {
		try {
			programPrefsMessage.setString("socksServer", server);
			programPrefsMessage.setInt("socksPort", port);
		} catch (Exception e) {
		}
	}

	/**
	 * The Time-stamp display option has been changed.
	 */
	public void timeStampDisplayChange(boolean show) {
		try {
			programPrefsMessage.setBoolean("dispTime", show);
		} catch (Exception e) {
		}
	}

	/**
	 * The display User Event option has changed.
	 */
	public void userEventDisplayChange(boolean show) {
		try {
			programPrefsMessage.setBoolean("dispUser", show);
		} catch (Exception e) {
		}
	}

	/**
	 * The Display Upload Events option has changed.
	 */
	public void uploadDisplayChange(boolean show) {
		try {
			programPrefsMessage.setBoolean("dispUpload", show);
		} catch (Exception e) {
		}
	}

	/**
	 * The Disply Chat option has changed.
	 */
	public void chatDisplayChange(boolean show) {
		try {
			programPrefsMessage.setBoolean("dispChat", show);
		} catch (Exception e) {
		}
	}

	/**
	 * The display Private messages option has changed.
	 */
	public void privateDisplayChange(boolean show) {
		try {
			programPrefsMessage.setBoolean("dispPriv", show);
		} catch (Exception e) {
		}
	}

	/**
	 * The Display Information messages option has changed.
	 */
	public void infoDisplayChange(boolean show) {
		try {
			programPrefsMessage.setBoolean("dispInfo", show);
		} catch (Exception e) {
		}
	}

	/**
	 * the Display Warning messages option has changed.
	 */
	public void warningDisplayChange(boolean show) {
		try {
			programPrefsMessage.setBoolean("dispWarn", show);
		} catch (Exception e) {
		}
	}

	/**
	 * the Display Error Messages option has changed.
	 */
	public void errorDisplayChange(boolean show) {
		try {
			programPrefsMessage.setBoolean("dispError", show);
		} catch (Exception e) {
		}
	}

	/**
	 * the Look and Feel setting changed.
	 */
	public void lafChange(String plafClassName) {
//		try {
//			UIManager.setLookAndFeel(plafClassName);
//			for (int x = 0; x < chatMessageListenerVect.size(); x++) {
//				((ChatPoster) chatMessageListenerVect.elementAt(x)).updateLafSetting();
//			}
//			SwingUtilities.updateComponentTreeUI(prefsFrame);
//			SwingUtilities.updateComponentTreeUI(aboutJavaShare);
//			prefsFrame.pack();
//			aboutJavaShare.pack();
//			programPrefsMessage.setString("LaF", plafClassName);
//		} catch (Exception e) {
//			System.out.println(e.toString());
//		}
	}

	/**
	 * Sound pack changed.
	 */
	public void soundPackChange(String soundPackName) {
//		try {
//			if (soundTriggerListenerVect.size() == 0) {
//				if (soundPackName.equals("System Beep")) {
//					SystemBeep soundPlayer = new SystemBeep();
//					addSoundEventListener(soundPlayer);
//				} else {
//					ApplicationSoundThreadManager soundPlayer = new ApplicationSoundThreadManager(soundPackName);
//					addSoundEventListener(soundPlayer);
//				}
//			}
//			programPrefsMessage.setString("soundPack", soundPackName);
//			updateActiveSoundPack(soundPackName);
//		} catch (Exception e) {
//		}
	}

	/**
	 * Sound on User Name event setting changed.
	 */
	public void soundOnUserNameChange(boolean signal) {
		try {
			programPrefsMessage.setBoolean("sndUName", signal);
		} catch (Exception e) {
		}
	}

	/**
	 * sound on Private Window popup setting changed.
	 */
	public void soundOnPrivateWindowChange(boolean signal) {
		try {
			programPrefsMessage.setBoolean("prvWSnd", signal);
		} catch (Exception e) {
		}
	}

	/**
	 * sound on Priave Message setting changed.
	 */
	public void soundOnPrivateMessageChange(boolean signal) {
		try {
			programPrefsMessage.setBoolean("prvMSnd", signal);
		} catch (Exception e) {
		}
	}

	/**
	 * sound on Watch pattern match setting changed.
	 */
	public void soundOnWatchPatternChange(boolean signal) {
		try {
			programPrefsMessage.setBoolean("sndWPat", signal);
		} catch (Exception e) {
		}
	}

	/**
	 * Implementation of ChatMessageListener
	 */
//	public void chatMessage(ChatMessage newMessage) {
//		// If the timer is set to be disabled, leave it. If not, restart it.
//		if (autoAwayTimer != null) {
//			try {
//				if (programPrefsMessage.getInt("awayTime") == -1) {
//					autoAwayTimer.stop();
//				} else {
//					autoAwayTimer.restart();
//				}
//			} catch (Exception e) {
//			}
//		}
//		if (localUserInfo.isAway()) {
//			localUserInfo.setAwayStatus(false);
//		}
//		if (newMessage.getType() == ChatMessage.PRIVATE_LOCAL_LOG_ONLY) {
//			// Intercept any local log only's comming from private chat panels.
//			// Transform them into Log Local User chats, and send them
//			// to the panels, but NOT the MuscleInterface
//			newMessage.setType(ChatMessage.LOG_LOCAL_USER_CHAT_MESSAGE);
//			newMessage.setSession("(" + transceiver.getLocalSessionID() + ") " + transceiver.getLocalUserName());
//			fireLogNewMessage(newMessage);
//		} else if (newMessage.getMessage().startsWith("/")) {
//			// Get command message here.
//			String commandLine = newMessage.getMessage();
//			commandLine = commandLine.substring(0);
//			// Make sure there's something after the command
//			String command = "";
//			if (commandLine.indexOf(" ") != -1) {
//				command = commandLine.substring(0, commandLine.indexOf(" ")).toUpperCase();
//				commandLine = commandLine.substring(command.length() + 1).trim();
//			} else {
//				command = commandLine.toUpperCase().trim();
//			}
//			// We have a Messaging command.
//			if (command.equals("/MSG")) {
//				String targetSession = transceiver.getUserDataModel().findSessionByName(commandLine);
//				if (!targetSession.equals("")) {
//					newMessage.setMessage(commandLine.substring(transceiver.getUserDataModel().findNameBySession(targetSession).length()).trim());
//					transceiver.sendTextMessage(newMessage.getMessage(), targetSession.trim());
//					if (newMessage.getType() == ChatMessage.LOG_LOCAL_USER_CHAT_MESSAGE) {
//						newMessage.setSession("(" + transceiver.getLocalSessionID() + ") " + transceiver.getLocalUserName() + " -> (" + transceiver.getUserDataModel().findNameBySession(targetSession.trim()) + ")");
//						newMessage.setLocalMessage(true);
//					}
//					newMessage.setPrivate(true);
//					fireLogNewMessage(newMessage);
//				} else {
//					// Eliminate anything after the ID - parsing by spaces.
//					if (commandLine.indexOf(" ") != -1) {
//						targetSession = commandLine.substring(0,
//								                                     commandLine.indexOf(" "));
//					} else {
//						targetSession = commandLine;
//					}
//					// See if the session exists!
//					if (!transceiver.getUserDataModel().findNameBySession(targetSession).equals("")) {
//						newMessage.setMessage(commandLine.substring(targetSession.length()).trim());
//						transceiver.sendTextMessage(newMessage.getMessage(), targetSession.trim());
//						if (newMessage.getType() == ChatMessage.LOG_LOCAL_USER_CHAT_MESSAGE) {
//							newMessage.setSession("(" + transceiver.getLocalSessionID() + ") " + transceiver.getLocalUserName() + " -> (" + transceiver.getUserDataModel().findNameBySession(targetSession.trim()) + ")");
//							newMessage.setLocalMessage(true);
//						}
//						newMessage.setPrivate(true);
//						fireLogNewMessage(newMessage);
//					} else {
//						// Could not find a name or session that matches.
//						fireLogNewMessage(new ChatMessage(transceiver, newMessage.getSession(),
//								                                 "Couldn't find specified user.", false,
//								                                 ChatMessage.LOG_ERROR_MESSAGE, true, newMessage.getTargetID()));
//					}
//				}
//			} else if (command.equals("/AWAY")) {
//				localUserInfo.setAwayStatus(!localUserInfo.isAway());
//			} else if (command.equals("/AWAYMSG")) {
//				if (!commandLine.toUpperCase().equals("/AWAYMSG")) {
//					localUserInfo.setAwayStatus(commandLine);
//					fireLogNewMessage(new ChatMessage(transceiver, ""
//							                                 , "Auto-away message set to " + commandLine
//							                                 , false, ChatMessage.LOG_INFORMATION_MESSAGE));
//				}
//			} else if (command.equals("/ALIAS")) {
//				if (!commandLine.toUpperCase().equals("/ALIAS")) {
//					StringTokenizer tokenizer = new StringTokenizer(commandLine, "=, ");
//					while (tokenizer.hasMoreTokens() && ((tokenizer.countTokens() % 2) == 0)) {
//						String key = tokenizer.nextToken();
//						String value = tokenizer.nextToken();
//						aliasTable.put(key, value);
//						fireLogNewMessage(new ChatMessage(transceiver, ""
//								                                 , "Set alias " + key + " = " + value
//								                                 , false, ChatMessage.LOG_INFORMATION_MESSAGE));
//					}
//				}
//			} else if (command.equals("/UNALIAS")) {
//				if (!commandLine.toUpperCase().equals("/UNALIAS")) {
//					if (aliasTable.containsKey(commandLine)) {
//						aliasTable.remove(commandLine);
//						fireLogNewMessage(new ChatMessage(transceiver, ""
//								                                 , "Removed alias " + commandLine
//								                                 , false, ChatMessage.LOG_INFORMATION_MESSAGE));
//					}
//				}
//			} else if (command.equals("/PRIV")) {
//				// Lookup by name first.
//				String targetSession = transceiver.getUserDataModel().findSessionByName(commandLine);
//				if (!targetSession.equals("")) {
//					// construct new Private Frame
//					PrivateFrame privFrame = new PrivateFrame(this,
//							                                         this, targetSession);
//					privFrame.pack();
//					privFrame.show();
//				} else {
//					// The name lookup failed, we'll try to lookup by session.
//					// Eliminate anything after the ID - parsing by spaces.
//					if (commandLine.indexOf(" ") != -1) {
//						commandLine = commandLine.substring(0,
//								                                   commandLine.indexOf(" "));
//					}
//					// See if the session exists
//					if (!transceiver.getUserDataModel().findNameBySession(commandLine).equals("")) {
//						// We just verified that a user with that ID exists.
//						// Incase the name isn't unique, we're going to stick
//						// with using the session which is stored in commandLine
//						PrivateFrame privFrame;
//						try {
//							privFrame = new PrivateFrame(this,
//									                            this, commandLine,
//									                            new Font(programPrefsMessage.getString("fontName"),
//											                                    programPrefsMessage.getInt("fontStyle"),
//											                                    programPrefsMessage.getInt("fontSize")));
//						} catch (Exception e) {
//							privFrame = new PrivateFrame(this, this, commandLine);
//						}
//						privFrame.pack();
//						privFrame.show();
//					} else {
//						// Could not find a name or session that matches.
//						fireLogNewMessage(new ChatMessage(transceiver, newMessage.getSession(),
//								                                 "Couldn't find specified user.", false,
//								                                 ChatMessage.LOG_ERROR_MESSAGE, true, newMessage.getTargetID()));
//					}
//				}
//			} else if (command.equals("/PING")) {
//				String targetSession = transceiver.getUserDataModel().findSessionByName(commandLine);
//				if (!targetSession.equals("")) {
//					transceiver.pingUser(targetSession);
//				} else {
//					if (commandLine.indexOf(" ") != -1) {
//						targetSession = commandLine.substring(0,
//								                                     commandLine.indexOf(" "));
//					} else {
//						targetSession = commandLine;
//					}
//					// See if the session exists!
//					if (!transceiver.getUserDataModel().findNameBySession(targetSession).equals("")) {
//						transceiver.pingUser(targetSession);
//					} else {
//						// Could not find a name or session that matches.
//						fireLogNewMessage(new ChatMessage(transceiver, newMessage.getSession(),
//								                                 "Couldn't find specified user.", false,
//								                                 ChatMessage.LOG_ERROR_MESSAGE, true, newMessage.getTargetID()));
//					}
//				}
//			} else if (command.equals("/ME")) {
//				transceiver.sendTextMessage(newMessage.getMessage(), "*");
//				fireLogNewMessage(new ChatMessage(transceiver, newMessage.getSession(),
//						                                 newMessage.getMessage(), false
//						                                 , ChatMessage.LOG_LOCAL_USER_CHAT_MESSAGE, true, newMessage.getTargetID()));
//			} else if (command.equals("/ACTION")) {
//				newMessage.setMessage("/me " + commandLine);
//				transceiver.sendTextMessage(newMessage.getMessage(), "*");
//				fireLogNewMessage(new ChatMessage(transceiver, newMessage.getSession(),
//						                                 newMessage.getMessage(), false
//						                                 , ChatMessage.LOG_LOCAL_USER_CHAT_MESSAGE, true, newMessage.getTargetID()));
//			} else if (command.equals("/NICK")) {
//				transceiver.setLocalUserName(commandLine);
//			} else if (command.equals("/STATUS")) {
//				transceiver.setLocalUserStatus(commandLine);
//			} else if (command.equals("/CLEAR")) {
//				fireLogNewMessage(new ChatMessage(transceiver, newMessage.getSession(), ""
//						                                 , false, ChatMessage.LOG_CLEAR_LOG_MESSAGES, false,
//						                                 newMessage.getTargetID()));
//			} else if (command.equals("/HELP")) {
//				fireLogNewMessage(
//						                 new ChatMessage(transceiver, "", "JavaShare 2 Command Refrence\n"
//								                                                  + "       /action <action> - do something\n"
//								                                                  + "       /alias [names and value] - create an alias\n"
//								                                                  + "       /autopriv <names or session ids> - specify AutoPriv users\n"
//								                                                  + "       /away tag - Force away state\n"
//								                                                  + "       /awaymsg tag - change the auto-away tag\n"
//								                                                  + "       /clear - clear the chat log\n"
//								                                                  + "       /clearonlogin - clear startup commands\n"
//								                                                  + "       /connect [serverName] - connect to a server\n"
//								                                                  + "       /disconnect - disconnect from the server\n"
//								                                                  + "       /help - show this help text\n"
//								                                                  + "       /ignore <names or session ids> - specify users to ignore\n"
//								                                                  + "       /me <action> - synonym for /action\n"
//								                                                  + "       /msg <name or session id> <text> - send a private message\n"
//								                                                  + "       /nick <name> - change your user name\n"
//								                                                  + "       /onlogin command - add a startup command\n"
//								                                                  + "       /priv <names or session ids> - Open Private Chat Window\n"
//								                                                  + "       /ping <names or session ids> - ping other clients\n"
//								                                                  + "       /quit - quit BeShare\n"
//								                                                  + "       /serverinfo - Request server status\n"
//								                                                  + "       /status Status - set user status string\n"
//								                                                  + "       /unalias <name> - remove an alias\n"
//								                                                  + "       /watch <name or session ids> - specify users to watch\n"
//								                                                  + "       /server <address> - Sets the server address.\n"
//								                                , false, ChatMessage.LOG_INFORMATION_MESSAGE));
//			} else if (command.equals("/CONNECT")) {
//				if (!commandLine.toUpperCase().equals("/CONNECT")) {
//					transceiver.setServerName(commandLine);
//				}
//				transceiver.connect();
//			} else if (command.equals("/QUIT")) {
//				quitRequested();
//			} else if (command.equals("/DISCONNECT")) {
//				transceiver.disconnect();
//			} else if (command.equals("/SERVER")) {
//				transceiver.setServerName(commandLine);
//			} else if (command.equals("/SERVERINFO")) {
//				// Sends out a serverinfo message
//				transceiver.getServerInfo();
//			} else if (command.equals("/WATCH")) {
//				// If the command is the /WATCH then we'll clear the pattern
//				if (commandLine.toUpperCase().equals("/WATCH")) {
//					while (!watchPatVect.isEmpty()) {
//						watchPatVect.removeElementAt(0);
//					}
//					fireLogNewMessage(new ChatMessage(transceiver, ""
//							                                 , "Watch pattern removed."
//							                                 , false, ChatMessage.LOG_INFORMATION_MESSAGE));
//				} else {
//					// Ok, so now we get to add to the pattern.
//					try {
//						if (commandLine.startsWith("*")) {
//							// Clear the existing stuff
//							while (!watchPatVect.isEmpty()) {
//								watchPatVect.removeElementAt(0);
//							}
//							// Add the new pattern
//							watchPatVect.addElement(new RE("all"
//									                              , RE.REG_ICASE));
//						} else {
//							watchPatVect.addElement(new RE(commandLine, RE.REG_ICASE));
//						}
//						fireLogNewMessage(new ChatMessage(transceiver, ""
//								                                 , "Added watch pattern: " + commandLine
//								                                 , false, ChatMessage.LOG_INFORMATION_MESSAGE));
//					} catch (REException ree) {
//						fireLogNewMessage(new ChatMessage(transceiver, newMessage.getSession(),
//								                                 "Invalid pattern expression", false,
//								                                 ChatMessage.LOG_ERROR_MESSAGE, true, newMessage.getTargetID()));
//					} catch (NullPointerException npe) {
//						fireLogNewMessage(new ChatMessage(transceiver, newMessage.getSession(),
//								                                 "Invalid pattern expression", false,
//								                                 ChatMessage.LOG_ERROR_MESSAGE, true, newMessage.getTargetID()));
//					}
//				}
//			} else if (command.equals("/ONLOGIN")) {
//				if (!commandLine.toUpperCase().equals("/ONLOGIN")) {
//					onLoginVect.addElement(commandLine);
//					fireLogNewMessage(new ChatMessage(transceiver, ""
//							                                 , "Recorded startup command: " + commandLine
//							                                 , false, ChatMessage.LOG_INFORMATION_MESSAGE));
//				} else {
//					fireLogNewMessage(new ChatMessage(transceiver, newMessage.getSession(),
//							                                 "No Login Command Specified", false,
//							                                 ChatMessage.LOG_ERROR_MESSAGE, true, newMessage.getTargetID()));
//				}
//			} else if (command.equals("/CLEARONLOGIN")) {
//				while (!onLoginVect.isEmpty()) {
//					onLoginVect.removeElementAt(0);
//				}
//				fireLogNewMessage(new ChatMessage(transceiver, ""
//						                                 , "Startup commands cleared."
//						                                 , false, ChatMessage.LOG_INFORMATION_MESSAGE));
//			} else if (command.equals("/AUTOPRIV")) {
//				if (commandLine.toUpperCase().equals("/AUTOPRIV")) {
//					while (!autoPrivVect.isEmpty()) {
//						autoPrivVect.removeElementAt(0);
//					}
//					fireLogNewMessage(new ChatMessage(transceiver, ""
//							                                 , "AutoPriv pattern removed."
//							                                 , false, ChatMessage.LOG_INFORMATION_MESSAGE));
//				} else {
//					try {
//						if (commandLine.startsWith("*")) {
//							// Clear out the existing patterns.
//							while (!autoPrivVect.isEmpty()) {
//								autoPrivVect.removeElementAt(0);
//							}
//							// Set it in.
//							autoPrivVect.addElement(new RE("all", RE.REG_ICASE));
//						} else {
//							autoPrivVect.addElement(new RE(commandLine, RE.REG_ICASE));
//						}
//						fireLogNewMessage(new ChatMessage(transceiver, ""
//								                                 , "AutoPriv added pattern: " + commandLine
//								                                 , false, ChatMessage.LOG_INFORMATION_MESSAGE));
//					} catch (REException ree) {
//						fireLogNewMessage(new ChatMessage(transceiver, newMessage.getSession(),
//								                                 "Invalid pattern expression", false,
//								                                 ChatMessage.LOG_ERROR_MESSAGE, true, newMessage.getTargetID()));
//					} catch (NullPointerException npe) {
//						fireLogNewMessage(new ChatMessage(transceiver, newMessage.getSession(),
//								                                 "Invalid pattern expression", false,
//								                                 ChatMessage.LOG_ERROR_MESSAGE, true, newMessage.getTargetID()));
//					}
//				}
//			} else if (command.equals("/IGNORE")) {
//				// If the command is the /IGNORE then we'll clear the pattern
//				if (commandLine.toUpperCase().equals("/IGNORE")) {
//					while (!ignorePatVect.isEmpty()) {
//						ignorePatVect.removeElementAt(0);
//					}
//					fireLogNewMessage(new ChatMessage(transceiver, ""
//							                                 , "Ignore pattern removed."
//							                                 , false, ChatMessage.LOG_INFORMATION_MESSAGE));
//				} else {
//					// Ok, so now we get to add to the pattern.
//					try {
//						if (commandLine.startsWith("*")) {
//							// Clear the existing stuff
//							while (!ignorePatVect.isEmpty()) {
//								ignorePatVect.removeElementAt(0);
//							}
//							// Add the new pattern
//							ignorePatVect.addElement(new RE("all"
//									                               , RE.REG_ICASE));
//						} else {
//							ignorePatVect.addElement(new RE(commandLine, RE.REG_ICASE));
//						}
//						fireLogNewMessage(new ChatMessage(transceiver, ""
//								                                 , "Added ignore pattern: " + commandLine
//								                                 , false, ChatMessage.LOG_INFORMATION_MESSAGE));
//					} catch (REException ree) {
//						fireLogNewMessage(new ChatMessage(transceiver, newMessage.getSession(),
//								                                 "Invalid pattern expression", false,
//								                                 ChatMessage.LOG_ERROR_MESSAGE, true, newMessage.getTargetID()));
//					} catch (NullPointerException npe) {
//						fireLogNewMessage(new ChatMessage(transceiver, newMessage.getSession(),
//								                                 "Invalid pattern expression", false,
//								                                 ChatMessage.LOG_ERROR_MESSAGE, true, newMessage.getTargetID()));
//					}
//				}
//			} else {
//				fireLogNewMessage(new ChatMessage(transceiver, "", "Command not " +
//						                                                   "recognized.", false,
//						                                 ChatMessage.LOG_ERROR_MESSAGE, true, ""));
//			}
//		} else if (newMessage.getMessage().length() > 0) {
//			// Check for Alias matches!
//			String possibleAlias;
//			try {
//				possibleAlias = newMessage.getMessage().trim().substring(0,
//						                                                        newMessage.getMessage().trim().indexOf(" "));
//			} catch (StringIndexOutOfBoundsException sioobe) {
//				possibleAlias = newMessage.getMessage().trim();
//			}
//			if (aliasTable.containsKey(possibleAlias)) {
//				String replacement = (String) aliasTable.get(possibleAlias);
//				newMessage.setMessage(replacement + " "
//						                      + newMessage.getMessage().trim().substring(possibleAlias.length()));
//			}
//
//			transceiver.sendTextMessage(newMessage.getMessage().trim(), "*");
//			// put our session ID and name into the session of any message I type.
//			if (newMessage.getType() == ChatMessage.LOG_LOCAL_USER_CHAT_MESSAGE) {
//				newMessage.setSession("(" + transceiver.getLocalSessionID() + ") " + transceiver.getLocalUserName());
//			}
//			fireLogNewMessage(newMessage);
//		}
//	}

	/**
	 * Tests a ChatMessage across all Regex patters stored in <code>regexVect
	 * </code>. If any pattern matches either the <code>session</code>,
	 * <code>name</code>, or <code>message</code> of the ChatMessage, then it
	 * returns <code>true</code>. If none of the fields mentioned above match,
	 * it returns <code>false</code>
	 *
	 * @param regexVect - A <code>Vector</code> of <code>RE</code> regex objects.
	 * @param temp      the <code>ChatMessage</code> to match patterns with.
	 * @return <code>true</code> if any pattern matches, <code>false</code> if
	 * no patterns match.
	 */
//	public boolean isMatchingPattern(Vector regexVect, ChatMessage temp) {
//		if (regexVect.size() > 0) {
//			if (((RE) regexVect.elementAt(0)).isMatch("all")) {
//				return true;
//			} else {
//				// Time to search for the regex expression.
//				for (int x = 0; x < regexVect.size(); x++) {
//					// Search by name
//					if (((RE) regexVect.elementAt(x)).isMatch((Object) transceiver.getUserDataModel().findNameBySession(temp.getSession()))) {
//						return true;
//						// Search by Session ID
//					} else if (((RE) regexVect.elementAt(x)).isMatch((Object) temp.getSession())) {
//						return true;
//						// Search by message Content
//					} else if (((RE) regexVect.elementAt(x)).isMatch((Object) temp.getMessage())) {
//						return true;
//					}
//				}
//			}
//		}
//		return false;
//	}
}
