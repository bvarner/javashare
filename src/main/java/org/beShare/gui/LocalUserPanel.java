/* Change-Log:
	1.1 - 6.4.2002 - Implemented auto-server update removal, and cleaned up javadocs.
*/
package org.beShare.gui;

import org.beShare.data.LocalUserDataStore;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * <p>LocalUserPanel - This panel contains the objects that set/display the local
 * users info.
 * 
 * <p>Class Started: 2-08-2002
 * <p>Last Update: 6-04-2002
 * 
 * @author Bryan Varner
 * @version 1.1
 */

public class LocalUserPanel extends JPanel implements ActionListener, FocusListener {
	LocalUserDataStore	userDataStore;
	
	JTextField 			serverText;
	DropMenu			serverMenu;
	
	JTextField			userStatusText;
	DropMenu			userStatusMenu;
	
	JTextField			userNameText;
	DropMenu			userNameMenu;
	
	int					awayStatus;
	int					hereStatus;
	boolean				nowAway;
	
	/**
	 * Constructor for Application
	 */
	public LocalUserPanel(LocalUserDataStore lUserData, boolean isApplet){
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		userDataStore = lUserData;
		
		if (! isApplet) {
			// Server Text and Combo Box construction.
			serverMenu = new DropMenu("Server:");
			if (! userDataStore.getServerName().equals(""))
				serverMenu.addItem(userDataStore.getServerName());
			
			// Setup the server Menu
			serverText = new JTextField(userDataStore.getServerName(), 20);
			
			serverMenu.addActionListener(this);
			serverText.addActionListener(this);
			serverText.addFocusListener(this);
			
			this.add(serverMenu);
			this.add(serverText);
			this.add(Box.createHorizontalStrut(6));
		}
		
		// User Name Combo and Text construction.
		userNameMenu = new DropMenu("Name:");
		userNameText = new JTextField(userDataStore.getLocalUserName(), 10);
		userNameMenu.addItem(userDataStore.getLocalUserName());
		
		userNameMenu.addActionListener(this);
		userNameText.addActionListener(this);
		userNameText.addFocusListener(this);
		
		// User Status menu and text construction.
		userStatusMenu = new DropMenu("Status:");
		userStatusMenu.addItem(userDataStore.getLocalUserStatus());
		userStatusText = new JTextField(userDataStore.getLocalUserStatus(), 10);
		
		userStatusMenu.addActionListener(this);
		userStatusText.addActionListener(this);
		userStatusText.addFocusListener(this);
		
		this.add(userNameMenu);
		this.add(userNameText);
		this.add(Box.createHorizontalStrut(6));
		this.add(userStatusMenu);
		this.add(userStatusText);
		
		awayStatus = 0;
		hereStatus = 0;
		nowAway = false;
	}
	
	/**
	 * Adds a server name to the ServerDropMenu object
	 * @param name The name (DNS) of the server to add
	 * @return true if the item is newly added, false if it existed already.
	 */
	public boolean addServerName(String name){
		return serverMenu.addItem(name);
	}
	
	/**
	 * Removes the server with the given name from the list if it exists.
	 * @return true if the item existed and was removed. False if not.
	 */
	public boolean removeServerName(String name){
		return serverMenu.removeItem(name);
	}
	
	/**
	 * Sets the Server Name Text field, and fires an ActionEvent from it.
	 * This adds/sets the server in the list, and activates it.
	 */
	public void setServerName(String name){
		serverText.setText(name);
		this.actionPerformed(new ActionEvent(serverText
			, ActionEvent.ACTION_PERFORMED, "SetServer"));
	}
	
	/**
	 * Adds a user name to the Nickname <code>DropMenu</code>
	 * @param name The nickname to add.
	 */
	public void addUserName(String name){
		userNameMenu.addItem(name);
	}
	
	/**
	 * Adds a user status to the Status <code>DropMenu</code>
	 * @param status The status to add.
	 */
	
	public void addStatus(String status){
		userStatusMenu.addItem(status);
	}
	
