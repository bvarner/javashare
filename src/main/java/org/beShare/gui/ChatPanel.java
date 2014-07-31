package org.beShare.gui;

import org.beShare.data.BeShareUser;
import org.beShare.data.UserHashAccessor;
import org.beShare.event.ChatMessageListener;
import org.beShare.gui.swingAddons.TableSorter;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

/**
	ChatPanel - This class is responsible for constructing new
	ChatMessagingPanels and the associated ChatPoster, as well as
	maintaining the visible user table.
	
	Last Update: 4-27-2002
				5.9.2002 - Added OneTouchExpandable(true) to the Split pane.
				5.13.2002 - added the 'Client' column to the user table.
				6.5.2002 - Now use the UserTable for the JTable. It dosen't hold focus.
				8.2.2002 - Fixed _userData to be userData. How this one made it this long...
	
	@author Bryan Varner
	@version 2.0 a1
*/
public class ChatPanel extends JPanel{
	JSplitPane				paneSplit;
	
	ChatMessagingPanel		chatTextPanel;
	
	JScrollPane				userTablePane;
	UserTable				userTable;
	TableSorter				userTableSorter;
	Vector					colNames;
	UserTableModel			userTableModel;
	ChatMessageListener		messageTarget;
	ChatPoster				chatPoster;
	
	/**
		Creates a new <code>ChatPanel</code> that sends messages to <code
		>parent</code> from it's enclosed <code>ChatMessagingPanel</code>. It
		also creats a <code>ChatPoster</code> that corresponds to the new
		<code>ChatMessagingPanel</code> and registers that new poster with the
		<code>parent</code> by invoking <code>addChatPoster</code> on the <code
		>parent</code>.<br>
		Confused yet?<br>
		This and all sub-components will use
		<code>hashAccess</code> for it's user data access.
	*/
	public ChatPanel(ChatMessageListener parent, UserHashAccessor hashAccess){
		super();
		messageTarget = parent;
		this.setLayout(new GridLayout(1,1,0,0));
		chatTextPanel = new ChatMessagingPanel(messageTarget, hashAccess);
		chatTextPanel.setPreferredSize(new Dimension(300,150));
		
		chatPoster = new ChatPoster(chatTextPanel, hashAccess);
		messageTarget.addChatPoster(chatPoster);
		
		Vector userData = new Vector();
		
		colNames = new Vector();
		colNames.addElement("Name");
		colNames.addElement("ID");
		colNames.addElement("Status");
		colNames.addElement("Files");
		colNames.addElement("Connection");
		colNames.addElement("Load");
		colNames.addElement("Client");
		
		// Inner class wich creates popup menu in the user table.
		class PopupListener extends MouseAdapter {
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}
			
			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}
			
