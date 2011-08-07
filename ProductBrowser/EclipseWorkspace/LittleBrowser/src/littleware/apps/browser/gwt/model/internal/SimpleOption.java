package littleware.apps.browser.gwt.model.internal;

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
}
