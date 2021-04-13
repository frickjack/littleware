# TL;DR

Littleware provides uses the builder pattern to build
immutable scala case classes.

## Problem and Audience

Many developers use immutable data structures in application code, and leverage the [builder pattern](https://en.wikipedia.org/wiki/Builder_pattern) to construct the application's initial state, and progress to new versions of the state in response to user inputs and other side effects. The [scala](https://scala-lang.org) programming language supports immutable data structures well, but does not provide a native implementation of the builder pattern.  The following describes a simple but useful scala builder framework that leverages re-usable data-validation lambda functions.

## A Scala Framework for Builders

The following are typical examples of an immutable `case class` in scala:

```
/**
 * Little resource name URI:
 * "lrn://${cloud}/${api}/${project}/${resourceType}/${drawer}/${path}"
 */
trait LRN {
    val cloud: String
    val api: String
    val projectId: UUID
    val resourceType: String
    val drawer: String
    val path: String
}

/**
 * Path based sharing - denormalized data
 */
case class LRPath(
    cloud: String,
    api: String,
    projectId: UUID,
    resourceType: String,
    drawer: String,
    path: String
) extends LRN {
}

/**
 * Id based sharing - normalized data
 */
case class LRId(
    cloud: String,
    api: String,
    projectId: UUID,
    resourceType: String,
    resourceId: UUID
) extends LRN {
    override val drawer = ":"
    val path = resourceId.toString()
}
```

Builders like the following simplify object construction and validation (compared to passing all the property values to the constructor).

```
object LRN {
    val zeroId:UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
    
    trait Builder[T <: LRN] extends PropertyBuilder[T] {
        val cloud = new Property("") withName "cloud" withValidator dnsValidator
        val api = new Property("") withName "api" withValidator LRN.apiValidator
        val projectId = new Property[UUID](null) withName "projectId" withValidator notNullValidator
        val resourceType = new Property("") withName "resourceType" withValidator LRN.resourceTypeValidator
        val path = new Property("") withName "path" withValidator pathValidator

        def copy(lrn:T):this.type = this.projectId(lrn.projectId).api(lrn.api
            ).cloud(lrn.cloud).resourceType(lrn.resourceType
            ).path(lrn.path)

        def fromSession(session:Session): this.type = this.cloud(session.lrp.cloud
            ).api(session.api
            ).projectId(session.projectId
            )
    }

    class LRPathBuilder extends Builder[LRPath] {        
        val drawer = new Property("") withName "drawer" withValidator drawerValidator

        override def copy(other:LRPath) = super.copy(other).drawer(other.drawer)

        def build():LRPath = {
            validate()
            LRPath(cloud(), api(), projectId(), resourceType(), drawer(), path())
        }
    }

    class LRIdBuilder extends Builder[LRId] {
        def build():LRId = {
            validate()
            LRId(cloud(), api(), projectId(), resourceType(), UUID.fromString(path()))
        }
    }

    def apiValidator = rxValidator(raw"[a-z][a-z0-9-]+".r)(_, _)
    
    def drawerValidator(value:String, name:String) = rxValidator(raw"([\w-_.*]+:)*[\w-_.*]+".r)(value, name) orElse {
        if (value.length > 1000) {
            Some(s"${name} is too long: ${value}")
        } else {
            None
        }
    }

    def pathValidator(value:String, name:String) = pathLikeValidator(value, name) orElse {
        if (value.length > 1000) {
            Some(s"${name} is too long: ${value}")
        } else {
            None
        }
    }

    def resourceTypeValidator = rxValidator(raw"[a-z][a-z0-9-]{1,20}".r)(_, _)

    // ...
}
```

This builder implementation does not leverage the type system to detect construction errors at compile time (this blog
shows an approach with [phantom types](https://medium.com/@maximilianofelice/builder-pattern-in-scala-with-phantom-types-3e29a167e863)),
but it is composable in a straight forward way.
A couple fun things about this implementation are that it leverages the builder pattern to define the properties in a builder (`new Property... withName ... withValidator ...`),
and the setters on the nested property class return the parent `Builder` type, so we can write code like this:

```
    @Test
    def testLRNBuilder() = try {
        val builder = builderProvider.get(
        ).cloud("test.cloud"
        ).api("testapi"
        ).drawer("testdrawer"
        ).projectId(LRN.zeroId
        ).resourceType("testrt"
        ).path("*")

        val lrn = builder.build()
        assertTrue(s"api equal: ${lrn.api} ?= ${builder.api()}", lrn.api == builder.api())
    } catch basicHandler
```

Unfortunately, the code (in https://github.com/frickjack/littleware under littleAudit/ and littleScala/) is in a state of flux, but the base `PropertyBuilder` can be copied into another code base - something like this:

```
/**
 * Extends Validator with support for some scala types
 */
trait LittleValidator extends Validator {
  @throws(classOf[ValidationException])
  override def validate():Unit = {
    val errors = checkSanity()
    if ( ! errors.isEmpty ) {
      throw new ValidationException(
        errors.foldLeft(new StringBuilder)( (sb,error) => { sb.append( error ).append( littleware.base.Whatever.NEWLINE ) } ).toString
      )
    }
  }


  /**
   * Same as checkIfValid, just scala-friendly return type
   */
  def checkSanity():Iterable[String]
}

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
        (option, propName) => option.flatMap({ it => memberValidator(it, propName) })
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

  def dnsValidator = rxValidator(raw"([\w-]{1,40}\.){0,10}[\w-]{1,40}".r)(_, _)
  def emailValidator = rxValidator(raw"[\w-_]{1,20}@\w[\w-.]{1,20}".r)(_, _)
  def pathLikeValidator = rxValidator(raw"([\w-:_.*]{1,255}/){0,20}[\w-:_.*]{1,255}".r)(_, _)


}

```
