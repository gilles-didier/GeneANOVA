/*
 * @(#)TablePad.java	1.13 98/08/28
 *
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
import sun.awt.image.codec.*;
import java.awt.image.BufferedImage;
import java.text.ParseException;
import javax.swing.tree.*;

class TablePad extends JPanel {
	private static ResourceBundle resources;
	private TableExp tableExp;
	private TablePanel tablePanel;
	private TaskPad taskPad;
	private ANOVADesign anovaDesign;
	private ANOVACompute anovaCompute;
	private JFrame anovaFrame, taskFrame;
	private ANOVAPad anovaPad;
	private CommandCluster[] tabCommand;
	private Action[] tabAction;
	private JMenu commandMenu;
	private Preferences pref;
	private File currentFile;
	private boolean canOpen = true;
	private JFrame statusFrame;
	private JProgressBar progress;
	private Vector treeVect = new Vector();
	private boolean empty = true;
	private int state;
	private static final int LOADING = 0;
	private static final int ANOVA = 1;
	private static final int CLUSTER = 2;
	private Vector listFrame = new Vector();
	private ColorTable colorTable, colorSpecial;
	private String errorTitle = "errorTitle";
	static {
		try {
			resources = ResourceBundle.getBundle("resources.TablePad", Locale.getDefault());
		} catch (MissingResourceException mre) {
			System.err.println("resources/TablePad.properties not found");
			System.exit(1);
		}
	}


	public TablePad() {
		super(true);
		Hashtable prefTable = new Hashtable();
		prefTable.put(getResourceString("programs"), new PrefImaPanel());
		prefTable.put(getResourceString("colors"), new PrefGraphPanel());
		prefTable.put(getResourceString("query"), new PrefStringPanel(getResourceString("queryAsk"), Const.query));
		pref = new Preferences(new PreferencesPanel(prefTable), null, ".Expression.pref", new setAction());
		tableExp = null;
		tablePanel = new TablePanel();
		tablePanel.getList().addMouseListener(listListener);
		// Force SwingSet to come up in the Cross Platform L&F
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception exc) {
			GeneralUtil.showError(this, "Error loading L&F: " + exc);
		}
//		getFrame().setTitle("noNameLabel");
		setBorder(BorderFactory.createEtchedBorder());
		setLayout(new BorderLayout());

		// install the command table
		commands = new Hashtable();
		Action[] actions = getActions();
		for (int i = 0; i < actions.length; i++) {
			Action a = actions[i];
			commands.put(a.getValue(Action.NAME), a);
		}
		menuItems = new Hashtable();
		menubar = createMenubar();
		maintainDependant();
		setLayout(new BorderLayout());
		add("North", menubar);
		add("Center", tablePanel);
		taskFrame = new JFrame(getResourceString("currentTasks"));
		taskPad = new TaskPad(getResourceString("remove"));
		taskFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		taskFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				showTask.actionPerformed(null);
			}
		});
		showTask.setEnabled(true);
		taskFrame.getContentPane().add("Center", taskPad);
		taskFrame.pack();
		taskFrame.setSize(300, 100);
		taskFrame.show();		
		saveANOVADesign.setEnabled(false);
	}

	public void exit() {
		if(tableExp != null && tableExp.isModified()) {
			switch (JOptionPane.showConfirmDialog(this, getResourceString("save")+" "+currentFile.getAbsolutePath(), "", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
				case JOptionPane.YES_OPTION :
					saveAs.actionPerformed(null);
 				case JOptionPane.NO_OPTION :
					System.exit(0);
					break;
				case JOptionPane.CANCEL_OPTION :
					break;
			}
		} else {
			System.exit(0);
		}		
	}
	
	private void save() {
		if (currentFile != null) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(currentFile));
				tableExp.writeTable(out);
				out.close();
				tableExp.setModified(false);
			} catch(IOException ioe){
				GeneralUtil.showError(TablePad.this, "IO error : \n"+ioe);
			}
		} else {
			saveAs.actionPerformed(null);
		}
	}
	
	public Action[] getActions() {
		int ind = 0, totalSize;
		totalSize = defaultActions.length+pref.getActions().length+dependantActions.length;
		Action res[] = new Action[totalSize];
		for(int i = 0; i<defaultActions.length; i++)
			res[ind++] = defaultActions[i];
		for(int i = 0; i<dependantActions.length; i++)
			res[ind++] = dependantActions[i];
		for(int i = 0; i<pref.getActions().length; i++)
			res[ind++] = pref.getActions()[i];
		return res;
	}

	protected Frame getFrame() {
		for (Container p = getParent(); p != null; p = p.getParent()) {
			if (p instanceof Frame) {
				return (Frame) p;
			}
		}
		return null;
	}

	protected JMenuItem createMenuItem(String cmd) {
		JMenuItem mi = new JMenuItem(getResourceString(cmd + labelSuffix));
		URL url = getResource(cmd + imageSuffix);
		if (url != null) {
			mi.setHorizontalTextPosition(JButton.RIGHT);
			mi.setIcon(new ImageIcon(url));
		}
		String me = getResourceString(cmd+menuSuffix);
		if(me != null) {
			return createMenu(cmd);
		} else {
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
	}
	
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


	protected JMenu createCommandsMenu() {
		if(commandMenu == null)
			commandMenu = new JMenu(getResourceString(commandsLabel + "Label"));
		else
			commandMenu.removeAll();
		if(tabCommand != null) {
			for (int i=0; i< tabCommand.length; i++) {
				JMenuItem mi = createCommandItem(tabCommand[i], tabAction[i]);
				if(mi != null)
					commandMenu.add(mi);
			}
		}
		return commandMenu;
	}
	protected void maintainCommandsMenu() {
		if(commandMenu == null)
			return;
		for(int i=0; i<commandMenu.getItemCount();) {
			JMenuItem item = commandMenu.getItem(i);
			if(item != null && !(item instanceof JMenu) && item.getActionCommand().startsWith("Command")) {
				commandMenu.remove(item);
			} else {
				i++;
			}
		}
		int pos = 0;
		if(tabCommand != null) {
			for (int i=0; i<tabCommand.length; i++) {
				JMenuItem item = createCommandItem(tabCommand[i], tabAction[i]);
				if(item != null) {
					commandMenu.insert(item, pos++);
				}
			}
		}
	}

	protected JMenuItem createCommandItem(CommandCluster f, Action a) {
		if(f == null)
			return null;
		JMenuItem mi = new JMenuItem(f.getName());
		mi.setActionCommand("Command_"+f.getName());
		mi.addActionListener(a);
	   	a.addPropertyChangeListener(createActionChangeListener(mi));
	   	mi.setEnabled(a.isEnabled());
		return mi;
	}

	public void setCommands(CommandCluster[] tab, Action[] tA) {
		commandMenu.removeAll();
		if(tab != null) {
 			for (int i=0; i< tab.length; i++) {
				JMenuItem mi = createCommandItem(tab[i], tA[i]);
				if(mi != null)
					commandMenu.add(mi);
			}
		}
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
				if(menuKeys[i].equals(commandsLabel))
					commandMenu = m;	
			}
		}
		if(commandMenu==null) {
			commandMenu = createCommandsMenu();
			mb.add(commandMenu);
		} else {
			maintainCommandsMenu();
		}
		return mb;
	}

	protected JMenu createMenu(String key) {
		String[] itemKeys = tokenize(getResourceString(key+menuSuffix));
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

	protected PropertyChangeListener createActionChangeListener(JMenuItem b) {
		return new ActionChangedListener(b);
	}

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
			} else if (propertyName.equals("enabled")) {
				Boolean enabledState = (Boolean) e.getNewValue();
				menuItem.setEnabled(enabledState.booleanValue());
			}
		}
	}
	
	private void disposeFrames() {
		for (Enumeration e = listFrame.elements() ; e.hasMoreElements() ;) {
			((JFrame) e.nextElement()).dispose();
		}
		listFrame.clear();
	}
	

	public void maintainDependant() {
		if(tableExp != null) {
			if(dependantActions != null)
			for(int i = 0; i<dependantActions.length; i++)
				if(dependantActions[i] != null)
					dependantActions[i].setEnabled(true);
			if(tabAction != null)
			for(int i = 0; i<tabAction.length; i++)
			if(tabAction[i] != null)
				tabAction[i].setEnabled(true);
		} else {
			if(dependantActions != null)
			for(int i = 0; i<dependantActions.length; i++)
				if(dependantActions[i] != null)
				dependantActions[i].setEnabled(false);
			if(tabAction != null)
			for(int i = 0; i<tabAction.length; i++)
			if(tabAction[i] != null)
				tabAction[i].setEnabled(false);
		}
	}
	

	private Hashtable commands;
	private Hashtable menuItems;
	private JMenuBar menubar;
	
	public static final String noNameLabel = "NoName.txt";

	public static final String menuSuffix = "Menu";
	public static final String imageSuffix = "Image";
	public static final String labelSuffix = "Label";
	public static final String actionSuffix = "Action";
	public static final String tipSuffix = "Tooltip";
	public static final String commandsLabel = "command";
	
	public static final String openAction = "open";
	public static final String saveAction = "save";
	public static final String saveAsAction = "saveas";
	public static final String saveByColumnAction = "saveColumn";
	public static final String saveByLineAction = "saveLine";
 	public static final String closeAction = "close";
	public static final String toLogAction = "toLog";
 	public static final String toSqrtAction = "toSqrt";
	public static final String toRankAction = "toRank";
	public static final String extractAction = "extract";
	public static final String saveANOVADesignAction = "saveANOVADesign";
	public static final String loadANOVADesignAction = "loadANOVADesign";
	public static final String enterANOVADesignAction = "enterANOVADesign";
	public static final String computeANOVAAction = "computeANOVA";
	public static final String saveTableAction = "saveTable";
	public static final String saveDatasAction = "saveDatas";
	public static final String sortAction = "sort";
	public static final String exitAction = "exit";
	public static final String saveDatasTransposedAction = "saveDatasTransposed";
	public static final String showANOVAFrameAction = "showANOVAFrame";
	public static final String showTaskFrameAction = "showTaskFrame";
	public static final String correlAnovaAction = "correlAnova";
	public static final String correlAnovaBisAction = "correlAnovaBis";
	public static final String correlAnovaTerAction = "correlAnovaTer";
	public static final String correlAnovaSqrtAction = "correlAnovaSqrt";
	public static final String correlAnovaBisSqrtAction = "correlAnovaBisSqrt";
	public static final String correlAnovaTerSqrtAction = "correlAnovaTerSqrt";
	public static final String correlAlainAction = "correlAlain";
	public static final String correlBetweenRowsAction = "correlRows";
	public static final String correlBetweenColsAction = "correlCols";
	public static final String helpAction = "help";
	public static final String toCenterReduceAction = "toCenterReduce";
	public static final String aNOVALocaleTableAction = "aNOVALocaleTable";
	public static final String pcaAction = "pca";

	private Action saveAs = new SaveAsAction();
	private Action saveANOVADesign =	new SaveANOVADesignAction();
	private Action showANOVA =	new ShowANOVAFrameAction();
	private Action showTask =	new ShowTaskFrameAction();

	private Action[] defaultActions = {
		new CloseAction(),
		new OpenAction(),
		new HelpAction(),
		new ExitAction(),
		showANOVA,
		showTask
	};

	private Action[] dependantActions = {
		new SaveByColumnAction(),
		new SaveByLineAction(),
		new ToCenterReduceAction(),
		new ToLogAction(),
		new ToSqrtAction(),
		new ToRankAction(),
		new SortAction(),
		new ExtractAction(),
		new ComputeANOVAAction(),
		new ANOVALocaleTableAction(),
		new SaveTableAction(),
		new SaveDatasAction(),
		new SaveDatasTransposedAction(),
		new SaveAction(),
		saveANOVADesign,
		new LoadANOVADesignAction(),
		new EnterANOVADesignAction(),
		new CorrelAnovaTerAction(),
		new PCAAction(),
		new CorrelAnovaAction(),
		new CorrelAnovaTerSqrtAction(),
		new CorrelAnovaBisSqrtAction(),
		new CorrelAnovaSqrtAction(),
		new CorrelBetweenColsAction(),
		new CorrelBetweenRowsAction(),
		saveAs
	};

	
	private static final String titleSuffix = "Title";
	private static final String messageSuffix = "Message";
	private static final String error = "error";
	private static final String errorReading = "errorReading";
	private static final String exit = "exit";
	private static final String newin = "new";
	private static final String helpIndex = "helpIndex";

	class HelpAction extends AbstractAction {
		private JFrame newFrame;
		HelpAction() {
			super(helpAction);
		}
		public void actionPerformed(ActionEvent e) {
			if(newFrame == null || !newFrame.isDisplayable()) {
				newFrame = new JFrame(getResourceString(helpAction+labelSuffix));
				newFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				newFrame.getContentPane().add(new Help(getResourceString(helpIndex)), BorderLayout.CENTER);
				newFrame.pack();
				newFrame.setSize(500, 600);
				newFrame.show();
			} else {
				newFrame.show();
				newFrame.toFront();
			}
		}		
	}

	class OpenAction extends AbstractAction {
		OpenAction() {
			super(openAction);
		}
		public void actionPerformed(ActionEvent e) {
			if(!canOpen)
				return;
			if(tableExp!=null && tableExp.isModified()) {
				switch (JOptionPane.showConfirmDialog(TablePad.this, getResourceString("save")+" "+currentFile.getAbsolutePath(), "", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
					case JOptionPane.YES_OPTION :
						save();
 					case JOptionPane.NO_OPTION :
						break;
					case JOptionPane.CANCEL_OPTION :
						return;
				}
			}
			taskPad.destroyAll();
			File f = GeneralUtil.getFileOpen(TablePad.this);
			if (f == null || !f.exists())
				return;
			JRadioButton radio1 = new JRadioButton(getResourceString("firstRowLab"));
			JRadioButton radio2 = new JRadioButton(getResourceString("firstRowDat"));
			JRadioButton radio3 = new JRadioButton(getResourceString("firstColLab"));
			JRadioButton radio4 = new JRadioButton(getResourceString("firstColDat"));
      			radio1.setSelected(true);
			radio3.setSelected(true);
			ButtonGroup group1 = new ButtonGroup();
			group1.add(radio1);
			group1.add(radio2);
			ButtonGroup group2 = new ButtonGroup();
			group2.add(radio3);
			group2.add(radio4);
			Object[] message = new Object[5];
			message[0] = radio1;
			message[1] = radio2;
			message[2] = radio3;
			message[3] = radio4;
			if(JOptionPane.showOptionDialog(TablePad.this, message, getResourceString("readOpt"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null)==JOptionPane.OK_OPTION) {
				boolean rowIsLab = ((JRadioButton) message[0]).isSelected();
				boolean colIsLab = ((JRadioButton) message[2]).isSelected();
				(new ReadTableThread(f, rowIsLab, colIsLab)).start();
			}
		}
	}

	class SaveAsAction extends AbstractAction {
		SaveAsAction() {
			super(saveAsAction);
		}
		public void actionPerformed(ActionEvent e) {
			File f = GeneralUtil.getFileSaveAs(TablePad.this);
			if(f == null)
				return;

			if (f.exists()) {
				getFrame().setTitle(f.getAbsolutePath());
				currentFile = f;
				try {
					BufferedWriter out = new BufferedWriter(new FileWriter(f));
					tableExp.writeTable(out);
					out.close();
					tableExp.setModified(false);
				} catch(IOException ioe){
					GeneralUtil.showError(TablePad.this, "IO error : \n"+ioe);
				}
			}
		}		
	}

	class SaveAction extends AbstractAction {
		SaveAction() {
			super(saveAction);
		}
		public void actionPerformed(ActionEvent e) {
			if(currentFile == null) {
				saveAs.actionPerformed(e);
			} else {
				if (currentFile.exists()) {
					try {
						BufferedWriter out = new BufferedWriter(new FileWriter(currentFile));
						tableExp.writeTable(out);
						out.close();
						tableExp.setModified(false);
					} catch(IOException ioe){
						GeneralUtil.showError(TablePad.this, "IO error : \n"+ioe);
					}
				}
			}
		}		
	}
	
 	class SaveTableAction extends AbstractAction {
		SaveTableAction () {
			super(saveTableAction);
		}
		public void actionPerformed(ActionEvent e) {
			File f = GeneralUtil.getFileSaveAs(TablePad.this);
			if(f == null)
				return;
			if (f.exists()) {
				try {
					BufferedWriter out = new BufferedWriter(new FileWriter(f));
					tableExp.writeTable(out);
					out.close();
				} catch(IOException ioe){
					GeneralUtil.showError(TablePad.this, "IO error : \n"+ioe);
				}
			}
		}		
	}

 	class SaveDatasTransposedAction extends AbstractAction {
		SaveDatasTransposedAction () {
			super(saveDatasTransposedAction);
		}
		public void actionPerformed(ActionEvent e) {
			File f = GeneralUtil.getFileSaveAs(TablePad.this);
			if(f == null)
				return;
			if (f.exists()) {
				try {
					BufferedWriter out = new BufferedWriter(new FileWriter(f));
					tableExp.writeDatasTransposed(out);
					out.close();
				} catch(IOException ioe){
						System.out.println("IO error : \n"+ioe);
				}
			}
		}		
	}
			
 	class SaveDatasAction extends AbstractAction {
		SaveDatasAction () {
			super(saveDatasAction );
		}
		public void actionPerformed(ActionEvent e) {
			File f = GeneralUtil.getFileSaveAs(TablePad.this);
			if(f == null)
				return;
			if (f.exists()) {
				try {
					BufferedWriter out = new BufferedWriter(new FileWriter(f));
					tableExp.writeDatas(out);
					out.close();
				} catch(IOException ioe){
					GeneralUtil.showError(TablePad.this, "IO error : \n"+ioe);
				}
			}
		}		
	}

 	class SaveByColumnAction extends AbstractAction {
		SaveByColumnAction () {
			super(saveByColumnAction );
		}
		public void actionPerformed(ActionEvent e) {
			File f = GeneralUtil.getFileSaveAs(TablePad.this);
			if(f == null)
				return;

			if (f.exists()) {
				try {
					BufferedWriter out = new BufferedWriter(new FileWriter(f));
					tableExp.writeDistByColumn(out);
					out.close();
				} catch(IOException ioe){
					GeneralUtil.showError(TablePad.this, "IO error : \n"+ioe);
				}
			}
		}		
	}
	
 	class SaveByLineAction extends AbstractAction {
		SaveByLineAction () {
			super(saveByLineAction );
		}
		public void actionPerformed(ActionEvent e) {
			File f = GeneralUtil.getFileSaveAs(TablePad.this);
			if(f == null)
				return;
			if (f.exists()) {
				try {
					BufferedWriter out = new BufferedWriter(new FileWriter(f));
					tableExp.writeDistByRow(out);
					out.close();
				} catch(IOException ioe){
					GeneralUtil.showError(TablePad.this, "IO error : \n"+ioe);
				}
			}
		}		
	}
	
 	class SetANOVADesign extends AbstractAction {
		SetANOVADesign() {
			super(saveANOVADesignAction );
		}
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() instanceof DesignANOVADialog) {
				if(((DesignANOVADialog) e.getSource()).getANOVA() != null)
					anovaDesign = ((DesignANOVADialog) e.getSource()).getANOVA();
					saveANOVADesign.setEnabled(true);
			}
		}
	}

	class SaveANOVADesignAction extends AbstractAction {
		SaveANOVADesignAction () {
			super(saveANOVADesignAction );
		}
		public void actionPerformed(ActionEvent e) {
			File f = GeneralUtil.getFileSaveAs(TablePad.this);
			if(f == null)
				return;
			if (f.exists()) {
				try {
					BufferedWriter out = new BufferedWriter(new FileWriter(f));
					anovaDesign.write(out);
					out.close();
				} catch(IOException ioe){
					GeneralUtil.showError(TablePad.this, "IO error : \n"+ioe);
				}
			}
		}		
	}

 	class LoadANOVADesignAction extends AbstractAction {
		LoadANOVADesignAction () {
			super(loadANOVADesignAction);
		}
		public void actionPerformed(ActionEvent e) {
			File f = GeneralUtil.getFileOpen(TablePad.this);
			if (f == null || !f.exists())
				return;
			try {
				BufferedReader in = new BufferedReader(new FileReader(f));
				if(in == null)
					return;
				ANOVADesign an = new ANOVADesign(in);
				saveANOVADesign.setEnabled(true);
				in.close();
				anovaDesign = an;
			} catch(IOException ioe){
				GeneralUtil.showError(TablePad.this, "IO error : \n"+ioe);
			} catch(ParseException pe){
				GeneralUtil.showError(TablePad.this, "Parse error : \n"+pe);
			}
		}		
	}

  	class ToCenterReduceAction extends AbstractAction {
		ToCenterReduceAction() {
			super(toCenterReduceAction);
		}
		public void actionPerformed(ActionEvent e) {
			disposeFrames();
			tableExp.toCenterReduce();
			tablePanel.repaint();
		}
	}

	class ToLogAction extends AbstractAction {
		ToLogAction() {
			super(toLogAction);
		}
		public void actionPerformed(ActionEvent e) {
			disposeFrames();
			tableExp.toLog();
			tablePanel.repaint();
		}
	}
	
 	class ToSqrtAction extends AbstractAction {
		ToSqrtAction() {
			super(toSqrtAction);
		}
		public void actionPerformed(ActionEvent e) {
			disposeFrames();
			tableExp.toSqrt();
			tablePanel.repaint();
		}
	}

	class ToRankAction extends AbstractAction {
		ToRankAction() {
			super(toRankAction);
		}
		public void actionPerformed(ActionEvent e) {
			disposeFrames();
			tableExp.toRank();
			tablePanel.repaint();
		}
	}

	class SortAction extends AbstractAction {
		SortAction() {
			super(sortAction);
		}
		public void actionPerformed(ActionEvent e) {
			disposeFrames();
			tableExp.sortRows();
			tablePanel.resetTableModel();
			tablePanel.repaint();
		}
	}
	
	class ExtractAction extends AbstractAction {
		ExtractAction() {
			super(extractAction);
		}
		public void actionPerformed(ActionEvent e) {
			ToggleListsBis toggle = new ToggleListsBis(tableExp.getRowLabelsIndexCopy(), new Object[0], getResourceString("unselected"), getResourceString("selected"));
			Object[] message = new Object[2];
			message[0] = new JLabel(getResourceString("selector"));
			message[1] = toggle;
			if(JOptionPane.showOptionDialog(TablePad.this, message, getResourceString("extract"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null)==JOptionPane.OK_OPTION) {
					Object[] sel = toggle.getList2();
					int[] index = new int[sel.length];
					for(int i=0; i<sel.length; i++) {
						index[i] = ((LabelIndex) sel[i]).getIndex();
					}
					disposeFrames();
					tableExp.extractRows(index);
					tablePanel.resetTableModel();
			}
		}
	}
	

 	class EnterANOVADesignAction extends AbstractAction {
		EnterANOVADesignAction() {
			super(enterANOVADesignAction);
		}
		public void actionPerformed(ActionEvent e) {
			DesignANOVADialog dial = new DesignANOVADialog(tableExp, new SetANOVADesign());
			dial.show();
		}
	}
 
	class ComputeANOVAAction extends AbstractAction {
		ComputeANOVAAction() {
			super(computeANOVAAction);
		}
		public void actionPerformed(ActionEvent e) {
			if(anovaDesign != null) {
				if(anovaDesign.isCompatibleWith(tableExp)) {
					Object[] message = new Object[2];
					NumberField numField = new NumberField(false, false, 1, 4);
					message[0] = new JLabel(getResourceString("orderInt"));
					message[1] = numField;
					if(JOptionPane.showOptionDialog(TablePad.this, message, "ANOVA", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null)==JOptionPane.OK_OPTION) {
						(new ComputeANOVAThread(numField.intValue())).start();
					}
				} else {
					GeneralUtil.showError(TablePad.this, getResourceString("designNotOK"));
				}
			} else {
				GeneralUtil.showError(TablePad.this, getResourceString("loadDesign"));
			}				
		}
	}
	class ShowANOVAFrameAction extends AbstractAction {
		ShowANOVAFrameAction() {
			super(showANOVAFrameAction);
		}
		public void actionPerformed(ActionEvent e) {
			if(anovaFrame != null) {
				if(anovaFrame.isVisible()) {
					anovaFrame.setVisible(false);
					putValue(Action.NAME, getResourceString("showAnova"));
				} else {
					anovaFrame.setVisible(true);
					putValue(Action.NAME, getResourceString("hideAnova"));
					anovaFrame.toFront();
				}
			} else {
				GeneralUtil.showError(TablePad.this, getResourceString("noAnova"));
			}				
		}
	}
	
	class ANOVALocaleTableAction extends AbstractAction {
		ANOVALocaleTableAction() {
			super(aNOVALocaleTableAction);
		}
		public void actionPerformed(ActionEvent e) {
			if(anovaDesign != null) {
				if(anovaDesign.isCompatibleWith(tableExp)) {
					JPanel tmpPane = new JPanel();
					JList selectList1 = new JList(anovaDesign.getLabelsIndexCopy());
					JList selectList2 = new JList(anovaDesign.getLabelsIndexCopy());
					selectList1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					selectList2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					tmpPane.add(selectList1);
					tmpPane.add(selectList2);
					Object[] message = new Object[2];
					message[0] = new JLabel(getResourceString("selectAFactor"));
					message[1] = tmpPane;
					if(JOptionPane.showOptionDialog(TablePad.this, message, getResourceString("correlAlain"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null)==JOptionPane.OK_OPTION) {
						Object sel = selectList1.getSelectedValue();
						int index1 = ((LabelIndex) sel).getIndex();
						sel = selectList2.getSelectedValue();
						int index2 = ((LabelIndex) sel).getIndex();
						if(index1 == index2)
							return;
						ANOVALocalePad newPad = new ANOVALocalePad((new ANOVACompute(anovaDesign)).getTableModelLocal(tableExp, index1, index2));
						JFrame newFrame = new JFrame("ANOVA");
						newFrame.getContentPane().add(newPad);
						listFrame.add(newFrame);
						newFrame.addWindowListener(frameCloser);
						newFrame.pack();
						newFrame.setSize(800,600);
						newFrame.show();
					}
				} else {
					GeneralUtil.showError(TablePad.this, getResourceString("designNotOK"));
				}
			} else {
				GeneralUtil.showError(TablePad.this, getResourceString("loadDesign"));
			}				
		}
	}
	class ShowTaskFrameAction extends AbstractAction {
		ShowTaskFrameAction() {
			super(showTaskFrameAction);
		}
		public void actionPerformed(ActionEvent e) {
			if(taskFrame != null) {
				if(taskFrame.isVisible()) {
					taskFrame.hide();
					putValue(Action.NAME, getResourceString("showTask"));
				} else {
					taskFrame.show();
					putValue(Action.NAME, getResourceString("hideTask"));
				}
			}			
		}
	}
	

	class AnovaListPoint extends ListPoint {
		double[][] res;
		Factor factor;
		
		public AnovaListPoint(double[][] r, Factor f) {
			res = r;
			factor = f;
		}
		public double getX(int indice) {
			return res[indice][0];
		}
		public double getY(int indice) {
			return res[indice][1];
		}
		public String getName(int indice) {
			switch(factor.getType()) {
				case Factor.ALL_ROWS :
					return tableExp.getRowLabels()[indice];
				case Factor.ALL_COLUMNS :
					return tableExp.getColLabels()[indice];
				case Factor.PARTIAL_ROWS :
				case Factor.PARTIAL_COLUMNS :
					return (factor.getName()+indice);
				default :
					return Integer.toString(indice);
			}			
		}
		public int size() {
			return res.length;
		}
	}

	class GeneListPoint extends ListPoint {
		double[][] res;
		
		public GeneListPoint(double[][] r) {
			res = r;
		}
		public double getX(int indice) {
			return res[indice][0];
		}
		public double getY(int indice) {
			return res[indice][1];
		}
		public String getName(int indice) {
			return tableExp.getRowLabels()[indice];
		}
		public int size() {
			return res.length;
		}
	}

	class AlainListPoint extends ListPoint {
		double[][] res;

		public AlainListPoint(double[][] r) {
			res = r;
		}
		public double getX(int indice) {
			return res[indice][0];
		}
		public double getY(int indice) {
			return res[indice][1];
		}
		public String getName(int indice) {
			return tableExp.getRowLabels()[indice];
		}
		public int size() {
			return tableExp.getNumberOfRows();
		}
	}
	class ColsListPoint extends ListPoint {
		int colX, colY;
		
		public ColsListPoint(int cX, int cY) {
			colX = cX; colY = cY;
		}
		
		public double getX(int indice) {
			return tableExp.getDatas()[indice][colX];
		}
		
		public double getY(int indice) {
			return tableExp.getDatas()[indice][colY];
		}
		public String getName(int indice) {
			return tableExp.getRowLabels()[indice];
		}
		public int size() {
			return tableExp.getNumberOfRows();
		}
	}

	class RowsListPoint extends ListPoint {
		int rowX, rowY;

		public RowsListPoint(int cX, int cY) {
			rowX = cX; rowY = cY;
		}
		
		public double getX(int indice) {
			return tableExp.getDatas()[rowX][indice];
		}
		
		public double getY(int indice) {
			return tableExp.getDatas()[rowY][indice];
		}
		public String getName(int indice) {
			return tableExp.getColLabels()[indice];
		}
		public int size() {
			return tableExp.getNumberOfCols();
		}
	}
	
	private WindowAdapter frameCloser = new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
			listFrame.remove(e.getWindow());
			e.getWindow().dispose();
		}
    	};
	private ShowInfo showInfo = new ShowInfo("http://srs.ebi.ac.uk/srs6bin/cgi-bin/wgetz?-newId+[SWALL-AllText:%n]+-lv+30+-view+SequenceSimple");
	private MouseListener listListener = new ListProtMouseListener(showInfo);
  	class CorrelAnovaTerAction extends AbstractAction {
		JFrame newFrame = null;

		CorrelAnovaTerAction() {
			super(correlAnovaTerAction);
		}
		public void actionPerformed(ActionEvent e) {
			if(anovaDesign == null) {
				GeneralUtil.showError(TablePad.this, getResourceString("loadDesign"));
				return;
			}
			if(!anovaDesign.isCompatibleWith(tableExp)) {
				GeneralUtil.showError(TablePad.this, getResourceString("designNotOK"));
				return;
			}
			if(newFrame != null && !newFrame.isDisplayable())
				newFrame = null;
			final boolean same = false;
			JPanel tmpPane = new JPanel();
			JTable table = new JTable(new AbstractTableModel() {
				public Dimension dim = new Dimension(CASE_SIZE, CASE_SIZE);
				public int getColumnCount() { return anovaDesign.getNumberOfFactors(); }
				public int getRowCount() { return anovaDesign.getNumberOfFactors();}
				public Object getValueAt(int row, int col) {
					if(row != col) {
						double[][] res = (new ANOVACompute(anovaDesign)).computeLocalTer(tableExp.getDatas(), row, col);
						return new PointIcon(dim, new AnovaListPoint(res, anovaDesign.getFactor(row)), same, false, colorTable.getColor(0));
					} else {
						return new String("");
					}
				}
				public String getColumnName(int column) {return anovaDesign.getFactor(column).getName();}
				public Class getColumnClass(int c) {return PointIcon.class;}
				public boolean isCellEditable(int row, int col) {return row != col;}
				public void setValueAt(Object aValue, int row, int column) {}
			});
			table.setDefaultRenderer(PointIcon.class, iconRenderer);
			table.setRowSelectionAllowed(false);
			JCheckBox bof = new JCheckBox();
			table.setDefaultEditor(PointIcon.class, new DefaultCellEditor(bof) {
				class showFrame extends AbstractAction {
						int row, col;
						public showFrame(int r, int c) {
							row = r; col = c;
						}
						public void actionPerformed(ActionEvent e) {
							double[][] res = (new ANOVACompute(anovaDesign)).computeLocalTer(tableExp.getDatas(), row, col);
							newFrame.setTitle(anovaDesign.getFactor(row).getName()+" / "+anovaDesign.getFactor(col).getName());
							newFrame.getContentPane().removeAll();
							newFrame.setSize(600, 600);
							PointPanel panel = new PointPanel(new AnovaListPoint(res, anovaDesign.getFactor(row)), same, colorTable, colorSpecial);
							panel.setListListener(listListener);
							panel.setShowAction(showInfo);
							newFrame.getContentPane().add(panel, BorderLayout.CENTER);
							newFrame.pack();
							newFrame.show();
							newFrame = null;
							stopCellEditing();
						}
				}
				public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
					JButton b = new JButton((Icon) value);
					b.addActionListener(new showFrame(row, col));
					return b;
				}
			});
			table.setRowHeight(CASE_SIZE);
			table.setGridColor(Color.black);
			table.setSelectionBackground(Color.white);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			table.sizeColumnsToFit(JTable.AUTO_RESIZE_OFF);
			JScrollPane scroller = new JScrollPane();
			JViewport port = scroller.getViewport();
			port.add(table);
			port.setScrollMode(JViewport.BLIT_SCROLL_MODE);
			JList rowList = new JList(anovaDesign.getLabelsIndexCopy());
			rowList.setBackground(Color.lightGray);
			rowList.setFixedCellHeight(CASE_SIZE);
			rowList.setCellRenderer(new MyCellRenderer(font));
			scroller.setRowHeaderView(rowList);
			tmpPane.add(scroller);
			if(newFrame == null) {
					newFrame = new JFrame(getResourceString("correlAnova"));
					newFrame.getContentPane().add(tmpPane, BorderLayout.CENTER);
					newFrame.pack();
					newFrame.setSize(600, 500);
					listFrame.add(newFrame);
					newFrame.addWindowListener(frameCloser);
					newFrame.show();
			} else {
				newFrame.toFront();
			}
		}
	}

  	class PCAAction extends AbstractAction {
		JFrame newFrame;
		PCAAction() {
			super(pcaAction);
		}
		public void actionPerformed(ActionEvent e) {
			if(newFrame != null && !newFrame.isDisplayable())
				newFrame = null;
			if(newFrame == null) {			
				newFrame = new JFrame(getResourceString(pcaAction+"Label"));
				newFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
				newFrame.setSize(600, 600);
				PointPCASuperPanel panel = new PointPCASuperPanel(tableExp, colorTable, colorSpecial);
				panel.setListListener(listListener);
				panel.setShowAction(showInfo);
				listFrame.add(newFrame);
				newFrame.getContentPane().add(panel, BorderLayout.CENTER);
				newFrame.pack();
				newFrame.show();
			} else {
				newFrame.setVisible(true);
				newFrame.toFront();
			}
		}
	}


  	class CorrelAnovaAction extends AbstractAction {
		JFrame newFrame = null;

		CorrelAnovaAction() {
			super(correlAnovaAction);
		}
		public void actionPerformed(ActionEvent e) {
			if(anovaDesign == null) {
				GeneralUtil.showError(TablePad.this, getResourceString("loadDesign"));
				return;
			}
			if(!anovaDesign.isCompatibleWith(tableExp)) {
				GeneralUtil.showError(TablePad.this, getResourceString("designNotOK"));
				return;
			}
			if(newFrame != null && !newFrame.isDisplayable())
				newFrame = null;
			final boolean same = false;
			JPanel tmpPane = new JPanel();
			JTable table = new JTable(new AbstractTableModel() {
				public Dimension dim = new Dimension(CASE_SIZE, CASE_SIZE);
				public int getColumnCount() { return anovaDesign.getNumberOfFactors(); }
				public int getRowCount() { return anovaDesign.getNumberOfFactors();}
				public Object getValueAt(int row, int col) {
					if(row != col) {
						ANOVACompute anovaComp = new ANOVACompute(anovaDesign);
						double[][] res = anovaComp.computeLocal(tableExp.getDatas(), row, col);
						int ddlF = anovaComp.getLocalDDLF(col);
						int ddlR = anovaComp.getLocalDDLR(tableExp.getDatas(), row);
						Line line[] = new Line[3];
						line[0] = new Line(1/StatisticFunctions.inverse(0.05, ddlF, ddlR), 0, "P-Value = "+Double.toString(0.05));
						line[1] = new Line(1/StatisticFunctions.inverse(0.01, ddlF, ddlR), 0, "P-Value = "+Double.toString(0.01));
						line[2] = new Line(1/StatisticFunctions.inverse(0.001, ddlF, ddlR), 0, "P-Value = "+Double.toString(0.001));
						return new PointIcon(dim, new AnovaListPoint(res, anovaDesign.getFactor(row)), same, false, colorTable.getColor(0), line);
					} else {
						return new String("");
					}
				}
				public String getColumnName(int column) {return anovaDesign.getFactor(column).getName();}
				public Class getColumnClass(int c) {return PointIcon.class;}
				public boolean isCellEditable(int row, int col) {return row != col;}
				public void setValueAt(Object aValue, int row, int column) {}
			});
			table.setDefaultRenderer(PointIcon.class, iconRenderer);
			table.setRowSelectionAllowed(false);
			JCheckBox bof = new JCheckBox();
			table.setDefaultEditor(PointIcon.class, new DefaultCellEditor(bof) {
				class showFrame extends AbstractAction {
						int row, col;
						public showFrame(int r, int c) {
							row = r; col = c;
						}
						public void actionPerformed(ActionEvent e) {
							ANOVACompute anovaComp = new ANOVACompute(anovaDesign);
							double[][] res = anovaComp.computeLocal(tableExp.getDatas(), row, col);
							int ddlF = anovaComp.getLocalDDLF(col);
							int ddlR = anovaComp.getLocalDDLR(tableExp.getDatas(), row);
							Line line[] = new Line[3];
							line[0] = new Line(1/StatisticFunctions.inverse(0.05, ddlF, ddlR), 0);
							line[1] = new Line(1/StatisticFunctions.inverse(0.01, ddlF, ddlR), 0);
							line[2] = new Line(1/StatisticFunctions.inverse(0.001, ddlF, ddlR), 0);
							newFrame.setTitle(anovaDesign.getFactor(row).getName()+" / "+anovaDesign.getFactor(col).getName());
							newFrame.getContentPane().removeAll();
							newFrame.setSize(600, 600);
							PointPanel panel = new PointPanel(new AnovaListPoint(res, anovaDesign.getFactor(row)), same, colorTable, colorSpecial, line);
							panel.setListListener(listListener);
							panel.setShowAction(showInfo);
							newFrame.getContentPane().add(panel, BorderLayout.CENTER);
							newFrame.pack();
							newFrame.show();
							newFrame = null;
							stopCellEditing();
						}
				}
				public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
					JButton b = new JButton((Icon) value);
					b.addActionListener(new showFrame(row, col));
					return b;
				}
			});
			table.setRowHeight(CASE_SIZE);
			table.setGridColor(Color.black);
			table.setSelectionBackground(Color.white);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			table.sizeColumnsToFit(JTable.AUTO_RESIZE_OFF);
			JScrollPane scroller = new JScrollPane();
			JViewport port = scroller.getViewport();
			port.add(table);
			port.setScrollMode(JViewport.BLIT_SCROLL_MODE);
			JList rowList = new JList(anovaDesign.getLabelsIndexCopy());
			rowList.setBackground(Color.lightGray);
			rowList.setFixedCellHeight(CASE_SIZE);
			rowList.setCellRenderer(new MyCellRenderer(font));
			scroller.setRowHeaderView(rowList);
			tmpPane.add(scroller);
			if(newFrame == null) {
					newFrame = new JFrame(getResourceString("correlAnova"));
					newFrame.getContentPane().add(tmpPane, BorderLayout.CENTER);
					newFrame.pack();
					newFrame.setSize(600, 500);
					listFrame.add(newFrame);
					newFrame.addWindowListener(frameCloser);
					newFrame.show();
			} else {
				newFrame.toFront();
			}
		}
	}


  	class CorrelAnovaTerSqrtAction extends AbstractAction {
		JFrame newFrame = null;

		CorrelAnovaTerSqrtAction() {
			super(correlAnovaTerSqrtAction);
		}
		public void actionPerformed(ActionEvent e) {
			if(anovaDesign == null) {
				GeneralUtil.showError(TablePad.this, getResourceString("loadDesign"));
				return;
			}
			if(!anovaDesign.isCompatibleWith(tableExp)) {
				GeneralUtil.showError(TablePad.this, getResourceString("designNotOK"));
				return;
			}
			if(newFrame != null && !newFrame.isDisplayable())
				newFrame = null;
			final boolean same = false;
			JPanel tmpPane = new JPanel();
			JTable table = new JTable(new AbstractTableModel() {
				public Dimension dim = new Dimension(CASE_SIZE, CASE_SIZE);
				public int getColumnCount() { return anovaDesign.getNumberOfFactors(); }
				public int getRowCount() { return anovaDesign.getNumberOfFactors();}
				public Object getValueAt(int row, int col) {
					if(row != col) {
						double[][] res = (new ANOVACompute(anovaDesign)).computeLocalTerSqrt(tableExp.getDatas(), row, col);
						return new PointIcon(dim, new AnovaListPoint(res, anovaDesign.getFactor(row)), same, false, colorTable.getColor(0));
					} else {
						return new String("");
					}
				}
				public String getColumnName(int column) {return anovaDesign.getFactor(column).getName();}
				public Class getColumnClass(int c) {return PointIcon.class;}
				public boolean isCellEditable(int row, int col) {return row != col;}
				public void setValueAt(Object aValue, int row, int column) {}
			});
			table.setDefaultRenderer(PointIcon.class, iconRenderer);
			table.setRowSelectionAllowed(false);
			JCheckBox bof = new JCheckBox();
			table.setDefaultEditor(PointIcon.class, new DefaultCellEditor(bof) {
				class showFrame extends AbstractAction {
						int row, col;
						public showFrame(int r, int c) {
							row = r; col = c;
						}
						public void actionPerformed(ActionEvent e) {
							double[][] res = (new ANOVACompute(anovaDesign)).computeLocalTerSqrt(tableExp.getDatas(), row, col);
							newFrame.setTitle(anovaDesign.getFactor(row).getName()+" / "+anovaDesign.getFactor(col).getName());
							newFrame.getContentPane().removeAll();
							newFrame.setSize(600, 600);
							PointPanel panel = new PointPanel(new AnovaListPoint(res, anovaDesign.getFactor(row)), same, colorTable, colorSpecial);
							panel.setListListener(listListener);
							panel.setShowAction(showInfo);
							newFrame.getContentPane().add(panel, BorderLayout.CENTER);
							newFrame.pack();
							newFrame.show();
							newFrame = null;
							stopCellEditing();
						}
				}
				public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
					JButton b = new JButton((Icon) value);
					b.addActionListener(new showFrame(row, col));
					return b;
				}
			});
			table.setRowHeight(CASE_SIZE);
			table.setGridColor(Color.black);
			table.setSelectionBackground(Color.white);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			table.sizeColumnsToFit(JTable.AUTO_RESIZE_OFF);
			JScrollPane scroller = new JScrollPane();
			JViewport port = scroller.getViewport();
			port.add(table);
			port.setScrollMode(JViewport.BLIT_SCROLL_MODE);
			JList rowList = new JList(anovaDesign.getLabelsIndexCopy());
			rowList.setBackground(Color.lightGray);
			rowList.setFixedCellHeight(CASE_SIZE);
			rowList.setCellRenderer(new MyCellRenderer(font));
			scroller.setRowHeaderView(rowList);
			tmpPane.add(scroller);
			if(newFrame == null) {
					newFrame = new JFrame(getResourceString("correlAnova"));
					newFrame.getContentPane().add(tmpPane, BorderLayout.CENTER);
					newFrame.pack();
					newFrame.setSize(600, 500);
					listFrame.add(newFrame);
					newFrame.addWindowListener(frameCloser);
					newFrame.show();
			} else {
				newFrame.toFront();
			}
		}
	}
	
	class IconCellRenderer extends JComponent implements TableCellRenderer {
		Color backGround;
		Icon icon;
		public IconCellRenderer() {
			backGround = (new Bof()).getSelectedBackground();
		}
		
		public Component getTableCellRendererComponent(JTable table, Object d, boolean isSelected, boolean hasFocus,int row, int column) {
			int frac = 0;
			setBackground(isSelected ? backGround : Color.white);
			if(d instanceof Icon || d instanceof PointIcon) {
				icon = (Icon) d;
			} else {
				icon = null;
			}
			return this;
		}

		public void paint(Graphics g) {
			if(icon == null)
				return;
			Dimension d = getSize();
			g.setColor(getBackground());
			g.fillRect(0, 0, d.width, d.height);
			icon.paintIcon(this, g, (d.width-icon.getIconWidth())/2, (d.height-icon.getIconHeight())/2);
		}

		class Bof extends JTable {
			public Bof() {
				super();
			}
			public Color getSelectedBackground() {
				return super.selectionBackground;
			}
		}
	}
	IconCellRenderer iconRenderer = new IconCellRenderer();
  	class CorrelAnovaBisSqrtAction extends AbstractAction {
		JFrame newFrame = null;

		CorrelAnovaBisSqrtAction() {
			super(correlAnovaBisSqrtAction);
		}
		public void actionPerformed(ActionEvent e) {
			if(anovaDesign == null) {
				GeneralUtil.showError(TablePad.this, getResourceString("loadDesign"));
				return;
			}
			if(!anovaDesign.isCompatibleWith(tableExp)) {
				GeneralUtil.showError(TablePad.this, getResourceString("designNotOK"));
				return;
			}
			if(newFrame != null && !newFrame.isDisplayable())
				newFrame = null;
			final boolean same = false;
			JPanel tmpPane = new JPanel();
			JTable table = new JTable(new AbstractTableModel() {
				public Dimension dim = new Dimension(CASE_SIZE, CASE_SIZE);
				public int getColumnCount() { return anovaDesign.getNumberOfFactors(); }
				public int getRowCount() { return anovaDesign.getNumberOfFactors();}
				public Object getValueAt(int row, int col) {
					if(row != col) {
						double[][] res = (new ANOVACompute(anovaDesign)).computeLocalBisSqrt(tableExp.getDatas(), row, col);
						return new PointIcon(dim, new AnovaListPoint(res, anovaDesign.getFactor(row)), same, false, colorTable.getColor(0));
					} else {
						return new String("");
					}
				}
				public String getColumnName(int column) {return anovaDesign.getFactor(column).getName();}
				public Class getColumnClass(int c) {return PointIcon.class;}
				public boolean isCellEditable(int row, int col) {return row != col;}
				public void setValueAt(Object aValue, int row, int column) {}
			});
			table.setDefaultRenderer(PointIcon.class, iconRenderer);
			table.setRowSelectionAllowed(false);
			JCheckBox bof = new JCheckBox();
			table.setDefaultEditor(PointIcon.class, new DefaultCellEditor(bof) {
				class showFrame extends AbstractAction {
						int row, col;
						public showFrame(int r, int c) {
							row = r; col = c;
						}
						public void actionPerformed(ActionEvent e) {
							double[][] res = (new ANOVACompute(anovaDesign)).computeLocalBisSqrt(tableExp.getDatas(), row, col);
							newFrame.setTitle(anovaDesign.getFactor(row).getName()+" / "+anovaDesign.getFactor(col).getName());
							newFrame.getContentPane().removeAll();
							newFrame.setSize(600, 600);
							PointPanel panel = new PointPanel(new AnovaListPoint(res, anovaDesign.getFactor(row)), same, colorTable, colorSpecial);
							panel.setListListener(listListener);
							panel.setShowAction(showInfo);
							newFrame.getContentPane().add(panel, BorderLayout.CENTER);
							newFrame.pack();
							newFrame.show();
							newFrame = null;
							stopCellEditing();
						}
				}
				public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
					JButton b = new JButton((Icon) value);
					b.addActionListener(new showFrame(row, col));
					return b;
				}
			});
			table.setRowHeight(CASE_SIZE);
			table.setGridColor(Color.black);
			table.setSelectionBackground(Color.white);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			table.sizeColumnsToFit(JTable.AUTO_RESIZE_OFF);
			JScrollPane scroller = new JScrollPane();
			JViewport port = scroller.getViewport();
			port.add(table);
			port.setScrollMode(JViewport.BLIT_SCROLL_MODE);
			JList rowList = new JList(anovaDesign.getLabelsIndexCopy());
			rowList.setBackground(Color.lightGray);
			rowList.setFixedCellHeight(CASE_SIZE);
			rowList.setCellRenderer(new MyCellRenderer(font));
			scroller.setRowHeaderView(rowList);
			tmpPane.add(scroller);
			if(newFrame == null) {
					newFrame = new JFrame(getResourceString("correlAnova"));
					newFrame.getContentPane().add(tmpPane, BorderLayout.CENTER);
					newFrame.pack();
					newFrame.setSize(600, 500);
					listFrame.add(newFrame);
					newFrame.addWindowListener(frameCloser);
					newFrame.show();
			} else {
				newFrame.toFront();
			}
		}
	}


  	class CorrelAnovaSqrtAction extends AbstractAction {
		JFrame newFrame = null;

		CorrelAnovaSqrtAction() {
			super(correlAnovaSqrtAction);
		}
		public void actionPerformed(ActionEvent e) {
			if(anovaDesign == null) {
				GeneralUtil.showError(TablePad.this, getResourceString("loadDesign"));
				return;
			}
			if(!anovaDesign.isCompatibleWith(tableExp)) {
				GeneralUtil.showError(TablePad.this, getResourceString("designNotOK"));
				return;
			}
			if(newFrame != null && !newFrame.isDisplayable())
				newFrame = null;
			final boolean same = false;
			JPanel tmpPane = new JPanel();
			JTable table = new JTable(new AbstractTableModel() {
				public Dimension dim = new Dimension(CASE_SIZE, CASE_SIZE);
				public int getColumnCount() { return anovaDesign.getNumberOfFactors(); }
				public int getRowCount() { return anovaDesign.getNumberOfFactors();}
				public Object getValueAt(int row, int col) {
					if(row != col) {
						double[][] res = (new ANOVACompute(anovaDesign)).computeLocalSqrt(tableExp.getDatas(), row, col);
						return new PointIcon(dim, new AnovaListPoint(res, anovaDesign.getFactor(row)), same, false, colorTable.getColor(0));
					} else {
						return new String("");
					}
				}
				public String getColumnName(int column) {return anovaDesign.getFactor(column).getName();}
				public Class getColumnClass(int c) {return PointIcon.class;}
				public boolean isCellEditable(int row, int col) {return row != col;}
				public void setValueAt(Object aValue, int row, int column) {}
			});
			table.setDefaultRenderer(PointIcon.class, iconRenderer);
			table.setRowSelectionAllowed(false);
			JCheckBox bof = new JCheckBox();
			table.setDefaultEditor(PointIcon.class, new DefaultCellEditor(bof) {
				class showFrame extends AbstractAction {
						int row, col;
						public showFrame(int r, int c) {
							row = r; col = c;
						}
						public void actionPerformed(ActionEvent e) {
							double[][] res = (new ANOVACompute(anovaDesign)).computeLocalSqrt(tableExp.getDatas(), row, col);
							newFrame.setTitle(anovaDesign.getFactor(row).getName()+" / "+anovaDesign.getFactor(col).getName());
							newFrame.getContentPane().removeAll();
							newFrame.setSize(600, 600);
							PointPanel panel = new PointPanel(new AnovaListPoint(res, anovaDesign.getFactor(row)), same, colorTable, colorSpecial);
							panel.setListListener(listListener);
							panel.setShowAction(showInfo);
							newFrame.getContentPane().add(panel, BorderLayout.CENTER);
							newFrame.pack();
							newFrame.show();
							newFrame = null;
							stopCellEditing();
						}
				}
				public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
					JButton b = new JButton((Icon) value);
					b.addActionListener(new showFrame(row, col));
					return b;
				}
			});
			table.setRowHeight(CASE_SIZE);
			table.setGridColor(Color.black);
			table.setSelectionBackground(Color.white);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			table.sizeColumnsToFit(JTable.AUTO_RESIZE_OFF);
			JScrollPane scroller = new JScrollPane();
			JViewport port = scroller.getViewport();
			port.add(table);
			port.setScrollMode(JViewport.BLIT_SCROLL_MODE);
			JList rowList = new JList(anovaDesign.getLabelsIndexCopy());
			rowList.setBackground(Color.lightGray);
			rowList.setFixedCellHeight(CASE_SIZE);
			rowList.setCellRenderer(new MyCellRenderer(font));
			scroller.setRowHeaderView(rowList);
			tmpPane.add(scroller);
			if(newFrame == null) {
					newFrame = new JFrame(getResourceString("correlAnova"));
					newFrame.getContentPane().add(tmpPane, BorderLayout.CENTER);
					newFrame.pack();
					newFrame.setSize(600, 500);
					listFrame.add(newFrame);
					newFrame.addWindowListener(frameCloser);
					newFrame.show();
			} else {
				newFrame.toFront();
			}
		}
	}


	class MyCellRenderer extends JLabel implements ListCellRenderer {
		public MyCellRenderer(Font font) {
			setBorder(BorderFactory.createEtchedBorder());
			setBackground(Color.lightGray);
			setForeground(Color.black);
			setFont(font);
			setOpaque(true);
		}
		public Component getListCellRendererComponent(JList list, Object value, int index,
		boolean isSelected,boolean cellHasFocus) {
			setText(value.toString());
			return this;
		}
	}



 	class CorrelBetweenRowsAction extends AbstractAction {
		JFrame newFrame = null;
		
		CorrelBetweenRowsAction() {
			super(correlBetweenRowsAction);
		}
		public void actionPerformed(ActionEvent e) {
			if(newFrame != null && !newFrame.isDisplayable())
				newFrame = null;
			final boolean same = true;
			JPanel tmpPane = new JPanel();
			JTable table = new JTable(new AbstractTableModel() {
				public Dimension dim = new Dimension(CASE_SIZE, CASE_SIZE);
				public int getColumnCount() { return tableExp.getNumberOfRows(); }
				public int getRowCount() { return tableExp.getNumberOfRows();}
				public Object getValueAt(int row, int col) {
					return new PointIcon(dim, new RowsListPoint(row, col), same, true, colorTable.getColor(0));
				}
				public String getColumnName(int column) {return tableExp.getRowLabels()[column];}
				public Class getColumnClass(int c) {return PointIcon.class;}
				public boolean isCellEditable(int row, int col) {return true;}
				public void setValueAt(Object aValue, int row, int column) {}
			});
			table.setDefaultRenderer(PointIcon.class, iconRenderer);
			table.setRowSelectionAllowed(false);
			JCheckBox bof = new JCheckBox();
			table.setDefaultEditor(PointIcon.class, new DefaultCellEditor(bof) {
				class showFrame extends AbstractAction {
						int row, col;
						public showFrame(int r, int c) {
							row = r; col = c;
						}
						public void actionPerformed(ActionEvent e) {
							newFrame.setTitle(getResourceString("correlTitle")+tableExp.getRowLabels()[row]+" "+getResourceString("and")+" "+tableExp.getRowLabels()[col]);
							newFrame.getContentPane().removeAll();
							PointRegPanel panel = new PointRegPanel(new RowsListPoint(row, col), same, colorTable, colorSpecial);
							panel.setListListener(listListener);
							panel.setShowAction(showInfo);
							newFrame.getContentPane().add(panel, BorderLayout.CENTER);
							newFrame.pack();
							newFrame.setSize(600, 600);
							newFrame.show();
							newFrame = null;
							stopCellEditing();
						}
				}
				public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
					JButton b = new JButton((Icon) value);
					b.addActionListener(new showFrame(row, col));
					return b;
				}
			});
			table.setRowHeight(CASE_SIZE);
			table.setGridColor(Color.black);
			table.setSelectionBackground(Color.white);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			table.sizeColumnsToFit(JTable.AUTO_RESIZE_OFF);
			JScrollPane scroller = new JScrollPane();
			JViewport port = scroller.getViewport();
			port.add(table);
			port.setScrollMode(JViewport.BLIT_SCROLL_MODE);
			JList rowList = new JList(tableExp.getRowLabels());
			rowList.setBackground(Color.lightGray);
			rowList.setFixedCellHeight(CASE_SIZE);
			rowList.setCellRenderer(new MyCellRenderer(font));
			scroller.setRowHeaderView(rowList);
			tmpPane.add(scroller);
			Object[] message = new Object[2];
			if(newFrame == null) {
					newFrame = new JFrame(getResourceString("correlRows"));
					newFrame.getContentPane().add(tmpPane, BorderLayout.CENTER);
					newFrame.pack();
					newFrame.setSize(600, 500);
					listFrame.add(newFrame);
					newFrame.addWindowListener(frameCloser);
					newFrame.show();
			} else {
				newFrame.toFront();
			}
		}
	}
	private static final int CASE_SIZE = 50;
	private static final Font font =  new Font("SansSerif", Font.PLAIN, 11);

 	class CorrelBetweenColsAction extends AbstractAction {
		JFrame newFrame = null;

		CorrelBetweenColsAction() {
			super(correlBetweenColsAction);
		}
		public void actionPerformed(ActionEvent e) {
			if(newFrame != null && !newFrame.isDisplayable())
				newFrame = null;
			final boolean same = true;
			JPanel tmpPane = new JPanel();
			JTable table = new JTable(new AbstractTableModel() {
				public Dimension dim = new Dimension(CASE_SIZE, CASE_SIZE);
				public int getColumnCount() { return tableExp.getNumberOfCols(); }
				public int getRowCount() { return tableExp.getNumberOfCols();}
				public Object getValueAt(int row, int col) {
					return new PointIcon(dim, new ColsListPoint(row, col), same, true, colorTable.getColor(0));
				}
				public String getColumnName(int column) {return tableExp.getColLabels()[column];}
				public Class getColumnClass(int c) {return PointIcon.class;}
				public boolean isCellEditable(int row, int col) {return true;}
				public void setValueAt(Object aValue, int row, int column) {}
			});
			table.setDefaultRenderer(PointIcon.class, iconRenderer);
			table.setRowSelectionAllowed(false);
			JCheckBox bof = new JCheckBox();
			table.setDefaultEditor(PointIcon.class, new DefaultCellEditor(bof) {
				class showFrame extends AbstractAction {
						int row, col;
						public showFrame(int r, int c) {
							row = r; col = c;
						}
						public void actionPerformed(ActionEvent e) {
							newFrame.setTitle(getResourceString("correlTitle")+tableExp.getColLabels()[row]+" "+getResourceString("and")+" "+tableExp.getColLabels()[col]);
							newFrame.getContentPane().removeAll();
							PointRegPanel panel = new PointRegPanel(new ColsListPoint(row, col), same, colorTable, colorSpecial);
							panel.setListListener(listListener);
							panel.setShowAction(showInfo);
							newFrame.getContentPane().add(panel, BorderLayout.CENTER);
							newFrame.pack();
							newFrame.setSize(600, 600);
							newFrame.show();
							newFrame = null;
							stopCellEditing();
						}
				}
				public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
					JButton b = new JButton((Icon) value);
					b.addActionListener(new showFrame(row, col));
					return b;
				}
			});
			table.setRowHeight(CASE_SIZE);
			table.setGridColor(Color.black);
			table.setSelectionBackground(Color.white);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			table.sizeColumnsToFit(JTable.AUTO_RESIZE_OFF);
			JScrollPane scroller = new JScrollPane();
			JViewport port = scroller.getViewport();
			port.add(table);
			port.setScrollMode(JViewport.BLIT_SCROLL_MODE);
			JList rowList = new JList(tableExp.getColLabels());
			rowList.setBackground(Color.lightGray);
			rowList.setFixedCellHeight(CASE_SIZE);
			rowList.setCellRenderer(new MyCellRenderer(font));
			scroller.setRowHeaderView(rowList);
			tmpPane.add(scroller);
			if(newFrame == null) {
					newFrame = new JFrame(getResourceString("correlCols"));
					newFrame.getContentPane().add(tmpPane, BorderLayout.CENTER);
					newFrame.pack();
					newFrame.setSize(600, 500);
					listFrame.add(newFrame);
					newFrame.addWindowListener(frameCloser);
					newFrame.show();
			} else {
				newFrame.toFront();
			}
		}
	}

	private int counterbis = 0;
	
	class CommandAction extends AbstractAction {
		CommandCluster command;
		CommandAction(CommandCluster comm) {
			super(comm.name);
			command = comm;
		}
		public void actionPerformed(ActionEvent e) {

			JRadioButton radio1 = new JRadioButton(getResourceString("clusterizeCols"));
			JRadioButton radio2 = new JRadioButton(getResourceString("clusterizeRows"));
			radio1.setSelected(true);
			ButtonGroup group1 = new ButtonGroup();
			group1.add(radio1);
			group1.add(radio2);
			Object[] message = new Object[2];
			message[0] = radio1;
			message[1] = radio2;
			if(JOptionPane.showOptionDialog(TablePad.this, message, getResourceString("clusterizeWhat"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null)==JOptionPane.OK_OPTION) {
				int type;
				if(((JRadioButton) message[0]).isSelected()) {
					type = CLUSTERIZE_COLS;
				} else {
					type = CLUSTERIZE_ROWS;
				}
				(new ComputeClusterThread(command, type)).start();
			}
		}
	}

	class CloseAction extends AbstractAction {
		CloseAction() {
			super(closeAction);
		}
		public void actionPerformed(ActionEvent e) {
			exit();
		}
	}
	
	
	class setAction extends AbstractAction {
		setAction() {
			super("set");
		}
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == null)
				return;
			String nString = ((Preferences) e.getSource()).get(Const.nbProgLabel);
			int numberOfCommands;
			if(nString == null)
				return;
			try {
				numberOfCommands = Integer.parseInt(nString);
			} catch(NumberFormatException pe) {
				return;
			}
			tabCommand = new CommandCluster[numberOfCommands];
			tabAction = new Action[numberOfCommands];
			for(int i = 0; i<numberOfCommands; i++) {
				tabCommand[i] = new CommandCluster(((Preferences) e.getSource()).get(Const.progNameLabel +i), ((Preferences) e.getSource()).get(Const.progPathLabel + i));
				tabAction[i] = new CommandAction(tabCommand[i]);
			}
			nString = ((Preferences) e.getSource()).get(Const.table);
			if(nString != null) {
				if(colorTable == null) {
					colorTable = new ColorTable(nString);
				} else {
					colorTable.setColors(nString);
				}
			} else {
				colorTable = new ColorTable(12);
			}
			nString = ((Preferences) e.getSource()).get(Const.special);
			if(nString != null) {
				if(colorSpecial == null) {
					colorSpecial = new ColorTable(nString);
				} else {
					colorSpecial.setColors(nString);
				}
			} else {
				colorSpecial= new ColorTable(5);
			}
			if((nString = ((Preferences) e.getSource()).get(Const.query)) != null) {
				showInfo.setQuery(nString);
			}
			maintainCommandsMenu();
			maintainDependant();
			repaintAllGraphics();
		}
	}

	public void repaintAllGraphics() {
		for (Enumeration e = listFrame.elements() ; e.hasMoreElements() ;) {
			((JFrame) e.nextElement()).repaint();
		}
	}
    /**
     * Really lame implementation of an exit command
     */
	class ExitAction extends AbstractAction {
		ExitAction() {
			super(exitAction);
		}

		public void actionPerformed(ActionEvent e) {
			exit();
		}
	}

	class ComputeANOVAThread extends Thread {
		boolean isOk = true;
		int order;
		public ComputeANOVAThread(int o) {
			setPriority(4);
			order = o;
		}
		public void run() {
			Task task = new Task("ANOVA"+Integer.toString(order)+currentFile.getAbsolutePath(), this, null);
			taskPad.add(task);
			ANOVACompute anovaCompute = new ANOVACompute();
			anovaCompute.setDesign(anovaDesign);
			anovaCompute.compute(tableExp.getDatas(), order);
			if(!isOk)
				return;
			if(anovaPad == null)
				anovaPad = new ANOVAPad(anovaCompute.getTableModel());
			else
				anovaPad.setModel(anovaCompute.getTableModel());
			if(anovaFrame == null) {
				anovaFrame = new JFrame("ANOVA");
				anovaFrame.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						showANOVA.actionPerformed(null);
					}
				});
				showANOVA.setEnabled(true);
				anovaFrame.getContentPane().add(anovaPad);	
				anovaFrame.pack();
				anovaFrame.setSize(800,200);
			}
			if(!anovaFrame.isVisible())
				showANOVA.actionPerformed(null);
			anovaFrame.toFront();
			taskPad.remove(task);
		}
		public void destroy() {
			isOk = false;
		}
	}
	private static final String stringTmp = System.getProperty("user.home")+File.separator+"tmp";
	class ComputeClusterThread extends Thread {
		Process curProcess;
		boolean isOk = true;
		public ComputeClusterThread(CommandCluster comm, int t) {
			setPriority(4);
			command = comm;
			type = t;
		}
		public void run() {
			File fin = new File(stringTmp+(counterbis)+".in");
			String sout = new String(stringTmp+(counterbis++)+".out");
			Task task = new Task(command.getName()+fin.getAbsolutePath(), this, null);
			taskPad.add(task);
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(fin));
				if(type == CLUSTERIZE_COLS)
					tableExp.writeDatas(out);
				else
					tableExp.writeDatasTransposed(out);
				out.close();
				isOk = true;
				curProcess = command.getProcess(fin.getAbsolutePath(), sout);
				InputStream input = curProcess.getInputStream();
				curProcess.waitFor();
				fin.delete();
				if(curProcess.exitValue() == 0) {
					File fout = new File(sout);
					if(fout.exists()) {
						TreeNode root;
						if(type == CLUSTERIZE_COLS)
							root = treeReader.read(new BufferedReader(new FileReader(fout)), tableExp.getColLabels());
						else
							root = treeReader.read(new BufferedReader(new FileReader(fout)), tableExp.getRowLabels());
						if(root != null && isOk) {
							MyImageProducer imageProducer = new MyImageProducer((DefaultMutableTreeNode) root);
							BufferedImage image = imageProducer.createImage();
							JFrame newFrame = new JFrame(command.getName()+" tree of "+getFrame().getTitle());
							newFrame.getContentPane().add(new ImagePad(image), BorderLayout.CENTER);
							newFrame.pack();
							newFrame.setSize(600, 300);
							newFrame.show();
							listFrame.add(newFrame);
						}
						fout.delete();
					}
				}
			} catch(InterruptedException iie) {
				GeneralUtil.showError(TablePad.this, "Process error : \n"+iie);
			} catch(FileNotFoundException fne) {
				GeneralUtil.showError(TablePad.this, "IO error : \n"+fne);
			} catch(IOException ioe){
				GeneralUtil.showError(TablePad.this, "IO error : \n"+ioe);
			}
			taskPad.remove(task);
		}
		public void destroy() {
			if(curProcess != null)
				curProcess.destroy();
			isOk = false;
		}
		JTextArea myOutput = new JTextArea(10,80);
		CommandCluster command;
		GeneTreeReader treeReader = new GeneTreeReader();
		int type;
	}
	
	class ReadTableThread extends Thread {
		File fin;
		boolean rowIsLab, colIsLab;
		public ReadTableThread(File f, boolean r, boolean c) {
			setPriority(4);
			fin = f;
			rowIsLab = r; colIsLab = c;
		}
		public void run() {
			canOpen = false;
			if(statusFrame == null) {
				statusFrame = new JFrame(getResourceString("inProgress"));
				progress = new JProgressBar();
				statusFrame.setSize(600, 55);
				Container fContentPane = statusFrame.getContentPane();
				fContentPane.add(progress);
			}
			progress.setString(getResourceString("reading")+" "+fin.getName());
			progress.setStringPainted(true);
			statusFrame.setVisible(true);
			progress.setMinimum(0);
			progress.setValue(0);
			progress.setMaximum((int) fin.length());
			TableReader tableReader = new TableReader();
			tableReader.setProgress(progress);
			TableExp newTableExp;
			try {
				newTableExp = tableReader.read(new BufferedReader(new FileReader(fin)), rowIsLab, colIsLab);
				if(newTableExp != null) {
					boolean bof = tableExp == null;
					disposeFrames();
					tablePanel.setTableExp(newTableExp);
					tableExp = newTableExp;
					if(bof)
						maintainDependant();
					getFrame().setTitle(fin.getAbsolutePath());
					currentFile = fin;
					if(anovaPad != null)
						anovaPad.resetModel();
					if(anovaFrame != null && anovaFrame.isVisible())
						showANOVA.actionPerformed(null);
					showANOVA.setEnabled(false);
				} else {
					GeneralUtil.showError(getFrame(), getResourceString(errorReading+messageSuffix));
				}
			} catch(IOException ioe){
				GeneralUtil.showError(TablePad.this, "IO error : \n"+ioe);
			}
			statusFrame.setVisible(false);
			canOpen = true;
		}
	}
	private static final int CLUSTERIZE_ROWS = 0;
	private static final int CLUSTERIZE_COLS = 1;
}
