package littleware.logic

import scala.collection.mutable.Buffer

/**
 * Horn-clause rule
 */
trait Rule {
  def conjunction:Set[Fact]
  def implies:Fact.Derived  
}

object Rule {
  private case class SimpleRule(
    conjunction:Set[Fact],
    implies:Fact.Derived
  ) extends Rule {}

  class Builder(private val conjunction:Buffer[Fact]) {
    def and(fact:Fact):this.type = {
      conjunction += fact
      this
    }

    def implies(fact:Fact.Derived): Rule =
      SimpleRule(Set[Fact]() ++ conjunction, fact)
  }

  def when(fact:Fact):Builder = new Builder(Buffer(fact))
}