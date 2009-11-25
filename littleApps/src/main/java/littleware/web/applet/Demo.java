package littleware.web.applet;

import javax.swing.*;         
import java.awt.*;


/**
 * Applet demo from the freakin java tutorial
 */
public class Demo extends JApplet {
	
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

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