	/**
	 * Sets the current user name
	 * @param n The name to set.
	 */
	public void setUserName(String n){
		userNameText.setText(n);
		this.actionPerformed(new ActionEvent(userNameText
			, ActionEvent.ACTION_PERFORMED, "UserNameSet"));
	}
	
	/**
	 * @return the Current UserName
	 */
	public String getUserName(){
		return userNameText.getText();
	}
	
	/**
	 * Sets the users status
	 * @param s The Stats to set
	 */
	public void setUserStatus(String s){
		userStatusText.setText(s);
		this.actionPerformed(new ActionEvent(userStatusText
			, ActionEvent.ACTION_PERFORMED, "UserStatsSet"));
	}
	
	/**
	 * Sets the default away status for auto-away or the /away command.
	 * @param s The away message.
	 */
	public void setAwayStatus(String s){
		boolean statusExists = false;
		for(int x = 0; x < userStatusMenu.getItemCount(); x++){
			if(((String)userStatusMenu.getItemAt(x)).equals(s))
			{
				statusExists = true;
				setAwayStatus(x);
			}
		}
		if(! statusExists){
			// Item Dosen't exist.. yet
			userStatusMenu.addItem(s);
			setAwayStatus(userStatusMenu.getItemCount() - 1);
		}
	}
	
	/**
	 * Sets the away status to an existing status in the status <code>DropMenu</code>
	 */
	public void setAwayStatus(int i){
		awayStatus = i;
	}
	
	/**
	 * Returns the position of the away status in the <code>DropMenu</code>
	 */
	public int getAwayStatus(){
		return awayStatus;
	}
	
	/**
	 * Sets the non-away status (obviously, the 'here' status) to and existing
	 * status in the status <code>DropMenu</code>
	 */
	public void setHereStatus(int i){
		hereStatus = i;
	}
	
	/**
	 * Returns the item index in the Status <code>DropMenu</code> that is used
	 * when you aren't away.
	 * @return Index to the Status <code>DropMenu</code>
	 */
	public int getHereStatus(){
		return hereStatus;
	}
	
	/**
	 * Sets the away status!
	 * 
	 * @param b If <code>true</code> switches to the currently set 'away'
	 * status. If <code>false</code> it will switch to the currently set 'here'
	 * status.
	 */
	public void setAwayStatus(boolean b){
		nowAway = b;
		if(nowAway){
			userStatusText.setText((String)userStatusMenu.getItemAt(awayStatus));
		} else {
			userStatusText.setText((String)userStatusMenu.getItemAt(hereStatus));
		}
		this.actionPerformed(new ActionEvent(userStatusText
			, ActionEvent.ACTION_PERFORMED, "UserStatsSet"));
	}
	
	/**
	 * @return <code>true</code> if in 'away' mode, <code>false</code> if in
	 * 'here' mode.
	 */
	public boolean isAway(){
		return nowAway;
	}
	
