import java.lang.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
public class Preferences extends Object {
	private static char SEP = '=';
	Hashtable h = new Hashtable();
	boolean isModified = false;
	String path = null;
	PreferencesAsker prefPane;
	Container parent;
	ActionListener setAction;
	public Preferences(){
		this(null, null, null, null);
	}
	
	public Preferences(PreferencesAsker aasker, Container aparent, String defa, ActionListener set) {
		prefPane = aasker;
		parent = aparent;
		setAction = set;
		if(defa != null)
			defaultFile = System.getProperty("user.home")+File.separator+defa;
		else
			defaultFile = System.getProperty("user.home")+File.separator+".preferences.pref";
		load(defaultFile);
	}

	protected Frame getFrame() {
		if(parent != null) {
			for (Container p = parent; p != null; p = p.getParent()) {
				if (p instanceof Frame) {
					return (Frame) p;
				}
			}
		} else {
			Frame tabFrame[] = Frame.getFrames();
			for (int i = 0; i<tabFrame.length; i++) {
				if(tabFrame[i].isEnabled())
					return tabFrame[i];
			}
			return tabFrame[tabFrame.length-1];
		}
		return null;
	}

	public void reset() {
		h.clear();
	}
	public boolean getIsModified() {
		return isModified;
	}
	public void setIsModified(boolean b) {
		isModified = b;
	}
	public void setPath(String p) {
		path = p;
	}
	public String getPath() {
		return path;
	}
	public void read(BufferedReader in){
		if(in == null)
			return;
		String curString;
		do {
			try {
				curString = in.readLine();
			} catch(IOException ioe) {
				break;
			}
			int index;
			if(curString != null && !curString.startsWith("#") && (index = curString.indexOf(SEP)) >= 1)
				h.put(curString.substring(0, index), curString.substring(index+1));
		} while(curString != null);
	}
	
	public void put(String key, String text) {
		if(key != null && text != null)
			h.put(key, text);
		isModified = true;
	}

	public String get(String key) {
		if(key != null)
			return (String) h.get(key);
		return null;
	}

	public void write(BufferedWriter out) {
		if(out == null)
			return;
		for (Enumeration e = h.keys() ; e.hasMoreElements() ;) {
			String key = (String) e.nextElement();
			String text = (String) h.get(key);
			try {
				out.write(key, 0, key.length());
				out.write(SEP);
				out.write(text, 0, text.length());
				out.newLine();
			} catch(IOException ioe) {
				break;
			}	
          }
	}

	public boolean shutdown() {
		if(isModified) {
			switch(JOptionPane.showConfirmDialog(getFrame(), "Save preferences?", "information", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE)) {
				case JOptionPane.CANCEL_OPTION :
					return false;
				case JOptionPane.NO_OPTION :
					return true;
				case JOptionPane.YES_OPTION :
					if(path != null) {
						save(path);
					} else {
						File file = askForFile(FileDialog.SAVE, "Save preferences");
						if(file != null) {
							path = file.getAbsolutePath();
							save(path);
						}
					}
				default :
					return true;
			}
		}
		return true;
	}
						
	
	public Action[] getActions() {
		return defaultActions;
	}

	private String defaultFile;
	private static final int LOAD = 0;
	private static final int SAVE = 1;

	public static final String setPreferencesAction = "setPreferences";
	public static final String loadPreferencesAction = "loadPreferences";
	public static final String savePreferencesAction = "savePreferences";
	public static final String saveAsPreferencesAction = "saveAsPreferences";
	public static final String setAsDefaultAction = "setAsDefault";

	public static final String revertPreferencesAction = "revertPreferences";

	private Action[] defaultActions = {
		new SetPreferencesAction(),
		new LoadPreferencesAction(),
		new SavePreferencesAction(),
		new SaveAsPreferencesAction(),
		new SetAsDefaultAction()
	};

	protected FileDialog fileDialog;
	protected JFileChooser fileChooser;

	class SetPreferencesAction extends AbstractAction {

 		SetPreferencesAction() {
 			super(setPreferencesAction);
		}

		public void actionPerformed(ActionEvent e) {
				Object message[] = new Object[1];
				message[0] = prefPane;
				if(JOptionPane.showOptionDialog(getFrame(), message, "Preferences", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null)==JOptionPane.OK_OPTION) {
					prefPane.set(null, Preferences.this);
					setAction.actionPerformed(new ActionEvent(Preferences.this, 0, "set"));
					setIsModified(true);
				}
		}
	}

	
	class LoadPreferencesAction extends AbstractAction {

 		LoadPreferencesAction() {
 			super(loadPreferencesAction);
		}

		public void actionPerformed(ActionEvent e) {
			File file = askForFile(LOAD, "Load preferences");
			load(file);
		}
	}

