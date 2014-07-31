/* Change Log:
	Class Started: 1-30-2002
	History
	-------
	1.0 - Finished impelmenting all chat functions.
	1.0.1 - Began transition to JavaShareEvents.
	1.0.2 - Made the move from ActionEvents to JavaShareEvents, also added
			Vector to hold registered listeners, and iterates through
			the vector when firing messages off. Now more than one target
			can receive messages.
	1.0.3 - Received Pings are now sent to the proper target(s).
	1.0.4 - ServerInfo function added.
	1.0.5 - setUploadBandwidth method was added.
	1.0.6 - convertFromUTFEight and convertToUTFEight methods added. All
			Incomming and outgoing Text is now converted with the appropriate
			method before being sent..
	1.0.7 - Began implementing the necessary functions for File-Sharing. This is
			where things will get tricky, and fun!
	1.1   - Removed all UTF-8 conversion functions. I've submitted patches to MUSCLE
			which allow it to send UTF-8. All conversion is now done by the MUSCLE
			classes themselves. Updated the version message to reflect the new versioning.
	1.1.1 - Included the new versioning information in the 'name' message sent during the
			the connect stage.
	2.0 a1 - Renamed the class to what it should have been. Tranto. :-) Also getting ready
			for file-sharing.
	2.0 a2 - connected boolean now actuall reflects the current connection status.
			firewalled boolean now exists. and get/set Firewall now works. About time, eh?
	2.0 a4 - Query support, removal and updating of subscribed non-name node (file info nodes).
	2.0 a5 - Fixed bug with setInstallId. That function is now gone. Integrated into another setLocalUserName.
			The installid was over-writing the existing data from the beshare/name node.
	2.0 a6 - Added ConnectionCheck class and the activity monitor variables.
	2.0 a7 - Added AutoReconnector class and the variables to track re-connects.
			Reworked a lot on the messageReceived() Disconnect tag section.
	2.0 a8 - 1.17.2003 MAJOR refactoring. muscleMessageReceived is toast, replaced by the 
			leaner, meaner, cleaner muscleMessageHandler().
*/
package org.beShare.network;

import com.meyer.muscle.client.MessageTransceiver;
import com.meyer.muscle.client.StorageReflectConstants;
import com.meyer.muscle.message.Message;
import com.meyer.muscle.support.TypeConstants;
import com.meyer.muscle.thread.MessageListener;
import com.meyer.muscle.thread.MessageQueue;
import org.beShare.data.BeShareUser;
import org.beShare.data.ChatMessage;
import org.beShare.data.SharedFileInfoHolder;
import org.beShare.event.JavaShareEvent;
import org.beShare.event.JavaShareEventListener;
import org.beShare.gui.AppPanel;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

/**
	Tranto - This class handles all incomming/outgoing Muscle messages
	and then sends the appropriate JavaShareEvents to any registered listener.
	
	This class maintains the connection between the muscle server, and the UI.
	The UI makes calls directly to the applications object of this class,
	while this class fires JavaShareEvents off to the UI.
	
	The idea here is that this class will hold all network functions,
	so that a GUI can be implemented in a modular form on top of it.
	
	@author Bryan Varner
	@version 2.0 a8
*/

public class Tranto implements MessageListener, StorageReflectConstants, TypeConstants
{
	public final String MUSCLE_INTERFACE_VERSION = AppPanel.applicationName + " " + AppPanel.buildVersion;
	
	private String 				serverName;
	private int 				serverPort;
	private MessageTransceiver 	beShareTransceiver =
			new MessageTransceiver(new MessageQueue(this));
			
	private String 		localSessionID = "";
	
	private Object 		serverConnect;
	private Object 		serverDisconnect;
	
	private Vector 		beShareEventListenerVect;
	
	private boolean 	requestedServerInfo;
	
	private boolean		connected;
	private boolean		firewalled;
	private int			pingCount;
	private boolean		queryActive;
	
	// Connection Management
	ConnectionCheck		connectionTest;
	private boolean		recentActivity = false;
	private int			connectionTimeout = 300; // in seconds = 5 Minutes
	AutoReconnector		autoRecon = null;
	
	private static final int ROOT_DEPTH						= 0;	// root node
	private static final int HOST_NAME_DEPTH				= 1;
	private static final int SESSION_ID_DEPTH				= 2;
	private static final int BESHARE_HOME_DEPTH				= 3;	// used to separate our stuff from other (non-BeShare) data on the same server
	private static final int USER_NAME_DEPTH				= 4;	// user's handle node would be found here
	private static final int FILE_INFO_DEPTH				= 5;	//This is where file names are
	
