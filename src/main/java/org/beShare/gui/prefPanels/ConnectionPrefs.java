/* Change-Log
	1.0 - 6.5.2002 - Initial Class creation.
*/
package org.beShare.gui.prefPanels;

import com.meyer.muscle.message.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Connection Preferences panel.
 */
public class ConnectionPrefs extends JPanel implements ActionListener,
		                                                       FocusListener {
	JavaSharePrefListener target;
	Message prefs;

	JTextField txtServer;
	JTextField txtPort;

	JComboBox cmboBandwidth;

	public ConnectionPrefs(JavaSharePrefListener prefHandler, Message prefMessage) {
		super(new GridLayout(7, 1, 3, 3));

		target = prefHandler;
		prefs = prefMessage;

		String[] bwLabels = {"300 Baud", "14.4 kbps", "28.8 kbps", "33.6 kbps",
		                     "57.6 kbps", "ISDN-64k", "ISDN-128k", "DSL",
		                     "Cable", "T1", "T3", "OC-3", "OC-12"};
		cmboBandwidth = new JComboBox(bwLabels);
		JLabel conSpeedLbl = new JLabel("Upload Bandwidth: ");
		JPanel conSpeedPanel = new JPanel();
		conSpeedPanel.setLayout(new BoxLayout(conSpeedPanel, BoxLayout.X_AXIS));
		conSpeedPanel.add(conSpeedLbl);
		conSpeedPanel.add(cmboBandwidth);

		JPanel socksSvrPanel = new JPanel(new BorderLayout());
		JPanel socksPrtPanel = new JPanel(new BorderLayout());
		txtServer = new JTextField();
		txtPort = new JTextField();
		socksSvrPanel.add(new JLabel("Socks Server: "), BorderLayout.WEST);
		socksSvrPanel.add(txtServer, BorderLayout.CENTER);
		socksPrtPanel.add(new JLabel("Socks Port: "), BorderLayout.WEST);
		socksPrtPanel.add(txtPort, BorderLayout.CENTER);

		add(conSpeedPanel);
		add(socksSvrPanel);
		add(socksPrtPanel);
		add(new JLabel("SOCKS Settings take effect", JLabel.CENTER));
		add(new JLabel("next time you run JavaShare2", JLabel.CENTER));

		setBorder(BorderFactory.createTitledBorder("Connection Preferences"));

		// Set the values from the Message.
		if (prefs.hasField("uploadBw")) {
			try {
				cmboBandwidth.setSelectedIndex(prefs.getInt("uploadBw"));
			} catch (Exception e) {
				cmboBandwidth.setSelectedIndex(4);
			}
		}

		if (prefs.hasField("socksServer") && prefs.hasField("socksPort")) {
			try {
				txtServer.setText(prefs.getString("socksServer"));
				if (prefs.getInt("socksPort") != -1) {
					txtPort.setText("" + prefs.getInt("socksPort"));
				}
			} catch (Exception e) {
				txtServer.setText("");
				txtPort.setText("");
			}
		} else {
			txtServer.setText("");
			txtPort.setText("");
		}

		// Register the listener.
		cmboBandwidth.addActionListener(this);
		txtServer.addFocusListener(this);
		txtPort.addFocusListener(this);
	}

	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == cmboBandwidth) {
			int speed = 0;
			switch (cmboBandwidth.getSelectedIndex()) {
				case 0:
					speed = 300;
					break;
				case 1:
					speed = 14400;
					break;
				case 2:
					speed = 28800;
					break;
				case 3:
					speed = 33600;
					break;
				case 4:
					speed = 57600;
					break;
				case 5:
					speed = 64000;
					break;
				case 6:
					speed = 128000;
					break;
				case 7:
					speed = 384000;
					break;
				case 8:
					speed = 768000;
					break;
				case 9:
					speed = 1500000;
					break;
				case 10:
					speed = 4500000;
					break;
				case 11:
					speed = 4500000;
					break;
				case 12:
					speed = 4500000;
					break;
				default:
					speed = 0;
					break;
			}
			String label = (String) cmboBandwidth.getItemAt(
					                                               cmboBandwidth.getSelectedIndex());
			target.bandwidthChange(label, speed, cmboBandwidth.getSelectedIndex());
		}
	}

	public void focusGained(FocusEvent e) {
	}

	public void focusLost(FocusEvent e) {
		try {
			target.socksChange(txtServer.getText(), Integer.parseInt(txtPort.getText()));
		} catch (NumberFormatException nfe) {
			target.socksChange(txtServer.getText(), -1);
		}
	}
}
