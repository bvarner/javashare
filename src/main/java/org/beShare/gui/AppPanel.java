package org.beShare.gui;

import blv.swing.AboutDialog;
import com.meyer.muscle.message.Message;
import com.meyer.muscle.support.Rect;
import gnu.regexp.RE;
import gnu.regexp.REException;
import org.beShare.data.*;
import org.beShare.event.*;
import org.beShare.gui.prefPanels.JavaSharePrefListener;
import org.beShare.network.JavaShareTransceiver;
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


/**
 *	AppPanel.java - JavaShare 2's main hub panel.
 *
 *	@author Bryan Varner
 */
public class AppPanel extends JPanel implements JavaShareEventListener,
												ChatMessageListener,
												ActionListener,
												UserHashAccessor,
												JavaSharePrefListener
{
	public static final String buildVersion = "2.1 snapshot ";
	public static final String applicationName = "JavaShare";
	public static final String pubVersion = applicationName + buildVersion;
	
	public static final String AutoUpdateURL = "http://beshare.tycomsystems.com/servers.txt";
	
	private static Hashtable	imageCache = new Hashtable();
	
	PrefsFrame 					prefsFrame;
	Message						programPrefsMessage;
	
	JSplitPane					queryChatSplit;
	
	ChatPanel					chatterPanel;
	boolean						saveUserSort;
	LocalUserPanel				localUserInfo;
	JavaShareTransceiver        muscleNetIO;
	AboutDialog					aboutJavaShare;
	
	TransferPanel				transPan;
	
	Hashtable					userHashTable;
	Hashtable					aliasTable;
	Object						menuBar;
	Vector						chatMessageListenerVect;
	Vector						soundTriggerListenerVect;
	
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
	public AppPanel(final JavaShareTransceiver muscleNetIO, final Message prefsMessage){
        this.programPrefsMessage = prefsMessage;

        this.muscleNetIO = muscleNetIO;

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
		
		this.prefsFrame = new PrefsFrame(this, programPrefsMessage);
		
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
		this.aboutJavaShare = new AboutDialog(this,
				"About JavaShare 2",
				true, aboutText, javaShareIcon, 2, 20);
		
		String[] serverList = null;
		String[] nickList = null;
		String[] statusList = null;
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
	        serverList[0] = "beshare.tycomsystems.com";
		serverList = MusclePreferenceReader.getStrings(programPrefsMessage, "servers", serverList);
		muscleNetIO.setServerName(serverList[MusclePreferenceReader.getInt(programPrefsMessage, "curServer", 0)]);
		
		// Nicknames
		nickList = new String[1];
        nickList[0] = "Binky";
		nickList = MusclePreferenceReader.getStrings(programPrefsMessage, "nicks", nickList);
        muscleNetIO.setLocalUserName(nickList[MusclePreferenceReader.getInt(programPrefsMessage, "curNick", 0)]);

		// Status
		statusList = new String[1];
		statusList[0] = "Here";
		statusList = MusclePreferenceReader.getStrings(programPrefsMessage, "status", statusList);
        muscleNetIO.setLocalUserStatus(statusList[MusclePreferenceReader.getInt(programPrefsMessage, "curStatus", 0)]);

        this.localUserInfo = new LocalUserPanel(muscleNetIO, false);
        this.localUserInfo.setHereStatus(MusclePreferenceReader.getInt(programPrefsMessage, "curStatus", 0));
        this.localUserInfo.setAwayStatus(MusclePreferenceReader.getInt(programPrefsMessage, "awayStatus", 0));

		// Minibrowser
		boolean useMiniBrowser = MusclePreferenceReader.getBoolean(programPrefsMessage, "miniBrowser", false);
		
		this.setLayout(new BorderLayout());
		chatMessageListenerVect = new Vector();
		soundTriggerListenerVect = new Vector();

		// add our list data to the localUserInfo...
        for(int x = 0; x < serverList.length; x++){
            if(! serverList[x].equals(muscleNetIO.getServerName()))
                    localUserInfo.addServerName(serverList[x]);
        }

        for(int x = 0; x < nickList.length; x++){
            if(! nickList[x].equals(muscleNetIO.getLocalUserName()))
                    localUserInfo.addUserName(nickList[x]);
        }

        for (int x = 0; x < statusList.length; x++){
            if(! statusList[x].equals(muscleNetIO.getLocalUserStatus()))
                    localUserInfo.addStatus(statusList[x]);
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
		
		menuBar = null;
		this.add(localUserInfo, BorderLayout.NORTH);
		
		this.add(chatterPanel, BorderLayout.CENTER);

        transPan = new TransferPanel(this.muscleNetIO, programPrefsMessage, this);
        prefsFrame.addFileTransferPrefs(programPrefsMessage, transPan);

        queryChatSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        queryChatSplit.add(transPan);
        queryChatSplit.add(chatterPanel);

        this.remove(chatterPanel);
        this.add(queryChatSplit, BorderLayout.CENTER);

        SwingUtilities.updateComponentTreeUI(this);

        this.muscleNetIO.addJavaShareEventListener(this);

        fireLogNewMessage(new ChatMessage("","Welcome to JavaShare 2" +
                "\nType /help for a list of " +
                "commands.", false,
                ChatMessage.LOG_INFORMATION_MESSAGE));


        this.muscleNetIO.setServerPort(2960);
        if(programPrefsMessage.getBoolean("autoLogin", false)){
            muscleNetIO.connect();
        }


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
			Rectangle frameBounds = ((ShareFrame)this.getRootPane().getParent()).getBounds();
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
		Returns the ChatPanel object.
		@return the ChatPanel child for this frame
	*/
	public ChatPanel getChatPanel(){
		return chatterPanel;
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
	 * Searches the User table for a SessionID which matches the supplied
	 * Name.
     *
     * @param startsWithName A String that starts with the name of a user.
     * @return The SessionID of that user, if one, or "" if no match is found.
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
	 * Searches the User Table for a name that matches the supplied SessionID.
	 * @param sessionID The SessionID to resolve a name for
	 * @return The Name of user matching sessionID, or an empty "" String.
	 */
	public String findNameBySession(String sessionID){
		if (userHashTable.containsKey(sessionID)){
			return ((BeShareUser)userHashTable.get(sessionID)).getName();
		} else {
			if(sessionID.equals("")){
				return muscleNetIO.getLocalUserName();
			} else {
				return "";
			}
		}
	}
	
	/**
	 * Searches through the user Table for names starting with the supplied
	 * string. If it finds one, it returns the rest of the name, not the entire
	 * name.
     *
     * @param partialName The Beginning of a name.
     * @return The Missing Remainder of the name or an empty "" String
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
			((ShareFrame)this.getRootPane().getParent()).setTitle(
					AppPanel.pubVersion + " @" + muscleNetIO.getServerName());
		} catch (Exception exc){
		}
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
			newMessage.setSession("(" + muscleNetIO.getLocalSessionID() + ") " + muscleNetIO.getLocalUserName());
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
						newMessage.setSession("(" + muscleNetIO.getLocalSessionID() + ") " + muscleNetIO.getLocalUserName() + " -> (" + findNameBySession(targetSession.trim()) + ")");
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
							newMessage.setSession("(" + muscleNetIO.getLocalSessionID() + ") " + muscleNetIO.getLocalUserName() + " -> (" + findNameBySession(targetSession.trim()) + ")");
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
                muscleNetIO.setLocalUserName(commandLine);
			} else if (command.equals("/STATUS")){
                muscleNetIO.setLocalUserStatus(commandLine);
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
                    muscleNetIO.setServerName(commandLine);
				}
				muscleNetIO.connect();
			} else if (command.equals("/QUIT")){
				quitRequested();
			} else if (command.equals("/DISCONNECT")){
				muscleNetIO.disconnect();
				chatterPanel.clearUserTable();
			} else if (command.equals("/SERVER")){
				muscleNetIO.setServerName(commandLine);
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
				newMessage.setSession("(" + muscleNetIO.getLocalSessionID() + ") " + muscleNetIO.getLocalUserName());
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
                localUserInfo.setServerName(muscleNetIO.getServerName());
                fireLogNewMessage(new ChatMessage("", "Active server"
                        + " changed to: " + muscleNetIO.getServerName(), false,
                        ChatMessage.LOG_USER_EVENT_MESSAGE, true, ""));
                updateFrameTitle();
			    break;
            }
			case JavaShareEvent.CONNECTION_DISCONNECT: {
				userHashTable.clear();
				chatterPanel.clearUserTable();
                break;
			}
            case JavaShareEvent.LOCAL_USER_NAME: {
                // TODO: Add the localUserInfo as a listener to the JavaShareTransceiver.
                localUserInfo.setUserName(muscleNetIO.getLocalUserName());
                fireLogNewMessage(new ChatMessage("", "Your name has been"
                        + " changed to " + muscleNetIO.getLocalUserName(), false,
                        ChatMessage.LOG_USER_EVENT_MESSAGE, true, ""));

                break;
            }
            case JavaShareEvent.LOCAL_USER_STATUS: {
                // TODO: Add the localUserInfo as a listener to the JavaShareTransceiver.
                localUserInfo.setUserStatus(muscleNetIO.getLocalUserStatus());
                fireLogNewMessage(new ChatMessage("", "Your Status has been"
                        + " changed to: " + muscleNetIO.getLocalUserStatus(), false,
                        ChatMessage.LOG_USER_EVENT_MESSAGE, true, ""));
                break;
            }
			case JavaShareEvent.SERVER_CONNECTED: {
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
				if (transPan != null) {
                    transPan.resetShareList();
                }
                break;
			}
			case JavaShareEvent.SERVER_DISCONNECTED: {
				userHashTable.clear();
				chatterPanel.clearUserTable();
                break;
			}
			case JavaShareEvent.UNKNOWN_MUSCLE_MESSAGE: {
				fireLogNewMessage(new ChatMessage("", "Unknown MUSCLE" +
					" message received.", false,
					ChatMessage.LOG_INFORMATION_MESSAGE, true, ""));
                break;
			}
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
						&& (temp.getMessage().indexOf(muscleNetIO.getLocalUserName()) != -1)
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
                break;
			}
			case JavaShareEvent.PING_RECEIVED: {
                break;
			}
			case JavaShareEvent.USER_DISCONNECTED: {
                BeShareUser tempUser = bse.getUser();
                fireLogNewMessage(new ChatMessage("", "User #"
                        + tempUser.getConnectID() + " (a.k.a. "
                        + findNameBySession(tempUser.getConnectID()) +
                        ") has disconnected.", false,
                        ChatMessage.LOG_USER_EVENT_MESSAGE, true, ""));
                userHashTable.remove(tempUser.getConnectID());
                chatterPanel.removeUser(tempUser);
                break;
            }
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
                break;
			}
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
                break;
			}
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
                break;
			}
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
                break;
			}
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
                break;
			}
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
                break;
			}
			case JavaShareEvent.FILE_INFO_ADD_TO_RESULTS: {
				transPan.addResult((SharedFileInfoHolder)bse.getSource());
                break;
			}
			case JavaShareEvent.FILE_INFO_REMOVE_RESULTS: {
				transPan.removeResult((SharedFileInfoHolder)bse.getSource());
                break;
			}
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
