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
import postscript.PSGr1;

public class PointRegPanel extends PointPanel {

	public PointRegPanel(ListPoint m,  ColorTable ct, ColorTable sp) {
		this(m, true, ct, sp);
	}
	
	public PointRegPanel(ListPoint m, boolean same) {
		this(m, same, null, null, null);
	}
	
	public PointRegPanel(ListPoint m, boolean same, ColorTable ct, ColorTable sp) {
		this(m, same, ct, sp, null);
	}

	public PointRegPanel(ListPoint m, boolean same, ColorTable ct, ColorTable sp, Line l[]) {
		super(m, same, ct, sp, l);
	}
	
	public PointRegPanel(ListPoint m) {
		this(m, true);
	}
	
	public PointRegPanel() {
		this(null);
	}
	
	public void finishCreate(ListPoint m, boolean same, ColorTable ct, ColorTable sp, Line l[]) {
		if(ct != null)
			colorTable = ct;
		else
			colorTable = new ColorTable(12);
		if(sp != null)
			colorSpecial = sp;
		else
			colorSpecial = new ColorTable(2);
		myListPoint = m;
		pointReg = new PointRegComponent(myListPoint, same, colorTable, colorSpecial, l, new MaintainListAction());
		modelSel.addListDataListener(new MyListDataListener());
		pointReg.setListModel(modelSel);
	}
	
	protected JToolBar getToolBar() {
		tmpToolBar = super.getToolBar();
		tmpToolBar.remove(glue);
		final Dimension dim = new Dimension(150, 40);
		final JTextArea regText = new JTextArea() {
			public Dimension getSize() { return dim; }
			public Dimension getPreferredSize() { return dim; }
			public Dimension getMaximumSize() { return dim; }
			public Dimension getMinimumSize() { return dim; }
		};
		regText.setBackground(Color.white);		
		regText.setBorder(BorderFactory.createEtchedBorder());		
		regText.setEditable(false);
		regText.setOpaque(true);
		((PointRegComponent) pointReg).setRegText(regText);
		JButton lin = new JButton(new ImageIcon(getClass().getResource("/resources/reg.gif")));
		LineAction lineAction = new LineAction(regText);
		lin.addActionListener(lineAction);
		tmpToolBar.add(Box.createHorizontalStrut(15));
		tmpToolBar.add(lin);
		((PointRegComponent) pointReg).setDrawLine(false);
		lineAction.actionPerformed(null);
		tmpToolBar.add(glue);
		return tmpToolBar;
	}
	
	private class LineAction implements ActionListener {
		final Component strut1 = Box.createHorizontalStrut(15);
		final Component strut2 = Box.createHorizontalStrut(5);
		final JButton uns = new JButton(new ImageIcon(getClass().getResource("/resources/unsel.gif")));
		Component regText;

		public LineAction(Component r) {
			uns.addActionListener(new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					((PointRegComponent) pointReg).toggleAllSelected();
					pointReg.repaint();
				}
			});
			regText = r;
		}
		
		public void actionPerformed(ActionEvent ae) {
			((PointRegComponent) pointReg).setDrawLine(!((PointRegComponent) pointReg).isDrawLine());
			pointReg.repaint();
			if(!((PointRegComponent) pointReg).isDrawLine()) {
				tmpToolBar.remove(strut1);
				tmpToolBar.remove(regText);
				tmpToolBar.remove(strut2);
				tmpToolBar.remove(uns);
				tmpToolBar.remove(glue);
			} else {
				tmpToolBar.add(strut1);
				tmpToolBar.add(regText);
				tmpToolBar.add(strut2);
				tmpToolBar.add(uns);
				tmpToolBar.add(glue);
			}
			tmpToolBar.repaint();
		}
	}	
}
