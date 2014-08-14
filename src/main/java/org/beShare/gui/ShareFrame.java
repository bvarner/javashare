/* Change Log:
	2-28.2002 - 1.0 - Initial Version
	12.19.2002 - 1.3 - Added private sizing and centering functions.
						These ensure that the full window is visible on-screen when the program starts.
						Even if the previously saved bounds were out of the current resolution.
*/
package org.beShare.gui;

import org.beShare.data.BeShareDefaultSettings;
import org.beShare.network.JavaShareTransceiver;
import org.beShare.network.ServerAutoUpdate;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * MainFrame - Creates the main frame (window) for the Application mode of
 * JavaShare2.
 *
 * @author Bryan Varner
 */
public class ShareFrame extends JFrame {
	AppPanel mainPanel;

	/**
	 * Default constructor Creates a new mainPanel, and JavaShareTransceiver.
	 * It then connects the two together.
	 */
	public ShareFrame(JavaShareTransceiver transceiver) {
		super(AppPanel.pubVersion);

		ImageIcon JavaShareIcon = AppPanel.loadImage("Images/BeShare.gif", this);
		this.setIconImage(JavaShareIcon.getImage());
		this.mainPanel = new AppPanel(transceiver, BeShareDefaultSettings.createDefaultSettings());
		this.setJMenuBar(new SwingMenuBar(mainPanel));

		this.getContentPane().add(mainPanel);
		// TODO: Set to disposeOnClose and make sure all our other threads are Daemon.
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		pack();

		// TODO: Add a listener so that we update our title any time things change in the transceivers connection info...

		// Now start the auto-update thread. Muhuhahaha.
		ServerAutoUpdate autoUpdate = new ServerAutoUpdate(mainPanel);
		autoUpdate.run();
	}
}
