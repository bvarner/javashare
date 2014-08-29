/*
	SharedFileInfoHolder - a class to hold information for a shared file.
*/
package org.beShare.data;

import java.io.File;

/**
 * Defines the necessary information for shared Files.
 *
 * @author Bryan Varner
 */
public class SharedFile {
	String fileName;
	long sizeInBytes;
	String fileKind;
	String fullPath;
	String sessionID;

	/**
	 * Default constructor, initializes stuff.
	 */
	public SharedFile() {
		fileName = "";
		sizeInBytes = 0;
		fullPath = "";
		fileKind = "";
		sessionID = "";
	}

	/**
	 * Constructor, takes a file and pre-fills everything except the mime-type.
	 */
	public SharedFile(File infoTarget) {
		fileName = infoTarget.getName();
		sizeInBytes = infoTarget.length();
		fullPath = infoTarget.getParent();
		fileKind = "";
		sessionID = "";
	}

	/**
	 * Retreives the file name
	 */
	public String getName() {
		return fileName;
	}

	/**
	 * Sets the file name.
	 */
	public void setName(String name) {
		fileName = name;
	}

	/**
	 * Finds the file size
	 */
	public long getSize() {
		return sizeInBytes;
	}

	/**
	 * Sets the file size.
	 */
	public void setSize(long size) {
		sizeInBytes = size;
	}

	/**
	 * Gets the file kind (mime-type).
	 */
	public String getKind() {
		return fileKind;
	}

	/**
	 * Sets the file kind.
	 */
	public void setKind(String kind) {
		fileKind = kind;
	}

	/**
	 * Retreives the files local path.
	 */
	public String getPath() {
		return fullPath;
	}

	/**
	 * Sets the files local path.
	 */
	public void setPath(String path) {
		fullPath = path;
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String session) {
		this.sessionID = session;
	}

	/**
	 * Returns true if sfh is data-wise identical to this object.
	 */
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		} else if (other instanceof SharedFile) {
			SharedFile sfh = (SharedFile) other;
			return (fileKind.equals(sfh.getKind()) &&
					        fileName.equals(sfh.getName()) &&
					        fullPath.equals(sfh.getPath()) &&
					        sizeInBytes == sfh.getSize() &&
					        sessionID.equals(sfh.getSessionID()));
		}
		return false;
	}

	/**
	 * Returns a string representation of the file.
	 */
	public String toString() {
		return "[" + fileName + "] - " + fileKind + " - " + fullPath + " - Size: " + sizeInBytes + " - SessionID: " + sessionID;
	}
}
