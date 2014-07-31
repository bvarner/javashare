/* Change-Log:
	8.3.2002 - CODE-A-THON! - Original creation.
	1.17.2003 - Now stores references to the user, rather than the user name.
*/
package org.beShare.gui;

import org.beShare.data.BeShareUser;

import javax.swing.table.DefaultTableModel;
import java.util.Vector;
/**
 *	QueryTableModel - A model for displaying and storing data for <code>JTable
 *	</code> views.
 *	
 *	@author Bryan Varner
 *	@version 2.0
 */

public class QueryTableModel extends DefaultTableModel{
	/**
		Creates a new model with Column headers <code>cols</code> and data model
		<code>data</code>
	*/
	public QueryTableModel(Vector cols, Vector data){
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
		Makes it so that the table will scroll horizontally, instead of
		resizing.
		
		@return false
		@overrides DefaultTableModel.getScrollableTracksViewportWidth()
	*/
	public boolean getScrollableTracksViewportWidth(){
		return false;
	}
	
	/**
	 * @overrides DefaultTableModel.getValueAt(int row, int column);
	 */
	public Object getValueAt(int row, int column) {
		// If it's the user column, return the users name, if it's not, forget it!
		if (column == 3)
			return ((BeShareUser)super.getValueAt(row, column)).getName();
		else
			return super.getValueAt(row, column);
	}
	
	/**
	 * @return the BeShareUser object for the given row.
	 */
	public BeShareUser getUser(int row) {
		return (BeShareUser)super.getValueAt(row, 3);
	}
	
	/**
	 * Searches for and removes a file that matches the specified goodies.
	 */
	public void removeFile(String userName, String fileName){
		for (int x = 0; x < getRowCount(); x++){
			if (userName.equals(getValueAt(x, 3).toString()) &&
				fileName.equals(getValueAt(x, 1).toString()))
			{
				removeRow(x);
			}
		}
	}
}
