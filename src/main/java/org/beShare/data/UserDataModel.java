package org.beShare.data;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


/**
 * A model for displaying and storing BeShare User Data.
 *
 * @author Bryan Varner
 */
public class UserDataModel extends AbstractTableModel {

	protected HashMap<String, BeShareUser> userMap = new HashMap<>();
	List<String> sessionIds = new ArrayList<>();

	public UserDataModel() {
		super();
	}

	/**
	 * Searches the User table for a SessionID which matches the supplied
	 * Name.
	 *
	 * @param startsWithName A String that starts with the name of a user.
	 * @return The SessionID of that user, if one, or "" if no match is found.
	 */
	public String findSessionByName(final String startsWithName) {
		String session = "";

		// Parse to the first space, if there is one.
		String firstToken = startsWithName;
		if (startsWithName.contains(" ")) {
			firstToken = startsWithName.substring(0, startsWithName.indexOf(' '));
		}

		// Iterate the list looking for matches.
		List<BeShareUser> partialMatches = new ArrayList<BeShareUser>(userMap.size());

		for (BeShareUser user : userMap.values()) {
			String name = user.getUserName();

			// Check for an exact match on the entire string.
			if (name.equalsIgnoreCase(startsWithName)) {
				session = user.getSessionID();
				break;
			} else if (name.equalsIgnoreCase(firstToken)) {
				session = user.getSessionID();
				break;
			} else if (firstToken.toUpperCase().startsWith(user.getName().toUpperCase())) {
				partialMatches.add(user);
			}
		}

		// If we don't find an exact match, check the partials list.
		if ("".equals(session) && !partialMatches.isEmpty()) {
			// Sort the list...
			Collections.sort(partialMatches, new Comparator<BeShareUser>() {
				@Override
				public int compare(BeShareUser o1, BeShareUser o2) {
					return o1.getUserName().compareTo(o2.getUserName());
				}
			});

			// Return the first entry.
			session = partialMatches.get(0).getSessionID();
		}

		return session;
	}

	/**
	 * Searches the User Table for a name that matches the supplied SessionID.
	 *
	 * @param sessionID The SessionID to resolve a name for
	 * @return The Name of user matching sessionID, or an empty "" String.
	 */
	public String findNameBySession(final String sessionID) {
		// Or if we're in the map...
		if (userMap.containsKey(sessionID)) {
			return userMap.get(sessionID).getUserName();
		}

		// Fallback to empty.
		return "";
	}

	/**
	 * Searches through the user Table for names starting with the supplied
	 * string. If it finds one, it returns the user name.
	 *
	 * @param partialName The Beginning of a name.
	 * @return The matching name, or "" if none is found.
	 */
	public String findCompletedName(final String partialName) {
		if (partialName.equals("")) {
			return "";
		}

		String partialSearch = partialName.toUpperCase();

		String completedName = "";
		for (BeShareUser user : this.userMap.values()) {
			String compareName = user.getUserName().toUpperCase();

			if (compareName.startsWith(partialSearch)) {
				completedName = user.getUserName();
				break;
			}
		}

		return completedName;
	}

	/**
	 * Attempts to lookup a User given a string. The string may be either a full username, or a sessionId.
	 *
	 * @param str
	 * @return A BeShareUser for that name, or sessionId, or <code>null</code> if none is found.
	 */
	public BeShareUser findByNameOrSession(final String str) {
		// Attempt a name lookup.
		String sessionId = findSessionByName(str);
		if ("".equals(sessionId)) {
			sessionId = str;
		}
		return getUser(sessionId);
	}

	/**
	 * Gets the BeShareUser for the given sessionID
	 *
	 * @param sessionID
	 * @return A BeShareUser, or <code>null</code>
	 */
	public BeShareUser getUser(final String sessionID) {
		return userMap.get(sessionID);
	}

	/**
	 * @return false
	 * @overrides DefaultTableModel.isCellEditable(int, int)
	 */
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	/**
	 * Adds the data from a user to the data model.
	 *
	 * @param userObj The user to add.
	 */
	public void addUser(BeShareUser userObj) {
		sessionIds.add(userObj.getSessionID());
		userMap.put(userObj.getSessionID(), userObj);

		int index = sessionIds.indexOf(userObj.getSessionID());
		fireTableRowsInserted(index, index);
	}

	/**
	 * Updates the data for an existing user. All users are matched by
	 * sessionID.
	 *
	 * @param userObj The User object containing the updated data.
	 */
	public void updateUser(BeShareUser userObj) {
		if (sessionIds.contains(userObj.getSessionID())) {
			userMap.put(userObj.getSessionID(), userObj);

			int index = sessionIds.indexOf(userObj.getSessionID());
			fireTableRowsUpdated(index, index);
		} else {
			addUser(userObj);
		}
	}

	/**
	 * Removes a user.
	 *
	 * @param userObj
	 */
	public void removeUser(BeShareUser userObj) {
		int index = sessionIds.indexOf(userObj.getSessionID());

		if (index > -1) {
			sessionIds.remove(index);
			userMap.remove(userObj.getSessionID());

			fireTableRowsDeleted(index, index);
		}
	}

	/**
	 * Clears the entire table.
	 */
	public void clear() {
		int size = sessionIds.size();

		sessionIds.clear();
		userMap.clear();

		if (size > 0) {
			fireTableRowsDeleted(0, size);
		}
	}

	@Override
	public int getRowCount() {
		return sessionIds.size();
	}

	@Override
	public int getColumnCount() {
		return 7;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
			case 0:
				return "Name";
			case 1:
				return "ID";
			case 2:
				return "Status";
			case 3:
				return "Files";
			case 4:
				return "Connection";
			case 5:
				return "Load";
			case 6:
				return "Client";
		}
		return "";
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		BeShareUser user = userMap.get(sessionIds.get(rowIndex));
		switch (columnIndex) {
			case 0:
				return user.getUserName();
			case 1:
				return user.getSessionID();
			case 2:
				return user.getStatus();
			case 3:
				return user.getFileCountString();
			case 4:
				return user.getBandwidthLabel();
			case 5:
				if (user.getUploadMax() == 0) {
					return "?";
				} else {
					return user.getLoadString();
				}
			case 6:
				return user.getClient();
		}
		return "";
	}
}
