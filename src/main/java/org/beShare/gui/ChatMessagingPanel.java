/*	ChangeLog:
		1.1 - Added addText(StyledString) as part of the chat logging overhaul
			The hope is to speed things up and eliminate the need for our
			rudimentary locking by making everything thread-safe.
		1.1.5 - Removed the original addText, and all Style Specifiers.
			Also removed the locking boolean.
		1.2 - Now uses custom JTextField, ChatInputLine which traps the TabKey correctly.
		1.2.1 - Chat log pane now has a focus adapter to keep it from getting focus... sortof.
			I don't feel like sub-classing unless I have to.
*/
package org.beShare.gui;

import org.beShare.data.ChatMessage;
import org.beShare.data.UserHashAccessor;
import org.beShare.event.ChatMessageListener;
import org.beShare.gui.text.StyledString;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Stack;
import java.util.Vector;
/**
	ChatMessagingPanel - This panel is responsible to firing off new
	ChatMessages from the local user, and for the adding of text to the chat
	log.
	
	Last Update: 6.5.2002
	
	@author Bryan Varner
	@version 1.2.1
*/
public class ChatMessagingPanel extends JPanel implements ActionListener,
															KeyListener,
															AdjustmentListener{
	JLabel					chatLabel;
	ChatInputLine			chatInput;
	JPanel					inputPanel;
	ChatMessageListener		messageTarget;
	
	Stack	 				recentLines;
	int						lineIndex;
	
	JTextPane				chatLog;
	ChatDocument			chatDoc;
	JScrollPane				logScrollPane;
	
	UserHashAccessor		userHashData;
	
	Vector					privateSessionVect;
	Timer					scrollTimer;
	int						scrollUpAdjustments;
	int						previousScrollPosition;
	
	/**
		Creates a new <code>ChatMessagingPanel</code> that sends new message to
		<code>target</code> and retreives it's user data from <code>hashAccess
		</code>
	*/
	public ChatMessagingPanel(ChatMessageListener target,
								UserHashAccessor hashAccess){
		super();
		scrollUpAdjustments = 0;
		previousScrollPosition = 0;
		userHashData = hashAccess;
		recentLines = new Stack();
		lineIndex = 0;
		messageTarget = target;
		
		this.setLayout(new BorderLayout());
		
		inputPanel = new JPanel();
		inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
		chatLabel = new JLabel("Chat: ");
		chatInput = new ChatInputLine(25);
		chatInput.setRequestFocusEnabled(true);
		chatInput.addActionListener(this);
		chatInput.addKeyListener(this);
		
		inputPanel.add(chatLabel);
		inputPanel.add(chatInput);
		
		/**
			Inner Class to implement a MouseAdapter for the chatLog
		*/
		class LinkClickListener extends MouseAdapter {
			public void mouseClicked(MouseEvent e){
				chatDoc.textClicked(chatLog.viewToModel(e.getPoint()));
			}
		}
		
		LinkClickListener URLClickListener = new LinkClickListener();
		
		chatDoc = new ChatDocument(true);
		chatLog = new JTextPane(chatDoc);
		
		// -- Blocked Selected Text being visible in Windows -- //
		//chatLog.addFocusListener(new FocusAdapter(){
		//		public void focusGained(FocusEvent e){
		//			FocusManager.getCurrentManager().focusNextComponent(chatLog);
		//		}
		//	});
		
		chatLog.setMargin(new Insets(2,2,2,2));
		chatLog.setEditable(false);
		chatLog.addMouseListener(URLClickListener);
		logScrollPane = new JScrollPane(chatLog);
		
		this.add(logScrollPane, BorderLayout.CENTER);
		this.add(inputPanel, BorderLayout.SOUTH);
		privateSessionVect = new Vector();
		// Set up the thread for scrolling the log. It has a delay of .01 sec.
		scrollTimer = new Timer(0010, new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				scrollText();
				scrollTimer.stop();
			}    
		});
		try {
			logScrollPane.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
		} catch (NoSuchMethodError nsme){
		}
		logScrollPane.getVerticalScrollBar().addAdjustmentListener(this);
	}
	
	/**
		Creates a new <code>ChatMessagingPanel</code> that sends new message to
		<code>target</code> and retreives it's user data from <code>hashAccess
		</code>. This constructor also adds <code>pSession</code> to the list of
		sessions this will privately send messages to.
	*/
	public ChatMessagingPanel(ChatMessageListener target,  
								UserHashAccessor hashAccess, String pSession){
		this(target, hashAccess);
		privateSessionVect.addElement(pSession);
	}
	
	/**
		Extension of the normal constructor, with support of minibroser on/off
	*/
	public ChatMessagingPanel(ChatMessageListener target,
		UserHashAccessor hashAccess, boolean miniBrowser){
		this(target, hashAccess);
		chatDoc.setUseMiniBrowser(miniBrowser);
	}
	
	/**
		Creates a new <code>ChatMessagingPanel</code> that sends new message to
		<code>target</code> and retreives it's user data from <code>hashAccess
		</code>. This constructor also adds <code>pSession</code> to the list of
		sessions this will privately send messages to. <code>miniBrowser</code>
		specifies weather or not to auto-launch http:// links in the minibrowser.
	*/
	public ChatMessagingPanel(ChatMessageListener target,  
								UserHashAccessor hashAccess, String pSession,
								boolean miniBrowser){
		this(target, hashAccess);
		privateSessionVect.addElement(pSession);
		chatDoc.setUseMiniBrowser(miniBrowser);
	}
	
	/**
		Set's the Status of weather or not we should use the MiniBrowser.
	*/
	public void setUseMiniBrowser(boolean miniBrowser){
		chatDoc.setUseMiniBrowser(miniBrowser);
	}
	
	/**
		Has the chat line text field request focus.
	*/
	public void requestChatLineFocus(){
		chatInput.requestFocus();
	}
	
	/**
		Responds to any actions that are sent to this panel.
		The only internally registered ActionEvent source is the
		chat text box. This gets the text, and sends a ChatMessage to
		the ChatMessageListener specified at construction time.
	*/
	public void actionPerformed(ActionEvent e){
		if (e.getSource() == chatInput){
			if(chatInput.getText().length() > 0){
				String message = "";
				// If the vect is > 0 then this is a private chat session.
				if((privateSessionVect.size() > 0) 
					&& (! chatInput.getText().startsWith("/"))){
					// Send out the local log message
					// This message will be Intercepted by AppPanel
					// and fired back to ONLY the registered posters
					// It will NOT be sent out the network connection.
					message = message + chatInput.getText();
					messageTarget.chatMessage(
						new ChatMessage("" , message
						, false
						, ChatMessage.PRIVATE_LOCAL_LOG_ONLY
						, true
						, (String)privateSessionVect.elementAt(0)));
					// Fire off the messages to the recipients without logging.
					// Because the Poster does not respond to PRIVATE_NO_LOG
					// these messages are not displayed locally.
					// They are still fired to any paoster, but no action
					// is taked by the poster.
					for(int x = 0; x < privateSessionVect.size(); x++){
						message = "/MSG " + userHashData.findNameBySession((String)privateSessionVect.elementAt(x)) + " ";
						message = message + chatInput.getText();
						messageTarget.chatMessage(
							new ChatMessage((String)privateSessionVect.elementAt(x) , message
							, false
							, ChatMessage.PRIVATE_NO_LOG
							, true
							, (String)privateSessionVect.elementAt(x)));
					}
				// This is a '/' command from this target window.
				} else if ((privateSessionVect.size() > 0) && 
							(chatInput.getText().toUpperCase().startsWith("/CLEAR"))){
					messageTarget.chatMessage(
						new ChatMessage("", chatInput.getText()
						, false
						, ChatMessage.LOG_LOCAL_USER_CHAT_MESSAGE
						, true
						, (String)privateSessionVect.elementAt(0)));
				// This is NOT a private chat - send out the message.
				} else {
					messageTarget.chatMessage(
						new ChatMessage("", chatInput.getText()
						, false
						, ChatMessage.LOG_LOCAL_USER_CHAT_MESSAGE
						, true
						, ""));
				}
				recentLines.push(chatInput.getText());
				lineIndex = recentLines.size();
				chatInput.setText("");
			}
		} else if(e.getActionCommand().startsWith("Private Chat with ")){
			// Fire the message for a new private chat off!
			messageTarget.chatMessage(
				new ChatMessage("", "/priv "
									+ e.getActionCommand().substring(18)
				, false
				, ChatMessage.LOG_LOCAL_USER_CHAT_MESSAGE
				, true
				, ""));
		} else if(e.getActionCommand().startsWith("Watch User: ")){
			// Watch User!!!
			messageTarget.chatMessage(
				new ChatMessage("", "/watch "
				+ e.getActionCommand().substring(12)
				, false
				, ChatMessage.LOG_LOCAL_USER_CHAT_MESSAGE
				, true
				, ""));
		}
	}
	
	/**
		Here because we implement Key Listener.
	*/
	public void keyPressed(KeyEvent e){
	}
	
	/**
		Responds to vaious events on the panel and implements nice UI things.
	*/
	public void keyReleased(KeyEvent e){
		if ((e.getKeyCode() == KeyEvent.VK_KP_UP) 
			|| (e.getKeyCode() == KeyEvent.VK_UP))
		{
			if(lineIndex > 0 && recentLines.size() > 0){
				lineIndex--;
				chatInput.setText((String)recentLines.elementAt(lineIndex));
			}
		} else if ((e.getKeyCode() == KeyEvent.VK_KP_DOWN) ||
			(e.getKeyCode() == KeyEvent.VK_DOWN))
		{
			if (recentLines.size() > lineIndex + 1){
				lineIndex++;
				chatInput.setText((String)recentLines.elementAt(lineIndex));
			} else {
				lineIndex = recentLines.size();
				chatInput.setText("");
			}
		} else if ((e.getKeyCode() == KeyEvent.VK_F1) || (e.getKeyCode() == KeyEvent.VK_TAB)){
			String completedName = "";
			int lastSpace = chatInput.getText().lastIndexOf(" ");
			if (lastSpace == -1){
				completedName = userHashData.findCompletedName(chatInput.getText().substring(0));
			} else {
				completedName = userHashData.findCompletedName(chatInput.getText().substring(lastSpace + 1));
			}
			chatInput.setText(chatInput.getText().substring(0, lastSpace + 1) + completedName);
		}
	}
	
	/**
		Here because we implement Key Listener.
	*/
	public void keyTyped(KeyEvent e){
	}
	
	/**
		Add the text from StyledString <code>textString</code> with the styles
		specified by <code>textString's</code> internal Styles.
		
		@param textString the StyledString to add
	*/
	public void addText(StyledString textString){
		chatDoc.appendString(textString);
		scrollTimer.start();
	}
	
	/**
		Clears the chat Log pane.
	*/
	public void clearText(){
		chatLog.setText("");
		chatDoc.clearURLs();
	}
	
	/**
		Adds a private session to the private session vector.
		@param sessionID The Session to add.
	*/
	public void addPrivateSession(String sessionID){
		privateSessionVect.addElement(sessionID);
	}
	
	/**
		Removes a session from the private Session vector
		@param sessionID The Session to remove.
	*/
	public void removePrivateSession(String sessionID){
		privateSessionVect.removeElement(sessionID);
	}
	
	/**
		Clears the vector of private sessions.
	*/
	public void clearPrivateSessions(){
		while(privateSessionVect.size() > 0){
			privateSessionVect.removeElementAt(0);
		}
	}
	
	/**
		Returns the timer used to scroll this Chat Log.
		@return The Swing Timer used to scroll the log.
	*/
	public Timer getScrollTimer(){
		return scrollTimer;
	}
	
	/**
		Determines if the local log pane needs to be scrolled. If it does, then
		it scrolls it to the bottom.
	*/
	public void scrollText(){
		JScrollBar vertBar = logScrollPane.getVerticalScrollBar();
		if(scrollUpAdjustments <= 10){
			vertBar.setValue(vertBar.getMaximum());
			scrollUpAdjustments = 0;
		}
	}
	
	/**
		This is called when the vertical ScrollBar of the chat log is moved.
		It's responsible for changing the background color.
	*/
	public void adjustmentValueChanged(AdjustmentEvent e){
		// Check our adjuster...
		if(e.getValue() <= previousScrollPosition){
			scrollUpAdjustments ++;
		} else if(scrollUpAdjustments > 0){
			scrollUpAdjustments --;
		} else {
			scrollUpAdjustments = 0;
		}
		// See if we're at the bottom... if we are, we'll set the adjustments to
		// 0 just for safe measure...
		Adjustable vertBar = e.getAdjustable();
		if(e.getValue() == vertBar.getMaximum() - vertBar.getVisibleAmount()){
			scrollUpAdjustments = 0;
		}
		previousScrollPosition = e.getValue();
		
		if (scrollUpAdjustments >= 10){
			chatLog.setBackground(new Color(245, 255, 255));
		} else {
			chatLog.setBackground(new Color(255, 255, 255));
		}
	}
	
	/**
		Crazy implementation for the copy menu.
		This this will copy either from the chat log or the chat input.
	*/
	public void copy(){
		if(chatInput.getSelectionStart() != chatInput.getSelectionEnd()){
			chatInput.copy();
		} else {
			chatLog.copy();
		}
	}
	
	/**
		Support for the paste menu, it pastes to the only logical place - the
		chat input field.
	*/
	public void paste(){
		chatInput.paste();
	}
	
	/**
		Cuts selected text in the same manner as <code>copy()</code>.
	*/
	public void cut(){
		if(chatInput.getSelectionStart() != chatInput.getSelectionEnd()){
			chatInput.cut();
		} else {
			chatLog.copy();
		}
	}
	
	/**
	 * Sets the font used for the chat input and chatlog.
	 */
	public void setChatFont(Font chatFont){
		try {
			chatLog.setFont(chatFont);
			chatInput.setFont(chatFont);
			// Invalidate Layout?
		} catch (NullPointerException npe){
		}
		this.validate();
	}
	
	/**
	 * Forces a UI update.
	 */
	public void updateLafSetting(){
		SwingUtilities.updateComponentTreeUI(this.getRootPane().getParent());
	}
}
