import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.beans.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.lang.*;
import java.text.*;
import java.lang.Character;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;

/**
 * <CODE>PreferencesPanel</CODE> is panel witch contains all image tables.
 *
 * @version 10 mar 2000
 * @author	Gilles Didier
 */
public class PreferencesPanel extends JPanel implements PreferencesAsker {
	JTabbedPane tabPanel = new JTabbedPane();
	Hashtable table;
	Dimension dim = new Dimension(600,200);

	public PreferencesPanel() {
		this(null, null);
	}
	public PreferencesPanel(Hashtable t) {
		this(null, t);
	}

	public PreferencesPanel(Preferences pref, Hashtable t) {
		table = t;
		init(null, pref);
		if(table == null || table.size() == 0)
			return;
		if(table.size() > 1) {
			for (Enumeration e = table.keys() ; e.hasMoreElements() ;) {
				String s = (String) e.nextElement();
				if(s != null && table.get(s)!=null)
					tabPanel.addTab(s, (JPanel) table.get(s));
			}
			tabPanel.setMinimumSize(dim);
			tabPanel.setPreferredSize(dim);
			add(tabPanel);
		} else {
				Enumeration e = table.keys();
				String s = (String) e.nextElement();
				if(s != null && table.get(s)!=null)
					add((JPanel) table.get(s));
		}		
	}
	
	public void init(String name, Preferences pref) {
		if(pref == null || table == null)
			return;
		for (Enumeration e = table.elements() ; e.hasMoreElements() ;) {
			((PreferencesAsker) e.nextElement()).init(name, pref);
		}
	}

	public void set(String name, Preferences pref) {
		if(pref == null || table == null)
			return;
		for (Enumeration e = table.elements() ; e.hasMoreElements() ;) {
			((PreferencesAsker) e.nextElement()).set(name, pref);
		}
	}
}
		