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

public abstract class PointPCAPanel extends PointPanel {
	protected TableExp tableExp;
	protected PCACompute pca;
	public int abs, ord, display;
	protected PointPCAPanel pointInd, pointVar;
	protected ActionListener comboAction;
	private static String indLabel, varLabel, dispLabel;
	static {
		try {
			ResourceBundle resources= ResourceBundle.getBundle("resources.PointPCAPanel", Locale.getDefault());
			indLabel = resources.getString("indLabel");
			varLabel = resources.getString("varLabel");
			dispLabel = resources.getString("dispLabel");
		} catch (MissingResourceException mre) {
			indLabel = "Lines";
			varLabel = "Columns";
			dispLabel = "Display";
		}
	}
	
	public PointPCAPanel(PCACompute p, TableExp t, ColorTable ct, ColorTable sp, ActionListener cA, int di) {
		tableExp = t;
		pca = p;
		abs = 0; ord = 1;
		comboAction = cA;
		display = di;
		finishCreate(getPCAListPoint(), true, false, ct, sp, null);
		layoutComp();
	}
	
	public void finishCreate(ListPoint m, boolean same, boolean reg, ColorTable ct, ColorTable sp, Line l[]) {
		if(ct != null)
			colorTable = ct;
		else
			colorTable = new ColorTable(12);
		if(sp != null)
			colorSpecial = sp;
		else
			colorSpecial = new ColorTable(2);
		myListPoint = m;
		modelSel.addListDataListener(new MyListDataListener());
	}
	
	public void setAbsOrd(int a, int o) {
		abs = a; ord = o;
		comboAbs.setSelectedIndex(abs); 
		comboOrd.setSelectedIndex(ord);
	}
	protected void layoutComp() {
		setLayout(new BorderLayout());
		JPanel tmpPane4 = new JPanel();
		tmpPane4.setLayout(new BorderLayout());
		tmpPane4.add("North", getToolBar());
		tmpPane4.add("Center", getGraphicPanel());
		add("Center", tmpPane4);
		add("East", getListPanel());
	}
	
	protected JComponent getTable() {
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
						return "Order";
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
		return new JScrollPane(table);
	}
	protected JComboBox comboAbs, comboOrd;	
	private static final int fractionDigits = 5;
	private static final int fractionDigitsP = 2;
	
	protected JToolBar getToolBar() {
		tmpToolBar = super.getToolBar();
		tmpToolBar.remove(glue);
		tmpToolBar.add(Box.createHorizontalStrut(15));
		double[] val = pca.getPercents();
		String[] valString = new String[val.length];
		NumberFormat nf = new DecimalFormat();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		for(int i=0; i<val.length; i++)
			valString[i] = Integer.toString(i+1)+" : "+nf.format(val[i])+"%";
		comboAbs = new JComboBox(valString);
		comboAbs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				if(abs != cb.getSelectedIndex()) {
					abs = cb.getSelectedIndex();
					update();
				}
			}
		});
		comboOrd = new JComboBox(valString);
		comboOrd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				if(ord != cb.getSelectedIndex()) {
					ord = cb.getSelectedIndex();
					update();
				}
			}
		});
		comboAbs.setSelectedIndex(abs);
		comboOrd.setSelectedIndex(ord);
		comboAbs.setPreferredSize(comboDim);
		comboAbs.setMinimumSize(comboDim);
		comboAbs.setMaximumSize(comboDim);
		comboOrd.setPreferredSize(comboDim);
		comboOrd.setMinimumSize(comboDim);
		comboOrd.setMaximumSize(comboDim);
		String dispString[] = new String[2];
		dispString[0] = indLabel;
		dispString[1] = varLabel;
		JComboBox comboDisp = new JComboBox(dispString);
		comboDisp.addActionListener(comboAction);
		comboDisp.setSelectedIndex(display);
		comboDisp.setPreferredSize(comboDim);
		comboDisp.setMinimumSize(comboDim);
		comboDisp.setMaximumSize(comboDim);

		tmpToolBar.add(new JLabel("X :"));		
		tmpToolBar.add(comboAbs);
		tmpToolBar.add(Box.createHorizontalStrut(10));
		tmpToolBar.add(new JLabel("Y :"));		
		tmpToolBar.add(comboOrd);
		tmpToolBar.add(Box.createHorizontalStrut(10));
		tmpToolBar.add(new JLabel(dispLabel));		
		tmpToolBar.add(comboDisp);
		tmpToolBar.add(glue);
		tmpToolBar.setFloatable(false);
		return tmpToolBar;
	}
	
	private static final Dimension comboDim = new Dimension(110, 30);
	
	public void update() {
		updatePCAListPoint();
		scrollPane.setVisible(false);
		horRule.setMinMaxScale(pointReg.getMinX(), pointReg.getMaxX(), pointReg.getScaleX());
		verRule.setMinMaxScale(pointReg.getMinY(), pointReg.getMaxY(), pointReg.getScaleY());
		scrollPane.getViewport().setViewPosition(new Point(0,0));
		scrollPane.setVisible(true);
		revalidate();
		horRule.repaint();
		verRule.repaint();
		pointReg.repaint();
		modelSel.clear();
	}
	
	protected abstract PCAListPoint getPCAListPoint();

	protected abstract void updatePCAListPoint();
	
	protected class PCAListPoint extends ListPoint {
		double[][] coord;
		double[] corr;
		String[] name;
		public PCAListPoint(double[][] r, double[] c, String[] n) {
			coord = r; corr = c; name = n;
		}
		public double getX(int indice) {
			return coord[indice][0];
		}
		public double getY(int indice) {
			return coord[indice][1];
		}
		public String getName(int indice) {
			return name[indice];
		}
		public int size() {
			return coord.length;
		}
	
		private static final String cLabel = "c";
		protected String getText(int indice) {
			StringWriter str = new StringWriter();
			str.write(super.getText(indice));
			str.write("<tt><font size=-2><p>");
			str.write(cLabel);
			str.write(" = ");
			str.write(cFormatter.format(corr[indice]));
			str.write("</font></tt>");
			return str.toString();
		}
	}
}

