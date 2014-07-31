/* Change-Log:
	12.17.2002 - Class Created
	1.14.2003 - Re-worked countRunning to ignore Failed and Completed transfers.
*/
package org.beShare.network;

import java.util.Vector;

/**
 * TransferQueue - An extension of Vector that implements type-safe add and remove.
 *			It will also Start any unstarted transfers up to <code>getMax()</code> any time
 * 			a Transfer is added or removed from the queue.
 * @author Bryan Varner
 * @version 2.0 1.14.2003
 */
public class TransferQueue extends Vector {
	int max = 0;
	
	/**
	 * Creates a new Queue for transfers. The max transfers defaults to 0
	 */
	public TransferQueue(){
		super();
	}
	
	/**
	 * Creates a new queue for transfers.
	 * @param maxConcurrent The Maximum number of tranfers that can simultaneously run.
	 */
	public TransferQueue(int maxConcurrent) {
		this();
		max = maxConcurrent;
	}
	
	/**
	 * @return the number of transfers that can run simultaneously.
	 */
	public int getMax() {
		return max;
	}
	
	/**
	 * Sets the number of simultaneous transfers.
	 * @param maxConcurrent The Maximum number of transfers that can simultaneously run.
	 */
	public void setMax(int maxConcurrent){
		max = maxConcurrent;
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
			Transfer tran = (Transfer)elementAt(x);
			if (tran.isStarted()) {
				if ((tran.getStatus() == Transfer.FINISHED) || (tran.getStatus() == Transfer.ERROR)) {
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
		Transfer trans = null;
		while (shouldStart()) {
			trans = getNextUnstarted();
			if (trans != null)
				trans.run();
			else
				break;
		}
	}
	
	/**
	 * @return the next Transfer that has yet to start.
	 */
	public Transfer getNextUnstarted(){
		Transfer next = null;
		
		int x = 0;
		while (x < size() && next == null) {
			if (!((Transfer)elementAt(x)).isStarted()) {
				next = (Transfer)elementAt(x);
				return next;
			}
			x++;
		}
		
		return null;
	}
	
	/**
	 * Adds a Transfer to this queue, the queue automatically checks to see if it should start the transfer.
	 */
	public void addTransfer(Transfer trans){
		trans.setName("Transfer" + size() + 1);
		trans.setStatus(Transfer.LOCALLY_QUEUED);
		addElement(trans);
		checkQueue();
	}
	
	/**
	 * Removes the transfer from the queue. The queue will check if it should start another transfer.
	 */
	public void removeTransfer(Transfer trans){
		removeElement(trans);
		trans.abort();
		
		checkQueue();
	}
}
