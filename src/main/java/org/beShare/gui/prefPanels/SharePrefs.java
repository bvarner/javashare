package org.beShare.gui.prefPanels;

import org.beShare.gui.MenuBar;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.prefs.Preferences;

/**
 * Manages the download and shared paths.
 */
public class SharePrefs extends JPanel {
	public SharePrefs(final Preferences preferences) {
		super(new GridLayout(7, 1, 3, 3));
		setBorder(BorderFactory.createTitledBorder("Sharing Preferences"));

		final JCheckBox chkSharingEnabled = new JCheckBox("Sharing Enabled", preferences.getBoolean("sharing", false));
		chkSharingEnabled.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preferences.putBoolean("sharing", chkSharingEnabled.isSelected());
			}
		});

		JPanel pnlDownload = new JPanel();
		pnlDownload.setLayout(new BoxLayout(pnlDownload, BoxLayout.X_AXIS));

		JPanel pnlShared = new JPanel();
		pnlShared.setLayout(new BoxLayout(pnlShared, BoxLayout.X_AXIS));

		final JTextField txtDownloadPath = new JTextField(preferences.get("downloadLocation", ""), 20);
		txtDownloadPath.setEditable(false);
		final JTextField txtSharedPath = new JTextField(preferences.get("sharedLocation", ""), 20);
		txtSharedPath.setEditable(false);

		final JButton btnChooseDownloadPath = new JButton("Choose");
		btnChooseDownloadPath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String dir =
						selectDirectory((Dialog) SwingUtilities.getWindowAncestor(SharePrefs.this), "Select a download location");
				txtDownloadPath.setText(dir);
				preferences.put("downloadLocation", dir);
			}
		});
		JButton btnChooseSharePath = new JButton("Choose");
		btnChooseSharePath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String dir =
						selectDirectory((Dialog) SwingUtilities.getWindowAncestor(SharePrefs.this), "Select a shared location");
				txtSharedPath.setText(dir);
				preferences.put("sharedLocation", dir);
			}
		});


		pnlDownload.add(new JLabel("Download To: "));
		pnlDownload.add(txtDownloadPath, BorderLayout.CENTER);
		pnlDownload.add(btnChooseDownloadPath, BorderLayout.EAST);

		pnlShared.add(new JLabel("Shared Folder: "));
		pnlShared.add(txtSharedPath, BorderLayout.CENTER);
		pnlShared.add(btnChooseSharePath, BorderLayout.EAST);

		this.add(pnlDownload);
		this.add(chkSharingEnabled);
		this.add(pnlShared);
	}


	private String selectDirectory(final Dialog owner, final String title) {
		if (MenuBar.isMacOS) {
			System.setProperty("apple.awt.fileDialogForDirectories", "true");
			FileDialog fd = new FileDialog(owner, "Select a shared location", FileDialog.LOAD);
			fd.setFilenameFilter(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return (new File(dir, name).isDirectory());
				}
			});
			fd.setMultipleMode(false);
			fd.setVisible(true);
			System.setProperty("apple.awt.fileDialogForDirectories", "false");
			File[] selected = fd.getFiles();
			if (selected.length > 0) {
				return selected[0].getAbsolutePath();
			}
		} else {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new FileFilter() {
				@Override
				public boolean accept(File f) {
					return f.isDirectory();
				}

				@Override
				public String getDescription() {
					return "Directories Only";
				}
			});
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (chooser.showOpenDialog(owner) == JFileChooser.APPROVE_OPTION) {
				return chooser.getSelectedFile().getAbsolutePath();
			}
		}
		return "";
	}
}
