package org.beShare.data;

import com.meyer.muscle.message.Message;

/**
 * A class of utilities to read values from a MUSCLE message similar to a Properties file.
 * @version 1.0
 * @author Bryan Varner
 */
public class MusclePreferenceReader {
	/** @return an int */
	public static int getInt(Message msg, String pref, int def) {
		if (msg.hasField(pref)) {
			try {
				return msg.getInt(pref);
			} catch (Exception e) {
				return def;
			}
		} else
			return def;
	}
	
	/** @return and array of ints */
	public static int[] getInts(Message msg, String pref, int[] defs) {
		if (msg.hasField(pref)) {
			try {
				return msg.getInts(pref);
			} catch (Exception e) {
				return defs;
			}
		} else
			return defs;
	}
	
	/** @return a boolean */
	public static boolean getBoolean(Message msg, String pref, boolean def) {
		if (msg.hasField(pref)) {
			try {
				return msg.getBoolean(pref);
			} catch (Exception e) {
				return def;
			}
		} else
			return def;
	}
	
	/** @return an int */
	public static long getLong(Message msg, String pref, long def) {
		if (msg.hasField(pref)) {
			try {
				return msg.getLong(pref);
			} catch (Exception e) {
				return def;
			}
		} else
			return def;
	}
	
	/** @return a String */
	public static String getString(Message msg, String pref, String def) {
		if (msg.hasField(pref)) {
			try {
				return msg.getString(pref);
			} catch (Exception e) {
				return def;
			}
		} else
			return def;
	}
	
	/** @return an array of strings */
	public static String[] getStrings(Message msg, String pref, String[] defs) {
		if (msg.hasField(pref)) {
			try {
				return msg.getStrings(pref);
			} catch (Exception e) {
				return defs;
			}
		} else
			return defs;
	}
}
