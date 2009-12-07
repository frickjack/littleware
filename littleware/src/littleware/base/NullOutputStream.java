package littleware.base;

import java.io.OutputStream;


/**
 * OutputStream that just writes to ether.
 * When software is ready to be released,
 * this writer can be passed to objects
 * that write debug messages to a stream.
 */
public class NullOutputStream extends OutputStream {
  public void write(int c) {}
  public void write(byte cbuf[]) {}
  public void write(byte cbuf[], int offset, int length) {}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

