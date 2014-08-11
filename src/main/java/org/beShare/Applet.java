package org.beShare;

import org.beShare.gui.AppPanel;
import org.beShare.gui.SwingMenuBar;
import org.beShare.network.JavaShareTransceiver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *  BeShare for Java Applet class. This constructs the Applet version of
 *  JavaShare 2. Class Started: 2-06-2002 Last Update: 2-06-2002
 *
 * @author     Bryan Varner
 * @created    March 8, 2002
 * @version    1.0
 */
public class Applet extends JApplet implements ActionListener {
	JavaShareTransceiver networkIO;
	AppPanel				mainPanel;
	SwingMenuBar			menuBar;
	
	
	/**
	 *  Applet Constructor. creates the MainPanel, and the network IO and then
	 *  attaches the two for messaging.
	 */
	public void init() {
		// Get the parameters from the applet tag. If something isn't specifed
		// then it uses a default value.
		String  serverName  = getParameter("server");
		if(serverName == null) {
			serverName = "";
		}
		String  userName    = getParameter("defaultUserName");
		if(userName == null) {
			userName = "Binky";
		}
		String  userStatus  = getParameter("defaultUserStatus");
		if(userStatus == null) {
			userStatus = "Here";
		}
        // Initialize the network interface
        networkIO = new JavaShareTransceiver();
		// Create the main Panel
		mainPanel = new AppPanel(networkIO, userName, userStatus, serverName, this);

		// Create the menubar
		menuBar = new SwingMenuBar(mainPanel, true);
		// Hook them all together! and let it rip!
		mainPanel.setListenToMenu(menuBar);

		this.getContentPane().add(mainPanel);
		this.setJMenuBar(menuBar);
	}


	/**
	 *  Description of the Method
	 */
	public void stop() {
		networkIO.disconnect();
	}


	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of Parameter
	 */
	public void actionPerformed(ActionEvent e) {
		JFrame  spawnFrame  = (JFrame)mainPanel.getParent().getParent().getParent().getParent();
		this.getContentPane().setLayout(new GridLayout(1, 1));
		while(this.getContentPane().getComponentCount() > 0) {
			this.getContentPane().remove(0);
		}
		this.getContentPane().add(mainPanel);
		this.setJMenuBar(menuBar);
		validate();
		spawnFrame.dispose();
		mainPanel.setAppletMode(true);
	}
}

