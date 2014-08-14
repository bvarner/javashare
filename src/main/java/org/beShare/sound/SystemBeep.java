//package org.beShare.sound;
//
//import org.beShare.event.SoundEvent;
//import org.beShare.event.SoundEventListener;
//
//import java.awt.*;
//
///**
// * SystemBeep.java - A SoundEventListener that uses the System beep.
// * This makes it possible to receive some sound notification on pre 1.2 JREs.
// *
// * @author Bryan Varner
// * @version 1.0
// */
//public class SystemBeep implements SoundEventListener {
//	Toolkit SystemTk;
//
//	/**
//	 * Default Constructor. Initializes the listener.
//	 */
//	public SystemBeep() {
//		SystemTk = Toolkit.getDefaultToolkit();
//	}
//
//	/**
//	 * Implements the Listener. And calls Toolkit.beep() for every event received.
//	 */
//	public void beShareSoundEventPerformed(SoundEvent bsse) {
//		SystemTk.beep();
//	}
//
//	/**
//	 * This method does nothing. It's here to fully implement the shound listener
//	 */
//	public void setSoundPack(String s) {
//	}
//}
