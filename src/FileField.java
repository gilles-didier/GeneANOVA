import java.lang.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.*;


/**
 * <CODE>FileField </CODE> is just a subclass of <CODE>JTextField</CODE> to enter positive float values.
 *
 * @version 10 mar 2000
 * @author	Gilles Didier
 */
public class FileField extends JPanel {
	private JTextField textField;
	private JButton chooseButton;
    /**
     * Constructs a new FileField.  A default model is created, the initial
     * string is null, and the number of columns is set to 0.
     */
    public FileField() {
        this(null, null, 0);
    }

    /**
     * Constructs a new FileField initialized with the specified text.
     * A default model is created and the number of columns is 0.
     *
     * @param text the text to be displayed, or null
     */
    public FileField(String text) {
        this(null, text, 0);
    }

    /**
     * Constructs a new empty FileField with the specified number of columns.
     * A default model is created and the initial string is set to null.
     *
     * @param columns  the number of columns to use to calculate 
     *   the preferred width.  If columns is set to zero, the
     *   preferred width will be whatever naturally results from
     *   the component implementation.
     */ 
    public FileField(int columns) {
        this(null, null, columns);
    }

    /**
     * Constructs a new FileField initialized with the specified text
     * and columns.  A default model is created.
     *
     * @param text the text to be displayed, or null
     * @param columns  the number of columns to use to calculate 
     *   the preferred width.  If columns is set to zero, the
     *   preferred width will be whatever naturally results from
     *   the component implementation.
     */
    public FileField(String text, int columns) {
        this(null, text, columns);
    }

/**
 * Constructs a new <CODE>FileField </CODE> with <CODE>cols</CODE> columns.
*/
	public FileField(Document doc, String text, int columns) {
		super();
		textField = new JTextField(doc, text, columns);
		chooseButton = new JButton("select");
		chooseButton.addActionListener(new ChoosePathAction());
		chooseButton.setPreferredSize(new Dimension(chooseButton.getPreferredSize().width, textField.getPreferredSize().height));
		add(textField);
		add(chooseButton);
	}
	
	class ChoosePathAction extends AbstractAction {

		ChoosePathAction() {
			super();
		}

		ChoosePathAction(String nm) {
			super(nm);
		}

		public void actionPerformed(ActionEvent e) {
			if(fileChooser == null)
				fileChooser = new JFileChooser();
			else
				fileChooser.rescanCurrentDirectory();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			File f = null;
			if(fileChooser.showOpenDialog(FileField.this) == JFileChooser.APPROVE_OPTION)	
				f = fileChooser.getSelectedFile();
			if (f == null) {
				return;
			}
			if (f.exists()) {
				textField.setText(f.getAbsolutePath());
			}
		}
	}
    /**
     * Returns the preferred size Dimensions needed for this 
     * TextField.  If a non-zero number of columns has been
     * set, the width is set to the columns multiplied by
     * the column width. 
     *
     * @return the dimensions
     *//*
    public Dimension getPreferredSize() {
            Dimension size = textField.getPreferredSize();
		size.width += chooseButton.getPreferredSize().width;
            return size;
    }*/

	private JFileChooser fileChooser;

	
	public void setText(String s) {
		textField.setText(s);
	}
	public String getText() {
		return textField.getText();
	}

}
