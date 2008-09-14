package littleware.base;

/**
 * Factory pattern interface
 */
public interface Factory<T> {
  /**
   * Create an object
   *
   * @exception FactoryException if unable to supply new object
   */
  public T create () throws FactoryException;

  /** 
   * Recycle an object which is no longer needed 
   *
   * @param x_o is the object to recycle
   * @throws FactoryException if x_o does not belong to this factory
   */
  public void recycle ( T x_o ) throws FactoryException;
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

