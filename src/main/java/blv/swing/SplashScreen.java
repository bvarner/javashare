package blv.swing;

import javax.swing.*;
import java.*;
import java.awt.*;

/**
 * SplashScreen - A Swing-based component for displaying Images as a splash
 * 					screen during program startup.
 * 
 * @author Bryan Varner
 * @version 1.1
 *
 * Change Log:
 *		1.1 - 6.2.2002 - Updated the constructor to display the window in the GUI thread.
 *			This seems to speed up how soon it is displayed.
 */

public class SplashScreen extends JWindow{
	JLabel 			lblImage;
	ImageIcon		imgSource;
	
	/**
	 * Default Constructor - Creates a new SplashScreen with a black, 1 pt
	 * 						border. The SplashScreen automatically adjusts to
	 * 						the size of the image and screen resoulution, and 
	 *						centers itself.
	 *
	 * @param splashFile The path to the image file to display.
	 */
	public SplashScreen(String splashFile) throws NoClassDefFoundError{
		super();
		imgSource = new ImageIcon(ClassLoader.getSystemResource(splashFile));
		lblImage = new JLabel(imgSource);
		this.getContentPane().setLayout(new GridLayout(1,1));
		this.getContentPane().add(lblImage);
		((JPanel)getContentPane()).setBorder(
				BorderFactory.createLineBorder(new	Color(0, 0, 0), 1));
		
		// Size it.
		pack();
		
		// Center it.
		GraphicsEnvironment systemGE
				= GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle screenRect = systemGE.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
		this.setBounds((screenRect.width / 2) - (this.getBounds().width / 2),
						(screenRect.height / 2) - (this.getBounds().height / 2),
						this.getBounds().width, this.getBounds().height);
		
		// Show it using the GUI thread.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				show();
			}
		});
	}
	
	/**
	 * Static Initializer - Returns a SplashScreen constructed to display
	 * 						<code>imageFile</code>.
	 *
	 * @param imageFile The path to the image file to display.
	 */
	public static SplashScreen createSplashScreen(String imageFile){
		SplashScreen theScreen = new SplashScreen(imageFile);
		return theScreen;
	}
}
