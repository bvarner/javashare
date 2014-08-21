package org.beShare.gui;

import org.beShare.gui.prefPanels.GeneralPrefs;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

/**
 * PrefsFrame - A Preference Frame. This is basically a dummy class, used to create an interface.
 * It registers the various prefsPanels with the PrefsListener, and allows a user to
 * cleanly switch between panels. That's it. No more, no less. We now let the panels do
 * all the hard work.
 */
public class PrefsFrame extends JDialog {
	JPanel mainPanel;

	JPanel prefHolder;
	CardLayout prefStack;

	JScrollPane listScroll;
	JList categoryList;

	// Create a card layout, and panels matching the above strings.
	// Create objects on those panels, and register the listener to change the value in the hashtable.

	public PrefsFrame(final JFrame owner) {
		super(owner, "JavaShare Preferences");

		ImageIcon JavaShareIcon = new ImageIcon(getClass().getClassLoader().getResource("Images/BeShare.gif"));
		this.setIconImage(JavaShareIcon.getImage());

		JPanel buttonPanel = new JPanel(new BorderLayout());
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PrefsFrame.this.dispatchEvent(new WindowEvent(PrefsFrame.this, WindowEvent.WINDOW_CLOSING));
			}
		});
		buttonPanel.add(closeButton, BorderLayout.EAST);
		this.getRootPane().setDefaultButton(closeButton);

		// Determine what to include in the prefs list, and create the list.
		DefaultListModel catList = new DefaultListModel();

		categoryList = new JList(catList);
		categoryList.setPreferredSize(new Dimension(175, 100));
		categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		categoryList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				prefStack.show(prefHolder, categoryList.getSelectedValue().toString());
			}
		});

		JPanel catPanel = new JPanel(new GridLayout(1, 1));
		catPanel.setBorder(BorderFactory.createTitledBorder("Category"));
		catPanel.add(categoryList);

		prefHolder = new JPanel();
		prefStack = new CardLayout();
		prefHolder.setLayout(prefStack);

		mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		mainPanel.add(catPanel, BorderLayout.WEST);
		mainPanel.add(prefHolder, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		this.setContentPane(mainPanel);

		prefHolder.add(new GeneralPrefs(), "General");
//		prefHolder.add(new ConnectionPrefs(), "Connection");
//		prefHolder.add(new DisplayPrefs(), "Display");
//		prefHolder.add(new AppearancePrefs(), "Appearance");
//		prefHolder.add(new SoundPrefs(), "Sounds");
//		prefHolder.add(new SharePrefs(), "File-Transfer");
//		prefHolder.add(new MimePrefs(), "File Types");

		catList.addElement("General");
//		catList.addElement("Connection");
//		catList.addElement("Display");
//		catList.addElement("Appearance");
//		catList.addElement("Sounds");
//		catList.addElement("File-Transfer");
//		catList.addElement("File Types");

		categoryList.setSelectedIndex(0);
		setResizable(true);
	}
}