	private static final int NET_CLIENT_NEW_CHAT_TEXT		= 2;
	private static final int NET_CLIENT_CONNECT_BACK_REQUEST = 3;
	private static final int NET_CLIENT_PING				= 5;
	private static final int NET_CLIENT_PONG				= 6;
	
	/**
	 * 	Default contstructor - Creates a new BeShare muscle interface
	 * 	with no server or port , and does not connect.
	 * 	@param bse the JavaShareEventListener
	 */
	public Tranto(JavaShareEventListener bse){
		serverName = "";
		serverPort = 2960;
		beShareEventListenerVect = new Vector();
		requestedServerInfo = false;
		connected = false;
		firewalled = false;
		queryActive = false;
		pingCount = 0;
		this.addJavaShareEventListener(bse);
		autoRecon = null;
		connectionTest = new ConnectionCheck();
		connectionTest.start();
	}
	
	/**
	* Creates a new BeShare muscle interface. Creates interface and connects to
	* server.
	* @param bse The JavaShareEventListener
	* @param serverName The name of the server to connect to
	* @param serverPort the port of the server to connect to
	*/
	public Tranto(JavaShareEventListener bse,
								String serverName,
								int serverPort)
	{
		this(bse);
		serverName = serverName;
		serverPort = serverPort;
		connect();
	}
	
	/**
	 *	Get the name of the server we're connecting to.
	 *	@return the Name of the server to connect to.
	 */
	public String getServerName() {
		return serverName;
	}
	
	/**
	 *	Gets the id of the local user.
	 *	@return String the local SessionID of this user.
	 */
	public String getLocalSessionID() {
		return localSessionID;
	}

	/**
	 *	Sets the name of the server to connect to.
	 *	@param sName - the Name of the server to connect to.
	 */
	public void setServerName(String sName) {
		serverName = sName;
	}
	
	/**
	 *	Returns the port number to connect with.
	 *	@return the port to connect with.
	 */
	public int getServerPort() {
		return serverPort;
	}
	
	/**
	 *	Sets the port number to connect with.
	 *	@param sPort - the port to connect with
	 */
	public void setServerPort(int sPort) {
		serverPort = serverPort;
	}
	
	/**
	 *	Add a new JavaShareEventListener to receive JavaShareEvents from this
	 *	Muscle Interface.
	 */
	public void addJavaShareEventListener(JavaShareEventListener bsel) {
		beShareEventListenerVect.addElement(bsel);
	}

	/**
	 *	Removes an existing JavaShareEventListener.
	 *	This stops the Listener from receiving messages from this Interface.
	 */
	public void removeJavaShareEventListener(JavaShareEventListener bsel) {
		beShareEventListenerVect.removeElement(bsel);
	}
	
	/**
	 *	Fires JavaShareEvents to all registered listeners.
	 */
	protected void fireJavaShareEvent(JavaShareEvent bse) {
		for(int x = 0; x < beShareEventListenerVect.size(); x++) {
			((JavaShareEventListener)beShareEventListenerVect.elementAt(x))
					.javaShareEventPerformed(bse);
		}
	}
	
	/**
	 *	Starts a new query. This will also stop any pending queries, and send a
	 *	clear query list message.
	 */
	public void startQuery(String sessionExpression, String fileExpression){
		stopQuery(); // If you can't figure out why we would do this... <sigh>
		queryActive = true;
		// This path string tells muscled which files it should inform us about.
		// Since we may be basing part of our query on the session ID, we use
		// the full path string for this query.
		String temp = "SUBSCRIBE:/*/";
		temp += sessionExpression;
		temp += "/beshare/";
		temp += getFirewalled() ? "files/" : "fi*/";  // If we're firewalled, we can only get non-firewalled files; else both types
		temp += fileExpression;
		
		// Send the subscription!
		Message queryMsg = new Message(PR_COMMAND_SETPARAMETERS);
		queryMsg.setBoolean(temp, true);
		beShareTransceiver.sendOutgoingMessage(queryMsg);
		
		// Send a ping to the server.  When we get it back, we know the
		// initial scan of the query is done!
		Message ping = new Message(PR_COMMAND_PING);
		ping.setInt("count", pingCount);
		beShareTransceiver.sendOutgoingMessage(ping);
		
		//_queryActive = true;
		//_sessionIDRegExp.SetPattern(sessionIDRegExp);
		//_fileNameRegExp.SetPattern(fileNameRegExp);
	}
	
