package org.beShare.event;

import org.beShare.data.BeShareUser;

import java.util.EventObject;

/**
	JavaShareEvent
	
	Last Update: 2-28-2002
	
	@author Bryan Varner
	@version 1.0
*/

public class JavaShareEvent extends EventObject{
	int				 	eventType;
	BeShareUser			eventUser;
	String				eventSourceServer;
	String				localName;
	String				localSession;
	
	public final static int CONNECTION_ATTEMPT = 0;
	public final static int CONNECTION_DISCONNECT = 1;
	public final static int SERVER_CONNECTED = 2;
	public final static int SERVER_DISCONNECTED = 3;
	public final static int UNKNOWN_MUSCLE_MESSAGE = 4;
	public final static int CHAT_MESSAGE = 5;
	public final static int PING_RECEIVED = 6;
	public final static int USER_DISCONNECTED = 7;
	public final static int USER_CONNECTED = 8;
	public final static int USER_STATUS_CHANGE = 9;
	public final static int USER_UPLOAD_STATS_CHANGE = 10;
	public final static int USER_BANDWIDTH_CHANGE = 11;
	public final static int USER_FIREWALL_CHANGE = 12;
	public final static int USER_FILE_COUNT_CHANGE = 13;
	public final static int FILE_INFO_ADD_TO_RESULTS = 14;
	public final static int FILE_INFO_REMOVE_RESULTS = 15;
	
	/**
		Creates a new BeShare event with object <code>source</code> and <code
		>type</code>.
	*/
	public JavaShareEvent(Object source, int type){
		super(source);
		eventType = type;
		eventSourceServer = null;
		localName = null;
		localSession = null;
	}
	
	/**
		Creates a new BeShare event with object <code>source</code> and <code
		>type</code>, as well as a defined <code>user</code> and <code
		>sourceServer</code>
	*/
	public JavaShareEvent(Object source, int type, BeShareUser user,
							String sourceServer){
		super(source);
		eventType = type;
		eventUser = user;
		eventSourceServer = sourceServer;
	}
	
	/**
		Creates a new BeShare event with object <code>source</code> and <code
		>type</code>, as well as a defined <code>user</code> and <code
		>sourceServer</code>
	*/	
	public JavaShareEvent(Object source, int type, BeShareUser user,
							String sourceServer, String lName,
							String lSession){
		super(source);
		eventType = type;
		eventUser = user;
		eventSourceServer = sourceServer;
		localName = lName;
		localSession = lSession;
	}
	
	/**
		@return the message type.
	*/
	public int getType(){
		return eventType;
	}
	
	/**
		@return the BeShareUser associated with this event.
	*/
	public BeShareUser getUser(){
		return eventUser;
	}
	
	/**
		@return A string representing the server the event originated from.
	*/
	public String getServer(){
		return eventSourceServer;
	}
	
	/**
		@return the local users name.
	*/
	public String getLocalName(){
		return localName;
	}
	
	/**
		@return the Local users session.
	*/
	public String getLocalSession(){
		return localSession;
	}
}
