/*
ChangeLog
	1.1 - Added appendString(StyledString) as part of the chat logging
		overhaul.
	
	1.1.5 - Removed all SimpleAttributeSets from this class, moved all style
		handling into StyledString.
	1.2 - Added Vector of ClickLinks and supporting methods.
		clearURLs(), textClicked() and add Anything with a URL style into
		the linkVect.
	2.0 - Changed from BeShareChatDocument to the more appropriate ChatDocument.
		Cleaned up code and formatting.
*/
package org.beShare.gui;

import org.beShare.gui.text.ClickLink;
import org.beShare.gui.text.StyledString;

import javax.swing.*;
import javax.swing.text.DefaultStyledDocument;
import java.util.Vector;

/**
	<p>ChatDocument - Defines the document styles for the Chat log.
	
	@author Bryan Varner
	@version 2.0 - 7.15.2002
*/
public class ChatDocument extends DefaultStyledDocument {
	Vector linkVect;
	String currentURL;
	boolean useMiniBrowser;
	
	/**
	 * A class to launch the browser in a separate thread from the 'GUI Thread'
	 */
	class browserThread extends org.beShare.gui.swingAddons.SwingWorker {
		public Object construct() {
			try{
				HttpLinkViewer.createHttpLinkViewer(currentURL);
			} catch (Exception e){
				JOptionPane.showMessageDialog(null,
					"An Error occured while opening the URL. THis page cannot be displayed.",
					"Mini-Browser Error", JOptionPane.ERROR_MESSAGE);
			}
			return null; // Not Used
		}
	}
	
	
	/**
	 *Default Constructor. Calls the super, and creates our vector of links.
	 */
	public ChatDocument(){
		super();
		linkVect = new Vector();
		currentURL = "";
		useMiniBrowser = false;
	}
	
	/**
	 * Construct a new Document, and set weather or not to use the MiniBrowser.
	 */
	public ChatDocument(boolean mb){
		this();
		useMiniBrowser = mb;
	}
	
	/**
	 *	<p>Appends the StyledString <code>textString</code> to the end of the
	 *	document with the sytles that were defined when text was added to <code>
	 *	textString</code>.
	 *	
	 *	@param textString the StyledString to add to this document.
	 */
	public void appendString(StyledString textString){
		textString.moveFirstSegment();
		for (int x = 0; x < textString.getSegmentCount(); x++){
			try{
				if(textString.isURLStyle()){
					// Check for URL [label] syntax...
					if (textString.getStringSegment().endsWith("]")){
						String url = textString.getStringSegment().substring(0, textString.getStringSegment().indexOf("[") - 1);
						linkVect.addElement(new ClickLink(getLength(), getLength() + url.length(), url));
						insertString(getLength(),
								textString.getStringSegment().substring(textString.getStringSegment().indexOf("[") + 1,
																		textString.getStringSegment().indexOf("]")),
								textString.getAttributeSet());
					} else {
						// Insert the full URL...
						linkVect.addElement(new ClickLink(getLength(), getLength() +
								textString.getStringSegment().length(),
								textString.getStringSegment().trim()));
						insertString(getLength(), textString.getStringSegment(),
									textString.getAttributeSet());
					}
				} else {
					insertString(getLength(), textString.getStringSegment(),
									textString.getAttributeSet());
				}
				textString.moveNextSegment();
			} catch (Exception e){System.err.println(e.toString());}
		}
	}
	
	
	/**
	 *	Clears the array of URLS.
	 */
	public void clearURLs(){
		while (! linkVect.isEmpty()){
			linkVect.removeElementAt(0);
		}
	}
	
	/**
	 * Sets the minibrowser usage status.
	 */
	public void setUseMiniBrowser(boolean use){
		useMiniBrowser = use;
	}
	
	/**
	 *	Checks to see if <code>clickLocation</code> is within the any of the
	 *	ranges for any links within the vector of <code>ClickLinks</code>
	 */
	public void textClicked(int clickLocation){
		for (int linkCounter = 0; linkCounter < linkVect.size(); linkCounter++){
			ClickLink tempLink = (ClickLink)linkVect.elementAt(linkCounter);
			if ((clickLocation > (tempLink.getStart() - 1)) 
				&& (clickLocation < (tempLink.getEnd() + 1)))
			{
				if (tempLink.getURL().startsWith("http://")){
					currentURL = tempLink.getURL().trim();
					// This might not compile on non-macOS Systems.
					if (useMiniBrowser){
						browserThread webBrowser = new browserThread();
						webBrowser.start();
					} else {
//						try{
//							com.apple.mrj.MRJFileUtils.openURL(currentURL);
//						} catch (Exception e){ // No such Method... no such class def... catch them all!
//						}
					}
				} else if (tempLink.getURL().startsWith("audio://")){
					currentURL = tempLink.getURL().trim();
//					try{
//						com.apple.mrj.MRJFileUtils.openURL(currentURL);
//					} catch (Exception e){ // No such Method... no such class def... catch them all!
//					}
				}
				// Check for other types.
			}
		}
	}
}
