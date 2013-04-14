package littleware.scala



/**
 * Trait for setting up simple builders based on setting and validating a set
 * of properties for a resulting class.
 *
 * Ex:
 *    val a, b = new Property[Int]( this, -1, (v:Int) => if ( v < 0 ) Seq( "property must be >= 0" ) else Nil )
 */
trait PropertyBuilder extends LittleValidator {
  builder =>
  import PropertyBuilder._
  type BuilderType = this.type

  import scala.collection.mutable.Buffer
  
  /**
   * List of properties allocated under this class - used by isReady() below -
   * use with caution.
   */
  protected val props:Buffer[Property[_]] = Buffer.empty
  
  /**
   * Default implementation is props.flatMap( _.checkSanity ) 
   */
  def checkSanity():Seq[String] = props.toSeq.flatMap( _.checkSanity() )
  
  /**
   * Call "checkSanity" on every property associated with this builder
   */
  def checkSanityByProp():Seq[(Property[_], Iterable[String])] =
    props.toSeq.map( (p) => p -> p.checkSanity() )
  
  /**
   * Shortcut for:
   *    val errors = checkSanity
   *    assert( errors.isEmpty, "sanity check passed on " + this + ": " + errors.mkString( "," ) )
   */
  def assertSanity():Unit = {
    val errors = checkSanity()
    assert( errors.isEmpty, "sanity check passed on " + this + ": " + errors.mkString( "," ))
  }
  
  override def toString():String = props.mkString( "," )
  
  
  /**
   * Typical property, so build has things like
   *     val a = new Property[Int]( -1, "a", (x:Int) => x > 0 )
   * Provides constructors with defaults for name and checkSanity.
   */
   class Property[T]( 
        protected var value:T,
        sanityTest:(T) => Iterable[String]
      ) extends LittleValidator {
      def this( value:T ) = this( value, (_:T) => Nil )
      
      def apply():T = value
      
      var name:String = "prop" + builder.props.size
      builder.props += this 
      
      /** Chainable assignment */
      def apply( v:T ):BuilderType = { value = v; builder }
      
      /** 
       * Does this property have a valid value for the parent Builder to access ?
       * This implementation just returns true 
       */
      def checkSanity():Iterable[String] = sanityTest( value )
      
      /**
       * Reset this property's name
       */
      def name( value:String ):this.type = { name = value; this }
      
      override def toString():String = "" + name + "=" + value + " (" + checkSanity.mkString(",") + ")"
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
   class NotNullProperty[T <: AnyRef]( _value:T, _test:(T) => Iterable[String] ) extends Property[T]( _value, _test ) {
     def this( v:T ) = this(v, 
                       sanityCheck( nullCheck )
                       )
     def this() = this( null.asInstanceOf[T] )
   } 
   
   /**
    * Convenience class tests Int Property.  Default test is value >= 0 
    */
   class IntProperty( _value:Int, test:(Int) => Iterable[String] ) extends Property[Int]( _value, test ) {
     
     def this( v:Int ) = this( v, sanityCheck( notNegativeInt ) )
     /** Alias for constructor (-1, (i) => i > 0) */
     def this() = this( -1 ) 
   }   
   
   /**
    * Property accepts multiple values
    */
   class BufferProperty[T] ( _test:(Buffer[T]) => Iterable[String] 
       ) extends Property[Buffer[T]]( Buffer.empty, _test ) {
     def this() = this( (_) => Nil )

     def add( v:T ):BuilderType = { value += v; builder }
     def addAll( v:Iterable[T] ):BuilderType = { value ++= v; builder }
     def clear():BuilderType = { value.clear; builder }
   } 
  
}

object PropertyBuilder {
  /**
   * Compose series of boolean sanity checks into
   * a single sanity-check function suitable for
   * attaching as a Property validator.
   */
  def sanityCheck[T]( checks:Product2[String,(T)=>Boolean]* ):(T) => Iterable[String] = {
    (v:T) => checks.filterNot( _ match { case (err,thunk) => thunk(v) } ).map( _._1 )
  }
  
  /** littleware.scala.Messages resource bundle */
  val rb = java.util.ResourceBundle.getBundle( "littleware.scala.Messages" )
  
  /**
   * Not-null check suitable for adding to sanityCheck
   * 
   * @return ( failure message, validation check ) pair
   */
  def nullCheck[T <: AnyRef]:(String,(T)=>Boolean) = {
    def check( t:T ):Boolean = t != null
    rb.getString( "ValidateNotNull") -> check
  }

  /**
   * Not-negative check suitable for adding to sanityCheck
   * 
   * @return ( failure message, validation check ) pair
   */  
  def notNegativeInt:(String,(Int) => Boolean) = {
    def check( t:Int ):Boolean = t >= 0
    rb.getString( "ValidateNonNegative") -> check
  }
}
