/* Change-Log:
	1.31.2002 - Initial Creation
	2.05.2002 - Changed Something
	5.13.2002 - v1.1 - added client support. Updated toString and getTableData.
	1.17.2003 - v2.0 - 
*/
package org.beShare.data;

/**
 * BeShare beShareUser - This class is a data storage class for creating user
 * objects to be stored in the local hash-table database.
 *
 * @author Bryan Varner
 * @version 2.0
 */

public class BeShareUser {
	// BeShare User Number
	private String sessionId;
	// name node
	private boolean bot;
	private String name;
	private int port;
	private long installid;
	private String client;
	private String ipaddress;
	// userstatus node
	private String userstatus;
	// uploadstats node
	private int cur;
	private int max;
	// bandwidth node
	private String label;
	private int bps;
	// filecount node
	private int filecount;
	// firewall?
	private boolean fires;
	// files - no firewall
	private boolean files;

	/**
	 * Creates a new BeShareUser with Session ID = cn
	 *
	 * @param sessionId The Session ID of the user
	 */
	public BeShareUser(final String sessionId) {
		this.sessionId = sessionId;
		this.bot = false;
		this.name = "<unknown>";
		this.port = -1;
		this.ipaddress = "NOTHING";
		this.client = "";
		this.installid = -1;
		this.userstatus = "";
		this.cur = -1;
		this.max = -1;
		this.label = "";
		this.bps = -1;
		this.filecount = -1;
		this.fires = false;
		this.files = true;
	}

	/**
	 * @return a string of the client app name and version
	 */
	public String getClient() {
		return client;
	}

	/**
	 * Sets the Client name and version
	 *
	 * @param c String representing the name and version of the client.
	 */
	public void setClient(final String c) {
		this.client = c;
	}

	/**
	 * Sets the upload characteristics.
	 *
	 * @param c The Current Number of uploads.
	 * @param m The maximum Number of uploads.
	 */
	public void setUpload(int c, int m) {
		this.cur = c;
		this.max = m;
	}

	/**
	 * Returns a string representation of the SessionID;
	 *
	 * @return the Session ID
	 */
	public String getSessionID() {
		return this.sessionId;
	}

	/**
	 * Retrieves weather or not this user is a bot.
	 *
	 * @return true if bot, false if not.
	 */
	public boolean isBot() {
		return this.bot;
	}

	/**
	 * Sets weather or not this user is a bot
	 *
	 * @param b Boolean representing bot status
	 */
	public void setBot(boolean b) {
		this.bot = b;
	}

	/**
	 * retreives the InstallID for the user.
	 *
	 * @return the InstallID, default = <code>0</code>.
	 */
	public long getInstallID() {
		return installid;
	}

	/**
	 * Sets the InstallID property for this user
	 *
	 * @param ID the installID for the user
	 */
	public void setInstallID(long ID) {
		installid = ID;
	}

	/**
	 * Retreives the user Name
	 *
	 * @return the name of the user, default is <code>''</code>
	 */
	public String getName() {
		return name;
	}

	/**
	 * sets the User Name
	 *
	 * @param n Name of user
	 */
	public void setName(String n) {
		name = n;
	}

	/**
	 * Retrieves only the username portion of the name, parsing out URLs.
	 *
	 * @return the name of the user, or empty string.
	 */
	public String getUserName() {
		String noUrlName = name;
		// Parse URLs from usernames.
		if (noUrlName.endsWith("]") && noUrlName.indexOf("[") >= 0) {
			noUrlName = noUrlName.substring(noUrlName.indexOf("[") + 1, noUrlName.indexOf("]"));
		}
		return noUrlName;
	}

	/**
	 * Gets the port for this users file transfer connections.
	 *
	 * @return The remote Port number, or <code>0</code> if no port was
	 * specified.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Sets the port for file transfers for a given user
	 *
	 * @param p The Remote port
	 */
	public void setPort(int p) {
		port = p;
	}

	/**
	 * Retreives the ip addres of the user.
	 *
	 * @return the ip address of the user.
	 */
	public String getIPAddress() {
		return ipaddress;
	}

	/**
	 * Sets the IPAddress for this user.
	 *
	 * @param ip The IPaddress of the user.
	 */
	public void setIPAddress(String ip) {
		ipaddress = ip;
	}

	/**
	 * Returns the Users current Status
	 *
	 * @return The Users Status
	 */
	public String getStatus() {
		return userstatus;
	}

