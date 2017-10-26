import java.lang.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.util.Arrays;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Component;
import java.awt.event.*;
import java.io.*;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.Color;


class ListSelectorColor extends JPanel {
	JList compList;
	JLabel number;
	JTextField st;
	boolean lock = false;
	ActionListener action;
	ColorTable colorTable;
	public static final int COLOR = 0;
	public static final int MARK = 1;
	public static final int UNMARK = 2;
	
	public ListSelectorColor(Object l[], int mode) {
		this(l, null, mode);
	}
	
	public ListSelectorColor(Object l[], ListCellRenderer lr) {
		this(l,lr, ListSelectionModel.SINGLE_SELECTION);
	}
	
	public ListSelectorColor(Object l[], ListCellRenderer lr, int mode) {
		this(l,lr, mode, null, null);
	}
	public ListSelectorColor(Object l[], ListCellRenderer lr, int mode, ActionListener a) {
		this(l,lr, mode, a, null);
	}	
	public ListSelectorColor(Object l[], ListCellRenderer lr, int mode, ActionListener a, ColorTable ct) {
		Arrays.sort(l);
		compList = new JList(l);
		if(lr != null)
			compList.setCellRenderer(lr);
		compList.setSelectionMode(mode);
		action = a ;
		if(ct != null)
			colorTable = ct;
		else
			colorTable = new ColorTable(12);
		finishCreate();
	}

	public ListSelectorColor(Object l[]) {
		this(l, null);
	}

	public ListSelectorColor(DefaultListModel m, ListCellRenderer lr) {
		this(m,lr, ListSelectionModel.SINGLE_SELECTION);
	}
	
	public ListSelectorColor(DefaultListModel m, int mode) {
		this(m, null, mode);
	}
	
	public ListSelectorColor(DefaultListModel m, ListCellRenderer lr, int mode) {
		this(m, lr, mode, null);
	}
	
	public ListSelectorColor(DefaultListModel m, ListCellRenderer lr, int mode, ActionListener a) {
		this(m, lr, mode, a, null);
	}
	
	public ListSelectorColor(DefaultListModel m, ListCellRenderer lr, int mode, ActionListener a, ColorTable ct) {
		compList = new JList(m);
		if(lr != null)
			compList.setCellRenderer(lr);
		compList.setSelectionMode(mode);
		action = a ;
		if(ct != null)
			colorTable = ct;
		else
			colorTable = new ColorTable(12);
		finishCreate();
	}
	public ListSelectorColor(DefaultListModel m) {
		this(m, null);
	}
	 

