package org.beShare.gui;

import org.w3c.dom.css.Rect;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * QueryProgressIndicator - Fades from Green to transparent smoothly to indicate that a query is in progress.
 *
 * @author Bryan Varner
 * @version 1.0 - 1.17.2003
 */
public class QueryProgressIndicator extends JPanel {
	Color progressColor = new Color(0, 160, 0);
	Color[] steps;
	int currentStep = -1;

	Timer progressRepainter;

	public QueryProgressIndicator() {
		setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
		currentStep = -1;
		progressRepainter = new Timer(100, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				repaint();
			}
		});
		setPreferredSize(new Dimension(32, 32));
		setMinimumSize(new Dimension(16, 16));
		setMaximumSize(new Dimension(128, 128));
		setStepCount(30);
	}

	public final void setStepCount(int stepcount) {
		this.steps = new Color[stepcount];
		setProgressColor(progressColor);
	}

	public final void setProgressColor(final Color color) {
		Color baseline = getBackground();
		this.progressColor = color;

		for (int i = 0; i < steps.length; i++) {
			steps[i] = new Color(baseline.getRed() + (((progressColor.getRed() - baseline.getRed()) / steps.length) * i),
			                     baseline.getGreen() + (((progressColor.getGreen() - baseline.getGreen()) / steps.length) * i),
					             baseline.getBlue() + (((progressColor.getBlue() - baseline.getBlue()) / steps.length) * i),
					             baseline.getAlpha() + (((progressColor.getAlpha() - baseline.getAlpha()) / steps.length) * i));
		}
	}

	@Override
	public boolean isOpaque() {
		return true;
	}

	/**
	 * Set weather or not a query is active.
	 */
	public void setActive(boolean active) {
		if (active) {
			currentStep = 0;
			progressRepainter.start();
		} else {
			currentStep = -1;
			progressRepainter.stop();
			repaint();
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Rectangle drawrect = getBounds();
		drawrect.setLocation(0, 0);
		drawrect.grow(-1, -1);

		if (drawrect.width < drawrect.height) {
			drawrect.height = drawrect.width;
		} else if (drawrect.width > drawrect.height) {
			drawrect.width = drawrect.height;
		}

		Rectangle bounds = getBounds();
		drawrect.x = (bounds.width - drawrect.width) / 2;
		drawrect.y = (bounds.height - drawrect.height) / 2;

		if (currentStep >= 0) {
			for (int i = 0; i < steps.length; i++) {
				g.setColor(steps[i]);
				g2d.fillArc(drawrect.x, drawrect.y, drawrect.width, drawrect.height, i * (360 / steps.length) + ((360 / steps.length) * currentStep), (360 / steps.length));
			}

			if (currentStep + 1 >= steps.length) {
				currentStep = 0;
			} else {
				currentStep++;
			}
		}
		g.setColor(Color.GRAY);
		g2d.drawArc(drawrect.x, drawrect.y, drawrect.width, drawrect.height, 0, 360);
	}
}
