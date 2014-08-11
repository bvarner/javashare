package org.beShare.gui;

import org.beShare.data.ChatMessage;
import org.beShare.data.UserHashAccessor;
import org.beShare.gui.text.StyledString;

import java.awt.*;
import java.util.Vector;
/**
	ChatPoster - This class is responsible for writing new message to a
	<code>ChatMessagingPanel</code>.
	
	Last Update: 4-23-2002
	
	@author Bryan Varner
	@version 1.2
	
	1.1 - 4.23.2002 - Edited addMessage to send StyledStrings instead of
						multiple strings with Style Constants.
						The result here has been the elimination of the multi
						-threaded text posting issues, and a _dramatic_ speed
						increase. The time it takes for a message to pass from
                        JavaShareTransceiver to the time it takes
						ChatDocument to append it is on average 30
						Milliseconds on a 1Ghz Pentium III running Win2k.
						Previous to this it was averaging around 60-80
						Milliseconds.
						
						I wish I would have benchmarked the MacOS Classic
						version.
	1.1.5 - 4.23.2002 - Cleaned up the constructors. Code re-use is yummy.
	1.2 - 4.23.2002 - Added parseStringForStyles
*/
public class ChatPoster {
	ChatMessagingPanel		chatMessageTarget;
	Vector					sessionVect;
	UserHashAccessor		userHashAccessor;
	
	/**
		Creates a new <code>ChatPoster</code> that posts messages to
		<code>target</code>, and get's it's user information from <code
		>hashAccess</code>.
	*/
	public ChatPoster(ChatMessagingPanel target, UserHashAccessor hashAccess){
		chatMessageTarget = target;
		sessionVect = new Vector();
		userHashAccessor = hashAccess;
	}
	
	/**
		Creates a new <code>ChatPoster</code> that posts messages to
		<code>target</code>, and get's it's user information from <code
		>hashAccess</code>. In addition, this <code>ChatPoster</code
		> will initially only respond to messages with a targetID of <code>privateID
		</code>.
	*/
	public ChatPoster(ChatMessagingPanel target, UserHashAccessor hashAccess, String privateID){
		this(target, hashAccess);
		sessionVect.addElement(privateID);
	}
	
