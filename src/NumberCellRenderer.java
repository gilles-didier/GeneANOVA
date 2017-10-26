import javax.swing.table.TableCellRenderer;
import javax.swing.JTable;
import java.awt.Component;
import java.awt.Color;

class NumberCellRenderer extends NumberLabel implements TableCellRenderer {
	Color backGround;
	int intFractionDigits, floatFractionDigits, doubleFractionDigits;

	public NumberCellRenderer() {
		this(0, 2, 2);
	}
	
	public NumberCellRenderer(int ifr, int ffr, int dfr) {
		super();
		intFractionDigits = ifr; floatFractionDigits = ffr; doubleFractionDigits = dfr;
		backGround = (new Bof()).getSelectedBackground();
	}	
	
	public Component getTableCellRendererComponent(JTable table, Object d, boolean isSelected, boolean hasFocus,int row, int column) {
		int frac = 0;
		if(d instanceof Integer) {
			frac = intFractionDigits;
		} else {
			if(d instanceof Float) {
				frac = floatFractionDigits;
			} else {
				if(d instanceof Double) {
					frac = doubleFractionDigits;
				}
			}
		}
		setValue(d, frac);
		setBackground(isSelected ? backGround : Color.white);
		return this;
	}
	
	class Bof extends JTable {
		public Bof() {
			super();
		}
		public Color getSelectedBackground() {
			return super.selectionBackground;
		}
	}
}
