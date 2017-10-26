import java.*;
import java.lang.*;
import javax.swing.*;
import java.awt.Component;
import java.awt.Color;
import java.awt.Font;

public class TaskCellRenderer extends JPanel implements ListCellRenderer { 
	private static Font font = new Font("SansSerif", Font.PLAIN, 10);
	public TaskCellRenderer() {
		super();
		setOpaque(true);
	}
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,boolean cellHasFocus) {
		JPanel panel = new JPanel(); 
		if(value instanceof Task) {
			Task task = (Task) value;
/*			removeAll();
 			add(new JLabel(task.getName()));
			add(task.getProgressBar());
         setBackground(isSelected ? Color.red : Color.white);
         setForeground(isSelected ? Color.white : Color.black);
*/			removeAll();
			JLabel label = new JLabel(task.getName());
			label.setFont(font);
 			panel.add(label);
			if(task.getProgressBar() != null)
				panel.add(task.getProgressBar());
         panel.setBackground(isSelected ? Color.red : Color.white);
         panel.setForeground(isSelected ? Color.white : Color.black);
		}
		return panel;
	}
 }
