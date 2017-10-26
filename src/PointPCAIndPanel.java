import java.awt.*;
import java.util.*;
import java.awt.List;
import java.awt.event.*;
import javax.swing.*;
import java.text.*;
import sun.awt.image.codec.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class PointPCAIndPanel extends PointPCAPanel {
	
	public PointPCAIndPanel(PCACompute p, TableExp t, ColorTable ct, ColorTable sp, ActionListener cA, int di) {
		super(p, t, ct, sp, cA, di);
	}
	
	public void finishCreate(ListPoint m, boolean same, boolean reg, ColorTable ct, ColorTable sp, Line l[]) {
		super.finishCreate(m, true, false, ct, sp, l);
		pointReg = new PointComponent(myListPoint, same, colorTable, colorSpecial, pca.getLines(abs, ord, tableExp.getColLabels()), new MaintainListAction());
		pointReg.setListModel(modelSel);
	}
	
	protected PointPCAPanel.PCAListPoint getPCAListPoint() {
		return new PCAListPoint(pca.getCoordinates(abs, ord), pca.getCorrelations(abs, ord), tableExp.getRowLabels());
	}

	protected void updatePCAListPoint() {
		pointReg.setListPointBis(new PCAListPoint(pca.getCoordinates(abs, ord), pca.getCorrelations(abs, ord), tableExp.getRowLabels()));
		pointReg.setLines(pca.getLines(abs, ord, tableExp.getColLabels()));
	}
}

