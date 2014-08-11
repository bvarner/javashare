package org.beShare;

import org.beShare.gui.ShareFrame;

import javax.swing.*;

/**
 *  BeShare for Java application class. This constructs the stand-alone version
 *  of the BeShare for Java client. 
 *
 * @author     Bryan Varner
 * @created    March 8, 2002
 */
public class Application {
	/**
	 *  Application Main - Creates a new instance of mainFrame Application.
	 *
	 * @param  args  Description of Parameter
	 */
	public static void main(String args[]) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ShareFrame shareFrame = new ShareFrame();
                shareFrame.show();
            }
        });
	}
}

