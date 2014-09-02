/* Change-Log:
	8.3.2002 - CODE-A-THON! - Original creation.
	1.17.2003 - Now stores references to the user, rather than the user name.
*/
package org.beShare.gui;

import org.beShare.data.BeShareUser;
import org.beShare.data.SharedFile;
import org.beShare.data.UserDataModel;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Renders SharedFile query results.
 *
 * @author Bryan Varner
 */

public class QueryTableModel extends AbstractTableModel {

	private List<SharedFile> files = new ArrayList<>();

	/**
	 * Creates a new model with Column headers <code>cols</code> and data model
	 * <code>data</code>
	 */
	public QueryTableModel() {
		super();
	}

	/**
	 * Adds a new file to the result list
	 */
	public void add(SharedFile file) {
		files.add(file);
		int index = files.indexOf(file);
		fireTableRowsInserted(index, index);
	}

	public void remove(SharedFile file) {
		files.remove(file);
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
	public void clear() {
		int size = files.size();
		files.clear();

		if (size > 0) {
			fireTableRowsDeleted(0, size);
		}
	}

	@Override
	public int getRowCount() {
		return files.size();
	}

	@Override
	public int getColumnCount() {
		return 7;
	}

	@Override
	public String getColumnName(int column) {
		switch(column) {
			case 1:
				return "File Name";
			case 2:
				return "File Size";
			case 3:
				return "User";
			case 4:
				return "Path";
			case 5:
				return "Kind";
			case 6:
				return "Connection";
		}
		return "";
	}

	@Override
	public Object getValueAt(int row, int column) {
		SharedFile sf = files.get(row);
		switch (column) {
			case 0:
				return FileTypeIconCache.getIcon(sf.getKind());
			case 1:
				return sf.getName();
			case 2:
				return sf.getSize();
			case 3:
				return sf.getSessionID();
			case 4:
				return sf.getPath();
			case 5:
				return sf.getKind();
			case 6:
				return sf.getSessionID();
		}
		return "";
	}
}
