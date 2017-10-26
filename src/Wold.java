
class Wold {
	double[][] x, y, t, u, a, b, c, d, e;
	int rank;
	boolean  modeAa, modeAb, reductionT, reductionU;
	public static final int INVARIANT_MODE = 0, CANONIC_MODE = 1, REGRESSION_MODE = 2;
	public static final int CANONIC_TYPE = 0, REDUNDANCY_TYPE = 1, REGRESSION_PLS_TYPE = 2, CANONIC_PLS_TYPE = 3;
	private int modeY;
	private static final double THRESHOLD = 0.0001;
	
	public Wold(Factor f, double[][] data, int type) {
		if(f.type == Factor.PARTIAL_COLUMNS && f.getCard() == 2) {
			int[] part = f.getPart(0);
			double[][] x = new double[data.length][part.length];
			for(int i=0; i<part.length; i++)
				for(int j=0; j<data.length; j++)
					x[j][i] = data[j][part[i]];
			part= f.getPart(1);
			double[][] y = new double[data.length][part.length];
			for(int i=0; i<part.length; i++)
				for(int j=0; j<data.length; j++)
					y[j][i] = data[j][part[i]];
			switch(type) {
				case CANONIC_TYPE :
					modeAa = false;
					reductionT = true;
					modeAb = false;
					reductionU = true;
					modeY = CANONIC_MODE;
				break;
				case REDUNDANCY_TYPE :
					modeAa = false;
					reductionT = true;
					modeAb = true;
					reductionU = true;
					modeY = INVARIANT_MODE;
				break;
				case REGRESSION_PLS_TYPE :
					modeAa = true;
					reductionT = false;
					modeAb = true;
					reductionU = false;
					modeY = REGRESSION_MODE;
				break;
				case CANONIC_PLS_TYPE :
					modeAa = true;
					reductionT = false;
					modeAb = true;
					reductionU = false;
					modeY = CANONIC_MODE;
				break;
				default :
					modeAa = true;
					reductionT = false;
					modeAb = true;
					reductionU = false;
					modeY = REGRESSION_MODE;
			}
			Compute();
		}
	}
	private void Compute() {
		if(x.length != y.length)
			return;
		double aPrec[] = new double[x[0].length];
		double norm;
		rank = x[0].length+y[0].length;
		t = new double[x.length][rank];
		u = new double[x.length][rank];
		a = new double[x[0].length][rank];
		b = new double[y[0].length][rank];
		c = new double[x[0].length][rank];
		d = new double[y[0].length][rank];
		e = new double[y[0].length][rank];
		for(int h=0; h<rank; h++) {
			for(int i=0; i<t.length; i++) {
				t[i][h] = x[i][0];
				u[i][h] = y[i][0];
			}
/*2.2 calcul de ah*/
			if(modeAa) {
				for(int i=0; i<aPrec.length; i++) {
					aPrec[i] = 0;
					double tmp = 0;
					for(int j=0; j<u.length; j++)
						if(!Double.isNaN(u[j][h]) && !Double.isNaN(x[j][i])) {
							aPrec[i] += x[j][i]*u[j][h];
							tmp += u[j][h]*u[j][h];
						}
					aPrec[i] /= tmp;
				}
			} else {
			}		
/*Norme le vecteur ah a 1*/
			norm = 0;
			for(int i=0; i<aPrec.length; i++)
				norm += aPrec[i]*aPrec[i];
			norm = Math.sqrt(norm);
			for(int i=0; i<a.length; i++)
				aPrec[i] /= norm;
/*th = Xh-1ah/ah*ah*/
			for(int i=0; i<t.length; i++) {
				t[h][i] = 0;
				double tmp = 0;
				for(int j=0; j<aPrec.length; j++)
					if(!Double.isNaN(aPrec[j]) && !Double.isNaN(x[i][j])) {
						t[h][i] += x[i][j]*aPrec[j];
						tmp += aPrec[j]*aPrec[j];
					}
				t[h][i] /= tmp;
			}
/*Reduit t*/
			if(reductionT) {
				norm = 0;
				for(int i=0; i<t.length; i++)
					norm += a[i][h]*a[i][h];
				norm = Math.sqrt(norm);
				for(int i=0; i<t.length; i++)
					t[i][h] /= norm;
			}
			if(modeAb) {
				for(int i=0; i<b.length; i++) {
					b[i][h] = 0;
					double tmp = 0;
					for(int j=0; j<t.length; j++)
						if(!Double.isNaN(t[j][h]) && !Double.isNaN(y[j][i])) {
							b[i][h] += y[j][i]*t[j][h];
							tmp += t[j][h]*t[j][h];
						}
					b[i][h] /= tmp;
				}
			} else {
			}		
/*Norme le vecteur bh a 1*/
			norm = 0;
			for(int i=0; i<b.length; i++)
				norm += b[i][h]*b[i][h];
			norm = Math.sqrt(norm);
			for(int i=0; i<b.length; i++)
				b[i][h] /= norm;
/*uh = Yh-1bh/bh*bh*/
			for(int i=0; i<u.length; i++) {
				u[h][i] = 0;
				double tmp = 0;
				for(int j=0; j<b.length; j++)
					if(!Double.isNaN(b[j][h]) && !Double.isNaN(y[i][j])) {
							u[h][i] += y[i][j]*b[j][h];
							tmp += b[j][h]*b[j][h];
					}
				u[h][i] /= tmp;
			}
/*Reduit uh*/
			if(reductionU) {
				norm = 0;
				for(int i=0; i<t.length; i++)
					norm += u[i][h]*u[i][h];
				norm = Math.sqrt(norm);
				for(int i=0; i<t.length; i++)
					u[i][h] /= norm;
			}
/*boucle tant que convergence de ah*/
			double distAA;
			do {	
				if(modeAa) {
					for(int i=0; i<a.length; i++) {
						a[i][h] = 0;
						double tmp = 0;
						for(int j=0; j<u.length; j++)
							if(!Double.isNaN(u[j][h]) && !Double.isNaN(x[j][i])) {
								a[i][h] += x[j][i]*u[j][h];
								tmp += u[j][h]*u[j][h];
							}
						a[i][h] /= tmp;
					}
				} else {
				}		
/*Norme le vecteur ah a 1*/
				norm = 0;
				for(int i=0; i<a.length; i++)
					norm += a[i][h]*a[i][h];
				norm = Math.sqrt(norm);
				for(int i=0; i<a.length; i++)
					a[i][h] /= norm;
/*th = Xh-1ah/ah*ah*/
				for(int i=0; i<t.length; i++) {
					t[h][i] = 0;
					double tmp = 0;
					for(int j=0; j<aPrec.length; j++)
						if(!Double.isNaN(a[j][h]) && !Double.isNaN(x[i][j])) {
							t[h][i] += x[i][j]*a[j][h];
							tmp += a[j][h]*a[j][h];
						}
					t[h][i] /= tmp;
				}
/*Reduit t*/
				if(reductionT) {
					norm = 0;
					for(int i=0; i<t.length; i++)
						norm += t[i][h]*t[i][h];
					norm = Math.sqrt(norm);
					for(int i=0; i<t.length; i++)
						t[i][h] /= norm;
				}
/*Calcule bh*/
				if(modeAb) {
					for(int i=0; i<b.length; i++) {
						b[i][h] = 0;
						double tmp = 0;
						for(int j=0; j<t.length; j++)
							if(!Double.isNaN(t[j][h]) && !Double.isNaN(y[j][i])) {
								b[i][h] += y[j][i]*t[j][h];
								tmp += t[j][h]*t[j][h];
							}
						b[i][h] /= tmp;
					}
				} else {
				}		
/*Norme le vecteur bh a 1*/
				norm = 0;
				for(int i=0; i<b.length; i++)
					norm += b[i][h]*b[i][h];
				norm = Math.sqrt(norm);
				for(int i=0; i<b.length; i++)
					b[i][h] /= norm;
/*uh = Yh-1bh/bh*bh*/
				for(int i=0; i<u.length; i++) {
					u[h][i] = 0;
					double tmp = 0;
					for(int j=0; j<b.length; j++)
						if(!Double.isNaN(b[j][h]) && !Double.isNaN(y[i][j])) {
							u[h][i] += y[i][j]*b[j][h];
							tmp += b[j][h]*b[j][h];
						}
					u[h][i] /= tmp;
				}
/*Reduit uh*/
				if(reductionU) {
					norm = 0;
					for(int i=0; i<t.length; i++)
						norm += u[i][h]*u[i][h];
					norm = Math.sqrt(norm);
					for(int i=0; i<t.length; i++)
						u[i][h] /= norm;
				}
/* Calcule la norme sup entre ah et aPrec et Fixe aPrec = ah*/
				distAA = 0;
				for(int i=0; i<aPrec.length; i++) {
					if(Math.abs(a[i][h]-aPrec[i])>distAA)
						distAA = Math.abs(a[i][h]);
					aPrec[i] = a[i][h];
				}
			} while(distAA>THRESHOLD);
/*2.3 ch=X'h-1th/th'th*/
			for(int i=0; i<c.length; i++) {
				c[h][i] = 0;
				double tmp = 0;
				for(int j=0; j<t.length; j++)
					if(!Double.isNaN(t[j][h]) && !Double.isNaN(x[j][i])) {
						c[h][i] += x[j][i]*t[j][h];
						tmp += t[j][h]*t[j][h];
					}
					c[h][i] /= tmp;
			}
/*2.4 dh = Y'h-1uh/uh'uh*/
			for(int i=0; i<d.length; i++) {
				d[i][h] = 0;
				double tmp = 0;
				for(int j=0; j<t.length; j++)
					if(!Double.isNaN(t[j][h]) && !Double.isNaN(y[j][i])) {
						d[i][h] += y[j][i]*t[j][h];
						tmp += t[j][h]*t[j][h];
					}
				d[i][h] /= tmp;
			}

/*2.5 eh = Y'h-1th/th'th*/
			for(int i=0; i<e.length; i++) {
				e[i][h] = 0;
				double tmp = 0;
				for(int j=0; j<t.length; j++)
					if(!Double.isNaN(u[j][h]) && !Double.isNaN(y[j][i])) {
						e[i][h] += y[j][i]*u[j][h];
						tmp += u[j][h]*u[j][h];
					}
				e[i][h] /= tmp;
			}
/*2.6 X = X-thc'h*/
			for(int i=0; i<x.length; i++) 
				for(int j=0; j<x[i].length; j++)
					x[i][j] -= t[i][h]*c[j][h];
/*2.7 selon..*/
			switch(modeY) {
				case REGRESSION_MODE :
					for(int i=0; i<y.length; i++) 
						for(int j=0; j<y[i].length; j++)
							y[i][j] -= t[i][h]*d[j][h];
					break;
				case CANONIC_MODE :
					for(int i=0; i<y.length; i++) 
						for(int j=0; j<y[i].length; j++)
							y[i][j] -= u[i][h]*e[j][h];
					break;
				default:
			}
		}
	}
}
