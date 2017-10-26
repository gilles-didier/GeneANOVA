import java.text.DecimalFormat;
import java.io.StringWriter;

public class PCACompute {
	public double value[], vector[][], theData[][], stddev[], mean[];
	DecimalFormat nf = new DecimalFormat();

	public  PCACompute(double[][] d) {
nf.setMinimumFractionDigits(4);
nf.setMaximumFractionDigits(4);
		theData = d;
		double[][] symmat = corcol(theData);
		double[] tmpValue = new double[symmat.length];
		vector = new double[symmat.length][symmat.length];
		double[] interm = new double[symmat.length];
		tred2(symmat, tmpValue, interm);
		for(int i=0; i<vector.length; i++)
			for(int j=0; j<vector.length; j++)
				vector[i][j] = symmat[i][j];
		tqli(tmpValue, interm, symmat);
		int[] index = new int[tmpValue.length];
		for(int i=0; i<tmpValue.length; i++)
			index[i] = i;
		quickSort(tmpValue, index, 0, tmpValue.length-1);
		value = new double[symmat.length];
		vector = new double[symmat.length][symmat.length];
		for(int i=0; i<value.length; i++) {
			value[i] = tmpValue[index[i]];
			for(int j=0; j<symmat.length; j++)
				vector[j][i] = symmat[j][index[i]];
		}
	}
	
	public double [][] getCoordinates(int v0, int v1) {
		double res[][] = new double[theData.length][2];
		for(int i = 0; i < theData.length; i++) {
	  		res[i][0] = 0;
			for(int k=0; k<vector.length; k++) {
				res[i][0] += ((theData[i][k]-mean[k])/stddev[k])*vector[k][v0];
			}
	  		res[i][1] = 0;
			for(int k=0; k<vector.length; k++) {
				res[i][1] += ((theData[i][k]-mean[k])/stddev[k])*vector[k][v1];
			}
		}
		return res;
	}

	public double [][] getVariablesCoordinates(int v0, int v1) {
		double res[][] = new double[vector.length][2];
		for(int i = 0; i < vector.length; i++) {
	  		res[i][0] = vector[i][v0]*Math.sqrt(value[v0]);
	  		res[i][1] = vector[i][v1]*Math.sqrt(value[v1]);
		}
		return res;
	}
	
	public Line[] getLines(int v0, int v1) {
		Line line[] = new Line[vector.length];
		for(int i=0; i<vector.length; i++) {
			line[i] = new Line(vector[i][v0]/vector[i][v1], 0);
		}
		return line;
	}

	public Line[] getLines(int v0, int v1, String[] n) {
		Line line[] = new Line[vector.length];
		for(int i=0; i<vector.length; i++) {
			double a;
			int o;
			if(vector[i][v0] != 0) {
				a = vector[i][v1]/vector[i][v0];
				o = (vector[i][v0]>0)?Line.FORWARD:Line.BACKWARD;
			} else {
				a = Double.NEGATIVE_INFINITY;
				o = (vector[i][v1]>0)?Line.FORWARD:Line.BACKWARD;
			}
			if(i<n.length)
				line[i] = new LineBis(a, 0, vector[i][v0]*vector[i][v0]+vector[i][v1]*vector[i][v1], n[i], o);
			else
				line[i] = new LineBis(a, 0, vector[i][v0]*vector[i][v0]+vector[i][v1]*vector[i][v1], o);
		}
		return line;
	}
	
	public double[] getValues() {
		return value;
	}
	public double[] getVariablesCorrelations(int v0, int v1) {
		double res[] = new double[vector.length];
		for(int i=0; i<vector.length; i++) {
			res[i] = vector[i][v0]*vector[i][v0]+vector[i][v1]*vector[i][v1];
		}
		return res;
	}
	public double [] getCorrelations(int v0, int v1) {
		double res[] = new double[theData.length];
		for(int i = 0; i < theData.length; i++) {
	  		double x = 0;
			for(int k=0; k<vector.length; k++) {
				x += ((theData[i][k]-mean[k])/stddev[k])*vector[k][v0];
			}
			double y = 0;
			for(int k=0; k<vector.length; k++) {
				y += ((theData[i][k]-mean[k])/stddev[k])*vector[k][v1];
			}
			double X = 0;
			for(int k=0; k<theData[i].length; k++)
				X += ((theData[i][k]-mean[k])/stddev[k])*((theData[i][k]-mean[k])/stddev[k]);
			res[i] = (x*x+y*y)/X;
		}
		return res;
	}

	public double[] getPercents() {
		double[] res = new double[value.length];
		double sum = 0;
		for(int i=0; i<value.length; i++)
			sum += value[i];
		for(int i=0; i<value.length; i++)
			res[i] = (100*value[i])/sum;
		return res;
	}

