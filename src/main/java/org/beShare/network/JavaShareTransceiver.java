package org.beShare.network;

import com.meyer.muscle.client.MessageTransceiver;
import com.meyer.muscle.message.Message;
import com.meyer.muscle.thread.MessageListener;
import com.meyer.muscle.thread.MessageQueue;
import com.meyer.muscle.thread.ThreadPool;
import org.beShare.Application;
import org.beShare.data.BeShareUser;
import org.beShare.data.SharedFile;
import org.beShare.data.UserDataModel;
import org.beShare.gui.AbstractDropMenuModel;
import org.beShare.gui.ChatDocument;
import org.beShare.gui.PrivateFrame;
import org.beShare.gui.text.StyledString;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import static com.meyer.muscle.client.StorageReflectConstants.PR_COMMAND_GETPARAMETERS;
import static com.meyer.muscle.client.StorageReflectConstants.PR_COMMAND_JETTISONRESULTS;
import static com.meyer.muscle.client.StorageReflectConstants.PR_COMMAND_NOOP;
import static com.meyer.muscle.client.StorageReflectConstants.PR_COMMAND_PING;
import static com.meyer.muscle.client.StorageReflectConstants.PR_COMMAND_REMOVEDATA;
import static com.meyer.muscle.client.StorageReflectConstants.PR_COMMAND_REMOVEPARAMETERS;
import static com.meyer.muscle.client.StorageReflectConstants.PR_COMMAND_SETDATA;
import static com.meyer.muscle.client.StorageReflectConstants.PR_COMMAND_SETPARAMETERS;
import static com.meyer.muscle.client.StorageReflectConstants.PR_NAME_KEYS;
import static com.meyer.muscle.client.StorageReflectConstants.PR_NAME_REMOVED_DATAITEMS;
import static com.meyer.muscle.client.StorageReflectConstants.PR_NAME_SERVER_MEM_AVAILABLE;
import static com.meyer.muscle.client.StorageReflectConstants.PR_NAME_SERVER_MEM_USED;
import static com.meyer.muscle.client.StorageReflectConstants.PR_NAME_SERVER_UPTIME;
import static com.meyer.muscle.client.StorageReflectConstants.PR_NAME_SERVER_VERSION;
import static com.meyer.muscle.client.StorageReflectConstants.PR_NAME_SESSION_ROOT;
import static com.meyer.muscle.client.StorageReflectConstants.PR_RESULT_DATAITEMS;
import static com.meyer.muscle.client.StorageReflectConstants.PR_RESULT_PARAMETERS;
import static com.meyer.muscle.support.TypeConstants.B_BOOL_TYPE;
import static com.meyer.muscle.support.TypeConstants.B_INT32_TYPE;
import static com.meyer.muscle.support.TypeConstants.B_INT64_TYPE;
import static com.meyer.muscle.support.TypeConstants.B_MESSAGE_TYPE;
import static com.meyer.muscle.support.TypeConstants.B_STRING_TYPE;


/**
 * JavaShareTransceiver - This class handles all incomming/outgoing Muscle messages
 * and then sends the appropriate JavaShareEvents to any registered listener.
 *
 * @author Bryan Varner
 */
public class JavaShareTransceiver implements MessageListener {
	public static final int portRange = 50;
	public static final int startingPortNumber = 7000;
	// Connection Management
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
	private static final String[] ALL_SESSIONS = new String[]{"*"};
	private static final Object serverConnect = new Object();
	private static final Object serverDisconnect = new Object();
	private int serverPort = 2960;
	private MessageTransceiver beShareTransceiver = new MessageTransceiver(new MessageQueue(this));
	private String localSessionID = "";
	private AbstractDropMenuModel<String> serverModel = new StringDropMenuModel(10);
	private AbstractDropMenuModel<String> nameModel = new StringDropMenuModel(5);
	private AbstractDropMenuModel<String> statusModel = new StringDropMenuModel(5);
	private long installId;
	private boolean connectInProgress = false;
	private boolean connected = false;
	private boolean firewalled = false;

	private int pingCount = 0;

	private boolean queryActive = false;

	private long lastEvent = System.currentTimeMillis();
	private long lastAction = System.currentTimeMillis();
	private boolean disconnectExpected = false;
	private int reconnectBackoff = -1;
	private int connectionTimeout = 300; // in seconds = 5 Minutes

