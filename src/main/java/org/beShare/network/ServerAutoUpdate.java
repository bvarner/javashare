package org.beShare.network;

import org.beShare.DefaultDropMenuModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.StringTokenizer;

/**
 * <p>ServerAutoUpdate - A Class which takes a URL, parses the file for BeShare
 * server information, and creates a list of servers based upon it.
 * To do the work, call Run() - it's a separate thread! Yeah!
 * <p/>
 * <p>Class Started: 2-08-2002
 * <p>Last Update: 12-19-2002
 *
 * @author Bryan Varner
 * @version 2.0
 */
class ServerAutoUpdate implements Runnable {
	private static final String SERVER_LIST_URL = "http://beshare.tycomsystems.com/servers.txt";
	private DefaultDropMenuModel<String> serverModel;

	/**
	 * Creates a new ServerAutoUpdate
	 */
	public ServerAutoUpdate(DefaultDropMenuModel<String> serverModel) {
		this.serverModel = serverModel;
	}

	public void run() {
		try (BufferedReader in = new BufferedReader(new InputStreamReader(new URL(SERVER_LIST_URL).openStream()))) {
			String line;
			while ((line = in.readLine()) != null) {
				if (line.startsWith("beshare_addserver")) {
					StringTokenizer tt = new StringTokenizer(line, "#=");
					while (tt.hasMoreTokens()) {
						tt.nextToken();
						serverModel.addElement(tt.nextToken().trim());
						tt.nextToken();
					}
				} else if (line.startsWith("beshare_removeserver")) {
					StringTokenizer tt = new StringTokenizer(line, "#=");
					while (tt.hasMoreTokens()) {
						tt.nextToken();
						serverModel.removeElement(tt.nextToken().trim());
						tt.nextToken();
					}
				}
			}
		} catch (IOException ioe) {
			System.err.println("Could not update server list from " + SERVER_LIST_URL + "\n" + ioe.toString());
		}
	}
}