	/**
	 * ActionListener interface Responds to ActionEvents directed at this
	 * panel.
	 * 
	 * This will determine what even took place, and send or forward the
	 * appropriate ActionEvent on to another container if necessary.
	 * 
	 * @param e The <code>ActionEvent</code> that occured.
	 */
	public void actionPerformed(ActionEvent e){
		// The serverMenu was Selected, and it's not blank.
		if(e.getSource() == serverMenu 
								&& (serverMenu.getSelectedIndex() != -1))
		{
			String selMenu = (String)serverMenu.getSelectedItem();
			if(selMenu != userDataStore.getServerName()){
				serverText.setText(selMenu);
				userDataStore.setServerName(selMenu);
			}
		}
		// The ServerText has had a Return Pressed.
		else if (e.getSource() == serverText){
			// iterate through the combo-box
			boolean addNew = true;
			for(int x = 0; x < serverMenu.getItemCount(); x++){
				if(((String)serverMenu.getItemAt(x)).equals(
						serverText.getText()))
				{
					serverMenu.setSelectedIndex(x);
					addNew = false;
				}
			}
			if(addNew){
				// Item does not exist
				serverMenu.addItem(serverText.getText());
				serverMenu.setSelectedIndex(
						serverMenu.getItemCount() - 1);
			}
			if(!serverText.getText().equals(userDataStore.getServerName())) {
				userDataStore.setServerName(serverText.getText());
			}
		}
		// The UserName Menu has Changed
		else if (e.getSource() == userNameMenu){
			String selMenu = (String)userNameMenu.getSelectedItem();
			if(selMenu != userDataStore.getLocalUserName()){
				userNameText.setText(selMenu);
				userDataStore.setLocalUserName(selMenu);
			}
		}
		// The UserName Text Field had a return pressed
		else if (e.getSource() == userNameText){
			// iterate through menu
			boolean addNew = true;
			for(int x = 0; x < userNameMenu.getItemCount(); x++){
				if(((String)userNameMenu.getItemAt(x)).equals(
						userNameText.getText()))
				{
					userNameMenu.setSelectedIndex(x);
					addNew = false;
				}
			}
			if(addNew){
				// Item Dosen't exist.. yet
				userNameMenu.addItem(userNameText.getText());
				userNameMenu.setSelectedIndex(
						userNameMenu.getItemCount() - 1);
			}
			if(! userNameText.getText().equals(userDataStore.getLocalUserName())){
				userDataStore.setLocalUserName(userNameText.getText());
			}
		}
		// The User Status menu has changed
		else if (e.getSource() == userStatusMenu){
			String selStatus = (String)userStatusMenu.getSelectedItem();
			if(selStatus != userDataStore.getLocalUserStatus()){
				userStatusText.setText(selStatus);
				userDataStore.setLocalUserStatus(selStatus);
				setHereStatus(userStatusMenu.getSelectedIndex());
			}
		// The user Status Text Box had a return pressed.
		} else if (e.getSource() == userStatusText){
			boolean addNew = true;
			for(int x = 0; x < userStatusMenu.getItemCount(); x++){
				if(((String)userStatusMenu.getItemAt(x)).equals(
						userStatusText.getText()))
				{
					userStatusMenu.setSelectedIndex(x);
					addNew = false;
				}
			}
			if(addNew){
				// Item Dosen't exist.. yet
				userStatusMenu.addItem(userStatusText.getText());
				userStatusMenu.setSelectedIndex(
						userStatusMenu.getItemCount() - 1);
			}
			if(! userStatusText.getText().equals(userDataStore.getLocalUserStatus())){
				userDataStore.setLocalUserStatus(userStatusText.getText());
			}
		}
		if ((! isAway()) && (userStatusMenu.getSelectedIndex() != -1)){
			setHereStatus(userStatusMenu.getSelectedIndex());
		}
	}
	
	/**
	 * Implements part of the FocusListener. We don't do anything with this
	 * one.
	 */
	public void focusGained(FocusEvent e){
	}
	
	/**
	 * Implements part of FocusListener.
	 */
	public void focusLost(FocusEvent e){
		if(e.getSource() == userStatusText){
			userStatusText.postActionEvent();
		}
		if (e.getSource() == serverText){
			serverText.postActionEvent();
		}
		if (e.getSource() == userNameText){
			userNameText.postActionEvent();
		}
	}
	
	/**
	 * @return a String array of server names.
	 */
	public String[] getServerList(){
		return serverMenu.getStringItems();
	}
	
	/**
	 * @return the current server address.
	 */
	public int getCurrentServer(){
		return serverMenu.getSelectedIndex();
	}
	
	/**
	 * @return a String array of nick names
	 */
	public String[] getNicksList(){
		return userNameMenu.getStringItems();
	}
	
	/**
	 * The current nick name.
	 */
	public int getCurrentNick(){
		return userNameMenu.getSelectedIndex();
	}
	
	/**
	 * @return a String Array of status.
	 */
	public String[] getStatusList(){
		return userStatusMenu.getStringItems();
	}
	
	/**
	 * @return the current status.
	 */
	public int getCurrentStatus(){
		return userStatusMenu.getSelectedIndex();
	}
}
