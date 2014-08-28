package org.beShare.network;

import javax.swing.AbstractListModel;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

/**
 * Manages queueing/starting/stopping of file-transfers.
 */
public class TransferModel extends AbstractListModel<AbstractTransfer> {
	TransferList uploads;
	TransferList downloads;

	/**
	 * Constructor - not much to see here, really.
	 */
	public TransferModel(final Preferences prefs) {
		uploads = new TransferList(prefs.getInt("concUploads", 2));
		downloads = new TransferList(prefs.getInt("concDownloads", 3));

		prefs.addPreferenceChangeListener(new PreferenceChangeListener() {
			@Override
			public void preferenceChange(PreferenceChangeEvent evt) {
				if ("concUploads".equals(evt.getKey())) {
					uploads.setMax(prefs.getInt("concUploads", 2));
				} else if ("concDownloads".equals(evt.getKey())) {
					downloads.setMax(prefs.getInt("concDownloads", 3));
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
	public synchronized AbstractTransfer getElementAt(int index) {
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
	public synchronized void add(AbstractTransfer t) {
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
	public synchronized void remove(AbstractTransfer t) {
		int oldSize = getSize();
		if (t instanceof Download) {
			downloads.remove(t);
			fireIntervalRemoved(this, 0, oldSize);
//		} else if (t instanceof Upload) {
//			uploads.remove(t);
//			fireIntervalRemoved(this, 0, oldSize);
		}
	}

	/**
	 * Removes the Transfer at the given index.
	 *
	 * @param index
	 */
	public synchronized void remove(int index) {
		if (index < downloads.size()) {
			downloads.remove(index);
		} else if (index - downloads.size() < uploads.size()) {
			uploads.remove(index - downloads.size());
		}
	}

	/**
	 * AbstractTransfers can tell this model their state has been altered.
	 *
	 * @param t
	 */
	void update(AbstractTransfer t) {
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

		if (!t.isActive()) {
			downloads.startNextPending();
			uploads.startNextPending();
		}
	}
}
