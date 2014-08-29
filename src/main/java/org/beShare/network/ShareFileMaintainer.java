/* Change Log:
	7.17.2002 - Threading works properly. Needs to have the file list vector stuff implemented.
	8.2.2002 - Finished class, re-worked to use sleep() instead of a GUI thread based timer.
				Everything seems to be working for this class, calling it 'done'.
*/
package org.beShare.network;

import org.beShare.data.SharedFile;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

/**
 * The piece of JavaShare that monitors the shared directory and sends the
 * necessary data to the JavaShareTransceiver interface. :-)
 *
 * @author Bryan Varner
 * @version 1.0 - 8.2.2002
 */
public class ShareFileMaintainer implements Runnable, ActionListener {
	int delay;
	int oneSecond = 1000; // millisecond multiplier.

	Vector fileList;

	JavaShareTransceiver connection;

	Timer updateTimer;

	String basePath;

	String serverName;

	/**
	 * Create a new ShareFileMaintainer with <code>delay</code> seconds between automatic updates,
	 * <code>connection</code> as the interface to send the file list to, and
	 * <code>prefsMessage</code> to read the settings from.
	 */
	public ShareFileMaintainer(JavaShareTransceiver connection) {
		this.connection = connection;
		fileList = new Vector();

		delay = 300 * oneSecond;

		updateTimer = new Timer(delay, this);
		basePath = "";
	}

	/**
	 * Gets the delay between updates.
	 */
	public int getDelay() {
		return delay / oneSecond;
	}

	/**
	 * Sets the delay between updates.
	 */
	public void setDelay(int delay) {
		updateTimer.stop();

		if (delay > 0) {
			this.delay = delay * oneSecond;
			updateTimer.setDelay(this.delay);
		}

		if (delay > 0) {
			updateTimer.start();
		}
	}

	/**
	 * sets the share folder. This will force an update.
	 */
	public void setSharePath(String path) {
		basePath = path;
		updateList();
	}

	/**
	 * Starts this bad-boy as a self-sustaining thread.
	 */
	public void run() {
		try {
			updateList();
			updateTimer.start();
		} catch (Exception ignored) {
		}
	}

	/**
	 * Implements the action-listener. The only thing this should receive events from is it's
	 * own timer. It's possible to get events from an outside source. Any event will force an
	 * update of the current file list.
	 */
	public void actionPerformed(ActionEvent e) {
		updateList();
	}

