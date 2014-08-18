///* Change Log:
//	1.1 - 6.4.2002 - Renamed class to conform with java Naming conventions.
//			This one slipped through the cracks before.
//	1.5 - 6.4.2002 - Added remove list tokenizing, and re-worked accessors to match.
//			Deprecated old methods, implemented new.
//	2.0 - 12.19.2002 - Reworked the stream reading complety now reads up to 64 bytes at a time.
//			Now it's own Thread. Did a lot of work on this cleaning it up and making it better.
//*/
//package org.beShare.network;
//
//import org.beShare.gui.AppPanel;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URL;
//import java.util.StringTokenizer;
//import java.util.Vector;
//
///**
// * <p>ServerAutoUpdate - A Class which takes a URL, parses the file for BeShare
// * server information, and creates a list of servers based upon it.
// * To do the work, call Run() - it's a separate thread! Yeah!
// * <p/>
// * <p>Class Started: 2-08-2002
// * <p>Last Update: 12-19-2002
// *
// * @author Bryan Varner
// * @version 2.0
// */
//public class ServerAutoUpdate extends Thread {
//	Vector serverList;
//	Vector removeList;
//
//	AppPanel pnl;
//
//	/**
//	 * Creates a new ServerAutoUpdate that retreives the server list from
//	 * <code>url</code>.
//	 */
//	public ServerAutoUpdate(AppPanel pnl) {
//		serverList = new Vector();
//		removeList = new Vector();
//		this.pnl = pnl;
//	}
//
//	public void run() {
//		try {
//			URL updateURL = new URL(pnl.AutoUpdateURL);
//
//			InputStream serverStream = updateURL.openStream();
//
//			System.out.println("Updating server list.");
//			byte[] inChars = new byte[64];
//			int read;
//			StringBuffer serverText = new StringBuffer();
//			while ((read = serverStream.read(inChars)) != -1) {
//				String instr = new String(inChars, 0, read);
//				serverText.append(instr);
//			}
//			serverStream.close();
//
//			StringTokenizer st = new StringTokenizer(serverText.toString(), "\n");
//			while (st.hasMoreTokens()) {
//				String token = st.nextToken();
//				if (token.startsWith("beshare_addserver")) {
//					StringTokenizer tt = new StringTokenizer(token, "#=");
//					while (tt.hasMoreTokens()) {
//						tt.nextToken();
//						serverList.addElement(tt.nextToken().trim());
//						tt.nextToken();
//					}
//				} else if (token.startsWith("beshare_removeserver")) {
//					StringTokenizer tt = new StringTokenizer(token, "#=");
//					while (tt.hasMoreTokens()) {
//						tt.nextToken();
//						removeList.addElement(tt.nextToken().trim());
//						tt.nextToken();
//					}
//				}
//			}
//		} catch (IOException ioe) {
//			System.out.println(ioe.toString());
//		}
//
//		pnl.addServers(serverList);
//		pnl.removeServers(removeList);
//	}
//
//
//	/**
//	 * Returns the size of the add server list.
//	 *
//	 * @return The size of the add server list.
//	 * @deprecated Use getAddListSize() instead.
//	 */
//	public int listSize() {
//		return serverList.size();
//	}
//
//	/**
//	 * Retrieves a server name on the add list.
//	 *
//	 * @return The Server name int the add list at <code>index</code>.
//	 * @deprecated Use getAddServer(int) instead.
//	 */
//	public String getServer(int index) {
//		return (String) serverList.elementAt(index);
//	}
//
//	/**
//	 * Returns the size of the add server list.
//	 *
//	 * @return The size of the add server list.
//	 */
//	public int getAddListSize() {
//		return serverList.size();
//	}
//
//	/**
//	 * @return The Server name int the add list at <code>index</code>.
//	 */
//	public String getAddServer(int index) {
//		return (String) serverList.elementAt(index);
//	}
//
//	/**
//	 * @return The size of the remove server list.
//	 */
//	public int getRemoveListSize() {
//		return removeList.size();
//	}
//
//	/**
//	 * @return the Server name in the remove list at <code>index</code>.
//	 */
//	public String getRemoveServer(int index) {
//		return (String) removeList.elementAt(index);
//	}
//}
