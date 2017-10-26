import java.lang.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.font.*;
import java.awt.Font;
import java.awt.Color;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * <CODE>NumberLabel</CODE> is just a subclass of <CODE>JTextField</CODE> to enter double values.
 *
 * @version 10 mar 2000
 * @author	Gilles Didier
 */

public class NumberLabel extends JLabel {
	private static  NumberFormat formatter = NumberFormat.getInstance();
	private static final int MAX_FRAC = 3;
	private double value;


/**
 * Constructs a new <CODE>NumberLabel</CODE> with <CODE>cols</CODE> columns.
*/
	public NumberLabel () {
		super();
		setOpaque(true);
		setForeground(Color.black);
		setFont(new Font("SansSerif", Font.PLAIN, 12));
		setValue(Double.NaN, 2);
	}
/**
 * Constructs a new <CODE>NumberLabel</CODE> with <CODE>cols</CODE> columns.
*/
	public NumberLabel (int frac) {
		this();
		setValue(Double.NaN, frac);
	}
	
/**
 * Constructs a new <CODE>NumberLabel</CODE> with <CODE>cols</CODE> columns.
*/
	public NumberLabel(Object d, int frac) {
		this();
		setValue(d, frac);
	}
/**
 * Constructs a new <CODE>NumberLabel</CODE> with <CODE>cols</CODE> columns.
*/
	public NumberLabel (double d, int frac) {
		this();
		setValue(d, frac);
	}
/**
 * Constructs a new <CODE>NumberLabel</CODE> with <CODE>cols</CODE> columns.
*/
	public NumberLabel (double d) {
		this(d, MAX_FRAC);
	}
	
	public void setFractionDigits(int n) {
		formatter.setMaximumFractionDigits(n);
		formatter.setMinimumFractionDigits(n);
	}

    public double getValue() {
        return value;
    }

	public void setValue(double value, int frac) {
		setFractionDigits(frac);
		setValue(value);
	}
	
	public void setValue(double value) {
		if(!Double.isNaN(value) && !Double.isInfinite(value)) {
			setHorizontalAlignment(RIGHT);
			setText(formatter.format(value));
		} else {
			setHorizontalAlignment(CENTER);
			if(Double.isNaN(value))
				setText("-");
			if(Double.isInfinite(value))
				setText("NS");
		}
	}
	 
	public void setValue(Object o, int frac) {
		if(o instanceof Number)
			setValue(((Number) o).doubleValue(), frac);
		else
			setValue(Double.NaN);
	}
	
	public void setValue(Object o) {
		if(o instanceof Number)
			setValue(((Number) o).doubleValue());
		else
			setValue(Double.NaN);
	}
}