	/**
	 *	Stops the current Query
	 */
	public void stopQuery(){
		queryActive = false;
		// Remove the subscription
		Message removeMsg = new Message(PR_COMMAND_REMOVEPARAMETERS);
		removeMsg.setString(PR_NAME_KEYS, "SUBSCRIBE:*beshare/fi*");
		beShareTransceiver.sendOutgoingMessage(removeMsg);

		// Have the server jettison (cancel) all the remaining results it hasn't sent to us.
		Message cancel = new Message(PR_COMMAND_JETTISONRESULTS);
		cancel.setString(PR_NAME_KEYS, "beshare/fi*/*");
		beShareTransceiver.sendOutgoingMessage(cancel);
	}


	/**
	 *	Sends a message to the host at targetSessionID requesting they connect
	 *	to us on <code>port</code> so we can transfer a file from them.
	 */
	public void sendConnectBackRequestMessage(String targetSessionID, int port){
		System.out.println("Tranto: Sending Connectback request");
		
		Message cbackMsg = new Message(NET_CLIENT_CONNECT_BACK_REQUEST);
		String target = "/*/" + targetSessionID + "/beshare";
		cbackMsg.setString(PR_NAME_KEYS, target);
		cbackMsg.setString("session", getLocalSessionID());
		cbackMsg.setInt("port", port);
		beShareTransceiver.sendOutgoingMessage(cbackMsg);
		System.out.println("Tranto: Connectback request Sent.");
	}
	
	/**
	 * Sets weather or not we should behave as if we are firewalled.
	 * If we aren't, peachy! If we are, who gives a crap?!
	 */
	public void setFirewalled(boolean firewall){
		firewalled = firewall;
	}
	
	/**
	 * Returns the current stats of the firewall setting.
	 */
	public boolean getFirewalled(){
		return firewalled;
	}
	
	/**
	 *Connects to MUSCLE server.
	 */
	public synchronized void connect(){
		disconnect();
		serverConnect = new Object();
		serverDisconnect = new Object();
		fireJavaShareEvent(new JavaShareEvent(this,
				JavaShareEvent.CONNECTION_ATTEMPT));
		beShareTransceiver.connect(serverName, serverPort,
				serverConnect, serverDisconnect);
	}
	
	/**
	 *	Disconnects from MUSCLE server
	 */
	public synchronized void disconnect(){
		fireJavaShareEvent(new JavaShareEvent(this,
				JavaShareEvent.CONNECTION_DISCONNECT));
		serverConnect = null;
		serverDisconnect = null;
		connected = false;
		beShareTransceiver.disconnect();
	}
	
	/**
	 *	Returns connection status
	 *	@return true if connected, false if not connected.
	 */
	public boolean isConnected(){
		return connected;
	}
	
	/**
	 *	Sends a message subscribing to the BeShare nodes.
	 */
	public void beShareSubscribe(){
		beShareTransceiver.sendOutgoingMessage(
				new Message(PR_COMMAND_GETPARAMETERS));
		// set up a subscription to the beShare tree.
		Message queryMsg = new Message(PR_COMMAND_SETPARAMETERS);
		queryMsg.setBoolean("SUBSCRIBE:beshare/*", true);
		beShareTransceiver.sendOutgoingMessage(queryMsg);
	}
	
	/**
	 *	Sends <code>text</code> to user with session of <code>session</code>
	 *	@param text The String to send
	 *	@param session The Persons session to send it to.
	 */
	public void sendTextMessage(String text, String session){
		Message chatMessage = new Message(NET_CLIENT_NEW_CHAT_TEXT);
		
		chatMessage.setString(PR_NAME_KEYS, "/*/"
									+ "(" + session + ")/beshare");
		chatMessage.setString("session", localSessionID);
		chatMessage.setString("text", text);
		if (! session.equals("*"))
			chatMessage.setBoolean("private", true);
		beShareTransceiver.sendOutgoingMessage(chatMessage);
	}
	
	/**
	 *	Sends a Ping to the user with the specified session id.
	 *	@param session The SessionID of the user to ping.
	 */
	public void pingUser(String session){
		Message pingMessage = new Message(NET_CLIENT_PING);
		pingMessage.setString(PR_NAME_KEYS, "/*/"
				+ session + "/beshare");
		pingMessage.setLong("when", System.currentTimeMillis());
		pingMessage.setString("session", localSessionID);
		beShareTransceiver.sendOutgoingMessage(pingMessage);
	}
	
