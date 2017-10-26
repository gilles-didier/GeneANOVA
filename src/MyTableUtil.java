import javax.swing.table.*;
import javax.swing.JLabel;
import javax.swing.JTable;
import java.awt.Component;
import java.text.NumberFormat;
import java.io.*;

public class MyTableUtil {
	private static  NumberFormat formatter = NumberFormat.getInstance();
	
	public static void writeTableModel(BufferedWriter out, TableModel m, int n) throws IOException  {
		formatter.setMaximumFractionDigits(n);
		formatter.setMinimumFractionDigits(n);
		int nCols = m.getColumnCount();
		int nRows = m.getRowCount();
		out.write(m.getColumnName(0));
		for(int j=1; j<nCols; j++)
			out.write('\t'+m.getColumnName(j));
		out.newLine();
		for(int i=0; i<nRows; i++) {
			out.write(format(m.getValueAt(i, 0)));
			for(int j=1; j<nCols; j++)
				out.write('\t'+format(m.getValueAt(i, j)));
			out.newLine();
		}
	}
	
	public static void writeTableModel(BufferedWriter out, TableModel m) throws IOException  {
		writeTableModel(out, m, 2);
	}
	

	public static void writeJTable(BufferedWriter out, JTable t) throws IOException {
		TableModel m = t.getModel();
		int nCols = m.getColumnCount();
		int nRows = m.getRowCount();
		out.write(m.getColumnName(0));
		for(int j=1; j<nCols; j++) {
			if(t.getDefaultRenderer(t.getColumnClass(j)) instanceof JLabel) {
				out.write('\t'+m.getColumnName(j));
			}
		}
		out.newLine();
		for(int i=0; i<nRows; i++) {
			out.write(format(m.getValueAt(i, 0)));
			for(int j=1; j<nCols; j++) {
				Component c = t.getCellRenderer(i, j).getTableCellRendererComponent(t, m.getValueAt(i, j), false, false, i, j);
				if(c instanceof JLabel) {
					if(c instanceof NumberLabel) {
						out.write('\t'+suppressBlank(((JLabel) c).getText()));
					} else {
						out.write('\t'+((JLabel) c).getText());
					}
				}
			}
			out.newLine();
		}
	}

	public static void writeJTable(BufferedWriter out, JTable t, boolean w[], int forbid) throws IOException {
		if(w == null) {
			writeJTable(out, t);
			return;
		}
		TableModel m = t.getModel();
		int nCols = m.getColumnCount();
		int nRows = m.getRowCount();
		int start;
		if(forbid != 0 && t.getCellRenderer(0, 0).getTableCellRendererComponent(t, m.getValueAt(0, 0), false, false, 0, 0) instanceof JLabel) {
			out.write(m.getColumnName(0));
			start = 1;
		} else {
			out.write(m.getColumnName(1));
			start = 2;
		}
		for(int j=start; j<nCols; j++) {
			if(j != forbid && t.getCellRenderer(0, j).getTableCellRendererComponent(t, m.getValueAt(0, j), false, false, 0, j) instanceof JLabel) {
				out.write('\t'+m.getColumnName(j));
			}
		}
		out.newLine();
		for(int i=0; i<nRows; i++) {
			if(i >= w.length || w[i]) {
				Component c0 = t.getCellRenderer(i, 0).getTableCellRendererComponent(t, m.getValueAt(i, 0), false, false, i, 0);
				if(forbid != 0 && c0 instanceof JLabel) {
					if(c0 instanceof NumberLabel) {
						char[] upper = ((JLabel) c0).getText().toCharArray();
						int ind = 0;
						for(int k=0; k<upper.length; k++)
							if((upper[k]>='0' && upper[k]<='9') || upper[k]==',' || upper[k]=='.' || upper[k]=='-')
								upper[ind++] = upper[k];
						out.write(new String(upper, 0, ind));
					} else 
						out.write(((JLabel) c0).getText());
					start = 1;
				} else {
					c0 = t.getCellRenderer(i, 1).getTableCellRendererComponent(t, m.getValueAt(i, 1), false, false, i, 1);
					out.write(((JLabel) c0).getText());
					start = 2;
				}
				for(int j=start; j<nCols; j++) {
					Component c = t.getCellRenderer(i, j).getTableCellRendererComponent(t, m.getValueAt(i, j), false, false, i, j);
					if(j != forbid && c instanceof JLabel) {
						if(c instanceof NumberLabel) {
							char[] upper = ((JLabel) c).getText().toCharArray();
							int ind = 0;
							for(int k=0; k<upper.length; k++)
								if((upper[k]>='0' && upper[k]<='9') || upper[k]==',' || upper[k]=='.' || upper[k]=='-')
									upper[ind++] = upper[k];
							out.write("\t"+new String(upper, 0, ind));
						} else 
							out.write("\t"+((JLabel) c).getText());
					}
				}
				out.newLine();
			}
		}
	}
	
	public static String suppressBlank(String s) {
		char[] upper = s.toCharArray();
		int ind = 0;
		for(int k=0; k<upper.length; k++)
		if((upper[k]>='0' && upper[k]<='9') || upper[k]==',' || upper[k]=='.' || upper[k]=='-')
			upper[ind++] = upper[k];
		return new String(upper, 0, ind);
	}
	
	public static String format(Object o) {
		if(o instanceof String)
			return (String) o;
		if(o instanceof Integer)
			return formatter.format(((Integer) o).longValue());
		if(o instanceof Double)
			return formatter.format(((Double) o).doubleValue());
		if(o instanceof Float)
			return formatter.format(((Float)o).doubleValue());
		return o.toString();
	}
}
