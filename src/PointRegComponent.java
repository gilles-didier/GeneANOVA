import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import java.util.*;
import java.text.NumberFormat;
import java.util.Locale;
import java.io.StringWriter;

public class PointRegComponent extends PointComponent {
	private double a,b,r;
	private byte[] isSelected;
	private static final byte UNSEL = 1;
	private static final byte SEL = 0;
	private static  NumberFormat formatter = NumberFormat.getInstance();
	private static final int FRAC = 2;
	private JTextArea regText = null;
	private boolean drawLine = true;
	

	public PointRegComponent(ListPoint m, boolean egalite_scale, ColorTable ct, ColorTable spe) {
		this(m, egalite_scale, ct, spe, null, null);
	}
	public PointRegComponent(ListPoint m, boolean egalite_scale, ColorTable ct, ColorTable spe, ActionListener mA) {
		this(m, egalite_scale, ct, spe, null, mA);
	}

	public PointRegComponent(ListPoint m, boolean egalite_scale, ColorTable ct, ColorTable spe, Line[] l) {
		this(m, egalite_scale, ct, spe, l, null);
	}

	public PointRegComponent(ListPoint m, boolean egalite_scale, ColorTable ct, ColorTable spe, Line[] l, ActionListener mA) {
		super(m, egalite_scale, ct, spe, l, mA);
		formatter.setMaximumFractionDigits(FRAC);
		formatter.setMinimumFractionDigits(FRAC);
		isSelected = new byte[myListPoint.size()];
		for(int i=0; i<isSelected.length; i++)
			isSelected[i] = SEL;
		computeRegression();
	}
	
	public void setRegText(JTextArea t) {
		regText = t;
		regText.setText(getRegString());
	}

	public String	getRegString() {
		StringWriter writer = new StringWriter();
		writer.write("y = ");
		writer.write(formatter.format(a));
		writer.write("x ");
		if(b>=0)
			writer.write("+ ");
		else
			writer.write("- ");
		writer.write(formatter.format(Math.abs(b)));
		writer.write("\nr =");
		writer.write(formatter.format(r));
		return writer.toString();
	}
	
	public boolean isDrawLine() {
		return drawLine;
	}
	
	public void setDrawLine(boolean d) {
		drawLine = d;
	}
	
	public void computeRegression() {
		double X = 0, Y = 0, XX = 0, YY = 0, XY = 0;
		int N = 0;

		for (int i=0;i<myListPoint.size();i++)
			if(isSelected[i] == SEL) {
				N++;
				X += myListPoint.getX(i);
				Y += myListPoint.getY(i);
				XX += myListPoint.getX(i)*myListPoint.getX(i);
				YY += myListPoint.getY(i)*myListPoint.getY(i);
				XY += myListPoint.getX(i)*myListPoint.getY(i);
			}
		a = (((double) N)*XY-X*Y)/(((double) N)*XX-X*X);
		b = (Y*XX-X*XY)/(((double) N)*XX-X*X);
		r = (((double) N)*XY-X*Y)/Math.sqrt((((double) N)*XX-X*X)*(((double) N)*YY-Y*Y));
		if(regText != null)
			regText.setText(getRegString());
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		if(drawLine) {
			Rectangle clip = g.getClipBounds();
			double xPS = screenToPointX(clip.x);
			double xPE = screenToPointX(clip.x+clip.width);
			double yPS = a*xPS+b;
			double yPE = a*xPE+b;
			g.setColor(colorSpecial.getColor(2));
			g.drawLine(pointToScreenX(xPS), pointToScreenY(yPS), pointToScreenX(xPE), pointToScreenY(yPE));
		}
	}
	
	public void draw(Graphics g, int xOffset, int yOffset) {
		super.draw(g, xOffset, yOffset);
		if(drawLine) {
			double xPS = screenToPointX(0);
			double xPE = screenToPointX(sizeX);
			double yPS = a*xPS+b;
			double yPE = a*xPE+b;
			g.setColor(colorSpecial.getColor(2));
			g.drawLine(pointToScreenX(xPS)+xOffset, pointToScreenY(yPS)-yOffset, pointToScreenX(xPE)+xOffset, pointToScreenY(yPE)-yOffset);
			g.setColor(Color.black);
		}
	}

	protected boolean drawPointTest(Graphics g, int i) {
		int x = pointToScreenX(myListPoint.getX(i));
		int y = pointToScreenY(myListPoint.getY(i));
		int xs = x-DEMI_POINT_SIZE, ys = y-DEMI_POINT_SIZE;
		if(isSelected[i] == SEL)
			g.fillOval(xs, ys, POINT_SIZE, POINT_SIZE);
		else
			g.drawOval(xs, ys, POINT_SIZE, POINT_SIZE);
		if(curRect != null)
			return curRect.intersects(new Rectangle(xs, ys, POINT_SIZE, POINT_SIZE));
		else
			return false;
	}
	
	protected void drawPoint(Graphics g, int i) {
		int x = pointToScreenX(myListPoint.getX(i));
		int y = pointToScreenY(myListPoint.getY(i));
		if(isSelected[i] == SEL)
			g.fillOval(x-DEMI_POINT_SIZE, y-DEMI_POINT_SIZE, POINT_SIZE, POINT_SIZE);
		else
			g.drawOval(x-DEMI_POINT_SIZE, y-DEMI_POINT_SIZE, POINT_SIZE, POINT_SIZE);
	}
	
	protected void drawPointBis(Graphics g, int i, int xOffset, int yOffset) {
		int x = pointToScreenX(myListPoint.getX(i))+xOffset;
		int y = pointToScreenY(myListPoint.getY(i))+yOffset;
		if(isSelected[i] == SEL)
			g.fillOval(x-DEMI_POINT_SIZE_BIS, y-DEMI_POINT_SIZE_BIS, POINT_SIZE_BIS, POINT_SIZE_BIS);
		else
			g.drawOval(x-DEMI_POINT_SIZE_BIS, y-DEMI_POINT_SIZE_BIS, POINT_SIZE_BIS, POINT_SIZE_BIS);
	}

	public double getA()
	{
		return a;
	}
	
	public double getB()
	{
		return b;
	}
	
	public double getR()
	{
		return r;
	}
	public void toggleIsSelected(int ind) {
		if(isSelected[ind] == SEL)
			isSelected[ind] = UNSEL;
		else
			isSelected[ind] = SEL;
	}
	
	public void toggleAllSelected() {
		for(int i=0; i<nbSel; i++)
			toggleIsSelected(selected[i]);
		computeRegression();
	}
}