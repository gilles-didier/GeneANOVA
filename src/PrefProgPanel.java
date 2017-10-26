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
import javax.swing.table.*;

/**
 * <CODE>PrefProgPanel</CODE> is panel witch contains all image tables.
 *
 * @version 10 mar 2000
 * @author	Gilles Didier
 */
public class PrefProgPanel extends JPanel implements PreferencesAsker {

	private String progName, progPath;
	private static String nameLabel, pathLabel;
	FileField resPath;
	JTextField nameField;
	static {
		try {
			ResourceBundle resources= ResourceBundle.getBundle("resources.PrefProgPanel", Locale.getDefault());
			nameLabel = resources.getString("nameLabel");
			pathLabel = resources.getString("pathLabel");
		} catch (MissingResourceException mre) {
			nameLabel = "Name of the command";
			pathLabel = "Program path";
		}
	}

	public PrefProgPanel() {
		this(0, null);
	}


	public PrefProgPanel(int num, Preferences pref) {
		super();
		init(Integer.toString(num), pref);
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gridbag);
		c.fill = GridBagConstraints.NONE; 
		JLabel labTmp;

		labTmp = new JLabel(nameLabel);
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.ipadx = 10;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.EAST;
		gridbag.setConstraints(labTmp, c);
		add(labTmp);
		nameField = new JTextField(progName, Const.colArg);
		c.weightx = 0.5;
		c.gridx = 1;
		c.ipadx = 0;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		gridbag.setConstraints(nameField, c);
		add(nameField);

		labTmp = new JLabel(pathLabel);
		c.weightx = 0.5;
		c.gridx = 0;
		++c.gridy;
		c.gridwidth = 1;
		c.ipadx = 10;
		c.anchor = GridBagConstraints.EAST;
		gridbag.setConstraints(labTmp, c);
		add(labTmp);
		resPath = new FileField(progPath, Const.colPath);
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridwidth = 1;
		c.ipadx = 0;
		c.anchor = GridBagConstraints.WEST;
		gridbag.setConstraints(resPath, c);
		add(resPath);
	}


	public void init(Preferences p) {
		init(Integer.toString(0), p);
	}

	public void set(String name, Preferences pref) {
		if(pref == null)
			return;
		pref.put(Const.progNameLabel+name, nameField.getText());
		pref.put(Const.progPathLabel+name, resPath.getText());
	}

	public void adjust() {
		resPath.setText(progPath);
	}

	
	public void init(String num, Preferences pref) {
		String tmp;
		if(pref != null && (tmp = pref.get(Const.progNameLabel+num)) != null)
			progName = tmp;
		else
			progName = new String();
		if(pref != null && (tmp = pref.get(Const.progPathLabel+num)) != null)
			progPath = tmp;
		else
			progPath = new String();
	}

	boolean isOk() {
		return true;
	}
}
		