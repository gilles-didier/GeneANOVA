import java.*;
import java.lang.*;
import javax.swing.*;
import java.awt.Component;
import java.awt.Color;
import java.awt.Font;

public class TaskPanel extends JPanel { 
	private static Font font = new Font("SansSerif", Font.PLAIN, 10);
	private Task task;
	
	public TaskPanel() {
		super();
	}
	
	public TaskPanel(Task t) {
		task = t;
		JLabel label = new JLabel(task.getName());
		label.setFont(font);
 		add(label);
		if(task.getProgressBar() != null)
			add(task.getProgressBar());
	}
	
	public Task getTask() {
		return task;
	}
 }
