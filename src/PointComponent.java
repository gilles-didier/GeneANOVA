import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.io.StringWriter;
import java.awt.font.FontRenderContext;

public class PointComponent extends JComponent implements Scrollable {
	protected ListPoint myListPoint;	
	protected int [][][] gridPoint;
	protected byte pointType[];
	protected int selected[], specialList[], nbSel, highList[], nbHigh;
	protected double maximumX, minimumX ,maximumY, minimumY, ecartX, ecartY, scaleX, scaleY, revScaleX, revScaleY;
	protected int margeX = 10, margeY = 10, sizeX, sizeY;
	protected int gridSize = 50;
	private double resolution = 4;
	protected static final int DEF_SIZE = 500;
	protected static final int POINT_SIZE = 8;
	protected static final int POINT_SIZE_BIS = 6;
	protected static final byte NOR = 0;
	protected static final byte SPE = 1;
	protected static final byte HIG = 1 << 1;
	protected static final byte COL = 1 << 2;
	private Dimension dim;
	private Rectangle2D.Double selRect = null;
	protected Rectangle curRect = new Rectangle();
	private DefaultListModel listModel;
	private LabelIndexIndexColor[] labelIndexColor;
	protected TreeSet coloredSet = new TreeSet(), markedSet = new TreeSet();
	protected ColorTable colorTable, colorSpecial;
	protected Line line[] = null;
//	private Font font = new Font("SansSerif", Font.PLAIN, 9);
	private Font font = new Font("Monospaced", Font.BOLD, 10);
	private FontRenderContext frc = new FontRenderContext(null, false, false);
	private ActionListener maintainAction;
	private boolean same;
	private Ellipse2D.Double[] ellipse;
	
	public PointComponent(ListPoint m, boolean egalite_scale, ColorTable ct, ColorTable spe) {
		this(m, egalite_scale, ct, spe, null, null);
	}

	public PointComponent(ListPoint m, boolean egalite_scale, ColorTable ct, ColorTable spe, ActionListener mA) {
		this(m, egalite_scale, ct, spe, null, mA);
	}

	public PointComponent(ListPoint m, boolean egalite_scale, ColorTable ct, ColorTable spe, Line[] l) {
		this(m, egalite_scale, ct, spe, l, null);
	}
	
	public PointComponent(ListPoint m, boolean egalite_scale, ColorTable ct, ColorTable spe, Line[] l, ActionListener mA) {
		setBackground(Color.white);
		setOpaque(true);
		setAutoscrolls(true);
		same = egalite_scale;
		colorTable = ct;
		colorSpecial = spe;
		maintainAction = mA;
		setListPoint(m);
		setLines(l);
		setToolTipText("Point zone");
		MyMouseInputAdapter myMouseInputAdapter = new MyMouseInputAdapter();
		addMouseListener(myMouseInputAdapter);
		addMouseMotionListener(myMouseInputAdapter);
	}
	
	public void setEllipses(Ellipse2D.Double e[]) {
		ellipse = e;
	}
	
	public void setMinMax(double minX, double maxX, double minY, double maxY) {
		minimumX = minX;
		maximumX = maxX;
		minimumY = minY;
		maximumY = maxY;
		ecartX = maximumX-minimumX;
		ecartY = maximumY-minimumY;
		scaleX = ((double) DEF_SIZE)/ecartX;
		scaleY = ((double) DEF_SIZE)/ecartY;
		initTab();
		if(same) {
			double min = Math.min(scaleX,scaleY);
			setScale(min, min);
		} else {
			setScale(scaleX, scaleY);
		}
	}

	public void setMinMaxScale(double minX, double maxX, double minY, double maxY, double scX, double scY) {
		minimumX = minX;
		maximumX = maxX;
		minimumY = minY;
		maximumY = maxY;
		ecartX = maximumX-minimumX;
		ecartY = maximumY-minimumY;
		initTab();
		setScale(scX, scY);
	}

	public void setListPoint(ListPoint m) {
		myListPoint = m;
		if(myListPoint.size() == 0)
			return;
		labelIndexColor = new LabelIndexIndexColor[myListPoint.size()];
		pointType = new byte[myListPoint.size()];
		selected =  new int[myListPoint.size()];
		nbSel = 0;
		for(int i=0; i<pointType.length; i++) {
			pointType[i] = NOR;
			labelIndexColor[i] = new LabelIndexIndexColor(myListPoint.getName(i), i);
		}
		findBounds();
		initTab();
		scaleX = ((double) DEF_SIZE)/ecartX;
		scaleY = ((double) DEF_SIZE)/ecartY;
		if(same) {
			double min = Math.min(scaleX,scaleY);
			setScale(min, min);
		} else {
			setScale(scaleX, scaleY);
		}
	}
	
	
	public void setListPointBis(ListPoint m) {
		if(myListPoint.size() != m.size())
			return;
		myListPoint = m;
		selRect = null;
		if(highList != null) {
			for(int i=0; i<highList.length; i++) {
				pointType[highList[i]] &= SPE;
			}
		}
		findBounds();
		initTab();
		scaleX = ((double) DEF_SIZE)/ecartX;
		scaleY = ((double) DEF_SIZE)/ecartY;
		if(same) {
			double min = Math.min(scaleX,scaleY);
			setScale(min, min);
		} else {
			setScale(scaleX, scaleY);
		}
	}
	
	public void setLines(Line[] l) {
		line = l;		
	}
	
	public LabelIndexIndexColor[] getLabelIndexIndexColor() {
		return labelIndexColor;
	}
	
	public Color getHigColor() {
		return colorSpecial.getColor(0);
	}
	public Color getSpeColor() {
		return colorSpecial.getColor(1);
	}
	public void setListModel(DefaultListModel m) {
		listModel = m;
	}
	
