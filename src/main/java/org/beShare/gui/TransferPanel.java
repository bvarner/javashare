/* Change Log
	1.4.2002 - Finalized the GUI for 2.0
*/
package org.beShare.gui;

import org.beShare.data.BeShareUser;
import org.beShare.data.SharedFile;
import org.beShare.gui.prefPanels.SharePrefsListener;
import org.beShare.gui.swingAddons.TableSorter;
import org.beShare.network.AbstractTransfer;
import org.beShare.network.Download;
import org.beShare.network.JavaShareTransceiver;
import org.beShare.network.ShareFileMaintainer;
import org.beShare.network.TransferManager;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.Vector;

/**
 * The panel in charge of querys, and file transfer queue viewing. This is a jack-of-all-trades
 * piece of code, breaking from the overly specialized classes found elsewhere in this program.
 *
 * @author Bryan Varner
 */
public class TransferPanel extends JPanel implements SharePrefsListener, ActionListener {
	ShareFileMaintainer sharedFileLister;

	JPanel pnlQuery;
	JPanel pnlTransfer;
	JSplitPane transferSplit;

	QueryProgressIndicator pnlInProgress;
	JPanel pnlQueryControl;
	DropMenu recentQueryMenu;
	JTextField txtQuery;
	JButton btnStartQuery;
	JButton btnStopQuery;

	QueryTable queryTable;
	QueryTableModel queryModel;
	TableSorter querySorter;
	JScrollPane tableScroller;
	Vector colNames;

	JButton btnDownloadFiles;
	JButton btnRemoveDownload;

	JavaShareTransceiver connection;

	TransferManager transMan;

	String session;
	String files;

	Hashtable typeIcons;

	JList lstTransfers;
	DefaultListModel transferList;

	public TransferPanel(JavaShareTransceiver connection) {
		this.connection = connection;

		transMan = new TransferManager(connection);

		typeIcons = new Hashtable();

		this.setLayout(new BorderLayout());
		pnlQuery = new JPanel(new BorderLayout());
		pnlTransfer = new JPanel(new BorderLayout());

		transferSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, pnlQuery, pnlTransfer);
		transferSplit.setOneTouchExpandable(true);
		pnlQuery.setPreferredSize(new Dimension(400, 200));
		transferSplit.setResizeWeight(.80);

		this.add(transferSplit, BorderLayout.CENTER);

		// Query preference stuff
//		if (prefsMessage.hasField("querys")) {
//			Vector oldQuerys = new Vector();
//			String[] oldStrings = prefsMessage.getStrings("querys");
//			for (int x = 0; x < oldStrings.length; x++) {
//				oldQuerys.addElement(oldStrings[x]);
//			}
//			recentQueryMenu = new DropMenu("Query:", oldQuerys, 15);
//		} else {
			recentQueryMenu = new DropMenu("Query:", 15);
//		}

		txtQuery = new JTextField("*.mp3", 12);
		btnStartQuery = new JButton("Start Query");
		btnStopQuery = new JButton("Stop Query");
		btnDownloadFiles = new JButton("Download Selected");
		btnDownloadFiles.setEnabled(false);
		btnDownloadFiles.addActionListener(this);
		pnlInProgress = new QueryProgressIndicator(btnStartQuery);

		pnlQueryControl = new JPanel();
		pnlQueryControl.setLayout(new BoxLayout(pnlQueryControl, BoxLayout.X_AXIS));

		// Add action Listeners here.
		recentQueryMenu.addActionListener(this);
		txtQuery.addActionListener(this);
		btnStartQuery.addActionListener(this);
		btnStopQuery.addActionListener(this);

		pnlQueryControl.add(Box.createHorizontalStrut(3));
		pnlQueryControl.add(pnlInProgress);
		pnlQueryControl.add(Box.createHorizontalStrut(3));
		pnlQueryControl.add(recentQueryMenu);
		pnlQueryControl.add(txtQuery);
		pnlQueryControl.add(Box.createHorizontalStrut(6));
		pnlQueryControl.add(btnStartQuery);
		pnlQueryControl.add(Box.createHorizontalStrut(3));
		pnlQueryControl.add(btnStopQuery);
		pnlQueryControl.add(Box.createHorizontalStrut(6));
		pnlQueryControl.add(btnDownloadFiles);
		pnlQueryControl.add(Box.createHorizontalStrut(3));

		// Query table!
		colNames = new Vector();
		colNames.addElement("");
		colNames.addElement("File Name");
		colNames.addElement("File Size");
		colNames.addElement("User");
		colNames.addElement("Path");
		colNames.addElement("Kind");
		colNames.addElement("Connection");
		//colNames.addElement("Modified");

		Vector tableData = new Vector();

		queryModel = new QueryTableModel(tableData, colNames);
		querySorter = new TableSorter(queryModel);
		queryTable = new QueryTable(querySorter);

		queryTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		queryTable.getColumn("").setCellRenderer(new IconCellRenderer());
		queryTable.getColumn("").setPreferredWidth(20);
		queryTable.getColumn("").setMaxWidth(20);
		queryTable.getColumn("").setResizable(false);
		int[] columnSizes = {20, 200, 100, 100, 200, 250, 100};
		setQueryTableColumnWidths(columnSizes);
		queryTable.setShowGrid(false);
		queryTable.setIntercellSpacing(new Dimension(0, 2));
		queryTable.setRowHeight(queryTable.getRowHeight() + 4);
		querySorter.addMouseListenerToHeaderInTable(queryTable);
		queryTable.getTableHeader().setReorderingAllowed(false);

		queryTable.getSelectionModel().addListSelectionListener(new QuerySelectionListener());

		tableScroller = new JScrollPane(queryTable,
				                               JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				                               JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		btnRemoveDownload = new JButton("Remove");
		btnRemoveDownload.setEnabled(false);
		btnRemoveDownload.addActionListener(this);

		//transferList = new DefaultListModel();
		lstTransfers = new JList(transMan);
		lstTransfers.addListSelectionListener(new TransferSelectionListener());
		lstTransfers.setCellRenderer(new TransferProgressRenderer());
		JScrollPane lstTranScroller = new JScrollPane(lstTransfers);

		pnlTransfer.add(lstTranScroller, BorderLayout.CENTER);
		pnlTransfer.add(btnRemoveDownload, BorderLayout.SOUTH);

		// Add the children to their parents!
		pnlQuery.add(pnlQueryControl, BorderLayout.SOUTH);
		pnlQuery.add(tableScroller, BorderLayout.CENTER);

		// Make and start the shared file upload thread.
		sharedFileLister = new ShareFileMaintainer(connection);
		Thread listThread = new Thread(sharedFileLister, "Mr. McFeely");
		listThread.setPriority(Thread.MIN_PRIORITY);
		listThread.start();
	}

	/**
	 * Implements part of the Prefs listener.
	 */
	public void sharingEnableChange(boolean enabled) {
		shareAutoUpdateEnableChange(enabled);
		if (enabled) {
			sharedFileLister.resetShareList();
			sharedFileLister.updateList();
			// Have JavaShareTransceiver start listening for transfers.
		} else {
			sharedFileLister.sharingDisabled();
			// Have JavaShareTransceiver stop listening for transfers.
		}
	}

	/**
	 * Implements part of the Prefs listener.
	 */
	public void sharePathChanged() {
		sharedFileLister.updateList();
	}

	/**
	 * Implements part of the Prefs listener.
	 */
	public void shareUpdateDelayChanged(int delay) {
		sharedFileLister.setDelay(delay);
	}

	/**
	 * Implements part of the Prefs listener.
	 */
	public void shareAutoUpdateEnableChange(boolean enabled) {
		if (enabled) {
			sharedFileLister.setDelay(sharedFileLister.getDelay());
		} else {
			sharedFileLister.setDelay(-1);
		}
	}

	/**
	 * Forces a re-uploading of the shared file list.
	 */
	public void resetShareList() {
		sharedFileLister.resetShareList();
	}

	/**
	 * Implements ActionListener.
	 */
	public void actionPerformed(ActionEvent e) {
		// The recent query menu was clicked.
		if (e.getSource() == recentQueryMenu
				    && (recentQueryMenu.getSelectedIndex() != -1)) {
			String selMenu = (String) recentQueryMenu.getSelectedItem();
			if (selMenu != txtQuery.getText()) {
				txtQuery.setText(selMenu);
				resetQuery();
			}
			// Query textbox had a return pressed.
		} else if ((e.getSource() == txtQuery) || (e.getSource() == btnStartQuery)) {
			boolean addNew = true;
			for (int x = 0; x < recentQueryMenu.getItemCount(); x++) {
				if (((String) recentQueryMenu.getItemAt(x)).equals(
						                                                  txtQuery.getText())) {
					recentQueryMenu.setSelectedIndex(x);
					addNew = false;
				}
			}
			if (addNew) {
				recentQueryMenu.addItem(txtQuery.getText());
				recentQueryMenu.setSelectedIndex(
						                                recentQueryMenu.getItemCount() - 1);
				//prefsMessage.setStrings("querys", recentQueryMenu.getStringItems());
			}
			resetQuery();
		} else if (e.getSource() == btnStopQuery) {
			// The query was stopped.
			connection.stopQuery();
			pnlInProgress.setQueryInProgress(false);
		} else if (e.getSource() == btnDownloadFiles) {
			// Find out what's selected.
			int[] selected = queryTable.getSelectedRows();
			if (selected.length > 0) {
				String filenames[] = new String[selected.length];

				for (int x = 0; x < selected.length; x++) {
					// Create the new file info.
					filenames[x] = queryTable.getModel().getValueAt(selected[x], 1).toString();
				}
				BeShareUser tempUser = queryModel.getUser(selected[0]);
				System.out.println("Downloading from " + tempUser.toString());
				Download fileTransfer = new Download(filenames,
						                                    tempUser.getIPAddress(),
						                                    tempUser.getPort(),
						                                    transMan.getDownloadPath(),
						                                    tempUser.getName(),
						                                    tempUser.getSessionID(),
						                                    tempUser.getFirewall(),
						                                    connection);
				transMan.addTransfer(fileTransfer);
			}
		} else if (e.getSource() == btnRemoveDownload) {
			// clear the completed downloads from the list.
			int[] selected = lstTransfers.getSelectedIndices();
			if (selected != null) {
				for (int x = selected.length; x > 0; x--) {
					transMan.removeTransfer((AbstractTransfer) transMan.getElementAt(selected[x - 1]));
				}
			}
		}
	}