	/**
	 *	Sends a <code>PR_COMMAND_GETPARAMETERS</code> to the server, and sets
	 *	the <code>requestedServerInfo</code> field. This is used when a the user
	 *	requests system Information.
	 */
	public void getServerInfo(){
		Message infoMessage = new Message(PR_COMMAND_GETPARAMETERS);
		beShareTransceiver.sendOutgoingMessage(infoMessage);
		requestedServerInfo = true;
	}
	
	/**
	 *	Uploads UserName information to the MUSCLE server
	 *	@param uName The Users handle
	 *	@param port the port they use for filesharing.
	 */
	public void setLocalUserName(String uName, int port){
		Message nameMessage = new Message();
		nameMessage.setString("name", uName);
		nameMessage.setInt("port", port);
		nameMessage.setString("version_name", AppPanel.applicationName);
		nameMessage.setString("version_num", "v" + AppPanel.buildVersion);
		setDataNodeValue("beshare/name", nameMessage);
	}
	
	/**
	 *	Uploads UserName information to the MUSCLE server
	 *	@param uName The Users handle
	 *	@param port the port they use for filesharing.
	 *  @param installid the unique identifier for this install.
	 */
	public void setLocalUserName(String uName, int port, long installid){
		Message nameMessage = new Message();
		nameMessage.setString("name", uName);
		nameMessage.setInt("port", port);
		nameMessage.setString("version_name", AppPanel.applicationName);
		nameMessage.setString("version_num", "v" + AppPanel.buildVersion);
		nameMessage.setLong("installid", installid);
		setDataNodeValue("beshare/name", nameMessage);
	}
	
	/**
	 *	Uploads Bandwidth information to the MUSCLE server
	 *	@param lbl String label 'T1', 'Cable', ...
	 *	@param bps The speed of the connection.
	 */
	public void setUploadBandwidth(String lbl, int bps){
		Message bwMessage = new Message();
		bwMessage.setString("label", lbl);
		bwMessage.setInt("bps", bps);
		setDataNodeValue("beshare/bandwidth", bwMessage);
	}

	/**
	 *	Uploads the local user status to the MUSCLE server
	 *	@param uStatus The Users current status
	 */
	public void setLocalUserStatus(String uStatus){
		Message statusMessage = new Message();
		statusMessage.setString("userstatus", uStatus);
		setDataNodeValue("beshare/userstatus", statusMessage);
	}

	/**
	 * Uploads the file-listing to the MUSCLE server. This is a
	 * tricky area, since BeShare's source is a bit less than
	 * easy for me to read.
	 */
	public void uploadFileListing(Vector fileListing){
		Message sizeMessage = new Message();
		sizeMessage.setInt("filecount", fileListing.size());
		setDataNodeValue("beshare/filecount", sizeMessage);
		System.out.println("Send File count message. Files Listed: " + fileListing.size());
		
		// From here we upload the actual list of files.
		Message uploadMessage = new Message(PR_COMMAND_SETDATA);
		for (int x = 0; x < fileListing.size(); x++){
			// Get the file-specific data into a sub-message.
			Message infoMessage = new Message();
			
			infoMessage.setLong("beshare:File Size", ((SharedFileInfoHolder)fileListing.elementAt(x)).getSize());
			//infoMessage.SetInt("beshare:Modification Time", 0);  // Java Dosen't support modification or creation dates on File objects
			infoMessage.setString("beshare:Path", ((SharedFileInfoHolder)fileListing.elementAt(x)).getPath());
			infoMessage.setString("beshare:Kind", ((SharedFileInfoHolder)fileListing.elementAt(x)).getKind());
			
			String filePath = "";
			if (getFirewalled()) {
				filePath += "beshare/fires/";
			} else {
				filePath += "beshare/files/";
			}
			filePath += ((SharedFileInfoHolder)fileListing.elementAt(x)).getName();
			uploadMessage.setMessage(filePath, infoMessage);

			if (x % 50 == 0) {
				// Once we hit 50 files in a message, we want to send it, create a new one, and continue.
				beShareTransceiver.sendOutgoingMessage(uploadMessage);
				uploadMessage = new Message(PR_COMMAND_SETDATA);
			}
		}
		beShareTransceiver.sendOutgoingMessage(uploadMessage);
		System.out.println("All Files uploaded!");
	}
	
