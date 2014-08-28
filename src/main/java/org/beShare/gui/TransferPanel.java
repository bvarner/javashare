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
import org.beShare.network.TransferItem;
import org.beShare.network.TransferStatus;

import javax.activation.MimetypesFileTypeMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
	DropMenu<String> queryMenu;
	QueryTable queryTable;
	TableSorter querySorter;
	JScrollPane tableScroller;
	JButton btnDownloadFiles;
	JButton btnRemoveDownload;
	JavaShareTransceiver transceiver;
	String session;
	String files;
	JList lstTransfers;
	private JScrollPane lstTranScroller;

	public TransferPanel(final JavaShareTransceiver transceiver) {
		this.transceiver = transceiver;

		queryMenu = new DropMenu<>("Query: ", 20, transceiver.getQueryModel());

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
				transceiver.stopQuery();
				pnlInProgress.setQueryInProgress(false);
			}
		});
		btnDownloadFiles = new JButton("Download Selected");
		btnDownloadFiles.setEnabled(false);
		btnDownloadFiles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HashMap<BeShareUser, Collection<TransferItem>> itemMap = new HashMap<>();

				int[] selected = queryTable.getSelectedRows();
				for (int i = 0; i < selected.length; i++) {
					BeShareUser remoteUser = transceiver.getUserDataModel().findByNameOrSession(queryTable.getValueAt(selected[i], 3).toString());
					Collection<TransferItem> items = itemMap.get(remoteUser);
					if (items == null) {
						items = new ArrayList<TransferItem>();
					}
					items.add(new TransferItem(
							                          transceiver.getPreferences().get("downloadLocation", (System.getProperty("user.home") + System.getProperty("path.Separator") + "Downloads")),
							                          queryTable.getValueAt(selected[i], 1).toString(),
							                          Long.parseLong(queryTable.getValueAt(selected[i], 2).toString()),
							                          (Icon)queryTable.getValueAt(selected[i], 0)));

					itemMap.put(remoteUser, items);
				}

				// Now create a download for each collection from each user.
				for (Map.Entry<BeShareUser, Collection<TransferItem>> entry : itemMap.entrySet()) {
					transceiver.getTransferModel().add(new Download(transceiver, entry.getValue(), entry.getKey()));
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
					session = transceiver.getUserDataModel().findByNameOrSession(session).getSessionID();

					files = files.substring(0, files.lastIndexOf("@"));
				}
				transceiver.startQuery(session, files);
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

		querySorter = new TableSorter(transceiver.getQueryTableModel());
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
				for (int i = selected.length - 1; i >= 0; i--) {
					transceiver.getTransferModel().remove(selected[i]);
				}
			}
		});

		//transferList = new DefaultListModel();
		lstTransfers = new JList(transceiver.getTransferModel());
		lstTransfers.setFocusable(false);
		lstTransfers.addListSelectionListener(new TransferSelectionListener());
		lstTransfers.setCellRenderer(new TransferProgressRenderer());
		lstTranScroller =
				new JScrollPane(lstTransfers, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		pnlTransfer.add(lstTranScroller, BorderLayout.CENTER);
		pnlTransfer.add(btnRemoveDownload, BorderLayout.SOUTH);

		// Add the children to their parents!
		pnlQuery.add(pnlQueryControl, BorderLayout.SOUTH);
		pnlQuery.add(tableScroller, BorderLayout.CENTER);

		// Make and start the shared file upload thread.
		sharedFileLister = new ShareFileMaintainer(transceiver);
		Thread listThread = new Thread(sharedFileLister, "Mr. McFeely");
		listThread.setPriority(Thread.MIN_PRIORITY);
		listThread.start();
	}

	/**
	 * Removes the file from the result set.
	 */
	public void removeResult(SharedFile killFile) {
		//Get the name and session id of the file, remove it from the table.
		transceiver.getQueryTableModel().removeFile(transceiver.getUserDataModel().findNameBySession(killFile.getSessionID()), killFile.getName());
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
	private class TransferProgressRenderer extends JPanel implements ListCellRenderer<AbstractTransfer> {
		JLabel details;
		JProgressBar progress;
		JLabel status;

		public TransferProgressRenderer() {
			super();
			setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
			setLayout(new BorderLayout());
			details = new JLabel();
			details.setFont(details.getFont().deriveFont(Font.PLAIN, 10.0f));
			details.setHorizontalTextPosition(SwingConstants.TRAILING);
			progress = new JProgressBar(0, 100);
			status = new JLabel();
			status.setFont(details.getFont());
			add(details, BorderLayout.NORTH);
			add(progress);
			add(status, BorderLayout.SOUTH);
		}

		@Override
		public Dimension getPreferredSize() {
			Dimension d = super.getPreferredSize();
			d.width = lstTranScroller.getViewport().getExtentSize().width;
			return d;
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends AbstractTransfer> list, AbstractTransfer transfer, int index,
		                                              boolean isSelected, boolean cellHasFocus) {
			if (isSelected) {
				setBackground(list.getSelectionBackground());
			} else {
				setBackground(list.getBackground());
			}

			details.setText(transfer.toString());
			TransferItem currentItem = transfer.getCurrentItem();
			if (currentItem != null) {
				details.setIcon(currentItem.getIcon());
			} else {
				details.setIcon(null);
			}
			progress.setValue(0);
			String progressReport = transfer.updateProgress(progress);
			if ("".equals(progressReport)) {
				status.setText(transfer.getStatus().getText());
			} else {
				status.setText(progressReport);
			}

			return this;
		}

		public void paint(Graphics g) {
			Graphics2D g2d = ((Graphics2D) g);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
