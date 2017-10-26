import java.lang.*;
import java.util.*;
import java.io.StringWriter;
import javax.swing.table.*;
import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;

class ANOVACompute extends Object {
	ANOVADesign design;
	Vector res, stiVect;
	double data[][], sst, st2, stiTab[], stot;
	int tot, dft, sti;
	int totflag;
	private static String indLabel, factLabel, residualLabel, totalLabel, sumLabel, dfLabel, varLabel, fLabel, pLabel, normLabel, selectLabel;

	static {
		try {
			ResourceBundle resources= ResourceBundle.getBundle("resources.ANOVACompute", Locale.getDefault());
			residualLabel = resources.getString("residualLabel");
			totalLabel = resources.getString("totalLabel");
			sumLabel = resources.getString("sumLabel");
			varLabel = resources.getString("varLabel");
			fLabel = resources.getString("fLabel");
			pLabel = resources.getString("pLabel");
			normLabel = resources.getString("normLabel");
			selectLabel = resources.getString("selectLabel");
			dfLabel = resources.getString("dfLabel");
			factLabel = resources.getString("factLabel");
			indLabel = resources.getString("indLabel");
		} catch (MissingResourceException mre) {
			residualLabel = "Residual";
			totalLabel = "Total";
			sumLabel = "Sum of squares";
			varLabel = "Mean squares";
			fLabel = "F";
			pLabel = "P-value";
			normLabel = "Normalised";
			selectLabel = "Selection";
			dfLabel = "DF";
			factLabel = "Factor";
			indLabel = "Indice";
		}
	}
	
	public ANOVACompute() {
	}

	public ANOVACompute(ANOVADesign de) {
		design = de;
	}
	public ANOVACompute(ANOVADesign de, double da[][]) {
		design = de;
		data = da;
	}
	
	public void setDesign(ANOVADesign d) {
		design = d;
	}
	
	public void setDatas(double da[][]) {
		data = da;
	}

	public void compute(double d[][], int maxOrder) {
		double st;
		data = d;
		tot = data.length*data[0].length;
		dft = tot-1;
		st = 0;
		for(int i=0; i<data.length; i++)
			for(int j=0; j<data[i].length; j++)
				st += data[i][j];
		st2 = Math.pow(st,2)/((double) tot);
		sst = 0;
		for(int i=0; i<data.length; i++)
			for(int j=0; j<data[i].length; j++)
				sst += Math.pow(data[i][j],2);
		stot = sst-st2;
		stiVect = new Vector();
		res = new Vector();
		for(int order=1; order<=maxOrder; order++) {
			int factor[] = new int[order+1];
			factor[0] = -1;
			computeSSP(0, order, factor, res);
		}
	}


	public TableModel getTableModel() {
		return new ANOVATableModel();
	}

	public class ANOVATableModel extends AbstractTableModel {
		public boolean act[];
		int rddl;
		double rss;
		
		public ANOVATableModel() {
			act = new boolean[res.size()];
			for(int i=0; i<act.length; i++)
				act[i] = true;
			computeResidual();
		}
		private void computeResidual() {
			rss = stot;
			rddl = dft;
			for(int i=0; i<res.size(); i++)
				if(act[i] == true) {
					rss -= ((LineRes) res.elementAt(i)).SS;
					rddl -= ((LineRes) res.elementAt(i)).DDL;
				}
		}
		public int getColumnCount() { return 7; }
		public int getRowCount() { return res.size()+2;}
		public Object getValueAt(int row, int col) {
			if(row<res.size()) {
				LineRes lr = (LineRes) res.elementAt(row);
				if(act[row]) {
					switch(col) {
						case 0 :
							return new Boolean(act[row]);
						case 1 :
							return lr.getName();
						case 2 :
							return new Double(lr.SS);
						case 3:
							return new Integer(lr.DDL);
						case 4 :
							return new Double(lr.SS/lr.DDL);
						case 5 :
							return new Double((rddl*(lr.SS/lr.DDL))/rss);
						case 6 :
							double F = (rddl*(lr.SS/lr.DDL))/rss;
							if(F >= 1)
							return new Float(StatisticFunctions.fisher((rddl*(lr.SS/lr.DDL))/rss, lr.DDL, rddl));
							else
								return new Float(Float.NaN);	
					}
				} else {
					switch(col) {
						case 0 :
							return new Boolean(act[row]);
						case 1 :
							return lr.getName();
						case 2 :
							return new Double(Double.NaN);
						case 3:
							return new Double(Double.NaN);
						case 4 :
							return new Double(Double.NaN);
						case 5 :
							return new Double(Double.NaN);
						case 6 :
							return new Float(Float.NaN);	
					}
				}
			} else {
				switch(row-res.size()) {
					case 0 :
						switch(col) {
							case 0 :
								return new Boolean(true);
							case 1 :
								return residualLabel;
							case 2 :
								return new Double(rss);
							case 3:
								return new Integer(rddl);
							case 4 :
								return new Double(rss/rddl);
							case 5 :
								return new Double(Double.NaN);
							case 6 :
								return new Float(Float.NaN);
						}
						break;
					case 1 :
						switch(col) {
							case 0 :
								return new Boolean(true);
							case 1 :
								return totalLabel;
							case 2 :
								return new Double(stot);
							case 3:
								return new Integer(dft);
							case 4 :
								return new Double(stot/dft);
							case 5 :
								return new Double(Double.NaN);
							case 6 :
								return new Float(Float.NaN);
						}
				}
			}			
			return "";
		}
		public String getColumnName(int col) {	
			switch(col) {
				case 0 :
					return selectLabel;
				case 1 :
					return factLabel;
				case 2 :
					return sumLabel;
				case 3:
					return dfLabel;
				case 4 :
					return varLabel;
				case 5 :
					return fLabel;
				case 6 :
					return pLabel;
			}
			return "";
		}
		public Class getColumnClass(int c) {return getValueAt(0, c).getClass();}
		public boolean isCellEditable(int row, int col) {return col == 0 && row < (res.size());}
		public void setValueAt(Object aValue, int row, int column) {
			act[row] = ((Boolean) aValue).booleanValue();
			computeResidual();
			fireTableDataChanged();
		}
	}

	private int dfa(Factor fact) {
		switch(fact.type) {
			case Factor.ALL_ROWS :
					return data.length-1;
			case Factor.ALL_COLUMNS :
					return data[0].length-1;
			case Factor.PARTIAL_ROWS :
			case Factor.PARTIAL_COLUMNS :
				return fact.getCard()-1;
		}
		return 1;
	}
	