	/**
	 * Removes the File list nodes from the server.
	 */
	public void removeFileListing(){
		Message removeNodes = new Message(PR_COMMAND_REMOVEDATA);
		removeNodes.setString(PR_NAME_KEYS, "beshare/fi*es");
		beShareTransceiver.sendOutgoingMessage(removeNodes);
	}

	/**
	 *	Uploads a command message to the remote DB
	 *	@param nodePath the node path in the database to update/set
	 *	@param nodeValue the message containing all values to store
	 */
	private void setDataNodeValue(String nodePath, Message nodeValue){
		Message uploadMessage = new Message(PR_COMMAND_SETDATA);
		uploadMessage.setMessage(nodePath, nodeValue);
		beShareTransceiver.sendOutgoingMessage(uploadMessage);
	}

	/**
	 *	This is our Message listener to the MUSCLE Server side of things.
	 *	From here we determine what to do with the incomming messages,
	 *	and post them to the object specified by
	 *	<code>setTarget()</code> if necessary.
	 *	@param message The Object being passed as a message
	 *	@param numleft The number of messages left in the queue
	 */
	public synchronized void messageReceived(Object message, int numleft){
		if (message == serverConnect){
			connected = true;
			// If this happens while an AutoReconect is active, kill the autoRecon.
			if (autoRecon != null) {
				autoRecon.interrupt();
				autoRecon = null;
			}
			fireJavaShareEvent(new JavaShareEvent(this,
					JavaShareEvent.SERVER_CONNECTED));
		} else if (message == serverDisconnect){
			if (autoRecon == null) {
				autoRecon = new AutoReconnector();
				disconnect(); // resets variables, sends message, etc.
				autoRecon.start();
			}
		} else if (message instanceof Message){
			try{
				recentActivity = true;
				//muscleMessageReceived((Message)message);
				muscleMessageHandler((Message)message);
			} catch (Exception ex) {
				System.out.println(ex.toString());
				ex.printStackTrace(System.out);
				System.out.println("Message Error Occured with:\n" + message.toString());
				// Try to pass the message on and see what happens.
				fireJavaShareEvent(new JavaShareEvent(message,
						JavaShareEvent.UNKNOWN_MUSCLE_MESSAGE));
			}
		}
	}
	