	public double[][] getVectors() {
		return vector;
	}
	
/* Create m * m correlation matrix from given n * m data matrix. */
	public double[][] covariance(double data[][]) {
		double x, stddev[], symmat[][];
		int n, m;
		n = data.length; m = data[0].length;
		symmat = new double[m][m];
		mean = new double[m];
		for(int j=0; j<m; j++) {
			mean[j] = 0.0;
			for(int i=0; i<n; i++) {
				mean[j] += data[i][j];
			}
			mean[j] /= (double) n;
		}
		for(int j1 = 0; j1<m; j1++) {
			symmat[j1][j1] = 1.0;
			for(int j2 = j1+1; j2 < m; j2++) {
				symmat[j1][j2] = 0.0;
				for(int i = 0; i < n; i++) {
					double xj1 = (data[i][j1] - mean[j1]);
					double xj2 = (data[i][j2] - mean[j2]);
					symmat[j1][j2] += (xj1*xj2);
				}
				symmat[j1][j2] /= (double) n;
				symmat[j2][j1] = symmat[j1][j2];
			}
		}
		return symmat;
	}

/* Create m * m correlation matrix from given n * m data matrix. */
	public double[][] corcol(double data[][]) {
		double x, symmat[][];
		int n, m, i, j, j1, j2;
		n = data.length; m = data[0].length;
		symmat = new double[m][m];
		mean = new double[m];
		stddev = new double[m];
		for(j = 0; j < m; j++) {
			mean[j] = 0.0;
			for(i = 0; i < n; i++) {
				mean[j] += data[i][j];
			}
			mean[j] /= (double)n;
		}
		for(j = 0; j < m; j++) {
			stddev[j] = 0.0;
			for(i = 0; i < n; i++) {
				stddev[j] += ((data[i][j]-mean[j])*(data[i][j]-mean[j]));
			}
			stddev[j] /= (double)n;
			stddev[j] = Math.sqrt(stddev[j]);
			if (stddev[j] == 0) stddev[j] = 1.0;
		}
		for(j1 = 0; j1 < m-1; j1++) {
			symmat[j1][j1] = 1.0;
			for(j2 = j1+1; j2 < m; j2++) {
				symmat[j1][j2] = 0.0;
				for(i = 0; i < n; i++) {
					double xj1 = (data[i][j1] - mean[j1])/(Math.sqrt((double)n)*stddev[j1]);
					double xj2 = (data[i][j2] - mean[j2])/(Math.sqrt((double)n)*stddev[j2]);
					symmat[j1][j2] += (xj1*xj2);
				}
				symmat[j2][j1] = symmat[j1][j2];
			}
		}
		symmat[m-1][m-1] = 1.0;
		return symmat;
	}

/*Householder reduction of a real, symmetric matrix a[1..n][1..n]. On output, a is replaced 
by the orthogonal matrix Q effecting the transformation. d[1..n] returns the diagonal ele- 
ments of the tridiagonal matrix, and e[1..n] the off-diagonal elements, with e[1]=0. Several 
statements, as noted in comments, can be omitted if only eigenvalues are to be found, in which 
case a contains no useful information on output. Otherwise they are to be included. 
*/
	public static void tred2(double  a[][], double d[], double e[]) {
		int l,k,j,i,n; 
		double scale,hh,h,g,f;
		n = a.length;
		for(i=n-1; i>=1; i--) { 
			l = i-1; 
			h = scale=0.0; 
			if (l > 0) { 
				for(k=0; k<=l; k++) 
					scale += Math.abs(a[i][k]); 
			if (scale == 0.0)
				e[i]=a[i][l]; 
			else { 
				for(k=0; k<=l; k++) { 
					a[i][k] /= scale;
					h += a[i][k]*a[i][k];
				} 
				f = a[i][l]; 
				g = (f >= 0.0 ? -Math.sqrt(h) : Math.sqrt(h)); 
				e[i]=scale*g; 
				h -= f*g;
				a[i][l]=f-g;
				f=0.0; 
				for(j=0; j<=l; j++) { 
					a[j][i]=a[i][j]/h;
					g=0.0;
					for(k=0; k<=j; k++) 
						g += a[j][k]*a[i][k]; 
					for(k=j+1; k<=l; k++) 
						g += a[k][j]*a[i][k]; 
					e[j]=g/h;
					f += e[j]*a[i][j]; 
				} 
				hh=f/(h+h);
				for(j=0; j<=l; j++) {
					f=a[i][j]; 
					e[j]=g=e[j]-hh*f; 
					for(k=0; k<=j; k++)
						a[j][k] -= (f*e[k]+g*a[i][k]); 
				} 
			} 
		} else 
			e[i]=a[i][l]; 
			d[i]=h; 
		} 
		d[0]=0.0; 
		e[0]=0.0; 
		for(i=0; i<n; i++) {
			l=i-1; 
			if (d[i]>0) {
				for(j=0; j<=l; j++) { 
					g=0.0; 
					for(k=0; k<=l; k++)
						g += a[i][k]*a[k][j]; 
					for(k=0; k<=l; k++) 
						a[k][j] -= g*a[k][i]; 
				} 
			} 
			d[i] = a[i][i];
			a[i][i] = 1.0;
			for(j=0;j<=l;j++)
				a[j][i]=a[i][j]=0.0; 
		} 
	}

