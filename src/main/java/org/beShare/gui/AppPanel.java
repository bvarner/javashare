/* Change Log:
 * 3.11.2002 - Version 1.0 Final Released!
 * 4.22.2002 - Updated version Info to reflect work on File Sharing.
 * 5.8.2002  - Added support for the System Beep method of sound events.
 * 5.9.2002  - Re-implemented the time stamp System.currentTimeInMillis().
 *				I haven't found a better way of doing this yet.
 * 5.13.2002 - Updated version information to reflect the latest development build.
 * 6.5.2002 - getCompletedName now returns the entire name, and is case-insensitive.
 *				reworked the calling methods to use this.
 * Begin 2.0 revisions
 * 7.14.2002 - Reworked some details and got Applet compatibility 100% back. Mostly,
 * 				this involves catching NPE's when trying to load ImageIcons.
 *				Also implemented AppletSoundThreadManager in the applet constructor.
 *				Yeah!!!
 * 7.15.2002 - Condensed the two applet constructors into one. Cleaned up, and began to look
 *				at how to include the query panel.
 * 7.16.2002 - FindSessionByName is now case-insensitive.
 * 8.3.2002  - Added getUserData()
 * 8.4.2002 - Now generates an InstallID on first-run.
 * 12.17.2002 - Updated the name completion and related functions to parse names for url [label]
 *				I've also commented out the lines that add file sharing to the application.
 *				I'll be making a 1.3 preliminary release soon.
 *				Seems there may still be a bug In Tranto with regards to uploading the user name to the server.
 * 12.19.2002 - Added addServers() and removeServers().
 *				Implemented the user Table Column Sorting Preference.
 *				Removed some un-needed instance variables.
 * 12.23.2002 - Fixed the mystical no-user name bug that was introduced into the 2.0 code base. The installid
 *				message was deleting all the data alread in /beshare/name. This has been fixed by integrating
 *				the installid into the name message.
 * 12.24.2002 - 1:00 AM - Reworked preference loading to use the new utility class. The code is MUCH cleaner now.
 *				Added the imageCache and loadImage method. All image loading now uses this. This will cut the
 *				overhead and memory useage of the file-sharing, and also makes the Applet properly load images.
 */
package org.beShare.gui;

/**
 *	AppPanel.java - JavaShare 2's main hub panel.
 *					This class handles the translation of incomming
 *					JavaShareEvents into ChatMessages or other froms of
 *					data handling.
 *					
 *					This class does a _LOT_ it's eaiser to understand if you break it down into the various
 *					interfaces it implements.
 *					
 *					This class is responsible for so much, you can honestly say
 *					it's the guts, heart, and soul of the program.
 *
 *	@version 2.0
 *	@author Bryan Varner
 */

