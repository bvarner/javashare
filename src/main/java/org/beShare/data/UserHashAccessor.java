package org.beShare.data;
/**
	UserHashAccessor - Defines the functions that are necessary for providing
	basic username/session hashtable services throughout JavaShare2.
	
	Last Update: 2-28-2002
	
	@author Bryan Varner
	@version 1.0
*/

public interface UserHashAccessor {
	public String findSessionByName(String startsWithName);
	public String findNameBySession(String sessionID);
	public String findCompletedName(String partialName);
	public BeShareUser getUserData(String sessionID);
}
