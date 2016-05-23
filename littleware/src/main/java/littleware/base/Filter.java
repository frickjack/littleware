/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.base;

/**
 * Filter thunk - accepts or rejects things - handy for Option.filter,
 * validation, ...
 */
public interface Filter<U> {
  public boolean accept(U v);

  
  /** Filter rejects empty strings */
  public static class EmptyStringFilter implements Filter<String> {
    /** null != v && ! v.trim().isEmpty() */
    @Override
    public boolean accept( String v ) { return (null != v) && (! v.trim().isEmpty()); }
  }
  
  /** Filter only accepts positive integers */
  public static class PositiveIntFilter implements Filter<Integer> {
    @Override
    public boolean accept( Integer v ) { return (null != v) && v.intValue() > 0; }
  }
  
}