	private void finishCreate() {
		compList.addListSelectionListener(new MyListSelectionListener());
		compList.getModel().addListDataListener(new MyListDataListener());
		st = new JTextField(2);
		st.setDocument(new AskDocument(compList));
		st.getDocument().addDocumentListener(new MyDocumentListener());
		number = new JLabel(Integer.toString(compList.getModel().getSize()));
		JPanel paneTmp = new JPanel();
		paneTmp.setLayout(new BoxLayout(paneTmp, BoxLayout.X_AXIS));
		final Dimension dim = new Dimension(20, 20);
		JMenu menu = new JMenu() {
			public Dimension getPreferredSize() {
				return dim;
			}
			public Dimension getMaximumSize() {
				return dim;
			}
			public Dimension getMinimumSize() {
				return dim;
			}
		};
		menu.setIcon(new ImageIcon(getClass().getResource("/resources/menu.gif")));
		menu.setUI(new IconMenuUI());
		JMenu menuColor = colorTable.getColorMenu("Color", new AbstractAction() {
			public void actionPerformed(ActionEvent ae) {
				int color = ((ColorTable.ColorMenuItem) ae.getSource()).getIndColor();
				Object[] sel = compList.getSelectedValues();
				for(int i=0; i<sel.length; i++)
					if(sel[i] instanceof LabelIndexIndexColor) {
						((LabelIndexIndexColor) sel[i]).setIndexColor(color);
					}
//				compList.repaint();
				if(action != null)
					action.actionPerformed(new ActionEvent(sel, COLOR, null));
			}
		});
		menu.add(menuColor);
		JMenuItem item;
		item = new JMenuItem("Mark");
		item.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent ae) {
				Object[] sel = compList.getSelectedValues();
				for(int i=0; i<sel.length; i++)
					if(sel[i] instanceof LabelIndexIndexColor) {
						((LabelIndexIndexColor) sel[i]).setMark(true);
					}
//				compList.repaint();
				if(action != null)
					action.actionPerformed(new ActionEvent(sel, MARK, null));
			}
		});
		menu.add(item);
		item = new JMenuItem("Unmark");
		item.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent ae) {
				Object[] sel = compList.getSelectedValues();
				for(int i=0; i<sel.length; i++)
					if(sel[i] instanceof LabelIndexIndexColor) {
						((LabelIndexIndexColor) sel[i]).setMark(false);
					}
//				compList.repaint();
				if(action != null)
					action.actionPerformed(new ActionEvent(sel, UNMARK, null));
			}
		});
		menu.add(item);
		JMenuBar menuBar  = new JMenuBar();
		menuBar.add(menu);
		add(menuBar);
		paneTmp.add(st);
		paneTmp.add(menuBar);
		JPanel paneNumber = new JPanel();
		paneNumber.setLayout(new BoxLayout(paneNumber, BoxLayout.X_AXIS));
		JButton buttonSave = new JButton(new ImageIcon(getClass().getResource("/resources/save.gif"))) {
			public Dimension getPreferredSize() {
				return dim;
			}
			public Dimension getMaximumSize() {
				return dim;
			}
			public Dimension getMinimumSize() {
				return dim;
			}
		};
		buttonSave.addActionListener(new SaveAction());
		paneNumber.add(Box.createHorizontalGlue());
		paneNumber.add(number);
		paneNumber.add(buttonSave);
		setLayout(new BorderLayout());
		add("Center", new JScrollPane(compList));
		add("North", paneTmp);
		add("South", paneNumber);
	}

	public JList getList() {
		return compList;
	}

	public void setFixedCellWidth(int WIDTH) {
		compList.setFixedCellWidth(WIDTH);
	}
	
	public Object[] getSelectedValues() {
		return compList.getSelectedValues();
	}
	public Object getSelectedValue() {
		return compList.getSelectedValue();
	}
	public void resetTextField() {
		st.setText("");
	}



	public void addListSelectionListener(ListSelectionListener listener) {
		compList.addListSelectionListener(listener);
	}
	class MyListSelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			if(!lock)
				st.setText("");
		}		
	}
	class MyDocumentListener implements DocumentListener {
		public void insertUpdate(DocumentEvent e) {
			change(e);
		}

		public void removeUpdate(DocumentEvent e) {
			change(e);
		}

		public void changedUpdate(DocumentEvent e) {
			change(e);
		}

		protected void change(DocumentEvent e) {
			lock = true;
			try {
			String s = e.getDocument().getText(0, e.getDocument().getLength());
			if(e.getDocument().getLength() == 0)
				return;
			int index;
			Object[] array = ((DefaultListModel)compList.getModel()).toArray();
			if((index = Arrays.binarySearch(array, s))<0) {
				index =(-1-index);
			}
			if(index >= compList.getModel().getSize())
				index = compList.getModel().getSize()-1;
			if(index <0)
				index = 0;
			if(compList.getSelectionMode() == ListSelectionModel.SINGLE_SELECTION) {
				compList.setSelectedIndex(index);
			} else {
				int i;
				for(i=index+1; i<array.length && array[i].toString().startsWith(s); i++)
				;
				compList.setValueIsAdjusting(true);
				compList.setSelectionInterval(index, i-1);
				compList.setValueIsAdjusting(false);
			}
			compList.ensureIndexIsVisible(index);
			} catch(BadLocationException ble) {}
			lock = false;
		}
	}
	class MyListDataListener implements ListDataListener {
		public void intervalAdded(ListDataEvent e) {
			contentsChanged(e);
		}
		public void intervalRemoved(ListDataEvent e) {
			contentsChanged(e);
		}
		public void contentsChanged(ListDataEvent e) {
			resetTextField();		
			number.setText(Integer.toString(compList.getModel().getSize()));
		}
	}

 	class AskDocument extends PlainDocument {
		JList list;
		public AskDocument(JList l) {
			super();
			list =l;
		}
		public void remove(int offs, int len) throws BadLocationException {
			if(offs+len == getLength())
				super.remove(offs, len);
		}
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
			if (str == null || str.length() == 0 || offs != getLength()) {
				return;
			}
			String ancStr = new String(getText(0, getLength()));
			ancStr = ancStr.concat(str);
			if(isOk(ancStr))
				super.insertString(offs, str, a);
		}
		public boolean isOk(String ancStr) {
			int index;
			if(list.getModel().getSize() == 0) {
				return false;
			}
			if((index = Arrays.binarySearch(((DefaultListModel)list.getModel()).toArray(), ancStr))<0) {
				index =(-1-index);
			}
			if(index >= list.getModel().getSize() && index>0)
				index = list.getModel().getSize()-1;
			return ((list.getModel().getElementAt(index)).toString().startsWith(ancStr));
		}
	}
	JFileChooser fileChooser;
 	class SaveAction extends AbstractAction {
		SaveAction() {
			super();
		}
		public void actionPerformed(ActionEvent e) {
			if(fileChooser == null)
				fileChooser = new JFileChooser();
			else
				fileChooser.rescanCurrentDirectory();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			boolean ok;
			File f = null;
			try{
				do {
					ok = true;
					if(fileChooser.showSaveDialog(ListSelectorColor.this) == JFileChooser.APPROVE_OPTION) {	
						f = fileChooser.getSelectedFile();
						if(f != null) {
							if(f.exists()) {
								if(JOptionPane.showConfirmDialog(ListSelectorColor.this, "Exists -Replace?", "", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
									f = null; 
									ok = false;
								}
							} else
								f.createNewFile();
							}
					}
				} while(ok == false);
			}catch (IOException ioe){
			}
			if(f == null)
				return;

			if (f.exists()) {
				try {
					BufferedWriter out = new BufferedWriter(new FileWriter(f));
					ListModel m = compList.getModel();
					for(int i=0; i<m.getSize(); i++) {
						out.write(m.getElementAt(i).toString());
						out.newLine();
					}
					out.close();
				} catch(IOException ioe){
//					JOptionPane.showMessageDialog(TablePad.this, "IO error : \n"+ioe, getResourceString(errorTitle), JOptionPane.WARNING_MESSAGE);
				}
			}
		}		
	}
	
	
}
