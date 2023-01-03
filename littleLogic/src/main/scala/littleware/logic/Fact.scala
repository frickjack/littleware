package littleware.logic


/**
 * Basic fact for building up rules
 */
trait Fact {
  def statement:String;

  {
    if (!statement.trim().toLowerCase().equals(statement) || statement.isEmpty()) {
      throw new IllegalArgumentException("statement must be lower case and non-empty")
    }
  }

  override def equals(other:Any):Boolean = other match {
    case fact:Fact => this.statement.equals(fact.statement)
    case _ => false
  }

  override def hashCode():Int = statement.hashCode()

  override def toString():String = statement
}

object Fact {
  abstract class Given() extends Fact {}

  case class GivenGenericStatement(statement:String) extends Given() {}

  case class Derived(statement:String) extends Fact {}
}