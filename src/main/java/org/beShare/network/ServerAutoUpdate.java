package org.beShare.network;

import com.meyer.muscle.thread.ThreadPool;
import org.beShare.gui.AbstractDropMenuModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

/**
 * <p>ServerAutoUpdate - A runnable that updates a DefaultDropMenuModel for available server names.
 *
 * @author Bryan Varner
 */
class ServerAutoUpdate implements Runnable {
	private static final String SERVER_LIST_URL = "http://beshare.tycomsystems.com/servers.txt";
	private AbstractDropMenuModel<String> serverModel;
	private Preferences preferences;

	/**
	 * Creates a new ServerAutoUpdate that modifies the given model.
	 */
	public ServerAutoUpdate(AbstractDropMenuModel<String> serverModel, Preferences preferences) {
		this.serverModel = serverModel;
		this.preferences = preferences;
		preferences.addPreferenceChangeListener(new PreferenceChangeListener() {
			@Override
			public void preferenceChange(PreferenceChangeEvent evt) {
				if (evt.getKey().equals("autoUpdateServers")) {
					ThreadPool.getDefaultThreadPool().startThread(ServerAutoUpdate.this);
				}
			}
		});
	}

	public void run() {
		if (preferences.getBoolean("autoUpdateServers", true)) {
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
				ioe.printStackTrace();
			}
		}
	}
}
