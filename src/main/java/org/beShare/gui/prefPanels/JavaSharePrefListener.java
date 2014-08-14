/* ChangeLog:
	7.16.2002 - code Clean up.
*/
package org.beShare.gui.prefPanels;

import java.awt.*;

/**
 * Interface for responding to changes in the preferences.
 */
public interface JavaSharePrefListener {
	// General Preferences
	public void autoAwayTimerChange(int time, int selectedIndex); // -1 represents disabled.

	public void autoUpdateServerChange(boolean autoUpdate);

	public void firewallSettingChange(boolean firewalled);

	public void loginOnStartupChange(boolean login);

	public void userSortChange(boolean sort);

	// Connection Preferences.
	public void bandwidthChange(String label, int speed, int index);

	public void socksChange(String server, int port);

	// Display Preferences.
	public void timeStampDisplayChange(boolean show);

	public void userEventDisplayChange(boolean show);

	public void uploadDisplayChange(boolean show);

	public void chatDisplayChange(boolean show);

	public void privateDisplayChange(boolean show);

	public void infoDisplayChange(boolean show);

	public void warningDisplayChange(boolean show);

	public void errorDisplayChange(boolean show);

	// Appearance Preferences
	public void updateChatFont(Font f);

	public void lafChange(String plafClassName);

	// Sound prefs
	public void soundPackChange(String soundPackName);

	public void soundOnUserNameChange(boolean signal);

	public void soundOnPrivateWindowChange(boolean signal);

	public void soundOnPrivateMessageChange(boolean signal);

	public void soundOnWatchPatternChange(boolean signal);
}