	public void setColored(int h[], int c) {
		if(h != null) {
			Graphics g = getGraphics();
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			if(c == 0) {
				g.setColor(colorTable.getColor(0));
				for(int i=0; i<h.length; i++) {
					coloredSet.remove(new Integer(h[i]));
					if(drawPointTest(g, h[i])) {
						Shape sha = g.getClip();
						g.setClip(curRect.x, curRect.y, curRect.width, curRect.height);
						g.setXORMode(getBackground());
						g.setColor(colorSpecial.getColor(3));
						drawPoint(g, h[i]);
						g.setClip(sha);
						g.setPaintMode();
					}	
				}
			} else {
				g.setColor(colorTable.getColor(c));
				for(int i=0; i<h.length; i++) {
					coloredSet.add(new Integer(h[i]));
					if(drawPointTest(g, h[i])) {
						Shape sha = g.getClip();
						g.setClip(curRect.x, curRect.y, curRect.width, curRect.height);
						g.setXORMode(getBackground());
						g.setColor(colorSpecial.getColor(3));
						drawPoint(g, h[i]);
						g.setClip(sha);
						g.setPaintMode();
					}	
				}
			}
			paintColored(g);
			paintSpecial(g);
			paintHigh(g);
			paintLines(g);
		}
	}
	public boolean getSame() {
		return same;
	}
	
	public void setColored(int h, int c) {
			Graphics g = getGraphics();
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			if(c == 0) {
				g.setColor(colorTable.getColor(0));
				coloredSet.remove(new Integer(h));
				if(drawPointTest(g, h)) {
					Shape sha = g.getClip();
					g.setClip(curRect.x, curRect.y, curRect.width, curRect.height);
					g.setXORMode(getBackground());
					g.setColor(colorSpecial.getColor(3));
					drawPoint(g, h);
					g.setClip(sha);
					g.setPaintMode();
				}	
			} else {
				g.setColor(colorTable.getColor(c));
				coloredSet.add(new Integer(h));
				if(drawPointTest(g, h)) {
					Shape sha = g.getClip();
					g.setClip(curRect.x, curRect.y, curRect.width, curRect.height);
					g.setXORMode(getBackground());
					g.setColor(colorSpecial.getColor(3));
					drawPoint(g, h);
					g.setClip(sha);
					g.setPaintMode();
				}
			}
			paintSpecial(g);
			paintHigh(g);
			paintLines(g);
	}
	
	public void setMarked(int h[], boolean b) {
		if(h != null) {
			Graphics g = getGraphics();
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			if(b) {
				for(int i=0; i<h.length; i++) {
					Integer inte = new Integer(h[i]);
					if(!markedSet.contains(inte)) {
						markedSet.add(inte);
						paintMark(g, h[i]);
					}
				}
			} else {
				for(int i=0; i<h.length; i++) {
					Integer inte = new Integer(h[i]);
					if(markedSet.contains(inte)) {
						markedSet.remove(inte);
						paintMark(g, h[i]);
					}
				}
			}
		}
	}

	public void setMarked(int h, boolean b) {
		if(!b) {
			markedSet.remove(new Integer(h));
		} else {
			markedSet.add(new Integer(h));
		}
		Graphics g = getGraphics();
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		paintMark(g, h);
	}
		
	public void setHigh(int h[]) {
		Graphics g = getGraphics();
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if(highList != null) {
			for(int i=0; i<highList.length; i++) {
				pointType[highList[i]] &= SPE;
			}
			eraseHigh(g);
		}
		highList = h;
		if(highList != null) {
			for(int i=0; i<highList.length; i++) {
				pointType[highList[i]] |= HIG;
			}
		}
		paintColored(g);
		paintSpecial(g);
		paintHigh(g);
		paintLines(g);
	}
	
	public void resetHigh() {
		if(highList != null) {
			for(int i=0; i<highList.length; i++) {
				pointType[highList[i]] &= SPE;
			}
			eraseHigh(getGraphics());
			highList = null;
			paintSpecial(getGraphics());
		}
	}
	
	public void setSpecial(int l[]) {
		Graphics g = getGraphics();
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if(specialList != null) {
			for(int i=0; i<specialList.length; i++) {
				pointType[specialList[i]] &= HIG;
			}
			eraseSpecial(g);
		}
		specialList = l;
		if(specialList != null) {		
			for(int i=0; i<specialList.length; i++) {
				pointType[specialList[i]] |= SPE;
			}
		}
		paintColored(g);
		paintSpecial(g);
		paintHigh(g);
		paintLines(g);
	}

	public void resetSpecial() {
		if(specialList != null) {
			for(int i=0; i<specialList.length; i++) {
				pointType[specialList[i]] &= HIG;
			}
 			eraseSpecial(getGraphics());
			specialList = null;
			paintHigh(getGraphics());
		}
	}

	private void findBounds() {
		maximumX = myListPoint.getX(0);
		minimumX = myListPoint.getX(0);
		maximumY = myListPoint.getY(0);
		minimumY = myListPoint.getY(0);
		for(int i=1; i<myListPoint.size(); i++) {
			double temp = myListPoint.getX(i);
			if(temp>maximumX) {
				maximumX = temp;
			}		
			if(temp<minimumX) {
				minimumX = temp;	
			}
			temp = myListPoint.getY(i);
			if(temp>maximumY) {
				maximumY = temp;
			}	
			if(temp<minimumY) {
				minimumY = temp;	
			}
		}
		ecartX = maximumX-minimumX;
		ecartY = maximumY-minimumY;
	}
	
