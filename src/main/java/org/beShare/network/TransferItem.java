package org.beShare.network;

import javax.swing.Icon;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * A TransferItem
 */
public class TransferItem {
	private File file;
	private long size;
	private long transferred;
	private byte[] digest;
	private Icon icon;

	private RandomAccessFile rw;

	public TransferItem(final String directory, final String name, final long size, final Icon icon) {
		this.file = new File(directory, name);
		this.size = size;
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
			rw = new RandomAccessFile(file, "rw");
		}

		return rw != null;
	}

	public void closeFile() {
		try {
			if (rw != null) {
				rw.close();
			}
		} catch (IOException ioe) {
			// can't do anything.
		} finally {
			rw = null;
		}
	}

	public void seekTo(long position) throws IOException {
		transferred = position;
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
