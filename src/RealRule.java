import java.awt.*;
import javax.swing.*;
import java.awt.geom.*;
import java.awt.*;
import java.awt.font.*;
import java.text.*;
/**
 * <CODE>RealRule</CODE> is component wich display a rule of real. 
 *
 * @version 10 mar 2000
 * @author	Gilles Didier
 */
public class RealRule extends JComponent {

/**
 * Orientation constant.
*/
	public static final int HORIZONTAL = 0;
/**
 * Orientation constant.
*/
	public static final int VERTICAL = 1;

	private static final int SIZE = 10;
	private static final int MAJOR_TICK_SIZE = 3;
	private static final int MINOR_TICK_SIZE = 2;
	private static final int ITEMSEP = 2;
	private static final int DEC = 2;
	private Color backCol = new Color(100, 150, 200);
	private int refLabel, refTick, orientation, tickIncrement = 1, labelIncrement = 5,
		xOffset, yOffset, wMax, hMax, unit, max, nDec, totalLength;
	private boolean reverse, mir;
	private double scale=1f;
	private Font font = new Font("SansSerif", Font.PLAIN, 10);
	private double minValue, maxValue, ecart, step, start;
 	NumberFormat nf = NumberFormat.getInstance();
	private Dimension dim;
	private int wi = 40, he = 40, markPos = 0;

/**
 * Constructs a new <CODE>RealRule</CODE> with orientation <CODE>o</CODE>, min <CODE>min</CODE>,  max <CODE>max</CODE> and scale <CODE>s</CODE>. 
*/
	public RealRule(int o, boolean r, boolean m, double min, double max, double s, int def) {
		orientation = o;
		reverse = r;
		mir = m;
		if(def != 0 && orientation == VERTICAL) {
			wi = def;
		} else {
			he = def;
		}
		setMinMaxScale(min, max, s);
		start = minValue;
	}

	public RealRule(int o, double min, double max, double s) {
		this(o, false, false, min, max, s, 0);
	}
/**
 * Set the start position to display the rule
*/
	public void setStart(double s) {
		start = s;
	}

/**
 * Sets the minimum value.
*/
	public void setMin(double m) {
		minValue = m;
		ecart= maxValue-minValue;
		computeStep();
	}



/**
 * Sets the maximum value.
*/
	public void setMax(double m) {
		maxValue = m;
		ecart= maxValue-minValue;
		computeStep();
	}	

/**
 * Sets the minimum and maximum values.
*/
	public void setMinMaxScale(double mi, double ma, double sc) {
//System.out.println(" mi "+mi+ " ma "+ma+" sc "+sc);
		minValue = mi;
		maxValue = ma;
		ecart= maxValue-minValue;
		setScale(sc);
		computeStep();
	}

	private void computeStep() {
		FontRenderContext frc = new FontRenderContext(null, false, false);
		String ch;
		if(ecart <= 0)
			maxValue++;
		double digit = (Math.log(ecart)/Math.log(10));
		int itemSize, nbItem;
		int w, h;
		if(digit <=0) {
			nDec = ((int) Math.ceil(-digit));
			if(Math.ceil(-digit) == (-digit))
				nDec++;
		} else {
			nDec = 0;
		}
		nbItem = 0;
		nf.setMaximumFractionDigits(nDec);
		nf.setMinimumFractionDigits(nDec);
		Rectangle2D rect;
		rect = font.getStringBounds(nf.format((double) maxValue), frc);
		w = (int) rect.getWidth();
		h = (int) rect.getHeight();

		if(orientation == HORIZONTAL)
			itemSize = w + ITEMSEP;
		else
			itemSize = h + ITEMSEP;

		nbItem = totalLength/itemSize;
		double x = (ecart/nbItem);
		if(x>0)
			x = (Math.log(x)/Math.log(10));
		else
			x = 0;		
		x = Math.ceil(x);
		if(-x > nDec)
			x = -nDec;
		step = Math.pow(10, x);
		if(step>1 && (2*ecart/step<nbItem))
			step/=2;
	}

