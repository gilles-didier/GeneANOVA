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
import postscript.EPSGraphics;

public class PointPanel extends JPanel {
	PointComponent pointReg;
	ListPoint myListPoint;
	RealRule horRule, verRule;
	JScrollPane scrollPane;
	ListSelectorColor selList, allList;
	DefaultListModel modelSel = new DefaultListModel(), modelAll = new DefaultListModel();
	ColorTable colorTable, colorSpecial;
	protected JToolBar tmpToolBar;

	private static String scaleLabel, setBoundsLabel;

	static {
		try {
			ResourceBundle resources= ResourceBundle.getBundle("resources.PointPanel", Locale.getDefault());
			scaleLabel = resources.getString("scaleLabel");
			setBoundsLabel = resources.getString("setBoundsLabel");
		} catch (MissingResourceException mre) {
			scaleLabel = "Scale";
			setBoundsLabel = "Set bounds and scale";
		}
	}

	public PointPanel(ListPoint m,  ColorTable ct, ColorTable sp) {
		this(m, true, ct, sp);
	}
	
	public PointPanel(ListPoint m, boolean same) {
		this(m, same, null, null, null);
	}
	
	public PointPanel(ListPoint m, boolean same, ColorTable ct, ColorTable sp) {
		this(m, same, ct, sp, null);
	}

	public PointPanel(ListPoint m, boolean same, ColorTable ct, ColorTable sp, Line l[]) {
		super();
		setOpaque(true);
		if(m!=null) {
			finishCreate(m, same, ct, sp, l);
			layoutComp();
		}
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
		pointReg = new PointComponent(myListPoint, same, colorTable, colorSpecial, l, new MaintainListAction());
		modelSel.addListDataListener(new MyListDataListener());
		pointReg.setListModel(modelSel);
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
	
	public PointPanel(ListPoint m) {
		this(m, true);
	}
	
	public PointPanel() {
		this(null);
	}
	protected JPanel getGraphicPanel() {
		horRule = new RealRule(RealRule.HORIZONTAL, false, true, pointReg.getMinX(), pointReg.getMaxX(), pointReg.getScaleX(), 20);
		verRule = new RealRule(RealRule.VERTICAL, true, false, pointReg.getMinY(), pointReg.getMaxY(), pointReg.getScaleY(), 40);
		pointReg.addMouseMotionListener(new MyMouseMotionListener());
		pointReg.addMouseListener(new MyMouseListener());
		scrollPane = new JScrollPane(pointReg);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setOpaque(true);
		scrollPane.setColumnHeaderView(horRule);
		scrollPane.setRowHeaderView(verRule);
		JPanel tmpPane1 = new JPanel();
		tmpPane1.setLayout(new BoxLayout(tmpPane1, BoxLayout.X_AXIS));
		tmpPane1.add(scrollPane);
		JPanel tmpPane2 = new JPanel();
		tmpPane2.setLayout(new BoxLayout(tmpPane2, BoxLayout.Y_AXIS));
		tmpPane2.add(tmpPane1);
		return tmpPane2;
	}
	
	protected JPanel getListPanel() {
		final Dimension dimL0 = new Dimension(90, 100);
		final Dimension dimL1 = new Dimension(90, 1000);
		ActionListener action1 = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				Object[] l = (Object[]) e.getSource();
				int[] h = new int[l.length];
				if(e.getID() == ListSelectorColor.COLOR) {
					int c;
					if(l.length>0)
						c = ((LabelIndexIndexColor) l[0]).getIndexColor();
					else
						c = 0;
					for(int i=0; i<l.length; i++)
						h[i] = ((LabelIndexIndexColor) l[i]).index;
					pointReg.setColored(h, c);
					allList.repaint();
					selList.repaint();
				}
				if(e.getID() == ListSelectorColor.MARK) {
					int c;
					if(l.length>0)
						c = ((LabelIndexIndexColor) l[0]).getIndexColor();
					else
						c = 0;
					for(int i=0; i<l.length; i++)
						h[i] = ((LabelIndexIndexColor) l[i]).index;
					pointReg.setMarked(h, true);
				}
				if(e.getID() == ListSelectorColor.UNMARK) {
					int c;
					if(l.length>0)
						c = ((LabelIndexIndexColor) l[0]).getIndexColor();
					else
						c = 0;
					for(int i=0; i<l.length; i++)
						h[i] = ((LabelIndexIndexColor) l[i]).index;
					pointReg.setMarked(h, false);
				}
			}
		};
		selList = new ListSelectorColor(modelSel, new MyColorListCellRenderer(), ListSelectionModel.MULTIPLE_INTERVAL_SELECTION, action1, colorTable) {
			public Dimension getPreferredSize() {
				return dimL0;
			}
			public Dimension getMaximumSize() {
				return dimL1;
			}
			public Dimension getMinimumSize() {
				return dimL0;
			}
		};
		selList.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Sel."));
		selList.addListSelectionListener(new MyListSelectionListener());
		
