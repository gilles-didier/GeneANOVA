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
import java.awt.geom.Ellipse2D;

public class PointPCAVarPanel extends PointPCAPanel {
	public PointPCAVarPanel(PCACompute p, TableExp t, ColorTable ct, ColorTable sp, ActionListener cA, int di) {
		super(p, t, ct, sp, cA, di);
	}
	public void finishCreate(ListPoint m, boolean same, boolean reg, ColorTable ct, ColorTable sp, Line l[]) {
		super.finishCreate(m, true, false, ct, sp, l);
		pointReg = new PointComponent(myListPoint, same, colorTable, colorSpecial, null, new MaintainListAction());
		Ellipse2D.Double e[] = new Ellipse2D.Double[1];
		e[0] = new Ellipse2D.Double(-1, 1, 2,2);
		pointReg.setEllipses(e);
		pointReg.setMinMax(-1,1,-1,1);
		pointReg.setListModel(modelSel);
	}

	protected PointPCAPanel.PCAListPoint getPCAListPoint() {
		return new PCAListPoint(pca.getVariablesCoordinates(abs, ord), pca.getVariablesCorrelations(abs, ord), tableExp.getColLabels());
	}

	protected void updatePCAListPoint() {
		pointReg.setListPointBis(new PCAListPoint(pca.getVariablesCoordinates(abs, ord), pca.getVariablesCorrelations(abs, ord), tableExp.getColLabels()));
		pointReg.setMinMax(-1,1,-1,1);
	}
}

