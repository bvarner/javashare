package org.beShare.data;

/**
 *  LocalUserDataStore - An Interface that defines a means of setting and
 *  retreiving the current status of several user settings.
 *
 * @author     Bryan Varner
 * @created    March 8, 2002
 */
public interface LocalUserDataStore {
    public String getLocalUserName();
    public void setLocalUserName(String name);

    public String getLocalUserStatus();
    public void setLocalUserStatus(String status);

    public String getServerName();
    public void setServerName(String serverName);
}