	/**
	 * Resets the JavaShareTransceiver query info. This also parses the text of txtQuery to build the query.
	 */
	private void resetQuery() {
		queryModel.clearTable();
		// start new query.
		session = "*";
		files = "*";
		if (txtQuery.getText().lastIndexOf("@") > -1) {
			session = txtQuery.getText().substring(
					                                      txtQuery.getText().lastIndexOf("@") + 1,
					                                      txtQuery.getText().length());
			// Try parsing the session as an int. if it succeedes we don't need to do jack squat.
			// If it fails, we can assume it is a username, and do a reverselookup for a session ID.
			try {
				Integer.parseInt(session);
			} catch (NumberFormatException nfe) {
				session = connection.getUserDataModel().findSessionByName(session);
			}
			if (txtQuery.getText().lastIndexOf("@") > 0) {
				files = txtQuery.getText().substring(0, txtQuery.getText().lastIndexOf("@"));
			}
		} else {
			files = txtQuery.getText();
		}
		connection.startQuery(session, files);
		pnlInProgress.setQueryInProgress(true);
	}

	/**
	 * Gets the list of querys.
	 */
	public String[] getRecentQueries() {
		return recentQueryMenu.getStringItems();
	}

	/**
	 * Clears the query results.
	 */
	public void clearQueryResults() {
		connection.stopQuery();
		queryModel.clearTable();
	}

	/**
	 * Adds a new file to the result list
	 */
	public void addResult(SharedFile newFile) {
		String size = (Long.toString(newFile.getSize()));
		if (size.length() <= 3) {
			size = size + "bytes";
		} else if (size.length() <= 6) {
			size = (newFile.getSize() / 1024) + " kb";
		} else if (size.length() <= 9) {
			size = ((double) (newFile.getSize() / (1024 ^ 2))) / 1000 + " MB";
		}

		ImageIcon fileIcon = new ImageIcon();

		// Here we go baby, the dynamic pre-loading of file icons.
		if (!typeIcons.containsKey(newFile.getKind())) {

			if (newFile.getKind().equals("")) {
				// Load the generic file icon.
				fileIcon = AppPanel.loadImage("Images/fileicons/notype.gif", this);
				typeIcons.put(newFile.getKind(), fileIcon);
			} else {
				// Replace / with ^ and . with &
				String fileName = newFile.getKind();
				fileName = fileName.replace('/', '^');
				fileName = fileName.replace('.', '&');
				fileName = fileName.concat(".gif");
				try {
					fileIcon = AppPanel.loadImage("Images/fileicons/" + fileName, this);
					typeIcons.put(newFile.getKind(), fileIcon);
				} catch (NullPointerException npe) {
					fileIcon = AppPanel.loadImage("Images/fileicons/notype.gif", this);
					typeIcons.put(newFile.getKind(), fileIcon);
				}
			}
		} else {
			fileIcon = (ImageIcon) typeIcons.get(newFile.getKind());
		}

		Object[] fileData = {fileIcon, // Icon image name here later!
		                     newFile.getName(),
		                     size,
		                     connection.getUserDataModel().getUser(newFile.getSessionID()),
		                     newFile.getPath(),
		                     newFile.getKind(),
		                     connection.getUserDataModel().getUser(newFile.getSessionID()).getBandwidthLabel()};
		queryModel.insertRow(0, fileData);

		Thread.yield(); // Force the query to play nice.
	}

	/**
	 * Removes the file from the result set.
	 */
	public void removeResult(SharedFile killFile) {
		//Get the name and session id of the file, remove it from the table.
		queryModel.removeFile(connection.getUserDataModel().findNameBySession(killFile.getSessionID()), killFile.getName());
	}

