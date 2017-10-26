/*
 * @(#)ImagePad.java	1.13 98/08/28
 *
 * Copyright 1997, 1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.net.URL;
import java.util.*;

import javax.swing.text.*;
import javax.swing.undo.*;
import javax.swing.event.*;
import javax.swing.*;
import sun.awt.image.codec.*;
import java.awt.image.BufferedImage;

/**
 * Sample application using the simple text editor component that
 * supports only one font.
 *
 * @author  Timothy Prinzing
 * @version 1.13 08/28/98
 */
class ImagePad extends StandardPad {
	private BufferedImage image;

	ImagePad(BufferedImage im) {
		super();
		setBorder(BorderFactory.createEtchedBorder());
		finishCreate();
		image = im;
		add("Center", new JScrollPane(new JLabel(new ImageIcon(image))));
	}

	public String getResourceName() {
		return "resources.ImagePad";
	}
	
	public Action[] getActions() {
		return defaultActions;
	}


	public static final String saveJpegAction = "saveJpeg";
	public static final String closeAction = "close";
 

    // --- action implementations -----------------------------------
    private Action[] defaultActions = {
		new SaveJpegAction(),
		new CloseAction()
    };

 	class SaveJpegAction extends AbstractAction {
		SaveJpegAction() {
			super(saveJpegAction);
		}
		public void actionPerformed(ActionEvent e) {
			File f = GeneralUtil.getFileSaveAs(ImagePad.this);
			if(f == null)
				return;
			if (f.exists()) {
				try {
					FileOutputStream out = new FileOutputStream(f);
					JPEGImageEncoderImpl jpegEnc = new JPEGImageEncoderImpl(out);
					jpegEnc.encode(image);
					out.close();
				} catch(IOException ioe){
					GeneralUtil.showError(ImagePad.this, "IO error : \n"+ioe);
				}
			}
		}		
	}

	class CloseAction extends AbstractAction {
		CloseAction() {
			super(closeAction);
		}
		public void actionPerformed(ActionEvent e) {
			getFrame().dispose();
		}
	}
}
