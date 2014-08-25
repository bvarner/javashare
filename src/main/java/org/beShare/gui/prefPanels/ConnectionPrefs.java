package org.beShare.gui.prefPanels;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.prefs.Preferences;

/**
 * Connection Preferences panel.
 */
public class ConnectionPrefs extends JPanel {
	public ConnectionPrefs(final Preferences preferences) {
		super(new GridLayout(7, 1, 3, 3));

		final DefaultComboBoxModel<BandwidthSetting> comboBoxModel = new DefaultComboBoxModel<>();

		comboBoxModel.addElement(new BandwidthSetting(14400, "14.4 kbps"));
		comboBoxModel.addElement(new BandwidthSetting(28800, "28.8 kbps"));
		comboBoxModel.addElement(new BandwidthSetting(33600, "33.6 kbps"));
		comboBoxModel.addElement(new BandwidthSetting(57600, "56.6 kbps"));
		comboBoxModel.addElement(new BandwidthSetting(64000, "ISDN-64k"));
		comboBoxModel.addElement(new BandwidthSetting(128000, "ISDN-128k"));
		comboBoxModel.addElement(new BandwidthSetting(384000, "DSL"));
		comboBoxModel.addElement(new BandwidthSetting(768000, "Cable"));
		comboBoxModel.addElement(new BandwidthSetting(1500000, "T1"));
		comboBoxModel.addElement(new BandwidthSetting(4500000, "T3"));
		comboBoxModel.addElement(new BandwidthSetting(4500000, "OC-3"));
		comboBoxModel.addElement(new BandwidthSetting(4500000, "OC-12"));
		comboBoxModel.addElement(new BandwidthSetting(0, "?"));

		JComboBox<BandwidthSetting> cmboBandwidth = new JComboBox<>(comboBoxModel);
		cmboBandwidth.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				DefaultListCellRenderer c =
						(DefaultListCellRenderer) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				c.setText(((BandwidthSetting)value).label);
				return c;
			}
		});
		JLabel conSpeedLbl = new JLabel("Upload Bandwidth: ");
		JPanel conSpeedPanel = new JPanel();
		conSpeedPanel.setLayout(new BoxLayout(conSpeedPanel, BoxLayout.X_AXIS));
		conSpeedPanel.add(conSpeedLbl);
		conSpeedPanel.add(cmboBandwidth);

		cmboBandwidth.setSelectedItem(new BandwidthSetting(preferences.node("bandwidth").getInt("bps", 0), ""));

		JPanel socksSvrPanel = new JPanel(new BorderLayout());
		JPanel socksPrtPanel = new JPanel(new BorderLayout());
		final JTextField txtServer = new JTextField(preferences.get("socksServer", ""));
		final JTextField txtPort = new JTextField(preferences.get("socksPort", ""));
		socksSvrPanel.add(new JLabel("Socks Server: "), BorderLayout.WEST);
		socksSvrPanel.add(txtServer, BorderLayout.CENTER);
		socksPrtPanel.add(new JLabel("Socks Port: "), BorderLayout.WEST);
		socksPrtPanel.add(txtPort, BorderLayout.CENTER);

		add(conSpeedPanel);
		add(socksSvrPanel);
		add(socksPrtPanel);
		add(new JLabel("SOCKS Settings take effect", JLabel.CENTER));
		add(new JLabel("the next time you run JavaShare", JLabel.CENTER));

		setBorder(BorderFactory.createTitledBorder("Connection Preferences"));

		// Register the listener.
		cmboBandwidth.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Preferences bwNode = preferences.node("bandwidth");
				BandwidthSetting bw = (BandwidthSetting)comboBoxModel.getSelectedItem();
				bwNode.put("label", bw.label);
				bwNode.putInt("bps", bw.bps);
			}
		});

		FocusListener socksFocusListener = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
					preferences.put("socksServer", txtServer.getText().trim());
					preferences.put("socksPort", txtPort.getText().trim());
			}
		};

		txtServer.addFocusListener(socksFocusListener);
		txtPort.addFocusListener(socksFocusListener);
	}

	public class BandwidthSetting {
		public String label;
		public Integer bps;

		public BandwidthSetting(int bps, String label) {
			this.bps = bps;
			this.label = label;
		}

		public boolean equals(Object other) {
			if (this == other) {
				return true;
			} else if (other instanceof BandwidthSetting) {
				return ((BandwidthSetting) other).bps.equals(bps);
			} else if (other instanceof Integer) {
				return ((Integer) other).intValue() == bps;
			} else {
				return false;
			}
		}
	}
}

