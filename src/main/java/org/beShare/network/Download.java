package org.beShare.network;

import com.meyer.muscle.message.FieldTypeMismatchException;
import com.meyer.muscle.message.Message;
import com.meyer.muscle.message.MessageException;
import org.beShare.data.MusclePreferenceReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A Download Transfer - Read Jeremy Friesner's BeShare File Transfer Descriptions to really understand this.
 * This class implements the Download functionality of a transfer. It fully complies with _all_
 * current (2.1.8) BeShare options.
 *
 * @author Bryan Varner
 */
public class Download extends AbstractTransfer {
	// Local stuff we need for various things.
	String[] files;
	String hostIP;
	int port;
	String remoteUserName;
	String remoteSessionID;
	boolean firewalledTransfer;
	boolean firewallSocketBound;
	int firewallListenPort;
	JavaShareTransceiver network;

	// Local file information.
	String localFileBasePath;

	// Current file - this tracks our progress.
	RandomAccessFile fileSlave;

	/**
	 * Creates a new download thread to do some work.
	 */
	public Download(String[] files, String hostIP, int port, String localFileBasePath, String remoteUserName, String remoteSessionID, boolean firewalled, JavaShareTransceiver network) {
		super();
		this.files = files;
		this.hostIP = hostIP;
		this.port = port;
		this.remoteUserName = remoteUserName;
		this.remoteSessionID = remoteSessionID;
		this.localFileBasePath = localFileBasePath;
		this.network = network;
		this.firewalledTransfer = firewalled;
		firewallListenPort = JavaShareTransceiver.startingPortNumber;
		fileSlave = null;
	}

	/**
	 * Connects to MUSCLE server.
	 */
	public void connect() {
		beShareTransceiver.disconnect();
		setStatus(CONNECTING);
		if (!firewalledTransfer) {
			beShareTransceiver.connect(hostIP, port, serverConnect, serverDisconnect);
		} else {
			setStatus(AWAITING_CALLBACK);
			// Bind to the port, then listen.
			ServerSocketChannel socketChannel = null;
			do {
				try {
					socketChannel = ServerSocketChannel.open();
					socketChannel.socket().bind(new InetSocketAddress(firewallListenPort));
				} catch (IOException ioe) {
					socketChannel = null;
					firewallListenPort++;
				}
			} while (socketChannel == null);
			beShareTransceiver.listen(socketChannel, serverConnect, serverDisconnect, false);
			network.sendConnectBackRequestMessage(remoteSessionID, firewallListenPort);
		}
	}

	/**
	 * Disconnects from MUSCLE server
	 */
	public void disconnect() {
		connected = false;
		beShareTransceiver.disconnect();
		setStatus(FINISHED);
		started = false;
	}

