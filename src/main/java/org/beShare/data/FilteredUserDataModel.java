package org.beShare.data;

import org.beShare.gui.swingAddons.TableMap;

import javax.swing.event.TableModelEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A TableMap that can filter a UserDataModel to only specific sessionIDs.
 * <p/>
 * The Filtered Model defers to the default model unless there are sessionIds present in this filter.
 * In that case, any table model change forces a checkModel() on this TableMap.
 * <p/>
 * The checkModel interrogates the delegate model for rows matching the sessionIds we're filtering by
 * and updates the internal rowMap which matches our visible rows to the backing models row number.
 */
public class FilteredUserDataModel extends TableMap {
	LinkedHashMap<String, RowMap> sessionIds = new LinkedHashMap<>();

	public FilteredUserDataModel(UserDataModel userDataModel) {
		super.setModel(userDataModel);
	}

	public UserDataModel getUserDataModel() {
		return (UserDataModel) getModel();
	}

	/**
	 * Returns a read-only copy of the current sessionIds.
	 *
	 * @return
	 */
	public Set<String> getSessionIds() {
		return Collections.unmodifiableSet(sessionIds.keySet());
	}

	/**
	 * Removes any existing session IDs and adds only the given Ids to the filter.
	 *
	 * @param sessions
	 */
	public void setSessionIds(final String sessions) {
		LinkedHashMap<String, RowMap> oldMapping = this.sessionIds;
		this.sessionIds = new LinkedHashMap<>();
		for (String sessionId : sessions.split(" ")) {
			this.sessionIds.put(sessionId, new RowMap());
		}
		checkModel();

		// Fire that all content rows have changed, and if oldMapping.size() > this.sessionIds.size() = those were removed.
		fireTableRowsUpdated(0, this.sessionIds.size() - 1);

		if (this.sessionIds.size() < oldMapping.size()) {
			fireTableRowsDeleted(this.sessionIds.size(), oldMapping.size());
		}

		if (this.sessionIds.size() > oldMapping.size()) {
			fireTableRowsInserted(oldMapping.size(), this.sessionIds.size() - 1);
		}
	}

	/**
	 * Determine if the FilteredUserDataModel is actively filtering, or if it's unfiltered.
	 *
	 * @return true if there are sessionIds we're limited to, false otherwise.
	 */
	public boolean isFiltering() {
		return !sessionIds.isEmpty();
	}

	@Override
	public int getRowCount() {
		if (sessionIds.isEmpty()) {
			return super.getRowCount();
		} else {
			return sessionIds.size();
		}
	}

	@Override
	public Object getValueAt(int aRow, int aColumn) {
		if (sessionIds.isEmpty()) {
			return super.getValueAt(aRow, aColumn);
		} else {
			for (Map.Entry<String, RowMap> entry : sessionIds.entrySet()) {
				if (entry.getValue().ourRow == aRow) {
					return super.getValueAt(entry.getValue().backingRow, aColumn);
				}
			}

			throw new IndexOutOfBoundsException("FilteredUserDataModel does not contain row[" + aRow + "]");
		}
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		checkModel();
		super.tableChanged(e);
	}

	/**
	 * Updates our internal state and row mappings.
	 */
	private void checkModel() {
		// Make sure our sessionIds values contain mappings for our rows and the rows in the backing model.
		if (!sessionIds.isEmpty()) {
			Set<String> removeLocal = new HashSet<>();
			int ourRow = 0;
			for (Map.Entry<String, RowMap> entry : sessionIds.entrySet()) {
				entry.getValue().ourRow = ourRow;
				entry.getValue().backingRow = getUserDataModel().sessionIds.indexOf(entry.getKey());

				// If the backing map doesn't contain this value, remove it from our filter list.
				if (entry.getValue().backingRow == -1) {
					removeLocal.add(entry.getKey());
				}
				ourRow++;
			}

			// Clean up any sessionIds which no longer exist.
			for (String key : removeLocal) {
				sessionIds.remove(key);
			}
		}
	}

	/**
	 * A simple class for holding row mappings.
	 */
	private class RowMap {
		int ourRow;
		int backingRow;
	}
}
