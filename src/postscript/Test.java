package postscript;

import java.awt.*;
import java.io .*;
import java.awt.event.*;
import java.lang.reflect.*;

/**
 * Test driver for PSGr
 * (C) 1996 E.J. Friedman-Hill and Sandia National Labs
 * @version 	2.1
 * @author 	Ernest Friedman-Hill
 * @author      ejfried@ca.sandia.gov
 * @author      http://herzberg.ca.sandia.gov
 */

public class Test
{
  static final int XSIZE = 150, YSIZE = 175, NPANELS=3;
  static final int BDR = 20;
  static final int BH = 64;
  static final int BW = 65;
  
  static int pStart(int idx) { return (idx-1) * (XSIZE + 2*BDR) + BDR; }

  private static byte[] getImageBytes(String filename) throws IOException
  {
    FileInputStream fis = new FileInputStream(filename);
    byte[] bytes= new byte[fis.available()];
    fis.read(bytes);
    fis.close();
    return bytes;
  }


  public static void main(String [] argv) throws Exception
  {
    if (argv.length < 1)
      {
        System.out.println("Must specify name of class to test on command line");
        System.exit(-1);
      }
    

    final String className = argv[0];
    final Image bart = Toolkit.getDefaultToolkit().createImage(getImageBytes("data/bart.gif"));
    final Image cross = Toolkit.getDefaultToolkit().createImage(getImageBytes("data/cross.gif"));
    final Panel p = new Panel() {
      public void paint(Graphics g)
      {

        // Outline
        Dimension d = getSize();
        g.drawRect(10, 10, d.width-20, d.height-20);
 
        // Panel 1
        int x1 = 10+BDR;
        int x2 = BDR + XSIZE - 10 - BW;
        int y1 = 10+BDR;
        int y2 = BDR + YSIZE - 10 - BH;
        g.drawImage(bart, BDR, BDR, XSIZE, YSIZE, this);
        g.drawImage(bart, x1, y1, this);
        g.drawImage(bart, x1, y2, this);
        g.drawImage(bart, x2, y1, this);
        g.drawImage(bart, x2, y2, this);

        g.drawLine(x1, y1, x2, y2);
        g.drawLine(x2, y1, x1, y2);

        x1 = pStart(2);
        y2 = YSIZE - BDR;

        //Panel 2
        g.setClip(new Rectangle(x1 + 30, BDR + 30, 100, 100));
        g.drawImage(bart, x1, BDR, x1+XSIZE, BDR+YSIZE, 10, 10, 40, 50, this);
        g.setClip(new Rectangle(getSize()));
        g.drawImage(cross, x1, y2, 30, 30, this);
        g.drawImage(cross, x1+40, y2, 30, 30, Color.green, this);
        g.drawImage(cross, x1+80, y2, 30, 30, Color.cyan, this);

        // Panel 3
        g.drawImage(cross, pStart(3), BDR, XSIZE, YSIZE, Color.blue, this);

        // Panel 4
        
        x1 = pStart(1);
        y1 = 10 + (2 * BDR) + YSIZE;
        
        g.setColor(Color.gray);
        g.fillRect(x1, y1, XSIZE, YSIZE);

        g.setColor(Color.red);
        g.fillRoundRect(x1+10, y1+10, XSIZE-20, YSIZE-20, 60, 20);

        g.setColor(Color.blue);
        g.fillRoundRect(x1+20, y1+20, XSIZE-40, YSIZE-40, 20, 60);

        g.setColor(Color.green);
        g.fillRoundRect(x1+30, y1+30, XSIZE-60, YSIZE-60, 60, 60);

        g.setColor(Color.red);
        g.fillOval(x1+40, y1+40, 20, 20);
        g.fillOval(x1+70, y1+40, 40, 20);
        g.fillOval(x1+40, y1+80, 40, 40);
        g.fillOval(x1+90, y1+80, 20, 40);

        // Panel 5
        
        x1 = pStart(2);
        
        g.setColor(Color.gray);
        g.drawRect(x1, y1, XSIZE, YSIZE);

        g.setColor(Color.red);
        g.drawRoundRect(x1+10, y1+10, XSIZE-20, YSIZE-20, 60, 20);

        g.setColor(Color.blue);
        g.drawRoundRect(x1+20, y1+20, XSIZE-40, YSIZE-40, 20, 60);

        g.setColor(Color.green);
        g.drawRoundRect(x1+30, y1+30, XSIZE-60, YSIZE-60, 60, 60);

        g.setColor(Color.red);
        g.drawOval(x1+40, y1+40, 20, 20);
        g.drawOval(x1+70, y1+40, 40, 20);
        g.drawOval(x1+40, y1+80, 40, 40);
        g.drawOval(x1+90, y1+80, 20, 40);

        // Panel 6        
        x1 = pStart(3);
        String testString = "The quick brown fox jumped over the lazy dog.";

        g.setColor(Color.black);
        g.setClip(new Rectangle(x1, y1, XSIZE, YSIZE));
        g.setFont(new Font("TimesRoman", Font.ITALIC, 24));
        g.drawString(testString, x1, y1 + 30);
        g.setFont(new Font("TimesRoman", Font.BOLD, 20));
        g.drawString(testString, x1, y1 + 60);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 12));
        g.drawString(testString, x1, y1 + 80);

        g.setFont(new Font("Courier", Font.ITALIC, 24));
        g.drawString(testString, x1, y1 + 100);
        g.setFont(new Font("Courier", Font.BOLD, 20));
        g.drawString(testString, x1, y1 + 130);
        g.setFont(new Font("Courier", Font.PLAIN, 12));
        g.drawString(testString, x1, y1 + 150);


      }
      public Dimension getPreferredSize()
      {
        return new Dimension(NPANELS * (XSIZE + BDR*2),  2 * (BDR*2 + YSIZE));
      }

    };
    final Frame f = new Frame("PSGr Test");
    Button b = new Button("Print");
    
    b.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        try
          {
            StringWriter sw = new StringWriter();
            Class clazz = Class.forName(className);
            Constructor c = clazz.getConstructor(new Class[] { Writer.class });
            PSGrBase postscript = (PSGrBase) c.newInstance(new Object[] { sw });
            p.paint(postscript);        
            PrintWriter pw = new PrintWriter(new FileWriter("psgr.ps"));
            pw.println(sw.toString());
            pw.println("showpage");
            pw.close();
          }
        catch (Exception e) { System.out.println(e); }
      }
    });

    f.setLayout (new BorderLayout());
    f.add(p, "Center");
    f.add(b, "South");
    f.pack();
    f.setVisible(true);

    f.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) { System.exit(0); }
    });
   
  }
}

