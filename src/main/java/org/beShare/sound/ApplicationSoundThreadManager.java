///*
// Change-Log:
// 		7.15.2002 - Renamed from BeShareSoundThreadManager. this BeShare stuff has to go.
//					Other than that, this class remains un-modified from it's original version.
//*/
//package org.beShare.sound;
//
//import org.beShare.event.SoundEvent;
//import org.beShare.event.SoundEventListener;
//import org.beShare.gui.swingAddons.SwingWorker;
//
//import java.applet.Applet;
//import java.applet.AudioClip;
//import java.util.Hashtable;
//
///**
// * <p>ApplicationSound Thread Manager - Manages threads and handles sound events.
// * <p>Each sound plays in it's own thread. So that sounds can multiplex and don't kill
// * performance or responsiveness of the GUI.
// *
// * @author Bryan Varner
// * @version 2.0 - 7.15.2002
// */
//public class ApplicationSoundThreadManager implements SoundEventListener {
//	protected String soundPackName = "";
//	protected Hashtable soundCache;
//
//	public ApplicationSoundThreadManager(String sPack) {
//		soundPackName = sPack + "/";
//		soundCache = new Hashtable();
//	}
//
//	/**
//	 * Default constructor. Will use the "Default" sound pack.
//	 */
//	public ApplicationSoundThreadManager() {
//		this("Default");
//	}
//
//	/**
//	 * Sets the sound pack to load the sounds from.
//	 */
//	public void setSoundPack(String sPack) {
//		soundPackName = sPack + "/";
//	}
//
//	/**
//	 * A sound event occured. Any recognized events will cause a sound to be played.
//	 */
//	public void beShareSoundEventPerformed(SoundEvent bsse) {
//		switch (bsse.getType()) {
//			case SoundEvent.USER_NAME_MENTIONED: {
//				SwingWorker soundPlayer = new SwingWorker() {
//					public Object construct() {
//						return playSound(soundPackName + "UserNameMentioned.au");
//					}
//
//					public void finished() {
//					}
//				};
//				soundPlayer.start();
//			}
//			break;
//			case SoundEvent.PRIVATE_MESSAGE_RECEIVED: {
//				SwingWorker soundPlayer = new SwingWorker() {
//					public Object construct() {
//						return playSound(soundPackName + "PrivateMessage.au");
//					}
//
//					public void finished() {
//					}
//				};
//				soundPlayer.start();
//			}
//			break;
//			case SoundEvent.WATCHED_USER_SPEAKS: {
//				SwingWorker soundPlayer = new SwingWorker() {
//					public Object construct() {
//						return playSound(soundPackName + "WatchedUserSpoke.au");
//					}
//
//					public void finished() {
//					}
//				};
//				soundPlayer.start();
//			}
//			break;
//			case SoundEvent.PRIVATE_MESSAGE_WINDOW: {
//				SwingWorker soundPlayer = new SwingWorker() {
//					public Object construct() {
//						return playSound(soundPackName + "PrivateWindowPopup.au");
//					}
//
//					public void finished() {
//					}
//				};
//				soundPlayer.start();
//			}
//			break;
//		}
//	}
//
//	/**
//	 * Work method to play the sound.
//	 */
//	protected Object playSound(String soundToPlay) {
//		// Load it from the cache.
//		AudioClip clip = (AudioClip) soundCache.get(soundToPlay);
//
//		// Construct a new AudioClip and cache it.
//		if (clip == null) {
//			try {
//				clip = Applet.newAudioClip(ClassLoader.getSystemResource(soundToPlay));
//				soundCache.put(soundToPlay, clip);
//			} catch (NullPointerException npe) {
//			} catch (NoSuchMethodError nsme) {
//			}
//		}
//
//		// If the clip was somehow properly constructed or retrieved from the cache, play it.
//		if (clip != null) {
//			clip.play();
//		}
//
//		return "done";
//	}
//}
