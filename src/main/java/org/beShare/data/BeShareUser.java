/* Change-Log:
	1.31.2002 - Initial Creation
	2.05.2002 - Changed Something
	5.13.2002 - v1.1 - added client support. Updated toString and getTableData.
	1.17.2003 - v2.0 - 
*/
package org.beShare.data;

/**
 *  BeShare beShareUser - This class is a data storage class for creating user
 *  objects to be stored in the local hash-table database.
 *
 * @author     Bryan Varner
 * @version    2.0
 */

public class BeShareUser {
	// BeShare User Number
	private  String   connectionNum;
	// name node
	private  boolean  bot;
	private  String   name;
	private  int      port;
	private  long     installid;
	private  String   client;
	private  String   ipaddress;
	// userstatus node
	private  String   userstatus;
	// uploadstats node
	private  int      cur;
	private  int      max;
	// bandwidth node
	private  String   label;
	private  int      bps;
	// filecount node
	private  int      filecount;
	// firewall?
	private  boolean  fires;
	// files - no firewall
	private  boolean  files;
	
	/**
	 *  Creates a new BeShareUser with Session ID = cn
	 *
	 * @param  cn  The Session ID of the user
	 */
	public BeShareUser(String cn) {
		connectionNum = cn;
		bot = false;
		name = "<unknown>";
		port = -1;
		ipaddress = "NOTHING";
		client = "";
		installid = -1;
		userstatus = "";
		cur = -1;
		max = -1;
		label = "";
		bps = -1;
		filecount = -1;
		fires = false;
		files = true;
	}
	
	/**
	 *  Sets weather or not this user is a bot
	 *
	 * @param  b  Boolean representing bot status
	 */
	public void setBot(boolean b) {
		bot = b;
	}
	
	/**
	 *  Sets the InstallID property for this user
	 *
	 * @param  ID  the installID for the user
	 */
	public void setInstallID(long ID) {
		installid = ID;
	}
	
	/**
	 *  sets the User Name
	 *
	 * @param  n  Name of user
	 */
	public void setName(String n) {
		name = n;
	}
	
	/**
	 *  Sets the port for file transfers for a given user
	 *
	 * @param  p  The Remote port
	 */
	public void setPort(int p) {
		port = p;
	}
	
	/**
	 *  Sets the User status
	 *
	 * @param  s  String representing users current status
	 */
	public void setStatus(String s) {
		userstatus = s;
	}
	
	/**
	 * Sets the Client name and version
	 * 
	 * @param c String representing the name and version of the client.
	 */
	public void setClient(String c){
		client = c;
	}
	
	/**
	 * @return a string of the client app name and version
	 */
	public String getClient(){
		return client;
	}
	
	/**
	 *  Sets the upload characteristics.
	 *
	 * @param  c  The Current Number of uploads.
	 * @param  m  The maximum Number of uploads.
	 */
	public void setUpload(int c, int m) {
		cur = c;
		max = m;
	}
	
	/**
	 *  Sets the current number of uploads.
	 *
	 * @param  c  The Current Number of Uploads
	 */
	public void setUploadCurrent(int c) {
		cur = c;
	}
	
	/**
	 *  Sets the Max number of Uploads
	 *
	 * @param  m  the Maximum number of uploads.
	 */
	public void setUploadMax(int m) {
		max = m;
	}
	
	/**
	 *  Sets the number of files this user has shared
	 *
	 * @param  f  Number of files shared by this user
	 */
	public void setFileCount(int f) {
		filecount = f;
	}
	
	/**
	 *  Sets a String representing the connection type.
	 *
	 * @param  l  Label of connection type
	 */
	public void setBandwidthLabel(String l) {
		label = l;
	}
	
	/**
	 *  Sets this users total bandwidth property
	 *
	 * @param  b  Thier bandwidth represented in bps.
	 */
	public void setBandwidthBps(int b) {
		bps = b;
	}
	
	/**
	 *  Sets weather this user is fire-walled or not
	 *
	 * @param  b  boolean representing their firewalled status
	 */
	public void setFirewall(boolean b) {
		if(b) {
			fires = true;
			files = false;
		} else {
			fires = false;
			files = true;
		}
	}
	
	/**
	 *  Returns a string representation of the SessionID;
	 *
	 * @return    the Session ID
	 * @deprecated use getSessionID instead.
	 */
	public String getConnectID() {
		return connectionNum;
	}
	
