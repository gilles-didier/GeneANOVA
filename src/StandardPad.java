/*
 * @(#)StandardPad.java	1.13 98/08/28
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
import sun.awt.image.codec.*;
import java.awt.image.BufferedImage;

/**
 * Sample application using the simple text editor component that
 * supports only one font.
 *
 * @author  Timothy Prinzing
 * @version 1.13 08/28/98
 */
public abstract class StandardPad extends JPanel {

	private static ResourceBundle resources;
	private static JFrame frontFrame;

	StandardPad() {
		super(true);
		try {
			resources = ResourceBundle.getBundle(getResourceName(), Locale.getDefault());
		} catch (MissingResourceException mre) {
			System.err.println(getResourceName()+" not found");
			System.exit(1);
		}
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception exc) {
			System.err.println("Error loading L&F: " + exc);
		}
		setLayout(new BorderLayout());
    }

	protected void finishCreate() {
	// install the command table
		commands = new Hashtable();
		Action[] actions = getActions();
		for (int i = 0; i < actions.length; i++) {
			Action a = actions[i];
			commands.put(a.getValue(Action.NAME), a);
		}
		menuItems = new Hashtable();
		menubar = createMenubar();
		add("North", menubar);
	}

    /**
     * Find the hosting frame, for the file-chooser dialog.
     */
    protected Frame getFrame() {
		for (Container p = getParent(); p != null; p = p.getParent()) {
			if (p instanceof Frame) {
				return (Frame) p;
			}
		}
		return null;
    }

    /**
     * This is the hook through which all menu items are
     * created.  It registers the result with the menuitem
     * hashtable so that it can be fetched with getMenuItem().
     * @see #getMenuItem
     */
    protected JMenuItem createMenuItem(String cmd) {
		JMenuItem mi = new JMenuItem(getResourceString(cmd + labelSuffix));
		URL url = getResource(cmd + imageSuffix);
		if (url != null) {
			mi.setHorizontalTextPosition(JButton.RIGHT);
			mi.setIcon(new ImageIcon(url));
		}
		String astr = getResourceString(cmd + actionSuffix);
		if (astr == null) {
			astr = cmd;
		}
		mi.setActionCommand(astr);
		Action a = getAction(astr);
		if (a != null) {
			mi.addActionListener(a);
			a.addPropertyChangeListener(createActionChangeListener(mi));
			mi.setEnabled(a.isEnabled());
		} else {
			mi.setEnabled(false);
		}
		menuItems.put(cmd, mi);
		return mi;
	}

    /**
     * Fetch the menu item that was created for the given
     * command.
     * @param cmd  Name of the action.
     * @returns item created for the given command or null
     *  if one wasn't created.
     */
	protected JMenuItem getMenuItem(String cmd) {
		return (JMenuItem) menuItems.get(cmd);
	}

	protected Action getAction(String cmd) {
		return (Action) commands.get(cmd);
	}

	protected String getResourceString(String nm) {
		String str;
		try {
			str = resources.getString(nm);
		} catch (MissingResourceException mre) {
			str = null;
		}
		return str;
	}

	protected URL getResource(String key) {
		String name = getResourceString(key);
		if (name != null) {
			URL url = this.getClass().getResource(name);
			return url;
		}
		return null;
	}



	protected JMenuBar getMenubar() {
		return menubar;
	}

    /**
     * Take the given string and chop it up into a series
     * of strings on whitespace boundries.  This is useful
     * for trying to get an array of strings out of the
     * resource file.
     */
	protected String[] tokenize(String input) {
		Vector v = new Vector();
		StringTokenizer t = new StringTokenizer(input);
		String cmd[];

		while (t.hasMoreTokens())
			v.addElement(t.nextToken());
		cmd = new String[v.size()];
		for (int i = 0; i < cmd.length; i++)
			cmd[i] = (String) v.elementAt(i);
		return cmd;
	}

    /**
     * Create the menubar for the app.  By default this pulls the
     * definition of the menu from the associated resource file. 
     */
	protected JMenuBar createMenubar() {
		JMenuItem mi;
		JMenuBar mb = new JMenuBar();

		String[] menuKeys = tokenize(getResourceString("menubar"));
		for (int i = 0; i < menuKeys.length; i++) {
			JMenu m = createMenu(menuKeys[i]);
			if (m != null) {
				mb.add(m);
			}
		}
		return mb;
	}

    /**
     * Create a menu for the app.  By default this pulls the
     * definition of the menu from the associated resource file.
     */
	protected JMenu createMenu(String key) {
		String[] itemKeys = tokenize(getResourceString(key));
		JMenu menu = new JMenu(getResourceString(key + "Label"));
		for (int i = 0; i < itemKeys.length; i++) {
			if (itemKeys[i].equals("-")) {
				menu.addSeparator();
			} else {
				JMenuItem mi = createMenuItem(itemKeys[i]);
				menu.add(mi);
			}
		}
		return menu;
	}

    // Yarked from JMenu, ideally this would be public.
	protected PropertyChangeListener createActionChangeListener(JMenuItem b) {
		return new ActionChangedListener(b);
 	}

    // Yarked from JMenu, ideally this would be public.
	private class ActionChangedListener implements PropertyChangeListener {
		JMenuItem menuItem;
		ActionChangedListener(JMenuItem mi) {
			super();
			this.menuItem = mi;
		}
		public void propertyChange(PropertyChangeEvent e) {
			String propertyName = e.getPropertyName();
			if (e.getPropertyName().equals(Action.NAME)) {
				String text = (String) e.getNewValue();
				menuItem.setText(text);
			} else
				if (propertyName.equals("enabled")) {
					Boolean enabledState = (Boolean) e.getNewValue();
					menuItem.setEnabled(enabledState.booleanValue());
				}
		}
	}

	protected Hashtable commands;
	protected Hashtable menuItems;
	protected JMenuBar menubar;


 
	public static final String noNameLabel = "NoName.txt";

    /**
     * Suffix applied to the key used in resource file
     * lookups for an image.
     */
    public static final String imageSuffix = "Image";
    /**
     * Suffix applied to the key used in resource file
     * lookups for a label.
     */
    public static final String labelSuffix = "Label";

    /**
     * Suffix applied to the key used in resource file
     * lookups for an action.
     */
    public static final String actionSuffix = "Action";

    /**
     * Suffix applied to the key used in resource file
     * lookups for tooltip text.
     */
    public static final String tipSuffix = "Tooltip";

    public abstract Action[] getActions();
    public abstract String getResourceName();

}
