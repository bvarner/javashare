package org.beShare.network;

import javax.swing.AbstractListModel;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

/**
 * Manages queueing/starting/stopping of file-transfers.
 */
public class TransferModel extends AbstractListModel<AbstractTransfer> {
	TransferList uploads;
	TransferList downloads;

	/**
	 * Constructor - not much to see here, really.
	 */
	public TransferModel(final JavaShareTransceiver connection) {
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
	@Override
	public AbstractTransfer getElementAt(int index) {
		if (index < downloads.size()) {
			return downloads.get(index);
		} else if (index > downloads.size() && (index < downloads.size() + uploads.size())) {
			return uploads.get(index - downloads.size());
		} else {
			return null;
		}
	}

	/**
	 * Adds a Transfer To the appropriate Queue and registers this object to receive status change notifications.
	 */
	public void add(AbstractTransfer t) {
		t.managedBy(this);
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
	public void remove(AbstractTransfer t) {
		t.managedBy(null);
		if (t instanceof Download) {
			downloads.remove(t);
			fireIntervalRemoved(this, 0, getSize());
//		} else if (t instanceof Upload) {
//			uploads.remove(t);
//			fireIntervalRemoved(this, 0, getSize());
		}
	}

	/**
	 * AbstractTransfers can tell this model their state has been altered.
	 * @param t
	 */
	void statusChanged(AbstractTransfer t) {
		int index = downloads.indexOf(t);
		if (index == -1) {
			index = uploads.indexOf(t);
			if (index != -1) {
				// Upload changed
				index += downloads.size();
			} else {
				// Couldn't figure out what changed.
				return;
			}
		}

		fireContentsChanged(this, index, index);
		downloads.checkQueue();
		uploads.checkQueue();
	}
}
