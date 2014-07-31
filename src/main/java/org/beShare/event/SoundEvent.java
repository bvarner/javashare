package org.beShare.event;

/**
	SoundEvent
	
	Last Update: 3-04-2002
	
	@author Bryan Varner
	@version 1.0
*/

public class SoundEvent {
	public final static int NO_TYPE_SPECIFIED = -1;
	public final static int USER_NAME_MENTIONED = 0;
	public final static int PRIVATE_MESSAGE_RECEIVED = 1;
//	public final static int AUTOCOMPLETE_FAILURE = 2;
//	public final static int DOWNLOAD_FINISHED = 3;
//	public final static int UPLOAD_STARTED = 4;
	public final static int WATCHED_USER_SPEAKS = 5;
	public final static int PRIVATE_MESSAGE_WINDOW = 6;
//	public final static int INACTIVE_CHAT_WINDOW_RECEIVED_TEXT = 7;
	
	private int type;
	
	/**
		Default Constructor, creates a New SoundEvent with <code
			>NO_TYPE_SPECIFIED</code>
	*/
	public SoundEvent(){
		type = -1;
	}
	
	/**
		Creates a new SoundEvent with type <code>t</code>
	*/
	public SoundEvent(int t){
		type = t;
	}
	
	/**
		@param t Type to assign to this SoundEvent.
	*/
	public void setType(int t){
		type = t;
	}
	
	/**
		@return the Type of event this is.
	*/
	public int getType(){
		return type;
	}
}
