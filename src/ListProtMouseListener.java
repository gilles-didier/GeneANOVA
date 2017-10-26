 import javax.swing.*; 
 import javax.swing.event.*; 
 import javax.swing.text.*; 
 import javax.swing.text.html.*; 
 import javax.swing.border.*; 
 import javax.accessibility.*; 
  
 import java.awt.*; 
 import java.awt.event.*; 
 import java.beans.*; 
 import java.util.*; 
 import java.io.*; 
 import java.applet.*; 
 import java.net.*; 
 
class ListProtMouseListener implements MouseListener {
//	private String prefix, suffix;
	private ActionListener showAction = null;

	public ListProtMouseListener(ActionListener s) {
		showAction = s;
	}
	public void setActionListener(ActionListener s) {
		showAction = s;
	}
	
	public void mousePressed(MouseEvent e) {
	}
	public void mouseReleased(MouseEvent e) {
	}
	public void mouseEntered(MouseEvent e) {
	}
	public void mouseExited(MouseEvent e) {
	}
	public void mouseClicked(MouseEvent e) {
		if(e.getSource() instanceof JList) {
			JList list = (JList) e.getSource();
			if(showAction != null && e.getClickCount() >= 2) {
				int index = list.locationToIndex(e.getPoint());
				if(index >=0) {
					String label = list.getModel().getElementAt(index).toString();
					showAction.actionPerformed(new ActionEvent(this, 0, label));
				}
			}
		}
	}

 }