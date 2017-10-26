import java.util.*;
import java.lang.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;
import java.io.*;
/**
 * <CODE>TableReader</CODE> is class used for reading a table of double numbers from a string.
 * Items in each line have to be separated by white spaces or tab character.
 * Lines have to be separated by new line or carriage return character. 
 *
 * @version 10 mar 2000
 * @author	Gilles Didier
 */

public class TableReader extends Object{
	private int numberOfRows=0, numberOfColumns=0, sizeOfTableau=0;
	private String colLabel[], rowLabel[];
	private double table[];
	private JProgressBar progress;
	private boolean firstRowIsLabels, firstColIsLabels ;
	private static final double BAD = Double.NaN;
	private static final NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
	private boolean log = false;
/**
 * Constructs a new <CODE>TableReader</CODE>.
 * <CODE>fR</CODE> to <CODE>true</CODE> indicates the first row of the table contains labels.
 * <CODE>fC</CODE> to <CODE>true</CODE> indicates the first column of the table contains labels.
*/ 
	public TableReader(boolean fR, boolean fC, boolean l) {
		firstRowIsLabels = fR;
		firstColIsLabels = fC;
		log = l;
	}
	public TableReader(boolean fR, boolean fC) {
		this(fR, fC, false);
	}

/**
 * Constructs a new <CODE>TableReader</CODE>.
 * Assumes both the first Row and the first column are labels.
*/ 
	public TableReader() {
		this(true, true, false);
	}
	
/**
 * Sets the progress bar to visualize progress of the reading.
*/ 
	public void setProgress(JProgressBar p) {
		progress = p;
	}

/**
 * Reads the table from the string <CODE>textTable</CODE>.
*/ 
	public TableExp read(BufferedReader in, boolean fR, boolean fC) throws IOException{
		firstRowIsLabels = fR;
		firstColIsLabels = fC;
		String curString;
		Vector lineVect = new Vector();
		Vector labelVect = new Vector();
		int tot = 0;
		if(progress != null) {
			tot = progress.getValue();
		}
		if(firstRowIsLabels) {
			String colString;
			colString = in.readLine();
			curString = in.readLine();
			if(curString == null) {
				throw(new IOException("Empty"));
//				return null;
			}
			StringTokenizer colTok = new StringTokenizer(colString, "\t");
			StringTokenizer curTok = new StringTokenizer(curString, "\t");
			if(firstColIsLabels)
				numberOfColumns = curTok.countTokens()-1;
			else
				numberOfColumns = curTok.countTokens();
			colLabel = new String[numberOfColumns];
			while(colTok.countTokens() > numberOfColumns)
				colTok.nextToken();
			int i;
			for(i=0; colTok.hasMoreTokens(); i++) {
				String tmpString = colTok.nextToken();
				tmpString = formatLabelString(tmpString);
				colLabel[i] = tmpString;
			}
			for(int j=i; j<numberOfColumns; j++) {
				colLabel[j] = Integer.toString(j);
			}
			if(progress != null) {
				progress.setValue(tot += colString.length()+1);
			}
		} else {
			curString = in.readLine();
			StringTokenizer curTok = new StringTokenizer(curString, "\t");
			if(firstColIsLabels)
				numberOfColumns = curTok.countTokens()-1;
			else
				numberOfColumns = curTok.countTokens();
			colLabel = new String[numberOfColumns];
			for(int j=0; j<numberOfColumns; j++) {
				colLabel[j] = Integer.toString(j);
			}
		}
		int ind = 0;
		while(curString != null) {
			curString = curString.replace(',', '.');
			StringTokenizer curTok = new StringTokenizer(curString, ";\t");
			if((firstColIsLabels && curTok.countTokens() == numberOfColumns+1) || (!firstColIsLabels && curTok.countTokens() == numberOfColumns)) {
				if(curTok.hasMoreTokens() && firstColIsLabels) {
					String tmpString = curTok.nextToken();
					tmpString = formatLabelString(tmpString);
					labelVect.add(tmpString);
				} else {
					labelVect.add(Integer.toString(ind++));
				}
				double[] curLine = new double[numberOfColumns];
				for(int i=0; i<numberOfColumns && curTok.hasMoreTokens(); i++) {
					String tmpString = curTok.nextToken();
					tmpString.replace(',', '.');
					tmpString = formatNumberString(tmpString);
					try{
						curLine[i] = nf.parse(tmpString).doubleValue();
					} catch (ParseException e) {
						curLine[i] = BAD ;
	   				}
				}
				lineVect.add(curLine);
			}
			if(progress != null) {
				progress.setValue(tot += curString.length()+1);
			}
			curString = in.readLine();
		}
/*		while(curString != null) {
			curString = curString.replace(',', '.');
			StringTokenizer curTok = new StringTokenizer(curString, ";\t");
			if(curTok.hasMoreTokens() && firstColIsLabels) {
				String tmpString = curTok.nextToken();
				tmpString = formatLabelString(tmpString);
				labelVect.add(tmpString);
			} else {
				labelVect.add(Integer.toString(ind++));
			}
			if(curTok.countTokens() != numberOfColumns) {
				throw(new IOException(Integer.toString(ind)+ " --- "+curString));
			} else {
			double[] curLine = new double[numberOfColumns];
			for(int i=0; i<numberOfColumns && curTok.hasMoreTokens(); i++) {
				String tmpString = curTok.nextToken();
				tmpString.replace(',', '.');
				tmpString = formatNumberString(tmpString);
				try{
					curLine[i] = nf.parse(tmpString).doubleValue();
				} catch (ParseException e) {
					curLine[i] = BAD ;
	   			}
			}
			lineVect.add(curLine);
			if(progress != null) {
				progress.setValue(tot += curString.length()+1);
			}
			curString = in.readLine();
		}
*/		numberOfRows = lineVect.size();
		rowLabel = new String[numberOfRows];
		double[][] data = new double[numberOfRows][];
		int i = 0;
		for (Enumeration e = lineVect.elements(), f = labelVect.elements(); e.hasMoreElements() && i<numberOfRows ;i++) {
			data[i] = (double[]) e.nextElement();
			rowLabel[i] = (String) f.nextElement();

		}
		return new TableExp(data, colLabel, rowLabel);
	}


