package org.beShare.gui.prefPanels;

import javax.swing.*;
import java.awt.*;

public class PreferencePanel extends JPanel {
	public PreferencePanel() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}
	
	public void firstComponent() {
		super.add(Box.createGlue());
	}
	
	public Component add(Component cmp) {
		super.add(Box.createVerticalStrut(3));
		super.add(cmp);
		super.add(Box.createVerticalStrut(3));
		return cmp;
	}
	
	public void lastComponent() {
		super.add(Box.createGlue());
	}
}
