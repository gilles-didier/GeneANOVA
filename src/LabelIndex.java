import java.lang.*;
import java.io.*;

class LabelIndex extends Object implements Comparable {
	String label;
	int index;
	
	public LabelIndex(String l, int i) {
		label = l;
		index = i;
	}
	
	public String toString() {
		return label;
	}
	public String getLabel() {
		return label;
	}
	public int getIndex() {
		return index;
	}
	public int compareTo(Object o) {
		if(o instanceof LabelIndex)
			return label.compareTo(o.toString());
		if(o instanceof String)
			return label.compareTo(o);
		return 0;
	}
}
