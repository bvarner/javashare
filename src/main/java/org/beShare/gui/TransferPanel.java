/* Change Log
	1.4.2002 - Finalized the GUI for 2.0
*/
package org.beShare.gui;

import org.beShare.data.BeShareUser;
import org.beShare.data.SharedFile;
import org.beShare.gui.swingAddons.TableSorter;
import org.beShare.network.AbstractTransfer;
import org.beShare.network.Download;
import org.beShare.network.JavaShareTransceiver;
import org.beShare.network.ShareFileMaintainer;
import org.beShare.network.TransferManager;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Hashtable;

/**
 * The panel in charge of querys, and file transfer queue viewing. This is a jack-of-all-trades
 * piece of code, breaking from the overly specialized classes found elsewhere in this program.
 *
 * @author Bryan Varner
 */
public class TransferPanel extends JPanel {
	ShareFileMaintainer sharedFileLister;

	JPanel pnlQuery;
	JPanel pnlTransfer;
	JSplitPane transferSplit;

	DropMenu<String> queryMenu = new DropMenu<>("Query: ", 15, new StringDropMenuModel(20));

	QueryTable queryTable;
	TableSorter querySorter;
	JScrollPane tableScroller;

	JButton btnDownloadFiles;
	JButton btnRemoveDownload;

	JavaShareTransceiver connection;

	TransferManager transMan;

	String session;
	String files;

	JList lstTransfers;

	public TransferPanel(final JavaShareTransceiver connection) {
		this.connection = connection;

		transMan = new TransferManager(connection);

		this.setLayout(new BorderLayout());
		pnlQuery = new JPanel(new BorderLayout(5, 5));
		pnlQuery.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		pnlTransfer = new JPanel(new BorderLayout(5, 5));
		pnlTransfer.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

		transferSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, pnlQuery, pnlTransfer);
		transferSplit.setBorder(BorderFactory.createEmptyBorder());
		pnlQuery.setPreferredSize(new Dimension(400, 200));
		transferSplit.setResizeWeight(.80);

		this.add(transferSplit, BorderLayout.CENTER);

		final JButton btnStopQuery = new JButton("Stop Query");
		final QueryProgressIndicator pnlInProgress = new QueryProgressIndicator();
		pnlInProgress.setPreferredSize(btnStopQuery.getPreferredSize());
		btnStopQuery.setEnabled(false);
		btnStopQuery.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnStopQuery.setEnabled(false);
				connection.stopQuery();
				pnlInProgress.setQueryInProgress(false);
			}
		});
		btnDownloadFiles = new JButton("Download Selected");
		btnDownloadFiles.setEnabled(false);
		btnDownloadFiles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Find out what's selected.
				int[] selected = queryTable.getSelectedRows();
				if (selected.length > 0) {
					String filenames[] = new String[selected.length];

					for (int x = 0; x < selected.length; x++) {
						// Create the new file info.
						filenames[x] = queryTable.getModel().getValueAt(selected[x], 1).toString();
					}
					BeShareUser tempUser = connection.getQueryTableModel().getUser(selected[0]);
					Download fileTransfer = new Download(filenames,
							                                    tempUser.getIPAddress(),
							                                    tempUser.getPort(),
							                                    transMan.getDownloadPath(),
							                                    tempUser.getName(),
							                                    tempUser.getSessionID(),
							                                    tempUser.getFirewall(),
							                                    connection);
					transMan.add(fileTransfer);
				}
			}
		});

		JPanel pnlQueryControl = new JPanel();
		pnlQueryControl.setLayout(new BoxLayout(pnlQueryControl, BoxLayout.X_AXIS));

		queryMenu.getModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				btnStopQuery.setEnabled(true);
				// start new query.
				session = "*";
				files = queryMenu.getModel().getSelectedItem();
				if (files.lastIndexOf("@") > -1) {
					session = files.substring(files.lastIndexOf("@") + 1).trim();
					session = connection.getUserDataModel().findByNameOrSession(session).getSessionID();

					files = files.substring(0, files.lastIndexOf("@"));
				}
				connection.startQuery(session, files);
				pnlInProgress.setQueryInProgress(true);
			}
		});

		pnlQueryControl.add(Box.createHorizontalStrut(3));
		pnlQueryControl.add(pnlInProgress);
		pnlQueryControl.add(Box.createHorizontalStrut(3));
		pnlQueryControl.add(queryMenu);
		pnlQueryControl.add(Box.createHorizontalStrut(6));
		pnlQueryControl.add(btnStopQuery);
		pnlQueryControl.add(Box.createHorizontalStrut(6));
		pnlQueryControl.add(btnDownloadFiles);
		pnlQueryControl.add(Box.createHorizontalStrut(3));

		querySorter = new TableSorter(connection.getQueryTableModel());
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
		btnRemoveDownload.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
//			// clear the completed downloads from the list.
				int[] selected = lstTransfers.getSelectedIndices();
				if (selected != null) {
					for (int x = selected.length; x > 0; x--) {
						transMan.remove(transMan.getElementAt(selected[x - 1]));
					}
				}
			}
		});

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
	 * Removes the file from the result set.
	 */
	public void removeResult(SharedFile killFile) {
		//Get the name and session id of the file, remove it from the table.
		connection.getQueryTableModel().removeFile(connection.getUserDataModel().findNameBySession(killFile.getSessionID()), killFile.getName());
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
	 * Sets the Download button enabled/disabled
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