	private File askForFile(int mode, String title) {
			if(fileChooser == null) {
				fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			}
			boolean ok;
			File f = null;
			if(mode == SAVE) {
				try{
				do {
					ok = true;
					if(fileChooser.showSaveDialog(getFrame()) == JFileChooser.APPROVE_OPTION) {	
						f = fileChooser.getSelectedFile();
						if(f != null) {
							if(f.exists()) {
								if(JOptionPane.showConfirmDialog(getFrame(), new String("Exists - Replace?"), "No title", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
									ok = false;
							} else
								f.createNewFile();
						}
					}
				} while(ok == false);
				}catch (IOException ioe){
						System.out.println("IO error : \n"+ioe);
				}
			} else {
				if(fileChooser.showOpenDialog(getFrame()) == JFileChooser.APPROVE_OPTION)	
				f = fileChooser.getSelectedFile();
			}
			if (f == null || !f.exists())
				return null;
			return f;
/*
		if(fileDialog == null)
			fileDialog = new FileDialog(getFrame());
		fileDialog.setTitle(title);
		fileDialog.setMode(mode);
		fileDialog.show();
		if(fileDialog.getFile() != null)
			return fileDialog.getDirectory()+File.separator+fileDialog.getFile();
		else
			return null;
*/
	}

	public void load(String file) {
		if (file == null) {
			return;
		}
		File f = new File(file);
		if (f.exists()) {
			try {
				// try to start reading
				BufferedReader in = new BufferedReader(new FileReader(f));
				reset();
				read(in);
				in.close();
				prefPane.init(null, Preferences.this);
				setIsModified(false);
				setAction.actionPerformed(new ActionEvent(Preferences.this, 0, "set"));
				setPath(f.getAbsolutePath());
			} catch (IOException er) {
				JOptionPane.showMessageDialog(getFrame(), "Error", "Error reading the file", JOptionPane.WARNING_MESSAGE);
			}
		} else {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getResource("/resources/preferences.pref").openStream()));
				reset();
				read(in);
				in.close();
				prefPane.init(null, Preferences.this);
				setIsModified(false);
				setAction.actionPerformed(new ActionEvent(Preferences.this, 0, "set"));
				setPath(f.getAbsolutePath());
			} catch (IOException er) {
			}
		}
	}

	public void load(File f) {
		if (f == null || !f.exists()) {
			return;
		}
		try {
			// try to start reading
			BufferedReader in = new BufferedReader(new FileReader(f));
			reset();
			read(in);
			in.close();
			prefPane.init(null, Preferences.this);
			setIsModified(false);
			setAction.actionPerformed(new ActionEvent(Preferences.this, 0, "set"));
			setPath(f.getAbsolutePath());
		} catch (IOException er) {
			JOptionPane.showMessageDialog(getFrame(), "Error", "Error reading the file", JOptionPane.WARNING_MESSAGE);
		}
	}

	private void save(String file) {
		if(file == null)
			return;
		File f = new File(file);
		try {
			// try to writing
			BufferedWriter out = new BufferedWriter(new FileWriter(f));
			write(out);
			out.close();
		} catch (IOException er) {
			JOptionPane.showMessageDialog(getFrame(), "Error","Error writing the file", JOptionPane.WARNING_MESSAGE);
		}
	}
	
	private void save(File f) {
		if(f == null)
			return;
		try {
			// try to writing
			BufferedWriter out = new BufferedWriter(new FileWriter(f));
			write(out);
			out.close();
		} catch (IOException er) {
			JOptionPane.showMessageDialog(getFrame(), "Error","Error writing the file", JOptionPane.WARNING_MESSAGE);
		}
	}

	class SaveAsPreferencesAction extends AbstractAction {

 		SaveAsPreferencesAction() {
 			super(saveAsPreferencesAction);
		}

		public void actionPerformed(ActionEvent e) {
			File file = askForFile(SAVE, "Save preferences");
			if(file != null) {
				path = file.getAbsolutePath();
				save(path);
			}
		}
	}

	class SavePreferencesAction extends AbstractAction {

 		SavePreferencesAction() {
 			super(savePreferencesAction);
		}

		public void actionPerformed(ActionEvent e) {
			if(path != null) {
				save(path);
			} else {
				File file = askForFile(SAVE, "Save preferences");
				if(file != null) {
					path = file.getAbsolutePath();
					save(path);
				}
			}	
		}
	}
	class SetAsDefaultAction extends AbstractAction {

 		SetAsDefaultAction() {
 			super(setAsDefaultAction);
		}

		public void actionPerformed(ActionEvent e) {
			String file = defaultFile;
			if(file != null) {
				path = file;
				save(path);
			}	
		}
	}
		
}