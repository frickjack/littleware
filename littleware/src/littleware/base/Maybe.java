/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.base;

/**
 * A little set/not-set object. Facilitates deferred loading and other patterns.
 */
public final class Maybe<T> {

  /**
   * Same kind of trick as Collections.EMPTY_LIST or whatever
   */
  public static final Option NONE = new SimpleOption();

  private static class SimpleOption<T> extends AbstractReference<T> implements Option<T> {

    public SimpleOption(T value) {
      super(value);
    }

    public SimpleOption() {
    }

    @Override
    public Option<T> filter( Filter<? super T> filter) {
      if (this.isEmpty()) {
        return this;
      } else if (filter.accept( this.get() ) ) {
        return this;
      } else {
        return NONE;
      }
    }
  }
  
  private static final long serialVersionUID = 10000001L;

  /**
   * Factory for unset Maybe
   */
  public static <T> Option<T> empty() {
    return NONE;
  }

  /**
   * Maybe factory set if val is not null, otherwise empty
   */
  public static <T> Option<T> something(T val) {
    if (null == val) {
      return new SimpleOption<>();
    } else {
      return new SimpleOption<>(val);
    }
  }

  
}
