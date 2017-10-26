import java.lang.Math;

public class StatisticFunctions {
private static final double [] coeff = {
									0.2316419,
									0.3989423,
									0.31938153,
									0.356563782,
									1.781477937,
									1.821255978,
									1.330274429,
									0.636619772
								};

	public static double fisher(double f, int ddl1a, int ddl2a) {
		double ddl1 = (double) ddl1a, ddl2 = (double) ddl2a;
		double y =(Math.pow(f,((double)1)/((double)3))*(1-2/(9*ddl2))-(1-2/(9*ddl1)))/
		Math.sqrt(2/(9*ddl1)+Math.pow(f, ((double)2)/((double)3))*2/(9*ddl2));
		double x = Math.abs(y);
		double t1=1/(1+coeff[0]*x), t2=t1*t1, t3=t2*t1, t4=t3*t1, t5=t4*t1;
		double z=coeff[1]*Math.exp(-x*x/2);
		double q=z*(coeff[2]*t1-coeff[3]*t2+coeff[4]*t3-coeff[5]*t4+coeff[6]*t5);
		if(y<0)
			q = 1-q;
		if(q>0.00001 && ddl1<30 && ddl2<30) {
			double s, p;
			if(ddl1a%2 == 0 || ddl2a%2 == 0) {
				x =ddl2/(ddl2+ddl1*f);
				double x1 = 1-x;
				double ddla = ddl1a, ddlb = ddl2a;
				if(ddl1a%2 != 0) {
					double tmp1 = x; x = x1; x1 = tmp1;
					double tmp2 = ddla; ddla = ddlb; ddlb = tmp2;
				}
				s = 1; p = 1;
				for(double r=2; r<=ddla-2; r+=2) {
					p *= x1*((double) (ddlb+r-2))/(((double)r)); s += p;
				}
				q = s*Math.pow(x, ddlb/2);
				if(ddl1a%2 != 0) {
					q = 1-q;
				}
			} else {
				double q1;
				double teta = Math.atan(Math.sqrt(ddl1/ddl2*f));
				double costeta = Math.cos(teta), pcos2 = Math.pow(costeta, 2);
				double sinteta = Math.sin(teta), psin2 = Math.pow(sinteta, 2);
				if(ddl2a == 1) {
					q1 = 1-teta*coeff[7];
				} else {
					s = costeta; p = 1;
					double pcos = costeta;
					for(int r=3; r<=ddl2a-2; r += 2) {
						p = p*(r-1)/r; pcos = pcos*pcos2; s = s+p*pcos;
					}
					q1 = 1-(s*sinteta+teta)*coeff[7];
				}
				if(ddl1a == 1) {
					q = q1;
				} else {
					s = 1; p = 1;
					double psin = 1;
					for(int r=3; r<=ddl1a-2; r += 2) {
						p = p*(ddl2+r-2)/r; psin = psin*psin2; s = s+p*psin;
					}
					double beta = coeff[7];
					for(double i = 2; i <= (ddl2a-1)/2; i++) {
						beta *= ((double) i)/(((double) i)-0.5);
					}
					if(ddl2a == 1) {
						q = q1+beta;
					} else {
						q = q1+2*beta;
					}
				}
			}
		}
		return q;
	}
private static final double FINC = 10;
private static final double PRECI = 0.00001;
	public static double inverse(double p, int ddl1a, int ddl2a) {
		double fStart = 1;
		double fEnd = FINC;
		while(fisher(fEnd , ddl1a, ddl2a)>p)
			fEnd += FINC;
		while((p-fisher(fEnd , ddl1a, ddl2a))>PRECI) {
			double mid = (fStart+fEnd)/2;
			if(fisher(mid , ddl1a, ddl2a)>p)
				fStart = mid;
			else
				fEnd = mid;
		}
		return fEnd;
	}
		

}
		
