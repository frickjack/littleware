package littleware.scala



/**
 * Trait for setting up simple builders based on setting and validating a set
 * of properties for a resulting class.
 *
 * Ex:
 *    val a, b = new Property[Int]( this, -1 ){ override def isReady = value >= 0 }
 */
trait PropertyBuilder extends LittleValidator {
  builder =>
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
  def checkSanity():Seq[String] = props.flatMap( _.checkSanity() )
  
  override def toString():String = props.mkString( "," )
  
  
  /**
   * Typical property, so build has things like
   *     val a = new Property[Int]( this, -1 ){ override def isReady = value >= 0 } 
   */
   class Property[T]( 
        var value:T
      ) extends LittleValidator {
      def apply():T = value
      
      /** Chainable assignment */
      def apply( v:T ):BuilderType = { value = v; builder }
      /** 
       * Does this property have a valid value for the parent Builder to access ?
       * This implementation just returns true 
       */
      def checkSanity():Seq[String] = Nil
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
   class NotNullProperty[T <: AnyRef]( _value:T, val name:String ) extends Property[T]( _value ) {
     def this() = this(null.asInstanceOf[T], "property" )
     def this( v:T ) = this( v, "property" )
     override def checkSanity():Seq[String] = Seq( name + ": property must not be null" )
   } 
   
   /**
    * Convenience class tests Int Property.  Default test is value >= 0 
    */
   class IntProperty( _value:Int, test:(Int) => Seq[String], name:String ) extends Property[Int]( _value ) {
     
     def this( v:Int, name:String ) = this( v, (i) => if( i >= 0 ) { Nil } else { Seq( "property must be >= 0: " + i ) }, name )
     def this( v:Int, test:(Int) => Seq[String] ) = this( v, test, "property" )
     def this( name:String ) = this( -1, name )
     def this( v:Int ) = this( v, "property" )
     /** Alias for constructor (-1, (i) => i > 0) */
     def this() = this( -1, "property" ) 
     
     override def checkSanity = test(value).map( name + ": " + _ ) 
   }
   
   
   
   /**
    * Property accepts multiple values
    */
   class BufferProperty[T] extends Property[Buffer[T]]( Buffer.empty ) {
     def add( v:T ):BuilderType = { value += v; builder }
     def addAll( v:Iterable[T] ):BuilderType = { value ++= v; builder }
   } 
  
}

