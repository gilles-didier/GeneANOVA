import java.lang.*;
import java.text.*;
import java.util.*;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.DefaultCellEditor;
import javax.swing.table.TableCellRenderer;

import javax.swing.*;
import javax.swing.border.Border;
import java.io.StringWriter;

import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.event.*;
/**
 * <CODE>ColorTable</CODE> is class which defines table of color for drawing image 
 *
 * @version 10 mar 2000
 * @author	Gilles Didier
 */

public class ColorTable extends Object{
/**
 * table of colors
*/
	public Color colorTable[];
/**
 * size of the table
*/
	int numberOfColors;


/**
 * Constructs a new color table with <CODE>nc</CODE> entries
*/
	public ColorTable() {
		numberOfColors = 0;
	}
/**
 * Constructs a new color table with <CODE>nc</CODE> entries
*/
	public ColorTable(int nc) {
		setNumberOfColors(nc);
		initTable();
	}

/**
 * Constructs a new color table from string <CODE>s</CODE>
*/
	public ColorTable(String s) {
		setColors(s);
	}	
/**
 * Inits a color table with a standard gray gradient.
*/
	public void initTable() {
		for(int i=0; i<numberOfColors; i++){
			colorTable[i] = new Color((i*255)/(numberOfColors-1), (i*255)/(numberOfColors-1), (i*255)/(numberOfColors-1));
		}
	}

/**
 * Returns the color in the entry <CODE>rank</CODE> of the table.
 * If <CODE>rank</CODE> is out of range returns the first or the last color.
*/
	public Color getColor(int rank) {
		if(rank < 0)
			return colorTable[0];
		if(rank >= numberOfColors)
			return colorTable[numberOfColors-1];
		return colorTable[rank];
	}


/**
 * Sets the entry <CODE>rank</CODE> of the table with the color <CODE>rgb</CODE>.
*/
	public void setColor(int rank, Color rgb) {
		if(rank < 0)
			return;
		if(rank >= numberOfColors)
			return;
		colorTable[rank] = rgb;
	}

/**
 * Returns the size of the table.
*/
	public int getNumberOfColors() {
		return numberOfColors;
	}

/**
 * Sets the size of the table. It generally involves a reinitialization of the table.
*/
	public void setNumberOfColors(int n) {
		numberOfColors = n;
		colorTable = new Color[numberOfColors];
	}

/**
 * Sets the entire table from the string <CODE>s</CODE>.
*/
	public void setColors(String s) {
		StringTokenizer li = new StringTokenizer(s, " \t");
		if((li.countTokens()) != numberOfColors)
			setNumberOfColors(li.countTokens());
		for(int i=0; li.hasMoreTokens(); i++) {
			colorTable[i] = getColorFromString(li.nextToken());
		}
	}


/**
 * Returns a string representation of the table
*/
	public String toString() {
		StringWriter str = new StringWriter();
		if(numberOfColors == 0)
			return "";
		str.write(getStringFromColor(colorTable[0]));
		for(int i=1; i<numberOfColors; i++) {
			str.write("\t");
			str.write(getStringFromColor(colorTable[i]));
		}
		return str.toString();
	}

	private static final String sepComp = "*";

	public static String getStringFromColor(Color c) {
		StringWriter str = new StringWriter();
		str.write(Integer.toString(c.getRed()));
		str.write(sepComp);
		str.write(Integer.toString(c.getGreen()));
		str.write(sepComp);
		str.write(Integer.toString(c.getBlue()));
		return str.toString();
	}

