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
		if (t instanceof Download) {
			int index = downloads.size();
			downloads.add(t);
			fireIntervalAdded(this, index, index);
//		} else if (t instanceof Upload) {
//			uploads.add(t);
//			fireIntervalAdded(this, 0, getSize());
		}
	}

	/**
	 * Removes the Transfer
	 */
	public void remove(AbstractTransfer t) {
		int index = indexOf(t);

		boolean removed = downloads.remove(t);
		if (!removed) {
			removed = uploads.remove(t);
		}

		if (index >= 0) {
			fireIntervalRemoved(this, index, index);
		}
	}


	public AbstractTransfer remove(int index) {
		AbstractTransfer removed;
		if (index < downloads.size()) {
			removed = downloads.remove(index);
		} else {
			removed = uploads.remove(index - downloads.size());
		}
		fireIntervalRemoved(this, index, index);
		return removed;
	}

	/**
	 * Gets the index of the given transfer.
	 *
	 * @param t
	 * @return
	 */
	public int indexOf(AbstractTransfer t) {
		int index = downloads.indexOf(t);
		if (index == -1) {
			index = uploads.indexOf(t);
			if (index != -1) {
				index += downloads.size();
			}
		}
		return index;
	}

	/**
	 * AbstractTransfers can tell this model their state has been altered.
	 *
	 * @param t
	 */
	void update(AbstractTransfer t) {
		int index = indexOf(t);
		if (index != -1) {
			fireContentsChanged(this, index, index);

			if (!t.isActive()) {
				downloads.startNextPending();
				uploads.startNextPending();
			}
		}
	}
}