	/**
	 * Sets the User status
	 *
	 * @param s String representing users current status
	 */
	public void setStatus(String s) {
		userstatus = s;
	}

	/**
	 * Get the Current number of uploads
	 *
	 * @return The number of uploads for this user
	 */
	public int getUploadCurrent() {
		return cur;
	}

	/**
	 * Sets the current number of uploads.
	 *
	 * @param c The Current Number of Uploads
	 */
	public void setUploadCurrent(int c) {
		cur = c;
	}

	/**
	 * Gets the Maximum number of uploads
	 *
	 * @return the Max number of uploads for this user
	 */
	public int getUploadMax() {
		return max;
	}

	/**
	 * Sets the Max number of Uploads
	 *
	 * @param m the Maximum number of uploads.
	 */
	public void setUploadMax(int m) {
		max = m;
	}

	/**
	 * Gets the current load in String form
	 *
	 * @return The loadString value
	 */
	public String getLoadString() {
		if (getUploadMax() == -1) {
			return new String("?");
		} else {
			return new String("" + (double) (getUploadCurrent() / getUploadMax()));
		}
	}

	/**
	 * Returns the number of files this user is sharing.
	 *
	 * @return the number of shared files
	 */
	public int getFileCount() {
		return filecount;
	}

	/**
	 * Sets the number of files this user has shared
	 *
	 * @param f Number of files shared by this user
	 */
	public void setFileCount(int f) {
		filecount = f;
	}

	/**
	 * Gets the fileCountString attribute of the BeShareUser object
	 *
	 * @return The fileCountString value
	 */
	public String getFileCountString() {
		if (filecount != -1) {
			if (!getFirewall()) {
				return new String("" + filecount);
			} else {
				return new String("(" + filecount + ")");
			}
		} else {
			return new String("?");
		}
	}

	/**
	 * Gets a string representing the type of connection
	 *
	 * @return The type of connection this user has
	 */
	public String getBandwidthLabel() {
		return label;
	}

	/**
	 * Sets a String representing the connection type.
	 *
	 * @param l Label of connection type
	 */
	public void setBandwidthLabel(String l) {
		label = l;
	}

	/**
	 * Get the BPS of their bandwidth as reported by them
	 *
	 * @return The Bandwith in bps
	 */
	public int getBandwidthBps() {
		return bps;
	}

	/**
	 * Sets this users total bandwidth property
	 *
	 * @param b Thier bandwidth represented in bps.
	 */
	public void setBandwidthBps(int b) {
		bps = b;
	}

	/**
	 * @return True if the user is Firewalled, Flase if not.
	 */
	public boolean getFirewall() {
		return fires;
	}

	/**
	 * Sets weather this user is fire-walled or not
	 *
	 * @param b boolean representing their firewalled status
	 */
	public void setFirewall(boolean b) {
		if (b) {
			fires = true;
			files = false;
		} else {
			fires = false;
			files = true;
		}
	}

	/**
	 * Gets an Object array suitable for inserting into a table
	 *
	 * @return The tableData value
	 */
	public Object[] getTableData() {
		if (getFirewall()) {
			Object[] data = {getName(),
			                 getSessionID(),
			                 getStatus(),
			                 "(" + getFileCountString() + ")",
			                 getBandwidthLabel(),
			                 getLoadString(),
			                 getClient()
			};
			return data;
		} else {
			Object[] data = {getName(),
			                 getSessionID(),
			                 getStatus(),
			                 getFileCountString(),
			                 getBandwidthLabel(),
			                 getLoadString(),
			                 getClient()
			};
			return data;
		}
	}

	/**
	 * @return A multi-line String Representation of the user.
	 */
	@Override
	public String toString() {
		return "Session ID = " + sessionId + "\n" +
				       "IP Address = " + ipaddress + "\n" +
				       "Bot = " + bot + "\n" +
				       "Name = " + name + "\n" +
				       "Port = " + port + "\n" +
				       "Client = " + client + "\n" +
				       "Install ID = " + installid + "\n" +
				       "Status = " + userstatus + "\n" +
				       "Upload Current = " + cur + "\n" +
				       "Upload Max = " + max + "\n" +
				       "Current Load = " + getLoadString() + "\n" +
				       "Bandwidth = " + label + " at " + bps + "\n";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof BeShareUser) {
			BeShareUser that = (BeShareUser) obj;
			return that.sessionId.equals(this.sessionId);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return sessionId.hashCode();
	}
}

