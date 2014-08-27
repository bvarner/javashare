/* Change-Log:
	12.17.2002 - Class Created
	1.14.2003 - Re-worked countRunning to ignore Failed and Completed transfers.
*/
package org.beShare.network;

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
		if (size() > countRunning()) {
			if (countRunning() < getMax()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @return The number of currently running transfers.
	 */
	public int countRunning() {
		int running = 0;
		for (int x = 0; x < size(); x++) {
			AbstractTransfer tran = get(x);
			if (tran.isStarted()) {
				if ((tran.getStatus() == AbstractTransfer.FINISHED) || (tran.getStatus() == AbstractTransfer.ERROR)) {
					// Do nothing, it's not running.
				} else {
					running++;
				}
			}
		}

		return running;
	}

	/**
	 * Forces the Queue to check itself. Running any unstarted transfers up to <code>getMax</code>.
	 */
	public void checkQueue() {
		AbstractTransfer trans = null;
		while (shouldStart()) {
			trans = getNextUnstarted();
			if (trans != null) {
				trans.run();
			} else {
				break;
			}
		}
	}

	/**
	 * @return the next Transfer that has yet to start.
	 */
	public AbstractTransfer getNextUnstarted() {
		AbstractTransfer next = null;

		int x = 0;
		while (x < size() && next == null) {
			if (!get(x).isStarted()) {
				next = get(x);
				return next;
			}
			x++;
		}

		return null;
	}

	/**
	 * Adds a Transfer to this queue, the queue automatically checks to see if it should start the transfer.
	 */
	@Override
	public boolean add(AbstractTransfer trans) {
		trans.setStatus(AbstractTransfer.LOCALLY_QUEUED);
		boolean ret = super.add(trans);
		checkQueue();
		return ret;
	}

	@Override
	public boolean remove(Object o) {
		boolean ret = super.remove(o);
		((AbstractTransfer) o).abort();
		checkQueue();
		return ret;
	}
}
