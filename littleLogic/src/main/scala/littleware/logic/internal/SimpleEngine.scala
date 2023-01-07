package littleware.logic.internal

import littleware.logic.{Engine, Fact, Rule}

import scala.annotation.tailrec

/**
 * Simple brute-force evaluation
 */
class SimpleEngine extends Engine {

  @tailrec
  private def queryRecursive(trueFacts:Set[Fact], unprovenRules:Set[Rule], testIfTrue:Set[Fact], resultsSoFar:Set[Rule]):Engine.QueryResult = {
    val provenRules = unprovenRules.filter(
      rule => rule.conjunction.subsetOf(trueFacts)
    )
    val newTrueFacts = trueFacts ++ provenRules.map(_.implies)
    val newTestIfTrue = testIfTrue.diff(newTrueFacts)
    if (provenRules.isEmpty) {
      Engine.QueryResult(trueFacts, resultsSoFar)
    } else if (!testIfTrue.isEmpty && newTestIfTrue.isEmpty) {
      Engine.QueryResult(trueFacts, resultsSoFar)
    } else {
      queryRecursive(
        newTrueFacts,
        unprovenRules.diff(provenRules),
        newTestIfTrue,
        resultsSoFar ++ provenRules
        )
    }
  }

  override def query(givenFacts:Set[Fact], rules:Set[Rule], testIfTrue:Set[Fact]):Engine.QueryResult =
    queryRecursive(givenFacts, rules, testIfTrue, Set[Rule]())
  
  override def query(variables:Map[String, String], factGenerator:(Map[String,String]) => Set[Fact], rules:Set[Rule], testIfTrue:Set[Fact]):Engine.QueryResult =
    queryRecursive(factGenerator(variables), rules, testIfTrue, Set[Rule]())

}

