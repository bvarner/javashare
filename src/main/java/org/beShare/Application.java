package org.beShare;

import org.beShare.gui.ShareFrame;
import org.beShare.network.JavaShareTransceiver;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * BeShare for Java application class. This constructs the stand-alone version
 * of the BeShare for Java client.
 *
 * @author Bryan Varner
 * @created March 8, 2002
 */
public class Application {
	public static String VERSION = "";

	public static List<Frame> FRAMES = new ArrayList<>();

	/**
	 * Application Main - Creates a new instance of mainFrame Application.
	 *
	 * @param args Description of Parameter
	 */
	public static void main(String args[]) {
		// Read the version string from the package....
		try (BufferedReader br = new BufferedReader(new InputStreamReader(Application.class.getClassLoader().getResourceAsStream("version.txt")))) {
			Application.VERSION = br.readLine();
		} catch (Exception ex) {
			System.err.println("Could not load version information from archive.");
			System.exit(1);
		}

		// Load preferences, generate an installId and save it.
		Preferences prefs = Preferences.userNodeForPackage(Application.class);
		long installId =
				prefs.getLong("installId", (((long) (Math.random() * Integer.MAX_VALUE)) << 32) | ((long) (Math.random() * Integer.MAX_VALUE)));
		prefs.putLong("installId", installId);

		// Create the transceiver
		final JavaShareTransceiver transceiver = new JavaShareTransceiver(prefs);

// Add support for the beoslaf.jar
//        try {
//            ClassLoader.getSystemClassLoader().loadClass("com.sun.java.swing.plaf.beos.BeOSLookAndFeel");
//            UIManager.installLookAndFeel(new UIManager.LookAndFeelInfo("BeOS R5", "com.sun.java.swing.plaf.beos.BeOSLookAndFeel"));
//        } catch (Exception e) {
//        }

// First we need to check our L&F setting!
//		if (programPrefsMessage.hasField("LaF")) {
//		try {
//			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
//		} catch (Exception ex) {
//			System.err.println("Failed to set LookAndFeel: " + ex.getMessage());
//		}
//		}

		// Start the GUI.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ShareFrame shareFrame = new ShareFrame(transceiver);
				shareFrame.pack();
				shareFrame.setVisible(true);
			}
		});
	}
}

