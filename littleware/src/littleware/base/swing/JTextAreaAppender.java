/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.base.swing;

import java.io.IOException;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Little class wraps a text area with an append manager.
 * The Appendable methods spool off to the SwingDispatchThread as necessary.
 */
public class JTextAreaAppender implements Appendable {

    private static final int MinBufferSize = 1024;
    private JTextArea textArea;
    private int bufferSize;

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public JTextArea getTextArea() {
        return textArea;
    }

    public void setTextArea(JTextArea textArea) {
        this.textArea = textArea;
    }

    public JTextAreaAppender(JTextArea textArea, int bufferSize) {
        this.textArea = textArea;
        this.bufferSize = bufferSize;
        if (this.bufferSize < MinBufferSize) {
            this.bufferSize = MinBufferSize;
        }
    }

    /**
     * Little Runnable for append to pass to SwingUtilities.invokeLater
     */
    private class AppendAction implements Runnable {

        private final String appendString;

        public AppendAction(String appendString) {
            this.appendString = appendString;
        }

        @Override
        public void run() {
            textArea.append(appendString);
            int size = textArea.getDocument().getLength();
            if (size > bufferSize) {
                textArea.replaceRange("", 0, (int)( size / 10.0 ));
            }
            /*...
            if (on_mode.equals(Mode.TAIL)) {
            textArea.setCaretPosition(textArea.getDocument().getLength());
            }
             */
        }
    }

    @Override
    public Appendable append(char c) {
        return this.append("" + c);
    }

    /**
     * Streams the CharSequence off to the Swing dispatch thread
     * asynchronously via SwingUtilities.invokeLater
     */
    @Override
    public Appendable append(CharSequence s_in) {
        final Runnable runner = new AppendAction(s_in.toString());
        if (SwingUtilities.isEventDispatchThread()) {
            runner.run();
        } else {
            SwingUtilities.invokeLater(runner);
        }
        return this;
    }

    @Override
    public Appendable append(CharSequence s_in, int i_start, int i_end) {
        return this.append(s_in.subSequence(i_start, i_end));
    }
}