	public static double sign(double a, double b) {
		return (b) < 0 ? -Math.abs(a) : Math.abs(a);
	}
	
/**  Tridiagonal QL algorithm -- Implicit  **********************/
	public static boolean tqli(double d[], double e[], double z[][]){
		int m, l, iter, i, k, n;
		double s, r, p, g, f, dd, c, b;
		n = d.length;
		for(i=1; i<n; i++)
    		e[i-1] = e[i];
		e[n-1] = 0.0;
		for(l=0; l<n; l++) {
    		iter = 0;
			do {
				for(m=l; m<n-1; m++) {
					dd = Math.abs(d[m]) + Math.abs(d[m+1]);
					if((Math.abs(e[m])+dd) == dd)
						break;
				}
				if (m != l) {
					if (iter++ == 30) {
						System.out.println("PROBLEM");
						return false;
					}
					g = (d[l+1]-d[l])/(((double)2)*e[l]);
					r = Math.sqrt((g*g)+((double)1));
					g = d[m]-d[l]+e[l]/(g+sign(r, g));
					s = c = (double)1;
					p = 0.0;
					for(i=m-1; i>=l; i--) {
						f = s*e[i];
						b = c*e[i];
						e[i+1] = (r = Math.sqrt(f*f+g*g));
						if(r == 0.0) {
							d[i+1] -= p;
							e[m] = 0;
							break;
						}
						s = f/r;
						c = g/r;
						g = d[i+1]-p;
						r = (d[i]-g)*s+((double)2)*c*b;
						d[i+1] = g+(p=s*r);
						g = c*r-b;
						for(k = 0; k < n; k++) {
							f = z[k][i+1];
							z[k][i+1] = s*z[k][i]+c*f;
							z[k][i] = c*z[k][i]-s*f;
						}
					}
					if(r == 0 && i>=0)
						continue;
					d[l] -= p;
					e[l] = g;
					e[m] = 0.0;
				}
			}  while (m != l);
		}
		return true;
	}

	public static class LineBis extends Line {
		public double corr = 0;
		private final DecimalFormat cFormatter =  new DecimalFormat("0.00E0");

		public LineBis(double A, double B) {
			super(A, B);
		}
	
		public LineBis(double A, double B, double C, int O) {
			super(A, B, O); corr = C;
		}

		public LineBis(double A, double B, String N) {
			super(A, B, N);
		}
		
		public LineBis(double A, double B, String N, int O) {
			super(A, B, N, O);
		}
	
		public LineBis(double A, double B, double C, String N) {
			super(A, B, N); corr = C;
		}
		
		public LineBis(double A, double B, double C, String N, int O) {
			super(A, B, N, O); corr = C;
		}
		
		private static final String cLabel = "c";
		
		public String getDescriptor() {
			StringWriter str = new StringWriter();
			str.write("<html><tt><font size=-1>");
			str.write(name);
			str.write("<p></font><font size=-2>");
			str.write(cLabel);
			str.write(" = ");
			str.write(cFormatter.format(corr));
			str.write("</font></tt></html>");
			return str.toString();
		}
	}
	
	private static void quickSort(double p[], int a[], int lo0, int hi0) {
		int lo = lo0;
		int hi = hi0;
		double mid;
		if ( hi0 > lo0) {
             	mid = p[a[(lo0+hi0)/2]];
			while( lo <= hi ) {
				while( ( lo < hi0 ) && ( p[a[lo]] > mid ))
					++lo;
				while( ( hi > lo0 ) && ( p[a[hi]] < mid ))
					--hi;
 				if( lo <= hi ) {
					swap(a, lo, hi);
					++lo; --hi;
				}
			}
			if( lo0 < hi )
				quickSort(p, a, lo0, hi );
 			if( lo < hi0 )
				quickSort(p, a, lo, hi0 );

		}
	}

	private static void swap(int a[], int i, int j) {
 		int T;
		T = a[i];
		a[i] = a[j];
		a[j] = T;
	}

}