import java.awt.*;
import java.awt.image.*;
import javax.swing.tree.*;
import java.util.Enumeration;
import java.awt.font.*;
import java.awt.geom.*;
/**
 * <CODE>MyImageProducer</CODE> is class which produces images from a DataTable.
 *
 * @version 10 mar 2000
 * @author	Gilles Didier
 */

public class MyImageProducer {
/**
 * Orientation constant.
*/
	public static final int HORIZONTAL = 0;
/**
 * Orientation constant.
*/
	public static final int VERTICAL = 1;
	
	private static final int THRESHOLD = 30;
	private static final int LEAF_WIDTH = 100;
	DefaultMutableTreeNode tree, treePosition;
	private int xOffset = 10, yOffset=10, orientation, leafHeight, sepHeight, totalHeight, totalWidth, leafPosition, utilWidth;
	private float max = 1;
	private Font font = new Font("SansSerif", Font.PLAIN, 10);
	

/**
 * Constructs a new <CODE>MyImageProducer</CODE> with  <CODE>d</CODE> as <CODE>DataTable</CODE>, 
 * <CODE>c</CODE> as <CODE>ColorTable</CODE>, scale <CODE>s</CODE> 
 * and discrete/continue colors property depending of the boolean <CODE>cont</CODE>.
*/
	public MyImageProducer(DefaultMutableTreeNode t, int w, int o) {
		tree = t;
		if(tree == null)
			tree = new DefaultMutableTreeNode("NULL");

		int numberOfLeafs = tree.getLeafCount();
		if(o == HORIZONTAL)
			orientation = HORIZONTAL;
		else
			orientation = VERTICAL;
		FontRenderContext frc = new FontRenderContext(null, false, false);
		Rectangle2D rect = font.getStringBounds("AA", frc);
		leafHeight = (int) rect.getHeight();		
		if(numberOfLeafs>THRESHOLD)
			sepHeight = 2;
		else
			sepHeight = 5;
		totalWidth = w+2*xOffset;
		leafPosition = totalWidth - LEAF_WIDTH;
		totalHeight = numberOfLeafs*leafHeight+(numberOfLeafs-1)*sepHeight+2*yOffset;
		max = ((Number) tree.getUserObject()).floatValue();
	}


/**
 * Constructs a new <CODE>MyImageProducer</CODE> with  <CODE>d</CODE> as <CODE>DataTable</CODE>, 
 * <CODE>c</CODE> as <CODE>ColorTable</CODE>, scale <CODE>s</CODE> 
 * and discrete/continue colors property depending of the boolean <CODE>cont</CODE>.
*/
	public MyImageProducer(DefaultMutableTreeNode t) {
		this(t, 500, VERTICAL);
	}


/**
 * Returns the <CODE>BufferedImage</CODE> representing datas 
 * from position <CODE>begPos</CODE> to position <CODE>endPos</CODE>
 * and from width <CODE>begWid</CODE> to width <CODE>endWid</CODE>.
*/
	public BufferedImage createImage() {
		int xPosition, yPosition, h, w, tmp;
		BufferedImage image = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.createGraphics();
		g.setColor(Color.white);
		g.setFont(font);
		g.fillRect(0, 0, totalWidth, totalHeight);
		drawTree(g, tree, yOffset);
		return image;
	}
	
	public Info drawTree(Graphics g, DefaultMutableTreeNode t, int offset) {
		Point position;
		if(t.isLeaf()) {
			position = new Point(leafPosition+xOffset, offset+leafHeight);
			g.setColor(Color.black);
			drawLeaf(g, t, position);
			return new Info(position, offset+leafHeight+sepHeight);
		} else {
			float f = ((Number) t.getUserObject()).floatValue();
			int inc = offset, i=0;
			Point list[] = new Point[t.getChildCount()];
			for (Enumeration e = t.children() ; e.hasMoreElements() ;) {
				Info info = drawTree(g, (DefaultMutableTreeNode) e.nextElement(), inc);
				list[i++] = info.position;
				inc = info.offset;
			}
			position = new Point((int) (leafPosition-(leafPosition*f)/max)+xOffset, (list[i-1].y+list[0].y)/2);
			g.setColor(Color.blue);
			g.drawLine(position.x, list[0].y, position.x, list[t.getChildCount()-1].y);
			for(i=0; i<t.getChildCount(); i++)
				drawLink(g, position, list[i]);
			return new Info(position, inc);
		}
	}
	
	public void drawLeaf(Graphics g, DefaultMutableTreeNode t, Point pos) {
		g.drawString(t.getUserObject().toString(), pos.x+2, pos.y+leafHeight/2);
	}
/*	public void drawLink(Graphics g, Point s, Point e) {
		g.drawLine(s.x, s.y, e.x, e.y);
	}
*/	
	public void drawLink(Graphics g, Point s, Point e) {
		g.drawLine(s.x, e.y, e.x, e.y);
	}

	private class Info {
		public Point position;
		public int offset;
		public Info(Point p, int o) {
			position = p;
			offset = o;
		}
	}
}

