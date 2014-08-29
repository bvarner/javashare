package blv.swing;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * JFontChooser.java - An implementation of a common Dialog for selecting a font.
 *
 * @author Bryan Varner
 */
public class JFontChooser extends JDialog implements ActionListener, ListSelectionListener {
	Font currentFont;

	JPanel mainPanel;
	JPanel stylePanel;
	JPanel stylePreviewParent;
	JPanel buttonPanel;
	JPanel listPanel;

	JList<String> lstFams;
	JLabel lblPreview;
	JCheckBox chkBold;
	JCheckBox chkItalic;
	JComboBox cmboSize;

	JButton btnCancel;
	JButton btnOk;

	boolean useStyles;

	/**
	 * Creates a default modal FontChooser with the title of "Font" and centered in the parents view.
	 */
	public JFontChooser(Frame parent) {
		this(parent, "Font", true, false);
	}

	/**
	 * Creates a default modal FontChooser with the title of "Font' and centered in the parents view.
	 * Also allows the use of styleized font lists.
	 */
	public JFontChooser(Frame parent, boolean styleizedList) {
		this(parent, "Font", true, styleizedList);
	}

	/**
	 * Creates a FontChooser with specified parent frame, title, and a modal specifier.
	 */
	public JFontChooser(Frame parent, String title, boolean modal, boolean styleizedList) {
		super(parent, title, modal);
		useStyles = styleizedList;

		mainPanel = (JPanel) this.getContentPane();
		mainPanel.setLayout(new BorderLayout());

		GraphicsEnvironment currentGE = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fontFams = currentGE.getAvailableFontFamilyNames();

		lstFams = new JList<>(fontFams);
		if (useStyles) {
			lstFams.setCellRenderer(new DefaultListCellRenderer() {
				public Component getListCellRendererComponent(JList list, Object value,
				                                              int index, boolean isSelected,
				                                              boolean cellHasFocus) {
					Component comp = super.getListCellRendererComponent(list,
							                                                   value, index, isSelected, cellHasFocus);
					comp.setFont(new Font(value.toString(), Font.PLAIN, 12));
					return comp;
				}
			});
		}

		lstFams.setSelectedIndex(0);
		lstFams.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lstFams.addListSelectionListener(this);
		JScrollPane lstFamsScroller = new JScrollPane(lstFams);
		listPanel = new JPanel(new GridLayout(1, 1));
		listPanel.add(lstFamsScroller);
		listPanel.setBorder(BorderFactory.createTitledBorder("Font Name"));

		stylePanel = new JPanel(new GridLayout(3, 1, 1, 1));
		stylePanel.setBorder(BorderFactory.createTitledBorder("Size & Style"));

		String[] txtSizes = {"9", "10", "12", "14", "16", "18", "24"};
		cmboSize = new JComboBox(txtSizes);
		cmboSize.setActionCommand("sizeChange");
		cmboSize.addActionListener(this);

		chkBold = new JCheckBox("Bold");
		chkBold.setActionCommand("bold");
		chkBold.addActionListener(this);

		chkItalic = new JCheckBox("Italic");
		chkItalic.setActionCommand("italic");
		chkItalic.addActionListener(this);

		stylePanel.add(cmboSize);
		stylePanel.add(chkBold);
		stylePanel.add(chkItalic);

		currentFont = new Font((String) lstFams.getSelectedValue(), Font.PLAIN, 9);

		lblPreview = new JLabel("The Quick Brown Fox Jumped...");
		lblPreview.setBorder(BorderFactory.createTitledBorder("Preview"));
		lblPreview.setFont(currentFont);

		stylePreviewParent = new JPanel(new BorderLayout());
		stylePreviewParent.add(stylePanel, BorderLayout.NORTH);
		stylePreviewParent.add(lblPreview, BorderLayout.SOUTH);

		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(this);

		btnOk = new JButton("OK");
		btnOk.addActionListener(this);

		buttonPanel = new JPanel(new BorderLayout());
		JPanel miniButtonPanel = new JPanel();
		miniButtonPanel.add(btnCancel);
		miniButtonPanel.add(btnOk);
		buttonPanel.add(miniButtonPanel, BorderLayout.EAST);

		mainPanel.add(listPanel, BorderLayout.WEST);
		mainPanel.add(stylePreviewParent, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		this.getRootPane().setDefaultButton(btnOk);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				currentFont = null;
			}
		});

		this.pack();
		// Try to center the dialog in the parent. If something goes wrong there, we center it in the
		// Screen.
		if (parent != null) {
			Rectangle parentBounds = parent.getBounds();
			Rectangle meBounds = this.getBounds();
			this.setBounds(parentBounds.x + ((parentBounds.width - meBounds.width) / 2)
					              , parentBounds.y + ((parentBounds.height - meBounds.height) / 2)
					              , meBounds.width
					              , meBounds.height);
		} else {
			GraphicsEnvironment systemGE = GraphicsEnvironment.getLocalGraphicsEnvironment();
			Rectangle screenRect = systemGE.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
			this.setBounds((screenRect.width / 2) - (this.getBounds().width / 2),
					              (screenRect.height / 2) - (this.getBounds().height / 2),
					              this.getBounds().width, this.getBounds().height);
		}
		this.setResizable(false);
	}

	/**
	 * Displays the Modal Font chooser.
	 *
	 * @return null if the dialog is canceled, otherwise, the the Font that was selected.
	 */
	public Font showFontDialog() {
		this.setVisible(true);

		return currentFont;
	}

	/**
	 * Displays the Modal Font chooser with <code>cFont</code> as the selected
	 * font.
	 *
	 * @return null if the dialog is canceled, otherwise, the the Font that was selected.
	 */
	public Font showFontDialog(Font cFont) {
		// Set the styles
		chkBold.setSelected((cFont.getStyle() == Font.BOLD) ||
				                    (cFont.getStyle() == (Font.BOLD | Font.ITALIC)));
		chkItalic.setSelected((cFont.getStyle() == Font.ITALIC) ||
				                      (cFont.getStyle() == (Font.BOLD | Font.ITALIC)));

		// Set the family name
		for (int x = 0; x < lstFams.getModel().getSize(); x++) {
			if (((String) lstFams.getModel().getElementAt(x)).toUpperCase().equals(
					                                                                      cFont.getFamily().toUpperCase())) {
				lstFams.setSelectedIndex(x);
			}
		}

		// Set the size.
		Integer sizeInt = new Integer(cFont.getSize());
		cmboSize.setSelectedItem(sizeInt.toString());

		return showFontDialog();
	}

	/**
	 * Implements the action Listener
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnOk) {
			this.dispose();
		} else if (e.getSource() == btnCancel) {
			currentFont = null;
			this.dispose();
		} else {
			displayPreview();
		}
	}

	/**
	 * Implements the List Selection Listener
	 */
	public void valueChanged(ListSelectionEvent e) {
		displayPreview();
	}

	private void displayPreview() {
		int style = Font.PLAIN;
		if (chkBold.isSelected()) {
			style = Font.BOLD;
		}
		if (chkItalic.isSelected()) {
			style = Font.ITALIC;
		}
		if (chkBold.isSelected() && (chkBold.isSelected() == chkItalic.isSelected())) {
			style = Font.BOLD | Font.ITALIC;
		}
		currentFont = new Font((String) lstFams.getSelectedValue()
				                      , style
				                      , Integer.parseInt((String) cmboSize.getSelectedItem()));
		lblPreview.setFont(currentFont);
	}
}