	private double constructSub(int iprec, int order, int maxOrder, int tabTmp[], int tabFact[]) {
		double minus = 0;
		if(order == maxOrder) {
			return ((LineRes) res.elementAt(findRes(tabTmp))).SS;
		}
		for(int i=iprec+1; i<=(tabFact.length-maxOrder+order); i++) {
			tabTmp[order] = tabFact[i];
			minus += constructSub(i, order+1, maxOrder, tabTmp, tabFact);
		}
		return minus;
	}
	
	private void computeSSP(int order, int maxOrder, int[] factor, Vector res) {
	 	if(order == maxOrder) {
			double ss, stmp, f;
			int tab[] = new int[maxOrder];
			int dfa = 1;
			for(int i=0; i<maxOrder; i++)
				tab[i] = factor[i+1];
			dfa = dfa(design.getFactor(tab[0]));
			for(int i=1; i<maxOrder; i++)
				dfa *= dfa(design.getFactor(tab[i]));	
			stmp = computeSti(1, maxOrder, factor, new SetToParse());
			ss = (stmp-st2);
			for(int i=1; i<maxOrder; i++) {
				int tmpTab[] = new int[i];
				ss -= constructSub(-1, 0, i, tmpTab, tab);
			}
			res.add(new LineRes(ss, dfa, tab));
		} else {
			for(int i = factor[order]+1; i<design.getNumberOfFactors(); i++) {
				factor[order+1] = i;
				computeSSP(order+1, maxOrder, factor, res);
			}
		}
	 }

	public double computeSti(int order, int maxOrder, int tabFact[], SetToParse set) {
		double res = 0;
		if(order<=maxOrder) {
			Factor fact = design.getFactor(tabFact[order]);
			int[] ancRows, ancCols;
			int ancMaxRows, ancMaxCols;
			switch(fact.type) {
				case Factor.ALL_ROWS :
					ancRows = set.rows;
					ancMaxRows = set.maxRows;
					set.rows = new int[1];
					for(int i=0; i<data.length; i++) {
						if(ancRows == null) {
							set.rows[0] = i;
							set.maxRows = 1;
						} else {
							int j;
							for(j=0; j<set.maxRows && ancRows[j] != i; j++)
							;
							if(j<set.maxRows) {
								set.rows[0] = i;
								set.maxRows = 1;
							} else {
								set.maxRows = 0;
							}
						}
						if(set.maxRows>0)
							res += computeSti(order+1, maxOrder, tabFact, set);
					}
					set.setRows(ancRows, ancMaxRows);
					break;
				case Factor.ALL_COLUMNS :
					ancCols = set.cols;
					ancMaxCols = set.maxCols;
					set.cols = new int[1];
					for(int i=0; i<data[0].length; i++) {
						if(ancCols == null) {
							set.cols[0] = i;
							set.maxCols = 1;
						} else {
							int j;
							for(j=0; j<set.maxCols && ancCols[j] != i; j++)
							;
							if(j<set.maxCols) {
								set.cols[0] = i;
								set.maxCols = 1;
							} else {
								set.maxCols = 0;
							}
						}
						if(set.maxCols>0)
							res += computeSti(order+1, maxOrder, tabFact, set);
					}
					set.setCols(ancCols, ancMaxCols);
					break;
				case Factor.PARTIAL_ROWS :
					ancRows = set.rows;
					ancMaxRows = set.maxRows;
					set.cols = new int[ancMaxRows];
					for(int i=0; i<fact.getCard(); i++) {
						int[] at = fact.getPart(i);
						if(ancRows == null) {
							set.setRows(at, at.length);
						} else {
							set.maxRows = 0;
							for(int k=0, l=0; k<at.length && l<ancMaxRows;) {
								if(at[k] == ancRows[l]) {
									set.rows[set.maxRows++] = at[k];
									k++; l++;
								} else {
									if(at[k] > ancRows[l])
										l++;
									else
										k++;
								}
							}
						}
						if(set.maxRows>0)
							res += computeSti(order+1, maxOrder, tabFact, set);
					}
					set.setRows(ancRows, ancMaxRows);
					break;
				case Factor.PARTIAL_COLUMNS :
					ancCols = set.cols;
					ancMaxCols = set.maxCols;
					set.cols = new int[ancMaxCols];
					for(int i=0; i<fact.getCard(); i++) {
						int[] at = fact.getPart(i);
						if(ancCols == null) {
							set.setCols(at, at.length);
						} else {
							set.maxCols = 0;
							for(int k=0, l=0; k<at.length && l<ancMaxCols;) {
								if(at[k] == ancCols[l]) {
									set.cols[set.maxCols++] = at[k];
									k++; l++;
								} else {
									if(at[k] > ancCols[l])
										l++;
									else
										k++;
								}
							}
						}
						if(set.maxCols>0)
							res += computeSti(order+1, maxOrder, tabFact, set);
					}
					set.setCols(ancCols, ancMaxCols);
					break;
			}			
		} else {
			double sti =0;
			int ni = 0;
			if(set.rows == null) {
				for(int i=0; i<data.length; i++) {
					if(set.cols == null) {
						for(int j=0; j<data[i].length; j++) {
							sti += data[i][j];
							ni++;
						}
					} else {
						for(int j=0; j<set.maxCols; j++) {
							sti += data[i][set.cols[j]];
							ni++;
						}
					}
				}
			} else {
				for(int i=0; i<set.maxRows; i++) {
					if(set.cols == null) {
						for(int j=0; j<data[set.rows[i]].length; j++) {
							sti += data[set.rows[i]][j];
							ni++;
						}
					} else {
						for(int j=0; j<set.maxCols; j++) {
							sti += data[set.rows[i]][set.cols[j]];
							ni++;
						}
					}
				}
			}
			res = Math.pow(sti,2)/ni;
		}		
		return res;			
	}

	class LineRes extends Object implements Comparable {
		public double SS;
		public int DDL, FA[];
		
		public LineRes(double ss, int ddl,int fa[]) {
			SS = ss; DDL = ddl; FA = fa;
		}
		public String getName() {
			StringWriter sW = new StringWriter();
			switch(FA[0]) {
				case -1:
					sW.write(residualLabel);
					break;
				case -2:
					sW.write(totalLabel);
					break;
				default:
					sW.write((design.getFactor(FA[0])).getName());
			}
			for(int i=1; i<FA.length; i++)
				sW.write(" "+(design.getFactor(FA[i])).getName());
			return sW.toString();
		}
		public String toString() {
			StringWriter sW = new StringWriter();
			sW.write((design.getFactor(FA[0])).getName());
			sW.write(getName());
			sW.write('\t'+Double.toString(SS)+'\t');
			sW.write(Integer.toString(DDL)+'\t');
			return sW.toString();
		}
		public int compareTo(Object o) {
			LineRes ol = (LineRes) o;
			if(ol.FA.length>this.FA.length)
				return 1;
			if(ol.FA.length<this.FA.length)
				return -1;
			for(int i=0; i<this.FA.length; i++) {
				if(ol.FA[i]>this.FA[i])
					return -1;
				if(ol.FA[i]<this.FA[i])
					return -1;
			}
			return 0;
		}
	}

