import java.lang.*;
import java.io.*;
import java.util.*;
import java.text.StringCharacterIterator;
import javax.swing.tree.*;
import java.text.*;

public class GeneTreeReader extends Object {

	private static final String NODE = "NODE";
	private static final String LEAF = "LEAF";
	private static NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);

	public GeneTreeReader() {
	}

	public static TreeNode read(BufferedReader in) {
		return read(in, null, NODE, LEAF);
	}
	
	public static TreeNode read(BufferedReader in, String label[]) {
		return read(in, label, NODE, LEAF);
	}
	public static TreeNode read(BufferedReader in, String node, String leaf) {
		return read(in, null, node, leaf);
	}
	
	public static TreeNode read(BufferedReader in, String label[], String node, String leaf) {
		DefaultMutableTreeNode curNode = new DefaultMutableTreeNode();
		if(in == null)
			return curNode;
		String curString;
		StringWriter strWriter = new StringWriter();
		Hashtable hash = new Hashtable();
		do {
			try {
				curString = in.readLine();
			} catch(IOException ioe) {
				break;
			}
			if(curString == null || !curString.startsWith(NODE))
				break;
			StringTokenizer st = new StringTokenizer(curString, " \t");
			String nodeString = st.nextToken().substring(4);
			if(hash.containsKey(nodeString))
				break;
			curNode = new DefaultMutableTreeNode(nodeString);
			hash.put(nodeString, curNode);
			while(st.hasMoreTokens()) {
				String childString = st.nextToken();
				if(st.hasMoreTokens()) {
					DefaultMutableTreeNode childNode;
					if(childString.startsWith(node)) {
						childNode = (DefaultMutableTreeNode) hash.get(childString.substring(4));
					} else {
						String lab = childString.substring(leaf.length());
						if(label != null) {
							try {
								int ind = Integer.parseInt(lab);
								if(ind<label.length)
									childNode = new DefaultMutableTreeNode(label[ind]);
								else
									childNode = new DefaultMutableTreeNode(lab);
							} catch(NumberFormatException ne) {
								childNode = new DefaultMutableTreeNode(lab);
							}
						} else {
							childNode = new DefaultMutableTreeNode(lab);
						}
					}
					curNode.add(childNode);
				} else {
					float level;
					try {
						level = nf.parse(childString).floatValue();
					}  catch (ParseException e) {
						level = (float) 0.0;
	   			}
					curNode.setUserObject(new Float(level));
				}
			}
		} while(curString != null);
		return curNode.getRoot();	
	}
}