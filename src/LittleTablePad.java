/*
 * @(#)ANOVAPad.java	1.13 98/08/28
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
class LittleTablePad extends StandardPad {

	private TableModel model;
	private JTable table;
	private JScrollPane scrollPane;
	private static final int fractionDigits = 2;
	private static final int fractionDigitsP = 5;

	public LittleTablePad(TableModel m) {
		super();
		setBorder(BorderFactory.createEtchedBorder());
		finishCreate();
		model = m;
		table = new JTable(m);
		table.setIntercellSpacing(new Dimension(3,1));
		TableCellRenderer cellRenderer = new NumberCellRenderer(0, fractionDigitsP, fractionDigits);
		table.setDefaultRenderer(Integer.class, cellRenderer);
		table.setDefaultRenderer(Double.class, cellRenderer);
		table.setDefaultRenderer(Float.class, cellRenderer);
		scrollPane = new JScrollPane(table);
		add("Center", scrollPane);
	}
	public LittleTablePad(JTable t) {
		super();
		setBorder(BorderFactory.createEtchedBorder());
		finishCreate();
		table = t;
		model = table.getModel();
		table.setIntercellSpacing(new Dimension(3,1));
/*		TableCellRenderer cellRenderer = new NumberCellRenderer(0, fractionDigitsP, fractionDigits);
		table.setDefaultRenderer(Integer.class, cellRenderer);
		table.setDefaultRenderer(Double.class, cellRenderer);
		table.setDefaultRenderer(Float.class, cellRenderer);
*/		scrollPane = new JScrollPane(table);
		add("Center", scrollPane);
	}

	public void setModel(TableModel tm) {
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
	
	public Action[] getActions() {
		return defaultActions;
	}
	
	public String getResourceName() {
		return "resources.LittleTablePad";
	}
	public static final String saveAction = "save";
	public static final String closeAction = "close";
 

    // --- action implementations -----------------------------------
    private Action[] defaultActions = {
		new SaveAction(),
    };


 	class SaveAction extends AbstractAction {
		SaveAction() {
			super(saveAction);
		}
		public void actionPerformed(ActionEvent e) {
			File f = GeneralUtil.getFileSaveAs(LittleTablePad.this);
			if(f == null || getModel() == null)
				return;
			if (f.exists()) {
				try {
					BufferedWriter out = new BufferedWriter(new FileWriter(f));
					MyTableUtil.writeJTable(out, getTable());
					out.close();
				} catch(IOException ioe){
					GeneralUtil.showError(LittleTablePad.this, "IO error : \n"+ioe);
				}
			}
		}		
	}
}
