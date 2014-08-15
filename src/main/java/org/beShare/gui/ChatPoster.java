//package org.beShare.gui;
//
//import org.beShare.data.ChatMessage;
//import org.beShare.gui.text.StyledString;
//
///**
// * ChatPoster - This class is responsible for writing new message to a
// * <code>ChatMessagingPanel</code>.
// * <p/>
// * TODO: Refactor this to encapsulate the ChatDocument or to be part of the ChatDocument
// *
// * @author Bryan Varner
// */
//public class ChatPoster {
//
//	public ChatPoster() {
//	}
//
//	/**
//	 * Writes a new message to the <code>target</code> specified at the time of
//	 * construction.
//	 *
//	 * @param theMessage The new message to log.
//	 */
//	public void addMessage(final ChatMessage theMessage) {
//		// Create the StyledString here, since we know we're going to need one.
//		StyledString chatString = new StyledString();
//		switch (theMessage.getType()) {
//			case ChatMessage.LOG_UPLOAD_EVENT_MESSAGE: {
//				// System Message
//				chatString.addStyledText("System: ", StyledString.SYSTEM_MESSAGE);
//				chatString.addStyledText(theMessage.getMessage() + "\n"
//						                        , StyledString.PLAIN_MESSAGE_STYLE);
//			}
//			break;
//			case ChatMessage.LOG_WATCH_PATTERN_MATCH: {
//				// A remote user said something.
//				if (theMessage.getMessage().startsWith("/me")) {
//					chatString.addStyledText("Action: "
//							                        , StyledString.USER_ACTION);
//					chatString.addStyledText(userDataModel.findNameBySession(theMessage.getSession())
//							                         + theMessage.getMessage().substring(3) + "\n"
//							                        , StyledString.WATCH_PATTERN);
//				} else {
//					chatString.addStyledText("(" + theMessage.getSession() + ") "
//							                         + userDataModel.findNameBySession(theMessage.getSession())
//							                         + ": ", StyledString.REMOTE_USER);
//					if (theMessage.isPrivate()) {
//						chatString.concatStyledText(parseStringForStyles(theMessage.getMessage() + "\n"
//								                                                , StyledString.PRIVATE));
//					} else {
//						chatString.concatStyledText(parseStringForStyles(theMessage.getMessage() + "\n"
//								                                                , StyledString.WATCH_PATTERN));
//					}
//				}
//			}
//			break;
//			case ChatMessage.LOG_CLEAR_LOG_MESSAGES: {
//				chatMessageTarget.clearText();
//			}
//		}
//		chatMessageTarget.addText(chatString);
//		chatMessageTarget.scrollText();
//	}
//
//}
