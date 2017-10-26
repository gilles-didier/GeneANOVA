import java.lang.*;
import java.io.*;
import java.util.StringTokenizer;
import java.text.ParseException;

class ANOVADesign extends Object {
	int numberOfFactors;
	Factor tabFactor[];
	
	public ANOVADesign(int n) {
		numberOfFactors = n;
		tabFactor = new Factor[numberOfFactors];
	}
	public ANOVADesign(BufferedReader in) throws ParseException {
		String lineCur;
		int n;
		try {
			lineCur = in.readLine();
			if(lineCur == null)
				throw new ParseException("problemIO ANOV", 0);
			StringTokenizer st = new StringTokenizer(lineCur, "=");
			if(st.nextToken().equals(numberFactorString)) {
				n = Integer.parseInt(st.nextToken());
				numberOfFactors = n;
				tabFactor = new Factor[numberOfFactors];
				for(int i=0; i<numberOfFactors; i++)
					tabFactor[i] = new Factor(in);
			}
		} catch(IOException ioe) {
			throw new ParseException("problemIO ANOV", 0);
		} catch(NumberFormatException nfe) {
			throw new ParseException("problemIN ANOV", 0);
		} catch(ParseException pe) {
			throw pe;
		}
	}
	public void write(BufferedWriter out) throws IOException {
		out.write(numberFactorString+"="+numberOfFactors);
		out.newLine();
		for(int i=0; i<numberOfFactors; i++)
			tabFactor[i].write(out);
	}
	public void setFactor(int i, Factor f) {
		tabFactor[i] = f;
	}
	public Factor getFactor(int i) {
		return tabFactor[i];
	}
	
	public LabelIndex[] getLabelsIndexCopyMinus(int non) {
		LabelIndex[] res = new LabelIndex[numberOfFactors-1];
		for(int i = 0; i<non; i++)
			res[i] = new LabelIndex(tabFactor[i].getName(), i);
		for(int i = non+1; i<numberOfFactors; i++)
			res[i-1] = new LabelIndex(tabFactor[i].getName(), i);
		return res;
	}
	
	public LabelIndex[] getLabelsIndexCopy() {
		LabelIndex[] res = new LabelIndex[numberOfFactors];
		for(int i = 0; i<numberOfFactors; i++)
			res[i] = new LabelIndex(tabFactor[i].getName(), i);
		return res;
	}
	
	public int getNumberOfFactors() {
		return numberOfFactors;
	}
	public boolean isCompatibleWith(TableExp t) {
		if(numberOfFactors == 0)
			return false;
		int i;
		for(i=0; i<numberOfFactors && tabFactor[i].isCompatibleWith(t); i++)
		;
		return i >= numberOfFactors;
	}
	private static final String numberFactorString = "NumberOfFactors";
}
