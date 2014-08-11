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
	MainFrame - Creates the main frame (window) for the Application mode of
	JavaShare2.
	
	Last Update: 12.19.2002
	
	@author Bryan Varner
	@version 1.3
*/
public class ShareFrame extends JFrame implements WindowListener{
	JavaShareTransceiver networkIO = null;
	AppPanel	mainPanel;
	Object		menuBar;
	Message		prefsMessage;
	/**
		Default constructor Creates a new mainPanel, and JavaShareTransceiver.
		It then connects the two together.
	*/
	public ShareFrame(){
		super(AppPanel.pubVersion);
		
		ImageIcon JavaShareIcon = AppPanel.loadImage("Images/BeShare.gif", this);
		this.setIconImage(JavaShareIcon.getImage());
		
		// Check to see if a prefrence file exists...
		String prefsFile = System.getProperty("user.home") 
							+ System.getProperty("file.separator")
							+ ".JavaShare2Prefs.dat";
        networkIO = new JavaShareTransceiver();

		mainPanel = null;
		FileInputStream fileStream = null;
		DataInputStream prefsInStream = null;
		try{
			fileStream = new FileInputStream(prefsFile);
			prefsInStream = new DataInputStream(fileStream);
		} catch (FileNotFoundException fnfe){
			// First time program has been run? Or prefs deleted.
		} catch (SecurityException se){
			// You don't have permission to read this file!!!
		} catch (IOException ioe){
			// DataInputStream could not be constructed around file!!!
		}
		
		if (prefsInStream != null){
			// We have a properly constructed IO Stream, Now to unflatten it!
			prefsMessage = new Message();
			try {
				prefsMessage.unflatten(prefsInStream, -1);
			} catch (UnflattenFormatException ufe){
				// The format was not correct!!! We can ignore this, and we'll
				// just use the defaults. No sweat off my back!
				prefsMessage = new Message();
			} catch (IOException ioe){
				// Some IO Exception occured during the Unflattening process.
				// We'll just re-set it to a default new Message, and carry on.
				prefsMessage = new Message();
			}
			mainPanel = new AppPanel(networkIO, prefsMessage);
		} else {
			prefsMessage = BeShareDefaultSettings.createDefaultSettings();
		}

		// If the attempt above to create an AppPanel from a Message fails, or
		// if something barfed before that, mainPanel will be Null, In this case
		// We'll just create the default Window, and hope it saves properly in
		// the future.
		if (mainPanel == null){
			mainPanel = new AppPanel(networkIO, BeShareDefaultSettings.createDefaultSettings());
		}

        // If it's not Mac OS we use a Swing Menu bar and attach it to the frame.
        menuBar = new SwingMenuBar(mainPanel, false);
        this.setJMenuBar((SwingMenuBar)menuBar);
        mainPanel.setListenToMenu((SwingMenuBar)menuBar);
		this.getContentPane().add(mainPanel);
		// Set the frames title to have the starting (default) server.
		mainPanel.updateFrameTitle();
		
		addWindowListener(this);
		
		pack();
		
		// Window Size Constraining.
		if (prefsMessage.hasField("mainWindowRect")){
			try {
				Rect windowBounds = prefsMessage.getRect("mainWindowRect");
				setBounds(windowBounds.getRectangle());
				if (MusclePreferenceReader.getBoolean(prefsMessage, "ensureWindowFits", true))
					sizeWindow(getBounds());
			} catch (MessageException me){
			}
		} else {
			center();
		}
		
		// Auto-Server List update
        if (prefsMessage.getBoolean("autoUpdServers", true)) {
            // Now start the auto-update thread. Muhuhahaha.
            ServerAutoUpdate autoUpdate = new ServerAutoUpdate(mainPanel);
            autoUpdate.run();
        }
	}
	
	/**
	 * Makes sure the window is visible within the current viewing device's bounds.
	 */
	private void sizeWindow(Rectangle windowBounds) {
		try {
			Rectangle device = screenBounds();
			// If the window is outside the bounds of the device...
			if (! device.contains(windowBounds)) {
				// move x toward 0 if necessary.
				if (windowBounds.x + windowBounds.width > device.width) {
					if(windowBounds.x > 0) {
						while (windowBounds.x > 0) {
							setLocation(new java.awt.Point(windowBounds.x / 2, windowBounds.y));
							windowBounds = getBounds();
						}
					}
				} else if (windowBounds.x < 0) {
					while (windowBounds.x < 0) {
						setLocation(new java.awt.Point(windowBounds.x + 1, windowBounds.y));
						windowBounds = getBounds();
					}
				}
				
				// Move y toward 0 if necessary
				if (windowBounds.y + windowBounds.height > device.height) {
					if (windowBounds.y > 0) {
						while (windowBounds.y > 0) {
							setLocation(new java.awt.Point(windowBounds.x, windowBounds.y / 2));
							windowBounds = getBounds();
						}
					}
				} else if (windowBounds.y < 0) {
					while (windowBounds.y < 0) {
						setLocation(new java.awt.Point(windowBounds.x, windowBounds.y + 1));
						windowBounds = getBounds();
					}
				}
				
				// Check if we're all visible now.
				if (!device.contains(windowBounds)) {
					// Shrink or enlarge to fit!
					windowBounds.setSize(device.width - 10, device.height - 40);
					setBounds(windowBounds);
				}
				
				// Center it one last time!
				center();
			}
		} catch (NoSuchMethodError nsme) {
			// Cactch errors on MacOS Classic.
		}
	}
	
	/**
	 * Centers the window in the current screen.
	 */
	private void center() {
		// Center me if I'm on a new enough JRE!
		Rectangle screenRect = screenBounds();
		this.setBounds((screenRect.width / 2) - (this.getBounds().width / 2),
				(screenRect.height / 2) - (this.getBounds().height / 2),
				this.getBounds().width, this.getBounds().height);
	}
	
	/**
	 * Returns the Current Screen Bounds.
	 */
	private Rectangle screenBounds() {
		try{
			GraphicsEnvironment systemGE
				= GraphicsEnvironment.getLocalGraphicsEnvironment();
			return systemGE.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
		} catch (NoClassDefFoundError ncdfe){
			Toolkit tk = Toolkit.getDefaultToolkit();
			return new Rectangle(tk.getScreenSize());
		}
	}
	
	/**
	 * Whenever the window is re-activated, we send a signal for the chat line
	 * to request focus! - There is a long chain of methods to get to it but it works.
	*/
	public void windowActivated(WindowEvent e){
		mainPanel.requestChatLineFocus();
	}
	
	public void windowClosed(WindowEvent e){}
	public void windowClosing(WindowEvent e){
		mainPanel.quitRequested(); // Hehe... BeOS programmers - does look a bit familiar?
	}
	public void windowDeactivated(WindowEvent e){}
	public void windowDeiconified(WindowEvent e){}
	public void windowIconified(WindowEvent e){}
	public void windowOpened(WindowEvent e){}
	
	public void show() {
		super.show();
	}
}
