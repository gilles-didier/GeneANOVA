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
 * <CODE>PrefProgPanel</CODE> is panel witch contains all image tables.
 *
 * @version 10 mar 2000
 * @author	Gilles Didier
 */
public class PrefImaPanel extends JPanel implements PreferencesAsker {


	private NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
	private int nbProg, nbFeat;
	JButton featBut;
	JTabbedPane tabPanel = new JTabbedPane();
	private Preferences pref;
	private static String addProgramLabel, removeLabel, progTabLabel;
	static {
		try {
			ResourceBundle resources= ResourceBundle.getBundle("resources.PrefImaPanel", Locale.getDefault());
			addProgramLabel = resources.getString("addProgramLabel");
			removeLabel = resources.getString("removeLabel");
			progTabLabel = resources.getString("progTabLabel");
		} catch (MissingResourceException mre) {
			addProgramLabel = "Add program";
			removeLabel = "Remove";
			progTabLabel = "Prog";
		}
	}
	
	public PrefImaPanel() {
		this(null);
	}

	public PrefImaPanel(Preferences pref) {
		init(null, pref);
		setLayout(new BorderLayout());
		if(tabPanel.getTabCount() == 0){
			PrefProgPanel prefProg = new PrefProgPanel();
			if(prefProg.isOk())
				tabPanel.addTab(progTabLabel+0, prefProg);
		}

		add("Center",tabPanel);
		JPanel buttonPanel = new JPanel();
		JButton b;
		b = new JButton(addProgramLabel);
		b.addActionListener(new AddProgAction());
		buttonPanel.add(b);
		b = new JButton(removeLabel);
		b.addActionListener(new RemoveAction());
		buttonPanel.add(b);

		add("South", buttonPanel);
	}

	public void init(String name, Preferences pref) {
		tabPanel.removeAll();
		if(pref == null)
			return;
		String tmp;
		if((tmp = pref.get(Const.nbProgLabel)) != null) {
			try {
				nbProg = nf.parse(tmp).intValue();
			}  catch (ParseException e) {
				nbProg =0;
	   		}
		}
		for(int i=0; i<nbProg; i++) {
			PrefProgPanel prefProg = new PrefProgPanel(i, pref);
			if(prefProg.isOk())
				tabPanel.addTab(progTabLabel+i, prefProg);
		}
	}

	public void set(String name, Preferences pref) {
		int indP = 0;
		
		for(int i=0; i<tabPanel.getTabCount(); i++)
			((PreferencesAsker) tabPanel.getComponentAt(i)).set(nf.format(indP++), pref);
		pref.put(Const.nbProgLabel, nf.format(indP));

	}
		
	class AddProgAction extends AbstractAction {
 		AddProgAction() {
 			super("AddProgAction");
		}

		public void actionPerformed(ActionEvent e) {
			tabPanel.addTab(progTabLabel+(tabPanel.getTabCount()), null, new PrefProgPanel(), null);
			tabPanel.setSelectedIndex(tabPanel.getTabCount()-1);
		}
	}

	class RemoveAction extends AbstractAction {
 		RemoveAction () {
 			super("RemoveAction ");
		}

		public void actionPerformed(ActionEvent e) {
			int i;
			int indP = 0, indF = 0, sel = tabPanel.getSelectedIndex();
			if(sel>=0) {
				tabPanel.removeTabAt(sel);
				for(i=0; i<tabPanel.getTabCount(); i++)
					tabPanel.setTitleAt(i, progTabLabel+indP++);
			}
		}
	}
}
		