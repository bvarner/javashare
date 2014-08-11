package org.beShare;

import org.beShare.gui.MainFrame;

/**
 *  BeShare for Java application class. This constructs the stand-alone version
 *  of the BeShare for Java client. 
 *  Class Started: 1.30.2002 
 *  Last Update: 4.25.2002
 *
 * @author     Bryan Varner
 * @created    March 8, 2002
 * @version    2.0
 * 
 * Change Log:
 * 	4.25.2002 - Added the SplashScreen.
 * 	8.3.2002 - Updated to 2.0
 */
public class Application {

	/**
	 *  Application Constructor creates a new MainFrame, show's the window, and
	 *  execution is handed over to the MainFrame's event thread. Yippie!
	 */
	public Application() {
		MainFrame  mainFrame  = new MainFrame();
		mainFrame.show();
	}


	/**
	 *  Application Main - Creates a new instace of mainFrame Application.
	 *
	 * @param  args  Description of Parameter
	 */
	public static void main(String args[]) {
		Application  javaShare  = new Application();
	}
}