	public static Color getColorFromString(String s) {
		StringTokenizer st = new StringTokenizer(s, sepComp);
		if(st.countTokens() != 3)
			return new Color(0,0,0);
		try {
			Color c = new Color(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
			return c;
		} catch(NumberFormatException nfe) {
			return new Color(0,0,0);
		}
	}

	JTable getColorTablePane() {
		ColorTableModel myModel = new ColorTableModel();
 		JTable table = new JTable(myModel);
		table.setPreferredScrollableViewportSize(new Dimension(40, 70));
		setUpColorRenderer(table);
		setUpColorEditor(table);
		return table;
	}

    class ColorRenderer extends JLabel implements TableCellRenderer {
        Border unselectedBorder = null;
        Border selectedBorder = null;
        boolean isBordered = true;

        public ColorRenderer(boolean isBordered) {
            super();
            this.isBordered = isBordered;
            setOpaque(true); //MUST do this for background to show up.
        }
        
        public Component getTableCellRendererComponent(
                                JTable table, Object color, 
                                boolean isSelected, boolean hasFocus,
                                int row, int column) {
            setBackground((Color)color);
            if (isBordered) {
                if (isSelected) {
                    if (selectedBorder == null) {
                        selectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                                  table.getSelectionBackground());
                    }
                    setBorder(selectedBorder);
                } else {
                    if (unselectedBorder == null) {
                        unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                                  table.getBackground());
                    }
                    setBorder(unselectedBorder);
                }
            }
            return this;
        }
    }

    private void setUpColorRenderer(JTable table) {
        table.setDefaultRenderer(Color.class,
                                 new ColorRenderer(true));
    }

    //Set up the editor for the Color cells.
	private void setUpColorEditor(JTable table) {
        //First, set up the button that brings up the dialog.
		final JButton button = new JButton("") {
			public void setText(String s) {
			}
		};

		button.setBackground(Color.white);
		button.setBorderPainted(false);
		button.setMargin(new Insets(0,0,0,0));
		button.setPreferredSize(new Dimension(10, 100));
		final ColorEditor colorEditor = new ColorEditor(button);
		table.setDefaultEditor(Color.class, colorEditor);

        //Set up the dialog that the button brings up.
		final JColorChooser colorChooser = new JColorChooser();
		ActionListener okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				colorEditor.currentColor = colorChooser.getColor();
			}
		};
		final JDialog dialog = JColorChooser.createDialog(button,
                                        "Pick a Color",
                                        true,
                                        colorChooser,
                                        okListener,
                                        null); //XXXDoublecheck this is OK

        //Here's the code that brings up the dialog.
		button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                button.setBackground(colorEditor.currentColor);
                colorChooser.setColor(colorEditor.currentColor);
                //Without the following line, the dialog comes up
                //in the middle of the screen.
                //dialog.setLocationRelativeTo(button);
                dialog.show();
            }
        });
    }

    /*
     * The editor button that brings up the dialog.
     * We extend DefaultCellEditor for convenience,
     * even though it mean we have to create a dummy
     * check box.  Another approach would be to copy
     * the implementation of TableCellEditor methods
     * from the source code for DefaultCellEditor.
     */
    class ColorEditor extends DefaultCellEditor {
        Color currentColor = null;

        public ColorEditor(JButton b) {
                super(new JCheckBox()); //Unfortunately, the constructor
                                        //expects a check box, combo box,
                                        //or text field.
            editorComponent = b;
            setClickCountToStart(1); //This is usually 1 or 2.

            //Must do this so that editing stops when appropriate.
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }

        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }

        public Object getCellEditorValue() {
            return currentColor;
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,  int column) {
            ((JButton)editorComponent).setText(value.toString());
            currentColor = (Color)value;
            return editorComponent;
        }
    }

	public AbstractTableModel getColorTableModel() {
		return new ColorTableModel();
	}

	private static final String colorName = "Colors";

	class ColorTableModel extends AbstractTableModel {
		public ColorTableModel() {
		}
		
		public int getColumnCount() {
			return 1;
		}
        
		public int getRowCount() {
			return colorTable.length;
		}

		public String getColumnName(int col) {
			return colorName;
		}

		public Object getValueAt(int row, int col) {
			return colorTable[row];
 		}

		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		public boolean isCellEditable(int row, int col) {
			return true;
 		}

		public void setValueAt(Object value, int row, int col) {
			colorTable[row] = (Color) value;
		}
	}
	
	JPanel getEditor() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0,(int) Math.ceil(Math.sqrt(numberOfColors))));
		for(int i=0; i<numberOfColors; i++) {
			panel.add(new MyColorButton(i, 20));
		}
		return panel;
	}
	
	JColorChooser colorChooser = new JColorChooser();
	
	public class MyColorButton extends JButton {
		private int indice;
		public MyColorButton(int i, int s) {
			super();
			setOpaque(true);
			indice = i;
			setBackground(ColorTable.this.colorTable[indice]);
			setBorderPainted(true);
			setMargin(new Insets(0,0,0,0));
			addActionListener(new MyActionListener());
			dim = new Dimension(s, s);
		}
		private Dimension dim;
		public Dimension getPreferredSize() {
			return dim;
		}
		public Dimension getMaximumSize() {
			return dim;
		}
		public Dimension getMinimumSize() {
			return dim;
		}
		public class MyActionListener implements ActionListener {
			public MyActionListener() {
			}
			public void actionPerformed(ActionEvent e) {
				colorChooser.setColor(ColorTable.this.colorTable[indice]);
				JDialog dialog = JColorChooser.createDialog(MyColorButton.this,"", true, colorChooser, okListener, null);
				dialog.show();
			}
		}
		private ActionListener okListener = new OkActionListener();
		public class OkActionListener implements ActionListener {
			public OkActionListener() {
			}
			public void actionPerformed(ActionEvent e) {
				ColorTable.this.colorTable[indice] = colorChooser.getColor();
				setBackground(ColorTable.this.colorTable[indice]);
			}
		}
	}
	
	public JMenu getColorMenu(ActionListener a) {
		final Dimension dim = new Dimension(20, 20);
		JMenu menu = new JMenu() {
			public Dimension getPreferredSize() {
				return dim;
			}
			public Dimension getMaximumSize() {
				return dim;
			}
			public Dimension getMinimumSize() {
				return dim;
			}
		};
		menu.setIcon(new ColorTableSquare());
		menu.setUI(new IconMenuUI());
		setupColorMenu(menu, a);
		return menu;
	}
	
	public JMenu getColorMenu(String s, ActionListener a) {
		JMenu menu = new JMenu(s);
		setupColorMenu(menu, a);
		return menu;
	}
	
	private void setupColorMenu(JMenu menu, ActionListener a) {
		menu.getPopupMenu().setLayout(new GridLayout(0,(int) Math.ceil(Math.sqrt(getNumberOfColors()))));
		for(int i=0; i<getNumberOfColors(); i++) {
			ColorMenuItem item = new ColorMenuItem(i);
			item.addActionListener(a);
			menu.add(item);
		}

	}