	public int compareTab(int tab1[], int tab2[]) {
		if(tab1.length>tab2.length)
			return 1;
		if(tab1.length<tab2.length)
			return -1;
		for(int i=0; i<tab2.length; i++) {
			if(tab1[i]>tab2[i])
				return -1;
			if(tab1[i]<tab2[i])
				return -1;
		}
		return 0;
	}

	private int findRes(int tab[]) {
		for(int i=0; i<res.size(); i++) {
			int[] tab2 = ((LineRes)res.elementAt(i)).FA;
			if(compareTab(tab, tab2)==0)
				return i;
		}
		return 0;
	}
	
	public class ANOVALocaleTableModel extends AbstractTableModel {
		int rddl, fddl, compare = 0;
		TableExp tableExp;
		Factor factor1, factor2;
		ResLocal result[];
		public static final int IND = 0;
		public static final int NAME = 1;
		public static final int SSF = 2;
		public static final int RES = 3;
		public static final int SST = 4;
		public static final int F = 5;
		public static final int PVALUE = 6;
		public static final int VAR = 7;
		public static final int VARN = 8;
		
		public class ResLocal extends Object implements Comparable {
			int indice = 0;
			double ssf = 0, res = 0, sst = 0;
			public ResLocal() {
			}
			public ResLocal(int i, double ssf0, double res0, double sst0) {
				indice = i; ssf = ssf0; res = res0; sst = sst0;
			}
			public void set(int i, double ssf0, double res0, double sst0) {
				indice = i; ssf = ssf0; res = res0; sst = sst0;
			}
			public int getIndice() {
				return indice;
			}
			public String getName() {
				switch(factor1.getType()) {
					case Factor.ALL_ROWS :
						return tableExp.getRowLabels()[indice];
					case Factor.ALL_COLUMNS :
						return tableExp.getColLabels()[indice];
					case Factor.PARTIAL_ROWS :
					case Factor.PARTIAL_COLUMNS :
						return (factor1.getName()+indice);
					default :
						return Integer.toString(indice);
				}			
			}
			public double getSSF() {
				return ssf;
			}
			public double getRES() {
				return res;
			}
			public double getSST() {
				return sst;
			}
			public double getF() {
				return (((double)rddl)*ssf)/(((double)fddl)*res);
			}
			public double getPValue() {
				double Fi = (((double)rddl)*ssf)/(((double)fddl)*res);
				if(Fi >= 1)
					return StatisticFunctions.fisher(Fi, fddl, rddl);
				else
					return Double.NaN;	
			}
			public double getVariance() {
				return ssf/((double)fddl);
			}
			public double getVarNormalise() {
				return ssf/(((double)fddl)*sst);
			}
			public int compareTo(Object o) {
				if(o instanceof ResLocal) {
					ResLocal to = (ResLocal) o;
					switch(compare) {
						case NAME :
							switch(factor1.getType()) {
								case Factor.ALL_ROWS :
								case Factor.ALL_COLUMNS :
									return this.getName().compareTo(to.getName());
								case Factor.PARTIAL_ROWS :
								case Factor.PARTIAL_COLUMNS :
									if(indice> to.getIndice())
										return 1;
									if(indice < to.getIndice())
										return -1;
									return 0;
								default :
									return 0;
							}
						case SSF :
							if(getSSF() < to.getSSF() || Double.isNaN(getSSF()) || Double.isNaN(to.getSSF()))
								return 1;
							if(getSSF() > to.getSSF())
								return -1;
							return 0;
						case RES :
							if(getRES() < to.getRES() || Double.isNaN(getRES()) || Double.isNaN(to.getRES()))
								return 1;
							if(getRES() > to.getRES())
								return -1;
							return 0;
						case SST :
							if(getSST() < to.getSST() || Double.isNaN(getSST()) || Double.isNaN(to.getSST()))
								return 1;
							if(getSST() > to.getSST())
								return -1;
							return 0;
						case F :
							if(getF() < to.getF() || Double.isNaN(getF()) || Double.isNaN(to.getF()))
								return 1;
							if(getF() > to.getF())
								return -1;
							return 0;
						case PVALUE :
							if(getPValue() < to.getPValue() || Double.isNaN(getPValue()) || Double.isNaN(to.getPValue()))
								return -1;
							if(getPValue() > to.getPValue())
								return 1;
							return 0;
						case VAR :
							if(getVariance() < to.getVariance() || Double.isNaN(getVariance()) || Double.isNaN(to.getVariance()))
								return 1;
							if(getVariance() > to.getVariance())
								return -1;
							return 0;
						case VARN :
							if(getVarNormalise() < to.getVarNormalise() || Double.isNaN(getVarNormalise()) || Double.isNaN(to.getVarNormalise()))
								return 1;
							if(getVarNormalise() > to.getVarNormalise())
								return -1;
							return 0;
						default :
							return 0;
					}
				}			
				return 0;
			}			
		}
		public ANOVALocaleTableModel(Factor f1, Factor f2, int df, int dr, TableExp t) {
			factor1 = f1; factor2 = f2; fddl = df; rddl = dr; tableExp = t;
			result = new ResLocal[getCard(factor1, tableExp.getNumberOfRows(), tableExp.getNumberOfCols())];
			for(int i=0; i<result.length; i++)
				result[i] = new ResLocal();
		}
		
		public ResLocal[] getResult() {
			return result;
		}
		
		public int getColumnCount() { return 9; }
		public int getRowCount() { return result.length;}
		public Object getValueAt(int row, int col) {
			if(row>=result.length) 
				return "-";
			switch(col) {
				case IND :
					return new Integer(row+1);
				case NAME :
					return result[row].getName();
				case SSF :
					return new Double(result[row].getSSF());
				case RES :
					return new Double(result[row].getRES());
				case SST:
					return new Double(result[row].getSST());
				case F :
					return new Double(result[row].getF());
				case PVALUE :
					return new Float(result[row].getPValue());
				case VAR :
					return new Double(result[row].getVariance());
				case VARN :
					return new Float(result[row].getVarNormalise());
			}
			return "";
		}
		