	public static String formatNumberString(String s) {
		char[] upper = s.toCharArray();
		int ind = 0;
		for(int k=0; k<upper.length; k++)
		if((upper[k]>='0' && upper[k]<='9') || upper[k]==',' || upper[k]=='.' || upper[k]=='-')
			upper[ind++] = upper[k];
		return new String(upper, 0, ind);
	}
	
	private String formatLabelString(String s) {
		char tmp[] = s.toCharArray();
		int start, end;
		for(start=0; start<tmp.length && (tmp[start] == ' '); start++)
		;
		for(end=tmp.length-1; end>=0 && (tmp[end] == ' '); end--)
		;
		if((start != 0) || (end != tmp.length-1))
			return new String(tmp, start, end-start+1);
		else
			return s;
	}

/**
 * Returns the table read as a <CODE>TableModel</CODE> object.
*/ 
	public TableModel getTable() {
		final Object[][] data = new Object[numberOfRows][numberOfColumns];
		int i,j;
		for(i=0; i<numberOfRows; i++)
			for(j=0; j<numberOfColumns; j++)
				data[i][j] = new Double(table[i*numberOfColumns+j]);
			TableModel dataModel = new AbstractTableModel() {
            	public int getColumnCount() { return numberOfColumns; }
           		public int getRowCount() { return numberOfRows;}
			public Object getValueAt(int row, int col) {return data[row][col];}
			public String getColumnName(int column) {return colLabel[column];}
			public Class getColumnClass(int c) {return getValueAt(0, c).getClass();}
			public boolean isCellEditable(int row, int col) {return false;}
			public void setValueAt(Object aValue, int row, int column) { ; }
		};
		return dataModel;
	}

/**
 * Returns the number of columns of the table read.
*/ 
	public int getNumberOfColumns() {
		return numberOfColumns;
	}

/**
 * Returns the number of rows of the table read.
*/ 
	public int getNumberOfRows() {
		return numberOfRows;
	}

/**
 * Returns the table of the values.
*/ 
	public double[] getDataBis() {
		return table;
	}
/**
 * Returns the table of the label of columns from the table read.
*/ 
	public Object[] getRowsLabels() {
		return rowLabel;
	}

/**
 * Returns the table of the label of rows from the table read.
*/ 
	public Object[] getColumnsLabels(){
		return colLabel;
	}

}