	/**
	 *  Returns a string representation of the SessionID;
	 *
	 * @return    the Session ID
	 */
	public String getSessionID() {
		return connectionNum;
	}
	
	/**
	 *  Retrieves weather or not this user is a bot.
	 *
	 * @return    true if bot, false if not.
	 */
	public boolean isBot() {
		return bot;
	}
	
	/**
	 *  retreives the InstallID for the user.
	 *
	 * @return    the InstallID, default = <code>0</code>.
	 */
	public long getInstallID() {
		return installid;
	}
	
	/**
	 *  Retreives the user Name
	 *
	 * @return    the name of the user, default is <code>''</code>
	 */
	public String getName() {
		return name;
	}
	
	/**
	 *  Gets the port for this users file transfer connections.
	 *
	 * @param  p  Description of Parameter
	 * @return    The remote Port number, or <code>0</code> if no port was
	 *      specified.
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * Sets the IPAddress for this user.
	 *
	 * @param ipaddr The IPaddress of the user.
	 */
	public void setIPAddress(String ip){
		ipaddress = ip;
	}
	
	/**
	 * Retreives the ip addres of the user.
	 * @return the ip address of the user.
	 */
	public String getIPAddress(){
		return ipaddress;
	}
	
	/**
	 *  Returns the Users current Status
	 *
	 * @return    The Users Status
	 */
	public String getStatus() {
		return userstatus;
	}
	
	/**
	 *  Get the Current number of uploads
	 *
	 * @return    The number of uploads for this user
	 */
	public int getUploadCurrent() {
		return cur;
	}
	
	/**
	 *  Gets the Maximum number of uploads
	 *
	 * @return    the Max number of uploads for this user
	 */
	public int getUploadMax() {
		return max;
	}
	
	/**
	 *  Gets the current load in String form
	 *
	 * @return    The loadString value
	 */
	public String getLoadString() {
		if(getUploadMax() == -1) {
			return new String("?");
		} else {
			return new String("" + (double)(getUploadCurrent() / getUploadMax()));
		}
	}
	
	/**
	 *  Returns the number of files this user is sharing.
	 *
	 * @return    the number of shared files
	 */
	public int getFileCount() {
		return filecount;
	}
	
	/**
	 *  Gets the fileCountString attribute of the BeShareUser object
	 *
	 * @return    The fileCountString value
	 */
	public String getFileCountString() {
		if(filecount != -1) {
			if (!getFirewall()){
				return new String("" + filecount);
			} else {
				return new String("(" + filecount + ")");
			}
		} else {
			return new String("?");
		}
	}
	
	/**
	 *  Gets a string representing the type of connection
	 *
	 * @return    The type of connection this user has
	 */
	public String getBandwidthLabel() {
		return label;
	}
	
	/**
	 *  Get the BPS of their bandwidth as reported by them
	 *
	 * @return    The Bandwith in bps
	 */
	public int getBandwidthBps() {
		return bps;
	}
	
	/**
	 * @return    True if the user is Firewalled, Flase if not.
	 */
	public boolean getFirewall() {
		return fires;
	}
	
	/**
	 *  Gets an Object array suitable for inserting into a table
	 *
	 * @return    The tableData value
	 */
	public Object[] getTableData() {
		if (getFirewall()){
			Object[]  data  = {getName(),
					getConnectID(),
					getStatus(),
					"(" + getFileCountString() + ")",
					getBandwidthLabel(),
					getLoadString(),
					getClient()
					};
			return data;
		} else {
			Object[]  data  = {getName(),
					getConnectID(),
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
	 * @return    A multi-line String Representation of the user.
	 */
	public String toString() {
		String  rtString  = new String();
		rtString = "Session ID = " + connectionNum + "\n";
		rtString = rtString + "IP Address = " + ipaddress + "\n";
		rtString = rtString + "Bot = " + bot + "\n";
		rtString = rtString + "Name = " + name + "\n";
		rtString = rtString + "Port = " + port + "\n";
		rtString = rtString + "Client = " + client + "\n";
		rtString = rtString + "Install ID = " + installid + "\n";
		rtString = rtString + "Status = " + userstatus + "\n";
		rtString = rtString + "Upload Current = " + cur + "\n";
		rtString = rtString + "Upload Max = " + max + "\n";
		rtString = rtString + "Current Load = " + getLoadString() + "\n";
		rtString = rtString + "Bandwidth = " + label + " at " + bps + "\n";
		return rtString;
	}
}