	public int getCaseX(double x) {
		int caseX = (int)((((double) gridSize)*(x-minimumX))/ecartXBis);
		return  caseX;
	}
	
	public int getCaseY(double y) {
		int caseY = (int)((((double) gridSize)*(y-minimumY))/ecartYBis);
		return  caseY;
	}
	public double getMinimumX() {
		return minimumX;
	}
	public double getMaximumX() {
		return maximumX;
	}
	public double getMinimumY() {
		return minimumY;
	}
	public double getMaximumY() {
		return maximumY;
	}

	public double getScaleX() {
		return scaleX;
	}
	public double getMinX() {
		return screenToPointX(0);
	}
	public double getMaxX() {
		return screenToPointX(sizeX);
	}
	public double getScaleY() {
		return scaleY;
	}
	public double getMinY() {
		return screenToPointY(sizeY);
	}
	public double getMaxY() {
		return screenToPointY(0);
	}
	
	private double ecartXBis, ecartYBis;
	private void initTab() {
		int caseX;
		int caseY;
		ecartXBis = (ecartX*((double) gridSize))/((double) (gridSize-1));
		ecartYBis = (ecartY*((double) gridSize))/((double) (gridSize-1));
		gridPoint = new int[gridSize][gridSize][];
		int tmp[][] = new int[gridSize][gridSize];
		for(int i=0; i<gridSize; i++)
			for(int j=0; j<gridSize; j++)
				tmp[i][j] = 0;
		for(int i=0; i<myListPoint.size(); i++) {
			caseX = getCaseX(myListPoint.getX(i));
			caseY = getCaseY(myListPoint.getY(i));
			if(caseX>=0 && caseX<gridSize && caseY>=0 && caseY<gridSize)
				tmp[caseX][caseY]++;
		}
		for(int i=0; i<gridSize; i++)
			for(int j=0;j<gridSize;j++) {
				gridPoint[i][j] = new int[tmp[i][j]];
				tmp[i][j] = 0;
			}
		for(int i=0; i<myListPoint.size(); i++) {
			caseX = getCaseX(myListPoint.getX(i));
			caseY = getCaseY(myListPoint.getY(i));
			if(caseX>=0 && caseX<gridSize && caseY>=0 && caseY<gridSize)
				gridPoint[caseX][caseY][tmp[caseX][caseY]++] = i;
		}
	}

	public void setScale(double sX, double sY) {
		double xSelStart = 0, xSelEnd = 0, ySelStart = 0, ySelEnd = 0;
		scaleX = sX; scaleY = sY;
		revScaleX = 1/scaleX; revScaleY = 1/scaleY;
		sizeX = ((int)(ecartX*scaleX))+2*margeX;
		sizeY = ((int)(ecartY*scaleY))+2*margeY;
		dim = new Dimension(sizeX, sizeY);		
		setSize(dim);		
		setPreferredSize(dim);	
		setMaximumSize(dim);	
		setMinimumSize(dim);	
		if(selRect != null) {
			curRect.setRect(pointToScreenX(selRect.x), pointToScreenY(selRect.y), (int) (scaleX*selRect.width), (int) (scaleY*selRect.height));
		}
	}
	
	public void zoomPlus() {
		setScale(scaleX*2.0, scaleY*2.0);
	}
	
	public void zoomMinus() {
		setScale(scaleX/2.0, scaleY/2.0);
	}
	
	public Point zoomPlus(Point p, Dimension d) {
		double xCenter = (screenToPointX(p.x)+screenToPointX(p.x+d.width)+revScaleX)/2;
		double yCenter = (screenToPointY(p.y)+screenToPointY(p.y+d.height)+revScaleY)/2;
		setScale(scaleX*2.0, scaleY*2.0);
		int xN;
		int yN = pointToScreenY(yCenter)-d.height/2;
		if(sizeX<d.width) {
			xN = 0;
		} else {
			xN = pointToScreenX(xCenter)-d.width/2;
			if(xN<0) xN=0;
			if(xN+d.width>=sizeX) xN=sizeX-d.width-1;
		}
		if(sizeY<d.height) {
			yN = 0;
		} else {
			yN = pointToScreenY(yCenter)-d.height/2;
			if(yN<0) yN=0;
			if(yN+d.height>=sizeY) yN=sizeY-d.height-1;
		}
		return new Point(xN, yN);
	}
	
	public Point zoomMinus(Point p, Dimension d) {
		double xCenter = (screenToPointX(p.x)+screenToPointX(p.x+d.width)+revScaleX)/2;
		double yCenter = (screenToPointY(p.y)+screenToPointY(p.y+d.height)+revScaleY)/2;
		setScale(scaleX/2.0, scaleY/2.0);
		int xN = pointToScreenX(xCenter)-d.width/2;
		int yN = pointToScreenY(yCenter)-d.height/2;
		if(sizeX<d.width) {
			xN = 0;
		} else {
			xN = pointToScreenX(xCenter)-d.width/2;
			if(xN<0) xN=0;
			if(xN+d.width>=sizeX) xN=sizeX-d.width-1;
		}
		if(sizeY<d.height) {
			yN = 0;
		} else {
			yN = pointToScreenY(yCenter)-d.height/2;
			if(yN<0) yN=0;
			if(yN+d.height>=sizeY) yN=sizeY-d.height-1;
		}
		return new Point(xN, yN);
	}
	
	public Dimension getSize() {
		return dim;
	}

	public Dimension getPreferredSize() {
		return dim;
	}
	public Dimension getMaximumSize() {
		return dim;
	}
	public Dimension getMinimumSize() {
		return dim;
	}
	
	protected int pointToScreenX(double x) {
		return ((int) ((x-minimumX)*scaleX))+margeX;
	}
	