	/**
	 *	Handles all MUSCLE Messages. Translates incomming messages into
	 *	JavaShareEvents and fires them off to the <code>ActionListener</code> as
	 *	set by <code>setActionListener</code>
	 */
	public void muscleMessageHandler(Message message) throws Exception {
		switch (message.what) {
			// Someone pinged us, let's pong them back!
			case NET_CLIENT_PING: {
				message.what = NET_CLIENT_PONG;
				message.setString(PR_NAME_KEYS, "/*/" + message.getString("session") + "/beshare");
				message.setString("session", localSessionID);
				message.setString("version", MUSCLE_INTERFACE_VERSION);
				beShareTransceiver.sendOutgoingMessage(message);
			}
			break;
			
			// New chat text!
			case NET_CLIENT_NEW_CHAT_TEXT: {
				fireJavaShareEvent(
					new JavaShareEvent(
						new ChatMessage(message.getString("session"),
							message.getString("text"), message.hasField("private"),
							ChatMessage.LOG_REMOTE_USER_CHAT_MESSAGE),
						JavaShareEvent.CHAT_MESSAGE));
			}
			break;
			
			// We just got a ping we sent back
			case NET_CLIENT_PONG: {
				long when = message.getLong("when");
				when = System.currentTimeMillis() - when;
				String pingMessage = "Ping returned in " + when + " milliseconds";
				if (message.hasField("version")){
					pingMessage += " (" + message.getString("version") + ")";
				}
				fireJavaShareEvent(
					new JavaShareEvent(
						new ChatMessage(message.getString("session"), pingMessage,
							true, ChatMessage.LOG_REMOTE_USER_CHAT_MESSAGE),
						JavaShareEvent.CHAT_MESSAGE));
			}
			break;
			
			// The Server sent us some informational parameters
			// We have to ask for these -- During the connect we ask for our session root so that we can
			// find out what our session ID is.
			case PR_RESULT_PARAMETERS: {
				// Did we ask for it?
				if (requestedServerInfo) {
					processServerInfo(message);
				} else {
					String sessionRoot = message.getString(PR_NAME_SESSION_ROOT);
					localSessionID = sessionRoot.substring(sessionRoot.lastIndexOf('/') + 1);
				}
			}
			break;
			
			// Dataitems have changed for a query we still have open.
			// There's basically two types of querys for BeShare, Users and Files.
			case PR_RESULT_DATAITEMS: {
				// Look for any removed items...
				if (message.hasField(PR_NAME_REMOVED_DATAITEMS)) {
					String removedNodes[] = message.getStrings(PR_NAME_REMOVED_DATAITEMS);
					for (int x = 0; x < removedNodes.length; x++) {
						if ((getPathDepth(removedNodes[x]) == USER_NAME_DEPTH) 
							&& lastNodeElement(removedNodes[x]).equals("name"))
						{
							// The User was removed
							fireJavaShareEvent(
								new JavaShareEvent(this, JavaShareEvent.USER_DISCONNECTED,
									new BeShareUser(sessionIDFromNode(removedNodes[x])), getServerName()));
						} else if (getPathDepth(removedNodes[x]) == FILE_INFO_DEPTH) {
							// Files were removed
							SharedFileInfoHolder holder = new SharedFileInfoHolder();
							if (removedNodes[x].indexOf("files/") != -1)
								holder.setName(lastNodeElement(removedNodes[x], "files/"));
							else
								holder.setName(lastNodeElement(removedNodes[x], "fires/"));
							holder.setSessionID(sessionIDFromNode(removedNodes[x]));
							fireJavaShareEvent(
								new JavaShareEvent(holder, JavaShareEvent.FILE_INFO_REMOVE_RESULTS));
						}
					} // next removed Node
				} // done with removal
				
				Iterator e = message.fieldNames();
				while (e.hasNext()) {
					String fieldName = (String)e.next();
					// Has a user changed?
					if (message.getFieldTypeCode(fieldName) == B_MESSAGE_TYPE) {
						if (getPathDepth(fieldName) == USER_NAME_DEPTH)	{
							String updatedNode = lastNodeElement(fieldName);
							Message userNameInfos[] = message.getMessages(fieldName);
							Message userNameNode = userNameInfos[userNameInfos.length - 1];
							
							// Construct the User object that's changed....
							BeShareUser user = new BeShareUser(sessionIDFromNode(fieldName));
							user.setIPAddress(ipFromNode(fieldName));
							if (userNameNode.hasField("name", B_STRING_TYPE))
								user.setName(userNameNode.getString("name"));
							if (userNameNode.hasField("installid", B_INT64_TYPE))
								user.setInstallID(userNameNode.getLong("installid"));
							if (userNameNode.hasField("port", B_INT32_TYPE)) {
								System.out.println(fieldName + " Port: " + userNameNode.getInt("port"));
								user.setPort(userNameNode.getInt("port"));
							}
							if (userNameNode.hasField("bot", B_BOOL_TYPE))
								user.setBot(userNameNode.getBoolean("bot"));
							if (userNameNode.hasField("version_name", B_STRING_TYPE) && 
								userNameNode.hasField("version_num", B_STRING_TYPE))
							{
								user.setClient(userNameNode.getString("version_name") + " " +
											userNameNode.getString("version_num"));
							}
							if (userNameNode.hasField("userstatus", B_STRING_TYPE))
								user.setStatus(userNameNode.getString("userstatus"));
							if (userNameNode.hasField("cur", B_INT32_TYPE))
								user.setUploadCurrent(userNameNode.getInt("cur"));
							if (userNameNode.hasField("max", B_INT32_TYPE))
								user.setUploadMax(userNameNode.getInt("max"));
							if (userNameNode.hasField("label", B_STRING_TYPE))
								user.setBandwidthLabel(userNameNode.getString("label"));
							if (userNameNode.hasField("bps", B_INT32_TYPE))
								user.setBandwidthBps(userNameNode.getInt("bps"));
							if (userNameNode.hasField("fires"))
								user.setFirewall(true);
							if (userNameNode.hasField("files"))
								user.setFirewall(false);
							if (userNameNode.hasField("filecount", B_INT32_TYPE))
								user.setFileCount(userNameNode.getInt("filecount"));
							if (updatedNode.equals("name"))
								fireJavaShareEvent(
									new JavaShareEvent(this, JavaShareEvent.USER_CONNECTED, user, getServerName()));
							else if (updatedNode.equals("userstatus"))
								fireJavaShareEvent(
									new JavaShareEvent(this, JavaShareEvent.USER_STATUS_CHANGE, user, getServerName()));
							else if (updatedNode.equals("uploadstats"))
								fireJavaShareEvent(
									new JavaShareEvent(this, JavaShareEvent.USER_UPLOAD_STATS_CHANGE, user, getServerName()));
							else if (updatedNode.equals("bandwidth"))
								fireJavaShareEvent(
									new JavaShareEvent(this, JavaShareEvent.USER_BANDWIDTH_CHANGE, user, getServerName()));
							else if (updatedNode.equals("fires"))
								fireJavaShareEvent(
									new JavaShareEvent(this, JavaShareEvent.USER_FIREWALL_CHANGE, user, getServerName()));
							else if (updatedNode.equals("files"))
								fireJavaShareEvent(
									new JavaShareEvent(this, JavaShareEvent.USER_FIREWALL_CHANGE, user, getServerName()));
							else if (updatedNode.equals("filecount"))
								fireJavaShareEvent(
									new JavaShareEvent(this, JavaShareEvent.USER_FILE_COUNT_CHANGE, user, getServerName()));
						} else if (getPathDepth(fieldName) == FILE_INFO_DEPTH && queryActive) {
							Message fileInfo = message.getMessage(fieldName);
							SharedFileInfoHolder holder = new SharedFileInfoHolder();
							holder.setSessionID(sessionIDFromNode(fieldName));
							
							if (fileInfo.hasField("beshare:File Size"))
								holder.setSize(fileInfo.getLong("beshare:File Size"));
							if (fileInfo.hasField("beshare:Path"))
								holder.setPath(fileInfo.getString("beshare:Path"));
							if (fileInfo.hasField("beshare:Kind"))
								holder.setKind(fileInfo.getString("beshare:Kind"));
							if (fieldName.indexOf("files/") != -1)
								holder.setName(lastNodeElement(fieldName, "files/"));
							else
								holder.setName(lastNodeElement(fieldName, "fires/"));
							
							Thread.yield(); // Force the query to play nice.
							
							fireJavaShareEvent(
								new JavaShareEvent(holder, JavaShareEvent.FILE_INFO_ADD_TO_RESULTS));
						} // FILE_INFO_DEPTH
					} // B_MESSAGE_TYPE
				} // Next field name
			}
			break;
		}
	}
	
