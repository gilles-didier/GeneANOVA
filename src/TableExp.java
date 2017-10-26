import java.lang.*;
import javax.swing.table.*;
import java.io.*;
import java.text.NumberFormat;
import java.util.Arrays;

public class TableExp extends Object{
	double[][] data;
	String[] colLabel, rowLabel;
	int numberOfRows, numberOfColumns;
	private static  NumberFormat nf = NumberFormat.getInstance();
	private static final int MAX_FRAC = 2;
	private boolean modified;
	private double log10 = Math.log(10);
	
	public TableExp(double d[][], String c[], String r[]) {
		data = d;
		colLabel = c;
		rowLabel = r;
		numberOfRows = data.length;
		numberOfColumns = data[0].length;
		nf.setMaximumFractionDigits(MAX_FRAC);
		nf.setMinimumFractionDigits(MAX_FRAC);
		modified = false;
	}
	public boolean isModified() {
		return modified;
	}
	public void setModified(boolean m) {
		modified = m;
	}
	public int getNumberOfRows() {
		return numberOfRows;
	}	
	public int getNumberOfCols() {
		return numberOfColumns;
	}	
	public double[][] getDatas() {
		return data;
	}

	public String[] getRowLabels() {
		return rowLabel;
	}

	public String[] getColLabels() {
		return colLabel;
	}
	
	public String[] getRowLabelsCopy() {
		String[] res = new String[rowLabel.length];
		for(int i = 0; i<rowLabel.length; i++)
			res[i] = rowLabel[i];
		return res;
	}
	
	public LabelIndex[] getRowLabelsIndexCopy() {
		LabelIndex[] res = new LabelIndex[rowLabel.length];
		for(int i = 0; i<rowLabel.length; i++)
			res[i] = new LabelIndex(rowLabel[i], i);
		return res;
	}
	
	public LabelIndex[] getColLabelsIndexCopy() {
		LabelIndex[] res = new LabelIndex[colLabel.length];
		for(int i = 0; i<colLabel.length; i++)
			res[i] = new LabelIndex(colLabel[i], i);
		return res;
	}
	
	public TableModel getTableModel() {
		TableModel dataModel = new AbstractTableModel() {
      public int getColumnCount() { return numberOfColumns; }
      public int getRowCount() { return numberOfRows;}
			public Object getValueAt(int row, int col) {
				if(row<data.length && col<data[row].length)
					return new Double(data[row][col]);
				else
					return "";
			}
			public String getColumnName(int column) {return colLabel[column];}
			public Class getColumnClass(int c) {return getValueAt(0, c).getClass();}
			public boolean isCellEditable(int row, int col) {return true;}
			public void setValueAt(Object aValue, int row, int column) { 
				if(aValue instanceof Number) {
					data[row][column] = ((Number) aValue).doubleValue();
					modified = true;
				}
		 	}
		};
		return dataModel;
	}


	public void toCenterReduce() {
		for(int j = 0; j<numberOfColumns; j++) {
			double m = 0;
			for(int i = 0; i<numberOfRows; i++)
				m += data[i][j];
			m /= numberOfRows;
			double v = 0;
			for(int i = 0; i<numberOfRows; i++)
				v += Math.pow(data[i][j]-m, 2);
			v /= numberOfRows-1;
			v = Math.sqrt(v);
			for(int i = 0; i<numberOfRows; i++)
				data[i][j] = (data[i][j]-m)/v;
		}
		modified = true;
	}

	public void toLog() {
		for(int i = 0; i<numberOfRows; i++)
			for(int j = 0; j<numberOfColumns; j++) {
				if(data[i][j] <= 0) {
					data[i][j] = 0;
				} else {
					data[i][j] = (Math.log(data[i][j]))/log10;
				}	
			}
		modified = true;
	}

	public void toSqrt() {
		for(int i = 0; i<numberOfRows; i++)
			for(int j = 0; j<numberOfColumns; j++) {
				if(data[i][j] < 0) {
					data[i][j] = 0;
				} else {
					data[i][j] = Math.sqrt(data[i][j]);
				}	
			}
		modified = true;
	}
	
	public void toRank() {
		int index[] = new int[numberOfRows];
		for(int col = 0; col<numberOfColumns; col++) {
			for(int i = 0; i<numberOfRows; i++)
				index[i] = i;
			QuickSort(col, index, 0, numberOfRows-1);
			for(int i = 0; i<numberOfRows;) {
				if(i == numberOfRows-1 || data[index[i]][col]<data[index[i+1]][col]) {
					data[index[i]][col] = i++ +1;
				} else {
					int n;
					double som = 2*i+3;
					for(n=2; i+n<numberOfRows && data[index[i+n-1]][col]==data[index[i+n]][col]; n++)
						som += i+1+n;
					som /= n;
					for(int j=0; j<n; j++)
						data[index[i++]][col] = som;
				}
			}
		}	
		modified = true;
	}

