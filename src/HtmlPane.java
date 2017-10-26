 import javax.swing.*; 
 import javax.swing.event.*; 
 import javax.swing.text.*; 
 import javax.swing.text.html.*; 
 import javax.swing.border.*; 
 import javax.accessibility.*; 
  
 import java.awt.*; 
 import java.awt.event.*; 
 import java.beans.*; 
 import java.util.*; 
 import java.io.*; 
 import java.applet.*; 
 import java.net.*; 
 
 class HtmlPane extends JPanel {
	private JEditorPane html;
	
	public HtmlPane(String urlS) {
		super();
		setLayout(new BorderLayout()); 
		try { 
			URL url = new URL(urlS);
			if(url != null) { 
				html = new JEditorPane(url); 
				html.setEditable(false); 
				html.addHyperlinkListener(createHyperLinkListener()); 
 		 
				JScrollPane scroller = new JScrollPane(); 
				JViewport vp = scroller.getViewport(); 
				vp.add(html); 
				add(scroller, BorderLayout.CENTER); 
			} 
		} catch (MalformedURLException e) { 
			System.out.println("Malformed URL: " + e);
		} catch (IOException e) { 
			System.out.println("IOException: " + e); 
		} 
	}

	public HtmlPane(URL url) { 
		super();
		setLayout(new BorderLayout()); 
		try { 
			if(url != null) { 
				html = new JEditorPane(url); 
				html.setEditable(false); 
				html.addHyperlinkListener(createHyperLinkListener()); 
 		 
				JScrollPane scroller = new JScrollPane(); 
				JViewport vp = scroller.getViewport(); 
				vp.add(html); 
				add(scroller, BorderLayout.CENTER); 
			} 
		} catch (IOException e) { 
			System.out.println("IOException: " + e); 
		} 
	} 
  
	public HyperlinkListener createHyperLinkListener() { 
		return new HyperlinkListener() { 
			public void hyperlinkUpdate(HyperlinkEvent e) { 
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) { 
					if (e instanceof HTMLFrameHyperlinkEvent) { 
						((HTMLDocument)html.getDocument()).processHTMLFrameHyperlinkEvent((HTMLFrameHyperlinkEvent)e); 
					} else { 
						try { 
							html.setPage(e.getURL()); 
						} catch (IOException ioe) { 
							System.out.println("IOE: " + ioe); 
						} 
					} 
				} 
			} 
		}; 
	}
      
} 
