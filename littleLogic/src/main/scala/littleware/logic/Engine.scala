package littleware.logic


/**
 * Horn-clause rule
 */
trait Engine {
  /**
   * Find the rules that apply given the given facts.
   * The testIfTrue parameter is used to stop the search once rules have been found
   * confirming the testIfTrue entries, and may be empty to just do an exhaustive search
   */
  def query(givenFacts:Set[Fact], rules:Set[Rule], testIfTrue:Set[Fact]):Engine.QueryResult
  
  /**
   * Little sugar equivalent to query(factGenerator(variables), rules, testIfTrue)
   */
  def query(variables:Map[String, String], factGenerator:(Map[String,String]) => Set[Fact], rules:Set[Rule], testIfTrue:Set[Fact]):Engine.QueryResult
}

object Engine {
  case class QueryResult(facts:Set[Fact], rules:Set[Rule]) {}
}