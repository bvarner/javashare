/* Change-Log
	1.0 - 6.5.2002 - Initial Class creation.
	1.0.1 - 6.6.2002 - Fixed bug in Font button action listener, cancel was applying the default font.
	2.0 a6 - 1.5.2003 - Now only displays the Supported Look And Feels for the current system.
*/
package org.beShare.gui.prefPanels;

import blv.swing.JFontChooser;
import com.meyer.muscle.message.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Appearances Preferences panel.
 */
public class AppearancePrefs extends PreferencePanel implements ActionListener {
	JavaSharePrefListener 	target;
	Message					prefs;
	
	JFontChooser			fontPicker;
	Font					currentFont = null;
	JButton					btnFont;
	JComboBox				cmboLaf;
	
	public AppearancePrefs(JavaSharePrefListener prefHandler, Message prefMessage){
		super();

		target = prefHandler;
		prefs = prefMessage;
		
		cmboLaf = new JComboBox();
		
		UIManager.LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
		for (int x = 0; x < lafs.length; x++){
			try {
				if (((LookAndFeel)Class.forName(lafs[x].getClassName()).newInstance()).isSupportedLookAndFeel()) {
					cmboLaf.addItem(new LafHolder(lafs[x].getName(), lafs[x].getClassName()));
					if (lafs[x].getName().equals(UIManager.getLookAndFeel().getName())){
						cmboLaf.setSelectedIndex(x);
					}
				}
			} catch (Exception e) {
			}
		}
		
		btnFont = new JButton("Set Display Font");
		
		fontPicker = new JFontChooser(null, true);
		
		setBorder(BorderFactory.createTitledBorder("Display Preferences"));
		
		JPanel lafHolder = new JPanel(new BorderLayout());
		lafHolder.add(new JLabel("Look And Feel: "), BorderLayout.WEST);
		lafHolder.add(cmboLaf, BorderLayout.CENTER);
		
		firstComponent();
		add(btnFont);
		//add(lafHolder);
		add(new JLabel("Look And Feel: "));
		add(cmboLaf);
		lastComponent();
		
		if (prefs.hasField("fontName")){
			try {
				currentFont = (new Font(prefs.getString("fontName"),
										prefs.getInt("fontStyle"),
										prefs.getInt("fontSize")));
			} catch (Exception e){
			}
		}
		
		
		btnFont.addActionListener(this);
		cmboLaf.addActionListener(this);
	}
	
	public void actionPerformed(ActionEvent ae){
		if (ae.getSource() == cmboLaf){
			target.lafChange(((LafHolder)cmboLaf.getSelectedItem()).getClassName());
		} else if (ae.getSource() == btnFont){
			if (currentFont == null){
				currentFont = new Font("Monospaced", Font.PLAIN, 12);
			}
			Font tempFont = fontPicker.showFontDialog(currentFont);
			if (tempFont != null){
				currentFont = tempFont;
				target.updateChatFont(tempFont);
			}
		}
	}
	
	private class LafHolder {
		String name;
		String classname;
		
		public LafHolder(String lafName, String lafClassName){
			name = lafName;
			classname = lafClassName;
		}
		
		public String getClassName(){
			return classname;
		}
		
		public String toString(){
			return name;
		}
	}
	
	public void updateUI(){
		if (fontPicker != null)
			SwingUtilities.updateComponentTreeUI(fontPicker);
		super.updateUI();
	}
}
