import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Dialog;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ColorButton extends JButton {
	private Color color;
	public ColorButton(Color c, int s) {
		super();
		setOpaque(true);
		color = c;
		setBackground(color);
		setBorderPainted(true);
		setMargin(new Insets(0,0,0,0));
		final JColorChooser colorChooser = new JColorChooser();
		final ActionListener okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				color = colorChooser.getColor();
				setBackground(color);
			}
		};
		final JDialog dialog = JColorChooser.createDialog(this,"", true, colorChooser,okListener,null);
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				colorChooser.setColor(color);
				dialog.show();
			}
		});
		dim = new Dimension(s, s);
	}
	
	public Color getColor() {
		return color;
	}
	
	public void setColor(Color c) {
		color = c;
	}
	private static Dimension dim;

	public Dimension getPreferredSize() {
		return dim;
	}
	public Dimension getMaximumSize() {
		return dim;
	}
	public Dimension getMinimumSize() {
		return dim;
	}
}
