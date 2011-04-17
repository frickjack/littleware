/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base;

/**
 * Factory pattern interface
 */
public interface Factory<T> {
  /**
   * Create an object
   *
   * @throws FactoryException if unable to supply new object
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

