package org.beShare.network;

import com.meyer.muscle.message.FieldTypeMismatchException;
import com.meyer.muscle.message.Message;
import org.beShare.data.BeShareUser;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import static com.meyer.muscle.support.TypeConstants.B_RAW_TYPE;

/**
 * A Download Transfer - Read Jeremy Friesner's BeShare File Transfer Descriptions if you need further documentation.
 * <p/>
 * This class implements the Download functionality of a transfer. It fully complies with the BeShare(2.1.8) options.
 *
 * @author Bryan Varner
 */
public class Download extends AbstractTransfer {
	// Local stuff we need for various things.
	BeShareUser remoteUser;

	private boolean firewallSocketBound;

	public Download(final JavaShareTransceiver transceiver, final Collection<TransferItem> items, final BeShareUser remoteUser) {
		super(transceiver, items);
		this.remoteUser = remoteUser;
		this.firewallSocketBound = false;
	}

	/**
	 * Connects to MUSCLE server.
	 */
	@Override
	public boolean start() {
		// If the remote user is not firewalled, we can connect directly to them.
		if (!remoteUser.isFirewalled()) {
			transferTransceiver.connect(remoteUser.getIPAddress(), remoteUser.getPort(), serverConnect, serverDisconnect);
			return true;
		} else {
			// We need to bind to a port, and listen for incoming connections (The remote user will connect back to us).
			setStatus(TransferStatus.AWAITING_CALLBACK);

			this.firewallSocketBound = false;
			int firewallListenPort = mainTransceiver.LOCAL_PORT_START;
			try (ServerSocketChannel channel = ServerSocketChannel.open()) {
				do {
					try {
						channel.bind(new InetSocketAddress(firewallListenPort));
						firewallSocketBound = true;
					} catch (IOException e) {
						firewallListenPort++; // Increment the port number.
					}
				}
				while (!firewallSocketBound && firewallListenPort < mainTransceiver.LOCAL_PORT_START + mainTransceiver.LOCAL_PORT_RANGE);

				if (firewallSocketBound) {
					transferTransceiver.listen(channel, serverConnect, serverDisconnect, false);
					mainTransceiver.sendConnectBackRequestMessage(remoteUser, firewallListenPort);
					return true;
				}
			} catch (IOException ioe) {
				// Could not open() a socket channel.
			}

			if (!firewallSocketBound) {
				setStatus(TransferStatus.ERROR);
				mainTransceiver.logError("Unable to bind a local port between " + mainTransceiver.LOCAL_PORT_START + " and " + (mainTransceiver.LOCAL_PORT_RANGE + mainTransceiver.LOCAL_PORT_START) + " for firewalled download");
			}
		}
		return false;
	}

	@Override
	protected void connected() {
		setStatus(TransferStatus.EXAMINING);

		// Setup and examine existing files.
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException nsae) {
			md = new MessageDigest("NILL") {
				@Override
				protected void engineUpdate(byte input) {
					return;
				}

				@Override
				protected void engineUpdate(byte[] input, int offset, int len) {
					return;
				}

				@Override
				protected byte[] engineDigest() {
					return new byte[0];
				}

				@Override
				protected void engineReset() {
					return;
				}
			};
		}

		String[] fileNames = new String[items.size()];
		long[] offsets = new long[items.size()];
		byte[][] digests = new byte[items.size()][md.getDigestLength()];

		// Reset then calculate a proper resume (if there is a resumable file) for each item.
		for (int i = 0; i < items.size(); i++) {
			TransferItem item = items.get(i);
			item.setResumeInfo(0, new byte[0]);
			if (md.getDigestLength() > 0) {
				if (item.getFile().exists()) {
					try (FileInputStream inStream = new FileInputStream(item.getFile())) {
						DigestInputStream digester = new DigestInputStream(inStream, md);
						long bytesRead = 0;
						byte[] buffer = new byte[4096];
						int pass = 0;
						do {
							pass = digester.read(buffer, 0, buffer.length);
							if (pass >= 0) {
								bytesRead += pass;
							}
						} while (!(pass < buffer.length));
						item.setResumeInfo(bytesRead, digester.getMessageDigest().digest());
					} catch (IOException ioe) {
						mainTransceiver.logError("Error inspecting existing download for potential resume: " + item.getFile().getName());
					}
				}
			}

			fileNames[i] = item.getFile().getName();
			offsets[i] = item.getTransferred();
			digests[i] = item.getDigest();
		}

		// Build the outgoing message.
		Message fileRequest = new Message(TRANSFER_COMMAND_FILE_LIST);
		fileRequest.setString("beshare:FromSession", mainTransceiver.localSessionID);
		fileRequest.setInt("mm", MUNGE_MODE_XOR);
		fileRequest.setStrings("files", fileNames);
		try {
			fileRequest.setByteBuffers("md5", B_RAW_TYPE, digests);
			fileRequest.setLongs("offsets", offsets);
		} catch (FieldTypeMismatchException ftme) {
			mainTransceiver.logError("Error attempting to resume downloaded files.");
		}

		transferTransceiver.sendOutgoingMessage(fileRequest);
	}

	@Override
	protected void disconnected() {
		if (connected) {
			if (firewallSocketBound) {
				transferTransceiver.stopListening();
			}
			transferTransceiver.disconnect();
		}
		if (!getStatus().equals(TransferStatus.ERROR)) {
			setStatus(TransferStatus.COMPLETE);
		}
	}

	/**
	 * We got a muscle message, time to see what it's for, and respond.
	 */
	@Override
	public void muscleMessageReceived(Message message) {
		switch (message.what) {
			// Signals that we're going to start getting data for a file.
			case TRANSFER_COMMAND_FILE_HEADER: {
				setStatus(TransferStatus.ACTIVE);
				String filename = message.getString("beshare:File Name", "");
				for (int i = 0; i < items.size(); i++) {
					if (items.get(i).getFile().getName().equals(filename)) {
						setCurrentItem(i);
						break;
					}
				}

				try {
					if (getCurrentItem().openFile()) {
						getCurrentItem().seekTo(message.getLong("beshare:StartOffset", 0));
					}
				} catch (IOException | NullPointerException ex) {
					abort();
					setStatus(TransferStatus.ERROR);
				}
			}
			break;

			case TRANSFER_COMMAND_FILE_DATA: {
				int mungeMode = message.getInt("mm", MUNGE_MODE_OFF);
				byte[][] data = (byte[][]) message.getData("data", new byte[0][0]);
				try {
					for (byte[] line : data) {
						if (mungeMode == MUNGE_MODE_XOR) {
							line = XORData(line);
						}
						getCurrentItem().write(line);
					}
					setStatus(TransferStatus.ACTIVE);
				} catch (IOException ioe) {
					abort();
					setStatus(TransferStatus.ERROR);
				}
			}
			break;

			case TRANSFER_COMMAND_NOTIFY_QUEUED: {
				setStatus(TransferStatus.REMOTELY_QUEUED);
			}
			break;
		}
	}

	/**
	 * Aborts the Transfer, closing any open files.
	 *
	 * @overrides Transfer.abort();
	 */
	public void abort() {
		for (TransferItem item : items) {
			item.closeFile();
		}
		if (firewallSocketBound) {
			transferTransceiver.stopListening();
		}
		transferTransceiver.disconnect();

	}

	@Override
	public String toString() {
		TransferItem item = getCurrentItem();
		if (item != null) {
			return "Downloading " + item.getFile().getName() + " from " + remoteUser.getName();
		} else {
			return "Downloading from " + remoteUser.getName();
		}
	}
}
