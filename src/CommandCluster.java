import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.beans.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.lang.*;
import java.text.*;
import java.lang.Character;
import sun.awt.image.codec.*;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;

public class CommandCluster extends Object {
	public String path;	
	public String name;


	public CommandCluster(String n, String p) {
		name = n;
		path = p;
	}

	public String constructCommandLine(String inPath, String outPath) {
		StringWriter writer = new StringWriter();
		writer.write(path);
		writer.write(" ");
		writer.write(inPath);
		writer.write(" ");
		writer.write(outPath);
		return writer.toString();
	}

	public Process getProcess(String inPath, String outPath)  throws IOException {
		String commandLine = constructCommandLine(inPath, outPath);
		return Runtime.getRuntime().exec(commandLine);
	}

	public String getName() {
		return name;
	}
	public void setName(String n) {
		name = n;
	}
}