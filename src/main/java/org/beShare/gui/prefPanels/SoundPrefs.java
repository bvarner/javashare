/* Change-Log
	1.0 - 6.6.2002 - Initial Class creation.
	1.1 - 6.7.2002 - Reworked the version checking for MacOSClassic compatibility.
					Seems I was using a 1.2 property to see if it was a 1.1 JRE.
*/
package org.beShare.gui.prefPanels;

import com.meyer.muscle.message.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Sound Preference Panel. Like all preference panels, it's pretty self-contained.
 */
public class SoundPrefs extends JPanel implements ActionListener {
	JavaSharePrefListener target;
	Message prefs;

	JComboBox cmboSoundPack;
	JCheckBox chkUserName;
	JCheckBox chkPrivateMessage;
	JCheckBox chkPrivateWindow;
	JCheckBox chkWatchPattern;

	public SoundPrefs(JavaSharePrefListener prefHandler, Message prefMessage) {
		// Create UI
		super(new GridLayout(8, 1, 3, 3));

		String[] sndPack = {"Disabled", "Default"};
		cmboSoundPack = new JComboBox(sndPack);
		cmboSoundPack.setSelectedIndex(1);
		chkUserName = new JCheckBox("User Name Mentioned");
		chkUserName.addActionListener(this);
		chkPrivateMessage = new JCheckBox("Private Message Received");
		chkPrivateMessage.addActionListener(this);
		chkPrivateWindow = new JCheckBox("New Private Window");
		chkPrivateWindow.addActionListener(this);
		chkWatchPattern = new JCheckBox("Watch pattern match");
		chkWatchPattern.addActionListener(this);

		add(cmboSoundPack);
		add(chkUserName);
		add(chkPrivateMessage);
		add(chkPrivateWindow);
		add(chkWatchPattern);

		setBorder(BorderFactory.createTitledBorder("Sound Preferences"));

		prefs = prefMessage;
		target = prefHandler;

		if (checkSoundAbility()) {
			// Load the soundpacks.
			try {
				File curDir = new File(System.getProperty("user.dir") + File.separator + "sounds");
				if (curDir.isDirectory()) {
					File[] soundPacks = curDir.listFiles();
					for (int x = 0; x < soundPacks.length; x++) {
						if (soundPacks[x].isDirectory()) {
							cmboSoundPack.addItem(soundPacks[x].getName());
						}
					}
				}
			} catch (SecurityException se) {
			}
		} else {
			String[] sysBeeplist = {"Disabled", "System Beep"};
			cmboSoundPack.setModel(new DefaultComboBoxModel(sysBeeplist));
		}

		// Set Preferences.
		if (prefs.hasField("sndUName")) {
			try {
				chkUserName.setSelected(prefs.getBoolean("sndUName"));
			} catch (Exception e) {
			}
		}
		if (prefs.hasField("sndWPat")) {
			try {
				chkWatchPattern.setSelected(prefs.getBoolean("sndWPat"));
			} catch (Exception e) {
			}
		}
		if (prefs.hasField("prvWSnd")) {
			try {
				chkPrivateWindow.setSelected(prefs.getBoolean("prvWSnd"));
			} catch (Exception e) {
			}
		}
		if (prefs.hasField("prvMSnd")) {
			try {
				chkPrivateMessage.setSelected(prefs.getBoolean("prvMSnd"));
			} catch (Exception e) {
			}
		}

		cmboSoundPack.addActionListener(this);

		if (prefs.hasField("soundPack")) {
			try {
				String soundPackName = prefs.getString("soundPack");
				// Now, we have to look for the sound pack!
				for (int x = 0; x < cmboSoundPack.getItemCount(); x++) {
					if (cmboSoundPack.getItemAt(x).toString().equals(soundPackName)) {
						cmboSoundPack.setSelectedIndex(x);
					}// If the soundpack never matches, we don't have to worry.
				}
			} catch (Exception e) {
			}
		}
	}

	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == cmboSoundPack) {
			if (cmboSoundPack.getSelectedIndex() == 0) {
				disableSounds();
			} else {
				enableSounds();
				target.soundPackChange(cmboSoundPack.getSelectedItem().toString());
			}
		} else if (ae.getSource() == chkUserName) {
			target.soundOnUserNameChange(chkUserName.isSelected());
		} else if (ae.getSource() == chkPrivateWindow) {
			target.soundOnPrivateWindowChange(chkPrivateWindow.isSelected());
		} else if (ae.getSource() == chkPrivateMessage) {
			target.soundOnPrivateMessageChange(chkPrivateMessage.isSelected());
		} else if (ae.getSource() == chkWatchPattern) {
			target.soundOnWatchPatternChange(chkWatchPattern.isSelected());
		}
	}

	private boolean checkSoundAbility() {
		if (System.getProperty("java.version").startsWith("1.1")) {
			return false;
		} else {
			return true;
		}
	}

	private void disableSounds() {
		chkUserName.setEnabled(false);
		chkPrivateMessage.setEnabled(false);
		chkPrivateWindow.setEnabled(false);
		chkWatchPattern.setEnabled(false);

		target.soundOnUserNameChange(false);
		target.soundOnPrivateWindowChange(false);
		target.soundOnPrivateMessageChange(false);
		target.soundOnWatchPatternChange(false);
	}

	private void enableSounds() {
		chkUserName.setEnabled(true);
		chkPrivateMessage.setEnabled(true);
		chkPrivateWindow.setEnabled(true);
		chkWatchPattern.setEnabled(true);

		target.soundOnUserNameChange(chkUserName.isSelected());
		target.soundOnPrivateMessageChange(chkPrivateMessage.isSelected());
		target.soundOnPrivateWindowChange(chkPrivateWindow.isSelected());
		target.soundOnWatchPatternChange(chkWatchPattern.isSelected());
	}
}