	/** 
		Writes a new message to the <code>target</code> specified at the time of
		construction.
		
		@param theMessage The new message to log.
	*/
	public void addMessage(ChatMessage theMessage){
		// Create the StyledString here, since we know we're going to need one.
		StyledString chatString = new StyledString();
		switch(theMessage.getType()){
			case ChatMessage.LOG_REMOTE_USER_CHAT_MESSAGE: {
				// Get the user's name...
				String remoteName = userHashAccessor.findNameBySession(theMessage.getSession());
				if (remoteName.endsWith("]"))
					remoteName = remoteName.substring(remoteName.indexOf("[") + 1, remoteName.indexOf("]"));
				
				// A remote user said something.
				if(theMessage.getMessage().startsWith("/me")){
					chatString.addStyledText("Action: ",
											StyledString.USER_ACTION_STYLE);
					chatString.addStyledText(remoteName
						+ theMessage.getMessage().substring(3) + "\n",
						StyledString.PLAIN_MESSAGE_STYLE);
				}	else {
					chatString.addStyledText("(" + theMessage.getSession() + ") "
						+ remoteName 
						+ ": ", StyledString.REMOTE_USER_NAME_STYLE);
					if(theMessage.isPrivate()){
						chatString.concatStyledText(parseStringForStyles(theMessage.getMessage() + "\n"
								, StyledString.PRIVATE_MESSAGE_STYLE));
					} else {
						chatString.concatStyledText(parseStringForStyles(
								theMessage.getMessage() + "\n",
								StyledString.PLAIN_MESSAGE_STYLE));
					}
				}
			} break;
			case ChatMessage.LOG_LOCAL_USER_CHAT_MESSAGE:{
				// You said something.
				if(theMessage.getMessage().startsWith("/me")){
					chatString.addStyledText("Action: "
						, StyledString.USER_ACTION_STYLE);
					chatString.addStyledText( userHashAccessor.findNameBySession(theMessage.getSession())
					    + theMessage.getSession() +
						theMessage.getMessage().substring(3) + "\n",
						StyledString.PLAIN_MESSAGE_STYLE);
				}	else {
					chatString.addStyledText(theMessage.getSession() + ": "
						, StyledString.LOCAL_USER_NAME_STYLE);
					chatString.concatStyledText(parseStringForStyles(theMessage.getMessage() + "\n"
						, StyledString.PLAIN_MESSAGE_STYLE));
				}
			} break;
			case ChatMessage.LOG_UPLOAD_EVENT_MESSAGE: {
				// System Message
				chatString.addStyledText("System: ", StyledString.SYSTEM_MESSAGE_STYLE);
				chatString.addStyledText(theMessage.getMessage() + "\n"
					, StyledString.PLAIN_MESSAGE_STYLE);
			} break;
			case ChatMessage.LOG_USER_EVENT_MESSAGE: {
				chatString.addStyledText("System: ", StyledString.SYSTEM_MESSAGE_STYLE);
				chatString.addStyledText(theMessage.getMessage() + "\n"
					, StyledString.PLAIN_MESSAGE_STYLE);
			} break;
			case ChatMessage.LOG_INFORMATION_MESSAGE: {
				chatString.addStyledText("System: ", StyledString.SYSTEM_MESSAGE_STYLE);
				chatString.addStyledText(theMessage.getMessage() + "\n"
					, StyledString.PLAIN_MESSAGE_STYLE);
			} break;
			case ChatMessage.LOG_WARNING_MESSAGE: {
				// Opps!
				chatString.addStyledText(theMessage.getMessage() + "\n"
					, StyledString.PLAIN_MESSAGE_STYLE);
			} break;
			case ChatMessage.LOG_ERROR_MESSAGE: {
				// Error - Red message
				chatString.addStyledText("Error: ",
										StyledString.SYSTEM_ERROR_STYLE);
				chatString.addStyledText(theMessage.getMessage() + "\n"
					, StyledString.PLAIN_MESSAGE_STYLE);
			} break;
			case ChatMessage.LOG_TIMESTAMP_MESSAGE: {
				chatString.addStyledText(theMessage.getMessage()
				, StyledString.REMOTE_USER_NAME_STYLE);
			} break;
			case ChatMessage.LOG_WATCH_PATTERN_MATCH: {
				// A remote user said something.
				if(theMessage.getMessage().startsWith("/me")){
					chatString.addStyledText("Action: "
						, StyledString.USER_ACTION_STYLE);
					chatString.addStyledText( userHashAccessor.findNameBySession(theMessage.getSession())
						+ theMessage.getMessage().substring(3) + "\n"
						, StyledString.WATCH_PATTERN_STYLE);
				}	else {
					chatString.addStyledText("(" + theMessage.getSession() + ") "
						+ userHashAccessor.findNameBySession(theMessage.getSession()) 
						+ ": ", StyledString.REMOTE_USER_NAME_STYLE);
					if(theMessage.isPrivate()){
						chatString.concatStyledText(parseStringForStyles(theMessage.getMessage() + "\n"
							, StyledString.PRIVATE_MESSAGE_STYLE));
					} else {
						chatString.concatStyledText(parseStringForStyles(theMessage.getMessage() + "\n"
							, StyledString.WATCH_PATTERN_STYLE));
					}
				}
			} break;
			case ChatMessage.LOG_CLEAR_LOG_MESSAGES: {
				chatMessageTarget.clearText();
			}
		}
		chatMessageTarget.addText(chatString);
		chatMessageTarget.scrollText();
	}
	
