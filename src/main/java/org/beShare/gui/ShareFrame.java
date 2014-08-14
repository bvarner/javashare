/* Change Log:
	2-28.2002 - 1.0 - Initial Version
	12.19.2002 - 1.3 - Added private sizing and centering functions.
						These ensure that the full window is visible on-screen when the program starts.
						Even if the previously saved bounds were out of the current resolution.
*/
package org.beShare.gui;

import com.meyer.muscle.message.Message;
import com.meyer.muscle.message.MessageException;
import com.meyer.muscle.support.Rect;
import com.meyer.muscle.support.UnflattenFormatException;
import org.beShare.data.BeShareDefaultSettings;
import org.beShare.data.MusclePreferenceReader;
import org.beShare.network.JavaShareTransceiver;
import org.beShare.network.ServerAutoUpdate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * MainFrame - Creates the main frame (window) for the Application mode of
 * JavaShare2.
 * <p/>
 * Last Update: 12.19.2002
 *
 * @author Bryan Varner
 * @version 1.3
 */
public class ShareFrame extends JFrame implements WindowListener {
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

		// TODO: Remove this.
		// Set the frames title to have the starting (default) server.
		mainPanel.updateFrameTitle();

		addWindowListener(this);

		pack();

		// Now start the auto-update thread. Muhuhahaha.
		ServerAutoUpdate autoUpdate = new ServerAutoUpdate(mainPanel);
		autoUpdate.run();
	}

	/**
	 * Whenever the window is re-activated, we send a signal for the chat line
	 * to request focus! - There is a long chain of methods to get to it but it works.
	 */
	public void windowActivated(WindowEvent e) {
		mainPanel.requestChatLineFocus();
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		mainPanel.quitRequested(); // Hehe... BeOS programmers - does look a bit familiar?
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}
}
