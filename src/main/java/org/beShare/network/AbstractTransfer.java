package org.beShare.network;

import com.meyer.muscle.client.MessageTransceiver;
import com.meyer.muscle.message.Message;
import com.meyer.muscle.thread.MessageListener;
import com.meyer.muscle.thread.MessageQueue;

import javax.swing.BoundedRangeModel;
import javax.swing.JProgressBar;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * AbstractTransfer contains some of the utility and common functionality needed to download and share files.
 *
 * @author Bryan Varner
 */
public abstract class AbstractTransfer implements Runnable {
	private boolean hasRun;
	private TransferStatus status;

	// Constants - translated from the BeShare source (where they were 'xxxx' style into their int equivalents.
	final static int TRANSFER_COMMAND_CONNECTED_TO_PEER = 1953720434; // a.k.a. 'tshr'
	final static int TRANSFER_COMMAND_DISCONNECTED_FROM_PEER = 1953720435;
	final static int TRANSFER_COMMAND_FILE_LIST = 1953720436; // a list of files the remote user would like to download
	final static int TRANSFER_COMMAND_FILE_HEADER = 1953720437; // contains filename, attributes, etc.
	final static int TRANSFER_COMMAND_FILE_DATA = 1953720438; // a chunk of the file's contents
	final static int TRANSFER_COMMAND_DEPRECATED = 1953720439; // was the on-empty message
	final static int TRANSFER_COMMAND_NOTIFY_QUEUED = 1953720440; // tells the receiving session he's being put on a wait list to download

	// Data Munging
	final static int MUNGE_MODE_OFF = 0;
	final static int MUNGE_MODE_XOR = 1;

	// Objects that signal when we get connected / disconnected.
	static final Object serverConnect = new Object();
	static final Object serverDisconnect = new Object();

	// A transceiver for communicating directly with the remote client for the transfer
	protected MessageTransceiver transferTransceiver;

	// The main transceiver for communicating with the main MUSCLE server.
	protected JavaShareTransceiver mainTransceiver;

	// Items to transfer, and the current item index.
	protected ArrayList<TransferItem> items;
	private int currentItem = -1;
	protected boolean connected = false;

	private DecimalFormat progressFormatter = new DecimalFormat("####.##");

	/**
	 * Constructs a Transfer associated to the given JavaShareTransceiver.
	 *
	 * @param mainTransceiver
	 */
	protected AbstractTransfer(final JavaShareTransceiver mainTransceiver, final Collection<TransferItem> items) {
		this.hasRun = false;
		this.items = new ArrayList<>(items);
		this.mainTransceiver = mainTransceiver;
		this.transferTransceiver = new MessageTransceiver(new MessageQueue(new MessageListener() {
			@Override
			public synchronized void messageReceived(Object message, int numLeftInQueue) throws Exception {
				if (message instanceof Message) {
					muscleMessageReceived((Message) message);
				} else if (message instanceof Socket) {
					socketConnected((Socket) message);
				} else if (message == serverConnect) {
					connected = true;
					connected();
				} else if (message == serverDisconnect) {
					disconnected();
					connected = false;
				}
			}
		}));
		this.status = TransferStatus.LOCALLY_QUEUED;
	}

	/**
	 * Returns true if this transfer is currently active.
	 *
	 * @return
	 */
	public boolean isActive() {
		return !TransferStatus.INACTIVE_STATES.contains(status);
	}

	/**
	 * Returns true if this Transfer's run() method has been invoked.
	 *
	 * @return
	 */
	public boolean hasRun() {
		return hasRun;
	}

	/**
	 * Performs an XOR on the data in the byte array.
	 */
	protected static byte[] XORData(byte[] data) {
		for (int x = 0; x < data.length; x++) {
			data[x] ^= 0xFF;
		}
		return data;
	}

	/**
	 * Returns a read-only view of the Items to transfer.
	 *
	 * @return
	 */
	public List<TransferItem> getItems() {
		return Collections.unmodifiableList(items);
	}

	/**
	 * Gets the current item, or null.
	 *
	 * @return
	 */
	public TransferItem getCurrentItem() {
		if (currentItem >= 0) {
			return items.get(currentItem);
		}
		return null;
	}

	/**
	 * Sets the index of the current item.
	 *
	 * @param i
	 */
	protected void setCurrentItem(int i) {
		if (i >= 0 && i > items.size() - 1) {
			throw new ArrayIndexOutOfBoundsException("Index " + i + " out of bounds for items list, size: " + items.size());
		}
		this.currentItem = i;
	}

	/**
	 * @return the status of this transfer.
	 */
	public TransferStatus getStatus() {
		return status;
	}

	/**
	 * Updates the status of this Transfer
	 *
	 * @param status
	 */
	protected void setStatus(final TransferStatus status) {
		this.status = status;
		this.mainTransceiver.getTransferModel().update(this);
	}

	/**
	 * Starts this transfer as a thread.
	 */
	public final void run() {
		hasRun = true;
		setStatus(TransferStatus.CONNECTING);

		// If we can't connect, set an error.
		if (!start()) {
			setStatus(TransferStatus.ERROR);
		}
	}


	/**
	 *  Invoked when the muscle transciever sends a connected signal.
	 */
	protected void connected() {
		return;
	}

	/**
	 * Invoked when the muscle transceiver sends a disconnected signal.
	 */
	protected void disconnected() {
		return;
	}

	/**
	 * Invoked when the muscle transceiver sends a socket.
	 *
	 * @param socket The socket which has accept()ed a connection.
	 */
	protected void socketConnected(final Socket socket) {
		return;
	}

	/**
	 * Invoked when our thread starts up, to start up the transceiver.
	 *
	 * @return true if the transceiver is started, false if not.
	 */
	protected abstract boolean start();

	/**
	 * A muscle message has been received! YEeeeeahhh!
	 */
	protected abstract void muscleMessageReceived(final Message message);

	/**
	 * Invoked when the transfer is aborted by the user.
	 */
	abstract void abort();

	public long getTotalBytes() {
		long accumulator = 0;
		for (TransferItem item : items) {
			accumulator += item.getSize();
		}
		return accumulator;
	}

	public long getTransferredBytes() {
		long accumulator = 0;
		for (TransferItem item : items) {
			accumulator += item.getTransferred();
		}
		return accumulator;
	}

	public String updateProgress(JProgressBar progress) {
		if (status.equals(TransferStatus.ACTIVE)) {
			progress.setIndeterminate(false);
			progress.setValue((int) (((double) getTransferredBytes() / getTotalBytes()) * 100));
			if (getTotalBytes() > (1024 * 1024)) {
				return progressFormatter.format((double) getTransferredBytes() / (1024 * 1024)) + "MB of " + progressFormatter.format((double) getTotalBytes() / (1024 * 1024)) + "MB";
			} else if (getTotalBytes() > 1024) {
				return progressFormatter.format((double) getTransferredBytes() / 1024) + "k of " + progressFormatter.format((double) getTotalBytes() / 1024) + "k";
			} else {
				return getTransferredBytes() + " of " + getTotalBytes() + " bytes";
			}
		} else if (status.equals(TransferStatus.REMOTELY_QUEUED) || status.equals(TransferStatus.LOCALLY_QUEUED) || status.equals(TransferStatus.ERROR)) {
			progress.setIndeterminate(false);
		} else if (status.equals(TransferStatus.COMPLETE)) {
			progress.setIndeterminate(false);
			progress.setValue(100);
		}
		return "";
	}
}