	/**
	 * Sets the column widths for the table
	 */
	private void setQueryTableColumnWidths(int[] colWidths) {
		if (colWidths != null) {
			TableColumnModel clmModel = queryTable.getColumnModel();

			for (int x = 0; x < colWidths.length; x++) {
				clmModel.getColumn(x).setPreferredWidth(colWidths[x]);
			}
		}
	}

	/**
	 * Returns the position of the divider between the query and transfer list.
	 */
	public int getDividerLocation() {
		return transferSplit.getDividerLocation();
	}

	/**
	 * The beautiful cell renderer that draws the beautiful BeOS file icons in the query results.
	 */
	private class IconCellRenderer extends DefaultTableCellRenderer {
		public Component getTableCellRendererComponent(JTable table,
		                                               Object value,
		                                               boolean isSelected,
		                                               boolean hasFocus,
		                                               int row,
		                                               int column) {
			Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			((IconCellRenderer) comp).setIcon((ImageIcon) value);
			((IconCellRenderer) comp).setText("");
			return comp;
		}
	}

	/**
	 * Renders a Transfers information in the list.
	 */
	private class TransferProgressRenderer extends DefaultListCellRenderer {
		String status = "";
		double progress = 0;
		boolean selected = false;
		Color selcolor = null;
		Color progColor = null;
		DecimalFormat progressFormatter = new DecimalFormat("####.##");

		public TransferProgressRenderer() {
			super();
			setOpaque(false);
			status = "";
		}

		public Component getListCellRendererComponent(JList list, Object value, int index,
		                                              boolean isSelected, boolean cellHasFocus) {
			if (selcolor == null) {
				Color old = list.getSelectionBackground();
				selcolor = new Color(old.getRed(), old.getGreen(), old.getBlue(), 168);
				progColor = new Color(0, old.getGreen(), 0, 54);
			}

			// Initialize the defaults
			progress = 0;
			selected = isSelected;

			String status = "<html>";
			AbstractTransfer t = (AbstractTransfer) value;

			if (value instanceof Download) {
				Download dwn = (Download) value;
				status += "Downloading from " + dwn.getUserName() + "<br>";
			} else {
				status += "Upload to " + "<br>";
			}

			if (t.getStatus() == AbstractTransfer.ACTIVE) {
				status += "File: " + t.getFileName() + "<br>";
			}

			switch (t.getStatus()) {
				case AbstractTransfer.CONNECTING:
					status += "Connecting<br>";
					break;
				case AbstractTransfer.AWAITING_CALLBACK:
					status += "Awaiting Callback<br>";
					break;
				case AbstractTransfer.ACTIVE:
					progress = ((double) t.getFileTransfered() / t.getFileSize());
					if (t.getFileSize() > (1024 * 1024)) {
						status +=
								"Transfered: " + progressFormatter.format((double) t.getFileTransfered() / (1024 * 1024)) + "MB of " + progressFormatter.format((double) t.getFileSize() / (1024 * 1024)) + "MB<br>";
					} else if (t.getFileSize() > 1024) {
						status +=
								"Transfered: " + progressFormatter.format((double) t.getFileTransfered() / 1024) + "k of " + progressFormatter.format((double) t.getFileSize() / 1024) + "k<br>";
					} else {
						status += "Transfered: " + t.getFileTransfered() + " of " + t.getFileSize() + " bytes<br>";
					}
					break;
				case AbstractTransfer.REMOTE_QUEUED:
					status += "Remotely Queued<br>";
					break;
				case AbstractTransfer.EXAMINING:
					status += "Examining...<br>";
					break;
				case AbstractTransfer.FINISHED:
					status += "Completed<br>";
					progress = 1.0;
					break;
				case AbstractTransfer.LOCALLY_QUEUED:
					status += "Locally Queued<br>";
					break;
				case AbstractTransfer.ERROR:
					status += "An Error Has Occured<br>";
					break;
				default:
					status += "";
			}
			status += "</html>";
			setText(status);

			return this;
		}

		public void paint(Graphics g) {
			// Draw the background.
			if (progress > 0) {
				g.setColor(progColor);
				g.fillRect(0, 0, (int) (getWidth() * progress), getHeight());
			}

			if (selected) {
				g.setColor(selcolor);
				g.fillRect(0, 0, getWidth(), getHeight());
			}

			// Paint the Mo-FO.
			super.paint(g);
		}
	}

	/**
	 * Sets the Download butten enabled/disabled
	 */
	private class QuerySelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			if (queryTable.getSelectedRow() >= 0) {
				btnDownloadFiles.setEnabled(true);
			} else {
				btnDownloadFiles.setEnabled(false);
			}
		}
	}

	/**
	 * Sets the Remove Transfer button enabled/disabled
	 */
	private class TransferSelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			if (lstTransfers.getSelectedIndex() >= 0) {
				btnRemoveDownload.setEnabled(true);
			} else {
				btnRemoveDownload.setEnabled(false);
			}
		}
	}
}