	private int awayTimeout = 300;
	private transient String restoreStatus = "";
	private String awayStatus = "";
	private boolean isAway = false;

	private UserDataModel userDataModel = new UserDataModel();
	private List<ChatDocument> chatDocuments = new ArrayList<>();

	private List<String> loginCommands = new ArrayList<>();
	private HashMap<String, String> aliases = new HashMap<>();

	private HashMap<String, Pattern> ignores = new LinkedHashMap<>();
	private HashMap<String, Pattern> autopriv = new LinkedHashMap<>();

	private Preferences preferences;

	/**
	 * Construct a new JavaShareTransceiver,
	 */
	public JavaShareTransceiver(final Preferences preferences) {
		this.preferences = preferences;
		this.installId = preferences.getLong("installId", 0l);
		this.awayTimeout = preferences.getInt("awayTimeout", 300);

		// TODO: Load the loginCommands
		// TODO: Load the aliases
		// TODO: Load the ignores
		// TODO: Load the autopriv

		ThreadPool.getDefaultThreadPool().startThread(new ConnectionCheck());
		ThreadPool.getDefaultThreadPool().startThread(new AutoAway());
		ThreadPool.getDefaultThreadPool().startThread(new ServerAutoUpdate(serverModel));

		serverModel.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				checkAwayStatus();
				logSystemMessage("Current server changed to " + serverModel.getSelectedItem());
				serverModel.saveTo(preferences, "servers");
				if (connected || connectInProgress) {
					connect();
				}
			}
		});
		serverModel.loadFrom(preferences, "servers");

		nameModel.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				checkAwayStatus();
				nameModel.saveTo(preferences, "names");
				sendUserName();
			}
		});
		nameModel.loadFrom(preferences, "names", "Binky");

		statusModel.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				statusModel.saveTo(preferences, "status");
				sendUserStatus();
			}
		});
		statusModel.loadFrom(preferences, "status", "Here|Away");
		awayStatus = preferences.get("awayStatus", "Away");
	}

	public boolean isConnected() {
		return connected;
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
	 * Gets the model for tracking serverNames and the selected server.
	 *
	 * @return
	 */
	public AbstractDropMenuModel<String> getServerModel() {
		return serverModel;
	}

	/**
	 * Gets the model for tracking user names the selected username.
	 *
	 * @return
	 */
	public AbstractDropMenuModel<String> getNameModel() {
		return nameModel;
	}


	/**
	 * Gets the model for tracking status and the selected status.
	 *
	 * @return
	 */
	public AbstractDropMenuModel<String> getStatusModel() {
		return statusModel;
	}


	/**
	 * Sends our username
	 */
	private void sendUserName() {
		// Remove the existing Username pattern.
		StyledString.removePattern(StyledString.USERNAME_PATTERN_NAME);
		String localUserName = nameModel.getSelectedItem();

		if (connected) {
			Message nameMessage = new Message();
			nameMessage.setString("name", localUserName);
			nameMessage.setInt("port", localUserPort);
			nameMessage.setLong("installid", installId);
			nameMessage.setString("version_name", "JavaShare");
			nameMessage.setString("version_num", Application.VERSION);
			setDataNodeValue("beshare/name", nameMessage);
		}

		logSystemMessage("Your name has been changed to " + localUserName);
		StyledString.addSystemPattern(StyledString.USERNAME_PATTERN_NAME, ".*" + localUserName + ".*", StyledString.LOCAL_USER_MENTIONED);
	}

	/**
	 * sends our status
	 */
	private void sendUserStatus() {
		String localUserStatus = statusModel.getSelectedItem();
		if (connected) {
			Message statusMessage = new Message();
			statusMessage.setString("userstatus", localUserStatus);
			setDataNodeValue("beshare/userstatus", statusMessage);
		}
		logSystemMessage("Your status has been changed to " + localUserStatus);
	}

	/**
	 * Gets the id of the local user.
	 *
	 * @return String the local SessionID of this user.
	 */
	@Deprecated
	public String getLocalSessionID() {
		return localSessionID;
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

	/**
	 * Add a ChatDocument to receive content from this JavaShareTransceiver.
	 *
	 * @param chatDocument
	 */
	public void addChatDocument(final ChatDocument chatDocument) {
		chatDocuments.add(chatDocument);
	}

	/**
	 * Remove a ChatDocument so it no longer receives content from this JavaShareTransceiver.
	 *
	 * @param chatDocument
	 */
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
		cbackMsg.setString("session", localSessionID);
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
	private synchronized void connect() {
		disconnectExpected = false;
		connectInProgress = true;
		connected = false;

		beShareTransceiver.disconnect();
		userDataModel.clear();

		String serverName = serverModel.getSelectedItem();
		logSystemMessage("Connecting to: " + serverName);
		beShareTransceiver.connect(serverName, serverPort, serverConnect, serverDisconnect);
	}

	/**
	 * Force a disconnect from the current server.
	 */
	private synchronized void disconnect() {
		disconnectExpected = true;
		beShareTransceiver.disconnect();
		userDataModel.clear();
	}

	/**
	 * Sends a message subscribing to the BeShare nodes.
	 */
	private void beShareSubscribe() {
		// Get our local session info.
		beShareTransceiver.sendOutgoingMessage(new Message(PR_COMMAND_GETPARAMETERS));
		// set up a subscription to the beShare tree.
		Message queryMsg = new Message(PR_COMMAND_SETPARAMETERS);
		queryMsg.setBoolean("SUBSCRIBE:beshare/*", true);
		beShareTransceiver.sendOutgoingMessage(queryMsg);
	}

	/**
	 * Handles input.
	 *
	 * @param text
	 * @param chatDoc
	 */
	public void handleInput(final String text, final ChatDocument chatDoc) {
		// If the input matches any alias...
		if (aliases.containsKey(text.trim())) {
			handleInput(aliases.get(text.trim()), chatDoc);
		} else {
			// Check if we're a command.
			if (text.startsWith("/")) {
				command(text, chatDoc);
			} else {
				sendChat(text, chatDoc, null);
			}
		}
	}

	/**
	 * Sends text to the users associated with the FilteredUserDataModel from the ChatDocument, <code>chatDoc</code>,
	 * unless <code>privateSessionIds</code> is specified. When <code>privateSessionIds</code> is not-null, and not-empty, the text
	 * is sent to only the sessions specified in the array.
	 *
	 * @param text              The String to send
	 * @param chatDoc           The document to update with the content you've sent.
	 * @param privateSessionIds An optional list of sessionIds to receive the message as private, which overrides the sessions obtained from a FilteredUserDataModel obtained from the chatDoc.
	 */
	private void sendChat(final String text, final ChatDocument chatDoc, final String[] privateSessionIds) {
		checkAwayStatus();
		String[] sessions = ALL_SESSIONS;
		if (chatDoc.getFilteredUserDataModel().isFiltering()) {
			sessions = chatDoc.getFilteredUserDataModel().getSessionIds().toArray(new String[0]);
		}

		// A privateSessionIds param overrides the defaults for the chatDoc (/msg to another user from a frame / panel)
		if (privateSessionIds != null && privateSessionIds.length > 0) {
			sessions = privateSessionIds;
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
		chatDoc.addEchoChatMessage(text, chatMessage.hasField("private"), localSessionID, nameModel.getSelectedItem(), privateSessionIds);
	}

	private void checkAwayStatus() {
		lastAction = System.currentTimeMillis();
		if (isAway) {
			isAway = false;
			statusModel.ensureSelected(restoreStatus);
		}
	}

	private void setAway() {
		restoreStatus = statusModel.getSelectedItem();
		isAway = true;
		statusModel.ensureSelected(awayStatus);
	}

	/**
	 * Handles text commands, echoing output to the given chatDoc.
	 *
	 * @param command
	 * @param chatDoc
	 */
	public void command(final String command, final ChatDocument chatDoc) {
		checkAwayStatus();
		String lowerCommand = command.toLowerCase().trim();
		if (lowerCommand.startsWith("/me ") || lowerCommand.startsWith("/action ")) {
			// This isn't really a command, but we'll handle it here.
			sendChat(command, chatDoc, null);
		} else if (lowerCommand.startsWith("/priv")) {
			new PrivateFrame(this, new String[]{command.substring(5).trim()}).setVisible(true);
		} else if (lowerCommand.startsWith("/msg")) {
			int msgStart = command.substring(5).trim().indexOf(" ");
			if (msgStart <= 0) {
				chatDoc.addErrorMessage("You didn't include a message to send.");
				chatDoc.addErrorMessage("Syntax: '/msg [user|sessionId] [message]");
			} else {
				String targetUser = command.substring(5).trim().substring(0, msgStart);
				String message = command.substring(5 + msgStart).trim();

				BeShareUser user = userDataModel.findByNameOrSession(targetUser);
				if (user != null) {
					sendChat(message, chatDoc, new String[]{user.getSessionID()});
				} else {
					chatDoc.addWarningMessage("Could not find a username or session # for: " + targetUser);
				}
			}
		} else if (lowerCommand.startsWith("/serverinfo")) {
			getServerInfo();
		} else if (lowerCommand.startsWith("/server")) {
			String serverName = command.substring(7).trim();
			if (!"".equals(serverName)) {
				serverModel.ensureSelected(serverName);
			}
		} else if (lowerCommand.startsWith("/connect")) {
			String serverName = command.substring(8).trim();
			if (!"".equals(serverName)) {
				serverModel.ensureSelected(serverName);
			}
			connect();
		} else if (lowerCommand.equalsIgnoreCase("/disconnect")) {
			disconnect();
		} else if (lowerCommand.equalsIgnoreCase("/clear")) {
			chatDoc.clear();
		} else if (lowerCommand.equalsIgnoreCase("/away")) {
			setAway();
		} else if (lowerCommand.startsWith("/awaymsg")) {
			awayStatus = command.substring(8).trim();
			if (!statusModel.contains(awayStatus)) {
				statusModel.addElement(awayStatus);
			}
			preferences.put("awayStatus", awayStatus);
			logSystemMessage("Auto-away status set to " + awayStatus);
		} else if (lowerCommand.startsWith("/alias")) {
			String pair = command.substring(6).trim();
			if (pair.equals("")) {
				// Print the current list of aliases.
				StringBuilder sb = new StringBuilder("Current Aliases:\n");
				for (Map.Entry<String, String> alias : aliases.entrySet()) {
					sb.append("        ").append(alias.getKey()).append('=').append(alias.getValue()).append('\n');
				}
				chatDoc.addSystemMessage(sb.toString());
			} else {
				String[] parts = pair.split("=");
				if (parts.length == 2) {
					aliases.put(parts[0], parts[1]);
					chatDoc.addSystemMessage("Set alias [" + parts[0] + "=" + parts[1] + "]");
				} else {
					chatDoc.addErrorMessage("Failed to parse alias: " + pair + " into [name=key]");
				}
			}
		} else if (lowerCommand.startsWith("/unalias")) {
			String names = command.substring(8).trim();
			if (names.length() > 0) {
				if (names.equals("all")) {
					StringBuilder removeAll = new StringBuilder("/unalias ");
					for (String name : aliases.keySet()) {
						removeAll.append(name + " ");
					}
					command(removeAll.toString(), chatDoc);
				} else {
					for (String name : names.split(" ")) {
						String alias = aliases.remove(name);
						if (alias != null) {
							chatDoc.addSystemMessage("Removed alias [" + name + "=" + alias + "]");
						} else {
							chatDoc.addErrorMessage("No alias found for: " + name);
						}
					}
				}
			} else {
				// Show the current aliases.
				command("/alias", chatDoc);
			}
		} else if (lowerCommand.startsWith("/watch")) {
			if (lowerCommand.equals("/watch")) { // If all they typed was 'watch', then list the current watches.
				StringBuilder sb = new StringBuilder("Current Watch Expressions:\n");
				for (String pattern : StyledString.getUserPatterns()) {
					sb.append("        ").append(pattern).append("\n");
				}
				chatDoc.addSystemMessage(sb.toString());
			} else {
				String watchList = command.substring(6).trim();
				for (String watchPattern : watchList.split(" ")) {
					StyledString.addUserPattern(watchPattern, ".*" + watchPattern + ".*", StyledString.WATCH_PATTERN);

					chatDoc.addSystemMessage("Added watch pattern: " + watchPattern);
				}
			}
		} else if (lowerCommand.startsWith("/unwatch")) {
			if (lowerCommand.length() > 8) {
				String removePattern = command.substring(8).trim();

				// If you type '/unwatch all' it will remove all patterns.
				if (removePattern.equals("all")) {
					for (String pattern : StyledString.getUserPatterns()) {
						StyledString.removePattern(pattern);
					}
					chatDoc.addSystemMessage("All watch patterns removed.");
				} else if (StyledString.removePattern(removePattern)) {
					chatDoc.addSystemMessage("Removed watch pattern: " + removePattern);
				} else {
					chatDoc.addErrorMessage("The pattern you entered, '" + removePattern + "', did not match a known watch pattern.");
				}
			} else {
				chatDoc.addWarningMessage("No pattern to remove specified.");
			}
		} else if (lowerCommand.startsWith("/ping")) {
			String targetUser = command.substring(5).trim();
			BeShareUser user = userDataModel.findByNameOrSession(targetUser);
			if (user != null) {
				pingUser(user.getSessionID());
				chatDoc.addSystemMessage("Ping sent to User #" + user.getSessionID() + " (a.k.a. " + user.getName() + ")");
			} else {
				chatDoc.addWarningMessage("Could not find a username or session # for: " + targetUser);
			}
		} else if (lowerCommand.startsWith("/autopriv")) {
			if (lowerCommand.equals("/autopriv")) { // If all that's entered is '/autopriv', list all current patterns.
				StringBuilder sb = new StringBuilder("Current Auto-Private Expressions:\n");
				for (String pattern : autopriv.keySet()) {
					sb.append("        ").append(pattern).append("\n");
				}
				chatDoc.addSystemMessage(sb.toString());
			} else {
				for (String pattern : command.substring(9).trim().split(" ")) {
					if (pattern.equals("*")) {
						autopriv.put(pattern, Pattern.compile(".*"));
					} else {
						autopriv.put(pattern, Pattern.compile(".*" + pattern + ".*"));
					}
					chatDoc.addSystemMessage("Added Auto-Private pattern: " + pattern);
				}
			}
		} else if (lowerCommand.startsWith("/ignore")) {
			String[] ignores = command.substring(7).trim().split(" ");
			if (ignores.length == 1 && ignores[0].equals("")) {
				StringBuilder sb = new StringBuilder("Current Ignore User Patterns:\n");
				for (Map.Entry<String, Pattern> ignore : this.ignores.entrySet()) {
					sb.append("        ").append(ignore.getKey()).append("\n");
				}
				chatDoc.addSystemMessage(sb.toString());
			} else {
				for (String ignore : ignores) {
					this.ignores.put(ignore, Pattern.compile(ignore));
					chatDoc.addSystemMessage("Added ignore pattern: " + ignore);
				}
			}
		} else if (lowerCommand.startsWith("/unignore")) {
			String[] unignores = command.substring(9).trim().split(" ");
			if (unignores.length == 0) {
				command("/ignore", chatDoc); // Show all the ignores
			} else if (unignores.length == 1 && unignores[0].equalsIgnoreCase("all")) {
				this.ignores.clear();
				chatDoc.addSystemMessage("All ignore patterns removed.");
			} else {
				for (String remove : unignores) {
					this.ignores.remove(remove);
					chatDoc.addSystemMessage("Removed ignore pattern: " + remove);
				}
			}
		} else if (lowerCommand.startsWith("/unautopriv")) {
			if (lowerCommand.length() > 11) {
				String removePattern = command.substring(11).trim();

				// If you type '/unautopriv all' it will remove all patterns.
				if (removePattern.equals("all")) {
					autopriv.clear();
					chatDoc.addSystemMessage("All Auto-Private patterns removed.");
				} else if (autopriv.remove(removePattern) != null) {
					chatDoc.addSystemMessage("Removed Auto-Private pattern: " + removePattern);
				} else {
					chatDoc.addErrorMessage("The pattern you entered, '" + removePattern + "', did not match a known Auto-Private pattern.");
				}
			} else {
				chatDoc.addWarningMessage("No pattern to remove specified.");
			}
		} else if (lowerCommand.startsWith("/nick")) {
			nameModel.ensureSelected(command.substring(5).trim());
		} else if (lowerCommand.startsWith("/onlogin")) {
			String startup = command.substring(8).trim();
			if (startup.equals("")) {
				StringBuilder sb = new StringBuilder("Current Startup Commands:\n");
				for (String cmd : loginCommands) {
					sb.append("        ").append(cmd).append("\n");
				}
				chatDoc.addSystemMessage(sb.toString());
			} else {
				loginCommands.add(startup);
			}
		} else if (lowerCommand.startsWith("/unonlogin")) {
			String remove = command.substring(10).trim();
			if (remove.equals("")) {
				command("/onlogin", chatDoc); // Show all current commands.
			} else if (remove.equals("all")) {
				StringBuilder sb = new StringBuilder("/unonlogin ");
				loginCommands.clear();
				chatDoc.addSystemMessage("All startup commands removed.");
			} else {
				if (loginCommands.remove(remove)) {
					chatDoc.addSystemMessage("Removed startup command: " + remove);
				} else {
					chatDoc.addErrorMessage("The command you entered: " + remove + " did not match any startup commands.");
				}
			}
		} else if (lowerCommand.startsWith("/quit")) {
			System.exit(0);
		} else if (lowerCommand.startsWith("/status")) {
			String status = command.substring(7).trim();
			statusModel.ensureSelected(status);
		} else if (lowerCommand.startsWith("/help")) {
			logSystemMessage("JavaShare Command Refrence\n"
					                 + "       /action [action] - do something\n"
					                 + "       /alias [name=[value]] - create an alias\n"
					                 + "       /autopriv [* | name | session id ...] - specify AutoPriv users\n"
					                 + "       /away [status] - Force away state\n"
					                 + "       /awaymsg [status] - change the auto-away tag\n"
					                 + "       /clear - clear the chat log\n"
					                 + "       /connect [serverName] - connect to a server\n"
					                 + "       /disconnect - disconnect from the server\n"
					                 + "       /help - show this help text\n"
					                 + "       /ignore [name | session id ...] - specify a user to ignore\n"
					                 + "       /unignore [name | session id ...] - specify a user to stop ignoring\n"
					                 + "       /me [action] - synonym for /action\n"
					                 + "       /msg [name | session id] [message] - send a private message\n"
					                 + "       /nick [name] - change your user name\n"
					                 + "       /onlogin [command] - add a startup command\n"
					                 + "       /priv [name | session id ...] - Open Private Chat Window\n"
					                 + "       /ping [name | session id ...] - ping other clients\n"
					                 + "       /quit - quit BeShare\n"
					                 + "       /server [address] - Sets the server address.\n"
					                 + "       /serverinfo - Request server status\n"
					                 + "       /status [status] - set user status string\n"
					                 + "       /unalias [all | name] - remove an alias\n"
					                 + "       /unonlogin [command] - remove a startup command\n"
					                 + "       /unwatch [all | name | session id ...] - Specify users to stop watching\n"
					                 + "       /unautopriv [all | name | session id ...] - Remove a user from auto-private\n"
					                 + "       /watch [name | session id ...] - specify users to watch\n");
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
	 * Sends a <code>PR_COMMAND_GETPARAMETERS</code> to the server.
	 */
	private void getServerInfo() {
		logSystemMessage("Server status requested.");
		Message infoMessage = new Message(PR_COMMAND_GETPARAMETERS);
		beShareTransceiver.sendOutgoingMessage(infoMessage);
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
			sendUserName();
			sendUserStatus();
			setUploadBandwidth("?", 0);

			if (!chatDocuments.isEmpty()) {
				for (String command : loginCommands) {
					// Hack hack hackity hack hack
					handleInput(command, chatDocuments.get(0));
				}
			}

			// TODO: Update the list of files you share.
		} else if (message == serverDisconnect) {
			connected = false;
			connectInProgress = false;
			logSystemMessage("Disconnected from: " + serverModel.getSelectedItem());
			if (!disconnectExpected && reconnectBackoff < 0) {
				reconnectBackoff = 0;
				ThreadPool.getDefaultThreadPool().startThread(new AutoReconnector());
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
				message.setString("version", Application.VERSION);
				beShareTransceiver.sendOutgoingMessage(message);
			}
			break;

			// New chat text!
			case NET_CLIENT_NEW_CHAT_TEXT: {
				String sourceSessionId = message.getString("session");
				String sourceUserName = userDataModel.findNameBySession(message.getString("session"));
				boolean isPrivate = message.hasField("private");
				boolean isIgnored = false;

				// Check against the ignore patterns.
				for (Map.Entry<String, Pattern> ignore : this.ignores.entrySet()) {
					if (ignore.getValue().matcher(sourceUserName).matches()) {
						isIgnored = true;
						break;
					}
				}

				// If we're not ignored...
				if (!isIgnored) {
					boolean privateChatExists = false;
					for (int i = chatDocuments.size() - 1; isPrivate && i >= 0 && !privateChatExists; i--) {
						privateChatExists = chatDocuments.get(i).willConsumePrivate(sourceSessionId);
					}

					// If there is no private, and we match an autoPrivate pattern...
					if (isPrivate && !privateChatExists) {
						for (Map.Entry<String, Pattern> privEntry : autopriv.entrySet()) {
							if (privEntry.getValue().matcher(sourceUserName).matches()) {
								// Create a new Private Dialog
								new PrivateFrame(this, new String[]{sourceSessionId}).setVisible(true);
								break;
							}
						}
					}

					boolean consumed = false;
					for (int i = chatDocuments.size() - 1; i >= 0 && !consumed; i--) {
						consumed =
								chatDocuments.get(i).addRemoteChatMessage(message.getString("text").trim(), sourceSessionId, isPrivate);
					}
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
				processServerInfo(message);
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
							logSystemMessage("User #" + removed.getSessionID() + " (a.k.a. " + removed.getName() + ") has disconnected.");
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
								logSystemMessage("User #" + sessionId + " is now connected.");
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
								logSystemMessage("User #" + user.getSessionID() + " is now known as " + user.getName());
							} else if (updatedNode.equals("userstatus")) {
								logSystemMessage("User #" + user.getSessionID() + " " +
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

							// TODO: Transfer query bits.
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
	 * by calling logSystemMessage();
	 */
	protected final void processServerInfo(Message message) {
		if (message.hasField(PR_NAME_SERVER_VERSION)) {
			logSystemMessage("Server version:  " + message.getString(PR_NAME_SERVER_VERSION, "<unknown>"));
		}
		if (message.hasField(PR_NAME_SERVER_UPTIME)) {
			long seconds = message.getLong(PR_NAME_SERVER_UPTIME, 1000000) / 1000000;
			long minutes = seconds / 60;
			seconds = seconds % 60;
			long hours = minutes / 60;
			minutes = minutes % 60;
			long days = hours / 24;
			hours = hours % 24;
			long weeks = days / 7;
			days = days % 7;
			logSystemMessage("Server uptime:   " + weeks + " weeks, " + days + " days, " + hours +
					                 ":" + minutes + ":" + seconds);
		}
		if (message.hasField(PR_NAME_SESSION_ROOT)) {
			String sessionRoot = message.getString(PR_NAME_SESSION_ROOT, "/");
			this.localSessionID = sessionRoot.substring(sessionRoot.lastIndexOf('/') + 1);

		}
		if (message.hasField(PR_NAME_SERVER_MEM_AVAILABLE) && message.hasField(PR_NAME_SERVER_MEM_USED)) {
			final float oneMeg = 1024.0f * 1024.0f;
			float memAvailableMB = ((float) message.getLong(PR_NAME_SERVER_MEM_AVAILABLE, 0)) / oneMeg;
			float memUsedMB = ((float) message.getLong(PR_NAME_SERVER_MEM_USED, 0)) / oneMeg;
			logSystemMessage("Server memory usage:    " + memUsedMB + "MB used (" + memAvailableMB + "MB available)");
		}
	}

	/**
	 * Sends <code>message</code> as a System information chat message
	 *
	 * @param message - The Text to display as an informational message.
	 */
	public final void logSystemMessage(final String message) {
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
	 * Automatically trigger the away setting if there hasn't been any local activity...
	 */
	private class AutoAway implements Runnable {
		public void run() {
			while (true) {
				try {
					Thread.currentThread().sleep(1000);
					// TODO: If auto-away is enabled....
					if (connected && System.currentTimeMillis() - lastAction >= (awayTimeout * 1000) && !isAway) {
						setAway();
					}
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
						logSystemMessage("Reconnecting...");
						break;
					case 30:
						// Seconds Message
						logSystemMessage("Reconnecting in " + reconnectBackoff + " seconds...");
						break;
					case 60:
						// Minutes message
						logSystemMessage("Reconnecting in 1 minute...");
						break;
					default:
						// Minutes message
						logSystemMessage("Reconnecting in " + (reconnectBackoff / 60) + " minutes...");
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

	private class StringDropMenuModel extends AbstractDropMenuModel<String> {
		StringDropMenuModel() {
			super();
		}

		StringDropMenuModel(int size) {
			super(size);
		}

		@Override
		public String elementToString(String obj) {
			return obj;
		}

		@Override
		public String elementFromString(String obj) {
			return obj;
		}
	}
}
