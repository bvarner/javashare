package org.beShare.network;

import com.meyer.muscle.message.Message;

import javax.swing.*;

/**
 * Manages queueing/starting/stopping of file-transfers.
 */
public class TransferManager extends AbstractListModel implements TransferProgressListener {
	TransferQueue upQueue;
	TransferQueue downQueue;

	String baseDownloadPath;
	String baseSharedPath;

	JavaShareTransceiver connection;
	Message prefsMessage;

	/**
	 * Constructor - not much to see here, really.
	 */
	public TransferManager(JavaShareTransceiver connection, Message prefsMessage) {
		this.connection = connection;
		this.prefsMessage = prefsMessage;

		try {
			baseDownloadPath = prefsMessage.getString("downloadPath");
		} catch (Exception e) {
			baseDownloadPath = System.getProperty("user.home") + System.getProperty("path.Separator") + "download";
		}

		// Read Max Transfer Settings from prefsMessage
		upQueue = new TransferQueue(2);
		downQueue = new TransferQueue(3);
	}

	/**
	 * Returns the size of the collective queues
	 */
	public int getSize() {
		return downQueue.size() + upQueue.size();
	}

	/**
	 * Returns the element at location in the combined list of queues
	 */
	public Object getElementAt(int index) {
		if (index < downQueue.size()) {
			return downQueue.elementAt(index);
		} else if (index > downQueue.size() && (index < downQueue.size() + upQueue.size())) {
			return upQueue.elementAt(index - downQueue.size());
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
			downQueue.addTransfer(t);
			fireIntervalAdded(this, 0, getSize());
			//} else if (t instanceof Upload) {
			//	upQueue.addTransfer(t);
			//	fireIntervalAdded(this, 0, getSize());
		}
	}

	/**
	 * Removes the Transfer from the Queues, this will halt the transfer.
	 */
	public void removeTransfer(AbstractTransfer t) {
		if (t instanceof Download) {
			downQueue.removeTransfer(t);
			fireIntervalRemoved(this, 0, getSize());
			//} else if (t instanceof Upload) {
			//	upQueue.removeTransfer(t);
			//	fireIntervalRemoved(this, 0, getSize());
		}
	}

	/**
	 * Implements the TransferProgressListener.
	 * This forces the List to update the transfer that fires off the change.
	 * It also forces the Queues to check if they should start the next transfer.
	 */
	public void transferStatusUpdate(AbstractTransfer t) {
		int index = downQueue.indexOf(t);
		if (index == -1) {
			index = upQueue.indexOf(t);
			if (index != -1) {
				// Upload changed
				index += downQueue.size();
			} else {
				// Couldn't figure out what changed.
				fireContentsChanged(this, 0, getSize());
				index = 0;
			}
		}

		fireContentsChanged(this, index, index);
		downQueue.checkQueue();
		upQueue.checkQueue();
	}

	/**
	 * @return the Path to Download files to.
	 */
	public String getDownloadPath() {
		return baseDownloadPath;
	}
}
