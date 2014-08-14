/* Change Log:
	1.17.2003 - Created, and working.
*/
package org.beShare.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * QueryProgressIndicator - Fades from Green to transparent smoothly to indicate that a query is in progress.
 *
 * @author Bryan Varner
 * @version 1.0 - 1.17.2003
 */
public class QueryProgressIndicator extends JPanel {
	Color drawColor;
	int alpha;
	boolean fade;

	boolean queryInProgress;
	Timer progressRepainter;

	public QueryProgressIndicator(JButton btn) {
		super.setPreferredSize(new Dimension(btn.getPreferredSize().height, btn.getPreferredSize().height));
		setBorder(BorderFactory.createEtchedBorder());
		queryInProgress = false;
		progressRepainter = new Timer(100,
				                             new ActionListener() {
					                             public void actionPerformed(ActionEvent e) {
						                             repaint();
					                             }
				                             });
	}

	/**
	 * Set weather or not a query is active.
	 */
	public void setQueryInProgress(boolean query) {
		queryInProgress = query;
		if (query) {
			progressRepainter.start();
		} else {
			progressRepainter.stop();
			repaint();
		}
	}

	/**
	 * Overridden paint. This baby is mess.
	 * We use this paint to paint to a child views graphics.
	 * Not generally a good idea, but for our purpose, it's the
	 * path of least resistance.
	 */
	public void paint(Graphics g) {
		super.paint(g);

		if (queryInProgress) {
			Rectangle drawrect = getBounds();
			if (drawColor == null) {
				drawColor = new Color(0, 230, 0);
				alpha = 0;
				fade = true; // We're heading down (transparent)
			} else {
				// Check the min/max fade.
				if (alpha < 1 || alpha > 230) {
					fade = !fade;
				}
				// increment/decrement alpha accordingly.
				if (fade) {
					alpha -= 10;
				} else {
					alpha += 10;
				}
				try {
					drawColor = new Color(drawColor.getRed(), drawColor.getGreen(), drawColor.getBlue(), alpha);
				} catch (NoSuchMethodError nsme) {
					drawColor = new Color(drawColor.getRed(), alpha, drawColor.getBlue());
				}
			}
			// Draw the background.
			g.setColor(UIManager.getColor("control"));
			g.fillRect(1, 1, drawrect.width - 3, drawrect.height - 3);
			// Draw my overlay color.
			g.setColor(drawColor);
			g.fillRect(1, 1, drawrect.width - 3, drawrect.height - 3);
		}
	}
}
