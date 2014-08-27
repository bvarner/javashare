package org.beShare.gui;

import javax.swing.Icon;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;

/**
 * Created by bvarner on 8/27/14.
 */
public class DownTriangleIcon implements Icon {
	private double width;
	private double height;

	private GeneralPath path;

	private Color fill = Color.BLACK;
	private Color stroke = null;

	public DownTriangleIcon(int width, int height) {
		this(width, height, null, null);
	}

	public DownTriangleIcon(int width, int height, final Color fill) {
		this(width, height, fill, null);
	}

	public DownTriangleIcon(int width, int height, final Color fill, final Color stroke) {
		this.width = width;
		this.height = height;

		this.path = new GeneralPath();
		path.moveTo(2,2);
		path.lineTo(width - 1, 2);
		path.lineTo(width / 2.0, height - 1);
		path.lineTo(2,2);
		path.closePath();

		if (fill != null) {
			this.fill = fill;
		}
		if (stroke != null) {
			this.stroke = stroke;
		}
	}

	@Override
	public int getIconHeight() {
		return (int)Math.ceil(height);
	}

	@Override
	public int getIconWidth() {
		return (int)Math.ceil(width);
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		if (fill != null || stroke != null) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			Area area = new Area(path);
			area.transform(AffineTransform.getTranslateInstance(x, y));

			if (fill != null) {
				g2d.setColor(fill);
				setFillSettings(g2d);
				g2d.fill(area);
			}

			if (stroke != null) {
				g2d.setColor(stroke);
				setStrokeSettings(g2d);
				g2d.draw(area);
			}
		}
	}

	protected void setFillSettings(final Graphics2D g) {
		return;
	}

	protected void setStrokeSettings(final Graphics2D g) {
		return;
	}
}
