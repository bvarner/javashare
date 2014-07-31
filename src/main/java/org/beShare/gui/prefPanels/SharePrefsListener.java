/* ChangeLog:
	7.16.2002 - Created the Class.
*/
package org.beShare.gui.prefPanels;

/**
 * Interface for responding to File Sharing setting changes.
 */
public interface SharePrefsListener {	// File-Transfer Settings
	public void sharingEnableChange(boolean enabled);
	
	public void sharePathChanged();
	
	public void shareUpdateDelayChanged(int delay);
	
	public void shareAutoUpdateEnableChange(boolean enabled);
}
