import java.awt.*;
import java.io.*;
import javax.swing.*;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.Locale;


class GeneralUtil {
	static File currentDir = null;
	private static String exist;
	private static String errorTitle;
	static {
		try {
			ResourceBundle resources= ResourceBundle.getBundle("resources.GeneralUtil", Locale.getDefault());
			exist = resources.getString("exist");
			errorTitle = resources.getString("errorTitle");
		} catch (MissingResourceException mre) {
			exist = "Exists - Replace?";
			errorTitle = "Error";
		}
	}

	public static File getFileOpen(Component c) {
		File f = null;
		JFileChooser fileChooser = new JFileChooser(currentDir);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if(fileChooser.showOpenDialog(c) == JFileChooser.APPROVE_OPTION)	
			f = fileChooser.getSelectedFile();
		currentDir = fileChooser.getCurrentDirectory();
		return f;
	}
	
	public static File getFileSaveAs(Component c) {
		boolean ok;
		File f = null;
		JFileChooser fileChooser = new JFileChooser(currentDir);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		try{
			do {
				ok = true;
				if(fileChooser.showSaveDialog(c) == JFileChooser.APPROVE_OPTION) {	
					f = fileChooser.getSelectedFile();
					if(f != null) {
						if(f.exists()) {
							if(JOptionPane.showConfirmDialog(c, exist, "", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
								f = null; 
								ok = false;
							}
						} else
							f.createNewFile();
					}
				}
			} while(ok == false);
		}catch (IOException ioe){
			showError(c, "IO error : \n"+ioe);
		}
		currentDir = fileChooser.getCurrentDirectory();
		return f;
	}
	
	private static JDialog dialog;
	private static int result = 0;
	
	public static File getCustomFileSaveAs(Component c, Component toAdd) { 
		boolean ok;
		File f = null;
		dialog = new JDialog(getFrame(c), true);
		JFileChooser fileChooser = new JFileChooser(currentDir) {
			public void approveSelection() {
				result = JFileChooser.APPROVE_OPTION;
				dialog.setVisible(false);
			}
			public void cancelSelection() {
				result = JFileChooser.CANCEL_OPTION;
				dialog.setVisible(false);
			}
		};
		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		JPanel custom = new JPanel(); 
		custom.setLayout(new BoxLayout(custom, BoxLayout.Y_AXIS));
		custom.add(fileChooser);
		custom.add(toAdd);
 		dialog.getContentPane().add(custom, BorderLayout.CENTER); 
 		dialog.pack(); 
		try{
			do {
				ok = true;
  				dialog.setLocationRelativeTo(c); 
				dialog.show(); 		
				if(result == JFileChooser.APPROVE_OPTION) {	
					f = fileChooser.getSelectedFile();
					if(f != null) {
						if(f.exists()) {
							if(JOptionPane.showConfirmDialog(c, exist, "", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
								f = null; 
								ok = false;
							}
						} else
							f.createNewFile();
					}
				}
			} while(ok == false);
		}catch (IOException ioe){
			showError(c, "IO error : \n"+ioe);
		}
		currentDir = fileChooser.getCurrentDirectory();
		return f;
	}
	
	public static void showError(Component c, String message) {
		JOptionPane.showMessageDialog(c, message, errorTitle, JOptionPane.WARNING_MESSAGE);
	}
	
	public static Frame getFrame(Component c) {
		for (Container p = c.getParent(); p != null; p = p.getParent()) {
			if (p instanceof Frame) {
				return (Frame) p;
			}
		}
		return null;
    }

}
