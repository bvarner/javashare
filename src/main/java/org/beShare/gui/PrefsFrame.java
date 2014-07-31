package org.beShare.gui;

import com.meyer.muscle.message.Message;
import org.beShare.gui.prefPanels.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * PrefsFrame - A Preference Frame. This is basically a dummy class, used to create an interface.
 * 				It registers the various prefsPanels with the PrefsListener, and allows a user to
 *				cleanly switch between panels. That's it. No more, no less. We now let the panels do
 *				all the hard work.
 */
public class PrefsFrame extends JFrame implements ActionListener,
													ListSelectionListener{
	JPanel			mainPanel;
	
	JPanel			prefHolder;
	CardLayout		prefStack;
	
	JScrollPane		listScroll;
	JList			categoryList;
	
	JButton			closeButton;
	
	// Create a card layout, and panels matching the above strings.
	// Create objects on those panels, and register the listener to change the value in the hashtable.
	
	public PrefsFrame(JavaSharePrefListener prefRecipient, Message prefsMessage){
		super("JavaShare 2 Preferences");
		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		ImageIcon JavaShareIcon = AppPanel.loadImage("Images/BeShare.gif", this);
		this.setIconImage(JavaShareIcon.getImage());		
		
		JPanel buttonPanel = new JPanel(new BorderLayout());
		closeButton = new JButton("Close");
		closeButton.addActionListener(this);
		buttonPanel.add(closeButton, BorderLayout.EAST);
		this.getRootPane().setDefaultButton(closeButton);
		
		// Determine what to include in the prefs list, and create the list.
		DefaultListModel catList = new DefaultListModel();
		catList.addElement("General");
		catList.addElement("Connection");
		catList.addElement("Display");
		catList.addElement("Appearance");
		catList.addElement("Sounds");
//		String[] catList = {"General", "Connection", "Display",
//							"Appearance", "Sounds"};
		categoryList = new JList(catList);
		categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		categoryList.addListSelectionListener(this);
		
		JPanel catPanel = new JPanel(new GridLayout(1,1));
		catPanel.setBorder(BorderFactory.createTitledBorder("Category"));
		catPanel.add(categoryList);
		
		prefHolder = new JPanel();
		prefStack = new CardLayout();
		prefHolder.setLayout(prefStack);
		
		mainPanel = new JPanel(new BorderLayout());
		
		mainPanel.add(catPanel, BorderLayout.WEST);
		mainPanel.add(prefHolder, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		this.setContentPane(mainPanel);
		
		prefHolder.add(new GeneralPrefs(prefRecipient, prefsMessage), "General");
		prefHolder.add(new ConnectionPrefs(prefRecipient, prefsMessage), "Connection");
		prefHolder.add(new DisplayPrefs(prefRecipient, prefsMessage), "Display");
		prefHolder.add(new AppearancePrefs(prefRecipient, prefsMessage), "Appearance");
		prefHolder.add(new SoundPrefs(prefRecipient, prefsMessage), "Sounds");
		
		categoryList.setSelectedIndex(0);
		setResizable(false);
		pack();
		try{
			GraphicsEnvironment systemGE
				= GraphicsEnvironment.getLocalGraphicsEnvironment();
				Rectangle screenRect = systemGE.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
				this.setBounds((screenRect.width / 2) - (this.getBounds().width / 2),
						(screenRect.height / 2) - (this.getBounds().height / 2),
						this.getBounds().width, this.getBounds().height);
		} catch (NoClassDefFoundError ncdfe){
			Toolkit tk = Toolkit.getDefaultToolkit();
			Rectangle screenRect = new Rectangle(tk.getScreenSize());
			this.setBounds((screenRect.width / 2) - (this.getBounds().width / 2),
					(screenRect.height / 2) - (this.getBounds().height / 2),
					this.getBounds().width, this.getBounds().height);
		}
	}
	
	public void actionPerformed(ActionEvent ae){
		if (ae.getSource() == closeButton){
			this.dispose();
		}
	}
	
	public void valueChanged(ListSelectionEvent lse){
		if(lse.getSource() == categoryList){
			prefStack.show(prefHolder, (String)categoryList.getSelectedValue());
		}
	}
	
	/**
	 * Adds the file-transfer Preferences to the list, and creates the pref panels.
	 */
	public void addFileTransferPrefs(Message prefsMessage, SharePrefsListener tranferPrefs){
		((DefaultListModel)categoryList.getModel()).addElement("File-Transfer");
		((DefaultListModel)categoryList.getModel()).addElement("File Types");
		prefHolder.add(new SharePrefs(tranferPrefs, prefsMessage), "File-Transfer");
		prefHolder.add(new MimePrefs(prefsMessage), "File Types");
		this.pack();
	}
}