		public String getColumnName(int col) {	
			switch(col) {
				case IND :
					return indLabel;
				case NAME :
					return factor1.getName();
				case SSF :
					return factor2.getName();
				case RES :
					return residualLabel;
				case SST:
					return totalLabel;
				case F :
					return fLabel;
				case PVALUE :
					return pLabel;
				case VAR :
					return varLabel;
				case VARN :
					return normLabel;
			}
			return "";
		}
		class sortAction extends AbstractAction {
			int ind;
			sortAction(int i) {
				super(getColumnName(i));
				ind = i;
			}
			public void actionPerformed(ActionEvent e) {
				compare = ind;
				Arrays.sort(result);
				fireTableDataChanged();
			}
		}
		public Action[] getActions() {
			Action[] a = new Action[getColumnCount()-1];
			for(int i=1; i<=a.length; i++)
				a[i-1] = new sortAction(i);
			return a;
		}
		public Class getColumnClass(int c) {return getValueAt(0, c).getClass();}
		public boolean isCellEditable(int row, int col) {return false;}
		public void setValueAt(Object aValue, int row, int column) { }
	}
	
	public ANOVALocaleTableModel getTableModelLocal(TableExp t, int factor1, int factor2) {
		int f1, f2;
		if(factor1<factor2) {
			f1 = factor1; f2 = factor2;
		} else {
			f1 = factor2; f2 = factor1;
		}
		double[][] d = t.getDatas();
		SetToParse set0 = getSetToParse(design.getFactor(factor1), 0);
		int ddl2 = getCard(design.getFactor(factor2))-1;
		int ddlres = set0.getCard(d.length, d[0].length)-1-ddl2;
		for(int f=0; f<f1; f++)
			ddlres -= getCard(design.getFactor(f))-1;
		for(int f=f1+1; f<f2; f++)
			ddlres -= getCard(design.getFactor(f))-1;
		for(int f=f2+1; f<design.getNumberOfFactors(); f++)
			ddlres -= getCard(design.getFactor(f))-1;
		ANOVALocaleTableModel res = new ANOVALocaleTableModel(design.getFactor(factor1), design.getFactor(factor2), ddl2, ddlres, t);
		setResLocal(t.getDatas(), factor1, factor2, res.getResult());
		return res;
	}
	
	public void setResLocal(double d[][], int factor1, int factor2, ANOVALocaleTableModel.ResLocal[] res) {
		if(factor1 == factor2)
			return;
		int f1, f2;
		if(factor1<factor2) {
			f1 = factor1; f2 = factor2;
		} else {
			f1 = factor2; f2 = factor1;
		}
		data = d;
		int cardf = getCard(design.getFactor(factor1));
		for(int i=0; i<res.length; i++) {
			SetToParse set = getSetToParse(design.getFactor(factor1), i);
			double st = computeLocalST(set);
			double ss = computeLocalSS(set);
			double sst = ss-st;
			double ssf2 = computeLocalSST(design.getFactor(factor2), set)-st;
			double resi = sst-ssf2;
			for(int f=0; f<f1; f++)
				resi -= computeLocalSST(design.getFactor(f), set)-st;
			for(int f=f1+1; f<f2; f++)
				resi -= computeLocalSST(design.getFactor(f), set)-st;
			for(int f=f2+1; f<design.getNumberOfFactors(); f++)
				resi -= computeLocalSST(design.getFactor(f), set)-st;
			res[i].set(i, ssf2, resi, sst);
		}
	}

	public int getLocalDDLF(int factor2) {
		return getCard(design.getFactor(factor2))-1;
	}	

	public int getLocalDDLR(double d[][], int factor1) {
		SetToParse set0 = getSetToParse(design.getFactor(factor1), 0);
		int ddlres = set0.getCard(d.length, d[0].length)-1;
		for(int f=0; f<factor1; f++)
			ddlres -= (double) getCard(design.getFactor(f))-1;
		for(int f=factor1+1; f<design.getNumberOfFactors(); f++)
			ddlres -= (double) getCard(design.getFactor(f))-1;
		return ddlres;
	}	

	public double [][] computeLocal(double d[][], int factor1, int factor2) {
		if(factor1 == factor2)
			return null;
		data = d;
		int cardf = getCard(design.getFactor(factor1));
		double[][] res = new double[cardf][2];
		int f1, f2;
		if(factor1<factor2) {
			f1 = factor1; f2 = factor2;
		} else {
			f1 = factor2; f2 = factor1;
		}
		SetToParse set0 = getSetToParse(design.getFactor(factor1), 0);
		double ddl2 = (double) getCard(design.getFactor(factor2))-1;
		double ddlres = ((double) set0.getCard(d.length, d[0].length)-1)-ddl2;
		for(int f=0; f<f1; f++)
			ddlres -= (double) getCard(design.getFactor(f))-1;
		for(int f=f1+1; f<f2; f++)
			ddlres -= (double) getCard(design.getFactor(f))-1;
		for(int f=f2+1; f<design.getNumberOfFactors(); f++)
			ddlres -= (double) getCard(design.getFactor(f))-1;
		for(int i=0; i<res.length; i++) {
			SetToParse set = getSetToParse(design.getFactor(factor1), i);
			double st = computeLocalST(set);
			double ss = computeLocalSS(set);
			double sst = ss-st;
			double ssf2 = computeLocalSST(design.getFactor(factor2), set)-st;
			double resi = sst-ssf2;
			for(int f=0; f<f1; f++)
				resi -= computeLocalSST(design.getFactor(f), set)-st;
			for(int f=f1+1; f<f2; f++)
				resi -= computeLocalSST(design.getFactor(f), set)-st;
			for(int f=f2+1; f<design.getNumberOfFactors(); f++)
				resi -= computeLocalSST(design.getFactor(f), set)-st;
			res[i][0] = ssf2/(ddl2*sst);
			res[i][1] = resi/(ddlres*sst);
		}
		return res;
	}

