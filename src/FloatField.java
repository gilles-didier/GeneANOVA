import java.lang.*;
import javax.swing.*;
import javax.swing.text.*;


/**
 * <CODE>FloatField</CODE> is just a subclass of <CODE>JTextField</CODE> to enter float values.
 *
 * @version 10 mar 2000
 * @author	Gilles Didier
 */

public class FloatField extends JTextField {

/**
 * Constructs a new <CODE>FloatField</CODE> with <CODE>cols</CODE> columns.
*/
	public FloatField (int cols) {
		super(cols);
	}

/**
 * Constructs a new <CODE>FloatField</CODE> with default number of columns.
*/
	public FloatField (String s) {
		super(s);
	}

/**
 * Overrides the standard method to filter.
*/
	protected Document createDefaultModel() {
		return new FloatDocument();
	}

	class FloatDocument extends PlainDocument {
		public void insertString(int offs, String str, AttributeSet a) 
			throws BadLocationException {
				if (str == null) {
					return;
				}
				char[] upper = str.toCharArray();
				boolean hasVirg = false;
				int k;
				char[] text = getText(0, getLength()).toCharArray();
				for(k=0; k<text.length && text[k] != '.'; k++);
				if(k<text.length)
					hasVirg = true;

				int j = 0;
				for (int i = 0; i < upper.length; i++) {
					if(offs == 0 && i==0 && upper[i] == '-')
						upper[j++] = upper[i];
					if(Character.isDigit(upper[i]))
						upper[j++] = upper[i];
					if(!hasVirg && 
					((j>0 && Character.isDigit(upper[j-1])) || (offs>0 && Character.isDigit(text[offs-1]))) &&
					(upper[i] == '.')) {
						hasVirg = true;
						upper[j++] = upper[i];
				}
			}
			super.insertString(offs, new String(upper, 0, j), a);
		}
	}
}
