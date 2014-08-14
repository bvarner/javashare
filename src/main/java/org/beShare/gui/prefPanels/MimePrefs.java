/* Change-Log
	1.0 - 7.15.2002 - Initial Class creation.
					Everything seems to be in order and working.
*/
package org.beShare.gui.prefPanels;

import com.meyer.muscle.message.FieldNotFoundException;
import com.meyer.muscle.message.Message;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Mime mappings Preferences panel.
 */
public class MimePrefs extends JPanel implements ActionListener {
	Message prefs;

	JList typeList;
	DefaultListModel listData;

	JButton btnUpdate;
	JButton btnAdd;
	JButton btnRemove;

	JTextField txtExtension;
	JTextField txtMimetype;

	String originalExtension;

	public MimePrefs(Message prefMessage) {
		super(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder("File Types"));

		originalExtension = new String();

		prefs = prefMessage;

		// Default list model with ignores null or empty additions.
		listData = new DefaultListModel() {
			@Override
			public void addElement(Object element) {
				if (element == null || "".equals(element)) {
					return;
				}
				super.addElement(element);
			}
		};

		do {
			String[] filetypes = prefs.getStrings("fileExtensions", new String[0]);
			for (int x = 0; x < filetypes.length; x++) {
				listData.addElement(new TypeListItem(filetypes[x], prefs.getString(filetypes[x], "")));
			}
			break;
		} while (true);

		typeList = new JList(listData);
		typeList.addListSelectionListener(new ListListener());
		typeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		typeList.setCellRenderer(new FileTypeRenderer());

		JScrollPane listScroller = new JScrollPane(typeList);

		JPanel pnlDetail = new JPanel(new GridLayout(2, 1, 2, 2));
		pnlDetail.setBorder(BorderFactory.createTitledBorder("Selected Type Settings"));
		JPanel pnlExtEdit = new JPanel(new BorderLayout());
		JPanel pnlTypEdit = new JPanel(new BorderLayout());

		pnlExtEdit.add(new JLabel("Extensions: "), BorderLayout.WEST);
		pnlTypEdit.add(new JLabel("MIME-Type: "), BorderLayout.WEST);

		txtExtension = new JTextField();
		txtMimetype = new JTextField();

		pnlExtEdit.add(txtExtension, BorderLayout.CENTER);
		pnlTypEdit.add(txtMimetype, BorderLayout.CENTER);

		pnlDetail.add(pnlExtEdit);
		pnlDetail.add(pnlTypEdit);

		JPanel pnlButtons = new JPanel(new GridLayout(1, 3, 2, 2));

		btnUpdate = new JButton("Update");
		btnUpdate.addActionListener(this);
		pnlButtons.add(btnUpdate);

		btnAdd = new JButton("Add Type");
		btnAdd.addActionListener(this);
		pnlButtons.add(btnAdd);

		btnRemove = new JButton("Remove");
		btnRemove.addActionListener(this);
		pnlButtons.add(btnRemove);

		this.add(listScroller, BorderLayout.NORTH);
		this.add(pnlDetail, BorderLayout.CENTER);
		this.add(pnlButtons, BorderLayout.SOUTH);
	}

	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == btnAdd) {
			// Add new item!
			listData.addElement(new TypeListItem(txtExtension.getText(), txtMimetype.getText()));
			String[] extensions = new String[listData.size()];
			for (int counter = 0; counter < listData.size(); counter++) {
				extensions[counter] = ((TypeListItem) listData.elementAt(counter)).getExtension();
			}
			prefs.setStrings("fileExtensions", extensions);
			prefs.setString(txtExtension.getText().trim(), txtMimetype.getText().trim());
		} else if (ae.getSource() == btnUpdate) {
			// Find the old type in the datalist. Set it to the new value.
			for (int counter = 0; counter < listData.size(); counter++) {
				if (((TypeListItem) listData.elementAt(counter)).getExtension().equals(originalExtension)) {
					TypeListItem updateItem = ((TypeListItem) listData.elementAt(counter));

					updateItem.setExtension(txtExtension.getText().trim());
					updateItem.setMimeType(txtMimetype.getText().trim());

					// set the item back to it's location, forcing a data update on the list.
					listData.setElementAt(updateItem, counter);
				}
			}
			// Re-create the index in the preferences.
			String[] extensions = new String[listData.size()];
			for (int counter = 0; counter < listData.size(); counter++) {
				extensions[counter] = ((TypeListItem) listData.elementAt(counter)).getExtension();
			}
			prefs.setStrings("fileExtensions", extensions);

			// Rename the field.
			try {
				prefs.renameField(originalExtension, txtExtension.getText().trim());
			} catch (FieldNotFoundException fnfe) {
			}
			;
			prefs.setString(txtExtension.getText().trim(), txtMimetype.getText().trim());

			// Set the new extension as the previously selected one. :-) We're all done with the update!
			originalExtension = txtExtension.getText().trim();

		} else {
			// Remove the data!

			// Remove the field from the prefs.
			prefs.removeField(((TypeListItem) listData.elementAt(typeList.getSelectedIndex())).getExtension());

			// Remove the entry from the visible list.
			listData.removeElementAt(typeList.getSelectedIndex());

			// Rebuild the key index array. Minus this key.
			String[] extensions = new String[listData.size()];

			for (int counter = 0; counter < listData.size(); counter++) {
				extensions[counter] = ((TypeListItem) listData.elementAt(counter)).getExtension();
			}

			prefs.setStrings("fileExtensions", extensions);
		}
	}

	/**
	 * Data holder for extensions and mime types.
	 */
	private class TypeListItem {
		String extension;
		String mimetype;

		/**
		 * Oh lala the constructor!
		 */
		public TypeListItem(String extension, String mimetype) {
			this.extension = extension;
			this.mimetype = mimetype;
		}

		public String getExtension() {
			return this.extension;
		}

		public void setExtension(String extension) {
			this.extension = extension;
		}

		public String getMimeType() {
			return this.mimetype;
		}

		public void setMimeType(String mimetype) {
			this.mimetype = mimetype;
		}
	}

	/**
	 * The List renderer - specifically made to render TypeListItems
	 */
	private class FileTypeRenderer extends DefaultListCellRenderer {
		String extension;
		String mimetype;

		/**
		 * creates and sets the default size of my renderer.
		 */
		public FileTypeRenderer() {
			super();

			setText(" ");
			// Set the minimum size!
			setMinimumSize(new Dimension(350, 1));
			Dimension d = getPreferredSize();
			d.width = 350;
			setPreferredSize(d);
		}

		public Component getListCellRendererComponent(JList list,
		                                              Object value,
		                                              int index,
		                                              boolean isSelected,
		                                              boolean cellHasFocus) {
			super.getListCellRendererComponent(list, new String(""), index, isSelected, cellHasFocus);

			extension = ((TypeListItem) value).getExtension();
			mimetype = ((TypeListItem) value).getMimeType();

			return this;
		}

		protected void paintComponent(Graphics g) {
			FontMetrics fm = g.getFontMetrics();

			String label = extension;
			for (int typeoffset = fm.stringWidth(extension); typeoffset <= 120; typeoffset += fm.charWidth(' ')) {
				label += " ";
			}
			label += mimetype;

			this.setText(label);

			super.paintComponent(g);
		}
	}

	private class ListListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent lse) {
			originalExtension = ((TypeListItem) listData.elementAt(typeList.getSelectedIndex())).getExtension();
			txtExtension.setText(originalExtension);
			txtMimetype.setText(((TypeListItem) listData.elementAt(typeList.getSelectedIndex())).getMimeType());
		}
	}
}
