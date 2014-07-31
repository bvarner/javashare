package org.beShare.data;

/**
 *  LocalUserDataStore - An Interface that defines a means of setting and
 *  retreiving the current status of several user settings. Last Update:
 *  2-28-2002
 *
 * @author     Bryan Varner
 * @created    March 8, 2002
 * @version    1.0
 */
public interface LocalUserDataStore {
	public void setLocalUserName(String name);
	public void setLocalUserStatus(String status);
	public void setServerName(String serverName);
	public String getLocalUserName();
	public String getLocalUserStatus();
	public String getServerName();
}

