package org.beShare.data;

/**
 *  ChatMessage - This class defines a new chat message. It holds all revelant
 *  data for the message. Class Started: 2-05-2002 Last Update: 3-02-2002
 *
 * @author     Bryan Varner
 * @created    March 8, 2002
 * @version    1.0
 */
public class ChatMessage {

	/**
	 *  Description of the Field
	 *
	 * @since
	 */
	public final static int LOG_REMOTE_USER_CHAT_MESSAGE = 1;
	/**
	 *  Description of the Field
	 *
	 * @since
	 */
	public final static int LOG_LOCAL_USER_CHAT_MESSAGE = 2;
	/**
	 *  Description of the Field
	 *
	 * @since
	 */
	public final static int LOG_INFORMATION_MESSAGE = 3;
	/**
	 *  Description of the Field
	 *
	 * @since
	 */
	public final static int LOG_USER_EVENT_MESSAGE = 4;
	/**
	 *  Description of the Field
	 *
	 * @since
	 */
	public final static int LOG_UPLOAD_EVENT_MESSAGE = 5;
	/**
	 *  Description of the Field
	 *
	 * @since
	 */
	public final static int LOG_WARNING_MESSAGE = 6;
	/**
	 *  Description of the Field
	 *
	 * @since
	 */
	public final static int LOG_ERROR_MESSAGE = 7;
	/**
	 *  Description of the Field
	 *
	 * @since
	 */
	public final static int LOG_CLEAR_LOG_MESSAGES = 8;
	/**
	 *  Description of the Field
	 *
	 * @since
	 */
	public final static int PRIVATE_NO_LOG = 9;
	/**
	 *  Description of the Field
	 *
	 * @since
	 */
	public final static int PRIVATE_LOCAL_LOG_ONLY = 10;
	/**
	 *  Description of the Field
	 *
	 * @since
	 */
	public final static int LOG_WATCH_PATTERN_MATCH = 11;
	/**
	 *  Description of the Field
	 *
	 * @since
	 */
	public final static int LOG_TIMESTAMP_MESSAGE = 12;
	String   session;
	String   message;
	boolean  prvMessage;
	int      typeMsg;

	boolean  localMessage;
	String   targetPosterID;


	/**
	 *  Create a new public message with source session <code>s</code>, message
	 *  text <code>m</code>, private message switch <code>p</code> and type <code>t</code>
	 *  .
	 *
	 * @param  s  Description of Parameter
	 * @param  m  Description of Parameter
	 * @param  p  Description of Parameter
	 * @param  t  Description of Parameter
	 */
	public ChatMessage(String s, String m, boolean p, int t) {
		session = s;
		message = m;
		prvMessage = p;
		typeMsg = t;

		localMessage = false;
		targetPosterID = "";
	}


	/**
	 *  Create a new public message with source session <code>s</code>, message
	 *  text <code>m</code>, private message switch <code>p</code>,type <code>t</code>
	 *  , local switch <code>l</code> and target session <code
	 *> tpid</code>.
	 *
	 * @param  s     Description of Parameter
	 * @param  m     Description of Parameter
	 * @param  p     Description of Parameter
	 * @param  t     Description of Parameter
	 * @param  l     Description of Parameter
	 * @param  tpid  Description of Parameter
	 */
	public ChatMessage(String s, String m, boolean p, int t, boolean l, String
			tpid) {
		session = s;
		message = m;
		prvMessage = p;
		typeMsg = t;

		localMessage = l;
		targetPosterID = tpid;
	}


	/**
	 *  Copy Constructor - Creates a duplicate copy of the message.
	 *
	 * @param  cm  Description of Parameter
	 */
	public ChatMessage(ChatMessage cm) {
		session = cm.getSession();
		message = cm.getMessage();
		prvMessage = cm.isPrivate();
		typeMsg = cm.getType();
		localMessage = cm.isLocalMessage();
		targetPosterID = cm.getTargetID();
	}


	/**
	 * @param  s  String representing the session id this message originated from
	 */
	public void setSession(String s) {
		session = s;
	}
	
	/**
	 * @param  m  String to deliver as the message.
	 */
	public void setMessage(String m) {
		message = m;
	}
	
	/**
	 * @param  b  boolean representing the private status of this message.
	 */
	public void setPrivate(boolean b) {
		prvMessage = b;
	}
	
	/**
	 * @param  t  ChatMessage type constant describing the type of this message.
	 */
	public void setType(int t) {
		typeMsg = t;
	}
	
	/**
	 * @param  b  Boolean representing the local message status of this
	 *      ChatMessage.
	 */
	public void setLocalMessage(boolean b) {
		localMessage = b;
	}
	
	/**
	 * @param  t  A String representation of the target session ID.
	 */
	public void setTargetID(String t) {
		targetPosterID = t;
	}
	
	/**
	 * @return    String representing the session id this message originated from
	 */
	public String getSession() {
		return session;
	}
	
	/**
	 * @return    the message.
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * @return    <code>true</code> if the message is private, <code>false</code >
	 *      if it's public.
	 */
	public boolean isPrivate() {
		return prvMessage;
	}
	
	/**
	 * @return    A ChatMessage type constant describing the type of this message.
	 */
	public int getType() {
		return typeMsg;
	}
	
	/**
	 * @return    <code>true</code> if it is a local message, <code>false</code >
	 *      if it isn't.
	 */
	public boolean isLocalMessage() {
		return localMessage;
	}
	
	/**
	 * @return    A String representation of the target session ID.
	 */
	public String getTargetID() {
		return targetPosterID;
	}
	
	/**
	 * @return    an Informational string about this ChatMessage.
	 */
	public String toString() {
		return "Session Source = " + session + " : " +
				"Message = " + message + " : " +
				"Private Status = " + prvMessage + " : " +
				"Type = " + typeMsg + " : " +
				"Local Status = " + localMessage + " : " +
				"TargetID = " + targetPosterID + "\n";
	}
}

