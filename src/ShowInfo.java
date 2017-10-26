import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.StringWriter;
import java.util.Vector;

public class ShowInfo implements ActionListener {
	String query[];
	
	public ShowInfo(String q) {
		setQuery(q);
	}
	
	private String separator = "%n";
	
	public void setQuery(String q) {
		query = tokenize(q, separator);;
	}
	
	private String constructQuery(String t) {
		StringWriter str = new StringWriter();
		if(query.length == 0)
			return "";
		str.write(query[0]);
		for(int i=1; i<query.length; i++) {
			str.write(t);
			str.write(query[i]);
		}
		return str.toString();
	}
	
	public void actionPerformed(ActionEvent e) {
		String label = e.getActionCommand();
		JFrame newFrame = new JFrame(label);
		newFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		newFrame.getContentPane().add(new HtmlPane(constructQuery(label)), BorderLayout.CENTER);
		newFrame.pack();
		newFrame.setSize(800, 600);
		newFrame.show();
	}
	
	static String[] tokenize(String s, String sep) {
		int next;
		int start=0;
		Vector v = new Vector();
		while((next = s.indexOf(sep, start)) > 0) {
			v.add(s.substring(start, next));
			start = next+sep.length();
		}
		v.add(s.substring(start, s.length()));
		String[] res = new String[v.size()];
		for(int i=0; i<res.length; i++)
			res[i] = (String) v.elementAt(i);
		return res;
	}
}