	public double [][] computeLocalSqrt(double d[][], int factor1, int factor2) {
		if(factor1 == factor2)
			return null;
		data = d;
		int cardf = getCard(design.getFactor(factor1));
		double[][] res = new double[cardf][2];
		int f1, f2;
		if(factor1<factor2) {
			f1 = factor1; f2 = factor2;
		} else {
			f1 = factor2; f2 = factor1;
		}
		SetToParse set0 = getSetToParse(design.getFactor(factor1), 0);
		double ddl2 = (double) getCard(design.getFactor(factor2))-1;
		double ddlres = ((double) set0.getCard(d.length, d[0].length)-1)-ddl2;
		for(int f=0; f<f1; f++)
			ddlres -= (double) getCard(design.getFactor(f))-1;
		for(int f=f1+1; f<f2; f++)
			ddlres -= (double) getCard(design.getFactor(f))-1;
		for(int f=f2+1; f<design.getNumberOfFactors(); f++)
			ddlres -= (double) getCard(design.getFactor(f))-1;
		for(int i=0; i<res.length; i++) {
			SetToParse set = getSetToParse(design.getFactor(factor1), i);
			double st = computeLocalST(set);
			double ss = computeLocalSS(set);
			double sst = ss-st;
			double ssf2 = computeLocalSST(design.getFactor(factor2), set)-st;
			double resi = sst-ssf2;
			for(int f=0; f<f1; f++)
				resi -= computeLocalSST(design.getFactor(f), set)-st;
			for(int f=f1+1; f<f2; f++)
				resi -= computeLocalSST(design.getFactor(f), set)-st;
			for(int f=f2+1; f<design.getNumberOfFactors(); f++)
				resi -= computeLocalSST(design.getFactor(f), set)-st;
			res[i][0] = ssf2/sst;
			res[i][1] = Math.log(StatisticFunctions.fisher((ddlres*ssf2)/(ddl2*resi), (int) ddl2, (int) ddlres))/LOG10;
		}
		return res;
	}
private static final double LOG10 = Math.log(10);

	
	public double [][] computeLocalBis(double d[][], int factor1, int factor2) {
		if(factor1 == factor2)
			return null;
		data = d;
		int cardf = getCard(design.getFactor(factor1));
		double[][] res = new double[cardf][2];
		int f1, f2;
		if(factor1<factor2) {
			f1 = factor1; f2 = factor2;
		} else {
			f1 = factor2; f2 = factor1;
		}
		SetToParse set0 = getSetToParse(design.getFactor(factor1), 0);
		double ddl2 = (double) getCard(design.getFactor(factor2))-1;
		double ddlres = ((double) set0.getCard(d.length, d[0].length)-1)-ddl2;
		for(int f=0; f<f1; f++)
			ddlres -= (double) getCard(design.getFactor(f))-1;
		for(int f=f1+1; f<f2; f++)
			ddlres -= (double) getCard(design.getFactor(f))-1;
		for(int f=f2+1; f<design.getNumberOfFactors(); f++)
			ddlres -= (double) getCard(design.getFactor(f))-1;
		for(int i=0; i<res.length; i++) {
			SetToParse set = getSetToParse(design.getFactor(factor1), i);
			double st = computeLocalST(set);
			double ss = computeLocalSS(set);
			double sst = ss-st;
			double ssf2 = computeLocalSST(design.getFactor(factor2), set)-st;
			double resi = sst-ssf2;
			for(int f=0; f<f1; f++)
				resi -= computeLocalSST(design.getFactor(f), set)-st;
			for(int f=f1+1; f<f2; f++)
				resi -= computeLocalSST(design.getFactor(f), set)-st;
			for(int f=f2+1; f<design.getNumberOfFactors(); f++)
				resi -= computeLocalSST(design.getFactor(f), set)-st;
			res[i][0] = ssf2/ddl2;
			res[i][1] = (ddlres*ssf2)/(ddl2*resi);
		}
		return res;
	}
	
	public double [][] computeLocalBisSqrt(double d[][], int factor1, int factor2) {
		if(factor1 == factor2)
			return null;
		data = d;
		int cardf = getCard(design.getFactor(factor1));
		double[][] res = new double[cardf][2];
		int f1, f2;
		if(factor1<factor2) {
			f1 = factor1; f2 = factor2;
		} else {
			f1 = factor2; f2 = factor1;
		}
		SetToParse set0 = getSetToParse(design.getFactor(factor1), 0);
		double ddl2 = (double) getCard(design.getFactor(factor2))-1;
		double ddlres = ((double) set0.getCard(d.length, d[0].length)-1)-ddl2;
		for(int f=0; f<f1; f++)
			ddlres -= (double) getCard(design.getFactor(f))-1;
		for(int f=f1+1; f<f2; f++)
			ddlres -= (double) getCard(design.getFactor(f))-1;
		for(int f=f2+1; f<design.getNumberOfFactors(); f++)
			ddlres -= (double) getCard(design.getFactor(f))-1;
		for(int i=0; i<res.length; i++) {
			SetToParse set = getSetToParse(design.getFactor(factor1), i);
			double st = computeLocalST(set);
			double ss = computeLocalSS(set);
			double sst = ss-st;
			double ssf2 = computeLocalSST(design.getFactor(factor2), set)-st;
			double resi = sst-ssf2;
			for(int f=0; f<f1; f++)
				resi -= computeLocalSST(design.getFactor(f), set)-st;
			for(int f=f1+1; f<f2; f++)
				resi -= computeLocalSST(design.getFactor(f), set)-st;
			for(int f=f2+1; f<design.getNumberOfFactors(); f++)
				resi -= computeLocalSST(design.getFactor(f), set)-st;
			res[i][0] = Math.sqrt(ssf2/ddl2);
			res[i][1] = (ddlres*ssf2)/(ddl2*resi);
		}
		return res;
	}
	
	public double [][] computeLocalTer(double d[][], int factor1, int factor2) {
		if(factor1 == factor2)
			return null;
		data = d;
		int cardf = getCard(design.getFactor(factor1));
		double[][] res = new double[cardf][2];
		int f1, f2;
		if(factor1<factor2) {
			f1 = factor1; f2 = factor2;
		} else {
			f1 = factor2; f2 = factor1;
		}
		SetToParse set0 = getSetToParse(design.getFactor(factor1), 0);
		double ddl2 = (double) getCard(design.getFactor(factor2))-1;
		double ddlres = ((double) set0.getCard(d.length, d[0].length)-1)-ddl2;
		for(int f=0; f<f1; f++)
			ddlres -= (double) getCard(design.getFactor(f))-1;
		for(int f=f1+1; f<f2; f++)
			ddlres -= (double) getCard(design.getFactor(f))-1;
		for(int f=f2+1; f<design.getNumberOfFactors(); f++)
			ddlres -= (double) getCard(design.getFactor(f))-1;
		for(int i=0; i<res.length; i++) {
			SetToParse set = getSetToParse(design.getFactor(factor1), i);
			double st = computeLocalST(set);
			double ss = computeLocalSS(set);
			double sst = ss-st;
			double ssf2 = computeLocalSST(design.getFactor(factor2), set)-st;
			double resi = sst-ssf2;
			for(int f=0; f<f1; f++)
				resi -= computeLocalSST(design.getFactor(f), set)-st;
			for(int f=f1+1; f<f2; f++)
				resi -= computeLocalSST(design.getFactor(f), set)-st;
			for(int f=f2+1; f<design.getNumberOfFactors(); f++)
				resi -= computeLocalSST(design.getFactor(f), set)-st;
			res[i][0] = ssf2/ddl2;
			res[i][1] = resi/ddlres;
		}
		return res;
	}