		LabelIndexIndexColor tot[] = new LabelIndexIndexColor[myListPoint.size()];
		for(int i=0; i<tot.length; i++)
			tot[i] = pointReg.getLabelIndexIndexColor()[i];
		Arrays.sort(tot);
		for(int i=0; i<tot.length; i++)
			modelAll.addElement(tot[i]);
		allList = new ListSelectorColor(modelAll, new MyColorListCellRenderer2(), ListSelectionModel.MULTIPLE_INTERVAL_SELECTION, action1, colorTable) {
			public Dimension getPreferredSize() {
				return dimL0;
			}
			public Dimension getMaximumSize() {
				return dimL1;
			}
			public Dimension getMinimumSize() {
				return dimL0;
			}
		};
		allList.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Tot."));
		allList.addListSelectionListener(new MyListSelectionListener());
		JPanel tmpPane3 = new JPanel();
		tmpPane3.setLayout(new BoxLayout(tmpPane3, BoxLayout.Y_AXIS));
		tmpPane3.add(allList);
		tmpPane3.add(selList);
		return tmpPane3;
	}
	
	public LabelIndex[] getLabelsIndexCopy() {
		LabelIndex[] res = new LabelIndex[myListPoint.size()];
		for(int i = 0; i<res.length; i++)
			res[i] = new LabelIndex(myListPoint.getName(i), i);
		return res;
	}
	
	protected final Component glue = Box.createHorizontalGlue();
	
	protected JToolBar getToolBar() {
		JToolBar tmpToolBar = new JToolBar();
		JButton zplus=new JButton(new ImageIcon(getClass().getResource("/resources/zoom+.gif")));
		zplus.addActionListener(new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					Point p = scrollPane.getViewport().getViewPosition();
					scrollPane.setVisible(false);
					Point pNew = pointReg.zoomPlus(p, scrollPane.getViewport().getSize());
					scrollPane.getViewport().setViewPosition(pNew);
					horRule.setMinMaxScale(pointReg.getMinX(), pointReg.getMaxX(), pointReg.getScaleX());
					verRule.setMinMaxScale(pointReg.getMinY(), pointReg.getMaxY(), pointReg.getScaleY());
					scrollPane.setVisible(true);
					revalidate();
					horRule.repaint();
					verRule.repaint();
				}
		});
		JButton zminus=new JButton(new ImageIcon(getClass().getResource("/resources/zoom-.gif")));
		zminus.addActionListener(new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					Point p = scrollPane.getViewport().getViewPosition();
					scrollPane.setVisible(false);
					Point pNew = pointReg.zoomMinus(p, scrollPane.getViewport().getSize());
					scrollPane.getViewport().setViewPosition(pNew);
					horRule.setMinMaxScale(pointReg.getMinX(), pointReg.getMaxX(), pointReg.getScaleX());
					verRule.setMinMaxScale(pointReg.getMinY(), pointReg.getMaxY(), pointReg.getScaleY());
					scrollPane.setVisible(true);
					revalidate();
					horRule.repaint();
					verRule.repaint();
				}
		});
		JButton setup=new JButton(new ImageIcon(getClass().getResource("/resources/setup.gif")));
		setup.addActionListener(new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					Point p = scrollPane.getViewport().getViewPosition();
					Object[] message;
					if(pointReg.getSame())
						message = new Object[10];
					else
						message = new Object[12];
					message[0] = new JLabel("X minimum ("+Double.toString(pointReg.getMinimumX())+")");
					message[1] = new NumberField (true,true, pointReg.getMinimumX(), 3);
					message[2] = new JLabel("X Maximum ("+Double.toString(pointReg.getMaximumX())+")");
					message[3] = new NumberField (true,true, pointReg.getMaximumX(), 3);
					message[4] = new JLabel("Y Minimum ("+Double.toString(pointReg.getMinimumY())+")");
					message[5] = new NumberField (true,true, pointReg.getMinimumY(), 3);
					message[6] = new JLabel("Y Maximum ("+Double.toString(pointReg.getMaximumY())+")");
					message[7] = new NumberField (true,true, pointReg.getMaximumY(), 3);
					if(pointReg.getSame()) {
						message[8] = new JLabel(scaleLabel+ " ("+Double.toString(pointReg.getScaleX())+")");
						message[9] = new NumberField (true,true, pointReg.getScaleX(), 5);
					} else {
						message[8] = new JLabel(scaleLabel+" X ("+Double.toString(pointReg.getScaleX())+")");
						message[9] = new NumberField (true,true, pointReg.getScaleX(), 5);
						message[10] = new JLabel(scaleLabel+" Y ("+Double.toString(pointReg.getScaleY())+")");
						message[11] = new NumberField (true,true, pointReg.getScaleY(), 5);
					}
					if(JOptionPane.showOptionDialog(PointPanel.this, message, setBoundsLabel, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null)==JOptionPane.OK_OPTION) {
						scrollPane.setVisible(false);
						if(pointReg.getSame())
							pointReg.setMinMaxScale(
								 ((NumberField)message[1]).doubleValue(),
								 ((NumberField)message[3]).doubleValue(),
								 ((NumberField)message[5]).doubleValue(),
								 ((NumberField)message[7]).doubleValue(),
								 ((NumberField)message[9]).doubleValue(),
								 ((NumberField)message[9]).doubleValue());
						else
							pointReg.setMinMaxScale(
								 ((NumberField)message[1]).doubleValue(),
								 ((NumberField)message[3]).doubleValue(),
								 ((NumberField)message[5]).doubleValue(),
								 ((NumberField)message[7]).doubleValue(),
								 ((NumberField)message[9]).doubleValue(),
								 ((NumberField)message[11]).doubleValue());
						horRule.setMinMaxScale(pointReg.getMinX(), pointReg.getMaxX(), pointReg.getScaleX());
						verRule.setMinMaxScale(pointReg.getMinY(), pointReg.getMaxY(), pointReg.getScaleY());
						scrollPane.setVisible(true);
						horRule.repaint();
						verRule.repaint();
						revalidate();
					}
				}
		});
		JButton jpe = new JButton(new ImageIcon(getClass().getResource("/resources/save2.gif")));
		jpe.addActionListener(new SaveJpegAction());
		tmpToolBar.add(zplus);
		tmpToolBar.add(Box.createHorizontalStrut(5));
		tmpToolBar.add(zminus);
		tmpToolBar.add(Box.createHorizontalStrut(10));
		tmpToolBar.add(setup);
		tmpToolBar.add(Box.createHorizontalStrut(10));
		tmpToolBar.add(jpe);
		tmpToolBar.add(glue);
		tmpToolBar.setBorderPainted(true);
		return tmpToolBar;
	}
	
	public void setShowAction(ActionListener a) {
		pointReg.setShowAction(a);
	}
	
	public BufferedImage createImage() {
		int xPosition, yPosition, h, w, tmp, totalWidth, totalHeight, xMargin = 50, yMargin = 20;
		totalWidth = pointReg.getSize().width+xMargin;
		totalHeight = pointReg.getSize().height+yMargin;
		BufferedImage image = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, totalWidth, totalHeight);
		pointReg.draw(g, xMargin, 0);
		horRule = new RealRule(RealRule.HORIZONTAL, false, false, pointReg.getMinX(), pointReg.getMaxX(), pointReg.getScaleX(), yMargin);
		verRule = new RealRule(RealRule.VERTICAL, true, false, pointReg.getMinY(), pointReg.getMaxY(), pointReg.getScaleY(), xMargin);
		horRule.drawRule(g, xMargin, pointReg.getSize().height);
		verRule.drawRule(g, 0, 0);
		return image;
	}

 	class SaveJpegAction extends AbstractAction {
		SaveJpegAction() {
			super();
		}
		public void actionPerformed(ActionEvent e) {
			JRadioButton radioJpeg = new JRadioButton("save as jpeg");
			JRadioButton radioPS = new JRadioButton("save as eps");
			ButtonGroup group1 = new ButtonGroup();
			group1.add(radioJpeg);
			group1.add(radioPS);
     		radioJpeg.setSelected(true);
			JPanel tmpPane = new JPanel();
			tmpPane.add(radioJpeg);
			tmpPane.add(radioPS);
			File f = GeneralUtil.getCustomFileSaveAs(PointPanel.this, tmpPane);
			if(f == null)
				return;
			if (f.exists()) {
				if(radioJpeg.isSelected()) {
					try {
						FileOutputStream out = new FileOutputStream(f);
						JPEGImageEncoderImpl jpegEnc = new JPEGImageEncoderImpl(out);
						jpegEnc.encode(createImage());
						out.close();
					} catch(IOException ioe){
						GeneralUtil.showError(PointPanel.this, "IO error : \n"+ioe);
					}
					return;
				}
				if(radioPS.isSelected()) {
					try {
						int xPosition, yPosition, h, w, tmp, totalWidth, totalHeight, xMargin = 50, yMargin = 20;
						totalWidth = pointReg.getSize().width+xMargin;
						totalHeight = pointReg.getSize().height+yMargin;
						EPSGraphics g = new EPSGraphics(new Rectangle(0, 0, totalWidth, totalHeight), new FileWriter(f));
						g.setColor(Color.white);
						g.fillRect(0, 0, totalWidth, totalHeight);
						pointReg.draw(g, xMargin, 0);
						horRule = new RealRule(RealRule.HORIZONTAL, false, false, pointReg.getMinX(), pointReg.getMaxX(), pointReg.getScaleX(), yMargin);
						verRule = new RealRule(RealRule.VERTICAL, true, false, pointReg.getMinY(), pointReg.getMaxY(), pointReg.getScaleY(), xMargin);
						horRule.drawRule(g, xMargin, pointReg.getSize().height);
						verRule.drawRule(g, 0, 0);
						g.end();
					} catch(IOException ioe){
						GeneralUtil.showError(PointPanel.this, "IO error : \n"+ioe);
					}
					return;
				}
			}
		}		
	}	
	public static final int JPEG_TYPE = 0;
	public static final int EPS_TYPE = 1;
	
	class WriteImageThread extends Thread {
		File fout;
		int type;
		public WriteImageThread(File f, int t) {
			setPriority(4);
			fout = f;
			type = t;
		}
		public void run() {
			switch(type) {
				case JPEG_TYPE :
					try {
						FileOutputStream out = new FileOutputStream(fout);
						JPEGImageEncoderImpl jpegEnc = new JPEGImageEncoderImpl(out);
						jpegEnc.encode(createImage());
						out.close();
					} catch(IOException ioe){
						GeneralUtil.showError(PointPanel.this, "IO error : \n"+ioe);
					}
				case EPS_TYPE :
					try {
						int xPosition, yPosition, h, w, tmp, totalWidth, totalHeight, xMargin = 50, yMargin = 20;
						totalWidth = pointReg.getSize().width+xMargin;
						totalHeight = pointReg.getSize().height+yMargin;
						EPSGraphics g = new EPSGraphics(new Rectangle(0, 0, totalWidth, totalHeight), new FileWriter(fout));
						g.setColor(Color.white);
						g.fillRect(0, 0, totalWidth, totalHeight);
						pointReg.draw(g, xMargin, 0);
						horRule = new RealRule(RealRule.HORIZONTAL, false, false, pointReg.getMinX(), pointReg.getMaxX(), pointReg.getScaleX(), yMargin);
						verRule = new RealRule(RealRule.VERTICAL, true, false, pointReg.getMinY(), pointReg.getMaxY(), pointReg.getScaleY(), xMargin);
						horRule.drawRule(g, xMargin, pointReg.getSize().height);
						verRule.drawRule(g, 0, 0);
						g.end();
					} catch(IOException ioe){
						GeneralUtil.showError(PointPanel.this, "IO error : \n"+ioe);
					}
			}
		}
	}
	
 	class MaintainListAction extends AbstractAction {
		MaintainListAction() {
			super();
		}
		public void actionPerformed(ActionEvent e) {
			allList.repaint();
			selList.repaint();
		}
	}
	
	public void setListListener(MouseListener li) {
		allList.getList().addMouseListener(li);
		selList.getList().addMouseListener(li);
	}
	
	public JList getSelList() {
		return selList.getList();
	}
	
	public JList getAllList() {
		return allList.getList();
	}

	class MyMouseMotionListener implements MouseMotionListener {
		public void mouseDragged(MouseEvent e)	{
			mouseMoved(e);
		}
		public void mouseMoved(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			horRule.setMark(x);
			verRule.setMark(y);
		}
	}
	
	class MyMouseListener implements MouseListener {
		MyMouseMotionListener motionListener;

		public void mouseClicked(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
			setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		}

		public void mouseExited(MouseEvent e) {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}

		public void mousePressed(MouseEvent e) {
 		}

		public void mouseReleased(MouseEvent e) {
		}
	}
	
	class MyListSelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			if(!e.getValueIsAdjusting()) {
				Object[] l = ((JList) e.getSource()).getSelectedValues();
				int li[] = new int[l.length];
				for(int i=0; i<li.length; i++) {
					li[i] = ((LabelIndex) l[i]).getIndex();
				}
				if(e.getSource() == selList.getList())
					pointReg.setHigh(li);
				else
					pointReg.setSpecial(li);
			}
		}		
	}

	class MyListDataListener implements ListDataListener {
		public void contentsChanged(ListDataEvent e) {
			pointReg.resetHigh();
		}
		public void intervalAdded(ListDataEvent e) {
			pointReg.resetHigh();
		}
		public void intervalRemoved(ListDataEvent e) {
			pointReg.resetHigh();
		}
	}

	class PopupMouseListener implements MouseListener {
		private JPopupMenu popup;

	public PopupMouseListener () {
		popup = new JPopupMenu();
		popup.setLayout(new GridLayout(0,3));
		for(int i=0; i<9; i++) {
			popup.add(new JMenuItem(new ColoredSquare(Color.red)));
		}
		popup.pack();
	}
	
	public void mousePressed(MouseEvent e) {
		popup.show(PointPanel.this, e.getX(), e.getY());
	}

	public void mouseReleased(MouseEvent e) {
		popup.setVisible(false);
	}
	public void mouseEntered(MouseEvent e) {
	}
	public void mouseExited(MouseEvent e) {
	}
	public void mouseClicked(MouseEvent e) {
	}

 }
  class ColoredSquare implements Icon {
	Color color;
	public ColoredSquare(Color c) {
	    this.color = c;
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
	    Color oldColor = g.getColor();
	    g.setColor(color);
	    g.fill3DRect(x,y,getIconWidth(), getIconHeight(), true);
	    g.setColor(oldColor);
	}
	public int getIconWidth() { return 12; }
	public int getIconHeight() { return 12; }

   }
	class MyColorListCellRenderer extends JLabel implements ListCellRenderer {
		Color superColor = null;
		
		public MyColorListCellRenderer() {
			setOpaque(true);
		}
		
		public void paint(Graphics g) {
			super.paint(g);
			if(superColor != null) {
				Rectangle clip = g.getClipBounds();
				Color oldColor = g.getColor();
				g.setColor(superColor);
				g.fillRect(clip.x, clip.y, clip.width, clip.height);
				g.setColor(oldColor);
			}
		}
		
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			setText(value.toString());
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			if(value instanceof LabelIndexIndexColor && (((LabelIndexIndexColor)value).getIndexColor() != 0)) {
				Color c = colorTable.getColor(((LabelIndexIndexColor)value).getIndexColor());
				setBackground(c);
				setForeground(list.getForeground());
				if((c.getRed()+c.getGreen()+c.getBlue())>200)
					setForeground(Color.black);
				else
					setForeground(Color.white);
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			if (isSelected) {
				Color c = pointReg.getHigColor();
				superColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), 100);
			} else {
				superColor = null;
			}
         return this;
     }
	}
	class MyColorListCellRenderer2 extends JLabel implements ListCellRenderer {
		Color superColor = null;
		
		public MyColorListCellRenderer2() {
			setOpaque(true);
		}
		
		public void paint(Graphics g) {
			super.paint(g);
			if(superColor != null) {
				Rectangle clip = g.getClipBounds();
				Color oldColor = g.getColor();
				g.setColor(superColor);
				g.fillRect(clip.x, clip.y, clip.width, clip.height);
				g.setColor(oldColor);
			}
		}
		
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			setText(value.toString());
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			if(value instanceof LabelIndexIndexColor && (((LabelIndexIndexColor)value).getIndexColor() != 0)) {
				Color c = colorTable.getColor(((LabelIndexIndexColor)value).getIndexColor());
				setBackground(c);
				setForeground(list.getForeground());
				if((c.getRed()+c.getGreen()+c.getBlue())>200)
					setForeground(Color.black);
				else
					setForeground(Color.white);
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			if (isSelected) {
				Color c = pointReg.getSpeColor();
				superColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), 100);
			} else {
				superColor = null;
			}
         return this;
     }
	}

}
