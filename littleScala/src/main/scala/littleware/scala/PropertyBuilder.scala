package littleware.scala

import scala.util.matching.Regex

/**
 * Trait for setting up simple builders based on setting and validating a set
 * of properties for a resulting class.
 *
 * Ex:
 *    val a, b = new Property[Int]( this, -1, (v:Int) => if ( v < 0 ) Seq( "property must be >= 0" ) else Nil )
 */
trait PropertyBuilder[B] extends LittleValidator {
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
    assert(errors.isEmpty, "sanity check passed on " + this + ": " + errors.mkString(","))
  }
  
  override def toString():String = props.mkString(",")   

  def copy(value:B):BuilderType
  def build():B

  /**
   * Typical property, so build has things like
   *     val a = new Property(-1) withName "a" withValidator { x => ... }
   *
   * Note: this type is intertwined with PropertyBuilder - don't
   *    try to pull it out of a being a subclass - turns into a mess
   */
  class Property[T](
      var value:T
    ) extends LittleValidator {    
    type Validator = (T,String) => Option[String]
    
    def apply():T = value
    
    var name:String = "prop" + builder.props.size
    var validator:Validator  = (_, _) => None

    override def checkSanity() = validator(this.value, this.name)
    def withValidator(v:Validator):this.type = {
      validator = v
      this
    }
    
    def withName(v:String):this.type = {
      this.name = v
      this
    }
    
    override def toString():String = "" + name + "=" + value + " (" + checkSanity().mkString(",") + ")"

    /** Chainable assignment */
    def apply(v:T):BuilderType = { value = v; builder }
  
    builder.props += this 
  }

  /**
   * Property accepts multiple values
   */
  class BufferProperty[T] extends Property[Buffer[T]](Buffer.empty) {
    def add( v:T ):BuilderType = { value += v; builder }
    def addAll( v:Iterable[T] ):BuilderType = { value ++= v; builder }
    def clear():BuilderType = { value.clear(); builder; }
  
    def withMemberValidator(memberValidator:(T,String) => Option[String]):this.type =
      withValidator(
        (buff, propName) => buff.view.flatMap({ it => memberValidator(it, propName) }).headOption
      )  
  }  

  class OptionProperty[T] extends Property[Option[T]](None) {
    def set(v:T):BuilderType = { value = Option(v); builder }

    def withMemberValidator(memberValidator:(T,String) => Option[String]):this.type =
      withValidator(
        (buff, propName) => buff.view.flatMap({ it => memberValidator(it, propName) }).headOption
      )  
  }
}

object PropertyBuilder {  
  /** littleware.scala.Messages resource bundle */
  val rb = java.util.ResourceBundle.getBundle( "littleware.scala.Messages" )

  def rxValidator(rx:Regex)(value:String, name:String):Option[String] = {
    if (null == value || !rx.matches(value)) {
      Some(s"${name}: ${value} !~ ${rx}")
    } else {
      None
    }
  }

  def notNullValidator(value:AnyRef, name:String):Option[String] = {
    if (null == value) {
      Some(s"${name}: is null")
    } else {
      None
    }
  }

  def positiveIntValidator(value:Int, name:String):Option[String] = {
    if (value <= 0) {
      Some(s"${name}: is not positive")
    } else {
      None
    }
  }

  def positiveLongValidator(value:Long, name:String):Option[String] = {
    if (value <= 0) {
      Some(s"${name}: is not positive")
    } else {
      None
    }
  }

  def dnsValidator = rxValidator(raw"([\w-]+\.)*[\w-]+".r)(_, _)

}