	public double [][] computeLocalTerSqrt(double d[][], int factor1, int factor2) {
		if(factor1 == factor2)
			return null;
		data = d;
		int cardf = getCard(design.getFactor(factor1));
		double[][] res = new double[cardf][2];
		int f1, f2;
		if(factor1<factor2) {
			f1 = factor1; f2 = factor2;
		} else {
			f1 = factor2; f2 = factor1;
		}
		SetToParse set0 = getSetToParse(design.getFactor(factor1), 0);
		double ddl2 = (double) getCard(design.getFactor(factor2))-1;
		double ddlres = ((double) set0.getCard(d.length, d[0].length)-1)-ddl2;
		for(int f=0; f<f1; f++)
			ddlres -= (double) getCard(design.getFactor(f))-1;
		for(int f=f1+1; f<f2; f++)
			ddlres -= (double) getCard(design.getFactor(f))-1;
		for(int f=f2+1; f<design.getNumberOfFactors(); f++)
			ddlres -= (double) getCard(design.getFactor(f))-1;
		for(int i=0; i<res.length; i++) {
			SetToParse set = getSetToParse(design.getFactor(factor1), i);
			double st = computeLocalST(set);
			double ss = computeLocalSS(set);
			double sst = ss-st;
			double ssf2 = computeLocalSST(design.getFactor(factor2), set)-st;
			double resi = sst-ssf2;
			for(int f=0; f<f1; f++)
				resi -= computeLocalSST(design.getFactor(f), set)-st;
			for(int f=f1+1; f<f2; f++)
				resi -= computeLocalSST(design.getFactor(f), set)-st;
			for(int f=f2+1; f<design.getNumberOfFactors(); f++)
				resi -= computeLocalSST(design.getFactor(f), set)-st;
			res[i][0] = Math.sqrt(ssf2/ddl2);
			res[i][1] = Math.sqrt(resi/ddlres);
		}
		return res;
	}

	private double computeLocalSST(Factor f, SetToParse set) {
		int cardf = getCard(f);
		double stmp = 0;
		for(int i=0; i<cardf; i++)
			stmp += computeLocalST(getSetToParse(f, i).inter(set));
		return stmp;
	 }

	public double computeLocalSS(SetToParse set) {
		double ss = 0;
		if(set.rows == null) {
			for(int i=0; i<data.length; i++) {
				if(set.cols == null) {
					for(int j=0; j<data[i].length; j++) {
						ss +=  Math.pow(data[i][j],2);
					}
				} else {
					for(int j=0; j<set.maxCols; j++) {
						ss +=  Math.pow(data[i][set.cols[j]],2);
					}
				}
			}
		} else {
			for(int i=0; i<set.maxRows; i++) {
				if(set.cols == null) {
					for(int j=0; j<data[set.rows[i]].length; j++) {
						ss +=  Math.pow(data[set.rows[i]][j],2);
					}
				} else {
					for(int j=0; j<set.maxCols; j++) {
						ss +=  Math.pow(data[set.rows[i]][set.cols[j]],2);
					}
				}
			}
		}	
		return ss;			
	}
	
	public double computeLocalST(SetToParse set) {
		double sti =0;
		if(set.rows == null) {
			for(int i=0; i<data.length; i++) {
				if(set.cols == null) {
					for(int j=0; j<data[i].length; j++) {
						sti += data[i][j];
					}
				} else {
					for(int j=0; j<set.maxCols; j++) {
						sti += data[i][set.cols[j]];
					}
				}
			}
		} else {
			for(int i=0; i<set.maxRows; i++) {
				if(set.cols == null) {
					for(int j=0; j<data[set.rows[i]].length; j++) {
						sti += data[set.rows[i]][j];
					}
				} else {
					for(int j=0; j<set.maxCols; j++) {
						sti += data[set.rows[i]][set.cols[j]];
					}
				}
			}
		}	
		return Math.pow(sti,2)/set.getCard();		
	}

	
	public int getCard(Factor f) {
		switch(f.getType()) {
			case Factor.ALL_ROWS :
				return data.length;
			case Factor.ALL_COLUMNS :
				return data[0].length;
			case Factor.PARTIAL_ROWS :
			case Factor.PARTIAL_COLUMNS :
				return f.getCard();
			default :
				return 0;
		}			
	}

	public int getCard(Factor f, int nR, int nC) {
		switch(f.getType()) {
			case Factor.ALL_ROWS :
				return nR;
			case Factor.ALL_COLUMNS :
				return nC;
			case Factor.PARTIAL_ROWS :
			case Factor.PARTIAL_COLUMNS :
				return f.getCard();
			default :
				return 0;
		}			
	}
	
	public SetToParse getSetToParse(Factor f, int i) {
		switch(f.getType()) {
			case Factor.ALL_ROWS :
				int row[] = new int[1];
				row[0] = i;
				return new SetToParse(row, null);
			case Factor.ALL_COLUMNS :
				int col[] = new int[1];
				col[0] = i;
				return new SetToParse(null, col);
			case Factor.PARTIAL_ROWS :
				return new SetToParse(cloneInt(f.getPart(i)), null);
			case Factor.PARTIAL_COLUMNS :
				return new SetToParse(null, cloneInt(f.getPart(i)));
			default :
				return new SetToParse(null, null);
		}					
	}
	
	private static final int[] cloneInt(int[] t) {
		int[] r = new int[t.length];
		for(int i=0; i<t.length; i++) {
			r[i] = t[i];
		}
		return r;
	}
	
	class SetToParse extends Object {
		public int[] rows, cols;
		public int maxRows, maxCols;
		
		public SetToParse(int[] r, int[] c, int mr, int mc) {
			rows = r; cols = c; maxRows = mr; maxCols = mc;
		}
		
		public SetToParse(SetToParse s) {
			maxRows = s.maxRows; maxCols = s.maxCols;
			rows = new int[maxRows]; cols = new int[maxCols]; 
			for(int i=0; i<maxRows; i++)
				rows[i] = s.rows[i];
			for(int i=0; i<maxCols; i++)
				cols[i] = s.cols[i];
		}
		
		public SetToParse(int[] r, int[] c) {
			rows = r; cols = c;
			if(rows == null)
				maxRows = 0;
			else
				maxRows = rows.length;
			if(cols == null)
				maxCols = 0;
			else
				maxCols = cols.length;
		}
		
		public SetToParse() {
				this(null, null, 0, 0);
		}
		
		public void setRows(int[] r, int mr) {
			rows = r; maxRows = mr;
		}
		
		public void setCols(int[] c, int mc) {
			cols = c; maxCols = mc;
		}
		
