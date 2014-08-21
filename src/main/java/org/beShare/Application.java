package org.beShare;

import org.beShare.gui.ShareFrame;
import org.beShare.network.JavaShareTransceiver;

import javax.swing.*;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

/**
 * BeShare for Java application class. This constructs the stand-alone version
 * of the BeShare for Java client.
 *
 * @author Bryan Varner
 * @created March 8, 2002
 */
public class Application {
	public static final String BUILD_VERSION = "3.0-SNAPSHOT";

	/**
	 * Application Main - Creates a new instance of mainFrame Application.
	 *
	 * @param args Description of Parameter
	 */
	public static void main(String args[]) {
		final JavaShareTransceiver transceiver = new JavaShareTransceiver(Preferences.userNodeForPackage(Application.class));

// Add support for the beoslaf.jar
//        try {
//            ClassLoader.getSystemClassLoader().loadClass("com.sun.java.swing.plaf.beos.BeOSLookAndFeel");
//            UIManager.installLookAndFeel(new UIManager.LookAndFeelInfo("BeOS R5", "com.sun.java.swing.plaf.beos.BeOSLookAndFeel"));
//        } catch (Exception e) {
//        }

// First we need to check our L&F setting!
//		if (programPrefsMessage.hasField("LaF")) {
//			try {
//				UIManager.setLookAndFeel(programPrefsMessage.getString("LaF", UIManager.getCrossPlatformLookAndFeelClassName()));
//			} catch (Exception ex) {
//				System.err.println("Failed to set LookandFeel: " + ex.getMessage());
//			}
//		}

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ShareFrame shareFrame = new ShareFrame(transceiver);
				shareFrame.pack();
				shareFrame.setVisible(true);
			}
		});
	}
}

