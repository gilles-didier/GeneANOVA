//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
//INTERFACE DECRIVANT LA LISTE DES POINTS				     //
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
import java.text.DecimalFormat;
import java.io.StringWriter;

public abstract class ListPoint {
	protected final DecimalFormat cFormatter =  new DecimalFormat("0.00E0");
 	protected final String xLabel = "x", yLabel = "y";
	public abstract double getX(int indice);
	public abstract double getY(int indice);
	public abstract String getName(int indice);
	public abstract int size();
	public String getDescriptor(int indice) {
		StringWriter str = new StringWriter();
		str.write("<html>");
		str.write(getText(indice));
		str.write("</html>");
		return str.toString();
	}
	protected String getText(int indice) {
		StringWriter str = new StringWriter();
		str.write("<tt><font size=-1>");
		str.write(getName(indice));
		str.write("</font><font size=-2><p>");
		str.write(xLabel);
		str.write(" = ");
		str.write(cFormatter.format(getX(indice)));
		str.write("<p>");
		str.write(yLabel);
		str.write(" = ");
		str.write(cFormatter.format(getY(indice)));
		str.write("</font></tt>");
		return str.toString();
	}
}