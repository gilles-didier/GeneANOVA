import java.lang.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.util.Arrays;
import java.awt.BorderLayout;


class ListSelectorBis extends JPanel {
	JList compList;
	JTextField st;
	boolean flag = true;

	
	public ListSelectorBis(Object l[], int mode) {
		this(l, null, mode);
	}
	
	public ListSelectorBis(Object l[], ListCellRenderer lr) {
		this(l,lr, ListSelectionModel.SINGLE_SELECTION);
	}
	
	public ListSelectorBis(Object l[], ListCellRenderer lr, int mode) {
		Arrays.sort(l);
		compList = new JList(l);
		if(lr != null)
			compList.setCellRenderer(lr);
		compList.setSelectionMode(mode);
		compList.addListSelectionListener(new MyListSelectionListener());
		compList.getModel().addListDataListener(new MyListDataListener());
		st = new JTextField();
		st.setDocument(new AskDocument(compList));
		st.getDocument().addDocumentListener(new MyDocumentListener());
		setLayout(new BorderLayout());
		add("Center", new JScrollPane(compList));
		add("North", st);
	}

	public ListSelectorBis(Object l[]) {
		this(l, null);
	}

	public ListSelectorBis(DefaultListModel m, ListCellRenderer lr) {
		this(m,lr, ListSelectionModel.SINGLE_SELECTION);
	}
	
	public ListSelectorBis(DefaultListModel m, int mode) {
		this(m, null, mode);
	}
	
	public ListSelectorBis(DefaultListModel m, ListCellRenderer lr, int mode) {
		compList = new JList(m);
		if(lr != null)
			compList.setCellRenderer(lr);
		compList.setSelectionMode(mode);
		compList.addListSelectionListener(new MyListSelectionListener());
		st = new JTextField();
		st.setDocument(new AskDocument(compList));
		st.getDocument().addDocumentListener(new MyDocumentListener());
		setLayout(new BorderLayout());
		add("Center", new JScrollPane(compList));
		add("North", st);
	}
	public ListSelectorBis(DefaultListModel m) {
		this(m, null);
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
	class MyListSelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
/*			if(flag)
				st.setText((String) compList.getSelectedValue());
			else
				flag = true;
*/		}		
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
			try {
			String s = e.getDocument().getText(0, e.getDocument().getLength());
			int index;
			Object[] array = ((DefaultListModel)compList.getModel()).toArray();
			if((index = Arrays.binarySearch(array, s))<0) {
				index =(-1-index);
			}
			if(index >= compList.getModel().getSize())
				index = compList.getModel().getSize()-1;
			flag = false;
			if(index <0)
				index = 0;
			if(compList.getSelectionMode() == ListSelectionModel.SINGLE_SELECTION) {
				compList.setSelectedIndex(index);
			} else {
				int i;
				for(i=index; i<array.length && array[i].toString().startsWith(s); i++)
				;
				int[] sel = new int[i-index];
				for(i=0; i<sel.length; i++) {
					sel[i] = index+i;
				}
				compList.setSelectedIndices(sel); 
			}
			compList.ensureIndexIsVisible(index);
			} catch(BadLocationException ble) {}
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

}
