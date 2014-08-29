/* Change-Log
	1.0 - 6.4.2002 - Initial Class creation.
	1.1 - 12.19.2002 - Added Save User Table Sorting.
*/
package org.beShare.gui.prefPanels;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

public class GeneralPrefs extends JPanel {

	public GeneralPrefs(final Preferences preferences) {
		super(new GridLayout(7, 1, 3, 3));

		DefaultComboBoxModel<Integer> comboBoxModel = new DefaultComboBoxModel<>();
		comboBoxModel.addElement(-1);
		comboBoxModel.addElement(2 * 60 * 1000);
		comboBoxModel.addElement(5 * 60 * 1000);
		comboBoxModel.addElement(10 * 60 * 1000);
		comboBoxModel.addElement(15 * 60 * 1000);
		comboBoxModel.addElement(20 * 60 * 1000);
		comboBoxModel.addElement(30 * 60 * 1000);
		comboBoxModel.addElement(1 * 60 * 60 * 1000);
		comboBoxModel.addElement(2 * 60 * 60 * 1000);

		final JComboBox autoAway = new JComboBox(comboBoxModel);
		autoAway.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				DefaultListCellRenderer c =
						(DefaultListCellRenderer) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				switch (value.toString()) {
					case "-1":
						setText("Disabled");
						break;
					case "120000":
						setText("2 Minutes");
						break;
					case "300000":
						setText("5 Minutes");
						break;
					case "600000":
						setText("10 Minutes");
						break;
					case "900000":
						setText("15 Minutes");
						break;
					case "1200000":
						setText("20 Minutes");
						break;
					case "1800000":
						setText("30 Minutes");
						break;
					case "3600000":
						setText("1 Hour");
						break;
					case "7200000":
						setText("2 Hours");
						break;
				}
				return c;
			}
		});
		autoAway.setSelectedItem(preferences.getInt("awayTimeout", -1));
		autoAway.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preferences.putInt("awayTimeout", Integer.parseInt(autoAway.getSelectedItem().toString()));
			}
		});

		JLabel autoAwayLabel = new JLabel("Auto-Away: ");
		JPanel autoAwayPanel = new JPanel();
		autoAwayPanel.setLayout(new BoxLayout(autoAwayPanel, BoxLayout.X_AXIS));
		autoAwayPanel.add(autoAwayLabel);
		autoAwayPanel.add(autoAway);

		final JCheckBox chkAutoUpdateServers =
				new JCheckBox("Auto-Update Server List", preferences.getBoolean("autoUpdateServers", true));
		chkAutoUpdateServers.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preferences.putBoolean("autoUpdateServers", chkAutoUpdateServers.isSelected());
			}
		});

		final JCheckBox chkLogin = new JCheckBox("Login on startup", preferences.getBoolean("autoLogin", false));
		chkLogin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preferences.putBoolean("autoLogin", chkLogin.isSelected());
			}
		});
		final JCheckBox chkFirewall = new JCheckBox("I'm Firewalled", preferences.getBoolean("firewalled", false));
		chkFirewall.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preferences.putBoolean("firewalled", chkFirewall.isSelected());
			}
		});

		add(autoAwayPanel);
		add(chkAutoUpdateServers);
		add(chkLogin);
		add(chkFirewall);

		setBorder(BorderFactory.createTitledBorder("General Preferences"));
	}
}
