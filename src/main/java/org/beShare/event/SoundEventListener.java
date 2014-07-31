package org.beShare.event;

/**
	SoundEventListener - defines the interface for classes that wish to
	listen for sound events.
	
	Last Update: 3-05-2002
	
	3.5.2002 - Added setSoundPack(String sPack) method.
	
	@author Bryan Varner
	@version 1.1
*/
public interface SoundEventListener{
	public void beShareSoundEventPerformed(SoundEvent bsse);
	public void setSoundPack(String sPack);
}
