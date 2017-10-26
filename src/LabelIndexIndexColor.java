import java.awt.Color;

class LabelIndexIndexColor extends LabelIndex {
	int indexColor = 0;
	boolean mark = false;
	
	public LabelIndexIndexColor(String l, int i) {
		super(l, i);
	}
	
	public LabelIndexIndexColor(String l, int i, int c) {
		super(l, i);
		indexColor = c;
	}

	public int getIndexColor() {
		return indexColor;
	}
	public void setIndexColor(int c) {
		indexColor = c;
	}
	public boolean getMark() {
		return mark;
	}
	public void setMark(boolean m) {
		mark = m;
	}
}
