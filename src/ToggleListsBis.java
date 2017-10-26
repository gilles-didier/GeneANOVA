import java.*;
import java.lang.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.awt.image.*;
import javax.swing.event.*;


class ToggleListsBis extends JPanel {
	ListSelectorBis list1, list2;
	DefaultListModel model1 = new DefaultListModel(), model2 = new DefaultListModel();
	JButton button1To2, button2To1;
	private final int WIDTH = 90;

	
	public ToggleListsBis(Object[] l1, Object[] l2, String stringList1, String stringList2, Icon ic1, Icon ic2) {
		initModels(l1,l2);
		list1 = new ListSelectorBis(model1, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list2 = new ListSelectorBis(model2, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list1.setFixedCellWidth(WIDTH);
		list2.setFixedCellWidth(WIDTH);
		if(ic1 != null)
			button1To2 = new JButton(ic1);
		else
			button1To2 = new JButton(new ImageIcon(getClass().getResource("/resources/rightArrow.gif")));
		if(ic1 != null)
			button2To1 = new JButton(ic2);
		else
			button2To1 = new JButton(new ImageIcon(getClass().getResource("/resources/leftArrow.gif")));
		button1To2.addActionListener(move1To2);button2To1.addActionListener(move2To1);
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.Y_AXIS));
		buttonPane.add(button1To2);
		buttonPane.add(Box.createRigidArea(new Dimension(0,5)));
		buttonPane.add(button2To1);
		JPanel listPane1 = new JPanel();
		listPane1.setLayout(new BoxLayout(listPane1, BoxLayout.Y_AXIS));
		listPane1.add(new JScrollPane(list1));
		if(stringList1 != null) {
			JLabel label1 = new JLabel(stringList1);
			label1.setAlignmentX(listPane1.CENTER_ALIGNMENT);
			label1.setBorder(BorderFactory.createEtchedBorder());
			listPane1.add(label1);
			listPane1.add(Box.createRigidArea(new Dimension(0,5)));
		}
		JPanel listPane2 = new JPanel();
		listPane2.setLayout(new BoxLayout(listPane2, BoxLayout.Y_AXIS));
		listPane2.add(new JScrollPane(list2));
		if(stringList2 != null) {
			JLabel label2 = new JLabel(stringList2);
			label2.setAlignmentX(listPane2.CENTER_ALIGNMENT);
			label2.setBorder(BorderFactory.createEtchedBorder());
			listPane2.add(label2);
			listPane2.add(Box.createRigidArea(new Dimension(0,5)));
		}
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(listPane1);
		add(Box.createRigidArea(new Dimension(5,0)));
		add(buttonPane);
		add(Box.createRigidArea(new Dimension(5,0)));
		add(listPane2);
	}
	
	public ToggleListsBis(Object[] l1, Object[] l2, String stringList1, String stringList2) {
		this(l1, l2, null, null, null, null);
	}
	public ToggleListsBis(Object[] l1, Object[] l2) {
		this(l1, l2, null, null);
	}
	
	public void initModels(Object[] l1, Object[] l2) {
		if(l1 == null)
			l1 = new Object[0];
		if(l2 == null)
			l2 = new Object[0];
		Arrays.sort(l1); Arrays.sort(l2);
		model1.clear();
		model2.clear();
		for(int i=0; i<l1.length; i++)
			model1.addElement(l1[i]);
		for(int i=0; i<l2.length; i++)
			model2.addElement(l2[i]);
	}
			
	ActionListener move1To2 = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			Object[] sel = list1.getSelectedValues();
			if(sel == null)
				return;
			for(int i=0; i<sel.length; i++) {
				insertInList2(sel[i]);
				model1.removeElement(sel[i]);
			}
			list1.resetTextField();
		}
	};
	
	ActionListener move2To1 = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			Object[] sel = list2.getSelectedValues();
			if(sel == null)
				return;
			for(int i=0; i<sel.length; i++) {
				insertInList1(sel[i]);
				model2.removeElement(sel[i]);
			}
			list2.resetTextField();
		}
	};

	public void insertInList1(Object o) {
		int i;
		for(i=0; i<model1.getSize() && ((Comparable) o).compareTo(model1.get(i))>0; i++);
		model1.insertElementAt(o, i);
	}

	public void insertInList2(Object o) {
		int i;
		for(i=0; i<model2.getSize() && ((Comparable) o).compareTo(model2.get(i))>0; i++);
		model2.insertElementAt(o, i);
	}
	
	public Object[] getList1() {
		return model1.toArray();
	}
	
	public Object[] getList2() {
		return model2.toArray();
	}	
	
}