	/**
	 * Messages comming from our tranceiver are sent here.
	 */
	public synchronized void messageReceived(Object message, int numleft) {
		if (message == serverConnect) {
			setStatus(CONNECTING);
			connected = true;
			Message gimmeList = new Message(TRANSFER_COMMAND_FILE_LIST);
			gimmeList.setString("beshare:FromSession", network.getLocalSessionID());
			// Add the data munging instruction for BeShare clients that support this.
			gimmeList.setInt("mm", MUNGE_MODE_XOR);

			//CHECK FOR existing files, and MD5's.
			try {
				setStatus(EXAMINING);
				MessageDigest md = MessageDigest.getInstance("MD5");
				File testFile = null;
				long offsets[] = new long[files.length];
				byte digests[][] = new byte[files.length][md.getDigestLength()];
				for (int x = 0; x < files.length; x++) {
					testFile = null;
					testFile = new File(localFileBasePath, files[x]);
					offsets[x] = testFile.length(); // Returns 0 if the file dosen't exist.
					if (testFile.exists()) {
						DigestInputStream digestReader = new DigestInputStream(new FileInputStream(testFile), md);

						byte[] input = new byte[128];
						int readbytes = 0;
						while (!(digestReader.read(input, 0, 128) < 128)) {
							;
						}
						digests[x] = digestReader.getMessageDigest().digest();
					}
				}
				gimmeList.setLongs("offsets", offsets);
				gimmeList.setByteBuffers("md5", B_RAW_TYPE, digests);

				setStatus(CONNECTING);
				System.out.println("Files To Download:");
				for (int x = 0; x < files.length; x++) {
					System.out.println(files[x] + " start offset: " + offsets[x] + " hash: " + digests[x]);
				}

			} catch (NoSuchAlgorithmException nsae) {
				System.err.println("MD5 Algorithm not supported!");
			} catch (FileNotFoundException fnfe) {
				System.err.println("File " + fnfe.getMessage() + " wasn't found.");
			} catch (FieldTypeMismatchException ftme) {
				System.err.println("Field Type Mismatch adding the Digests to the file list message.");
			} catch (IOException ioe) {
				System.err.println("IOException while hashing file");
			}

			// Add the file list, send the friggen list off.
			gimmeList.setStrings("files", files);
			beShareTransceiver.sendOutgoingMessage(gimmeList);
		} else if (message == serverDisconnect) {
			if (connected) {
				beShareTransceiver.disconnect();
			}
			setStatus(FINISHED);
			// Close our last opened file. We're a dead thread.
			try {
				if (fileSlave != null) {
					fileSlave.close();
				}
			} catch (IOException ioe) {
				// just screw it.
				fileSlave = null;
			}
			connected = false;
		} else if (message instanceof Message) {
			// We got a muscle message (YEAH!) time to handle it.
			try {
				muscleMessageReceived((Message) message);
			} catch (Exception ex) {
			}
		}
		// If we were going to listen for multiple incomming connections on the given port, we'd enable this.
		// But since we're doing a one-shot deal with a download -- forget it. I'll leave it here as a nice
		// discussion piece.
		// ---------------------------------------------------------------------------------------------------
		// else if (message instanceof Socket) {
		//	System.out.println("Got the Socket -- Connection Open!");
		//	beShareTransceiver = new MessageTransceiver(new MessageQueue(this), (Socket)message, serverConnect, serverDisconnect);
		//	System.out.println("New Transceiver created, awaiting serverConnect object.");
		//}
	}


	/**
	 * We got a muscle message, time to see what it's for, and respond.
	 */
	public void muscleMessageReceived(Message message) {
		switch (message.what) {
			case TRANSFER_COMMAND_FILE_HEADER: {
				// Close the file
				try {
					if (fileSlave != null) {
						fileSlave.close();
					}
					currentFile = new File(localFileBasePath, message.getString("beshare:File Name"));
					fileSlave = new RandomAccessFile(currentFile, "rw");

					long seekto = MusclePreferenceReader.getLong(message, "beshare:StartOffset", 0);
					System.out.println("Writing to file at offset: " + seekto);
					fileSlave.seek(seekto);

					// Time to get this party started!
					totalFileSize = message.getLong("beshare:File Size");
					setFileTransfered(seekto);
					// The usual debug info.
				} catch (IOException ioe) {
					abort();
					setStatus(ERROR);
				} catch (MessageException me) {
					abort();
					setStatus(ERROR);
				}
			}
			break;

			case TRANSFER_COMMAND_FILE_DATA: {
				setStatus(ACTIVE);
				try {
					int mungeMode = MusclePreferenceReader.getInt(message, "mm", MUNGE_MODE_OFF);
					byte[][] data = (byte[][]) message.getData("data");

					for (int i = 0; i < data.length; i++) {
						switch (mungeMode) {
							case MUNGE_MODE_XOR:
								fileSlave.write(AbstractTransfer.XORData(data[i]));
								break;
							default: // No Munging
								fileSlave.write(data[i]);
						}
						setFileTransfered(transferedSize + data[i].length);
					}
				} catch (Exception e) {
					abort();
					setStatus(ERROR);
				}
			}
			break;

			case TRANSFER_COMMAND_NOTIFY_QUEUED: {
				setStatus(REMOTE_QUEUED);
			}
			break;

			default:
				System.out.println(message);
		}
	}

	/**
	 * Returns who we're downloading from.
	 */
	public String getUserName() {
		return remoteUserName;
	}

	/**
	 * Aborts the Transfer, closing any open files.
	 *
	 * @overrides Transfer.abort();
	 */
	public void abort() {
		if (fileSlave != null) {
			try {
				fileSlave.close();
			} catch (Exception e) {
			}
		}
		super.abort();
	}
}
