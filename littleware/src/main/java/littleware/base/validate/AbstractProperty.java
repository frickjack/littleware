package littleware.base.validate;

import java.util.Collection;

/**
 * We setup a lot of Builder POJOs that just provide a bunch of properties and a
 * build() method that validates the properties and constructs an immutable
 * result. A builder thus winds up with a lot of boiler plate getProp, setProp,
 * putProp (same as setProp, but returns the builder) methods. This class helps
 * alleviate that. A builder should typically define a subtype Property:
 * <pre>
 *     class Builder extends AbstractValidator {
 *      class Property<V> extends AbstractProperty<Builder,V> { ... } ...
 *
 *      public final Property<Int> prop1 = new Property( ... )
 *      public Collection<String> checkIfValid() { .... }
 *      public String toString() { return AbstractProperty.toString( prop1, prop2, ... ); }
 *     }
 * </pre>
 */
public class AbstractProperty<O, V> extends AbstractValidator {

  private final O object;
  public final String name;
  protected V value;

  public AbstractProperty(O object, String name, V value) {
    this.object = object;
    this.name = name;
    this.value = value;
  }

  public V get() {
    return value;
  }

  public O set(V v) {
    value = v;
    return object;
  }

  @Override
  public String toString() {
    return "Property( " + name + ":" + value + " )";
  }
  

  /**
   * Default checkIfValid method just checks that the property is not null ...
   * @return 
   */
  @Override
  public Collection<String> checkIfValid() {
    return this.buildErrorTracker().check( get() != null, "property may not be null" ).getErrors();
  }
  
}
