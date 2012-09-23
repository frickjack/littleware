package littleware.scala



/**
 * Trait for setting up simple builders based on setting and validating a set
 * of properties for a resulting class.
 *
 * Ex:
 *    val a, b = new Property[Int]( this, -1 ){ override def isReady = value >= 0 }
 */
trait PropertyBuilder {
  builder =>
  type BuilderType = this.type

  import scala.collection.mutable.Buffer
  
  /**
   * List of properties allocated under this class - used by isReady() below -
   * use with caution.
   */
  protected val props:Buffer[Property[_]] = Buffer.empty
  
  /**
   * Default implementation is props.find( ! _.isReady ).isEmpty 
   */
  def isReady:Boolean = props.find( ! _.isReady ).isEmpty
  
  override def toString():String = props.mkString( "," )
  
  
  /**
   * Typical property, so build has things like
   *     val a = new Property[Int]( this, -1 ){ override def isReady = value >= 0 } 
   */
   class Property[T]( 
        var value:T
        ) {
      def apply():T = value
      
      /** Chainable assignment */
      def apply( v:T ):BuilderType = { value = v; builder }
      /** 
       * Does this property have a valid value for the parent Builder to access ?
       * This implementation just returns true 
       */
      def isReady:Boolean = true
   }
   
   /**
    * Option property initialized to None with extra chainable setter method, isValid = true
    */
   class OptionProperty[T] extends Property[Option[T]]( None ) {
     def set( v:T ):BuilderType = apply( Option(v) )
   } 
   
   /** 
    * Convenience class for isReady = value != null,
    * Includes no-arg constructor that initializes value=null 
    */
   class NotNullProperty[T <: AnyRef]( _value:T ) extends Property[T]( _value ) {
     def this() = this(null.asInstanceOf[T])
     override def isReady = value != null
   } 
   
   /**
    * Convenience class tests Int Property.  Default test is value >= 0 
    */
   class IntProperty( _value:Int, test:(Int) => Boolean ) extends Property[Int]( _value ) {
     /** Alias for constructor (-1, (i) => i > 0) */
     def this() = this( -1, (i) => i >= 0 )
     override def isReady = test(value)
   }
   
   
   
   /**
    * Property accepts multiple values
    */
   class BufferProperty[T] extends Property[Buffer[T]]( Buffer.empty ) {
     def add( v:T ):BuilderType = { value += v; builder }
     def addAll( v:Iterable[T] ):BuilderType = { value ++= v; builder }
   } 
  
}

