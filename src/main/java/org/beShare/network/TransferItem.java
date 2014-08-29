package org.beShare.network;

import javax.swing.Icon;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * An item involved in a transfer.
 */
public class TransferItem {
	private File file;
	private long size;
	private long transferred;
	private byte[] digest;
	private Icon icon;

	private RandomAccessFile rw;

	public TransferItem(final String directory, final String name, final Icon icon) {
		this.file = new File(directory, name);
		this.size = 0;
		this.transferred = 0;
		this.digest = new byte[0];
		this.rw = null;
		this.icon = icon;
	}

	public File getFile() {
		return file;
	}

	public long getSize() {
		return size;
	}

	public Icon getIcon() {
		return icon;
	}

	public long getTransferred() {
		return transferred;
	}

	public byte[] getDigest() {
		return digest;
	}

	public void setResumeInfo(long offset, byte[] digest) {
		this.transferred = offset;
		this.digest = digest;
	}

	public boolean openFile() throws IOException {
		if (rw == null) {
			try {
				rw = new RandomAccessFile(file, "rw");
			} catch (IOException ioe) {
				ioe.printStackTrace();
				rw = null;
			}
		}

		return rw != null;
	}

	public void closeFile() {
		try {
			if (rw != null) {
				rw.close();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			// can't do anything.
		} finally {
			rw = null;
		}
	}

	public void seekTo(long position, long size) throws IOException {
		this.transferred = position;
		this.size = size;
		if (openFile()) {
			rw.seek(position);
		}
	}

	public void write(byte[] buffer) throws IOException {
		transferred += buffer.length;
		if (openFile()) {
			rw.write(buffer);
		}
	}
}