	/**
	*	Parses text for strings that should be styled. use defaultStyle as the
	*	default for non-styled text.
	*	
	*	It sure as hell ain't pretty, but it's extendable and works!
	* @return A StyledString that's properly formatted.
	* @param text the String to parse and format.
	* @param defaultStyle the default (unstyled) formatting for the text.
	*/
	public StyledString parseStringForStyles(String text, int defaultStyle){
		int startPos = -1;
		int endPos = 0;
		
		StyledString output = new StyledString();
		// Setup the strings to color here.
		String styStrings[] = {"http://", "beshare:", "audio://", 
									userHashAccessor.findNameBySession("")};
		// Setup the constants to define the colors for the strings as above.
		int styInts[] = {StyledString.URL_STYLE, StyledString.URL_STYLE, StyledString.URL_STYLE,
							StyledString.LOCAL_USER_NAME_SAID_STYLE};
		// As long as there's more text, keep parsing.
		while(text.length() > 0){
			// Find the first occurance of any styled type. If it's a url, parse
			// to either the next space or the end of the line, whichever is first.
			for(int x = 0; x < styStrings.length; x++){
				if (text.indexOf(styStrings[x]) > -1){
					if ((startPos > -1) && (text.indexOf(styStrings[x]) < startPos))
					{
						startPos = text.indexOf(styStrings[x]);
						if((x >= 0) && (x <= 1)){
							endPos = text.indexOf(" ", startPos);
							// In case a linefeed comes before the next space.
							if (text.indexOf("\n", startPos) < endPos){
								endPos = text.indexOf("\n", startPos);
							}
							if (endPos == -1){
								endPos = text.length();
							}
							// Check for URL label
							if (text.indexOf("[", startPos) == endPos + 1) {
								endPos = text.indexOf("]", startPos) + 1;
							}
						} else {
							endPos = text.indexOf(styStrings[x])
									 + styStrings[x].length();
						}
					} else if (startPos == -1){
						startPos = text.indexOf(styStrings[x]);
						if((x >= 0) && (x <= 1)){
							endPos = text.indexOf(" ", startPos);
							// In case a linefeed comes before the next space.
							if (text.indexOf("\n", startPos) < endPos){
								endPos = text.indexOf("\n", startPos);
							}
							if (endPos == -1){
								endPos = text.length();
							}
							// Check for URL label
							if (text.indexOf("[", startPos) == endPos + 1) {
								endPos = text.indexOf("]", startPos) + 1;
							}
						} else {
							endPos = text.indexOf(styStrings[x])
									 + styStrings[x].length();
						}
					}
				}
			}
			// We now have a start and end position of the first colored block
			if (startPos == -1){ // No colors need to be added.
				output.addStyledText(text, defaultStyle);
				text = "";
			} else {
				// If there's leading text, add it here with default styling
				// and then trim it from the beginning.
				if(startPos != 0){
					output.addStyledText(text.substring(0, startPos), defaultStyle);
					text = text.substring(startPos);
				}
				// Get only what we're going to add.
				String addtext = text.substring(0, (endPos - startPos));
				// Find out what style it needs to be, then add it.
				for(int x = 0; x < styStrings.length; x++){
					if(addtext.startsWith(styStrings[x])){
						output.addStyledText(addtext, styInts[x]);
					}
				}
				// remove the text we just added from the line.
				text = text.substring(addtext.length());
				// reset our position so we parse for the next occurance
				startPos = -1;
			}
		}
		return output;
	}
	
	/**
		Adds another session to the list of sessions this poster will respond
		to.
		@param session The new session to respond to.
	*/
	public void addResponseSession(String session){
		if(sessionVect.size() >= 0){
			sessionVect.addElement(session);
		}
	}
	
	/**
		@param session The session to test if this poster responds to.
		@return <code>true</code> if this poster responds to <code>session
		</code>.
	*/
	public boolean respondsToSession(String session){
		// If any of the registered sessions match, return a true
		for(int x = 0; x < sessionVect.size(); x++){
			if(((String)sessionVect.elementAt(x)).equals(session)){
				return true;
			}
		}
		if(session.equals("") && (sessionVect.size() == 0)){
				return true;
		} else {
			return false;
		}
	}
	
	/**
		Removes all response session from this Poster - making it respond to all
		messages sent to it.
	*/
	public void clearResponseSessions(){
		while(sessionVect.size() > 0){
			sessionVect.removeElementAt(0);
		}
	}
	
	/**
		@return a <code>Vector</code> of <code>String</code>s that this poster
		responds to.
	*/
	public Vector getResponseSessions(){
		return sessionVect;
	}
	
	/**
	 * Sets the Font used for the ChatMessagingPanel this poster is responsible for.
	 */
	public void setChatFont(Font chatFont){
		chatMessageTarget.setChatFont(chatFont);
	}
	
	/**
	 * Sets weather or not the miniBrowser should be used.
	 */
	public void useMiniBrowser(boolean use){
		chatMessageTarget.setUseMiniBrowser(use);
	}
	
	/**
	 * updates the component UI.
	 */
	public void updateLafSetting(){
		chatMessageTarget.updateLafSetting();
	}
}
