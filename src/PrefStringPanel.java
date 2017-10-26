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
 * <CODE>PrefHTMLPanel</CODE> is panel witch contains all image tables.
 *
 * @version 10 mar 2000
 * @author	Gilles Didier
 */
public class PrefStringPanel extends JPanel implements PreferencesAsker {
	JTextField stringField = new JTextField(30);
	String ident;

	public PrefStringPanel(Preferences pref, String label, String id) {
		init(null, pref);
		ident = id;
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE; 
		setLayout(gridbag);
		JLabel labTmp = new JLabel(label);
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.ipadx = 10;
		c.anchor = GridBagConstraints.EAST;
		gridbag.setConstraints(labTmp, c);
		add(labTmp);
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 2;
		c.ipadx = 0;
		c.anchor = GridBagConstraints.WEST;
		gridbag.setConstraints(stringField, c);
		add(stringField);
	}

	public PrefStringPanel(String l, String i) {
		this(null, l, i);
	}

	public void init(String name, Preferences pref) {
		String tmp;
		if(pref != null && (tmp = pref.get(ident)) != null)
			stringField.setText(tmp);
		else
			stringField.setText("");
	}
	
	public void set(String name, Preferences pref) {
		if(pref != null)
			pref.put(ident, stringField.getText());
	}
}
		