import blv.swing.AboutDialog;
import com.meyer.muscle.message.Message;
import com.meyer.muscle.support.Rect;
import gnu.regexp.RE;
import gnu.regexp.REException;
import org.beShare.data.*;
import org.beShare.event.*;
import org.beShare.gui.prefPanels.JavaSharePrefListener;
import org.beShare.network.Tranto;
import org.beShare.sound.AppletSoundThreadManager;
import org.beShare.sound.ApplicationSoundThreadManager;
import org.beShare.sound.SystemBeep;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class AppPanel extends JPanel implements JavaShareEventListener,
												ChatMessageListener,
												ActionListener,
												UserHashAccessor,
												LocalUserDataStore,
												JavaSharePrefListener
{
	public static final String buildVersion = "2.0 a2 ";
	public static final String applicationName = "JavaShare";
	public static final String pubVersion = applicationName + buildVersion;
	
	public static final String AutoUpdateURL = "http://beshare.tycomsystems.com/servers.txt";
	
	public static final int		startingPortNumber = 7000;
	public static final int		portRange = 50;
	
	private static Hashtable	imageCache = new Hashtable();
	
	PrefsFrame 					prefsFrame;
	Message						programPrefsMessage;
	
	JSplitPane					queryChatSplit;
	
	ChatPanel					chatterPanel;
	boolean						saveUserSort;
	boolean						appletMode;
	LocalUserPanel				localUserInfo;
	Tranto						muscleNetIO;
	AboutDialog					aboutJavaShare;
	
	TransferPanel				transPan;
	
	Hashtable					userHashTable;
	Hashtable					aliasTable;
	Object						menuBar;
	Vector						chatMessageListenerVect;
	Vector						soundTriggerListenerVect;
	
	String						localUserName;
	String						localUserStatus;
	String						currentServerName;
	
	Vector						autoPrivVect;
	Vector						watchPatVect;
	Vector						ignorePatVect;
	Vector						onLoginVect;
	
	Timer						autoAwayTimer;
	
	GregorianCalendar			calendar = new GregorianCalendar();
	
	
	
	/**
		Creates a new AppPanel based on the fields stored in <code>prefsMessage
		</code> any fields that are required but are not present are filled in
		with default values.
	*/
	public AppPanel(Message prefsMessage){
		programPrefsMessage = prefsMessage;
		
		// Ok, Install ID Time! Wheeee!!!!
		if (!programPrefsMessage.hasField("installid")){
			int maxint = 2147483647;
			long installID = (((long)(Math.random() * maxint)) << 32)|((long)(Math.random() * maxint));
			programPrefsMessage.setLong("installid", installID);
		}
		
		// Add support for the beoslaf.jar
		try {
			ClassLoader.getSystemClassLoader().loadClass("com.sun.java.swing.plaf.beos.BeOSLookAndFeel");
			UIManager.installLookAndFeel(new UIManager.LookAndFeelInfo("BeOS R5", "com.sun.java.swing.plaf.beos.BeOSLookAndFeel"));
		} catch (Exception e) {
		} catch (NoSuchMethodError nsme) {
			// For pre-1.2 JREs.
		}
		
		// First we need to check our L&F setting!
		if (programPrefsMessage.hasField("LaF")){
			try {
				UIManager.setLookAndFeel(programPrefsMessage.getString("LaF"));
			} catch (Exception e) {
			}
		}
		
		prefsFrame = new PrefsFrame(this, programPrefsMessage);
		
		String[] aboutText = {"JavaShare 2",
								"Version " + buildVersion,
								"",
								"Created By: Bryan Varner",
								"",
								"Inspired by the works of: Jeremy Friesner",
								"",
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
		ImageIcon javaShareIcon = loadImage("Images/BeShare.gif", this);
		aboutJavaShare = new AboutDialog(this,
				"About JavaShare 2",
				true, aboutText, javaShareIcon, 2, 20);
		
		String[] serverList = null;
		String[] nickList = null;
		String[] statusList = null;
		int hereStatus = 0;
		// Retreive the SOCKS settings.
		if(programPrefsMessage.hasField("socksServer") &&
			programPrefsMessage.hasField("socksPort"))
		{
			String svr = MusclePreferenceReader.getString(programPrefsMessage, "socksServer", "");
			int port = MusclePreferenceReader.getInt(programPrefsMessage, "socksPort", -1);
			
			if (!svr.equals("") && port != -1) {
				Properties prop = System.getProperties();
				prop.put("sockProxyHost", svr);
				prop.put("sockProxyPort", "" + port);
				System.setProperties(prop);
			}
		}
		
		// Servers
		serverList = new String[1];
		currentServerName = "";
		serverList = MusclePreferenceReader.getStrings(programPrefsMessage, "servers", serverList);
		currentServerName = serverList[MusclePreferenceReader.getInt(programPrefsMessage, "curServer", 0)];
		
		// Nicknames
		nickList = new String[1];
		hereStatus = 0;
		localUserName = "Binky";
		nickList = MusclePreferenceReader.getStrings(programPrefsMessage, "nicks", nickList);
		hereStatus = MusclePreferenceReader.getInt(programPrefsMessage, "curNick", hereStatus);
		if (hereStatus < nickList.length)
			localUserName = nickList[hereStatus];
		
		// Status
		statusList = new String[1];
		statusList[0] = "Here";
		localUserStatus = "";
		statusList = MusclePreferenceReader.getStrings(programPrefsMessage, "status", statusList);
		localUserStatus = statusList[MusclePreferenceReader.getInt(programPrefsMessage, "curStatus", 0)];
		
		// Minibrowser
		boolean useMiniBrowser = MusclePreferenceReader.getBoolean(programPrefsMessage, "miniBrowser", false);
		
		this.setLayout(new BorderLayout());
		chatMessageListenerVect = new Vector();
		soundTriggerListenerVect = new Vector();
		localUserInfo = new LocalUserPanel(this, false);
		localUserInfo.setHereStatus(hereStatus);
		
		// Set up our Preferences!
		localUserInfo.setAwayStatus(MusclePreferenceReader.getInt(programPrefsMessage, "awayStatus", 0));
		
		// add our list data to the localUserInfo...
		if(serverList != null){
			for(int x = 0; x < serverList.length; x++){
				if(! serverList[x].equals(currentServerName))
						localUserInfo.addServerName(serverList[x]);
			}
		}
		
		if(nickList != null){
			for(int x = 0; x < nickList.length; x++){
				if(! nickList[x].equals(localUserName))
						localUserInfo.addUserName(nickList[x]);
			}
		}
		
		if(statusList != null){
			for (int x = 0; x < statusList.length; x++){
				if(! statusList[x].equals(localUserStatus))
						localUserInfo.addStatus(statusList[x]);
			}
		}
		
		// Load the auto-priv regular expressions.
		autoPrivVect = new Vector();
		String[] strPrivs = null;
		strPrivs = MusclePreferenceReader.getStrings(programPrefsMessage, "autoPriv", strPrivs);
		if (strPrivs != null) {
			for (int x = 0; x < strPrivs.length; x++) {
				if (strPrivs[x].startsWith("(?:"))
					strPrivs[x] = strPrivs[x].substring(3, strPrivs[x].length() - 1);
				try {
					autoPrivVect.addElement(new RE(strPrivs[x]));
				} catch (REException ree){
				}
			}
		}
		
		// Load the watch patterns
		watchPatVect = new Vector();
		String[] strWatch = null;
		strWatch = MusclePreferenceReader.getStrings(programPrefsMessage, "watchPat", strWatch);
		if (strWatch != null ) {
			for (int x = 0; x < strWatch.length; x++) {
				if (strWatch[x].startsWith("(?:"))
					strWatch[x] = strWatch[x].substring(3, strWatch[x].length() - 1);
				try {
					watchPatVect.addElement(new RE(strWatch[x]));
				} catch (REException ree){
				}
			}
		}
		
		// Load the ignore patterns
		ignorePatVect = new Vector();
		String[] strIgnore = null;
		strIgnore = MusclePreferenceReader.getStrings(programPrefsMessage, "ignorePat", strIgnore);
		if (strIgnore != null) {
			for (int x = 0; x < strIgnore.length; x++) {
				if (strIgnore[x].startsWith("(?:"))
					strIgnore[x] = strIgnore[x].substring(3, strIgnore[x].length() - 1);
				try {
					ignorePatVect.addElement(new RE(strIgnore[x]));
				} catch (REException ree){
				}
			}
		}
		
		// Load the on-login commands.
		onLoginVect = new Vector();
		String[] strOnLogin = null;
		strOnLogin = MusclePreferenceReader.getStrings(programPrefsMessage, "onLogin", strOnLogin);
		if (strOnLogin != null) {
			for (int x = 0; x < strOnLogin.length; x++) {
				onLoginVect.addElement(strOnLogin[x]);
			}
		}
		
		// Load the alias info.
		aliasTable = new Hashtable();
		String[] keys = MusclePreferenceReader.getStrings(programPrefsMessage, "aliasKeys", null);
		String[] elements = MusclePreferenceReader.getStrings(programPrefsMessage, "aliasElements", null);
		if (keys != null && elements != null) {
			for (int x = 0; x < keys.length; x++) {
				aliasTable.put(keys[x], elements[x]);
			}
		}
		
		// Create the chat panel and restore the split position if one is available.
		chatterPanel = new ChatPanel(this, this, useMiniBrowser);
		int divider = MusclePreferenceReader.getInt(programPrefsMessage, "chatDivider", -1);
		if (divider != -1)
			chatterPanel.setDividerLocation(divider);
		
		// User Table Sorting and column widths.
		saveUserSort = MusclePreferenceReader.getBoolean(programPrefsMessage, "userSort", false);
		if (saveUserSort) 
			chatterPanel.setUserListSortColumn(MusclePreferenceReader.getInt(programPrefsMessage, "sortUserTable", 0));
		chatterPanel.setUserTableColumnWidths(MusclePreferenceReader.getInts(programPrefsMessage, "userTableColumnWidths", null));
		
		// Set the font.
		if (programPrefsMessage.hasField("fontName")){
			try {
				updateChatFont(new Font(programPrefsMessage.getString("fontName"),
										programPrefsMessage.getInt("fontStyle"),
										programPrefsMessage.getInt("fontSize")));
			} catch (Exception e){
			}
		}
		
		// Create the sound listener.
		soundPackChange(MusclePreferenceReader.getString(programPrefsMessage, "soundPack", "Default"));
		
		userHashTable = new Hashtable();
		
		muscleNetIO = null;
		menuBar = null;
		appletMode = false;
		this.add(localUserInfo, BorderLayout.NORTH);
		
		this.add(chatterPanel, BorderLayout.CENTER);
		
		SwingUtilities.updateComponentTreeUI(this);
		
		fireLogNewMessage(new ChatMessage("","Welcome to JavaShare 2" +
											"\nType /help for a list of " +
											"commands.", false,
									ChatMessage.LOG_INFORMATION_MESSAGE));
	}
	
	/**
	 *	Default Constructor for Application
	 *  Creates a new AppPanel from a message containing the default settings.
	 */
	public AppPanel(){
		this(BeShareDefaultSettings.createDefaultSettings());
	}
	
	/**
	 *	Constructor for use with the Applet
	 *  Creates a new AppPanel from a message containing the default settings.
	 *  It then re-creates any objects that require changes to run in an
	 *  Applet context. Also adds an AppletSoundThreadManager.
	 */
	public AppPanel(String defaultName, String defaultStatus,
						String defaultServer, JApplet sndSource){
		this(BeShareDefaultSettings.createDefaultAppletSettings(defaultName,
					defaultStatus, defaultServer));
		// Remove the stuff we don't want.
		
		// LoacalUserPanel needs to be applet mode
		this.remove(localUserInfo);
		localUserInfo = new LocalUserPanel(this, true);
		this.add(localUserInfo, BorderLayout.NORTH);
		
		currentServerName = defaultServer;
		
		appletMode = true;
		addSoundEventListener(new AppletSoundThreadManager(sndSource));
	}
	
	/**
		Retreives all prefrences, saves them in a <code>Message</code> and
		flattens it into a file in the current Users home directory.
	*/
	public void savePrefs(){
		String prefsFile = System.getProperty("user.home") 
							+ System.getProperty("file.separator")
							+ ".JavaShare2Prefs.dat";
		FileOutputStream fileStream = null;
		DataOutputStream prefsOutStream = null;
		try{
			fileStream = new FileOutputStream(prefsFile, false);
			prefsOutStream = new DataOutputStream(fileStream);
		} catch (FileNotFoundException fnfe){
			// Could not open file! There's really nothing I can do about this.
		} catch (SecurityException se){
			// SECURITY EXCEPTION! Again, nothing I can do.
		}
		// If all objects were created properly, this won't be null, so save!
		if(prefsOutStream != null){
			// Add preferences from the user information.
			programPrefsMessage.setStrings("servers", localUserInfo.getServerList());
			if(localUserInfo.getCurrentServer() == -1){
				programPrefsMessage.setInt("curServer", 0);
			} else {
				programPrefsMessage.setInt("curServer", localUserInfo.getCurrentServer());
			}
			programPrefsMessage.setStrings("nicks", localUserInfo.getNicksList());
			if(localUserInfo.getCurrentNick() == -1){
				programPrefsMessage.setInt("curNick", 0);
			} else {
				programPrefsMessage.setInt("curNick", localUserInfo.getCurrentNick());
			}
			programPrefsMessage.setStrings("status", localUserInfo.getStatusList());
			if(localUserInfo.getCurrentStatus() == -1){
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
			Rectangle frameBounds = ((MainFrame)this.getRootPane().getParent()).getBounds();
			Rect fBounds = new Rect(frameBounds.x, frameBounds.y, 
									frameBounds.width,
									frameBounds.height);
			programPrefsMessage.setRect("mainWindowRect", fBounds);
			programPrefsMessage.setInt("chatDivider", chatterPanel.getDividerLocation());
			
			// User Table Sorting
			programPrefsMessage.setInt("sortUserTable", chatterPanel.getUserListSortColumn());
			programPrefsMessage.setBoolean("userSort", saveUserSort);
			
			// User table column widths.
			programPrefsMessage.setInts("userTableColumnWidths", chatterPanel.getUserTableColumnWidths());
			
			// Transfer Panel settings
			if (transPan != null) {
				programPrefsMessage.setInt("transferSplit", transPan.getDividerLocation());
			}
			try{
				programPrefsMessage.flatten(prefsOutStream);
			} catch (IOException ioe){
				// There was an error writing the prefs to the stream (file)
				// Go Figure, nothing I can do. I believe the phrase for this
				// is 'aah, screw it.'
			}
		}
	}
	
	/**
	 * Returns the User data for the specified user.
	 */
	public BeShareUser getUserData(String sessionID){
		return (BeShareUser)userHashTable.get(sessionID);
	}
	
	/**
		Registers an new ChatPoster to receive message data.
	*/
	public void addChatPoster(ChatPoster bscp){
		chatMessageListenerVect.addElement(bscp);
		try {
			bscp.useMiniBrowser(programPrefsMessage.getBoolean("miniBrowser"));
		} catch (Exception e){
		}
	}
	
	/**
		Removes a registered ChatPoster.
	*/
	public void removeChatPoster(ChatPoster killPoster){
		chatMessageListenerVect.removeElement(killPoster);
	}
	
	/**
		Checks to see if a sessionID has a registered ChatPoster.
	*/
	protected String isPosterRegisteredForSession(String sessionID){
		for(int x = 0; x < chatMessageListenerVect.size(); x++){
			ChatPoster tempPoster =
				((ChatPoster)chatMessageListenerVect.elementAt(x));
			
			if(tempPoster.respondsToSession(sessionID)){
				return sessionID;
			}
		}
		return "";
	}
	
	/**
		Sends a <code>ChatMessage</code> to the registered ChatPosters
	*/
	protected void fireLogNewMessage(ChatMessage cMessage){
		try {
			switch(cMessage.getType()){
				case ChatMessage.LOG_REMOTE_USER_CHAT_MESSAGE : {
					if(! programPrefsMessage.getBoolean("dispPriv") && cMessage.isPrivate()){
						return;
					} else if (cMessage.isPrivate()){
						break;
					} else if(! programPrefsMessage.getBoolean("dispChat")){
						return;
					}
				}
				break;
				case ChatMessage.LOG_INFORMATION_MESSAGE : {
					if(! programPrefsMessage.getBoolean("dispInfo")){
						return;
					}
				}
				break;
				case ChatMessage.LOG_USER_EVENT_MESSAGE : {
					if(! programPrefsMessage.getBoolean("dispUser")){
						return;
					}
				}
				break;
				case ChatMessage.LOG_UPLOAD_EVENT_MESSAGE : {
					if(! programPrefsMessage.getBoolean("dispUpload")){
						return;
					}
				}
				break;
				case ChatMessage.LOG_WARNING_MESSAGE : {
					if(! programPrefsMessage.getBoolean("dispWarn")){
						return;
					}
				}
				break;
				case ChatMessage.LOG_ERROR_MESSAGE : {
					if(! programPrefsMessage.getBoolean("dispError")){
						return;
					}
				}
				break;
			}
			for(int x = 0; x < chatMessageListenerVect.size(); x++){
				if(((ChatPoster)chatMessageListenerVect.elementAt(x))
						.respondsToSession(cMessage.getTargetID()))
				{
					if(programPrefsMessage.getBoolean("dispTime") &&
						(cMessage.getType() != ChatMessage.PRIVATE_NO_LOG))
					{
					// Send a timestamp message
						ChatMessage timeStamp = new ChatMessage(cMessage);
						timeStamp.setType(ChatMessage.LOG_TIMESTAMP_MESSAGE);
						try {
							// FIX THIS BY SUB-CLASSING GregorianCalendar
							calendar.setTime(new Date(System.currentTimeMillis()));
						} catch (IllegalAccessError iae){
							programPrefsMessage.setBoolean("dispTime", false);
						}
						// Decide if we need to pre-pend the 0 to the minutes.
						if (calendar.get(Calendar.MINUTE) < 10){
							timeStamp.setMessage("[" + (calendar.get(Calendar.MONTH)+1) +
											  "/"+ calendar.get(Calendar.DAY_OF_MONTH) +
											  " " + calendar.get(Calendar.HOUR_OF_DAY) +
											  ":0" + calendar.get(Calendar.MINUTE) + "] ");
						} else {
							timeStamp.setMessage("[" + (calendar.get(Calendar.MONTH)+1) +
											  "/"+ calendar.get(Calendar.DAY_OF_MONTH) +
											  " " + calendar.get(Calendar.HOUR_OF_DAY) +
											  ":" + calendar.get(Calendar.MINUTE) + "] ");
						}
					  ((ChatPoster)chatMessageListenerVect.elementAt(x)).addMessage(timeStamp);					
					}
					((ChatPoster)chatMessageListenerVect.elementAt(x)).addMessage(cMessage);
				}
			}
		} catch (Exception e){
		}
	}
	
	/**
	 * Updates the Font for text entry and display with all Messageing panels via their posters.
	 */
	public void updateChatFont(Font f){
		try {
			programPrefsMessage.setString("fontName", f.getName());
			programPrefsMessage.setInt("fontSize", f.getSize());
			programPrefsMessage.setInt("fontStyle", f.getStyle());
		} catch (Exception e){
		}
		for(int x = 0; x < chatMessageListenerVect.size(); x++){
			((ChatPoster)chatMessageListenerVect.elementAt(x)).setChatFont(f);
		}
	}
	
	/**
		Adds a SoundEventListener to detect sound events fired from here.
	*/
	public void addSoundEventListener(SoundEventListener bssel){
		soundTriggerListenerVect.addElement(bssel);
	}
	
	/**
		Removes a BeShareSoundListener that's already been added.
	*/
	public void removeSoundEventListener(SoundEventListener bssel){
		soundTriggerListenerVect.removeElement(bssel);
	}
	
	/**
		Sends a <code>SoundEvent</code> to the registered listeners
	*/
	protected void fireSoundEvent(SoundEvent bsse){
		for (int x = 0; x < soundTriggerListenerVect.size(); x++){
			((SoundEventListener)soundTriggerListenerVect.elementAt(x)).
						beShareSoundEventPerformed(bsse);
		}
	}
	
	/**
		Sets the current sound pack for any registered SoundListeners
	*/
	protected void updateActiveSoundPack(String sPack){
		if (sPack.equals("System Beep")){
			while (!soundTriggerListenerVect.isEmpty()){
				soundTriggerListenerVect.removeElementAt(0);
			}
			soundTriggerListenerVect.addElement(new SystemBeep());
		}
		for (int x = 0; x < soundTriggerListenerVect.size(); x++){
			((SoundEventListener)soundTriggerListenerVect.elementAt(x)).
						setSoundPack(sPack);
		}
	}
	
	/**
		Sets the connection to send data out on.
		@param mi the Tranto to send data out on
	*/
	public void setNetworkInterface(Tranto mi){
		muscleNetIO = mi;
		
		// If we aren't in applet mode, add the file-sharing stuff with this Tranto interface.
		if (!appletMode){
			transPan = new TransferPanel(muscleNetIO, programPrefsMessage, this);
			prefsFrame.addFileTransferPrefs(programPrefsMessage, transPan);
			
			queryChatSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			queryChatSplit.add(transPan);
			queryChatSplit.add(chatterPanel);
			
			this.remove(chatterPanel);
			this.add(queryChatSplit, BorderLayout.CENTER);
		}
		muscleNetIO.setServerName(getServerName());
		muscleNetIO.setServerPort(2960);
		try {
			if(programPrefsMessage.getBoolean("autoLogin")){
				muscleNetIO.connect();
			}
		} catch (Exception e){
		}
	}
	
	/**
		Returns the ChatPanel object.
		@return the ChatPanel child for this frame
	*/
	public ChatPanel getChatPanel(){
		return chatterPanel;
	}
	
	/**
		Sets the applet flag.
	*/
	public void setAppletMode(boolean b){
		appletMode = b;
	}
	
	/**
		Registers a JMenuBar to listen for ActionEvents from.
	*/
	public void setListenToMenu(Object mb){
		menuBar = mb;
	}
	
	/**
		Gets the menu bar which is currently registered for receiving events.
	*/
	public Object getListenToMenu(){
		return menuBar;
	}
	
	/**
		Removes the JMenuBar for Event Listening.
	*/
	public void removeListenToMenu(){
		menuBar = null;
	}
	
	/**
		calls requestChatLineFocus on the ChatPanel.
	*/
	protected void requestChatLineFocus(){
		chatterPanel.requestChatLineFocus();
	}
	
	/**
		Searches the User table for a SessionID which matchs the supplied
		Name.
		@param startsWithName A String that starts with the name of a user.
		@return The SessionID of that user, if one, or "" if no match is found.
	*/
	public String findSessionByName(String startsWithName){
		// first let's try exact matches there are times when
		// startsWithname IS the name.
		Enumeration hashKeys = userHashTable.keys();
		String targetSession = "";
		while(hashKeys.hasMoreElements() && (targetSession.equals(""))){
			BeShareUser tempUser = (BeShareUser)userHashTable.get(hashKeys.nextElement());
			
			String compareName = tempUser.getName().toUpperCase();
			
			// Parse the name for URL and label... Damn things...
			if (compareName.endsWith("]")){
				try {
					compareName = compareName.substring(compareName.indexOf("[") + 1, compareName.indexOf("]"));
				} catch (Exception e){
				}
			}
			
			if(startsWithName.toUpperCase().equals(compareName)){
				targetSession = tempUser.getConnectID();
			}
		}
		// Now let's parse it to the first space and try that.
		hashKeys = userHashTable.keys();
		try{
			String nameNoSpace = startsWithName.substring(0, startsWithName.indexOf(" "));
			while(hashKeys.hasMoreElements() && (targetSession.equals(""))){
				BeShareUser tempUser = (BeShareUser)userHashTable.get(hashKeys.nextElement());
				if (tempUser.getName().toUpperCase().equals(nameNoSpace.toUpperCase())){
					targetSession = tempUser.getConnectID();
				}
			}
		} catch (IndexOutOfBoundsException ioobe){
		}
		// If we don't find a match before this, we look for the closest match.
		hashKeys = userHashTable.keys();
		while(hashKeys.hasMoreElements() && (targetSession.equals(""))){
			BeShareUser tempUser = (BeShareUser)userHashTable.get(hashKeys.nextElement());
			if(startsWithName.toUpperCase().startsWith(tempUser.getName().toUpperCase())){
				targetSession = tempUser.getConnectID();
			}
		}
		return targetSession;
	}
	
	/**
		Searches the User Table for a name that matches the supplied SessionID.
		@param sessionID The SessionID to resolve a name for
		@return The Name of user matching sessionID, or an empty "" String.
	*/
	public String findNameBySession(String sessionID){
		if (userHashTable.containsKey(sessionID)){
			return ((BeShareUser)userHashTable.get(sessionID)).getName();
		} else {
			if(sessionID.equals("")){
				return getLocalUserName();
			} else {
				return "";
			}
		}
	}
	
	/**
		Searches through the user Table for names starting with the supplied
		string. If it finds one, it returns the rest of the name, not the entire
		name.
		@param partialName The Beginning of a name.
		@return The Missing Remainder of the name or an empty "" String
	*/
	public String findCompletedName(String partialName){
		if (partialName.equals(""))
			return "";
		
		Enumeration hashKeys = userHashTable.keys();
		String completedName = "";
		while(hashKeys.hasMoreElements() && (completedName.equals(""))){
			BeShareUser tempUser = (BeShareUser)userHashTable.get(hashKeys.nextElement());
			
			String compareName = tempUser.getName().toUpperCase();
			// Parse the name for URL and label... Damn things...
			if (compareName.endsWith("]")){
				try {
					compareName = compareName.substring(compareName.indexOf("[") + 1, compareName.indexOf("]"));
				} catch (Exception e){
				}
			}
			
			if(compareName.startsWith(partialName.toUpperCase())){
				if (tempUser.getName().endsWith("]"))
					completedName = tempUser.getName().substring(tempUser.getName().indexOf("[") + 1, tempUser.getName().indexOf("]"));
				else
					completedName = tempUser.getName();
			}
		}
		return completedName;
	}
	
	/**
		Sets the local user's name, and registers the new value with
		the network interface and userPanel.
		@param user The new User Name
	*/
	public void setLocalUserName(String user){
		localUserName = user;
		muscleNetIO.setLocalUserName(localUserName, startingPortNumber);
		localUserInfo.setUserName(localUserName);
		fireLogNewMessage(new ChatMessage("", "Your name has been"
			+ " changed to " + localUserName, false, 
			ChatMessage.LOG_USER_EVENT_MESSAGE, true, ""));
	}
	
	/**
		Sets the local user Status and registeres the new value with the
		network intervace and userPanel.
		@param status The new Status for the local user.
	*/
	public void setLocalUserStatus(String status){
		localUserStatus = status;
		localUserInfo.setUserStatus(localUserStatus);
		muscleNetIO.setLocalUserStatus(localUserStatus);
		fireLogNewMessage(new ChatMessage("", "Your Status has been"
			+ " changed to: " + localUserStatus, false, 
			ChatMessage.LOG_USER_EVENT_MESSAGE, true, ""));
	}
	
	/**
		Sest the Server Name and registeres the change with the network
		interface and userPanel.
		@param server The name of the server to open connections to.
	*/
	public void setServerName(String server){
		currentServerName = server;
		muscleNetIO.setServerName(currentServerName);
		localUserInfo.setServerName(currentServerName);
		if (muscleNetIO.isConnected()){
			muscleNetIO.connect();
		}
		localUserInfo.setUserStatus(localUserStatus);
		fireLogNewMessage(new ChatMessage("", "Active server"
			+ " changed to: " + currentServerName, false,
			ChatMessage.LOG_USER_EVENT_MESSAGE, true, ""));
		updateFrameTitle();
	}
	
	/**
	 * Takes a vector of Server Names and adds them to the server list.
	 * @param servers A vector of server names as Strings
	 */
	public void addServers(Vector servers) {
		for (int x = 0; x < servers.size(); x++) {
			if (localUserInfo.addServerName((String)servers.elementAt(x))) {
				fireLogNewMessage(new ChatMessage("", "Added server: " + (String)servers.elementAt(x)
					, false, ChatMessage.LOG_USER_EVENT_MESSAGE, true, ""));
			}
		}
	}
	
	/**
	 * Takes a vector of server names and removes them from the server list.
	 * @param servers a vector of server names as Strings
	 */
	public void removeServers(Vector servers) {
		for (int x = 0; x < servers.size(); x++) {
			if (localUserInfo.removeServerName((String)servers.elementAt(x))) {
				fireLogNewMessage(new ChatMessage("", "Removed server: " + (String)servers.elementAt(x)
					, false, ChatMessage.LOG_USER_EVENT_MESSAGE, true, ""));
			}
		}
	}
	
	/**
	 * Sets the frame that this appPanel is a child of to contain the current server.
	 */
	public void updateFrameTitle(){
		try{
			((MainFrame)this.getRootPane().getParent()).setTitle(
					AppPanel.pubVersion + " @" +	currentServerName);
		} catch (Exception exc){
		}
	}
	
	/**
		Gets the local user Name
		@return The Local Users name.
	*/
	public String getLocalUserName(){
		return localUserName;
	}
	
	/**
		@return the Local users status.
	*/
	public String getLocalUserStatus(){
		return localUserStatus;
	}
	
	/**
		@return The name of the server all active connections will be 
		opened to.
	*/
	public String getServerName(){
		return currentServerName;
	}
	
	/**
		Returns a <code>String</code> array from given <code>Vector</code>
		@param v The Vector to return strings from.
		@return A String array of the contents of the vectore. All conversion is
		done by invoking the <code>toString()</code> method.
	*/
	public String[] getStringArrFromVect(Vector v){
		String[] stringList = new String[v.size()];
		for(int x = 0; x < v.size(); x++){
			stringList[x] = v.elementAt(x).toString();
		}
		return stringList;
	}
	
	/**
		Returns a String array of the keys of a Hashtable.
		@param h The Hashtable to get the keys from.
		@return A String array of the <code>h</code>'s keys.
	*/
	public String[] getKeysAsStringArr(Hashtable h){
		String[] stringList = new String[h.size()];
		Enumeration enu = h.keys();
		for(int x = 0; x < h.size(); x ++){
			stringList[x] = enu.nextElement().toString();
		}
		return stringList;
	}
	
	/**
		Returns a String array of the elements of a Hashtable.
		@param h The Hashtable to get the elements from.
		@return A String array of the <code>h</code>'s elements.
	*/
	public String[] getElementsAsStringArr(Hashtable h){
		String[] stringList = new String[h.size()];
		Enumeration enu = h.elements();
		for(int x = 0; x < h.size(); x ++){
			stringList[x] = enu.nextElement().toString();
		}
		return stringList;
	}
	
	/**
		Implementation of ActionListener
	*/
	public void actionPerformed(ActionEvent e){
		if (e.getActionCommand() == "menuConnect"){
			muscleNetIO.connect();
		} else if (e.getActionCommand() == "menuDisconnect"){
			muscleNetIO.disconnect();
			chatterPanel.clearUserTable();
			if (transPan != null){
				transPan.clearQueryResults();
			}
			fireLogNewMessage(new ChatMessage("", "You are " + 
				"disconnected from the MUSCLE server.", false,
				ChatMessage.LOG_ERROR_MESSAGE, true, ""));
		} else if (e.getActionCommand() == "menuQuit"){
			quitRequested();
		} else if (e.getActionCommand() == "spawn"){
			chatMessage(new ChatMessage("", "/SPAWN"
					, false
					, ChatMessage.LOG_LOCAL_USER_CHAT_MESSAGE
					, true
					, ""));
		} else if (e.getActionCommand() == "menuPrivate"){
			PrivateFrame privFrame = new PrivateFrame(this,
							this, " ");
			privFrame.pack();
			privFrame.show();
		} else if (e.getActionCommand() == "menuClear"){
			chatMessage(new ChatMessage("", "/CLEAR"
					, false
					, ChatMessage.LOG_LOCAL_USER_CHAT_MESSAGE
					, true
					, ""));
		} else if (e.getActionCommand() == "menuAbout"){
			aboutJavaShare.show();
		} else if (e.getActionCommand() == "cut"){
			chatterPanel.cut();
		} else if (e.getActionCommand() == "copy"){
			chatterPanel.copy();
		} else if (e.getActionCommand() == "paste"){
			chatterPanel.paste();
		} else if (e.getActionCommand() == "prefs"){
			prefsFrame.show();
		} else if (e.getSource() == autoAwayTimer){
			// Set the status to away and stop the timer. No need to run it.
			try {
				localUserInfo.setAwayStatus(true);
				autoAwayTimer.stop();
			} catch (NullPointerException npe){
				autoAwayTimer.stop();
				// This happens if the auto-away is disabled.
				// It's not any type of a problem. The timer just isn't created
				// unless we need it.
			}
		}
	}
	
	// -------------------
	// Preference Listener
	// -------------------
	
	/**
	 * AutoAway preference has Changed.
	 * @param time The new timer delay in seconds.
	 */
	public void autoAwayTimerChange(int time, int selectedIndex){
		if (autoAwayTimer != null){
			if (time > 0){
				autoAwayTimer.setDelay(time);
			} else {
				autoAwayTimer.stop();
			}
		} else {
			autoAwayTimer = new Timer(time, this);
		}
		try{
			programPrefsMessage.setInt("awayTime", time);
			programPrefsMessage.setInt("awayTimeIndex", selectedIndex);
		} catch (Exception e){
		}
	}
	
	/**
	 * Auto-Update server on startup has changed.
	 * @param autoUpdate <code>True</code> if we should update, <code>false</code> if we shouldn't.
	 */
	public void autoUpdateServerChange(boolean autoUpdate){
		try {
			programPrefsMessage.setBoolean("autoUpdServers", autoUpdate);
		} catch (Exception e){
		}
	}
	
	/**
	 * Firewall setting changed.
	 * @param firewalled Boolean representing the on/off status of firewalling.
	 */
	public void firewallSettingChange(boolean firewalled){
		try {
			programPrefsMessage.setBoolean("firewalled", firewalled);
			muscleNetIO.setFirewalled(firewalled);
			transPan.resetShareList();
		} catch (Exception e){
		}
	}
	
	/**
	 * Automatic Login on startup setting changed.
	 * @param login Boolean representing the on/off status of auto-login.
	 */
	public void loginOnStartupChange(boolean login){
		try {
			programPrefsMessage.setBoolean("autoLogin", login);
		} catch (Exception e){
		}
	}
	
	/**
	 * Mini-browser setting has changed.
	 * @param use Boolean value for weather or not we should use the mini-browser.
	 */
	public void useMiniBrowserChange(boolean use){
		try {
			programPrefsMessage.setBoolean("miniBrowser", use);
			for(int x = 0; x < chatMessageListenerVect.size(); x++){
				((ChatPoster)chatMessageListenerVect.elementAt(x)).useMiniBrowser(use);
			}
		} catch (Exception e){
		}
	}
	
	/**
	 * Save User Sorting
	 * @param sort true to save the setting, false to not save it.
	 */
	public void userSortChange(boolean sort) {
		saveUserSort = sort;
	}
	
	/**
	 * The Bandwidth setting has changed.
	 * @param lablel The 'name' of the connection type. T1, Cable, etc.
	 * @param speed The Speed in bps of the connection.
	 */
	public void bandwidthChange(String label, int speed, int index){
		try {
			programPrefsMessage.setString("uploadLabel", label);
			programPrefsMessage.setInt("uploadValue", speed);
			programPrefsMessage.setInt("uploadBw", index);
			if (muscleNetIO != null){
				muscleNetIO.setUploadBandwidth(
						programPrefsMessage.getString("uploadLabel"),
						programPrefsMessage.getInt("uploadValue"));
			}
		} catch (Exception e){
		}
	}
	
	/**
	 * Receives a SOCKS setting change. if <code>port</code> = -1, disable the SOCKS.
	 *
	 * @param server The name (or ip address) of the SOCKS server.
	 * @param port The port the SOCKS server listens on. Typically 1080.
	 */
	public void socksChange(String server, int port){
		try {
			programPrefsMessage.setString("socksServer", server);
			programPrefsMessage.setInt("socksPort", port);
		} catch (Exception e){
		}
	}
	
	/**
	 * The Time-stamp display option has been changed.
	 */
	public void timeStampDisplayChange(boolean show){
		try {
			programPrefsMessage.setBoolean("dispTime", show);
		} catch (Exception e){
		}
	}
	
	/**
	 * The display User Event option has changed.
	 */
	public void userEventDisplayChange(boolean show){
		try {
			programPrefsMessage.setBoolean("dispUser", show);
		} catch (Exception e){
		}
	}
	
	/**
	 * The Display Upload Events option has changed.
	 */
	public void uploadDisplayChange(boolean show){
		try {
			programPrefsMessage.setBoolean("dispUpload", show);
		} catch (Exception e){
		}
	}
	
	/**
	 * The Disply Chat option has changed.
	 */
	public void chatDisplayChange(boolean show){
		try {
			programPrefsMessage.setBoolean("dispChat", show);
		} catch (Exception e){
		}
	}
	
	/**
	 * The display Private messages option has changed.
	 */
	public void privateDisplayChange(boolean show){
		try {
			programPrefsMessage.setBoolean("dispPriv", show);
		} catch (Exception e){
		}
	}
	
	/**
	 * The Display Information messages option has changed.
	 */
	public void infoDisplayChange(boolean show){
		try {
			programPrefsMessage.setBoolean("dispInfo", show);
		} catch (Exception e){
		}
	}
	
	/**
	 * the Display Warning messages option has changed.
	 */
	public void warningDisplayChange(boolean show){
		try {
			programPrefsMessage.setBoolean("dispWarn", show);
		} catch (Exception e){
		}
	}
	
	/**
	 * the Display Error Messages option has changed.
	 */
	public void errorDisplayChange(boolean show){
		try {
			programPrefsMessage.setBoolean("dispError", show);
		} catch (Exception e){
		}
	}
	
	/**
	 * the Look and Feel setting changed.
	 */
	public void lafChange(String plafClassName){
		try {
			UIManager.setLookAndFeel(plafClassName);
			for(int x = 0; x < chatMessageListenerVect.size(); x++){
				((ChatPoster)chatMessageListenerVect.elementAt(x)).updateLafSetting();
			}
			SwingUtilities.updateComponentTreeUI(prefsFrame);
			SwingUtilities.updateComponentTreeUI(aboutJavaShare);
			prefsFrame.pack();
			aboutJavaShare.pack();
			programPrefsMessage.setString("LaF", plafClassName);
		} catch (Exception e){
			System.out.println(e.toString());
		}
	}
	
	/**
	 * Sound pack changed.
	 */
	public void soundPackChange(String soundPackName){
		try {
			if (soundTriggerListenerVect.size() == 0){
				if (soundPackName.equals("System Beep")){
					SystemBeep soundPlayer = new SystemBeep();
					addSoundEventListener(soundPlayer);
				} else {
					ApplicationSoundThreadManager soundPlayer = new ApplicationSoundThreadManager(soundPackName);
					addSoundEventListener(soundPlayer);
				}
			}
			programPrefsMessage.setString("soundPack", soundPackName);
			updateActiveSoundPack(soundPackName);
		} catch (Exception e){
		}
	}
	
	/**
	 * Sound on User Name event setting changed.
	 */
	public void soundOnUserNameChange(boolean signal){
		try {
			programPrefsMessage.setBoolean("sndUName", signal);
		} catch (Exception e){
		}
	}
	
	/**
	 * sound on Private Window popup setting changed.
	 */
	public void soundOnPrivateWindowChange(boolean signal){
		try {
			programPrefsMessage.setBoolean("prvWSnd", signal);
		} catch (Exception e){
		}
	}
	
	/**
	 * sound on Priave Message setting changed.
	 */
	public void soundOnPrivateMessageChange(boolean signal){
		try {
			programPrefsMessage.setBoolean("prvMSnd", signal);
		} catch (Exception e){
		}
	}
	
	/**
	 * sound on Watch pattern match setting changed.
	 */
	public void soundOnWatchPatternChange(boolean signal){
		try {
			programPrefsMessage.setBoolean("sndWPat", signal);
		} catch (Exception e){
		}
	}
	
	/**
		This is called by either the window closing method, or by the Quit menu.
		This is our clean-up method. We'll eventually confirm the quit if they
		have file transfers in progress, and abort if they change thier mind.
		For now, we're going to save our local Preferences, and then quit.
	*/
	public void quitRequested(){
		savePrefs();
		System.exit(0);
	}
	
	/**
		Implementation of ChatMessageListener
	*/
	public void chatMessage(ChatMessage newMessage){
		// If the timer is set to be disabled, leave it. If not, restart it.
		if (autoAwayTimer != null){
			try {
				if (programPrefsMessage.getInt("awayTime") == -1){
					autoAwayTimer.stop();
				} else {
					autoAwayTimer.restart();
				}
			} catch (Exception e){
			}
		}
		if (localUserInfo.isAway()){
			localUserInfo.setAwayStatus(false);
		}
		if (newMessage.getType() == ChatMessage.PRIVATE_LOCAL_LOG_ONLY){
			// Intercept any local log only's comming from private chat panels.
			// Transform them into Log Local User chats, and send them
			// to the panels, but NOT the MuscleInterface
			newMessage.setType(ChatMessage.LOG_LOCAL_USER_CHAT_MESSAGE);
			newMessage.setSession("(" + muscleNetIO.getLocalSessionID() + ") " + getLocalUserName());
			fireLogNewMessage(newMessage);
		} else if (newMessage.getMessage().startsWith("/")){
			// Get command message here.
			String commandLine = newMessage.getMessage();
			commandLine = commandLine.substring(0);
			// Make sure there's something after the command
			String command = "";
			if(commandLine.indexOf(" ") != -1){
				command = commandLine.substring(0, commandLine.indexOf(" ")).toUpperCase();
				commandLine = commandLine.substring(command.length() + 1).trim();
			} else {
				command = commandLine.toUpperCase().trim();
			}
			// We have a Messaging command.
			if(command.equals("/MSG")){
				String targetSession = findSessionByName(commandLine);
				if(! targetSession.equals("")){
					newMessage.setMessage(commandLine.substring(findNameBySession(targetSession).length()).trim());
					muscleNetIO.sendTextMessage(newMessage.getMessage(), targetSession.trim());
					if(newMessage.getType() == ChatMessage.LOG_LOCAL_USER_CHAT_MESSAGE){
						newMessage.setSession("(" + muscleNetIO.getLocalSessionID() + ") " + getLocalUserName() + " -> (" + findNameBySession(targetSession.trim()) + ")");
						newMessage.setLocalMessage(true);
					}
					newMessage.setPrivate(true);
					fireLogNewMessage(newMessage);
				} else {
					// Eliminate anything after the ID - parsing by spaces.
					if(commandLine.indexOf(" ") != -1){
						 targetSession = commandLine.substring(0,
								commandLine.indexOf(" "));
					} else {
						targetSession = commandLine;
					}
					// See if the session exists!
					if(! findNameBySession(targetSession).equals("")){
						newMessage.setMessage(commandLine.substring(targetSession.length()).trim());
						muscleNetIO.sendTextMessage(newMessage.getMessage(), targetSession.trim());
						if(newMessage.getType() == ChatMessage.LOG_LOCAL_USER_CHAT_MESSAGE){
							newMessage.setSession("(" + muscleNetIO.getLocalSessionID() + ") " + getLocalUserName() + " -> (" + findNameBySession(targetSession.trim()) + ")");
							newMessage.setLocalMessage(true);
						}
						newMessage.setPrivate(true);
						fireLogNewMessage(newMessage);
					} else {
						// Could not find a name or session that matches.
						fireLogNewMessage(new ChatMessage(newMessage.getSession(), 
							"Couldn't find specified user.", false,
							ChatMessage.LOG_ERROR_MESSAGE, true, newMessage.getTargetID()));
					}
				}
			} else if (command.equals("/AWAY")){
				localUserInfo.setAwayStatus(!localUserInfo.isAway());
			} else if (command.equals("/SPAWN") && appletMode){
				// Create the new frame.
				JFrame newParent = new JFrame("JavaShare2 Applet Edition - "
												+ muscleNetIO.getServerName());
				// Find the applet - this navigates the class heirarchy.
				org.beShare.Applet appletParent = (org.beShare.Applet)this.getRootPane().getParent();
				appletParent.remove(this);
				newParent.getContentPane().add(this);
				
				newParent.setJMenuBar(appletParent.getJMenuBar());
				appletParent.setJMenuBar(null);
				
				newParent.pack();
				newParent.show();
				
				// Create the new Stuff..
				JLabel spawnedLabel = new JLabel("JavaShare 2 is now in" +
												" Spawned Applet Mode. Closing" +
												" this window will close" +
												" JavaShare 2.", JLabel.CENTER);
				JButton unSpawn = new JButton("Un-Spawn");
				unSpawn.addActionListener(appletParent);
				appletParent.getContentPane().setLayout(new BorderLayout());
				JPanel btnPanel = new JPanel();
				btnPanel.add(unSpawn);
				JPanel lblPanel = new JPanel();
				lblPanel.add(spawnedLabel);
				appletParent.getContentPane().add(lblPanel, BorderLayout.NORTH);
				appletParent.getContentPane().add(btnPanel, BorderLayout.CENTER);
				// Re-do the applets layout.
				appletParent.validate();
				// Don't forget to set appletMode = false so they can't do this
				// again.
				appletMode = false;
			} else if (command.equals("/AWAYMSG")){
				if(! commandLine.toUpperCase().equals("/AWAYMSG")){
					localUserInfo.setAwayStatus(commandLine);
					fireLogNewMessage(new ChatMessage(""
						,"Auto-away message set to " + commandLine
						,false,	ChatMessage.LOG_INFORMATION_MESSAGE));
				}
			} else if (command.equals("/ALIAS")){
				if(! commandLine.toUpperCase().equals("/ALIAS")){
					StringTokenizer tokenizer = new StringTokenizer(commandLine,"=, ");
					while(tokenizer.hasMoreTokens() && ((tokenizer.countTokens() % 2) == 0)){
						String key = tokenizer.nextToken();
						String value = tokenizer.nextToken();
						aliasTable.put(key, value);
						fireLogNewMessage(new ChatMessage(""
							,"Set alias " + key + " = " + value
							,false,	ChatMessage.LOG_INFORMATION_MESSAGE));
					}
				}
			} else if (command.equals("/UNALIAS")){
				if (! commandLine.toUpperCase().equals("/UNALIAS")){
					if(aliasTable.containsKey(commandLine)){
						aliasTable.remove(commandLine);
						fireLogNewMessage(new ChatMessage(""
							,"Removed alias " + commandLine
							,false,	ChatMessage.LOG_INFORMATION_MESSAGE));
					}
				}
			} else if (command.equals("/PRIV")){
				// Lookup by name first.
				String targetSession = findSessionByName(commandLine);
				if(! targetSession.equals("")){
					// construct new Private Frame
					PrivateFrame privFrame = new PrivateFrame(this,
							this, targetSession);
					privFrame.pack();
					privFrame.show();
				} else {
					// The name lookup failed, we'll try to lookup by session.
					// Eliminate anything after the ID - parsing by spaces.
					if(commandLine.indexOf(" ") != -1){
						commandLine = commandLine.substring(0,
								commandLine.indexOf(" "));
					}
					// See if the session exists
					if(! findNameBySession(commandLine).equals("")){
						// We just verified that a user with that ID exists.
						// Incase the name isn't unique, we're going to stick
						// with using the session which is stored in commandLine
						PrivateFrame privFrame;
						try {
							privFrame = new PrivateFrame(this,
									this, commandLine, 
									new Font(programPrefsMessage.getString("fontName"),
											programPrefsMessage.getInt("fontStyle"),
											programPrefsMessage.getInt("fontSize")));
						} catch (Exception e){
							privFrame = new PrivateFrame(this, this, commandLine);
						}
						privFrame.pack();
						privFrame.show();
					} else {
						// Could not find a name or session that matches.
						fireLogNewMessage(new ChatMessage(newMessage.getSession(), 
							"Couldn't find specified user.", false,
							ChatMessage.LOG_ERROR_MESSAGE, true, newMessage.getTargetID()));
					}
				}
			} else if (command.equals("/PING")){
				String targetSession = findSessionByName(commandLine);
				if(! targetSession.equals("")){
					muscleNetIO.pingUser(targetSession);
				} else {
					if(commandLine.indexOf(" ") != -1){
						 targetSession = commandLine.substring(0,
								commandLine.indexOf(" "));
					} else {
						targetSession = commandLine;
					}
					// See if the session exists!
					if(! findNameBySession(targetSession).equals("")){
						muscleNetIO.pingUser(targetSession);
					} else {
						// Could not find a name or session that matches.
						fireLogNewMessage(new ChatMessage(newMessage.getSession(), 
							"Couldn't find specified user.", false,
							ChatMessage.LOG_ERROR_MESSAGE, true, newMessage.getTargetID()));
					}
				}
			} else if (command.equals("/ME")){
				muscleNetIO.sendTextMessage(newMessage.getMessage(), "*");
				fireLogNewMessage(new ChatMessage(newMessage.getSession(),
						newMessage.getMessage(), false
						, ChatMessage.LOG_LOCAL_USER_CHAT_MESSAGE, true, newMessage.getTargetID()));
			} else if (command.equals("/ACTION")){
				newMessage.setMessage("/me " + commandLine);
				muscleNetIO.sendTextMessage(newMessage.getMessage(), "*");
				fireLogNewMessage(new ChatMessage(newMessage.getSession(),
						newMessage.getMessage(), false
						, ChatMessage.LOG_LOCAL_USER_CHAT_MESSAGE, true, newMessage.getTargetID()));
			} else if (command.equals("/NICK")){
				setLocalUserName(commandLine);
			} else if (command.equals("/STATUS")){
				setLocalUserStatus(commandLine);
			} else if (command.equals("/CLEAR")){
				fireLogNewMessage(new ChatMessage(newMessage.getSession(), ""
				, false, ChatMessage.LOG_CLEAR_LOG_MESSAGES, false,
				newMessage.getTargetID()));
			} else if (command.equals("/HELP")){
				fireLogNewMessage(
						new ChatMessage("","JavaShare 2 Command Refrence\n"
						+ "       /action <action> - do something\n"
						+ "       /alias [names and value] - create an alias\n"
						+ "       /autopriv <names or session ids> - specify AutoPriv users\n"
						+ "       /away tag - Force away state\n"
						+ "       /awaymsg tag - change the auto-away tag\n"
						+ "       /clear - clear the chat log\n"
						+ "       /clearonlogin - clear startup commands\n"
						+ "       /connect [serverName] - connect to a server\n"
						+ "       /disconnect - disconnect from the server\n"
						+ "       /help - show this help text\n"
						+ "       /ignore <names or session ids> - specify users to ignore\n"
						+ "       /me <action> - synonym for /action\n"
						+ "       /msg <name or session id> <text> - send a private message\n"
						+ "       /nick <name> - change your user name\n"
						+ "       /onlogin command - add a startup command\n"
						+ "       /priv <names or session ids> - Open Private Chat Window\n"
						+ "       /ping <names or session ids> - ping other clients\n"
						+ "       /quit - quit BeShare\n"
						+ "       /serverinfo - Request server status\n"
						+ "       /status Status - set user status string\n"
						+ "       /unalias <name> - remove an alias\n"
						+ "       /watch <name or session ids> - specify users to watch\n"
						+ "       /server <address> - Sets the server address.\n"
						,false,	ChatMessage.LOG_INFORMATION_MESSAGE));
			} else if (command.equals("/CONNECT")){
				if (! commandLine.toUpperCase().equals("/CONNECT")){
					setServerName(commandLine);
				}
				muscleNetIO.connect();
			} else if (command.equals("/QUIT")){
				quitRequested();
			} else if (command.equals("/DISCONNECT")){
				muscleNetIO.disconnect();
				chatterPanel.clearUserTable();
			} else if (command.equals("/SERVER")){
				setServerName(commandLine);
			} else if (command.equals("/SERVERINFO")){
				// Sends out a serverinfo message
				muscleNetIO.getServerInfo();
			} else if (command.equals("/WATCH")){
				// If the command is the /WATCH then we'll clear the pattern
				if (commandLine.toUpperCase().equals("/WATCH")){
					while(! watchPatVect.isEmpty()){
						watchPatVect.removeElementAt(0);
					}
					fireLogNewMessage(new ChatMessage(""
						,"Watch pattern removed."
						,false, ChatMessage.LOG_INFORMATION_MESSAGE));
				} else {
				// Ok, so now we get to add to the pattern.
					try{
						if(commandLine.startsWith("*")){
							// Clear the existing stuff
							while(! watchPatVect.isEmpty()){
								watchPatVect.removeElementAt(0);
							}
							// Add the new pattern
							watchPatVect.addElement(new RE("all"
							, RE.REG_ICASE));
						} else {
							watchPatVect.addElement(new RE(commandLine, RE.REG_ICASE));
						}
						fireLogNewMessage(new ChatMessage(""
							,"Added watch pattern: " + commandLine
							,false,	ChatMessage.LOG_INFORMATION_MESSAGE));
					} catch (REException ree){
						fireLogNewMessage(new ChatMessage(newMessage.getSession(), 
							"Invalid pattern expression", false,
							ChatMessage.LOG_ERROR_MESSAGE, true, newMessage.getTargetID()));
					} catch (NullPointerException npe){
						fireLogNewMessage(new ChatMessage(newMessage.getSession(), 
							"Invalid pattern expression", false,
							ChatMessage.LOG_ERROR_MESSAGE, true, newMessage.getTargetID()));
					}
				}
			} else if (command.equals("/ONLOGIN")){
				if(! commandLine.toUpperCase().equals("/ONLOGIN")){
					onLoginVect.addElement(commandLine);
					fireLogNewMessage(new ChatMessage(""
						,"Recorded startup command: " + commandLine
						,false, ChatMessage.LOG_INFORMATION_MESSAGE));
				} else {
					fireLogNewMessage(new ChatMessage(newMessage.getSession(), 
						"No Login Command Specified", false,
						ChatMessage.LOG_ERROR_MESSAGE, true, newMessage.getTargetID()));
				}
			} else if (command.equals("/CLEARONLOGIN")){
				while(! onLoginVect.isEmpty()){
					onLoginVect.removeElementAt(0);
				}
				fireLogNewMessage(new ChatMessage(""
					,"Startup commands cleared."
					,false,	ChatMessage.LOG_INFORMATION_MESSAGE));
			} else if (command.equals("/AUTOPRIV")){
				if (commandLine.toUpperCase().equals("/AUTOPRIV")){
					while(! autoPrivVect.isEmpty()){
						autoPrivVect.removeElementAt(0);
					}
					fireLogNewMessage(new ChatMessage(""
						,"AutoPriv pattern removed."
						,false,	ChatMessage.LOG_INFORMATION_MESSAGE));
				} else {
					try{
						if(commandLine.startsWith("*")){
							// Clear out the existing patterns.
							while(! autoPrivVect.isEmpty()){
								autoPrivVect.removeElementAt(0);
							}
							// Set it in.
							autoPrivVect.addElement(new RE("all", RE.REG_ICASE));
						} else {
							autoPrivVect.addElement(new RE(commandLine, RE.REG_ICASE));
						}
						fireLogNewMessage(new ChatMessage(""
							,"AutoPriv added pattern: " + commandLine
							,false,	ChatMessage.LOG_INFORMATION_MESSAGE));
					} catch (REException ree){
						fireLogNewMessage(new ChatMessage(newMessage.getSession(), 
							"Invalid pattern expression", false,
							ChatMessage.LOG_ERROR_MESSAGE, true, newMessage.getTargetID()));
					} catch (NullPointerException npe){
						fireLogNewMessage(new ChatMessage(newMessage.getSession(), 
							"Invalid pattern expression", false,
							ChatMessage.LOG_ERROR_MESSAGE, true, newMessage.getTargetID()));
					}
				}
			} else if (command.equals("/IGNORE")){
				// If the command is the /IGNORE then we'll clear the pattern
				if (commandLine.toUpperCase().equals("/IGNORE")){
					while(! ignorePatVect.isEmpty()){
						ignorePatVect.removeElementAt(0);
					}
					fireLogNewMessage(new ChatMessage(""
						,"Ignore pattern removed."
						,false, ChatMessage.LOG_INFORMATION_MESSAGE));
				} else {
				// Ok, so now we get to add to the pattern.
					try{
						if(commandLine.startsWith("*")){
							// Clear the existing stuff
							while(! ignorePatVect.isEmpty()){
								ignorePatVect.removeElementAt(0);
							}
							// Add the new pattern
							ignorePatVect.addElement(new RE("all"
							, RE.REG_ICASE));
						} else {
							ignorePatVect.addElement(new RE(commandLine, RE.REG_ICASE));
						}
						fireLogNewMessage(new ChatMessage(""
							,"Added ignore pattern: " + commandLine
							,false,	ChatMessage.LOG_INFORMATION_MESSAGE));
					} catch (REException ree){
						fireLogNewMessage(new ChatMessage(newMessage.getSession(), 
							"Invalid pattern expression", false,
							ChatMessage.LOG_ERROR_MESSAGE, true, newMessage.getTargetID()));
					} catch (NullPointerException npe){
						fireLogNewMessage(new ChatMessage(newMessage.getSession(), 
							"Invalid pattern expression", false,
							ChatMessage.LOG_ERROR_MESSAGE, true, newMessage.getTargetID()));
					}
				}
			} else {
				fireLogNewMessage(new ChatMessage("", "Command not " + 
					"recognized.", false,
					ChatMessage.LOG_ERROR_MESSAGE, true, ""));
			}
		} else if(newMessage.getMessage().length() > 0){
			// Check for Alias matches!
			String possibleAlias;
			try{
				possibleAlias = newMessage.getMessage().trim().substring(0,
					newMessage.getMessage().trim().indexOf(" "));
			} catch (StringIndexOutOfBoundsException sioobe){
				possibleAlias = newMessage.getMessage().trim();
			}
			if (aliasTable.containsKey(possibleAlias)){
				String replacement = (String)aliasTable.get(possibleAlias);
				newMessage.setMessage(replacement + " "
					+ newMessage.getMessage().trim().substring(possibleAlias.length()));
			}
			
			muscleNetIO.sendTextMessage(newMessage.getMessage().trim(), "*");
			// put our session ID and name into the session of any message I type.
			if(newMessage.getType() == ChatMessage.LOG_LOCAL_USER_CHAT_MESSAGE){
				newMessage.setSession("(" + muscleNetIO.getLocalSessionID() + ") " + getLocalUserName());
			}
			fireLogNewMessage(newMessage);
		}
	}
	
	/**
	 *	Implementation of JavaShareEventListener
	 */
	public void javaShareEventPerformed(JavaShareEvent bse){
		switch (bse.getType()){
			case JavaShareEvent.CONNECTION_ATTEMPT: {
			} break;
			case JavaShareEvent.CONNECTION_DISCONNECT: {
				userHashTable.clear();
				chatterPanel.clearUserTable();
			} break;
			case JavaShareEvent.SERVER_CONNECTED: {
				muscleNetIO.beShareSubscribe();
				
				// Determine what setLocalUserName to use.
				if (programPrefsMessage.hasField("installid")) {
					try {
						muscleNetIO.setLocalUserName(getLocalUserName(), startingPortNumber, programPrefsMessage.getLong("installid"));
					} catch (Exception e){
						muscleNetIO.setLocalUserName(getLocalUserName(), startingPortNumber);
					}
				} else
					muscleNetIO.setLocalUserName(getLocalUserName(), startingPortNumber);
				muscleNetIO.setLocalUserStatus(getLocalUserStatus());
				
				try {
					muscleNetIO.setUploadBandwidth(programPrefsMessage.getString("uploadLabel"),
													programPrefsMessage.getInt("uploadValue"));
				} catch (Exception e){
				}
				// Do the login commands!
				for(int x = 0; x < onLoginVect.size(); x++){
					// Send the commands out as if you typed them!
					chatMessage(
						new ChatMessage("", (String)onLoginVect.elementAt(x)
								, false
								, ChatMessage.LOG_LOCAL_USER_CHAT_MESSAGE
								, true
								, ""));
				}
				if (transPan != null)
					transPan.resetShareList();
			} break;
			case JavaShareEvent.SERVER_DISCONNECTED: {
				userHashTable.clear();
				chatterPanel.clearUserTable();
			} break;
			case JavaShareEvent.UNKNOWN_MUSCLE_MESSAGE: {
				fireLogNewMessage(new ChatMessage("", "Unknown MUSCLE" +
					" message received.", false,
					ChatMessage.LOG_INFORMATION_MESSAGE, true, ""));
			} break;
			case JavaShareEvent.CHAT_MESSAGE: {
				ChatMessage temp = (ChatMessage)bse.getSource();
				// If this message matches the ignore pattern, why do anything
				// else with it?
				if (isMatchingPattern(ignorePatVect, temp)){
					return;
				}
				// Private Message Determination.
				if(temp.isPrivate()){
					// If this matchs the autopriv patterns, and there's no
					// poster registered for the sessionID, we make a new
					// private chat window.
					if (isMatchingPattern(autoPrivVect, temp)
							&& (isPosterRegisteredForSession(
							temp.getSession()).equals("")))
					{
						PrivateFrame privFrame = new PrivateFrame(this,
							this, temp.getSession());
						privFrame.pack();
						privFrame.show();
						try {
							if (programPrefsMessage.getBoolean("prvWSnd")){
								fireSoundEvent(new
									SoundEvent(SoundEvent.PRIVATE_MESSAGE_WINDOW));
							}
						} catch (Exception e){
						}
					}
					temp.setTargetID(isPosterRegisteredForSession(temp.getSession()));
				}
				// This scans the incomming, non-private message to see if our
				// name was said, and fires a SoundEvent if it was.
				try {
					if((! temp.isPrivate())
						&& (temp.getMessage().indexOf(getLocalUserName()) != -1)
						&& programPrefsMessage.getBoolean("sndUName"))
					{
						fireSoundEvent(new
								SoundEvent(SoundEvent.USER_NAME_MENTIONED));
					} else if (temp.isPrivate() && programPrefsMessage.getBoolean("prvMSnd")){
						fireSoundEvent(new
								SoundEvent(SoundEvent.PRIVATE_MESSAGE_RECEIVED));
					}
					// If the message matches a watch pattern, set the message type.
					if (isMatchingPattern(watchPatVect, temp)){
						temp.setType(ChatMessage.LOG_WATCH_PATTERN_MATCH);
						if (programPrefsMessage.getBoolean("sndWPat")){
							fireSoundEvent(new
							SoundEvent(SoundEvent.WATCHED_USER_SPEAKS));
						}
					}
				} catch (Exception e){
				}
				fireLogNewMessage(temp);
			} break;
			case JavaShareEvent.PING_RECEIVED: {
			} break;
			case JavaShareEvent.USER_DISCONNECTED: {
				BeShareUser tempUser = bse.getUser();
				fireLogNewMessage(new ChatMessage("", "User #"
					+ tempUser.getConnectID() + " (a.k.a. "
					+ findNameBySession(tempUser.getConnectID()) + 
					") has disconnected.", false,
					ChatMessage.LOG_USER_EVENT_MESSAGE, true, ""));
				userHashTable.remove(tempUser.getConnectID());
				chatterPanel.removeUser(tempUser);
			} break;
			case JavaShareEvent.USER_CONNECTED: {
				BeShareUser tempUser = bse.getUser();
				// Determine if it's an add or an update.
				if (userHashTable.containsKey(tempUser.getConnectID())){
					BeShareUser oldUserData = (BeShareUser)userHashTable.get(tempUser.getConnectID());
					oldUserData.setName(tempUser.getName());
					oldUserData.setClient(tempUser.getClient());
					oldUserData.setPort(tempUser.getPort());
					userHashTable.put(tempUser.getConnectID(), oldUserData);
					chatterPanel.updateUser(oldUserData);
					fireLogNewMessage(new ChatMessage("", "User #"
						+ tempUser.getConnectID() + " is now known as "
						+ tempUser.getName(), false, ChatMessage.LOG_USER_EVENT_MESSAGE,
						true, ""));
				} else {
					userHashTable.put(tempUser.getConnectID(), tempUser);
					chatterPanel.addUser(tempUser);
					fireLogNewMessage(new ChatMessage("", "User #" + 
						tempUser.getConnectID() + " is now connected.", false,
						ChatMessage.LOG_USER_EVENT_MESSAGE, true, ""));
				}
			} break;
			case JavaShareEvent.USER_STATUS_CHANGE: {
				BeShareUser tempUser = bse.getUser();
				if (userHashTable.containsKey(tempUser.getConnectID())){
					BeShareUser oldUserData	= (BeShareUser)userHashTable.get(tempUser.getConnectID());
					oldUserData.setStatus(tempUser.getStatus());
					if (tempUser.getPort() != -1)
						oldUserData.setPort(tempUser.getPort());
					userHashTable.put(tempUser.getConnectID(), oldUserData);
					chatterPanel.updateUser(oldUserData);
					fireLogNewMessage(new ChatMessage("", "User #"
						+ tempUser.getConnectID() + " (a.k.a. "
						+ findNameBySession(tempUser.getConnectID()) + 
						") is now " + oldUserData.getStatus() + ".", false,
						ChatMessage.LOG_USER_EVENT_MESSAGE, true, ""));
				} else {
					tempUser.setName("<unknown>");
					userHashTable.put(tempUser.getConnectID(), tempUser);
					chatterPanel.addUser(tempUser);
					fireLogNewMessage(new ChatMessage("", "User #"
						+ tempUser.getConnectID() + " (a.k.a. "
						+ findNameBySession(tempUser.getConnectID()) + 
						") is now " + tempUser.getStatus() + ".", false,
						ChatMessage.LOG_USER_EVENT_MESSAGE, true, ""));
				}
			} break;
			case JavaShareEvent.USER_UPLOAD_STATS_CHANGE: {
				BeShareUser tempUser = bse.getUser();
				if (userHashTable.containsKey(tempUser.getConnectID())){
					BeShareUser oldUserData	= (BeShareUser)userHashTable.get(tempUser.getConnectID());
					if(tempUser.getUploadCurrent() != -1)
						oldUserData.setUploadCurrent(tempUser.getUploadCurrent());
					if(tempUser.getUploadMax() != -1)
						oldUserData.setUploadMax(tempUser.getUploadMax());
					if (tempUser.getPort() != -1)
						oldUserData.setPort(tempUser.getPort());
					userHashTable.put(tempUser.getConnectID(), oldUserData);
					chatterPanel.updateUser(oldUserData);
				} else {
					tempUser.setName("<unknown>");
					userHashTable.put(tempUser.getConnectID(), tempUser);
					chatterPanel.addUser(tempUser);
				}
			} break;
			case JavaShareEvent.USER_BANDWIDTH_CHANGE: {
				BeShareUser tempUser = bse.getUser();
				if (userHashTable.containsKey(tempUser.getConnectID())){
					BeShareUser oldUserData	= (BeShareUser)userHashTable.get(tempUser.getConnectID());
					if(tempUser.getBandwidthLabel() != "")
						oldUserData.setBandwidthLabel(tempUser.getBandwidthLabel());
					if(tempUser.getBandwidthBps() != -1)
						oldUserData.setBandwidthBps(tempUser.getBandwidthBps());
					userHashTable.put(tempUser.getConnectID(), oldUserData);
					chatterPanel.updateUser(oldUserData);
				} else {
					tempUser.setName("<unknown>");
					userHashTable.put(tempUser.getConnectID(), tempUser);
					chatterPanel.addUser(tempUser);
				}
			} break;
			case JavaShareEvent.USER_FIREWALL_CHANGE: {
				BeShareUser tempUser = bse.getUser();
				if (userHashTable.containsKey(tempUser.getConnectID())){
					BeShareUser oldUserData	= (BeShareUser)userHashTable.get(tempUser.getConnectID());
					oldUserData.setFirewall(tempUser.getFirewall());
					userHashTable.put(tempUser.getConnectID(), oldUserData);
					chatterPanel.updateUser(oldUserData);
				} else {
					tempUser.setName("<unknown>");
					userHashTable.put(tempUser.getConnectID(), tempUser);
					chatterPanel.addUser(tempUser);
				}
			} break;
			case JavaShareEvent.USER_FILE_COUNT_CHANGE: {
				BeShareUser tempUser = bse.getUser();
				if (userHashTable.containsKey(tempUser.getConnectID())){
					BeShareUser oldUserData	= (BeShareUser)userHashTable.get(tempUser.getConnectID());
					oldUserData.setFileCount(tempUser.getFileCount());
					userHashTable.put(tempUser.getConnectID(), oldUserData);
					chatterPanel.updateUser(oldUserData);
				} else {
					tempUser.setName("<unknown>");
					userHashTable.put(tempUser.getConnectID(), tempUser);
					chatterPanel.addUser(tempUser);
				}
			} break;
			case JavaShareEvent.FILE_INFO_ADD_TO_RESULTS: {
				transPan.addResult((SharedFileInfoHolder)bse.getSource());
			} break;
			case JavaShareEvent.FILE_INFO_REMOVE_RESULTS: {
				transPan.removeResult((SharedFileInfoHolder)bse.getSource());
			} break;
		}
	}
	
	/**
		Tests a ChatMessage across all Regex patters stored in <code>regexVect
		</code>. If any pattern matches either the <code>session</code>,
		<code>name</code>, or <code>message</code> of the ChatMessage, then it
		returns <code>true</code>. If none of the fields mentioned above match,
		it returns <code>false</code>
		@param regexVect - A <code>Vector</code> of <code>RE</code> regex objects.
		@param temp the <code>ChatMessage</code> to match patterns with.
		@return <code>true</code> if any pattern matches, <code>false</code> if
		no patterns match.
	*/
	public boolean isMatchingPattern(Vector regexVect, ChatMessage temp){
		if (regexVect.size() > 0){
			if (((RE)regexVect.elementAt(0)).isMatch("all")){
				return true;
			} else {
				// Time to search for the regex expression.
				for(int x = 0; x < regexVect.size(); x++){
					// Search by name
					if (((RE)regexVect.elementAt(x)).isMatch((Object)findNameBySession(temp.getSession()))){
						return true;
					// Search by Session ID
					} else if (((RE)regexVect.elementAt(x)).isMatch((Object)temp.getSession())){
						return true;
					// Search by message Content
					} else if (((RE)regexVect.elementAt(x)).isMatch((Object)temp.getMessage())){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Gets an ImageIcon specified by <code>name</code> for </code>cmp</code>
	 */
	public static ImageIcon loadImage(String name, Component cmp) {
		ImageIcon img = (ImageIcon)imageCache.get(name);
		
		if (img == null) {
			URL fileLoc = null;
			try {
				URLClassLoader urlLoader = (URLClassLoader)cmp.getClass().getClassLoader();
				fileLoc = urlLoader.findResource(name);
			} catch (NoClassDefFoundError ncdfe) {
				// For pre-Java 2 platforms
				fileLoc = name.getClass().getClassLoader().getSystemResource(name);
			}
			
			if (fileLoc != null) {
				img = new ImageIcon(fileLoc);
				imageCache.put(name, img);
			}
		}
		
		return img;
	}
}
