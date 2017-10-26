import java.lang.*;
import java.io.*;
import java.text.ParseException;
import java.util.*;

class Factor extends Object {
	String name;
	public static final int ALL_ROWS = 0;
	public static final int ALL_COLUMNS = 1;
	public static final int PARTIAL_ROWS = 2;
	public static final int PARTIAL_COLUMNS = 3;
	int [][] tabIndex;
	int type, card;

	public Factor(String na, int t, int np) {
		name = na;
		type = t;
		if(type == PARTIAL_ROWS || type == PARTIAL_COLUMNS) {
			card = np;
			tabIndex = new int[np][];
		}
	}

	public Factor(BufferedReader in) throws ParseException {
		String lineCur;
		int n;
		try {
			lineCur = in.readLine();
			StringTokenizer st = new StringTokenizer(lineCur, "=");
			if(st.nextToken().equals(nameString)) {
				name = st.nextToken();
			} else {
				throw new ParseException("problemIN FACT Name", 0);
			}			
			lineCur = in.readLine();
			st = new StringTokenizer(lineCur, "=");
			if(st.nextToken().equals(typeString)) {
				type = Integer.parseInt(st.nextToken());
			} else {
				throw new ParseException("problemIN FACT Type", 0);
			}			
			if(type == PARTIAL_ROWS || type == PARTIAL_COLUMNS) {
				Vector v = new Vector();
				lineCur = in.readLine();
				while(!lineCur.startsWith(endString)) {
					st = new StringTokenizer(lineCur, "=");
					if(st.nextToken().equals(atomString)) {
						StringTokenizer stAt = new StringTokenizer(st.nextToken(), " \t");
						int[] at = new int[stAt.countTokens()];
						for(int i=0; i<at.length && stAt.hasMoreTokens(); i++) {
							at[i] = Integer.parseInt(stAt.nextToken());
						}
						v.add(at);
					}
					lineCur = in.readLine();
				}
				card = v.size();
				tabIndex = new int[card][];
				int i=0;
				for (Enumeration e = v.elements() ; e.hasMoreElements() ; i++) {
					tabIndex[i] = (int[]) e.nextElement();
					Arrays.sort(tabIndex[i]);
				}
			}

		} catch(IOException ioe) {
			throw new ParseException("problemIO FACT", 0);
		} catch(NumberFormatException nfe) {
			throw new ParseException("problemIN FACT", 0);
		}
	}
	public int getType() {
		return type;
	}
	public int[][] getPart() {
		return tabIndex;
	}
	public int[] getPart(int i) {
		return tabIndex[i];
	}
	public int getCard() {
		return card;
	}
	public void write(BufferedWriter out) throws IOException {
		out.write(nameString+"="+name);
		out.newLine();
		out.write(typeString+"="+type);
		out.newLine();
		if(type == PARTIAL_ROWS || type == PARTIAL_COLUMNS) {
			for(int i=0; i<card; i++) {
				out.write(atomString+"="+tabIndex[i][0]);
				for(int j=1; j<tabIndex[i].length; j++)
					out.write(" "+tabIndex[i][j]);
				out.newLine();
			}
			out.write(endString);
			out.newLine();
		}
	}
	
	public void setAtom(int i, int l[]) {
		tabIndex[i] = l;
		Arrays.sort(tabIndex[i]);
	}
	public String getName() {
		return name;
	}
	public boolean isCompatibleWith(TableExp t) {
		int max = 0;
		switch(type) {
			case Factor.ALL_ROWS :
			case Factor.ALL_COLUMNS :
				return true;
			case Factor.PARTIAL_ROWS :
				max = t.getNumberOfRows();
				break;
			case Factor.PARTIAL_COLUMNS :
				max = t.getNumberOfCols();
				break;
		}			
		for(int i=0; i<tabIndex.length; i++) {
			int j;
			for(j=0; j<tabIndex[i].length && tabIndex[i][j]<max; j++)
			;
			if(j<tabIndex[i].length)
				return false;
		}
		return true;
	}
	private static final String nameString = "name";
	private static final String typeString = "type";
	private static final String atomString = "atom";
	private static final String endString = "end";
}