	public void extractRows(int sel[]) {
		int cur = 0;
		double[][] dbis = new double[sel.length][];
		String[] rowBis = new String[sel.length];
		for(int i=0; i<sel.length; i++) {
			dbis[i] = data[sel[i]];
			rowBis[i] = rowLabel[sel[i]];
		}
		numberOfRows = sel.length;
		rowLabel = rowBis;
		data = dbis;
		modified = true;
	}

	public void sortRows() {
		LabelIndex tab[] = getRowLabelsIndexCopy();
		Arrays.sort(tab);
		double[][] dbis = new double[numberOfRows][];
		String[] rowBis = new String[numberOfRows];
		for(int i=0; i<numberOfRows; i++) {
			dbis[i] = data[tab[i].getIndex()];
			rowBis[i] = rowLabel[tab[i].getIndex()];
		}
		rowLabel = rowBis;
		data = dbis;
		modified = true;
	}
	private void swapRow(int i, int j) {
		if(i != j) {
			String stmp = rowLabel[i];
			double[] ttmp = data[i];
			rowLabel[i] = rowLabel[j];
			data[i] = data[j];
			rowLabel[j] = stmp;
			data[j] = ttmp;
		}
	}

	private double euclidDistByRow(int i, int j) {
		double sum = 0;
		for(int k=0; k<numberOfColumns; k++) {
			double diff = data[i][k]-data[j][k];
			sum += diff*diff;
		}
		return Math.sqrt(sum);
	}
		
 
	private double euclidDistByColumn(int i, int j) {
		double sum = 0;
		for(int k=0; k<numberOfRows; k++) {
			double diff = data[k][i]-data[k][j];
			sum += diff*diff;
		}
		return Math.sqrt(sum);
	}
		
	public void writeDistByRow(BufferedWriter out) throws IOException {
		for(int i=1; i<numberOfRows; i++) {
			for(int j=0; j<i; j++)
				out.write(Double.toString(euclidDistByRow(i,j))+" ");
			out.newLine();
		}
	}

	public void writeDistByColumn(BufferedWriter out)   throws IOException {
		for(int i=1; i<numberOfColumns; i++) {
			for(int j=0; j<i; j++)
				out.write(Double.toString(euclidDistByColumn(i,j))+" ");
			out.newLine();
		}
	}

	public void writeDatas(BufferedWriter out) throws IOException {
		for(int i=0; i<numberOfRows; i++) {
			out.write(Double.toString(data[i][0]));
			for(int j=1; j<numberOfColumns; j++)
				out.write('\t'+Double.toString(data[i][j]));
			out.newLine();
		}
	}

	public void writeDatasTransposed(BufferedWriter out) throws IOException {
		for(int j=0; j<numberOfColumns; j++) {
			out.write(Double.toString(data[0][j]));
			for(int i=1; i<numberOfRows; i++)
				out.write('\t'+Double.toString(data[i][j]));
			out.newLine();
		}
	}
	
	public void writeTable(BufferedWriter out) throws IOException {	
		for(int j=0; j<numberOfColumns; j++)
			out.write('\t'+colLabel[j]);
		out.newLine();
		for(int i=0; i<numberOfRows; i++) {
			out.write(rowLabel[i]);
			for(int j=0; j<numberOfColumns; j++)
//				out.write('\t'+nf.format(data[i][j]));
				out.write('\t'+Double.toString(data[i][j]));
			out.newLine();
		}
	}
	private void QuickSort(int col, int a[], int lo0, int hi0) {
		int lo = lo0;
		int hi = hi0;
		double mid;
		if ( hi0 > lo0) {
             	mid = data[a[(lo0+hi0)/2]][col];
			while( lo <= hi ) {
				while( ( lo < hi0 ) && ( data[a[lo]][col] < mid ))
					++lo;
				while( ( hi > lo0 ) && ( data[a[hi]][col] > mid ))
					--hi;
 				if( lo <= hi ) {
					swap(a, lo, hi);
					++lo; --hi;
				}
			}
			if( lo0 < hi )
				QuickSort(col, a, lo0, hi );
 			if( lo < hi0 )
				QuickSort(col, a, lo, hi0 );
		}
	}

	private void swap(int a[], int i, int j) {
 		int T;
		T = a[i];
		a[i] = a[j];
		a[j] = T;
	}
}