			private void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					JPopupMenu userMenu = new JPopupMenu();
					String userName = userTable.getValueAt(
							userTable.rowAtPoint(
							new Point(e.getX(), e.getY())), 0).toString();
					JMenuItem privChatItem = 
							new JMenuItem("Private Chat with " + userName);
					privChatItem.addActionListener(chatTextPanel);
					JMenuItem watchUserItem = new JMenuItem("Watch User: "
																+ userName);
					watchUserItem.addActionListener(chatTextPanel);
					userMenu.add(privChatItem);
					userMenu.addSeparator();
					userMenu.add(watchUserItem);
					userMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		}
		
		userTableModel = new UserTableModel(userData, colNames);
		userTableSorter = new TableSorter(userTableModel);
		userTable = new UserTable(userTableSorter);
		MouseListener userTableListener = new PopupListener();
		userTable.addMouseListener(userTableListener);
		userTableSorter.addMouseListenerToHeaderInTable(userTable);
		userTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		userTable.setPreferredScrollableViewportSize(new Dimension(150, 300));
		userTablePane = new JScrollPane(userTable, 
									JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
									JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		paneSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatTextPanel
			, userTablePane);
		paneSplit.setOneTouchExpandable(true);
		chatTextPanel.setPreferredSize(new Dimension(400,150));
		try{
			paneSplit.setResizeWeight(.80);
		} catch (NoSuchMethodError nsme){
		}
		add(paneSplit);
	}
	
	/**
		Constructor used to specify the use of the miniBrowser.
	*/
	public ChatPanel(ChatMessageListener parent, UserHashAccessor hashAccess, boolean miniBrowser){
		this(parent, hashAccess);
		chatTextPanel.setUseMiniBrowser(miniBrowser);
	}
	
	public ChatPanel(ChatMessageListener parent, UserHashAccessor hashAccess, Font f){
		this(parent, hashAccess);
		chatTextPanel.setChatFont(f);
	}
	
	public ChatPanel(ChatMessageListener parent, UserHashAccessor hashAccess, boolean miniBrowser, Font f){
		this(parent, hashAccess);
		chatTextPanel.setUseMiniBrowser(miniBrowser);
		chatTextPanel.setChatFont(f);
	}
	
	/**
		Empties the user table.
	*/
	public void clearUserTable(){
		while(userTableModel.getRowCount() > 0)
			userTableModel.removeRow(0);
		//userTableSorter.resetAutoSort();
	}
	
	/**
		Set's the default divide size of the split pane.
	*/
	public void setDefaultDivide(){
		paneSplit.setDividerLocation(.80);
	}
	
	/**
	 * Sets the split pane to the position specified by <code>location</code>.
	 * @param location The position for the split to be placed at.
	 */
	public void setDividerLocation(int location){
		paneSplit.setDividerLocation(location);
	}
	
	/**
	 * Returns the current position of the split.
	 * @return the position of the split view.
	 */
	public int getDividerLocation(){
		return paneSplit.getDividerLocation();
	}
	
	/**
		Adds a new user to the table.
		
		@param newUser The user to add to the userTable.
	*/
	public void addUser(BeShareUser newUser){
		userTableModel.addUser(newUser);
	}
	
	/**
		Updates a users datat in the table.
		
		@param tweakUser The new user data to update.
	*/
	public void updateUser(BeShareUser tweakUser){
		userTableModel.updateUser(tweakUser);
	}
	
	/**
		Removes a user from the user table.
		
		@param removeUser The user to remove.
	*/
	public void removeUser(BeShareUser removeUser){
		userTableModel.removeRow(userTableModel.findUserRow(removeUser));
	}
	
	/**
		@return the poster that this <code>ChatPanel</code> created and
		registered.
	*/
	public ChatPoster getMessagePoster(){
		return chatPoster;
	}
	
	/**
		Passes this call along to the ChatMessagingPanel.
	*/
	public void requestChatLineFocus(){
		chatTextPanel.requestChatLineFocus();
	}
	
	/**
	 * Implements the Cut menu operation.
	 */
	public void cut(){
		chatTextPanel.cut();
	}
	
	/**
	 * Implements the Copy menu operation.
	 */
	public void copy(){
		chatTextPanel.copy();
	}
	
	/**
	 * Implements the Paste menu operation.
	 */
	public void paste(){
		chatTextPanel.paste();
	}
	
	/**
	 * Gets the column the user table is currently sorted by
	 */
	public int getUserListSortColumn(){
		return userTableSorter.column;
	}
	
	/**
	 * Sets the column to sort the user table by, and forces a resort.
	 */
	public void setUserListSortColumn(int column){
		if (userTableSorter != null) {
			userTableSorter.column = column;
			userTableSorter.sortByColumn(column);
		}
	}
	
	/**
	 * @return an array of integers. Each element is the width of the corresponding user table column.
	 */
	public int[] getUserTableColumnWidths() {
		TableColumnModel clmModel = userTable.getColumnModel();
		int[] colWidths = new int[clmModel.getColumnCount()];
		
		for (int x = 0; x < colWidths.length; x++) {
			colWidths[x] = clmModel.getColumn(x).getWidth();
		}
		
		return colWidths;
	}
	
	/**
	 * Sets the column widths for the User Table. Each element in the array represents a column.
	 * @param colWidths an Array of integers consisting of the widths for each column
	 */
	public void setUserTableColumnWidths(int[] colWidths) {
		if (colWidths != null) {
			TableColumnModel clmModel = userTable.getColumnModel();
			
			for (int x = 0; x < colWidths.length; x++) {
				clmModel.getColumn(x).setPreferredWidth(colWidths[x]);
			}
		}
	}
}