		public int getCard(int nR, int nC) {
			if(rows != null) {
				nR = maxRows;
			}
			if(cols != null) {
				nC = maxCols;
			}
			return nC*nR;		
		}
		public int getCard() {
			int nR, nC;
			if(rows != null) {
				nR = maxRows;
			} else {
				nR = data.length;
			}
			if(cols != null) {
				nC = maxCols;
			} else {
				nC = data[0].length;
			}
			return nC*nR;		
		}
		public String toString() {
			StringWriter str = new StringWriter();
			str.write("nrows ");
			if(rows == null)
				str.write("all");
			else
				str.write(Integer.toString(maxRows));
			str.write("  ----  ");
			str.write("ncols ");
			if(cols == null)
				str.write("all");
			else
				str.write(Integer.toString(maxCols));
			return str.toString();
		}
		public SetToParse inter(SetToParse a) {
			if(a.rows != null) {
				if(rows == null) {
					maxRows = a.maxRows;
					rows = a.rows;
					rows = cloneInt(a.rows);
				} else {
					int newMax = 0;
					for(int k=0, l=0; k<maxRows && l<a.maxRows;) {
						if(rows[k] == a.rows[l]) {
							rows[newMax++] = a.rows[l];
							k++; l++;
						} else {
							if(rows[k] > a.rows[l])
								l++;
							else
								k++;
						}
					}
					maxRows = newMax;
				}
			}
			
			if(a.cols != null) {
				if(cols == null) {
					maxCols = a.maxCols;
					cols = cloneInt(a.cols);
				} else {
					int newMax = 0;
					for(int k=0, l=0; k<maxCols && l<a.maxCols;) {
						if(cols[k] == a.cols[l]) {
							cols[newMax++] = a.cols[l];
							k++; l++;
						} else {
							if(cols[k] > a.cols[l])
								l++;
							else
								k++;
						}
					}
					maxCols = newMax;
				}
			}
			return this;
		}
	}
}
/*
	public double [][] computeLocalTer(double d[][], int factor1, int factor2) {
		if(factor1 == factor2)
			return null;
		data = d;
		int cardf = getCard(design.getFactor(factor1));
		double[][] res = new double[cardf][2];
		int f1, f2;
		if(factor1<factor2) {
			f1 = factor1; f2 = factor2;
		} else {
			f1 = factor2; f2 = factor1;
		}
		SetToParse set0 = getSetToParse(design.getFactor(factor1), 0);
		double ddl1 = (double) set0.getCard(d.length, d[0].length)-1;
		double ddl2 = (double) getCard(design.getFactor(factor2))-1;
		double ddlres = ddl1-ddl2;
		for(int i=0; i<res.length; i++) {
			SetToParse set = getSetToParse(design.getFactor(factor1), i);
			double st = computeLocalST(set);
			double ss = computeLocalSS(set);
			double sst = ss-st;
			double ssf2 = computeLocalSST(design.getFactor(factor2), set)-st;
			double resi = sst-ssf2;
			res[i][0] =ssf2/ddl2;
//			res[i][1] = resi/ddlres;
			res[i][1] = (ddlres*ssf2)/(ddl2*resi);
		}
		return res;
	}
	
	public double [][] computeLocalTerSqrt(double d[][], int factor1, int factor2) {
		if(factor1 == factor2)
			return null;
		data = d;
		int cardf = getCard(design.getFactor(factor1));
		double[][] res = new double[cardf][2];
		int f1, f2;
		if(factor1<factor2) {
			f1 = factor1; f2 = factor2;
		} else {
			f1 = factor2; f2 = factor1;
		}
		SetToParse set0 = getSetToParse(design.getFactor(factor1), 0);
		double ddl2 = (double) getCard(design.getFactor(factor2))-1;
		double ddlres = ((double) set0.getCard(d.length, d[0].length)-1)-ddl2;
		for(int i=0; i<res.length; i++) {
			SetToParse set = getSetToParse(design.getFactor(factor1), i);
			double st = computeLocalST(set);
			double ss = computeLocalSS(set);
			double sst = ss-st;
			double ssf2 = computeLocalSST(design.getFactor(factor2), set)-st;
			double resi = sst-ssf2;
			res[i][0] = Math.sqrt(ssf2/ddl2);
//			res[i][1] = Math.sqrt(resi/ddlres);
			res[i][1] = (ddlres*ssf2)/(ddl2*resi);
		}
		return res;
	}

	public double [][] computeLocalTer2Sqrt(double d[][], int factor1, int factor2) {
		if(factor1 == factor2)
			return null;
		data = d;
		int cardf = getCard(design.getFactor(factor1));
		double[][] res = new double[cardf][2];
		int f1, f2;
		if(factor1<factor2) {
			f1 = factor1; f2 = factor2;
		} else {
			f1 = factor2; f2 = factor1;
		}
		double sTerm, sTot, sFact2;
		SetToParse all = new SetToParse(null, null);
		sTerm = computeLocalST(all);
		sTot = computeLocalSS(all)-sTerm;
		sFact2 = computeLocalSST(design.getFactor(factor2), all)-sTerm;
		for(int i=0; i<res.length; i++) {
			SetToParse set = getSetToParse(design.getFactor(factor1), i);
			double st = computeLocalST(set);
			res[i][0] = Math.sqrt((computeLocalSST(design.getFactor(factor2), set)-st)/sFact2);
			res[i][1] = Math.sqrt((computeLocalSS(set) - st)/sTot);
		}
		return res;
	}


	public double [][] computeLocalBis2Sqrt(double d[][], int factor1, int factor2) {
		if(factor1 == factor2)
			return null;
		data = d;
		int cardf = getCard(design.getFactor(factor1));
		double[][] res = new double[cardf][2];
		int f1, f2;
		if(factor1<factor2) {
			f1 = factor1; f2 = factor2;
		} else {
			f1 = factor2; f2 = factor1;
		}
		for(int i=0; i<res.length; i++) {
			SetToParse set = getSetToParse(design.getFactor(factor1), i);
			double st = computeLocalST(set);
			double ss = computeLocalSS(set);
			double sst = ss-st;
			double ssf2 = computeLocalSST(design.getFactor(factor2), set)-st;
			int tabFact[] = new int[design.getNumberOfFactors()-2];
			int ind = 0;
			for(int f=0; f<f1; f++)
				tabFact[ind++] = f;
			for(int f=f1+1; f<f2; f++)
				tabFact[ind++] = f;
			for(int f=f2+1; f<design.getNumberOfFactors(); f++)
				tabFact[ind++] = f;
			double resi = ss-computeSti(0, design.getNumberOfFactors()-3, tabFact, set);
			res[i][0] = Math.sqrt(ssf2/sst);
			res[i][1] = Math.sqrt(resi/sst);
		}
		return res;
	}
	
	public double [][] computeLocalSqrt2(double d[][], int factor1, int factor2) {
		if(factor1 == factor2)
			return null;
		data = d;
		int cardf = getCard(design.getFactor(factor1));
		double[][] res = new double[cardf][2];
		int f1, f2;
		if(factor1<factor2) {
			f1 = factor1; f2 = factor2;
		} else {
			f1 = factor2; f2 = factor1;
		}
		for(int i=0; i<res.length; i++) {
			SetToParse set = getSetToParse(design.getFactor(factor1), i);
			double st = computeLocalST(set);
			double ss = computeLocalSS(set);
			double sst = ss-st;
			double ssf2 = computeLocalSST(design.getFactor(factor2), set)-st;
			double resi = sst-ssf2;
			for(int f=0; f<f1; f++)
				resi -= computeLocalSST(design.getFactor(f), set)-st;
			for(int f=f1+1; f<f2; f++)
				resi -= computeLocalSST(design.getFactor(f), set)-st;
			for(int f=f2+1; f<design.getNumberOfFactors(); f++)
				resi -= computeLocalSST(design.getFactor(f), set)-st;
			res[i][0] = Math.sqrt(ssf2/sst);
			res[i][1] = Math.sqrt(resi/sst);
		}
		return res;
	}
	
	public double [][] computeLocal4Sqrt(double d[][], int factor1, int factor2) {
		if(factor1 == factor2)
			return null;
		data = d;
		int cardf = getCard(design.getFactor(factor1));
		double[][] res = new double[cardf][2];
		int f1, f2;
		if(factor1<factor2) {
			f1 = factor1; f2 = factor2;
		} else {
			f1 = factor2; f2 = factor1;
		}
		for(int i=0; i<res.length; i++) {
			SetToParse set = getSetToParse(design.getFactor(factor1), i);
			double st = computeLocalST(set);
			double ss = computeLocalSS(set);
			double sst = ss-st;
			double ssf2 = computeLocalSST(design.getFactor(factor2), set)-st;
			double resi = 0;
			for(int f=0; f<f1; f++)
				resi += computeLocalSST(design.getFactor(f), set)-st;
			for(int f=f1+1; f<f2; f++)
				resi += computeLocalSST(design.getFactor(f), set)-st;
			for(int f=f2+1; f<design.getNumberOfFactors(); f++)
				resi += computeLocalSST(design.getFactor(f), set)-st;
			res[i][0] = Math.sqrt(ssf2/sst);
			res[i][1] = Math.sqrt(resi/sst);
		}
		return res;
	}
	
	public double [][] computeLocalTer2(double d[][], int factor1, int factor2) {
		if(factor1 == factor2)
			return null;
		data = d;
		int cardf = getCard(design.getFactor(factor1));
		double[][] res = new double[cardf][2];
		int f1, f2;
		if(factor1<factor2) {
			f1 = factor1; f2 = factor2;
		} else {
			f1 = factor2; f2 = factor1;
		}
		double sTerm, sTot, sFact2;
		SetToParse all = new SetToParse(null, null);
		sTerm = computeLocalST(all);
		sTot = computeLocalSS(all)-sTerm;
		sFact2 = computeLocalSST(design.getFactor(factor2), all)-sTerm;
		for(int i=0; i<res.length; i++) {
			SetToParse set = getSetToParse(design.getFactor(factor1), i);
			double st = computeLocalST(set);
			res[i][0] = (computeLocalSST(design.getFactor(factor2), set)-st)/sFact2;
			res[i][1] = (computeLocalSS(set) - st)/sTot;
		}
		return res;
	}


	public double [][] computeLocalBis2(double d[][], int factor1, int factor2) {
		if(factor1 == factor2)
			return null;
		data = d;
		int cardf = getCard(design.getFactor(factor1));
		double[][] res = new double[cardf][2];
		int f1, f2;
		if(factor1<factor2) {
			f1 = factor1; f2 = factor2;
		} else {
			f1 = factor2; f2 = factor1;
		}
		for(int i=0; i<res.length; i++) {
			SetToParse set = getSetToParse(design.getFactor(factor1), i);
			double st = computeLocalST(set);
			double ss = computeLocalSS(set);
			double sst = ss-st;
			double ssf2 = computeLocalSST(design.getFactor(factor2), set)-st;
			int tabFact[] = new int[design.getNumberOfFactors()-2];
			int ind = 0;
			for(int f=0; f<f1; f++)
				tabFact[ind++] = f;
			for(int f=f1+1; f<f2; f++)
				tabFact[ind++] = f;
			for(int f=f2+1; f<design.getNumberOfFactors(); f++)
				tabFact[ind++] = f;
			double resi = ss-computeSti(0, design.getNumberOfFactors()-3, tabFact, set);
			res[i][0] = ssf2/sst;
			res[i][1] = resi/sst;
		}
		return res;
	}
	
	public double [][] computeLocal2(double d[][], int factor1, int factor2) {
		if(factor1 == factor2)
			return null;
		data = d;
		int cardf = getCard(design.getFactor(factor1));
		double[][] res = new double[cardf][2];
		int f1, f2;
		if(factor1<factor2) {
			f1 = factor1; f2 = factor2;
		} else {
			f1 = factor2; f2 = factor1;
		}
		for(int i=0; i<res.length; i++) {
			SetToParse set = getSetToParse(design.getFactor(factor1), i);
			double st = computeLocalST(set);
			double ss = computeLocalSS(set);
			double sst = ss-st;
			double ssf2 = computeLocalSST(design.getFactor(factor2), set)-st;
			double resi = sst-ssf2;
			for(int f=0; f<f1; f++)
				resi -= computeLocalSST(design.getFactor(f), set)-st;
			for(int f=f1+1; f<f2; f++)
				resi -= computeLocalSST(design.getFactor(f), set)-st;
			for(int f=f2+1; f<design.getNumberOfFactors(); f++)
				resi -= computeLocalSST(design.getFactor(f), set)-st;
			res[i][0] = ssf2/sst;
			res[i][1] = resi/sst;
		}
		return res;
	}
	public double [][] computeLocal4(double d[][], int factor1, int factor2) {
		if(factor1 == factor2)
			return null;
		data = d;
		int cardf = getCard(design.getFactor(factor1));
		double[][] res = new double[cardf][2];
		int f1, f2;
		if(factor1<factor2) {
			f1 = factor1; f2 = factor2;
		} else {
			f1 = factor2; f2 = factor1;
		}
		for(int i=0; i<res.length; i++) {
			SetToParse set = getSetToParse(design.getFactor(factor1), i);
			double st = computeLocalST(set);
			double ss = computeLocalSS(set);
			double sst = ss-st;
			double ssf2 = computeLocalSST(design.getFactor(factor2), set)-st;
			double resi = 0;
			for(int f=0; f<f1; f++)
				resi += computeLocalSST(design.getFactor(f), set)-st;
			for(int f=f1+1; f<f2; f++)
				resi += computeLocalSST(design.getFactor(f), set)-st;
			for(int f=f2+1; f<design.getNumberOfFactors(); f++)
				resi += computeLocalSST(design.getFactor(f), set)-st;
			res[i][0] = ssf2/sst;
			res[i][1] = resi/sst;
		}
		return res;
	}
*/