/* Change-Log
	1.0 - 6.4.2002 - Initial Class creation.
	1.1 - 12.19.2002 - Added Save User Table Sorting.
*/
package org.beShare.gui.prefPanels;

import com.meyer.muscle.message.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GeneralPrefs extends JPanel implements ActionListener {
	JavaSharePrefListener 	target;
	Message					prefs;
	
	final int oneSecond = 1000;
	final int oneMinute = 60 * oneSecond;
	
	
	JComboBox				autoAway;
	JCheckBox				chkAutoUpdateServers;
	JCheckBox				chkFirewall;
	JCheckBox				chkLogin;
	JCheckBox				chkMiniBrowser;
	JCheckBox				chkSaveUserSort;
	
	public GeneralPrefs(JavaSharePrefListener prefHandler, Message prefMessage){
		super(new GridLayout(7, 1, 3, 3));
		
		target = prefHandler;
		prefs = prefMessage;
		
		String[] autoAwayTimes = {"Disabled", "2 Minutes", "5 Minutes", 
			"10 Minutes", "15 Minutes", "20 Minutes", "30 Minutes", "1 Hour",
			"2 Hours"};
		autoAway = new JComboBox(autoAwayTimes);
		autoAway.setSelectedIndex(2);
		
		JLabel autoAwayLabel = new JLabel("Auto-Away: ");
		
		JPanel autoAwayPanel = new JPanel();
		autoAwayPanel.setLayout(new BoxLayout(autoAwayPanel, BoxLayout.X_AXIS));
		autoAwayPanel.add(autoAwayLabel);
		autoAwayPanel.add(autoAway);
		
		chkAutoUpdateServers = new JCheckBox("Auto-Update Server List", true);
		
		chkFirewall = new JCheckBox("I'm Firewalled", false);
		
		chkLogin = new JCheckBox("Login on startup", true);
		
		chkMiniBrowser = new JCheckBox("Use Mini-Browser for http:// Links", true);
		
		chkSaveUserSort = new JCheckBox("Save User Table Sort Column", false);
		
		add(autoAwayPanel);
		add(chkFirewall);
		add(chkAutoUpdateServers);
		add(chkLogin);
		add(chkMiniBrowser);
		add(chkSaveUserSort);
		
		setBorder(BorderFactory.createTitledBorder("General Preferences"));
		
		// Set the values from the Message.
		if (prefs.hasField("autoUpdServers")){
			try{
				chkAutoUpdateServers.setSelected(prefs.getBoolean("autoUpdServers"));
			} catch (Exception e){
			}
		}
		if (prefs.hasField("awayTimeIndex")){
			try{
				autoAway.setSelectedIndex(prefs.getInt("awayTimeIndex"));
			} catch (Exception e){
			}
		}
		if (prefs.hasField("firewalled")){
			try{
				chkFirewall.setSelected(prefs.getBoolean("firewalled"));
			} catch (Exception e){
			}
		}
		if (prefs.hasField("autoLogin")){
			try{
				chkLogin.setSelected(prefs.getBoolean("autoLogin"));
			} catch (Exception e){
			}
		}
		if (prefs.hasField("miniBrowser")){
			try{
				chkMiniBrowser.setSelected(prefs.getBoolean("miniBrowser"));
			} catch (Exception e){
			}
		}
		if (prefs.hasField("userSort")){
			try {
				chkSaveUserSort.setSelected(prefs.getBoolean("userSort"));
			} catch (Exception e){
			}
		}
		
		// Register the listener.
		autoAway.addActionListener(this);
		chkAutoUpdateServers.addActionListener(this);
		chkFirewall.addActionListener(this);
		chkLogin.addActionListener(this);
		chkMiniBrowser.addActionListener(this);
		chkSaveUserSort.addActionListener(this);
	}
	
	public void actionPerformed(ActionEvent ae){
		if (ae.getSource() == autoAway){
			int time = -1;
			switch (autoAway.getSelectedIndex()){
				case 0: {
					time = -1;
					break;
				}
				case 1: {
					time = 120 * oneSecond;
					break;
				}
				case 2: {
					time = 300 * oneSecond;
					break;
				}
				case 3: {
					time = 10 * oneMinute;
					break;
				}
				case 4: {
					time = 15 * oneMinute;
					break;
				}
				case 5: {
					time = 20 * oneMinute;
					break;
				}
				case 6: {
					time = 30 * oneMinute;
					break;
				}
				case 7: {
					time = 60 * oneMinute;
					break;
				}
				case 8: {
					time = 120 * oneMinute;
					break;
				}
			}
			target.autoAwayTimerChange(time, autoAway.getSelectedIndex());
		} else if (ae.getSource() == chkAutoUpdateServers){
			target.autoUpdateServerChange(chkAutoUpdateServers.isSelected());
		} else if (ae.getSource() == chkFirewall){
			target.firewallSettingChange(chkFirewall.isSelected());
		} else if (ae.getSource() == chkLogin){
			target.loginOnStartupChange(chkLogin.isSelected());
		} else if (ae.getSource() == chkMiniBrowser){
			target.useMiniBrowserChange(chkMiniBrowser.isSelected());
		} else if (ae.getSource() == chkSaveUserSort) {
			target.userSortChange(chkSaveUserSort.isSelected());
		}
	}
}