	/**
	 * @return the name of the last node element.
	 */
	private final String lastNodeElement(String node) {
		return node.substring(node.lastIndexOf('/') + 1);
	}
	
	/**
	 * @param node the full path to the node containing the data
	 * @param element the name of the node containing the data. ex. "/files"
	 * @return the contents of the last node element
	 */
	private final String lastNodeElement(String node, String element) {
		return node.substring(node.lastIndexOf(element) + element.length(), node.length());
	}
	
	/**
	 * @param node a node to get the session ID from.
	 * @return the session ID where this node originates from
	 */
	private final String sessionIDFromNode(String node) {
		int start = node.indexOf('/', 1) + 1;
		return node.substring(start, node.indexOf('/', start));
	}
	
	/**
	 * @param node a node to get the IPAddress from
	 * @return the IP address of the originating host.
	 */
	private final String ipFromNode(String node) {
		return node.substring(1, node.indexOf('/', 1));
	}
	
	/**
	 * Taks a Server Information message and sends the information out to the JavaShare Listeners
	 * by calling logInformation();
	 */
	protected final void processServerInfo(Message message) throws Exception {
		requestedServerInfo = false;
		logInformation("Server status requested.");
		if (message.hasField(PR_NAME_SERVER_VERSION))
			logInformation("Server version:  " + message.getString(PR_NAME_SERVER_VERSION));
		if (message.hasField(PR_NAME_SERVER_UPTIME)) {
			long seconds = message.getLong(PR_NAME_SERVER_UPTIME) / 1000000;
			long minutes = seconds / 60;  seconds = seconds % 60;
			long hours   = minutes / 60;  minutes = minutes % 60;
			long days    = hours   / 24;  hours   = hours   % 24;
			long weeks   = days    /  7;  days    = days    % 7;
			logInformation("Server uptime:   " + weeks + " weeks, " + days + " days, " + hours + 
							":" + minutes + ":" + seconds);
		}
		if (message.hasField(PR_NAME_SESSION_ROOT))
			logInformation("Local Session Root:    " + message.getString(PR_NAME_SESSION_ROOT));
		if (message.hasField(PR_NAME_SERVER_MEM_AVAILABLE) && message.hasField(PR_NAME_SERVER_MEM_USED)) {
			final float oneMeg = 1024.0f * 1024.0f;
			float memAvailableMB = ((float)message.getLong(PR_NAME_SERVER_MEM_AVAILABLE)) / oneMeg;
			float memUsedMB      = ((float)message.getLong(PR_NAME_SERVER_MEM_USED))     / oneMeg;
			logInformation("Server memory usage:    " + memUsedMB + "MB used (" + 
							memAvailableMB + "MB available)");
		}
	}
	
