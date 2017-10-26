import java.awt.*;
import java.util.*;
import java.awt.List;
import java.awt.event.*;
import javax.swing.*;
import java.text.*;
import sun.awt.image.codec.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class PointPCASuperPanel extends PointPanel {
	TableExp tableExp;
	PCACompute pca;
	int display;
	PointPCAPanel pointInd, pointVar;
	JSplitPane splitPane;
	
	public PointPCASuperPanel(TableExp t, ColorTable ct, ColorTable sp) {
		tableExp = t;
		pca = new PCACompute(tableExp.getDatas());
		display = 0;
		ActionListener action = new ComboAction();
		pointInd = new PointPCAIndPanel(pca, tableExp, ct, sp, action, 0);
		pointVar = new PointPCAVarPanel(pca, tableExp, ct, sp, action, 1);
		layoutComp();
	}

	protected void layoutComp() {
		setLayout(new BorderLayout());
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pointInd, new LittleTablePad(getTable()));
		splitPane.setResizeWeight(0.80);
		splitPane.setContinuousLayout(false); 
		splitPane.setOneTouchExpandable(true); 
		add("Center", splitPane);
	}
	
	protected JTable getTable() {
		JTable table = new JTable(new AbstractTableModel() {
				public int getColumnCount() {return 1+pca.getVectors()[0].length; }
				public int getRowCount() { return 1+pca.getPercents().length;}
				public Object getValueAt(int row, int col) {
					if(col<1) {
						if(row<1) {
							return "%";
						} else {
							return tableExp.getColLabels()[row-1];
						}
					} else {
						if(row<1) {
							return new Float(pca.getPercents()[col-1]);
						} else {
							return new Double(pca.getVectors()[row-1][col-1]);
						}
					}		
				}
				public String getColumnName(int col) {
					if(col<1) {
						return "\\";
					} else {
						return Integer.toString(col);
					}		
				}
				public Class getColumnClass(int col) {return getValueAt(0, col).getClass();}
				public boolean isCellEditable(int row, int col) {return false;}
				public void setValueAt(Object aValue, int row, int column) {}
		});
		table.setIntercellSpacing(new Dimension(3,1));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableCellRenderer cellRenderer = new NumberCellRenderer(0, fractionDigitsP, fractionDigits);
		table.setDefaultRenderer(Integer.class, cellRenderer);
		table.setDefaultRenderer(Double.class, cellRenderer);
		table.setDefaultRenderer(Float.class, cellRenderer);
		return table;
	}
	
	public void setListListener(MouseListener li) {
		pointInd.setListListener(li);
		pointVar.setListListener(li);
	}
	public void setShowAction(ActionListener a) {
		pointInd.setShowAction(a);
		pointVar.setShowAction(a);
	}
	private static final int fractionDigits = 5;
	private static final int fractionDigitsP = 2;
	
	public class ComboAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(pointInd == null || pointVar == null)
				return;
			JComboBox cb = (JComboBox) e.getSource();
			if(display != cb.getSelectedIndex()) {
				display = cb.getSelectedIndex();
				if(display == 0) {
					pointInd.setAbsOrd(pointVar.abs, pointVar.ord); 
					pointInd.update();
					splitPane.setTopComponent(pointInd);
					cb.setSelectedIndex(1);
				} else {
					pointVar.setAbsOrd(pointInd.abs, pointInd.ord); 
					pointVar.update();
					splitPane.setTopComponent(pointVar);
					cb.setSelectedIndex(0);
				}
			}
		}
	}
}
			
