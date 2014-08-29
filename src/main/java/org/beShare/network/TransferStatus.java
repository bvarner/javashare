package org.beShare.network;

import java.util.Arrays;
import java.util.List;

/**
 * Enumerations which represent the states of a Transfer.
 */
public enum TransferStatus {
	LOCALLY_QUEUED("Locally Queued"),
	CONNECTING("Connecting"),
	AWAITING_CALLBACK("Awaiting Callback"),
	ACTIVE("Active"),
	REMOTELY_QUEUED("Remotely Queued"),
	EXAMINING("Examining Files"),
	COMPLETE("Complete"),
	ERROR("Error");

	static final List<TransferStatus> INACTIVE_STATES =
			Arrays.asList(new TransferStatus[]{LOCALLY_QUEUED, COMPLETE, ERROR});

	private String text;

	TransferStatus(final String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
}
