package org.beShare.network;

import javax.swing.AbstractListModel;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

/**
 * Manages queueing/starting/stopping of file-transfers.
 */
public class TransferManager extends AbstractListModel implements TransferProgressListener {
	TransferList uploads;
	TransferList downloads;

	String baseDownloadPath;
	String baseSharedPath;

	JavaShareTransceiver connection;

	/**
	 * Constructor - not much to see here, really.
	 */
	public TransferManager(final JavaShareTransceiver connection) {
		this.connection = connection;
		this.baseDownloadPath =
				connection.getPreferences().get("downloadLocation", System.getProperty("user.home") + System.getProperty("path.Separator") + "Downloads");

		uploads = new TransferList(connection.getPreferences().getInt("concUploads", 2));
		downloads = new TransferList(connection.getPreferences().getInt("concDownloads", 3));

		connection.getPreferences().addPreferenceChangeListener(new PreferenceChangeListener() {
			@Override
			public void preferenceChange(PreferenceChangeEvent evt) {
				if ("concUploads".equals(evt.getKey())) {
					uploads.setMax(connection.getPreferences().getInt("concUploads", 2));
				} else if ("concDownloads".equals(evt.getKey())) {
					downloads.setMax(connection.getPreferences().getInt("concDownloads", 3));
				}
			}
		});
	}

	/**
	 * Returns the size of the collective queues
	 */
	public int getSize() {
		return downloads.size() + uploads.size();
	}

	/**
	 * Returns the element at location in the combined list of queues
	 */
	public Object getElementAt(int index) {
		if (index < downloads.size()) {
			return downloads.get(index);
		} else if (index > downloads.size() && (index < downloads.size() + uploads.size())) {
			return uploads.get(index - downloads.size());
		} else {
			return "";
		}
	}

	/**
	 * Adds a Transfer To the appropriate Queue and registers this object to receive status change notifications.
	 */
	public void addTransfer(AbstractTransfer t) {
		t.addTransferProgressListener(this);
		if (t instanceof Download) {
			downloads.add(t);
			fireIntervalAdded(this, 0, getSize());
//		} else if (t instanceof Upload) {
//			uploads.add(t);
//			fireIntervalAdded(this, 0, getSize());
		}
	}

	/**
	 * Removes the Transfer from the Queues, this will halt the transfer.
	 */
	public void removeTransfer(AbstractTransfer t) {
		if (t instanceof Download) {
			downloads.remove(t);
			fireIntervalRemoved(this, 0, getSize());
//		} else if (t instanceof Upload) {
//			uploads.remove(t);
//			fireIntervalRemoved(this, 0, getSize());
		}
	}

	/**
	 * Implements the TransferProgressListener.
	 * This forces the List to update the transfer that fires off the change.
	 * It also forces the Queues to check if they should start the next transfer.
	 */
	public void transferStatusUpdate(AbstractTransfer t) {
		int index = downloads.indexOf(t);
		if (index == -1) {
			index = uploads.indexOf(t);
			if (index != -1) {
				// Upload changed
				index += downloads.size();
			} else {
				// Couldn't figure out what changed.
				fireContentsChanged(this, 0, getSize());
				index = 0;
			}
		}

		fireContentsChanged(this, index, index);
		downloads.checkQueue();
		uploads.checkQueue();
	}

	/**
	 * @return the Path to Download files to.
	 */
	public String getDownloadPath() {
		return baseDownloadPath;
	}
}
