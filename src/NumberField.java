import java.lang.*;
import javax.swing.*;
import javax.swing.text.*;
//java.lang.NumberFormatException

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * <CODE>NumberField</CODE> is just a subclass of <CODE>JTextField</CODE> to enter double values.
 *
 * @version 10 mar 2000
 * @author	Gilles Didier
 */

public class NumberField extends JTextField {
	private static final int MAX_FRAC = 6;
	private static final int COL = 10;
	private boolean canFloat, canNeg;

/**
 * Constructs a new <CODE>NumberField</CODE> with <CODE>cols</CODE> columns.
*/
	public NumberField () {
		this(true, true);
	}
/**
 * Constructs a new <CODE>NumberField</CODE> with <CODE>cols</CODE> columns.
*/
	public NumberField (boolean f, boolean n) {
		this(f, n, new Integer(0), MAX_FRAC);
	}
	
	public NumberField (boolean f, boolean n, Number d) {
		this(f, n, d, MAX_FRAC);
	}

/**
 * Constructs a new <CODE>NumberField</CODE> with <CODE>cols</CODE> columns.
*/
	public NumberField (boolean f, boolean n, Number d, int frac) {
		super(10);
		canFloat =f; canNeg = n;
		if(canFloat) {
			setValue(d.doubleValue());
		} else {
			setValue(d.intValue());
		}
	}

/**
 * Constructs a new <CODE>NumberField</CODE> with <CODE>cols</CODE> columns.
*/
	public NumberField(boolean f, boolean n, int d, int frac) {
		super(10);
		canFloat =f; canNeg = n;
		setValue(d);
	}

/**
 * Constructs a new <CODE>NumberField</CODE> with <CODE>cols</CODE> columns.
*/
	public NumberField (boolean f, boolean n, double d, int frac) {
		super(10);
		canFloat =f; canNeg = n;
		setValue(d);
	}

/**
 * Overrides the standard method to filter.
*/
	protected Document createDefaultModel() {
		return new DoubleDocument();
	}

	class DoubleDocument extends PlainDocument {
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
			if (str == null) {
				return;
			}
			char[] upper = str.toCharArray();
			boolean hasVirg = false, localCanNeg, localCanVirg, hasE = false;
			int k, ePos = -1, virgPos = -1;
			char[] text = getText(0, getLength()).toCharArray();
			for(k=0; k<text.length && (text[k] != '.'); k++);
			if(k<text.length) {
				hasVirg = true;
				virgPos = k;
			}
			for(k=0; k<text.length && (text[k] != 'E'); k++);
			if(k<text.length) {
				hasE = true;
				ePos = k;
			}
			if(hasE && ePos<offs)
				hasVirg = true;
			if(hasVirg && virgPos<offs)
				hasE = true;
			int j = 0;
			for (int i = 0; i < upper.length; i++) {
				if(Character.isDigit(upper[i]))
					upper[j++] = upper[i];
				if(canNeg && offs == 0 && (j==0) && upper[i] == '-')
					upper[j++] = upper[i];
				if(canFloat && ((j==0 && offs == (ePos+1)) || (j>0 && upper[j-1] == 'E')) && upper[i] == '-')
					upper[j++] = upper[i];
				if(canFloat && !hasE && ((j>0 && Character.isDigit(upper[j-1])) || (offs>0 && Character.isDigit(text[offs-1]))) && (upper[i] == 'E' || upper[i] == 'e')) {
					hasE = true;
					hasVirg = true;
					upper[j++] = 'E';
				}
				if(canFloat && !hasVirg && ((j>0 && Character.isDigit(upper[j-1])) || (offs>0 && Character.isDigit(text[offs-1]))) && ((upper[i] == '.') || (upper[i] == ','))) {
					hasVirg = true;
					upper[j++] = '.';
				}
			}
			super.insertString(offs, new String(upper, 0, j), a);
		}
	}
	
	public double doubleValue() {
        double retVal = 0f;
        try {
            retVal = Double.parseDouble(getText());
        } catch (NumberFormatException e) {
        }
        return retVal;
   }
	
	public double floatValue() {
		double retVal = 0f;
		try {
           retVal = Float.parseFloat(getText());
       } catch (NumberFormatException e) {
       }
		return retVal;
	}

	public int intValue() {
        int retVal = 0;
		try {
           retVal = Integer.parseInt(getText());
       } catch (NumberFormatException e) {
       }
		return retVal;
	}

    public void setValue(double value) {
        setText(Double.toString(value));
    }
    public void setValue(int value) {
       setText(Integer.toString(value));
    }
}
