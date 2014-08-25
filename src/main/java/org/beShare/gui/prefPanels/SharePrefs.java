///* Change-log:
//	7.15.2002 - Class created.
//*/
//package org.beShare.gui.prefPanels;
//
//import com.meyer.muscle.message.Message;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.io.File;
//
///**
// * Manages the download and shared paths. Also manages the share list auto-updating prefs.
// */
//public class SharePrefs extends JPanel implements ActionListener {
//	SharePrefsListener target;
//	Message prefs;
//
//	JTextField txtDownloadPath;
//	JTextField txtSharedPath;
//
//	JButton btnChooseDownloadPath;
//	JButton btnChooseSharePath;
//
//	JButton btnUpdateNow;
//
//	JCheckBox chkAutoListUpdate;
//
//	JCheckBox chkSharingEnabled;
//
//	JTextField txtUpdateDelay;
//
//	JFileChooser dirPicker;
//
//	public SharePrefs(SharePrefsListener prefHandler, Message prefMessage) {
//		super(new GridLayout(7, 1, 3, 3));
//		setBorder(BorderFactory.createTitledBorder("Sharing Preferences"));
//
//		chkSharingEnabled = new JCheckBox("Sharing Enabled");
//
//		dirPicker = new JFileChooser();
//		dirPicker.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//		dirPicker.setDialogType(JFileChooser.OPEN_DIALOG);
//
//		target = prefHandler;
//		prefs = prefMessage;
//
//		JPanel pnlDownload = new JPanel();
//		pnlDownload.setLayout(new BoxLayout(pnlDownload, BoxLayout.X_AXIS));
//
//		JPanel pnlShared = new JPanel();
//		pnlShared.setLayout(new BoxLayout(pnlShared, BoxLayout.X_AXIS));
//
//		txtDownloadPath = new JTextField(20);
//		txtDownloadPath.setEditable(false);
//		txtSharedPath = new JTextField(20);
//		txtSharedPath.setEditable(false);
//
//		btnChooseDownloadPath = new JButton("Choose");
//		btnChooseSharePath = new JButton("Choose");
//
//		pnlDownload.add(new JLabel("Download To: "));
//		pnlDownload.add(txtDownloadPath, BorderLayout.CENTER);
//		pnlDownload.add(btnChooseDownloadPath, BorderLayout.EAST);
//
//		pnlShared.add(new JLabel("Shared Folder: "));
//		pnlShared.add(txtSharedPath, BorderLayout.CENTER);
//		pnlShared.add(btnChooseSharePath, BorderLayout.EAST);
//
//		chkAutoListUpdate = new JCheckBox();
//
//		txtUpdateDelay = new JTextField(3);
//
//		JPanel pnlUpdate = new JPanel();
//		pnlUpdate.setLayout(new BoxLayout(pnlUpdate, BoxLayout.X_AXIS));
//
//		pnlUpdate.add(chkAutoListUpdate);
//		pnlUpdate.add(new JLabel("Auto-update shared files every "));
//		pnlUpdate.add(txtUpdateDelay);
//		pnlUpdate.add(new JLabel(" seconds."));
//
//		btnUpdateNow = new JButton("Update Shared List");
//		btnUpdateNow.addActionListener(this);
//
//		JPanel pnlUpdateButton = new JPanel();
//		pnlUpdateButton.add(btnUpdateNow);
//
//		this.add(pnlDownload);
//		this.add(chkSharingEnabled);
//		this.add(pnlShared);
//		this.add(pnlUpdate);
//		this.add(pnlUpdateButton);
//
//		// Set the values from the Message.
//		if (prefs.hasField("fileSharingEnabled")) {
//			try {
//				chkSharingEnabled.setSelected(prefs.getBoolean("fileSharingEnabled"));
//			} catch (Exception e) {
//			}
//		}
//
//		if (prefs.hasField("downloadPath")) {
//			try {
//				txtDownloadPath.setText(prefs.getString("downloadPath"));
//			} catch (Exception e) {
//			}
//		}
//
//		if (prefs.hasField("sharedPath")) {
//			try {
//				txtSharedPath.setText(prefs.getString("sharedPath"));
//			} catch (Exception e) {
//			}
//		}
//
//		if (prefs.hasField("autoShareUpdate")) {
//			try {
//				chkAutoListUpdate.setSelected(prefs.getBoolean("autoShareUpdate"));
//			} catch (Exception e) {
//			}
//		}
//
//		if (prefs.hasField("autoShareDelay")) {
//			try {
//				txtUpdateDelay.setText("" + prefs.getInt("autoShareDelay"));
//			} catch (Exception e) {
//			}
//		}
//		// Add action Listeners.
//		chkSharingEnabled.addActionListener(this);
//		btnChooseDownloadPath.addActionListener(this);
//		btnChooseSharePath.addActionListener(this);
//		chkAutoListUpdate.addActionListener(this);
//		txtUpdateDelay.addActionListener(this);
//	}
//
//	public void actionPerformed(ActionEvent ae) {
//		if (ae.getSource() == btnChooseDownloadPath) {
//			dirPicker.setCurrentDirectory(new File(txtDownloadPath.getText().trim()));
//			dirPicker.changeToParentDirectory();
//			dirPicker.setDialogTitle("Choose a new Folder to Download to");
//			int retval = dirPicker.showOpenDialog(this);
//			// If it was approved. Set the options. All new downloads will be saved there.
//			if (retval == JFileChooser.APPROVE_OPTION) {
//				prefs.setString("downloadPath", dirPicker.getSelectedFile().getAbsolutePath());
//				txtDownloadPath.setText(dirPicker.getSelectedFile().getAbsolutePath());
//			}
//		} else if (ae.getSource() == btnChooseSharePath) {
//			dirPicker.setCurrentDirectory(new File(txtSharedPath.getText().trim()));
//			dirPicker.changeToParentDirectory();
//			dirPicker.setDialogTitle("Choose a new Folder to Share");
//			int retval = dirPicker.showOpenDialog(this);
//			// If it was approved. Set the options. All new downloads will be saved there.
//			if (retval == JFileChooser.APPROVE_OPTION) {
//				prefs.setString("sharedPath", dirPicker.getSelectedFile().getAbsolutePath());
//				txtSharedPath.setText(dirPicker.getSelectedFile().getAbsolutePath());
//				// Update the list, if sharing is enabled.
//				if (chkSharingEnabled.isSelected()) {
//					target.sharePathChanged();
//				}
//			}
//		} else if (ae.getSource() == chkAutoListUpdate) {
//			target.shareAutoUpdateEnableChange(chkAutoListUpdate.isSelected());
//			if (chkAutoListUpdate.isSelected()) {
//				txtUpdateDelay.setEnabled(true);
//			} else {
//				txtUpdateDelay.setEnabled(false);
//			}
//			prefs.setBoolean("autoShareUpdate", chkAutoListUpdate.isSelected());
//		} else if (ae.getSource() == txtUpdateDelay) {
//			try {
//				target.shareUpdateDelayChanged(Integer.parseInt(txtUpdateDelay.getText()));
//				prefs.setInt("autoShareDelay", Integer.parseInt(txtUpdateDelay.getText()));
//			} catch (NumberFormatException nfe) {
//			}
//		} else if (ae.getSource() == chkSharingEnabled) {
//			prefs.setBoolean("fileSharingEnabled", chkSharingEnabled.isSelected());
//			target.sharingEnableChange(chkSharingEnabled.isSelected());
//			if (chkSharingEnabled.isSelected()) {
//				target.shareAutoUpdateEnableChange(chkAutoListUpdate.isSelected());
//			} else {
//				target.shareAutoUpdateEnableChange(false);
//			}
//			chkAutoListUpdate.setEnabled(chkSharingEnabled.isSelected());
//			txtUpdateDelay.setEnabled(chkSharingEnabled.isSelected());
//			btnUpdateNow.setEnabled(chkSharingEnabled.isSelected());
//		} else if (ae.getSource() == btnUpdateNow) {
//			target.sharePathChanged();
//		}
//	}
//}
