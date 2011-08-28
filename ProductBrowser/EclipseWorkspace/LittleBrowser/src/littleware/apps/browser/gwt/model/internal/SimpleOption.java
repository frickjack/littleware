package littleware.apps.browser.gwt.model.internal;

import java.util.Iterator;
import java.util.NoSuchElementException;

import littleware.apps.browser.gwt.model.GwtOption;

public class SimpleOption<T> implements GwtOption<T>, java.io.Serializable {
	private T       value = null;
	
	public SimpleOption() {}
	public SimpleOption( T value ) {
		this.value = value;
	}
	
	@Override
	public boolean isDefined() {
		return (null != value);
	}

	@Override
	public boolean isEmpty() {
		return ! isDefined();
	}

	@Override
	public T get() {
		if ( ! isDefined() ) {
			throw new IllegalStateException( "Unset option" );
		}
		return value;
	}

	@Override
	public T getOr(T other) {
		if ( isEmpty() ) {
			return other;
		}
		return get();
	}

	@Override
	public boolean equals( Object other ) {
		return (other instanceof SimpleOption) &&
				((SimpleOption<?>) other).value == this.value;
	}
	@Override
	public Iterator<T> iterator() {
		// TODO Auto-generated method stub
		return new Iterator<T>() {
			private boolean hasNext = true;
			
			@Override
			public boolean hasNext() {
				// TODO Auto-generated method stub
				return isDefined() && hasNext;
			}

			@Override
			public T next() {
				if ( hasNext() ) {
					hasNext = false;
					return value;
				}
				throw new NoSuchElementException();
			}

			@Override
			public void remove() {
				// TODO Auto-generated method stub
				
			}
			
		};
	}
}
