package org.beShare.gui;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpLinkViewer extends JFrame implements HyperlinkListener{
	JPanel			contentPanel;
	
	JScrollPane		htmlScroll;
	JEditorPane		htmlView;
	
	public HttpLinkViewer(String baseDoc){
		super("JavaShare Mini-Browser: " + baseDoc);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		try {
			htmlView = new JEditorPane(new URL(baseDoc));
			htmlView.setEditable(false);
			htmlView.addHyperlinkListener(this);
			
			contentPanel = (JPanel)this.getContentPane();
			contentPanel.setLayout(new BorderLayout());
			
			htmlScroll = new JScrollPane(htmlView
					, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
					, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			
			contentPanel.add(htmlScroll, BorderLayout.CENTER);
		} catch(MalformedURLException mue){
			JOptionPane.showMessageDialog(null,
				"An Error occured while opening the URL. THis page cannot be displayed.",
				"Mini-Browser Error", JOptionPane.ERROR_MESSAGE);
		} catch(IOException ioe){
			JOptionPane.showMessageDialog(null,
				"An Error has occured while trying to read this page.",
				"Mini-Browser Error", JOptionPane.ERROR_MESSAGE);
			this.dispose();
		} catch (StringIndexOutOfBoundsException sioobe){
			JOptionPane.showMessageDialog(null,
				"An Error has occured while trying to read this page.",
				"Mini-Browser Error", JOptionPane.ERROR_MESSAGE);
			this.dispose();
		}
		pack();
		
		// Center me.
		try{
			GraphicsEnvironment systemGE
				= GraphicsEnvironment.getLocalGraphicsEnvironment();
				Rectangle screenRect = systemGE.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
				this.setBounds((screenRect.width / 2) - (this.getBounds().width / 2),
						(screenRect.height / 2) - (this.getBounds().height / 2),
						this.getBounds().width, this.getBounds().height);
		} catch (NoClassDefFoundError ncdfe){
		}
		show();
	}
	
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			if (e instanceof HTMLFrameHyperlinkEvent) {
			((HTMLDocument)htmlView.getDocument()).processHTMLFrameHyperlinkEvent(
					(HTMLFrameHyperlinkEvent)e);
			} else {
				try {
					htmlView.setPage(e.getURL());
					this.setTitle("JavaShare Mini-Browser: " +
									e.getURL().toString());
				} catch (IOException ioe) {
					JOptionPane.showMessageDialog(this,
						"An Error has occured while trying to read this page.",
						"Mini-Browser Error", JOptionPane.ERROR_MESSAGE);
					this.dispose();
				}
			}
		}
	}
	
	public Dimension getPreferredSize(){
		return new Dimension(480, 300);
	}
	
	public static void createHttpLinkViewer(String baseDoc){
		HttpLinkViewer htmlViewerFrame = new HttpLinkViewer(baseDoc);
	}
}
