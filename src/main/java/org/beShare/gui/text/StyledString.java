package org.beShare.gui.text;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.util.Vector;

/**
	StyledString - Part of the chat text display over-haul
	
	This class keeps two vectors. One of Strings, the other of styles. All
	Strings added to an instance of this class must have an accompanying style.
	Simple vector navigation is included in the external interface. It should be
	enough for any situation we'll encounter.
	
	Last Update: 4.23.2002
	
	@author Bryan Varner
	@version 1.0
*/

public class StyledString {
	Vector		stringVect;
	Vector 		styleVect;
	
	SimpleAttributeSet plainStyle;
	SimpleAttributeSet localNameStyle;
	SimpleAttributeSet systemMessageStyle;
	SimpleAttributeSet userNameStyle;
	SimpleAttributeSet localSpeakStyle;
	SimpleAttributeSet userActionStyle;
	SimpleAttributeSet privateStyle;
	SimpleAttributeSet sysErrStyle;
	SimpleAttributeSet urlStyle;
	
	public static final int SYSTEM_MESSAGE_STYLE = 0;
	public static final int PLAIN_MESSAGE_STYLE = 1;
	public static final int REMOTE_USER_NAME_STYLE = 2;
	public static final int LOCAL_USER_NAME_STYLE = 3;
	public static final int LOCAL_USER_NAME_SAID_STYLE = 4;
	public static final int USER_ACTION_STYLE = 5;
	public static final int PRIVATE_MESSAGE_STYLE = 6;
	public static final int SYSTEM_ERROR_STYLE = 7;
	public static final int WATCH_PATTERN_STYLE = 7;
	public static final int URL_STYLE = 8;
	
	int currentElement;
	
	/**
		Our lovely Constructor. This will initialize all our styles, and create
		the empty Vectors.
	*/
	public StyledString(){
		stringVect = new Vector();
		styleVect = new Vector();
		
		currentElement = 0;
		
		plainStyle = new SimpleAttributeSet();
		StyleConstants.setBold(plainStyle, false);
		StyleConstants.setItalic(plainStyle, false);
		StyleConstants.setForeground(plainStyle, Color.black);
		
		localNameStyle = new SimpleAttributeSet(plainStyle);
		StyleConstants.setForeground(localNameStyle, new Color(255, 128, 0));
		
		systemMessageStyle = new SimpleAttributeSet(plainStyle);
		StyleConstants.setBold(systemMessageStyle, true);
		StyleConstants.setForeground(systemMessageStyle, new Color(0, 0, 128));
		
		userNameStyle = new SimpleAttributeSet(plainStyle);
		StyleConstants.setBold(userNameStyle, true);
		
		localSpeakStyle = new SimpleAttributeSet(userNameStyle);
		StyleConstants.setForeground(localSpeakStyle, new Color(0, 128, 0));
		
		userActionStyle = new SimpleAttributeSet(userNameStyle);
		StyleConstants.setForeground(userActionStyle, new Color(128, 0 , 128));
		
		privateStyle = new SimpleAttributeSet(plainStyle);
		StyleConstants.setForeground(privateStyle, new Color(0, 128, 128));
		
		sysErrStyle = new SimpleAttributeSet(systemMessageStyle);
		StyleConstants.setForeground(sysErrStyle, new Color(128, 0, 0));
		
		urlStyle = new SimpleAttributeSet(plainStyle);
		StyleConstants.setForeground(urlStyle, new Color(0, 0, 255));
		StyleConstants.setUnderline(urlStyle, true);
	}
	
	/**
		Adds <code>text</code> with <code>type</code> of style. See the public
		constants in this class for valid <code>type</code>s.
	*/
	public void addStyledText(String text, int type){
		stringVect.addElement(text);
		switch(type){
			case SYSTEM_MESSAGE_STYLE:{
				styleVect.addElement(systemMessageStyle);
			} break;
			
			case PLAIN_MESSAGE_STYLE:{
				styleVect.addElement(plainStyle);
			} break;
			
			case REMOTE_USER_NAME_STYLE:{
				styleVect.addElement(userNameStyle);
			} break;
			
			case LOCAL_USER_NAME_STYLE:{
				styleVect.addElement(localSpeakStyle);
			} break;
			
			case USER_ACTION_STYLE:{
				styleVect.addElement(userActionStyle);
			}break;
			
			case PRIVATE_MESSAGE_STYLE:{
				styleVect.addElement(privateStyle);
			}break;
			
			case SYSTEM_ERROR_STYLE:{
				styleVect.addElement(sysErrStyle);
			}break;
			
			case LOCAL_USER_NAME_SAID_STYLE:{
				styleVect.addElement(localNameStyle);
			}break;
			
			case URL_STYLE:{
				styleVect.addElement(urlStyle);
			}break;
		}
		currentElement = 0;
		return;
	}
	
	public void addStyledText(String text, SimpleAttributeSet attribs){
		stringVect.addElement(text);
		styleVect.addElement(attribs);
		currentElement = 0;
	}
	
	/**
		Appends all strings and styles from <code>copy</code> to this
		StyledString.
	*/
	public void concatStyledText(StyledString copy){
		copy.moveFirstSegment();
		for (int x = 0; x < copy.getSegmentCount(); x++){
			stringVect.addElement(copy.getStringSegment());
			styleVect.addElement(copy.getAttributeSet());
			copy.moveNextSegment();
		}
		currentElement = 0;
	}
	
	/**
		Gets the current string segment. By default, the constructor sets this
		to the first segment.
		
		@return The String if there is one, "" if not.
	*/
	public String getStringSegment(){
		try {
			return (String)stringVect.elementAt(currentElement);
		} catch (ArrayIndexOutOfBoundsException aioobe) {
			return "";
		}
	}
	
	/**
		Repositions both Vectors to get the first segment with the next call to
		either's get function.
	*/
	public void moveFirstSegment(){
		currentElement = 0;
	}
	
	/**
		Gets the SimpleAttributeSet for the current String.
	*/
	public SimpleAttributeSet getAttributeSet(){
		try {
			return (SimpleAttributeSet)styleVect.elementAt(currentElement);
		} catch (ArrayIndexOutOfBoundsException aioobe) {
			return null;
		}
	}
	
	/**
		Returns true if the current segment is styled as a link.
	*/
	public boolean isURLStyle() {
		if (styleVect.elementAt(currentElement).toString().equals(urlStyle.toString())){
			return true;
		} else {
			return false;
		}
	}
	
	/**
		Moves the vector to the next segment.
		Careful here, it is possible to move beyond the end of the vector.
	*/
	public void moveNextSegment(){
		currentElement++;
	}
	
	/**
	 * Moves the vector to the previous segment.
	 * Careful here, you can re-wind out bounds.
	 */
	public void movePreviousSegment(){
		currentElement--;
	}
	
	/**
		Returns the size of the Vectors. Useful for iteration from an external
		source.
	*/
	public int getSegmentCount(){
		return stringVect.size();
	}
}
