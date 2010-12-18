package littleware.base;


/**
 * The shared object factory simply
 * returns the same cached object for
 * each call to the 'create' method.
 */
class SharedObjectFactory implements Factory {
  private Object ox_shared_object;

  /** Set the shared object this factory should manage */
  public SharedObjectFactory ( Object x_shared_object ) {
    ox_shared_object = x_shared_object;
  }

  /** Get the object managed by this factory */
  public Object create () { return ox_shared_object; }

  /** 
   * Recycle an object which is no longer needed 
   *  -- this is a do-nothig operation for the
   *        SharedObjectFactory
   *
   * @param x_o is the object to recycle
   * @throws IllegalArgumentException if x_o does not 
   *          correspond to the type tracked by this 
   *          factory
   */
  public void recycle ( Object x_o ) throws IllegalArgumentException {}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

