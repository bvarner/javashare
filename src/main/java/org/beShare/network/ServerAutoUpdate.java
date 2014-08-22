package org.beShare.network;

import org.beShare.gui.AbstractDropMenuModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.StringTokenizer;

/**
 * <p>ServerAutoUpdate - A runnable that updates a DefaultDropMenuModel for available server names.
 *
 * @author Bryan Varner
 */
class ServerAutoUpdate implements Runnable {
	private static final String SERVER_LIST_URL = "http://beshare.tycomsystems.com/servers.txt";
	private AbstractDropMenuModel<String> serverModel;

	/**
	 * Creates a new ServerAutoUpdate that modifies the given model.
	 */
	public ServerAutoUpdate(AbstractDropMenuModel<String> serverModel) {
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
						String serverName = tt.nextToken().trim();
						if (!serverModel.contains(serverName)) {
							serverModel.addElement(serverName);
						}
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