	public void setMark(int p) {
		markPos = p;
		repaint();
	}
	
	public void drawMark(int p) {
		Graphics g;
		g=getGraphics();
		g.setXORMode(getBackground());
			
		if(orientation == HORIZONTAL) {
			markPos = p;
			g.drawLine(markPos, 0, markPos, dim.height-1);
		} else {
			markPos = p;
			g.drawLine(0, markPos, dim.width-1, markPos);
		}
	}
	
	public void drawMark() {
		Graphics g;
		g=getGraphics();
		g.setXORMode(getBackground());
			
		if(orientation == HORIZONTAL) {
			g.drawLine(markPos, 0, markPos, dim.height-1);
		} else {
			g.drawLine(0, markPos, dim.width-1, markPos);
		}
	}

/**
 * Sets the font to display the labels. 
*/
	public void setFont(Font f) {
		font = f;
		computeStep();
	}

/**
 * Sets the scale. 
*/
	public void setScale(double s) {
		scale = s;
		totalLength = (int) (ecart*scale);
		if(orientation == VERTICAL)
			dim = new Dimension(wi, totalLength);
		else
			dim = new Dimension(totalLength, he);
		setSize(dim);
		setPreferredSize(dim);
		setMinimumSize(dim);
		setMaximumSize(dim);
		computeStep();
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

	private int globalToLocal(double x) {
		return (int) Math.floor((x-minValue)*scale);		
	}

	private double localToGlobal(int xl) {
		return ((double) xl)/scale+minValue;		
	}

/**
 * Paints the rule. 
*/
	public void paintComponent(Graphics g) {
		int i, xOffset, yOffset, yTop, length, width, height, size;
		double endG, mi, ma, ec, ind;
		Dimension d = getSize();
		g.setFont(font); 
		width = d.width;
		height = d.height;
		g.setColor(backCol);
		g.fillRect(0, 0, width, height);
		FontRenderContext frc = new FontRenderContext(null, false, false);
		mi = (double) (Math.ceil(minValue/step)*step);
		ma = (double) (Math.floor(maxValue/step)*step);
		if(mi == 0)
			mi = Math.abs(0);
		g.setColor(Color.black);
		if(orientation == HORIZONTAL) {
			if(mir)
				g.drawLine(0,  height-1, width-1,  height-1);
			else
				g.drawLine(0, 0, width-1, 0);
		} else {
			if(mir)
				g.drawLine(0, 0, 0, height-1);
			else
				g.drawLine(width-1, 0, width-1, height-1);
		}
		
		for(ind=mi; ind<=ma; ind+=step){
			int x;
			String ch;
 			x = globalToLocal(ind);
			if(orientation == HORIZONTAL) {
				if(reverse)
					x=width-1-x;
				if(x>=0 && x<width) {
					String text;
					if(mir)
						g.drawLine(x, height-1-MAJOR_TICK_SIZE, x, height-1);
					else
						g.drawLine(x, 0, x, MAJOR_TICK_SIZE);
					if((text = nf.format((double) ind)) != null) {
						Rectangle2D rect;
						rect = font.getStringBounds(text, frc);
						int h = (int) (rect.getHeight()-rect.getHeight()/3);
 						int w = (int) rect.getWidth();
						if(mir)
							g.drawString(text, x-w/2, height-1-MAJOR_TICK_SIZE-DEC);
						else
							g.drawString(text, x-w/2, MAJOR_TICK_SIZE+DEC+h);
					}
				}
			} else {
				if(reverse)
					x=height-1-x;
				if(x>=0 && x<height) {
					String text;
					if(mir)
						g.drawLine(MAJOR_TICK_SIZE, x, width-1,x);
					else
						g.drawLine(width-MAJOR_TICK_SIZE, x, width-1,x);
					if((text = nf.format((double) ind)) != null) {
						Rectangle2D rect;
						rect = font.getStringBounds(text, frc);
						int h = (int) (rect.getHeight()-rect.getHeight()/3);
 						int w = (int) rect.getWidth();
						if(mir)
							g.drawString(text, MAJOR_TICK_SIZE+w+DEC, x+h/2);
						else
							g.drawString(text, width-MAJOR_TICK_SIZE-w-DEC, x+h/2);
					}
				}
			}
 		}
		g.setXORMode(getBackground());
		g.setColor(Color.red);
		if(orientation == HORIZONTAL) {
			g.drawLine(markPos, 0, markPos, dim.height-1);
		} else {
			g.drawLine(0, markPos, dim.width-1, markPos);
		}

	}
	
	public void drawRule(Graphics g, int xOffset, int yOffset) {
		int i, yTop, length, width, height, size;
		double endG, mi, ma, ec, ind;
		Dimension d = getSize();
		g.setFont(font); 
		width = d.width;
		height = d.height;
		FontRenderContext frc = new FontRenderContext(null, false, false);
		mi = (double) (Math.ceil(minValue/step)*step);
		ma = (double) (Math.floor(maxValue/step)*step);
		if(mi == 0)
			mi = Math.abs(0);

		g.setColor(Color.black);
		if(orientation == HORIZONTAL) {
			if(mir)
				g.drawLine(xOffset, yOffset+height-1, xOffset+width-1,  yOffset+height-1);
			else
				g.drawLine(xOffset, yOffset, xOffset+width-1, yOffset);
		} else {
			if(mir)
				g.drawLine(xOffset, yOffset, xOffset, yOffset+height-1);
			else
				g.drawLine(xOffset+width-1, yOffset, xOffset+width-1, yOffset+height-1);
		}
		
		for(ind=mi; ind<=ma; ind+=step){
			int x;
			String ch;
 			x = globalToLocal(ind);
			if(orientation == HORIZONTAL) {
				if(reverse)
					x=width-1-x;
				if(x>=0 && x<width) {
					String text;
					if(mir)
						g.drawLine(xOffset+x, yOffset+height-1-MAJOR_TICK_SIZE, xOffset+x, yOffset+height-1);
					else
						g.drawLine(xOffset+x, yOffset, xOffset+x,  yOffset+MAJOR_TICK_SIZE);
					if((text = nf.format((double) ind)) != null) {
						Rectangle2D rect;
						rect = font.getStringBounds(text, frc);
						int h = (int) (rect.getHeight()-rect.getHeight()/3);
 						int w = (int) rect.getWidth();
						if(mir)
							g.drawString(text, xOffset+x-w/2, yOffset+height-1-MAJOR_TICK_SIZE-DEC);
						else
							g.drawString(text, xOffset+x-w/2, yOffset+MAJOR_TICK_SIZE+DEC+h);
					}
				}
			} else {
				if(reverse)
					x=height-1-x;
				if(x>=0 && x<height) {
					String text;
					if(mir)
						g.drawLine(xOffset+MAJOR_TICK_SIZE, yOffset+x, xOffset+width-1, yOffset+x);
					else
						g.drawLine(xOffset+width-MAJOR_TICK_SIZE, yOffset+x, xOffset+width-1, yOffset+x);
					if((text = nf.format((double) ind)) != null) {
						Rectangle2D rect;
						rect = font.getStringBounds(text, frc);
						int h = (int) (rect.getHeight()-rect.getHeight()/3);
 						int w = (int) rect.getWidth();
						if(mir)
							g.drawString(text, xOffset+MAJOR_TICK_SIZE+w+DEC, yOffset+x+h/2);
						else
							g.drawString(text, xOffset+width-MAJOR_TICK_SIZE-w-DEC, yOffset+x+h/2);
					}
				}
			}
 		}
	}
}
