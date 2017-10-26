
public class Line {
	public double a, b;
	public String name;
	public int orientation;
	public static final int NONE = 0;
	public static final int FORWARD = 1;
	public static final int BACKWARD = 2;
	
	public Line(double A, double B) {
		this(A, B, null);
	}
	
	public Line(double A, double B, int O) {
		this(A, B, null, O);
	}

	public Line(double A, double B, String N) {
		this(A, B, null, NONE);
	}
	
	public Line(double A, double B, String N, int O) {
		a = A; b = B; name = N; orientation = O;
	}
	
	public String getDescriptor() {
		return name;
	}
}