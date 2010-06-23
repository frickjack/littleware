/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.web.applet;

import javax.swing.*;         
import java.awt.*;


/**
 * Applet demo from the freakin java tutorial
 */
public class Demo extends JApplet {
	
    @Override
    public void init() {
        JLabel w_label = new JLabel(
								  "Swing applet test - OK!");
        w_label.setHorizontalAlignment(JLabel.CENTER);
		
        //Add border.  Should use createLineBorder, but then the bottom
        //and left lines don't appear -- seems to be an off-by-one error.
        w_label.setBorder(BorderFactory.createMatteBorder(1,1,2,2,Color.black));
		
        getContentPane().add( w_label, BorderLayout.CENTER);
    }
}