	/**
	 * Sends <code>message</code> as a System information chat message
	 *
	 * @param message - The Text to display as an informational message.
	 */
	protected final void logInformation(String message) {
		fireJavaShareEvent(
			new JavaShareEvent(
				new ChatMessage("",
					message, false,	ChatMessage.LOG_INFORMATION_MESSAGE),
				JavaShareEvent.CHAT_MESSAGE));
	}
	
	/**
	 *	returns the depth in the path the last node is
	 *	@param path The path to parse for it's depth.
	 */
	private int getPathDepth(String path){
		if (path.equals("/")) return 0;
		int depth = 0;
		for (int i = path.length()-1; i >= 0; i--)
			if (path.charAt(i) == '/') depth++;
		return depth;
	}
	
	/** @return the Timeout setting in milliseconds. */
	private long getTimeout() {
		return connectionTimeout * 1000;
	}
	
	/** Sets the timeout setting. @param seconds Consider it a timeout if inactive for this many seconds. */
	private void setTimeout(int seconds) {
		connectionTimeout = seconds;
	}
	
	/**
	 * Thread to test the connection activity and re-connect to the MUSCLE server if need be.
	 */
	private class ConnectionCheck extends Thread {
		/**
		 * Creates a new Connection Checking daemon thread.
		 * This thread runs as a Daemon, and will check the connection every if no
		 * muscle activity has taken place for getTimeout() seconds. If a muscle message
		 * is received, no action is taken.
		 */
		public ConnectionCheck() {
			setDaemon(true);
			setName("MUSCLE_Activity_Monitor");
		}
		
		/**
		 * Runs the thread connection test
		 */
		public void run() {
			while(true) {
				if (isConnected()) {
					if (!recentActivity) { // We send a check.
						beShareTransceiver.sendOutgoingMessage(new Message(PR_COMMAND_NOOP));
					}
				}
				try {
					sleep(getTimeout()); // 2.5 minutes
				} catch (InterruptedException ie) {
					// What, you think I care about this exception?!
				}
			}
		}
	}
	
	/**
	 * Performs the time-delayed auto-reconnect.
	 */
	private class AutoReconnector extends Thread {
		private int waitTime = 0;
		
		/**
		 * Creates a new AutoReconnect thread.
		 */
		public AutoReconnector() {
			setDaemon(true);
			setName("Auto_Reconnect");
		}
		
		/**
		 * Called by the threads start() method.
		 */
		public void run() {
			while (true) {
				if (isConnected()) {
					// If we're connected, bail out!
					return;
				} else {
					switch (waitTime) {
						case 0:
							// Reconnecting now message
							fireJavaShareEvent(new JavaShareEvent(
								new ChatMessage("",
									"Reconnecting...",
									false,
									ChatMessage.LOG_INFORMATION_MESSAGE),
									JavaShareEvent.CHAT_MESSAGE)
							);
							break;
						case 30:
							// Seconds Message
							fireJavaShareEvent(new JavaShareEvent(
								new ChatMessage("",
									"Reconnecting in " + waitTime + " seconds...",
									false,
									ChatMessage.LOG_INFORMATION_MESSAGE),
									JavaShareEvent.CHAT_MESSAGE)
							);
							break;
						case 60:
							// Minutes message
							fireJavaShareEvent(new JavaShareEvent(
								new ChatMessage("",
									"Reconnecting in " + (waitTime / 60) + " minute...",
									false,
									ChatMessage.LOG_INFORMATION_MESSAGE),
									JavaShareEvent.CHAT_MESSAGE)
							);
							break;
						default:
							// Minutes message
							fireJavaShareEvent(new JavaShareEvent(
								new ChatMessage("",
									"Reconnecting in " + (waitTime / 60) + " minutes...",
									false,
									ChatMessage.LOG_INFORMATION_MESSAGE),
									JavaShareEvent.CHAT_MESSAGE)
							);
					}
					try {
						sleep(waitTime * 1000);
						if (waitTime == 0)
							waitTime = 30;
						else
							waitTime *= 2;
						connect();
					} catch (InterruptedException ie) {
						// Who cares?
					}
				}
			}
		}
		
	}
}
