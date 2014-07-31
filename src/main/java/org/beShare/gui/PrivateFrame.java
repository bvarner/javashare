package org.beShare.gui;

import org.beShare.data.BeShareUser;
import org.beShare.data.UserHashAccessor;
import org.beShare.event.ChatMessageListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.StringTokenizer;
import java.util.Vector;

/**
	PrivateFrame - A frame (window) for Private chat sessions.
	
	Last Update: 2-28-2002
				5.9.2002 - Added OneTouchExpandable(true) to the Split pane.
							Also made the window opened event request that the chat input
							receive the focus.
				8.3.2002 - Now reverse-lookups names in the chat with field.
	@author Bryan Varner
	@version 2.0
*/
public class PrivateFrame extends JFrame implements WindowListener,
														ActionListener{
	JPanel					mainPanel;
	JPanel					chatWithPanel;
	JLabel					chatWithLabel;
	JTextField				chatWithField;
	
	Vector					chatTableColNames;
	Vector					chatTableNames;
	
	UserTable				chatTable;
	UserTableModel			chatTableModel;
	JScrollPane				chatTableScroller;
	
	JSplitPane				chatTableMessageSplit;
	
	ChatMessagingPanel		chatPanel;
	ChatPoster		chatPrivPoster;
	
	ChatMessageListener		chatListener;
	UserHashAccessor		hashAccess;
	String					privSessions;
	
	/**
		Creates a new frame for a private chat session.
		@param userHash UserHashAccessor all user data is retrieved from
		@param chatTarget Where new messages are posted to and received from
		@param sessionID the initial sessionID to send and receive private
		messages from. 
	*/
	public PrivateFrame(UserHashAccessor userHash,
							ChatMessageListener chatTarget,
							String sessionID){
		super("Private Chat with " 
				+ userHash.findNameBySession(sessionID));
		ImageIcon JavaShareIcon = AppPanel.loadImage("Images/BeShare.gif", this);
		this.setIconImage(JavaShareIcon.getImage());
		
		this.addWindowListener(this);
		
		chatListener = chatTarget;
		hashAccess = userHash;
		privSessions = sessionID;
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		chatWithPanel = new JPanel();
		chatWithPanel.setLayout(
				new BoxLayout(chatWithPanel, BoxLayout.X_AXIS));
		
		chatWithLabel = new JLabel("Chat With: ");
		chatWithField = new JTextField(privSessions);
		chatWithField.addActionListener(this);
		
		chatWithPanel.add(chatWithLabel);
		chatWithPanel.add(chatWithField);
		
		chatPanel = new ChatMessagingPanel(chatListener,
													hashAccess,
													privSessions);
		chatPanel.setPreferredSize(new Dimension(400,150));
		
		chatPrivPoster = new ChatPoster(chatPanel,
													hashAccess,
													privSessions);
		chatListener.addChatPoster(chatPrivPoster);
		
		chatTableColNames = new Vector();
		chatTableColNames.addElement("Name");
		chatTableColNames.addElement("ID");
		
		chatTableNames = new Vector();
		
		chatTableModel = new UserTableModel(chatTableNames, 
												chatTableColNames);
		chatTable = new UserTable(chatTableModel);
		chatTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		chatTable.setPreferredScrollableViewportSize(new Dimension(150, 150));
		
		chatTableScroller = new JScrollPane(chatTable);
		
		BeShareUser initialUser = new BeShareUser(privSessions);
		initialUser.setName(hashAccess.findNameBySession(privSessions));
		chatTableModel.addUser(initialUser);
		
		
		chatTableMessageSplit = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT, chatPanel,
				chatTableScroller);
		chatTableMessageSplit.setOneTouchExpandable(true);
		
		try{
			chatTableMessageSplit.setResizeWeight(.80);
		} catch (NoSuchMethodError nsme){
		}

		mainPanel.add(chatWithPanel, BorderLayout.NORTH);
		mainPanel.add(chatTableMessageSplit, BorderLayout.CENTER);
		this.getContentPane().add(mainPanel);
		
		pack();
		
		try {
			GraphicsEnvironment systemGE
				= GraphicsEnvironment.getLocalGraphicsEnvironment();
				Rectangle screenRect = systemGE.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
				this.setBounds((screenRect.width / 2) - (this.getBounds().width / 2),
						(screenRect.height / 2) - (this.getBounds().height / 2),
						this.getBounds().width, this.getBounds().height);
		} catch (NoClassDefFoundError ncdfe){
		}
	}
	
	public PrivateFrame(UserHashAccessor userHash,
						ChatMessageListener chatTarget,
						String sessionID, Font chatFont){
		this(userHash, chatTarget, sessionID);
		if (chatFont != null){
			chatPanel.setChatFont(chatFont);
		}
	}
	
	public void windowActivated(WindowEvent e){
		chatPanel.requestChatLineFocus();
	}
	public void windowClosed(WindowEvent e){
		this.dispose();
	}
	public void windowClosing(WindowEvent e){
		chatPanel.getScrollTimer().stop(); // Stop the Scrolling thread.
		chatListener.removeChatPoster(chatPrivPoster);
	}
	public void windowDeactivated(WindowEvent e){}
	public void windowDeiconified(WindowEvent e){}
	public void windowIconified(WindowEvent e){}
	public void windowOpened(WindowEvent e){
		chatPanel.requestChatLineFocus();
	}
	
	public void actionPerformed(ActionEvent e){
		chatTableModel.clearTable();
		chatPrivPoster.clearResponseSessions();
		chatPanel.clearPrivateSessions();
		StringTokenizer sessionToke = 
				new StringTokenizer(chatWithField.getText(), ", ");
		this.setTitle("Private Chat with");
		while(sessionToke.hasMoreTokens()){
			String userSession = sessionToke.nextToken();
			try {
				Integer.parseInt(userSession);
			} catch (NumberFormatException nfe){
				// User name, non-numeric value!
				userSession = hashAccess.findSessionByName(userSession);
			}
			chatPrivPoster.addResponseSession(userSession);
			chatPanel.addPrivateSession(userSession);
			BeShareUser tempUser = new BeShareUser(userSession);
			tempUser.setName(hashAccess.findNameBySession(userSession));
			this.setTitle(this.getTitle() + " " 
							+ hashAccess.findNameBySession(userSession));
			chatTableModel.addUser(tempUser);
		}
	}
}
