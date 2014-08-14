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
 * ShareFrame is a parent window of a connection to a BeShare (MUSCLE) server.
 * It renders the state maintained by the JavaShareTransceiver and provides a GUI.
 *
 * @author Bryan Varner
 */
public class ShareFrame extends JFrame {

	public ShareFrame(JavaShareTransceiver transceiver) {
		super(AppPanel.pubVersion);

		ImageIcon JavaShareIcon = AppPanel.loadImage("Images/BeShare.gif", this);
		this.setIconImage(JavaShareIcon.getImage());
		this.setContentPane(new AppPanel(transceiver, BeShareDefaultSettings.createDefaultSettings()));
		this.setJMenuBar(new SwingMenuBar((AppPanel)this.getContentPane()));

		// TODO: Set to disposeOnClose and make sure all our other threads are Daemon.
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		// TODO: Add listeners here to the transceiver to keep our state in line.

		// TODO: Refactor The majority of AppPanel into this class.
	}



}