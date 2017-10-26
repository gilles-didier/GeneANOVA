import java.lang.*;
import javax.swing.JProgressBar;



public class Task extends Object {
	private String name;
	private Thread thread;
	private JProgressBar progress;
	
	public Task(String n, Thread t, JProgressBar p) {
		name = n;
		thread = t;
		progress = p;
	}
	public Task(String n, Thread t) {
		this(n, t, null);
	}
	
	public String getName() {
		return name;
	}
	
	public JProgressBar getProgressBar() {
		return progress;
	}
	
	public void destroy() {
		thread.destroy();
	}
	public String toString() {
		return name;
	}
}
