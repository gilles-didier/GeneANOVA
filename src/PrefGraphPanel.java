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
 * <CODE>PrefGraphPanel</CODE> is panel witch contains all image tables.
 *
 * @version 10 mar 2000
 * @author	Gilles Didier
 */
public class PrefGraphPanel extends JPanel implements PreferencesAsker {

	private static String noLabel, sel1Label, sel2Label, tableLabel, lineLabel, areaLabel, markLabel;
	private ColorTable colorTable, colorSpecial;
	private ColorButton noButton, sel1Button, sel2Button, lineButton, areaButton, markButton;
	private static final int SIZE_BUTTON = 20;
	private JPanel panel;
	static {
		try {
			ResourceBundle resources= ResourceBundle.getBundle("resources.PrefGraphPanel", Locale.getDefault());
			sel1Label = resources.getString("sel1Label");
			sel2Label = resources.getString("sel2Label");
			lineLabel = resources.getString("lineLabel");
			areaLabel = resources.getString("areaLabel");
			markLabel = resources.getString("markLabel");
			tableLabel = resources.getString("tableLabel");
		} catch (MissingResourceException mre) {
			sel1Label = "Selection 1 color : ";
			sel2Label = "Selection 2 color : ";
			lineLabel = "Line color : ";
			areaLabel = "Selection area color : ";
			markLabel = "Mark color : ";
			tableLabel = "Menu colors";
		}
	}

	public PrefGraphPanel() {
		this(0, null);
	}


	public PrefGraphPanel(int num, Preferences pref) {
		super();
		init(pref);		
	}

	public void define() {
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gridbag);
		c.fill = GridBagConstraints.NONE; 
		JLabel labTmp;

		labTmp = new JLabel(sel1Label);
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 1;
		c.ipadx = 10;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.EAST;
		gridbag.setConstraints(labTmp, c);
		add(labTmp);
		sel1Button = new ColorButton(colorSpecial.getColor(0), SIZE_BUTTON);
		c.weightx = 0.5;
		c.gridx = 1;
		c.ipadx = 0;
		c.anchor = GridBagConstraints.WEST;
		gridbag.setConstraints(sel1Button, c);
		add(sel1Button);
		
		labTmp = new JLabel(sel2Label);
		c.gridx = 0;
		++c.gridy;
		c.ipadx = 10;
		c.anchor = GridBagConstraints.EAST;
		gridbag.setConstraints(labTmp, c);
		add(labTmp);
		sel2Button = new ColorButton(colorSpecial.getColor(1), SIZE_BUTTON);
		c.gridx = 1;
		c.ipadx = 0;
		c.anchor = GridBagConstraints.WEST;
		gridbag.setConstraints(sel2Button, c);
		add(sel2Button);
		
		labTmp = new JLabel(lineLabel);
		c.gridx = 0;
		++c.gridy;
		c.ipadx = 10;
		c.anchor = GridBagConstraints.EAST;
		gridbag.setConstraints(labTmp, c);
		add(labTmp);
		lineButton = new ColorButton(colorSpecial.getColor(2), SIZE_BUTTON);
		c.gridx = 1;
		c.ipadx = 0;
		c.anchor = GridBagConstraints.WEST;
		gridbag.setConstraints(lineButton, c);
		add(lineButton);

		labTmp = new JLabel(areaLabel);
		c.gridx = 0;
		++c.gridy;
		c.ipadx = 10;
		c.anchor = GridBagConstraints.EAST;
		gridbag.setConstraints(labTmp, c);
		add(labTmp);
		areaButton = new ColorButton(colorSpecial.getColor(3), SIZE_BUTTON);
		c.gridx = 1;
		c.ipadx = 0;
		c.anchor = GridBagConstraints.WEST;
		gridbag.setConstraints(areaButton, c);
		add(areaButton);

		labTmp = new JLabel(markLabel);
		c.gridx = 0;
		++c.gridy;
		c.ipadx = 10;
		c.anchor = GridBagConstraints.EAST;
		gridbag.setConstraints(labTmp, c);
		add(labTmp);
		markButton = new ColorButton(colorSpecial.getColor(4), SIZE_BUTTON);
		c.gridx = 1;
		c.ipadx = 0;
		c.anchor = GridBagConstraints.WEST;
		gridbag.setConstraints(markButton, c);
		add(markButton);

		
		labTmp = new JLabel(tableLabel);
		c.gridx = 2;
		c.gridy = 0;
		c.ipadx = 10;
		c.anchor = GridBagConstraints.CENTER;
		gridbag.setConstraints(labTmp, c);
		add(labTmp);
		JPanel panel = colorTable.getEditor();
		c.gridx = 2;
		c.gridy = 1;
		c.gridheight = 5;
		c.ipadx = 0;
		c.anchor = GridBagConstraints.CENTER;
		gridbag.setConstraints(panel, c);
		add(panel);
	}

	public void set(String name, Preferences pref) {
		if(pref == null)
			return;
		colorSpecial.setColor(0, sel1Button.getColor());
		colorSpecial.setColor(1, sel2Button.getColor());
		colorSpecial.setColor(2, lineButton.getColor());
		colorSpecial.setColor(3, areaButton.getColor());
		colorSpecial.setColor(4, markButton.getColor());
		pref.put(Const.special, colorSpecial.toString());
		pref.put(Const.table, colorTable.toString());
	}

	public void adjust() {
	}

	public void init(Preferences p) {
		init("", p);
	}
	
	public void init(String num, Preferences pref) {
		String tmp = null;
		if(pref != null && (tmp = pref.get(Const.special)) != null) {
			colorSpecial = new ColorTable(tmp);
		} else {
			colorSpecial = new ColorTable(5);
		}
		if(pref != null && (tmp = pref.get(Const.table)) != null) {
			colorTable = new ColorTable(tmp);
		} else {
			colorTable = new ColorTable(12);
		}
		removeAll();
		define();
	}

	boolean isOk() {
		return true;
	}
}
		