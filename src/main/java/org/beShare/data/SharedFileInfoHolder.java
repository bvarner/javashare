/*
	SharedFileInfoHolder - a class to hold information for a shared file.
*/
package org.beShare.data;

import java.io.File;
/**
 * Holds data and provides convience methods for maintaining data on
 * the files that are shared.
 *
 * @author Bryan Varner
 * @version 1.0 - 8.2.2002
 */
public class SharedFileInfoHolder {
	String	fileName;
	long	sizeInBytes;
	String	fileKind;
	String	fullPath;
	String	sessionID;
	
	/**
	 * Default constructor, initializes stuff.
	 */
	public SharedFileInfoHolder(){
		fileName = "";
		sizeInBytes = 0;
		fullPath = "";
		fileKind = "";
		sessionID = "";
	}
	
	/**
	 * Constructor, takes a file and pre-fills everything except the mime-type.
	 */
	public SharedFileInfoHolder(File infoTarget){
		fileName = infoTarget.getName();
		sizeInBytes = infoTarget.length();
		fullPath = infoTarget.getParent();
		fileKind = "";
		sessionID = "";
	}
	
	/**
	 * Sets the file name.
	 */
	public void setName(String name){
		fileName = name;
	}
	
	/**
	 * Retreives the file name
	 */
	public String getName(){
		return fileName;
	}
	
	/**
	 * Sets the file size.
	 */
	public void setSize(long size){
		sizeInBytes = size;
	}
	
	/**
	 * Finds the file size
	 */
	public long getSize(){
		return sizeInBytes;
	}
	
	/**
	 * Sets the file kind.
	 */
	public void setKind(String kind){
		fileKind = kind;
	}
	
	/**
	 * Gets the file kind (mime-type).
	 */
	public String getKind(){
		return fileKind;
	}
	
	/**
	 * Sets the files local path.
	 */
	public void setPath(String path){
		fullPath = path;
	}
	
	/**
	 * Retreives the files local path.
	 */
	public String getPath(){
		return fullPath;
	}
	
	public void setSessionID(String session){
		this.sessionID = session;
	}
	
	public String getSessionID(){
		return sessionID;
	}
	
	/**
	 * Returns true if sfh is data-wise identical to this object.
	 */
	public boolean equals(SharedFileInfoHolder sfh){
		return (fileKind.equals(sfh.getKind()) &&
				fileName.equals(sfh.getName()) &&
				fullPath.equals(sfh.getPath()) &&
				sizeInBytes == sfh.getSize() &&
				sessionID.equals(sfh.getSessionID()));
	}
	
	/**
	 * Returns a string representation of the file.
	 */
	public String toString(){
		return "[" + fileName + "] - " + fileKind + " - " + fullPath + " - Size: " + sizeInBytes + " - SessionID: " + sessionID;
	}
}
