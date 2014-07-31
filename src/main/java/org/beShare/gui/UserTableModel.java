package org.beShare.gui;

import org.beShare.data.BeShareUser;

import javax.swing.table.DefaultTableModel;
import java.util.Vector;
/**
	UserTableModel - A model for displaying and storing data for <code>JTable
	</code> views.
	
	Last Update: 2-28-2002
	
	@author Bryan Varner
	@version 1.0
*/

public class UserTableModel extends DefaultTableModel{
	/**
		Creates a new model with Column headers <code>cols</code> and data model
		<code>data</code>
	*/
	public UserTableModel(Vector cols, Vector data){
		super(cols, data);
	}
	
	/**
		@return false
		@overrides DefaultTableModel.isCellEditable(int, int)
	*/
	public boolean isCellEditable(int row, int col){
		return false;
	}
	
	/**
		Clears all data from the table.
	*/
	public void clearTable(){
		for (int x = (getRowCount() - 1); x >= 0; x--)
			this.removeRow(x);
	}
	
	/**
		Adds the data from a user to the data model.
		@param userObj The user to add.
	*/
	public void addUser(BeShareUser userObj){
		addRow(userObj.getTableData());
	}
	
	/**
		Updates the data for an existing user. All users are matched by
		sessionID.
		@param userObj The User object containing the updated data.
	*/
	public void updateUser(BeShareUser userObj){
		for (int x = 0; x < getRowCount(); x++){
			if(userObj.getConnectID().equals(getValueAt(x,1).toString())){
				// Parse the name for URL and label... Damn things...
				if (userObj.getName().endsWith("]"))
					setValueAt(userObj.getName().substring(userObj.getName().indexOf("[") + 1, userObj.getName().indexOf("]")), x, 0);
				else
					setValueAt(userObj.getName(), x, 0);
				setValueAt(userObj.getStatus(), x, 2);
				setValueAt(userObj.getFileCountString(), x, 3);
				setValueAt(userObj.getBandwidthLabel(), x, 4);
				if (userObj.getUploadMax() == 0){
					setValueAt("?", x, 5);
				} else {
					setValueAt(userObj.getLoadString(), x, 5);
				}
				setValueAt(userObj.getClient(), x, 6);
				return;
			}
		}
	}
	
	/**
		@return the Row at which <code>searchUser</code> can be found in the
		model.
	*/
	public int findUserRow(BeShareUser searchUser){
		for (int x = 0; x < getRowCount(); x++){
			if(searchUser.getConnectID().equals(getValueAt(x,1).toString())){
				return x;
			}
		}
		return -1;
	}
	
	/**
		Makes it so that the table will scroll horizontally, instead of
		resizing.
		
		@return false
		@overrides DefaultTableModel.getScrollableTracksViewportWidth()
	*/
	public boolean getScrollableTracksViewportWidth(){
		return false;
	}
	
	
}
