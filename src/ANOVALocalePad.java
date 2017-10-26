/*
 * @(#)ANOVALocalePad.java	1.13 98/08/28
 *
 * Copyright 1997, 1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.net.URL;
import java.util.*;

import javax.swing.text.*;
import javax.swing.undo.*;
import javax.swing.event.*;
import javax.swing.*;
import javax.swing.table.*;
/**
 * Sample application using the simple text editor component that
 * supports only one font.
 *
 * @author  Timothy Prinzing
 * @version 1.13 08/28/98
 */
class ANOVALocalePad extends StandardPad {

	private ANOVACompute.ANOVALocaleTableModel model;
	private JTable table;
	private JScrollPane scrollPane;
	private static final int fractionDigits = 2;
	private static final int fractionDigitsP = 5;

	public ANOVALocalePad(ANOVACompute.ANOVALocaleTableModel m) {
		super();
		setBorder(BorderFactory.createEtchedBorder());
		model = m;
		finishCreate();
		table = new JTable(m);
		table.setIntercellSpacing(new Dimension(3,1));
		TableCellRenderer cellRenderer = new NumberCellRenderer(0, fractionDigitsP, fractionDigits);
		table.setDefaultRenderer(Integer.class, cellRenderer);
		table.setDefaultRenderer(Double.class, cellRenderer);
		table.setDefaultRenderer(Float.class, cellRenderer);
		scrollPane = new JScrollPane(table);
		add("Center", scrollPane);
	}

	public void setModel(ANOVACompute.ANOVALocaleTableModel tm) {
		model = tm;
		table.setModel(model);
		table.sizeColumnsToFit(-1);
	}

	public TableModel getModel() {
		return model;
	}
	
	public JTable getTable() {
		return table;
	}

	public void resetModel() {
		model = null;
		table.setModel(new DefaultTableModel());
	}	


	
	protected JMenu createSortMenu() {
		JMenu sortMenu = new JMenu(getResourceString("sortLabel"));
		Action[] tabSort = model.getActions();
		if(tabSort != null) {
			for (int i=0; i< tabSort.length; i++) {
				sortMenu.add(new JMenuItem(tabSort[i]));
			}
		}
		return sortMenu;
	}


 	protected JMenuBar createMenubar() {
		JMenuBar mb = super.createMenubar();
		mb.add(createSortMenu());
		return mb;
	}

	public Action[] getActions() {
		return defaultActions;
	}
	
	public String getResourceName() {
		return "resources.ANOVALocalePad";
	}

    public static final String saveAction = "save";
    public static final String closeAction = "close";
 

    // --- action implementations -----------------------------------
    private Action[] defaultActions = {
		new SaveAction(),
		new CloseAction()
    };


 	class SaveAction extends AbstractAction {
		SaveAction() {
			super(saveAction);
		}
		public void actionPerformed(ActionEvent e) {
			File f = GeneralUtil.getFileSaveAs(ANOVALocalePad.this);
			if(f == null)
				return;
			if (f.exists()) {
				try {
					BufferedWriter out = new BufferedWriter(new FileWriter(f));
					MyTableUtil.writeJTable(out, table);
					out.close();
				} catch(IOException ioe) {
					GeneralUtil.showError(ANOVALocalePad.this, "IO error : \n"+ioe);
				}
			}
		}		
	}

    /**
     * Really lame implementation of an exit command
     */
	class CloseAction extends AbstractAction {

		CloseAction() {
			super(closeAction);
		}

		public void actionPerformed(ActionEvent e) {
			getFrame().dispose();
		}
	}
}
