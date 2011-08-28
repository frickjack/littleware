package littleware.apps.browser.gwt.model;

import littleware.apps.browser.gwt.model.internal.SimpleOption;

public interface GwtOption<T> extends java.io.Serializable, Iterable<T> {
	public boolean isDefined();
	public boolean isEmpty();
	public T       get();
	public T       getOr( T other );

	public static class Factory {
		public static <R> GwtOption<R> empty() { return new SimpleOption<R>(); }
		public static <R> GwtOption<R> empty( Class<R> ctype ) { return new SimpleOption<R>(); }

		/**
		 * Return empty if value is null
		 */
		public static <R> GwtOption<R> some( R value ) { 
			if( null != value ) {
				return new SimpleOption<R>( value ); 
			} else {
				return empty();
			}
		}

		public static <R> GwtOption<R> some( R value, Class<R> ctype ) {
			if ( null != value ) {
				return new SimpleOption<R>( value );
			} else {
				return empty( ctype );
			}
		}
	}
}
