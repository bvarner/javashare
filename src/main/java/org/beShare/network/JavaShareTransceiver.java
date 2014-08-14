package org.beShare.network;

import com.meyer.muscle.client.MessageTransceiver;
import com.meyer.muscle.client.StorageReflectConstants;
import com.meyer.muscle.message.Message;
import com.meyer.muscle.support.TypeConstants;
import com.meyer.muscle.thread.MessageListener;
import com.meyer.muscle.thread.MessageQueue;
import com.meyer.muscle.thread.ThreadPool;
import org.beShare.Application;
import org.beShare.data.BeShareUser;
import org.beShare.data.SharedFile;
import org.beShare.data.UserDataModel;
import org.beShare.gui.ChatDocument;
import org.beShare.gui.text.StyledString;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.prefs.Preferences;

/**
 * JavaShareTransceiver - This class handles all incomming/outgoing Muscle messages
 * and then sends the appropriate JavaShareEvents to any registered listener.
 *
 * @author Bryan Varner
 */
public class JavaShareTransceiver implements MessageListener, StorageReflectConstants, TypeConstants {
	public static final int portRange = 50;
	public static final int startingPortNumber = 7000;
	private int localUserPort = startingPortNumber;
	private static final int ROOT_DEPTH = 0;    // root node
	private static final int HOST_NAME_DEPTH = 1;
	private static final int SESSION_ID_DEPTH = 2;
	private static final int BESHARE_HOME_DEPTH = 3;
	// used to separate our stuff from other (non-BeShare) data on the same server
	private static final int USER_NAME_DEPTH = 4;    // user's handle node would be found here
	private static final int FILE_INFO_DEPTH = 5;    //This is where file names are
	private static final int NET_CLIENT_NEW_CHAT_TEXT = 2;
	private static final int NET_CLIENT_CONNECT_BACK_REQUEST = 3;
	private static final int NET_CLIENT_PING = 5;
	private static final int NET_CLIENT_PONG = 6;
	public final String MUSCLE_INTERFACE_VERSION = "JavaShare " + Application.BUILD_VERSION;
	private final Object serverConnect = new Object();
	private final Object serverDisconnect = new Object();
	// Connection Management
	private String serverName = "";
	private int serverPort = 2960;
	private MessageTransceiver beShareTransceiver = new MessageTransceiver(new MessageQueue(this));
	private String localSessionID = "";
	private String localUserName;
	private String localUserStatus;
	private long localUserInstallId;
	private List<ChatDocument> chatDocuments = new ArrayList<>();
	private boolean requestedServerInfo = false;
	private boolean connectInProgress = false;
	private boolean connected = false;
	private boolean firewalled = false;
	private int pingCount = 0;
	private boolean queryActive = false;
	private long lastEvent = System.currentTimeMillis();
	private boolean disconnectExpected = false;
	private int reconnectBackoff = -1;
	private int connectionTimeout = 300; // in seconds = 5 Minutes
	private UserDataModel userDataModel = new UserDataModel();

	/**
	 * Construct a new JavaShareTransceiver,
	 */
	public JavaShareTransceiver(final Preferences preferences) {
		ThreadPool.getDefaultThreadPool().startThread(new ConnectionCheck());
	}

	/**
	 * Provides access to the global UserDataModel for the main JavaShare Conneciton.
	 *
	 * @return The UserDataModel in use by this Transceiver.
	 */
	public UserDataModel getUserDataModel() {
		return userDataModel;
	}

	/**
	 * Get the name of the server we're connecting to.
	 *
	 * @return the Name of the server to connect to.
	 */
	public String getServerName() {
		return serverName;
	}

	/**
	 * Sets the name of the server to connect to.
	 *
	 * @param sName - the Name of the server to connect to.
	 */
	public void setServerName(final String sName) {
		if (!sName.equalsIgnoreCase(this.serverName)) {
			this.serverName = sName;
			if (connected || connectInProgress) {
				connect();
			}
		}
	}

	/**
	 * Gets the id of the local user.
	 *
	 * @return String the local SessionID of this user.
	 */
	public String getLocalSessionID() {
		return localSessionID;
	}

	/**
	 * Returns the port number to connect with.
	 *
	 * @return the port to connect with.
	 */
	public int getServerPort() {
		return serverPort;
	}

	/**
	 * Sets the port number to connect with.
	 *
	 * @param sPort - the port to connect with
	 */
	public void setServerPort(int sPort) {
		if (this.serverPort != sPort) {
			this.serverPort = sPort;
			if (connected || connectInProgress) {
				connect();
			}
		}
	}

