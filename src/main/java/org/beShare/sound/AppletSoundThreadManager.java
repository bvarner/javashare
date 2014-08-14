///*
// 	AppletSoundThreadManager.java - Sound for Applets! Whooo hooo!!!
//*/
//package org.beShare.sound;
//
//import java.applet.Applet;
//import java.applet.AudioClip;
//import java.net.MalformedURLException;
//import java.net.URL;
//
///**
// * A SoundThreadManager class for Applets. This is _Identical_ to the normal SoundThreadManager
// * with the exception that it takes an applet as a parameter to the constructor, and it uses that
// * applet to play sounds.
// * <p/>
// * Change Log:
// * 7.14.2002 - Initial Creation and tweaking. Applets now play alert sounds!!! Yeah!!!
// */
//
//public class AppletSoundThreadManager extends ApplicationSoundThreadManager {
//	Applet soundSource;
//
//	public AppletSoundThreadManager(String sPack, Applet src) {
//		super(sPack);
//		soundSource = src;
//	}
//
//	public AppletSoundThreadManager(Applet src) {
//		this("Default", src);
//	}
//
//	/**
//	 * Plays the Sound, constructing the clip from the Applet passed to the constructor.
//	 *
//	 * @overrides ApplicationSoundThreadManager.playSound
//	 */
//	protected Object playSound(String soundToPlay) {
//		// Load it from the cache.
//		AudioClip clip = (AudioClip) soundCache.get(soundToPlay);
//
//		// Construct a new AudioClip and cache it.
//		if (clip == null) {
//			try {
//				clip = soundSource.getAudioClip(new URL(soundSource.getParameter("SoundDirURL") + "/" + soundToPlay));
//				soundCache.put(soundToPlay, clip);
//			} catch (NullPointerException npe) {
//			} catch (NoSuchMethodError nsme) {
//			} catch (MalformedURLException mue) {
//				System.out.println(mue.toString());
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
