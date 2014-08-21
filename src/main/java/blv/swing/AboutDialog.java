/**
 * AboutDialog.java - A low-down funky about box!
 *
 * This is a rather slick About Dialog utility class!
 *
 * @author Bryan Varner
 * @version 1.0 - 6.1.2002 - Initial Creation.
 */
package blv.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AboutDialog extends JDialog implements ActionListener {
	JPanel mainPanel;
	CreditPanel creditPanel;
	JLabel lblAppImage;

	JButton btnOk;

	/**
	 * Creates a new AboutDialog with the following parameters:
	 *
	 * @param owner     - The owner frame of this Dialog. It will be centered in owners bounds.
	 * @param title     - The Title of the About Dialog.
	 * @param modal     - Specifies weather or not this should be a blocking dialog.
	 * @param aboutText - A String array containing the text to be scrolled in the credits area.
	 *                  The first element, <code>aboutText[0]</code> is used for the application title.
	 * @param appIcon   - An ImageIcon to be displayed beside the AppName (<code>aboutText[0]</code>)
	 *                  in the dialog.
	 * @param maxLoops  - The maximum times the credits will loop per user click.
	 * @param speed     - speed is 100ths of a second between repaint()s.
	 */
	public AboutDialog(Frame owner, String title, boolean modal,
	                   String[] aboutText, ImageIcon appIcon,
	                   int maxLoops, int speed) {
		super(owner, title, modal);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		mainPanel = new JPanel(new BorderLayout());

		creditPanel = new CreditPanel(aboutText, maxLoops, speed);

		JPanel titleHolder = new JPanel(new BorderLayout());
		lblAppImage = new JLabel(aboutText[0],
				                        appIcon,
				                        SwingConstants.LEADING);

		lblAppImage.setFont(new Font(lblAppImage.getFont().getName(), Font.BOLD, 24));

		titleHolder.add(lblAppImage, BorderLayout.WEST);

		JPanel btnHolder = new JPanel(new BorderLayout());
		btnOk = new JButton("Close");
		btnOk.addActionListener(this);

		btnHolder.add(btnOk, BorderLayout.EAST);

		mainPanel.add(titleHolder, BorderLayout.NORTH);
		mainPanel.add(creditPanel, BorderLayout.CENTER);
		mainPanel.add(btnHolder, BorderLayout.SOUTH);

		getRootPane().setDefaultButton(btnOk);

		setContentPane(mainPanel);
		pack();

		Dimension d = getSize();
		d.height += 100;
		setPreferredSize(d);
		setResizable(false);

		recenter();
	}

	/**
	 * Detects the Button Press, and Disposes of the dialog.
	 */
	public void actionPerformed(ActionEvent e) {
		this.dispose();
	}

	/**
	 * Re-centers the dialog within the bounds of it's owner.
	 */
	public void recenter() {
		Rectangle ownerBounds = getParent().getBounds();
		Rectangle meBounds = this.getBounds();
		this.setBounds(ownerBounds.x + ((ownerBounds.width - meBounds.width) / 2)
				              , ownerBounds.y + ((ownerBounds.height - meBounds.height) / 2)
				              , meBounds.width
				              , meBounds.height);
	}

	/**
	 * Stops the scroll timer if active, and disposes of the window.
	 *
	 * @overrides JDialog.dispose()
	 */
	public void dispose() {
		creditPanel.stopScroll();
		super.dispose();
	}

	/**
	 * Custom class for scrolling text within an area.
	 */
	final class CreditPanel extends JPanel {
		// Scrolling data
		int loops;
		int maxLoops;
		int yPos;
		boolean scrolling;
		String[] text;

		int lineHeight;

		Timer scrollTimer;

		/**
		 * Creates a new CreditPanel that scrolls text.
		 *
		 * @param textToScroll - The text to be scrolled. Element [0] is ignored.
		 * @param maximumLoops - The maximum times to scroll when a user starts the scroll.
		 * @param speed        - speed is 100ths of a second between repaint()s.
		 */
		public CreditPanel(String[] textToScroll, int maximumLoops, int speed) {
			super();

			// scroll data.
			scrolling = false;
			loops = 0;
			maxLoops = maximumLoops;
			yPos = 0;
			text = textToScroll;

			// Size of the text. This is Look and Feel dependant,
			// so we find it the first time we paint.
			lineHeight = (getFontMetrics(getFont())).getHeight();

			// Find the lenght of the longest line of text...
			int longestLine = 0;
			for (int lineCnt = 1; lineCnt < text.length; lineCnt++) {
				int currentLine = getFontMetrics(getFont()).stringWidth(text[lineCnt]);
				if (currentLine > longestLine) {
					longestLine = currentLine;
				}
			}

			setPreferredSize(new Dimension(longestLine + 20, lineHeight * 6));

			// Make the timer.
			scrollTimer = new Timer(speed,
					                       new ActionListener() {
						                       public void actionPerformed(ActionEvent evt) {
							                       yPos--;
							                       repaint();
						                       }
					                       });

			// Add the start/stop scroll by clicking.
			this.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					scrolling = !scrolling;
					if (scrolling) {
						scrollTimer.start();
					} else {
						scrollTimer.stop();
					}
				}
			});
		}

		/**
		 * Our lovely paint method to draw the text to the area.
		 *
		 * @overrides JPanel.paint(Graphics)
		 */
		public void paint(Graphics g) {
			super.paint(g);

			Graphics2D g2d = (Graphics2D)g;
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			// Iterate through the String array, top to bottom, drawing each line. If a line would be clipped
			// by the graphics bounding rect, we don't display it.
			for (int lineCnt = 1; lineCnt < text.length; lineCnt++) {
				int lineY = (lineHeight * (lineCnt)) + yPos;
				if ((lineY < g.getClipBounds().height) || scrolling) {
					g.drawString(text[lineCnt], 10, lineY);
				}

				// If the last line is printed 20 above the clipping region, we set
				// it to start printing at the bottom of the view.
				if ((lineY == -20) && (lineCnt == text.length - 1)) {
					yPos = this.getBounds().height;
					loops++;
				}

				// Check if we should continue to scroll.
				if ((yPos == 0) && (loops == maxLoops)) {
					scrollTimer.stop();
					scrolling = false;
					loops = 0;
				}
			}
		}

		/**
		 * Stops the scroll timer, and scrolling.
		 */
		public void stopScroll() {
			scrollTimer.stop();
			scrolling = false;
		}
	}
}
