/* Change-Log
	1.0 - 6.5.2002 - Initial Class creation.
*/
package org.beShare.gui.prefPanels;

import com.meyer.muscle.message.Message;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DisplayPrefs extends PreferencePanel implements ActionListener {
	JavaSharePrefListener target;
	Message prefs;

	JCheckBox chkTimeStamps;
	JCheckBox chkUserEvents;
	JCheckBox chkUploads;
	JCheckBox chkChat;
	JCheckBox chkPrivMessages;
	JCheckBox chkInfoMessages;
	JCheckBox chkWarningMessages;
	JCheckBox chkErrorMessages;

	public DisplayPrefs(JavaSharePrefListener prefHandler, Message prefMessage) {
		super();
		//super(new GridLayout(8, 1, 3, 3));

		target = prefHandler;
		prefs = prefMessage;

		chkTimeStamps = new JCheckBox("Time Stamps", true);
		chkUserEvents = new JCheckBox("User Events", true);
		chkUploads = new JCheckBox("Uploads", true);
		chkChat = new JCheckBox("Chat", true);
		chkPrivMessages = new JCheckBox("Private Messages", true);
		chkInfoMessages = new JCheckBox("Info Messages", true);
		chkWarningMessages = new JCheckBox("Warning Messages", true);
		chkErrorMessages = new JCheckBox("Error Messages", true);

		firstComponent();
		add(chkTimeStamps);
		add(chkUserEvents);
		add(chkUploads);
		add(chkChat);
		add(chkPrivMessages);
		add(chkInfoMessages);
		add(chkWarningMessages);
		add(chkErrorMessages);
		lastComponent();

		setBorder(BorderFactory.createTitledBorder("Display Preferences"));

		// Set the values from the Message.
		if (prefs.hasField("dispTime")) {
			try {
				chkTimeStamps.setSelected(prefs.getBoolean("dispTime"));
			} catch (Exception e) {
			}
		}
		if (prefs.hasField("dispUser")) {
			try {
				chkUserEvents.setSelected(prefs.getBoolean("dispUser"));
			} catch (Exception e) {
			}
		}
		if (prefs.hasField("dispUpload")) {
			try {
				chkUploads.setSelected(prefs.getBoolean("dispUpload"));
			} catch (Exception e) {
			}
		}
		if (prefs.hasField("dispChat")) {
			try {
				chkChat.setSelected(prefs.getBoolean("dispChat"));
			} catch (Exception e) {
			}
		}
		if (prefs.hasField("dispPriv")) {
			try {
				chkPrivMessages.setSelected(prefs.getBoolean("dispPriv"));
			} catch (Exception e) {
			}
		}
		if (prefs.hasField("dispInfo")) {
			try {
				chkInfoMessages.setSelected(prefs.getBoolean("dispInfo"));
			} catch (Exception e) {
			}
		}
		if (prefs.hasField("dispWarn")) {
			try {
				chkWarningMessages.setSelected(prefs.getBoolean("dispWarn"));
			} catch (Exception e) {
			}
		}
		if (prefs.hasField("dispError")) {
			try {
				chkErrorMessages.setSelected(prefs.getBoolean("dispError"));
			} catch (Exception e) {
			}
		}

		// Register the listener.
		chkTimeStamps.addActionListener(this);
		chkUserEvents.addActionListener(this);
		chkUploads.addActionListener(this);
		chkChat.addActionListener(this);
		chkPrivMessages.addActionListener(this);
		chkInfoMessages.addActionListener(this);
		chkWarningMessages.addActionListener(this);
		chkErrorMessages.addActionListener(this);
	}

	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == chkTimeStamps) {
			target.timeStampDisplayChange(chkTimeStamps.isSelected());
		} else if (ae.getSource() == chkUserEvents) {
			target.userEventDisplayChange(chkUserEvents.isSelected());
		} else if (ae.getSource() == chkUploads) {
			target.uploadDisplayChange(chkUploads.isSelected());
		} else if (ae.getSource() == chkChat) {
			target.chatDisplayChange(chkChat.isSelected());
		} else if (ae.getSource() == chkPrivMessages) {
			target.privateDisplayChange(chkPrivMessages.isSelected());
		} else if (ae.getSource() == chkInfoMessages) {
			target.infoDisplayChange(chkInfoMessages.isSelected());
		} else if (ae.getSource() == chkWarningMessages) {
			target.warningDisplayChange(chkWarningMessages.isSelected());
		} else if (ae.getSource() == chkErrorMessages) {
			target.errorDisplayChange(chkErrorMessages.isSelected());
		}
	}
}
