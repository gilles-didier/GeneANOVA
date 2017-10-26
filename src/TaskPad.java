import java.*;
import java.lang.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.awt.image.*;
import javax.swing.event.*;


class TaskPad extends JPanel {
	private JList list;
	private DefaultListModel model = new DefaultListModel();
	private JButton buttonDestroy;
	private static final int WIDTH = 100;

	public TaskPad(String message) {
		list = new JList(model);
		list.setFixedCellWidth(WIDTH);
		buttonDestroy = new JButton(message);
		buttonDestroy.addActionListener(destroy);
		setLayout(new BorderLayout());
		add("Center", new JScrollPane(list));
		add("South", buttonDestroy);
	}
	public TaskPad() {
		this("Remove");
	}
		
	public void add(Task t) {
		model.addElement(t);
	}

	public void remove(Task t) {
		model.removeElement(t);
	}

	public void destroyAll() {
		for(int i=0; i<model.size(); i++) {
			Task t = (Task) model.elementAt(i);
			t.destroy();
		}
		model.removeAllElements();
	}
	
	ActionListener destroy = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			Task sel = (Task) list.getSelectedValue();
			if(sel == null)
				return;
			sel.destroy();
			model.removeElement(sel);
		}
	};
	
	public int getNumberOfTasks() {
		return model.size();
	}
}
