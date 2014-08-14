package org.beShare.event;

import org.beShare.data.BeShareUser;

import java.util.EventObject;

/**
 * JavaShareEvent
 *
 * @author Bryan Varner
 */

public class JavaShareEvent extends EventObject {
	public final static int CONNECTION_ATTEMPT = 0;
	public final static int CONNECTION_DISCONNECT = 1;
	public final static int SERVER_CONNECTED = 2;
	public final static int SERVER_DISCONNECTED = 3;
	public final static int UNKNOWN_MUSCLE_MESSAGE = 4;
	public final static int PING_RECEIVED = 6;
	public final static int USER_DISCONNECTED = 7;
	public final static int USER_CONNECTED = 8;
	public final static int USER_NAME_CHANGE = 18;
	public final static int USER_STATUS_CHANGE = 9;
	public final static int USER_UPLOAD_STATS_CHANGE = 10;
	public final static int USER_BANDWIDTH_CHANGE = 11;
	public final static int USER_FIREWALL_CHANGE = 12;
	public final static int USER_FILE_COUNT_CHANGE = 13;
	public final static int FILE_INFO_ADD_TO_RESULTS = 14;
	public final static int FILE_INFO_REMOVE_RESULTS = 15;
	public final static int LOCAL_USER_STATUS = 16;
	public final static int LOCAL_USER_NAME = 17;
	int eventType;
	BeShareUser eventUser;

	/**
	 * Creates a new BeShare event with object <code>source</code> and <code
	 * >type</code>.
	 */
	public JavaShareEvent(Object source, int type) {
		this(source, type, null);
	}

	/**
	 * Creates a new BeShare event with object <code>source</code> and <code
	 * >type</code>, as well as a defined <code>user</code> and <code
	 * >sourceServer</code>
	 */
	public JavaShareEvent(Object source, int type, BeShareUser user) {
		super(source);
		eventType = type;
		eventUser = user;
	}


	/**
	 * @return the message type.
	 */
	public int getType() {
		return eventType;
	}

	/**
	 * @return the BeShareUser associated with this event.
	 */
	public BeShareUser getUser() {
		return eventUser;
	}
}
