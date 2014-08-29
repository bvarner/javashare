package org.beShare.gui;

import org.beShare.data.BeShareUser;
import org.beShare.data.FilteredUserDataModel;
import org.beShare.gui.text.ClickLink;
import org.beShare.gui.text.StyledString;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import java.awt.*;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p>ChatDocument - Defines the document styles for the Chat log.
 *
 * @author Bryan Varner
 */
public class ChatDocument extends DefaultStyledDocument {
	List<ClickLink> links = new ArrayList<>();
	FilteredUserDataModel filteredUserDataModel;
	SimpleDateFormat sdf = new SimpleDateFormat("'['M/dd HH:mm'] '");

	/**
	 * Construct a new Document, and set weather or not to use the MiniBrowser.
	 */
	public ChatDocument(FilteredUserDataModel filteredUserDataModel) {
		super();
		this.filteredUserDataModel = filteredUserDataModel;
	}

	public void addSystemMessage(final String message) {
		appendString(new StyledString("System: ", StyledString.SYSTEM_MESSAGE).append(message));
	}

	public void addWarningMessage(final String message) {
		appendString(new StyledString("Warning: ", StyledString.SYSTEM_ERROR).append(message));
	}

	public void addErrorMessage(final String message) {
		appendString(new StyledString("Error: ", StyledString.SYSTEM_ERROR).append(message, StyledString.SYSTEM_ERROR));
	}

	/**
	 * Determines if this ChatDocument would consume a message from the given sourceSessionId with the given private flag.
	 * @param sourceSessionId
	 * @return
	 */
	public boolean willConsumePrivate(final String sourceSessionId) {
		return filteredUserDataModel.isFiltering() && filteredUserDataModel.getSessionIds().contains(sourceSessionId);
	}

	public boolean addRemoteChatMessage(final String message, final String sourceSessionId, final boolean isPrivate) {
		// If the message is private, make sure we're the intended target.
		// OR if we're not filtering... display it.
		if ((isPrivate && filteredUserDataModel.getSessionIds().contains(sourceSessionId)) || !filteredUserDataModel.isFiltering()) {
			SimpleAttributeSet remoteStyle = StyledString.PLAIN;
			if (isPrivate) {
				remoteStyle = StyledString.PRIVATE;
			}

			StyledString styledString;
			// Find the real user and do the things...
			BeShareUser remoteUser = filteredUserDataModel.getUserDataModel().getUser(sourceSessionId);
			if (remoteUser != null) {
				// /me & /action replacement.
				String lowerFirstToken = message.split(" ")[0].trim().toLowerCase();
				if (lowerFirstToken.equals("/me")) {
					styledString = new StyledString("Action: ", StyledString.USER_ACTION).append(remoteUser.getName()).append(" " + message.substring((4)), remoteStyle);
				} else if (lowerFirstToken.equals("/action")) {
					styledString = new StyledString("Action: ", StyledString.USER_ACTION).append(remoteUser.getName()).append(" " + message.substring((8)), remoteStyle);
				} else {
					styledString = new StyledString("(" + sourceSessionId + ") " + remoteUser.getName() + ": ", StyledString.REMOTE_USER).append(message, remoteStyle);
				}
			} else {
				styledString =
						new StyledString("Could not find user for sessionId : " + sourceSessionId + " when posting remote chat.", StyledString.SYSTEM_ERROR);
			}

			appendString(styledString);
			return true;
		}
		return false;
	}

	public void addEchoChatMessage(final String message, final boolean isPrivate, final String localSessionId, final String localUserName, final String[] privateSessionIds) {
		StyledString styledString;
		SimpleAttributeSet defaultStyle = StyledString.PLAIN;
		if (isPrivate) {
			defaultStyle = StyledString.PRIVATE;
		}

		String lowerFirstToken = message.split(" ")[0].trim().toLowerCase();
		if (lowerFirstToken.equals("/me")) {
			styledString = new StyledString("Action: ", StyledString.USER_ACTION).append(localUserName + " ").append(" " + message.substring((4)), defaultStyle);
		} else if (lowerFirstToken.equals("/action")) {
			styledString = new StyledString("Action: ", StyledString.USER_ACTION).append(localUserName + " ").append(" " + message.substring((8)), defaultStyle);
		} else if (privateSessionIds == null || privateSessionIds.length == 0) {
			styledString = new StyledString("(" + localSessionId + ") " + localUserName + ": ", StyledString.LOCAL).append(message, defaultStyle);
		} else {
			// Build a list of the usernames in the private chat.
			StringBuilder userList = new StringBuilder("");
			for (int i = 0; i < privateSessionIds.length; i++) {
				userList.append(filteredUserDataModel.getUserDataModel().findNameBySession(privateSessionIds[i]));
				if (i + 1 < privateSessionIds.length) {
					userList.append(" ");
				}
			}
			styledString = new StyledString("(" + localSessionId + ") " + localUserName + " -> (" + userList + "): ", StyledString.LOCAL).append(message, defaultStyle);
		}
		appendString(styledString);
	}

	/**
	 * <p>Appends the StyledString <code>textString</code> to the end of the
	 * document with the sytles that were defined when text was added to <code>
	 * textString</code>.
	 *
	 * @param textString the StyledString to add to this document.
	 */
	private void appendString(StyledString textString) {
		writeLock();
		try {
			// Log the timestamp.
			insertString(getLength(), sdf.format(new Date()), StyledString.REMOTE_USER);

			Iterator<Map.Entry<String, SimpleAttributeSet>> segments = textString.entrySet().iterator();
			while (segments.hasNext()) {
				Map.Entry<String, SimpleAttributeSet> segment = segments.next();
				if (segment.getValue().equals(StyledString.URI) && segments.hasNext()) {
					Map.Entry<String, SimpleAttributeSet> labelSegment = segments.next();

					// The current 'segment' is the URL, add a ClickLink definition for the size of the label.
					links.add(new ClickLink(getLength(), getLength() + labelSegment.getKey().length(), segment.getKey()));

					insertString(getLength(), labelSegment.getKey(), segment.getValue());
				} else {
					insertString(getLength(), segment.getKey(), segment.getValue());
				}
			}

			insertString(getLength(), "\n", StyledString.PLAIN);
		} catch (Exception ex) {
			System.out.println("Error occurred updating ChatDocument: " + ex);
		} finally {
			writeUnlock();
		}
	}

	public FilteredUserDataModel getFilteredUserDataModel() {
		return filteredUserDataModel;
	}

	/**
	 * Empties the document
	 */
	public void clear() {
		writeLock();
		try {
			links.clear();
			replace(0, getLength(), "", null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		} finally {
			writeUnlock();
		}
	}

	/**
	 * Checks to see if <code>clickLocation</code> is within the any of the
	 * ranges for any links within the vector of <code>ClickLinks</code>
	 */
	public void textClicked(int clickLocation) {
		for (ClickLink link : links) {
			if ((clickLocation > (link.getStart() - 1)) && (clickLocation < (link.getEnd() + 1))) {
				if (link.getURL().startsWith("http://")) {
					if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
						try {
							Desktop.getDesktop().browse(new URI(link.getURL().trim()));
						} catch (Exception ex) {
							System.err.println("Failed to browse to URL: " + link.getURL().trim());
						}
					}
				} else if (link.getURL().startsWith("mailto:")) {
					try {
						Desktop.getDesktop().mail(new URI(link.getURL().trim()));
					} catch (Exception ex) {
						System.err.println("Failed to browse to URL: " + link.getURL().trim());
					}
				}
			}
		}
	}
}