	/**
	 * Updates the file-list. The compare operations here are probably over-commented.
	 * Here's the jist, it rebuilds the file vector if it needs to, and dosen't if it's
	 * not necessary. It uses several tests to decide what to do.
	 */
	public void updateList() {
//		// If we aren't connected, why bother?
//		if (connection.isConnected()) {
//			if (fileList.size() == 0) {
//				// Empty fileList means we are
//				//	1. sharing nothing and this will execute fast,
//				//	2. Connecting to a server.
////				if (prefs.hasField("sharedPath")) {
////					String sharedPath;
////					try {
////						sharedPath = prefs.getString("sharedPath");
////						// Create a File Object for the base path.
////
////						File baseDir = new File(sharedPath);
////						// If there's a file selected, grab it's parent directory to share.
////						if (!baseDir.isDirectory()) {
////							if (baseDir.getParent() != null) {
////								baseDir = new File(baseDir.getParent());
////							} else {
////								// We have a file with no parent shared - theoretically not
////								// possible. So we'll disable sharing and bail out.
////								sharingDisabled();
////								return;
////							}
////						}
////
////						recurseDirectory(baseDir);
////						uploadList();
////						serverName = connection.getServerName();
////					} catch (Exception e) {
////						System.out.println(e.toString());
////					}
////				}
//			} else {
//				// In this case we have an existing fileList thats got something in it.
//				// We need to find out if it's up to date and accurate.
//				// This copy block is ugly, but it is 1.1 safe.
//				Vector oldFileList = new Vector();
//				for (int x = 0; x < fileList.size(); x++) {
//					oldFileList.addElement(fileList.elementAt(x));
//				}
//
//				while (fileList.size() > 0) {
//					fileList.removeElementAt(0);
//				}
//
////				try {
////					String sharedPath = prefs.getString("sharedPath");
////					// Create a File Object.
////					File baseDir = new File(sharedPath);
////					// Make sure we have a directory shared.
////					if (!baseDir.isDirectory()) {
////						if (baseDir.getParent() != null) {
////							baseDir = new File(baseDir.getParent());
////						} else {
////							sharingDisabled();
////							return;
////						}
////					}
////
////					recurseDirectory(baseDir);
////				} catch (Exception e) {
////					System.out.println(e.toString());
////				}
//
//				// Test #1, are they the same size? If not, instant update.
//				if (oldFileList.size() != fileList.size()) {
//					uploadList();
//				} else if (!serverName.equals(connection.getServerName())) {
//					// Our connection has changed servers. Clear out all our list data,
//					// (we need to rebuild it), then force an update immediately.
//					while (fileList.size() > 0) {
//						fileList.removeElementAt(0);
//					}
//					serverName = connection.getServerName();
//					updateList();
//				} else {
//					// We couldn't find any cosmetic changes, so now it's off to the races.
//					// Item by item comparison of the contents of the vectors.
//					int x = 0;
//					while (x < fileList.size()) {
//						if (!((SharedFile) fileList.elementAt(x)).equals(((SharedFile) fileList.elementAt(x)))) {
//							break;
//						}
//						x++;
//					}
//					// See if we bailed out before the full size(), something didn't match.
//					if (x < fileList.size()) {
//						fileList = new Vector(oldFileList);
//						// Re-send fileList to JavaShareTransceiver!
//						uploadList();
//						serverName = connection.getServerName();
//					}
//				}
//			}
//		} else {
//			// fudge the servername here. Once we have a solid connection
//			// everything gets re-flushed anyhow.
//			serverName = "";
//		}
	}

	/**
	 * This clears the list, and disables the sharing.
	 */
	public void sharingDisabled() {
		setDelay(-1);
		serverName = "";
		while (fileList.size() > 0) {
			fileList.removeElementAt(0);
		}
		connection.uploadFileListing(fileList);
		connection.removeFileListing();
	}

	/**
	 * Clears the internal list, forces a server change,
	 * and re-uploads the file list.
	 */
	public void resetShareList() {
		connection.removeFileListing();
		serverName = "";
		while (fileList.size() > 0) {
			fileList.removeElementAt(0);
		}
		updateList();
	}


	/**
	 * Sets mime-types, and sends an upload command to tranto.
	 */
	private void uploadList() {
		setMimeTypes();
		connection.uploadFileListing(fileList);
	}

	/**
	 * Recursively calls itself to populate the class member fileList with entries.
	 */
	private void recurseDirectory(File baseDir) {
		String[] childList = baseDir.list();
		for (int x = 0; x < childList.length; x++) {
			File childFile = new File(baseDir, childList[x]);
			if (childFile.isDirectory()) {
				recurseDirectory(childFile);
			} else {
				fileList.addElement(new SharedFile(childFile));
			}
		}
	}

	/**
	 * Sets mime-types for files in the fileList vector.
	 * Does a live match against the current mime-type database.
	 */
	private void setMimeTypes() {
//		for (int x = 0; x < fileList.size(); x++) {
//			// set the Mime-type of that last element you added.
//			// Find the .extension of the file.
//			SharedFile childFile = (SharedFile) fileList.elementAt(x);
//
//			int extensionOffset = childFile.getName().lastIndexOf(".");
//			if (extensionOffset > -1) {
//				String extension = childFile.getName().substring(
//						                                                childFile.getName().lastIndexOf("."),
//						                                                childFile.getName().length());
//				if (prefs.hasField(extension)) {
//					try {
//						((SharedFileInfoHolder) fileList.elementAt(x)).setKind(prefs.getString(extension));
//					} catch (Exception e) {
//					}
//				}
//			}
//		}
	}
}
