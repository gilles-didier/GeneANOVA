import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;

class PointIcon implements Icon {
				
	protected ListPoint myListPoint;	
	protected double maximumX, minimumX ,maximumY, minimumY, ecartX, ecartY, scaleX, scaleY, revScaleX, revScaleY;
	protected int margeX = 1, margeY = 1, sizeX, sizeY, xOffset = 0, yOffset = 0;
	protected static final int POINT_SIZE = 3;
	private boolean equalScale, hasLine;
	private Dimension dim;
	private static final Random rand = new Random();
	protected int gridSize = 20;
	protected int [][][] gridPoint;
	private double a,b,r;
	private Color lineColor = Color.green, pointColor = Color.black;
	private Line[] line = null;
	
	public PointIcon(Dimension d, ListPoint m, boolean eS, boolean reg) {
		this(d, m, eS, reg, Color.black);
	}
	public PointIcon(Dimension d, ListPoint m, boolean eS) {
		this(d, m, eS, false, Color.black);
	}
	public PointIcon(Dimension d, ListPoint m) {
		this(d, m, false, false, Color.black);
	}
	public PointIcon(Dimension d, ListPoint m, Color c) {
		this(d, m, false, false, c);
	}
	
	public PointIcon(Dimension d, ListPoint m, boolean eS, Color c) {
		this(d, m, eS, false, c);
	}
	
	public PointIcon(Dimension d, ListPoint m, boolean eS, boolean reg, Color c) {
		this(d, m, eS, reg, c, null);
	}
	
	public PointIcon(Dimension d, ListPoint m, boolean eS, Color c, Line[] l) {
		this(d, m, eS, false, c, l);
	}
	
	public PointIcon(Dimension d, ListPoint m, boolean eS, boolean reg, Color c, Line[] l) {
		myListPoint = m;
		if(myListPoint.size() == 0)
			return;	
		findBounds();
		initTab();
		equalScale = eS;
		hasLine = reg;
		pointColor = c;
		line = l;
		scaleX = ((double) d.width)/ecartX;
		scaleY = ((double) d.height)/ecartY;
		if(equalScale) {
			double min = Math.min(scaleX,scaleY);
			setScale(min, min);
		} else {
			setScale(scaleX, scaleY);
		}
		if(hasLine)
			computeRegression();
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
			gridPoint[caseX][caseY][tmp[caseX][caseY]++] = i;
		}
	}

	

	public void setScale(double sX, double sY) {
		scaleX = sX; scaleY = sY;
		revScaleX = 1/scaleX; revScaleY = 1/scaleY;
		sizeX = ((int)(ecartX*scaleX))+2*margeX;
		sizeY = ((int)(ecartY*scaleY))+2*margeY;
	}
		
	protected int pointToScreenX(double x) {
		return ((int) ((x-minimumX)*scaleX))+margeX;
	}
	
	protected double screenToPointX(int x) {
		return ((double) (x-margeX))/scaleX+minimumX;
	}
	
	protected int pointToScreenY(double y) {
		return sizeY-((int) ((y-minimumY)*scaleY))-margeY;
	}

	
	public void draw(Graphics g) {
		g.setColor(pointColor);
		for(int i=0; i<gridSize; i++)
			for(int j=0; j<gridSize; j++)
				for(int k=0; k<gridPoint[i][j].length && k<2; k++) {
					drawPoint(g, gridPoint[i][j][k]);	
				}
	}

	protected void drawPoint(Graphics g, int i) {
		int x = xOffset+pointToScreenX(myListPoint.getX(i));
		int y = yOffset+pointToScreenY(myListPoint.getY(i));
		g.fillOval(x-POINT_SIZE/2, y-POINT_SIZE/2, POINT_SIZE, POINT_SIZE);
	}
	
	public void computeRegression() {
		double X = 0, Y = 0, XX = 0, YY = 0, XY = 0;
		int N = myListPoint.size();

		for (int i=0;i<myListPoint.size();i++) {
			X += myListPoint.getX(i);
			Y += myListPoint.getY(i);
			XX += myListPoint.getX(i)*myListPoint.getX(i);
			YY += myListPoint.getY(i)*myListPoint.getY(i);
			XY += myListPoint.getX(i)*myListPoint.getY(i);
		}
		a = (((double) N)*XY-X*Y)/(((double) N)*XX-X*X);
		b = (Y*XX-X*XY)/(((double) N)*XX-X*X);
		r = (((double) N)*XY-X*Y)/Math.sqrt((((double) N)*XX-X*X)*(((double) N)*YY-Y*Y));
	}
	private void drawLine(Graphics g) {
		double xPS = screenToPointX(0);
		double xPE = screenToPointX(sizeX);
		double yPS = a*xPS+b;
		double yPE = a*xPE+b;
		g.setColor(lineColor);
		g.drawLine(pointToScreenX(xPS)+xOffset, pointToScreenY(yPS)+yOffset, pointToScreenX(xPE)+xOffset, pointToScreenY(yPE)+yOffset);
	}
	
	public void drawLines(Graphics g) {
		if(line != null)
			for(int i=0; i<line.length; i++)
				paintLine(g, line[i]);
	}

	public void paintLine(Graphics g, Line l) {
		if(l != null) {
			Rectangle clip = g.getClipBounds();
			double xPS = screenToPointX(0);
			double xPE = screenToPointX(sizeX);
			double yPS = l.a*xPS+l.b;
			double yPE = l.a*xPE+l.b;
			g.drawLine(pointToScreenX(xPS)+xOffset, pointToScreenY(yPS)+yOffset, pointToScreenX(xPE)+xOffset, pointToScreenY(yPE)+yOffset);
		}
	}
	
	public void paintIcon(Component c, Graphics g, int x, int y) {
		xOffset = x; yOffset = y;
		g.setColor(Color.white);
		g.fillRect(x, y, sizeX, sizeY);
		draw(g);
		g.setColor(lineColor);
		drawLines(g);
		if(hasLine)
			drawLine(g);
	}
	
	public int getIconWidth() { return sizeX; }
	public int getIconHeight() { return sizeY; }
}
