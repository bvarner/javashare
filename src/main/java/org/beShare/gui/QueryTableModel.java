/* Change-Log:
	8.3.2002 - CODE-A-THON! - Original creation.
	1.17.2003 - Now stores references to the user, rather than the user name.
*/
package org.beShare.gui;

import org.beShare.data.BeShareUser;
import org.beShare.data.SharedFile;
import org.beShare.data.UserDataModel;

import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableModel;
import java.util.HashMap;

/**
 * QueryTableModel - A model for displaying and storing data for <code>JTable
 * </code> views.
 *
 * @author Bryan Varner
 * @version 2.0
 */

public class QueryTableModel extends DefaultTableModel {
	private HashMap<String, ImageIcon> icons = new HashMap<>();
	private UserDataModel userDataModel;

	/**
	 * Creates a new model with Column headers <code>cols</code> and data model
	 * <code>data</code>
	 */
	public QueryTableModel(final UserDataModel userDataModel) {
		super(new String[]{"", "File Name", "File Size", "User", "Path", "Kind", "Connection"}, 0);
		this.userDataModel = userDataModel;
	}

	/**
	 * Adds a new file to the result list
	 */
	public void addResult(SharedFile newFile) {
		String size = (Long.toString(newFile.getSize()));
		if (size.length() <= 3) {
			size = size + "bytes";
		} else if (size.length() <= 6) {
			size = (newFile.getSize() / 1024) + " kb";
		} else if (size.length() <= 9) {
			size = ((double) (newFile.getSize() / (1024 ^ 2))) / 1000 + " MB";
		}

		ImageIcon fileIcon = new ImageIcon();

		if (!icons.containsKey(newFile.getKind())) {
			if (newFile.getKind().equals("")) {
				// Load the generic file icon.
				fileIcon = new ImageIcon(getClass().getClassLoader().getResource("Images/fileicons/notype.gif"));
				icons.put(newFile.getKind(), fileIcon);
			} else {
				// Replace / with ^ and . with &
				String fileName = newFile.getKind();
				fileName = fileName.replace('/', '^');
				fileName = fileName.replace('.', '&');
				fileName = fileName.concat(".gif");
				try {
					fileIcon =
							new ImageIcon(getClass().getClassLoader().getResource("Images/fileicons/" + fileName));
					icons.put(newFile.getKind(), fileIcon);
				} catch (NullPointerException npe) {
					fileIcon =
							new ImageIcon(getClass().getClassLoader().getResource("Images/fileicons/notype.gif"));
					icons.put(newFile.getKind(), fileIcon);
				}
			}
		} else {
			fileIcon = icons.get(newFile.getKind());
		}

		BeShareUser user = userDataModel.getUser(newFile.getSessionID());
		Object[] fileData = {fileIcon, // Icon image name here later!
		                     newFile.getName(),
		                     size,
		                     user,
		                     newFile.getPath(),
		                     newFile.getKind(),
		                     user.getBandwidthLabel()};
		insertRow(0, fileData);
	}

	/**
	 * @return false
	 * @overrides DefaultTableModel.isCellEditable(int, int)
	 */
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	/**
	 * Clears all data from the table.
	 */
	public void clearTable() {
		for (int x = (getRowCount() - 1); x >= 0; x--) {
			this.removeRow(x);
		}
	}

	/**
	 * Makes it so that the table will scroll horizontally, instead of
	 * resizing.
	 *
	 * @return false
	 * @overrides DefaultTableModel.getScrollableTracksViewportWidth()
	 */
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	/**
	 * @overrides DefaultTableModel.getValueAt(int row, int column);
	 */
	public Object getValueAt(int row, int column) {
		// If it's the user column, return the users name, if it's not, forget it!
		if (column == 3) {
			return ((BeShareUser) super.getValueAt(row, column)).getName();
		} else {
			return super.getValueAt(row, column);
		}
	}

	/**
	 * @return the BeShareUser object for the given row.
	 */
	public BeShareUser getUser(int row) {
		return (BeShareUser) super.getValueAt(row, 3);
	}

	/**
	 * Searches for and removes a file that matches the specified goodies.
	 */
	public void removeFile(String userName, String fileName) {
		for (int x = 0; x < getRowCount(); x++) {
			if (userName.equals(getValueAt(x, 3).toString()) &&
					    fileName.equals(getValueAt(x, 1).toString())) {
				removeRow(x);
			}
		}
	}
}