	protected int pointToScreenY(double y) {
		return sizeY-((int) ((y-minimumY)*scaleY))-margeY;
	}

	protected double screenToPointX(int x) {
		return ((double) (x-margeX))/scaleX+minimumX;
	}

	protected double screenToPointY(int y) {
		return ((double) (sizeY-y-margeY))/scaleY+minimumY;
	}
	
	public void paint(Graphics g) {
		Rectangle clip = g.getClipBounds();
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.white);
		g.fillRect(clip.x, clip.y, clip.width, clip.height);
		int startX = getCaseX(screenToPointX(clip.x));
		int endX = getCaseX(screenToPointX(clip.x+clip.width)+revScaleX);
		int startY = getCaseY(screenToPointY(clip.y+clip.height));	
		int endY = getCaseY(screenToPointY(clip.y)+revScaleY);
		if(startX<0)
			startX = 0;
		if(startY<0)
			startY = 0;
		if(endX>=gridSize)
			endX = gridSize-1;
		if(endY>=gridSize)
			endY = gridSize-1;
		g.setColor(colorTable.getColor(0));
		for(int i=startX; i<=endX; i++)
			for(int j=startY; j<=endY; j++)
				for(int k=0; k<gridPoint[i][j].length; k++) {
					if(labelIndexColor[gridPoint[i][j][k]].getIndexColor() == 0) {
						drawPoint(g, gridPoint[i][j][k]);
					}
				}				
		if(selRect != null) {
			g.setXORMode(getBackground());
			g.setColor(colorSpecial.getColor(3));
			g.fillRect(curRect.x, curRect.y, curRect.width, curRect.height);
			g.setPaintMode();
		}
		paintColored(g);
		paintSpecial(g);
		paintHigh(g);
		paintEllipses(g);
		paintLines(g);
		paintMarked(g);
	}
	
	public void paintLines(Graphics g) {
		if(line != null) {
			g.setPaintMode();
			g.setColor(colorSpecial.getColor(2));
			for(int i=0; i<line.length; i++)
				paintLine(g, line[i]);
		}
	}

	public void paintLine(Graphics g, Line l) {
		if(l != null) {
			Rectangle clip = g.getClipBounds();
			if(!Double.isInfinite(l.a)) {
				double xPS1 = screenToPointX(0);
				double xPE1 = screenToPointX(sizeX);
				double yPS1 = l.a*xPS1+l.b;
				double yPE1 = l.a*xPE1+l.b;
				double yPS2 = screenToPointY(sizeY);
				double yPE2 = screenToPointY(0);
				double xPS2 = (yPS2-l.b)/l.a;
				double xPE2 = (yPE2-l.b)/l.a;
				double xS, xE, yS, yE;
				if(yPS1 < yPS2) {
					xS = xPS2; yS = yPS2;
				} else {
					xS = xPS1; yS = yPS1;
				}
				if(yPE1 > yPE2) {
					xE = xPE2; yE = yPE2;
				} else {
					xE = xPE1; yE = yPE1;
				}
				if(yE<yPS2) {
					xE = xPS2; yE = yPS2;
				}
				if(yS>yPE2) {
					xS = xPE2; yS = yPE2;
				}
				g.drawLine(pointToScreenX(xS), pointToScreenY(yS), pointToScreenX(xE), pointToScreenY(yE));
				if(l.orientation != Line.NONE) {
					double	a = -(scaleY*l.a)/scaleX, 
								b = -scaleY*(-l.b+((double)margeY-sizeY)/scaleY-minimumY+l.a*(-(((double)margeX))/scaleX+minimumX));
					double x1, y1, x2, y2;
					if(l.orientation == Line.FORWARD) {
						x1 = (xE-minimumX)*scaleX+((double)margeX);
 						y1 = ((double) sizeY)-(((yE-minimumY)*scaleY))-((double)margeY);
						x2 = ((double) x1)-((double) ARROW)/Math.sqrt(1+a*a);
						y2 = a*((double)x2)+b;
					} else {
						x1 = (xS-minimumX)*scaleX+((double)margeX);
 						y1 = ((double) sizeY)-(((yS-minimumY)*scaleY))-((double)margeY);
						x2 = ((double) x1)+((double) ARROW)/Math.sqrt(1+a*a);
						y2 = a*((double)x2)+b;
					}
					int xA = (int) Math.round(x2 + ((double) ARROW)/Math.sqrt(1+1/(a*a)));
					int yA = (int) Math.round((1/a)*(-((double) xA)+x2)+y2);
					int xB = (int) Math.round(x2 - ((double) ARROW)/Math.sqrt(1+1/(a*a)));
					int yB = (int) Math.round((1/a)*(-((double) xB)+x2)+y2);
					g.drawLine((int) x1, (int) y1, xA, yA); g.drawLine((int) x1, (int) y1, xB, yB);
				}
			} else {
				double xS = l.b;
				double xE = l.b;
				double yS = screenToPointY(sizeY);
				double yE = screenToPointY(0);
				g.drawLine(pointToScreenX(xS), pointToScreenY(yS), pointToScreenX(xE), pointToScreenY(yE));
				if(l.orientation != Line.NONE) {
					int xA, yA, xB, yB, x1, y1;
					if(l.orientation == Line.FORWARD) {
						x1 = pointToScreenX(xE);
						y1 = pointToScreenY(yE);
						xA = x1 - ARROW;
						yA = y1 - ARROW;
						xB = x1 + ARROW;
						yB = y1 - ARROW;
					} else {
						x1 = pointToScreenX(xS);
						y1 = pointToScreenY(yS);
						xA = x1 - ARROW;
						yA = y1 - ARROW;
						xB = x1 + ARROW;
						yB = y1 - ARROW;
					}
					g.drawLine(x1, y1, xA, yA); g.drawLine(x1, y1, xB, yB);
				}
			}
		}
	}

	public static final int ARROW = 7;

	public void paintEllipses(Graphics g) {
		if(ellipse != null) {
			g.setPaintMode();
			g.setColor(colorSpecial.getColor(2));
			for(int i=0; i<ellipse.length; i++)
				paintEllipse(g, ellipse[i]);
		}
	}

	public void paintEllipse(Graphics g, Ellipse2D.Double e) {
		if(e != null) {
			int xS = pointToScreenX(e.x);
			int yS = pointToScreenY(e.y);
			int w = (int) Math.round(e.width*scaleX);
			int h = (int) Math.round(e.height*scaleY);
			g.drawOval(xS, yS, w, h);
		}
	}
	
	public void paintColored(Graphics g) {
		for(Iterator e = coloredSet.iterator() ; e.hasNext() ;) {
			Integer key = (Integer) e.next();
			int i = ((Integer) key).intValue();
			g.setColor(colorTable.getColor(labelIndexColor[i].getIndexColor()));
			drawPoint(g, i);
		}
	}
	public void paintMarked(Graphics g) {
		g.setColor(colorSpecial.getColor(4));
		for(Iterator e = markedSet.iterator() ; e.hasNext() ;) {
			paintMark(g, ((Integer) e.next()).intValue());
		}
	}

	public void DrawRectangle(Graphics g) {
		g.setXORMode(getBackground());
		g.setColor(colorSpecial.getColor(3));
		g.fillRect(curRect.x, curRect.y, curRect.width, curRect.height);
		g.setPaintMode();
	}
	
	public void paintSpecial(Graphics g) {
		if(specialList != null) {
			for(int i=0; i<specialList.length; i++) {
				if(pointType[specialList[i]] == SPE) {
					g.setColor(colorSpecial.getColor(1));
					drawPoint(g, specialList[i]);
				}
			}
		}
	}

	public void eraseSpecial(Graphics g) {
		if(specialList != null) {
			for(int i=0; i<specialList.length; i++) {
				if(labelIndexColor[specialList[i]].getIndexColor() == 0)
					g.setColor(colorTable.getColor(0));
				else
					g.setColor(colorTable.getColor(labelIndexColor[specialList[i]].getIndexColor()));
				if(drawPointTest(g, specialList[i])) {
					Shape sha = g.getClip();
					g.setClip(curRect.x, curRect.y, curRect.width, curRect.height);
					g.setXORMode(getBackground());
					g.setColor(colorSpecial.getColor(3));
					drawPoint(g, specialList[i]);
					g.setClip(sha);
					g.setPaintMode();
				}	
			}
		}
	}

	public void paintHigh(Graphics g) {
		if(highList != null) {
			for(int i=0; i<highList.length; i++) {
				g.setColor(colorSpecial.getColor(0));
				drawPoint(g, highList[i]);	
			}
		}
	}
	
	public void eraseHigh(Graphics g) {
		if(highList != null) {
			for(int i=0; i<highList.length; i++) {
				if(labelIndexColor[highList[i]].getIndexColor() == 0)
					g.setColor(colorTable.getColor(0));
				else
					g.setColor(colorTable.getColor(labelIndexColor[highList[i]].getIndexColor()));
				drawPoint(g, highList[i]);	
				Shape sha = g.getClip();
				g.setClip(curRect.x, curRect.y, curRect.width, curRect.height);
				g.setXORMode(getBackground());
				g.setColor(colorSpecial.getColor(3));
				drawPoint(g, highList[i]);
				g.setClip(sha);
				g.setPaintMode();
			}
		}
	}
	
	void paintMark(Graphics g, int i) {
		g.setXORMode(getBackground());
		g.setColor(colorSpecial.getColor(4));
		g.setFont(font);
		String text = myListPoint.getName(i);
		Rectangle2D rect;
		rect = font.getStringBounds(text, frc);
		int h = (int) (rect.getHeight());
 		int w = (int) (rect.getWidth());
		g.drawString(text, pointToScreenX(myListPoint.getX(i))+DEMI_POINT_SIZE, pointToScreenY(myListPoint.getY(i))+h-1);
		g.setPaintMode();
	}
	
	protected void setColor(Graphics g, int i) {
		switch(pointType[i]) {
			case NOR :
				if(labelIndexColor[i].getIndexColor() == 0)
					g.setColor(colorTable.getColor(0));
				else
					g.setColor(colorTable.getColor(labelIndexColor[i].getIndexColor()));
				return;
			case SPE :
				g.setColor(colorSpecial.getColor(1));
				return;
			default :
				g.setColor(colorSpecial.getColor(0));
				return;
		}
	}
	
	public void draw(Graphics g, int xOffset, int yOffset) {
		if(selRect != null) {
			g.setXORMode(getBackground());
			g.setColor(colorSpecial.getColor(3));
			g.fillRect(curRect.x+xOffset, curRect.y+yOffset, curRect.width, curRect.height);
			g.setPaintMode();
		}
		g.setColor(colorTable.getColor(0));
		for(int i=0; i<myListPoint.size(); i++) {
			drawPointBis(g, i, xOffset, yOffset);	
		}
		for(Iterator e = coloredSet.iterator() ; e.hasNext() ;) {
			Integer key = (Integer) e.next();
			int i = ((Integer) key).intValue();
			g.setColor(colorTable.getColor(labelIndexColor[i].getIndexColor()));
			drawPointBis(g, i, xOffset, yOffset);
		}
		drawLines(g, xOffset, yOffset);
		drawEllipses(g, xOffset, yOffset);
		drawMarked(g, xOffset, yOffset);
	}

	public void drawMarked(Graphics g, int xOffset, int yOffset) {
		g.setColor(colorSpecial.getColor(4));
		for(Iterator e = markedSet.iterator() ; e.hasNext() ;) {
			drawMark(g, ((Integer) e.next()).intValue(), xOffset, yOffset);
		}
	}

	void drawMark(Graphics g, int i, int xOffset, int yOffset) {
		g.setXORMode(getBackground());
		g.setColor(colorSpecial.getColor(4));
		g.setFont(font);
		String text = myListPoint.getName(i);
		Rectangle2D rect;
		rect = font.getStringBounds(text, frc);
		int h = (int) (rect.getHeight());
 		int w = (int) (rect.getWidth());
		g.drawString(text, pointToScreenX(myListPoint.getX(i))+DEMI_POINT_SIZE_BIS+xOffset, pointToScreenY(myListPoint.getY(i))+h-1+yOffset);
		g.setPaintMode();
	}
	
	
	public boolean isSpecial(int i) {
		if(pointType[i] == SPE)
			return true;
		else
			return false;
	}
	

	protected void drawPoint(Graphics g, int i) {
		int x = pointToScreenX(myListPoint.getX(i));
		int y = pointToScreenY(myListPoint.getY(i));
		g.fillOval(x-DEMI_POINT_SIZE, y-DEMI_POINT_SIZE, POINT_SIZE, POINT_SIZE);
	}
	
	protected void drawPointTer(Graphics g, int i) {
		int x = pointToScreenX(myListPoint.getX(i));
		int y = pointToScreenY(myListPoint.getY(i));
		g.fillOval(x-DEMI_POINT_SIZE+1, y-DEMI_POINT_SIZE+1, POINT_SIZE-2, POINT_SIZE-2);
	}
	
	protected final int DEMI_POINT_SIZE = POINT_SIZE/2;
	protected boolean drawPointTest(Graphics g, int i) {
		int x = pointToScreenX(myListPoint.getX(i));
		int y = pointToScreenY(myListPoint.getY(i));
		int xs = x-DEMI_POINT_SIZE, ys = y-DEMI_POINT_SIZE;
		g.fillOval(xs, ys, POINT_SIZE, POINT_SIZE);
		if(curRect != null)
			return curRect.intersects(new Rectangle(xs, ys, POINT_SIZE, POINT_SIZE));
		else
			return false;
	}
	
	protected final int DEMI_POINT_SIZE_BIS = POINT_SIZE_BIS/2;
	protected void drawPointBis(Graphics g, int i, int xOffset, int yOffset) {
		int x = pointToScreenX(myListPoint.getX(i))+xOffset;
		int y = pointToScreenY(myListPoint.getY(i))+yOffset;
		g.fillOval(x-DEMI_POINT_SIZE_BIS, y-DEMI_POINT_SIZE_BIS, POINT_SIZE_BIS, POINT_SIZE_BIS);
	}
	
	public void drawLines(Graphics g, int xOffset, int yOffset) {
		if(line != null) {
			g.setColor(colorSpecial.getColor(2));
			for(int i=0; i<line.length; i++)
				drawLine(g, line[i], xOffset, yOffset);
		}
	}

	public void drawLine(Graphics g, Line l, int xOffset, int yOffset) {
		if(l != null) {
			Rectangle clip = g.getClipBounds();
			if(!Double.isInfinite(l.a)) {
				double xPS1 = screenToPointX(0);
				double xPE1 = screenToPointX(sizeX);
				double yPS1 = l.a*xPS1+l.b;
				double yPE1 = l.a*xPE1+l.b;
				double yPS2 = screenToPointY(sizeY);
				double yPE2 = screenToPointY(0);
				double xPS2 = (yPS2-l.b)/l.a;
				double xPE2 = (yPE2-l.b)/l.a;
				double xS, xE, yS, yE;
				if(yPS1 < yPS2) {
					xS = xPS2; yS = yPS2;
				} else {
					xS = xPS1; yS = yPS1;
				}
				if(yPE1 > yPE2) {
					xE = xPE2; yE = yPE2;
				} else {
					xE = xPE1; yE = yPE1;
				}
				if(yE<yPS2) {
					xE = xPS2; yE = yPS2;
				}
				if(yS>yPE2) {
					xS = xPE2; yS = yPE2;
				}
				g.drawLine(pointToScreenX(xS)+xOffset, pointToScreenY(yS)+yOffset, pointToScreenX(xE)+xOffset, pointToScreenY(yE)+yOffset);
				if(l.orientation != Line.NONE) {
					double	a = -(scaleY*l.a)/scaleX, 
								b = -scaleY*(-l.b+((double)margeY-sizeY)/scaleY-minimumY+l.a*(-(((double)margeX))/scaleX+minimumX));
					double x1, y1, x2, y2;
					if(l.orientation == Line.FORWARD) {
						x1 = (xE-minimumX)*scaleX+((double)margeX);
 						y1 = ((double) sizeY)-(((yE-minimumY)*scaleY))-((double)margeY);
						x2 = ((double) x1)-((double) ARROW)/Math.sqrt(1+a*a);
						y2 = a*((double)x2)+b;
					} else {
						x1 = (xS-minimumX)*scaleX+((double)margeX);
 						y1 = ((double) sizeY)-(((yS-minimumY)*scaleY))-((double)margeY);
						x2 = ((double) x1)+((double) ARROW)/Math.sqrt(1+a*a);
						y2 = a*((double)x2)+b;
					}
					int xA = (int) Math.round(x2 + ((double) ARROW)/Math.sqrt(1+1/(a*a)));
					int yA = (int) Math.round((1/a)*(-((double) xA)+x2)+y2);
					int xB = (int) Math.round(x2 - ((double) ARROW)/Math.sqrt(1+1/(a*a)));
					int yB = (int) Math.round((1/a)*(-((double) xB)+x2)+y2);
					g.drawLine((int) x1+xOffset, (int) y1+yOffset, xA+xOffset, yA+yOffset); g.drawLine((int) x1+xOffset, (int) y1+yOffset, xB+xOffset, yB+yOffset);
				}
			} else {
				double xS = l.b;
				double xE = l.b;
				double yS = screenToPointY(sizeY);
				double yE = screenToPointY(0);
				g.drawLine(pointToScreenX(xS)+xOffset, pointToScreenY(yS)+yOffset, pointToScreenX(xE)+xOffset, pointToScreenY(yE)+yOffset);
				if(l.orientation != Line.NONE) {
					int xA, yA, xB, yB, x1, y1;
					if(l.orientation == Line.FORWARD) {
						x1 = pointToScreenX(xE);
						y1 = pointToScreenY(yE);
						xA = x1 - ARROW;
						yA = y1 - ARROW;
						xB = x1 + ARROW;
						yB = y1 - ARROW;
					} else {
						x1 = pointToScreenX(xS);
						y1 = pointToScreenY(yS);
						xA = x1 - ARROW;
						yA = y1 - ARROW;
						xB = x1 + ARROW;
						yB = y1 - ARROW;
					}
					g.drawLine(x1+xOffset, y1+yOffset, xA+xOffset, yA+yOffset);
					g.drawLine(x1+xOffset, y1+yOffset, xB+xOffset, yB+yOffset);
				}
			}
		}
	}
	public void drawEllipses(Graphics g, int xOffset, int yOffset) {
		if(ellipse != null) {
			g.setPaintMode();
			g.setColor(colorSpecial.getColor(2));
			for(int i=0; i<ellipse.length; i++)
				drawEllipse(g, ellipse[i], xOffset, yOffset);
		}
	}

	public void drawEllipse(Graphics g, Ellipse2D.Double e, int xOffset, int yOffset) {
		if(e != null) {
			int xS = pointToScreenX(e.x)+xOffset;
			int yS = pointToScreenY(e.y)+yOffset;
			int w = (int) Math.round(e.width*scaleX);
			int h = (int) Math.round(e.height*scaleY);
			g.drawOval(xS, yS, w, h);
		}
	}
	
	public int findPoint (Point p) {
		return findPoint(p.x, p.y);
	}
	
	private ActionListener showAction = null;
	
	public void setShowAction(ActionListener a) {
		showAction = a;
	}
	
	public int findPoint (int x, int y) {
		double xP = screenToPointX(x);
		double yP = screenToPointY(y);
		int startX = getCaseX(xP);
		int endX = getCaseX(xP+revScaleX);
		int startY = getCaseY(yP);	
		int endY = getCaseY(yP+revScaleY);
		if(startX<0)
			startX = 0;
		if(startY<0)
			startY = 0;
		if(endX>=gridSize)
			endX = gridSize-1;
		if(endY>=gridSize)
			endY = gridSize-1;
		double X = xP+revScaleX/2;
		double Y = yP+revScaleY/2;
		double min = Double.MAX_VALUE;
		int ind = -1;
		for(int i=startX; i<=endX; i++)
			for(int j=startY; j<=endY; j++)
				for(int k=0; k<gridPoint[i][j].length; k++) {
					int indTmp = gridPoint[i][j][k];
					double distmp = Math.sqrt((X-myListPoint.getX(indTmp))*(X-myListPoint.getX(indTmp))+(Y-myListPoint.getY(indTmp))*(Y-myListPoint.getY(indTmp)));
					if(distmp<=min) {
						min = distmp;
						ind = indTmp;
					}
				}
		if(ind >= 0) {
			double dX = x-pointToScreenX(myListPoint.getX(ind));
			double dY = y-pointToScreenY(myListPoint.getY(ind));
			if(Math.sqrt(dX*dX+dY*dY) > POINT_SIZE)
			 ind = -1;
		}
		return ind;
	}

	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 10;
	}
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 1;
	}

	public String getToolTipText(MouseEvent event) {
		int ind = findPoint(event.getX(), event.getY());
		if(ind >= 0)
			return myListPoint.getDescriptor(ind);
		else {
			if(line != null) {
				int minInd = -1;
				double min = 0;
				for(int i=0; i<line.length; i++) {
					double b = 1/scaleY, 
						a = line[i].a/scaleX, 
						c = -line[i].b+((double)margeY-sizeY)/scaleY-minimumY+line[i].a*(-(((double)margeX))/scaleX+minimumX);
					double distLine = Math.abs(((double)event.getY())*b+a*((double)event.getX())+c)/Math.sqrt(a*a+b*b);
					if(minInd<0 || min>distLine) {
						min = distLine;
						minInd = i;
					}
				}

				if(minInd>=0 && min <= minDistLine && line[minInd ].name != null) {
					return line[minInd].getDescriptor();	
				}
			}
		}
		return null;
	}
	

	private static final double minDistLine = 2;

	public JPopupMenu getPopup(int i) {
		JPopupMenu p = new JPopupMenu();
		JMenuItem item;
		if(!labelIndexColor[i].getMark())
			item = new JMenuItem("Mark");
		else
			item = new JMenuItem("Unmark");
		item.addActionListener(new ShowLabel(i));
		item.setEnabled(true);
		p.add(item);
		JMenu colorMenu = colorTable.getColorMenu("Color", new SetColor(i));
		p.add(colorMenu);
		p.add(new JLabel(myListPoint.getName(i)));
		return p;
	}
	
	class ShowLabel extends AbstractAction {
		int indice;
		ShowLabel(int i) {
			super();
			indice = i;
		}
		public void actionPerformed(ActionEvent e) {
			labelIndexColor[indice].setMark(!labelIndexColor[indice].getMark());
			setMarked(indice, labelIndexColor[indice].getMark());
		}
	}
	
	class SetColor extends AbstractAction {
		int indice;
		SetColor(int i) {
			super();
			indice = i;
		}
		public void actionPerformed(ActionEvent e) {
			int color = ((ColorTable.ColorMenuItem) e.getSource()).getIndColor();
			labelIndexColor[indice].setIndexColor(color);
			setColored(indice, color);
			if(maintainAction != null)
				maintainAction.actionPerformed(null);
		}
	}
	
	class MyMouseInputAdapter extends MouseInputAdapter {
		private int x1,x2,y1,y2;

		public MyMouseInputAdapter() {
			super();
		}
		
		public void mouseClicked(MouseEvent me) {
			if(me.getModifiers() == MouseEvent.BUTTON1_MASK){
				if(me.getClickCount() == 1) {
					if(selRect != null) {
						Graphics g = getGraphics();
						((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						g.setXORMode(getBackground());
						g.setColor(colorSpecial.getColor(3));
						g.fillRect(curRect.x, curRect.y, curRect.width, curRect.height);
						g.setPaintMode();
						selRect = null;
						repaint();
					}
				} else {
					if(showAction != null) {
						int ind = findPoint(me.getPoint());
						if(ind >= 0) {
							showAction.actionPerformed(new ActionEvent(this, 0, myListPoint.getName(ind)));
						}
					}
				}
			}
		}
		
		public void mousePressed(MouseEvent me) {
			if(me.getModifiers() == MouseEvent.BUTTON1_MASK) {
				dragRectFlag = true;
				x1=me.getX();
				y1=me.getY();
				x2=x1;
				y2=y1;
				if(selRect != null) {
					Graphics g = getGraphics();
					((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g.setXORMode(getBackground());
					g.setColor(colorSpecial.getColor(3));
					g.fillRect(curRect.x, curRect.y, curRect.width, curRect.height);
					g.setPaintMode();
					selRect = null;
				}
				curRect.setRect(x1,y1, 0, 0);
			} else {
				int ind = findPoint(me.getPoint());
				if(ind >= 0) {
					popup = getPopup(ind);
					maybeShowPopup(me);
				}
			}
		}
		JPopupMenu popup;		
		public void mouseReleased(MouseEvent me) {
			if(me.getModifiers() == MouseEvent.BUTTON1_MASK) {
				dragRectFlag = false;
				x2=me.getX();
				y2=me.getY();
				
				int width=Math.abs(x1-x2);
				int height=Math.abs(y1-y2);
				int x = Math.min(x1,x2);
				int y = Math.min(y1,y2);
				double xStart = screenToPointX(x);
				double xEnd = screenToPointX(x+width)+revScaleX;
				double yStart = screenToPointY(y+height);
				double yEnd = screenToPointY(y)+revScaleY;
				selRect = new Rectangle2D.Double(xStart, yEnd, xEnd-xStart, yEnd-yStart);
				int startX = getCaseX(xStart), endX = getCaseX(xEnd), startY = getCaseY(yStart), endY = getCaseY(yEnd);
				if(startX<0)
					startX = 0;
				if(startY<0)
					startY = 0;
				if(endX>=gridSize)
					endX = gridSize-1;
				if(endY>=gridSize)
					endY = gridSize-1;
				boolean modif = false;
				nbSel = 0;
				for(int i=startX; i<=endX; i++)
					for(int j=startY; j<=endY; j++)
						for(int k=0; k < gridPoint[i][j].length; k++) {
							double xK = myListPoint.getX(gridPoint[i][j][k]), yK = myListPoint.getY(gridPoint[i][j][k]);
							if(xK>=xStart && xK<=xEnd && yK>=yStart && yK<=yEnd) {
								selected[nbSel++] = gridPoint[i][j][k];
							}
						}
				if(listModel != null) {
					listModel.clear();
					LabelIndexIndexColor tmp[] = new LabelIndexIndexColor[nbSel];
					for(int i=0; i<nbSel; i++)
						tmp[i] = labelIndexColor[selected[i]];
					Arrays.sort(tmp);
					for(int i=0; i<nbSel; i++)
						listModel.addElement(tmp[i]);
				}
			} else {
				maybeShowPopup(me);
			}
		}
		private boolean dragRectFlag = false;
	
		public void mouseDragged(MouseEvent me) {
			if(dragRectFlag) {
				Graphics g = getGraphics();
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setXORMode(getBackground());
				g.setColor(colorSpecial.getColor(3));
				g.fillRect(curRect.x, curRect.y, curRect.width, curRect.height);
				x2=me.getX();
				if(x2<0)
					x2 = 0;
				if(x2>=sizeX)
					x2 = sizeX-1;
				y2=me.getY();
				if(y2<0)
					y2 = 0;
				if(y2>=sizeY)
					y2 = sizeY-1;
				curRect.setRect(Math.min(x1,x2), Math.min(y1,y2), Math.abs(x1-x2), Math.abs(y1-y2));
				g.fillRect(curRect.x, curRect.y, curRect.width, curRect.height);
				g.setPaintMode();
			}
		}
		
		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}
	public class indexColor {
		int index;
		Color color;
		
		public indexColor(int index, Color color) {
		}
	}
}