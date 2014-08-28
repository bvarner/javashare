/* Change-Log:
	12.17.2002 - Class Created
	1.14.2003 - Re-worked countRunning to ignore Failed and Completed transfers.
*/
package org.beShare.network;

import com.meyer.muscle.thread.ThreadPool;
import java.util.ArrayList;

public class TransferList extends ArrayList<AbstractTransfer> {
	int maxActive = 0;

	/**
	 * Creates a new queue for transfers.
	 *
	 * @param maxConcurrent The Maximum number of tranfers that can simultaneously run.
	 */
	public TransferList(int maxConcurrent) {
		super();
		maxActive = maxConcurrent;
	}

	/**
	 * @return the number of transfers that can run simultaneously.
	 */
	public int getMax() {
		return maxActive;
	}

	/**
	 * Sets the number of simultaneous transfers.
	 *
	 * @param maxConcurrent The Maximum number of transfers that can simultaneously run.
	 */
	public void setMax(int maxConcurrent) {
		this.maxActive = maxConcurrent;
	}

	/**
	 * @return if the Queue should start a transfer.
	 */
	private boolean shouldStart() {
		return size() > getActive() && getActive() < getMax();
	}

	/**
	 * @return The number of currently active transfers.
	 */
	private int getActive() {
		int running = 0;
		for (int x = 0; x < size(); x++) {
			if (get(x).isActive()) {
				running++;
			}
		}

		return running;
	}

	/**
	 * Forces the Queue to check itself. Running any unstarted transfers up to <code>getMax</code>.
	 */
	void startNextPending() {
		AbstractTransfer trans = getNextPending();
		if (trans != null) {
			ThreadPool.getDefaultThreadPool().startThread(trans);
		}
	}

	/**
	 * @return the next Transfer that has yet to start.
	 */
	public AbstractTransfer getNextPending() {
		AbstractTransfer next = null;

		for (int x = 0; x < size() && next == null; x++) {
			if (!get(x).hasRun()) {
				next = get(x);
			}
		}

		return next;
	}

	/**
	 * Adds a Transfer to this queue, the queue automatically checks to see if it should start the transfer.
	 */
	@Override
	public boolean add(AbstractTransfer trans) {
		boolean ret = super.add(trans);
		startNextPending();
		return ret;
	}

	@Override
	public boolean remove(Object o) {
		AbstractTransfer transfer = (AbstractTransfer)o;
		boolean ret = super.remove(transfer);
		if (ret) {
			if (transfer.isActive()) {
				transfer.abort();
			}
		}
		startNextPending();
		return ret;
	}
}
