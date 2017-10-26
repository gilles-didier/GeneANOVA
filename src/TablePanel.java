import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.awt.image.*;
import javax.swing.event.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.text.*;
import javax.swing.table.*;

/**
 * <CODE>TableComponent</CODE> is the component class which displays a table.
 *
 * @version 10 mar 2000
 * @author	Gilles Didier
 */

public class TablePanel extends JPanel {
	TableModel tableModel;
	TableExp tableExp;
	JTable table;
	JList rowList;
	JScrollPane scrollPane;
	int fractionDigits = 2;
	private static final int rowHeight = 17;
/**
 * Constructs a new <CODE>TableComponent</CODE>.
*/
	public TablePanel(TableExp t) {
		super();
		table = new JTable();
		table.setIntercellSpacing(new Dimension(3,1));
		if(t == null) {
			tableExp = null;
			tableModel = null;
		} else {
			tableExp = t;
			tableModel = tableExp.getTableModel();
			table.setModel(tableModel);
		}
		setUpTable();
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setRowHeight(rowHeight);
		scrollPane = new JScrollPane(table);
		rowList = new JList();
		rowList.setBackground(Color.lightGray);
		rowList.setCellRenderer(new MyCellRenderer());
		rowList.setFixedCellHeight(rowHeight);
		scrollPane.setRowHeaderView(rowList);
		rowList.setAutoscrolls(false);
		setLayout(new BorderLayout());
		add("Center", scrollPane);
	}

	public TablePanel() {
		this(null);
	}

/**
 * Constructs a new <CODE>TablePanel</CODE>.
*/
	public TablePanel(double[][] d,  String[] cL, String[] rL) {
		this(new TableExp(d, cL, rL));
	}

	public void setTableExp(TableExp te) {
		tableExp = te;
		resetTableModel();
	}	
	public void resetTableModel() {
		tableModel = tableExp.getTableModel();
		table.setModel(tableModel);
		rowList.setListData(tableExp.getRowLabels());
		table.setRowHeight(rowHeight);
		rowList.setFixedCellHeight(rowHeight);
		scrollPane.repaint();
	}
	public JList getList() {
		return rowList;
	}
	public TableExp getTableExp() {
		return tableExp;
	}	
	private void setUpTable() {
		final NumberField doubleField = new NumberField();
		doubleField.setHorizontalAlignment(JTextField.RIGHT);
		DefaultCellEditor doubleEditor = new DefaultCellEditor(doubleField) {
				public Object getCellEditorValue() {
					return new Double(doubleField.doubleValue());
				}
			};
		table.setDefaultEditor(Double.class, doubleEditor);
		table.setDefaultRenderer(Double.class, new NumberCellRenderer(fractionDigits, fractionDigits, fractionDigits));
	}
	
	class MyCellRenderer extends JLabel implements ListCellRenderer {
		public MyCellRenderer() {
			setBorder(BorderFactory.createEtchedBorder());
			setBackground(Color.lightGray);
			setForeground(Color.black);
			setOpaque(true);
		}
		public Component getListCellRendererComponent(JList list, Object value, int index,
		boolean isSelected,boolean cellHasFocus) {
			setText((String) value);
			return this;
		}
	}
	
}
