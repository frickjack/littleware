package littleware.apps.browser.gwt.model;

import littleware.apps.browser.gwt.model.internal.SimpleOption;

public interface GwtOption<T> {
	public boolean isDefined();
	public boolean isEmpty();
	public T       get();
	public T       getOr( T other );
	
	public static class Factory {
	 public static <R> GwtOption<R> empty() { return new SimpleOption<R>(); }
	 public static <R> GwtOption<R> empty( Class<R> ctype ) { return new SimpleOption<R>(); }
	 
	 public static <R> GwtOption<R> some( R value ) { return new SimpleOption<R>( value ); }
	 public static <R> GwtOption<R> some( R value, Class<R> ctype ) {
		 return new SimpleOption<R>( value );
	 }
	}
}