	public void addChatDocument(final ChatDocument chatDocument) {
		chatDocuments.add(chatDocument);
	}

	public void removeChatDocument(final ChatDocument chatDocument) {
		chatDocuments.remove(chatDocument);
	}

	/**
	 * Starts a new query. This will also stop any pending queries, and send a
	 * clear query list message.
	 */
	public void startQuery(String sessionExpression, String fileExpression) {
		stopQuery(); // If you can't figure out why we would do this... <sigh>
		queryActive = true;

		// This path string tells muscled which files it should inform us about.
		// Since we may be basing part of our query on the session ID, we use
		// the full path string for this query.
		String temp = "SUBSCRIBE:/*/";
		temp += sessionExpression;
		temp += "/beshare/";
		temp +=
				getFirewalled() ? "files/" : "fi*/";  // If we're firewalled, we can only get non-firewalled files; else both types
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
	}

	/**
	 * Stops the current Query
	 */
	public void stopQuery() {
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
	 * Sends a message to the host at targetSessionID requesting they connect
	 * to us on <code>port</code> so we can transfer a file from them.
	 */
	public void sendConnectBackRequestMessage(String targetSessionID, int port) {
		System.out.println("JavaShareTransceiver: Sending Connectback request");

		Message cbackMsg = new Message(NET_CLIENT_CONNECT_BACK_REQUEST);
		String target = "/*/" + targetSessionID + "/beshare";
		cbackMsg.setString(PR_NAME_KEYS, target);
		cbackMsg.setString("session", getLocalSessionID());
		cbackMsg.setInt("port", port);
		beShareTransceiver.sendOutgoingMessage(cbackMsg);
		System.out.println("JavaShareTransceiver: Connectback request Sent.");
	}

	/**
	 * Returns the current stats of the firewall setting.
	 */
	public boolean getFirewalled() {
		return firewalled;
	}

	/**
	 * Sets weather or not we should behave as if we are firewalled.
	 * If we aren't, peachy! If we are, who gives a crap?!
	 */
	public void setFirewalled(boolean firewall) {
		firewalled = firewall;
	}

	/**
	 * Connects to MUSCLE server.
	 */
	public synchronized void connect() {
		disconnectExpected = false;
		connectInProgress = true;
		connected = false;

		beShareTransceiver.disconnect();
		userDataModel.clear();

		logInformation("Connecting to: " + serverName);
		beShareTransceiver.connect(serverName, serverPort, serverConnect, serverDisconnect);
	}

	/**
	 * Force a disconnect from the current server.
	 */
	public synchronized void disconnect() {
		disconnectExpected = true;
		beShareTransceiver.disconnect();
		userDataModel.clear();
	}

	/**
	 * Returns connection status
	 *
	 * @return true if connected, false if not connected.
	 * @deprecated Please remove this method, and get the shared file list maintenance added refactored...
	 */
	public synchronized boolean isConnected() {
		return connected;
	}

	/**
	 * Sends a message subscribing to the BeShare nodes.
	 */
	private void beShareSubscribe() {
		beShareTransceiver.sendOutgoingMessage(
				                                      new Message(PR_COMMAND_GETPARAMETERS));
		// set up a subscription to the beShare tree.
		Message queryMsg = new Message(PR_COMMAND_SETPARAMETERS);
		queryMsg.setBoolean("SUBSCRIBE:beshare/*", true);
		beShareTransceiver.sendOutgoingMessage(queryMsg);
	}

	/**
	 * Sends <code>text</code> to user with session of <code>session</code>
	 *
	 * @param text    The String to send
	 * @param chatDoc The document to update with the content you've sent.
	 */
	public void sendChat(final String text, final ChatDocument chatDoc) {
		String[] sessions = {"*"};
		if (chatDoc.getFilteredUserDataModel().isFiltering()) {
			sessions = chatDoc.getFilteredUserDataModel().getSessionIds().toArray(new String[0]);
		}

		Message chatMessage = new Message(NET_CLIENT_NEW_CHAT_TEXT);
		for (String session : sessions) {
			chatMessage.setString(PR_NAME_KEYS, "/*/(" + session + ")/beshare");
			chatMessage.setString("session", localSessionID);
			chatMessage.setString("text", text);
			if (!session.equals("*")) {
				chatMessage.setBoolean("private", true);
			} else {
				chatMessage.removeField("private");
			}
			beShareTransceiver.sendOutgoingMessage(chatMessage);
		}

		// Post back what you sent to the document you're working with.
		chatDoc.addEchoChatMessage(text, chatMessage.hasField("private"), localSessionID, localUserName);
	}

	/**
	 * Handles text commands, echoing output to the given chatDoc.
	 *
	 * @param command
	 * @param chatDoc
	 */
	public void command(final String command, final ChatDocument chatDoc) {
		String lowerCommand = command.toLowerCase().trim();
		if (lowerCommand.startsWith("/me ")) {
			sendChat(command, chatDoc);
		} else if (lowerCommand.startsWith("/priv")) {
			// TODO: Implement me.
			System.out.println("New Private Frame to [" + command.substring(5).trim() + "]");
		} else if (lowerCommand.equalsIgnoreCase("/connect")) {
			connect();
		} else if (lowerCommand.equalsIgnoreCase("/disconnect")) {
			disconnect();
		} else if (lowerCommand.equalsIgnoreCase("/clear")) {
			chatDoc.clear();
		}
	}

	/**
	 * Sends a Ping to the user with the specified session id.
	 *
	 * @param session The SessionID of the user to ping.
	 */
	public void pingUser(String session) {
		Message pingMessage = new Message(NET_CLIENT_PING);
		pingMessage.setString(PR_NAME_KEYS, "/*/" + session + "/beshare");
		pingMessage.setLong("when", System.currentTimeMillis());
		pingMessage.setString("session", localSessionID);
		beShareTransceiver.sendOutgoingMessage(pingMessage);
	}

	/**
	 * Sends a <code>PR_COMMAND_GETPARAMETERS</code> to the server, and sets
	 * the <code>requestedServerInfo</code> field. This is used when a the user
	 * requests system Information.
	 */
	public void getServerInfo() {
		Message infoMessage = new Message(PR_COMMAND_GETPARAMETERS);
		beShareTransceiver.sendOutgoingMessage(infoMessage);
		requestedServerInfo = true;
	}

	/**
	 * Uploads UserName information to the MUSCLE server
	 *
	 * @param uName     The Users handle
	 * @param port      the port they use for filesharing.
	 * @param installid the unique identifier for this install.
	 */
	private void setLocalUserName(final String uName, final int port, final long installid) {
		if (this.localUserName != null) {
			StyledString.KEYWORD_STYLES.remove(".*" + this.localUserName + ".*");
		}

		this.localUserName = uName;
		this.localUserPort = port;
		this.localUserInstallId = installid;

		if (connected) {
			Message nameMessage = new Message();
			nameMessage.setString("name", localUserName);
			nameMessage.setInt("port", this.localUserPort);
			nameMessage.setLong("installid", this.localUserInstallId);
			nameMessage.setString("version_name", "JavaShare");
			nameMessage.setString("version_num", "v" + Application.BUILD_VERSION);
			setDataNodeValue("beshare/name", nameMessage);
		}

		logInformation("Your name has been changed to " + localUserName);
		StyledString.KEYWORD_STYLES.put(".*" + localUserName + ".*", StyledString.USER_MENTIONED);
	}

	public String getLocalUserName() {
		return this.localUserName;
	}

	public void setLocalUserName(final String name) {
		setLocalUserName(name, this.localUserPort, this.localUserInstallId);
	}

	/**
	 * Uploads Bandwidth information to the MUSCLE server
	 *
	 * @param lbl String label 'T1', 'Cable', ...
	 * @param bps The speed of the connection.
	 */
	public void setUploadBandwidth(String lbl, int bps) {
		if (connected) {
			Message bwMessage = new Message();
			bwMessage.setString("label", lbl);
			bwMessage.setInt("bps", bps);
			setDataNodeValue("beshare/bandwidth", bwMessage);
		}
	}

	public String getLocalUserStatus() {
		return this.localUserStatus;
	}

	/**
	 * Uploads the local user status to the MUSCLE server
	 *
	 * @param uStatus The Users current status
	 */
	public void setLocalUserStatus(final String uStatus) {
		this.localUserStatus = uStatus;

		if (connected) {
			Message statusMessage = new Message();
			statusMessage.setString("userstatus", this.localUserStatus);
			setDataNodeValue("beshare/userstatus", statusMessage);
		}
		logInformation("Your status has been changed to " + getLocalUserStatus());
	}

	/**
	 * Uploads the file-listing to the MUSCLE server. This is a
	 * tricky area, since BeShare's source is a bit less than
	 * easy for me to read.
	 */
	public void uploadFileListing(Vector fileListing) {
		Message sizeMessage = new Message();
		sizeMessage.setInt("filecount", fileListing.size());
		setDataNodeValue("beshare/filecount", sizeMessage);
		System.out.println("Send File count message. Files Listed: " + fileListing.size());

		// From here we upload the actual list of files.
		Message uploadMessage = new Message(PR_COMMAND_SETDATA);
		for (int x = 0; x < fileListing.size(); x++) {
			// Get the file-specific data into a sub-message.
			Message infoMessage = new Message();

			infoMessage.setLong("beshare:File Size", ((SharedFile) fileListing.elementAt(x)).getSize());
			//infoMessage.SetInt("beshare:Modification Time", 0);  // Java Dosen't support modification or creation dates on File objects
			infoMessage.setString("beshare:Path", ((SharedFile) fileListing.elementAt(x)).getPath());
			infoMessage.setString("beshare:Kind", ((SharedFile) fileListing.elementAt(x)).getKind());

			String filePath = "";
			if (getFirewalled()) {
				filePath += "beshare/fires/";
			} else {
				filePath += "beshare/files/";
			}
			filePath += ((SharedFile) fileListing.elementAt(x)).getName();
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
	public void removeFileListing() {
		Message removeNodes = new Message(PR_COMMAND_REMOVEDATA);
		removeNodes.setString(PR_NAME_KEYS, "beshare/fi*es");
		beShareTransceiver.sendOutgoingMessage(removeNodes);
	}

	/**
	 * Uploads a command message to the remote DB
	 *
	 * @param nodePath  the node path in the database to update/set
	 * @param nodeValue the message containing all values to store
	 */
	private void setDataNodeValue(String nodePath, Message nodeValue) {
		Message uploadMessage = new Message(PR_COMMAND_SETDATA);
		uploadMessage.setMessage(nodePath, nodeValue);
		beShareTransceiver.sendOutgoingMessage(uploadMessage);
	}

	/**
	 * This is our Message listener to the MUSCLE Server side of things.
	 * From here we determine what to do with the incomming messages,
	 * and post them to the object specified by
	 * <code>setTarget()</code> if necessary.
	 *
	 * @param message The Object being passed as a message
	 * @param numleft The number of messages left in the queue
	 */
	@Override
	public synchronized void messageReceived(Object message, int numleft) {
		if (message == serverConnect) {
			connected = true;
			connectInProgress = false;
			reconnectBackoff = -1;

			// Send our current User information to the server.
			beShareSubscribe();
			setLocalUserName(localUserName);
			setLocalUserStatus(localUserStatus);
			setUploadBandwidth("?", 0);

			// TODO: Execute any 'onlogin' commands.

			// TODO: Update the list of files you share.
		} else if (message == serverDisconnect) {
			connected = false;
			connectInProgress = false;
			if (!disconnectExpected && reconnectBackoff < 0) {
				logInformation("Disconnected from: " + serverName);
				reconnectBackoff = 0;
				ThreadPool.getDefaultThreadPool().startThread(new AutoReconnector());
			} else {
				logError("Connection failed to: " + serverName);
			}
		} else if (message instanceof Message) {
			try {
				lastEvent = System.currentTimeMillis();
				muscleMessageHandler((Message) message);
			} catch (Exception ex) {
				System.out.println(ex.toString());
				ex.printStackTrace(System.out);
				System.out.println("Message Error Occured with:\n" + message.toString());
			}
		}
	}

	/**
	 * Handles all MUSCLE Messages. Translates incomming messages into
	 * JavaShareEvents and fires them off to the <code>ActionListener</code> as
	 * set by <code>setActionListener</code>
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
				for (ChatDocument doc : chatDocuments) {
					doc.addRemoteChatMessage(message.getString("text"), message.getString("session"), message.hasField("private"));
				}
			}
			break;

			// We just got a ping we sent back
			case NET_CLIENT_PONG: {
				long when = message.getLong("when");
				when = System.currentTimeMillis() - when;
				String pingMessage = "Ping returned in " + when + " milliseconds";
				if (message.hasField("version")) {
					pingMessage += " (" + message.getString("version") + ")";
				}

				for (ChatDocument doc : chatDocuments) {
					doc.addRemoteChatMessage(pingMessage, message.getString("session"), true);
				}
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
								    && lastNodeElement(removedNodes[x]).equals("name")) {
							// The User was removed
							BeShareUser removed = new BeShareUser(sessionIDFromNode(removedNodes[x]));
							userDataModel.removeUser(removed);
							logInformation("User #" + removed.getSessionID() + " (a.k.a. " + removed.getName() + ") has disconnected.");
						} else if (getPathDepth(removedNodes[x]) == FILE_INFO_DEPTH) {
							// Files were removed
							SharedFile holder = new SharedFile();
							if (removedNodes[x].indexOf("files/") != -1) {
								holder.setName(lastNodeElement(removedNodes[x], "files/"));
							} else {
								holder.setName(lastNodeElement(removedNodes[x], "fires/"));
							}
							holder.setSessionID(sessionIDFromNode(removedNodes[x]));
							// transferPanel.removeResult(holder);
						}
					} // next removed Node
				} // done with removal

				Iterator e = message.fieldNames();
				while (e.hasNext()) {
					String fieldName = (String) e.next();
					// Has a user changed?
					if (message.getFieldTypeCode(fieldName) == B_MESSAGE_TYPE) {
						if (getPathDepth(fieldName) == USER_NAME_DEPTH) {
							String updatedNode = lastNodeElement(fieldName);
							Message userNameInfos[] = message.getMessages(fieldName);
							Message userNameNode = userNameInfos[userNameInfos.length - 1];

							// Construct the User object that's changed....
							String sessionId = sessionIDFromNode(fieldName);
							BeShareUser user = userDataModel.getUser(sessionId);
							if (user == null) {
								logInformation("User #" + sessionId + " is now connected.");
								user = new BeShareUser(sessionId);
							}
							user.setIPAddress(ipFromNode(fieldName));
							if (userNameNode.hasField("name", B_STRING_TYPE)) {
								user.setName(userNameNode.getString("name"));
							}
							if (userNameNode.hasField("installid", B_INT64_TYPE)) {
								user.setInstallID(userNameNode.getLong("installid"));
							}
							if (userNameNode.hasField("port", B_INT32_TYPE)) {
								System.out.println(fieldName + " Port: " + userNameNode.getInt("port"));
								user.setPort(userNameNode.getInt("port"));
							}
							if (userNameNode.hasField("bot", B_BOOL_TYPE)) {
								user.setBot(userNameNode.getBoolean("bot"));
							}
							if (userNameNode.hasField("version_name", B_STRING_TYPE) &&
									    userNameNode.hasField("version_num", B_STRING_TYPE)) {
								user.setClient(userNameNode.getString("version_name") + " " +
										               userNameNode.getString("version_num"));
							}
							if (userNameNode.hasField("userstatus", B_STRING_TYPE)) {
								user.setStatus(userNameNode.getString("userstatus"));
							}
							if (userNameNode.hasField("cur", B_INT32_TYPE)) {
								user.setUploadCurrent(userNameNode.getInt("cur"));
							}
							if (userNameNode.hasField("max", B_INT32_TYPE)) {
								user.setUploadMax(userNameNode.getInt("max"));
							}
							if (userNameNode.hasField("label", B_STRING_TYPE)) {
								user.setBandwidthLabel(userNameNode.getString("label"));
							}
							if (userNameNode.hasField("bps", B_INT32_TYPE)) {
								user.setBandwidthBps(userNameNode.getInt("bps"));
							}
							if (userNameNode.hasField("fires")) {
								user.setFirewall(true);
							}
							if (userNameNode.hasField("files")) {
								user.setFirewall(false);
							}
							if (userNameNode.hasField("filecount", B_INT32_TYPE)) {
								user.setFileCount(userNameNode.getInt("filecount"));
							}
							userDataModel.updateUser(user);

							if (updatedNode.equals("name")) {
								logInformation("User #" + user.getSessionID() + " is now known as " + user.getName());
							} else if (updatedNode.equals("userstatus")) {
								logInformation("User #" + user.getSessionID() + " " +
										               "(a.k.a. " + user.getName() + ") " +
										               "is now " + user.getStatus() + ".");
							}
						} else if (getPathDepth(fieldName) == FILE_INFO_DEPTH && queryActive) {
							Message fileInfo = message.getMessage(fieldName);
							SharedFile holder = new SharedFile();
							holder.setSessionID(sessionIDFromNode(fieldName));

							if (fileInfo.hasField("beshare:File Size")) {
								holder.setSize(fileInfo.getLong("beshare:File Size"));
							}
							if (fileInfo.hasField("beshare:Path")) {
								holder.setPath(fileInfo.getString("beshare:Path"));
							}
							if (fileInfo.hasField("beshare:Kind")) {
								holder.setKind(fileInfo.getString("beshare:Kind"));
							}
							if (fieldName.indexOf("files/") != -1) {
								holder.setName(lastNodeElement(fieldName, "files/"));
							} else {
								holder.setName(lastNodeElement(fieldName, "fires/"));
							}

							Thread.yield(); // Force the query to play nice.

							// transferPanel.addResult(holder);
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
	 * @param node    the full path to the node containing the data
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
		if (message.hasField(PR_NAME_SERVER_VERSION)) {
			logInformation("Server version:  " + message.getString(PR_NAME_SERVER_VERSION));
		}
		if (message.hasField(PR_NAME_SERVER_UPTIME)) {
			long seconds = message.getLong(PR_NAME_SERVER_UPTIME) / 1000000;
			long minutes = seconds / 60;
			seconds = seconds % 60;
			long hours = minutes / 60;
			minutes = minutes % 60;
			long days = hours / 24;
			hours = hours % 24;
			long weeks = days / 7;
			days = days % 7;
			logInformation("Server uptime:   " + weeks + " weeks, " + days + " days, " + hours +
					               ":" + minutes + ":" + seconds);
		}
		if (message.hasField(PR_NAME_SESSION_ROOT)) {
			logInformation("Local Session Root:    " + message.getString(PR_NAME_SESSION_ROOT));
		}
		if (message.hasField(PR_NAME_SERVER_MEM_AVAILABLE) && message.hasField(PR_NAME_SERVER_MEM_USED)) {
			final float oneMeg = 1024.0f * 1024.0f;
			float memAvailableMB = ((float) message.getLong(PR_NAME_SERVER_MEM_AVAILABLE)) / oneMeg;
			float memUsedMB = ((float) message.getLong(PR_NAME_SERVER_MEM_USED)) / oneMeg;
			logInformation("Server memory usage:    " + memUsedMB + "MB used (" +
					               memAvailableMB + "MB available)");
		}
	}

	/**
	 * Sends <code>message</code> as a System information chat message
	 *
	 * @param message - The Text to display as an informational message.
	 */
	public final void logInformation(final String message) {
		for (ChatDocument doc : chatDocuments) {
			doc.addSystemMessage(message);
		}
	}

	public final void logError(final String message) {
		for (ChatDocument doc : chatDocuments) {
			doc.addErrorMessage(message);
		}
	}

	/**
	 * returns the depth in the path the last node is
	 *
	 * @param path The path to parse for it's depth.
	 */
	private int getPathDepth(String path) {
		if (path.equals("/")) {
			return 0;
		}
		int depth = 0;
		for (int i = path.length() - 1; i >= 0; i--) {
			if (path.charAt(i) == '/') {
				depth++;
			}
		}
		return depth;
	}

	/**
	 * Thread to test the connection activity and re-connect to the MUSCLE server if need be.
	 */
	private class ConnectionCheck implements Runnable {
		final static long timeoutMillis = 60 * 2500;

		/**
		 * Runs the thread connection test
		 */
		public void run() {
			while (true) {
				if (connected && System.currentTimeMillis() - lastEvent >= timeoutMillis) {
					beShareTransceiver.sendOutgoingMessage(new Message(PR_COMMAND_NOOP));
				}

				// Sleep 2.5 minutes.
				try {
					Thread.currentThread().sleep(timeoutMillis);
				} catch (InterruptedException ie) {
				}
			}
		}
	}

	/**
	 * Performs the time-delayed auto-reconnect.
	 */
	private class AutoReconnector implements Runnable {
		@Override
		public void run() {
			while (!connected && !connectInProgress && reconnectBackoff >= 0) {
				switch (reconnectBackoff) {
					case 0:
						logInformation("Reconnecting...");
						break;
					case 30:
						// Seconds Message
						logInformation("Reconnecting in " + reconnectBackoff + " seconds...");
						break;
					case 60:
						// Minutes message
						logInformation("Reconnecting in 1 minute...");
						break;
					default:
						// Minutes message
						logInformation("Reconnecting in " + (reconnectBackoff / 60) + " minutes...");
				}
				connect();
				try {
					Thread.currentThread().sleep(reconnectBackoff * 1000);
				} catch (InterruptedException ie) {
				}
				if (reconnectBackoff == 0) {
					reconnectBackoff = 30;
				} else {
					reconnectBackoff *= 2;
				}
			}
		}
	}
}
