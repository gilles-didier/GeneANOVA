import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.beans.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.lang.*;
import java.text.*;
import java.lang.Character;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.table.*;
import javax.swing.event.*;


public class Expression extends JPanel {
	private static TablePad tablePad; 	
    /**
     * To shutdown when run as an application.  This is a
     * fairly lame implementation.   A more self-respecting
     * implementation would at least check to see if a save
     * was needed.
     */
	protected static final class AppCloser extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			tablePad.exit();
		}
	}
	
	public static void main(String[] args) {
		try {
			String vers = System.getProperty("java.version");
			if (vers.compareTo("1.3.0") < 0) {
				System.out.println("!!!WARNING: Swing must be run with a " + "1.3.0 or higher version VM!!!");
			}
			JFrame frontFrame = new JFrame();
//			frontFrame.setTitle(noNameLabel);
			frontFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			frontFrame.setBackground(Color.lightGray);
			frontFrame.getContentPane().setLayout(new BorderLayout());
			tablePad = new TablePad();
			frontFrame.getContentPane().add("Center", tablePad);
			frontFrame.addWindowListener(new AppCloser());
			frontFrame.pack();
			frontFrame.setSize(550, 600);
			frontFrame.show();
		} catch (Throwable t) {
			System.out.println("uncaught exception: " + t);
			t.printStackTrace();
		}
	}
	
}