/*	public JMenu getColorMenu(ActionListener a) {
		final Dimension dim = new Dimension(20, 20);
		JMenu menu = new JMenu() {
			public Dimension getPreferredSize() {
				return dim;
			}
			public Dimension getMaximumSize() {
				return dim;
			}
			public Dimension getMinimumSize() {
				return dim;
			}
		};
		menu.setIcon(new ColorTableSquare());
		menu.setUI(new IconMenuUI());
		menu.getPopupMenu().setLayout(new GridLayout(0,(int) Math.ceil(Math.sqrt(getNumberOfColors()))));
		for(int i=0; i<getNumberOfColors(); i++) {
			ColorMenuItem item = new ColorMenuItem(i);
			item.addActionListener(a);
			menu.add(item);
		}
		return menu;
	}
*/	
	public class ColorMenuItem extends JMenuItem {
		int color;
		Dimension dimL0 = new Dimension(16, 16);
		public  ColorMenuItem(int c) {
			this(c, new Dimension(16, 16));
		}
		public  ColorMenuItem(int c, Dimension dim) {
			super();
	 		color = c;
			Dimension dimL0 = dim;
			setText(null);
			setIcon(new MyColoredSquare(c,12));
			setUI(new IconMenuItemUI());
		}
		public int getIndColor() {
			return color;
		}
		public Color getColor() {
			return colorTable[color];
		}
		public Dimension getPreferredSize() {
			return dimL0;
		}
		public Dimension getMaximumSize() {
			return dimL0;
		}
		public Dimension getMinimumSize() {
			return dimL0;
		}
	}
	
	class ColorTableSquare implements Icon {
		public ColorTableSquare() {
		}
				
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Color oldColor = g.getColor();
			int l = (int) Math.ceil(Math.sqrt(getNumberOfColors()));
			double w = ((double)getIconWidth())/((double)l);
			double h = ((double)getIconHeight())/((double)getNumberOfColors()/l);
			if(w<1)
				w = 1;
			for(int i=0; i<getNumberOfColors(); i++) {
				int posX =(int) (((double)(i%l))*w);
				int posY =(int) (((double)(i/l))*h);
				g.setColor(getColor(i));
				g.fillRect(x+posX,y+posY, (int) Math.ceil(w), (int) Math.ceil(h));
				g.setColor(Color.black);
				g.drawRect(x, y, getIconWidth()-1, getIconHeight()-1);
//				g.fillRect(x+(i*getIconWidth())/(colorTable.getNumberOfColors()-1),y, w, getIconHeight());
			}
		}
		public int getIconWidth() { return 20; }
		public int getIconHeight() { return 20; }
   }
	
	class MyColoredSquare implements Icon {
		int size;
		int indice;
		
		public MyColoredSquare(int i) {
			this(i, 12);
		}
	
		public MyColoredSquare(int i, int s) {
			indice = i;
			size = s;
		}
				
		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(getColor(indice));
			g.fillRect(x,y, getIconWidth(), getIconHeight());
		}
		public int getIconWidth() { return size; }
		public int getIconHeight() { return size; }
	}
}


