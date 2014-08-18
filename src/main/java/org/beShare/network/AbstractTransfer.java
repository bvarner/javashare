package org.beShare.network;

import com.meyer.muscle.client.MessageTransceiver;
import com.meyer.muscle.message.Message;
import com.meyer.muscle.thread.MessageListener;
import com.meyer.muscle.thread.MessageQueue;

import java.io.File;
import java.util.Vector;

/**
 * Defines an abstract class for transferring files.
 *
 * @author Bryan Varner
 */
public abstract class AbstractTransfer extends Thread implements MessageListener {

	// Status flags.
	public final static int NO_STATUS = -1;
	int status = NO_STATUS;
	public final static int CONNECTING = 1000;
	public final static int AWAITING_CALLBACK = 1001;
	public final static int ACTIVE = 1002;
	public final static int REMOTE_QUEUED = 1003;
	public final static int EXAMINING = 1004;
	public final static int FINISHED = 1005;
	public final static int LOCALLY_QUEUED = 1006;
	public final static int ERROR = 1007;
	// Constants - translated from the BeShare source (where they were 'xxxx' style into their int equivalents.
	final static int TRANSFER_COMMAND_CONNECTED_TO_PEER = 1953720434; // a.k.a. 'tshr'
	final static int TRANSFER_COMMAND_DISCONNECTED_FROM_PEER = 1953720435;
	final static int TRANSFER_COMMAND_FILE_LIST = 1953720436; // a list of files the remote user would like to download
	final static int TRANSFER_COMMAND_FILE_HEADER = 1953720437; // contains filename, attributes, etc.
	final static int TRANSFER_COMMAND_FILE_DATA = 1953720438; // a chunk of the file's contents
	final static int TRANSFER_COMMAND_DEPRECATED = 1953720439; // was the on-empty message
	final static int TRANSFER_COMMAND_NOTIFY_QUEUED = 1953720440;
	// tells the receiving session he's being put on a wait list to download
	// Data Munging
	final static int MUNGE_MODE_OFF = 0;
	final static int MUNGE_MODE_XOR = 1;
	// Objects that signal when we get connected / disconnected.
	static final Object serverConnect = new Object();
	static final Object serverDisconnect = new Object();
	boolean started = false;
	// Server stuff.
	MessageTransceiver beShareTransceiver = new MessageTransceiver(new MessageQueue(this));
	boolean connected = false;
	// File tracking
	File currentFile = null;
	long totalFileSize = 0;
	long transferedSize = 0;

	Vector transferListenVect;

	protected AbstractTransfer() {
		status = NO_STATUS;
		started = false;
		transferListenVect = new Vector();
	}

	/**
	 * Performs an XOR on the data in the byte array.
	 */
	public static byte[] XORData(byte[] data) {
		for (int x = 0; x < data.length; x++) {
			data[x] ^= 0xFF;
		}
		return data;
	}

	/**
	 * @return the status of this transfer.
	 */
	public synchronized int getStatus() {
		return status;
	}

	/**
	 * Sets the status of this transfer.
	 */
	public synchronized void setStatus(int s) {
		status = s;
		fireProgressChange();
	}

	/**
	 * Starts up our thread, and connects to the server.
	 */
	public void run() {
		started = true;
		connect();
	}

	/**
	 * Open a connection to the remote host.
	 */
	public abstract void connect();

	/**
	 * Disconnect the existing connection to the remote host.
	 */
	public abstract void disconnect();

	/**
	 * You've got a message in your queue!
	 */
	public abstract void messageReceived(Object message, int numleft);

	/**
	 * A muscle message has been received! YEeeeeahhh!
	 */
	public abstract void muscleMessageReceived(Message message);

	/**
	 * @return the name of the currently transferring file.
	 */
	public String getFileName() {
		return currentFile.getName();
	}

	/**
	 * @return the number of bytes that have been transfered.
	 */
	public long getFileTransfered() {
		return transferedSize;
	}

	/**
	 * Sets the number of bytes transfered.
	 */
	protected void setFileTransfered(long transfered) {
		transferedSize = transfered;
		fireProgressChange();
	}

	/**
	 * @return the size of the file (in bytes) that is being transfered.
	 */
	public long getFileSize() {
		return totalFileSize;
	}

	/**
	 * Sets the total size of the current file in bytes.
	 */
	protected void setFileSize(long size) {
		totalFileSize = size;
	}

	/**
	 * Aborts this transfer, all files in the list are halted.
	 */
	public void abort() {
		disconnect();
		setStatus(ERROR);
		started = false;
		System.out.println("ABORTED!");
	}

	/**
	 * @return true if connected, false if not.
	 */
	public synchronized boolean isConnected() {
		return connected;
	}

	/**
	 * @return true if the transfers thread has been started, false if not.
	 */
	public synchronized boolean isStarted() {
		return started;
	}

	/**
	 * Adds a Transfer Progress Listener to be notified when progress or status changes.
	 */
	public void addTransferProgressListener(TransferProgressListener l) {
		transferListenVect.addElement(l);
	}

	/**
	 * Sends a signal to all listeners that this transfer has had progress made.
	 */
	protected void fireProgressChange() {
		for (int x = 0; x < transferListenVect.size(); x++) {
			((TransferProgressListener) transferListenVect.elementAt(x)).transferStatusUpdate(this);
		}
	}
}
