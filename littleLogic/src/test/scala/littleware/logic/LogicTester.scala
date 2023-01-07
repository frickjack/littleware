package littleware.logic

import com.google.gson
import com.google.inject
import java.util.logging.{ Level, Logger }

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import Engine.QueryResult

/**
 * Little tester of logic Facts, Rules, and Engines
 */
@ExtendWith(Array(classOf[littleware.test.LittleParameterResolver]))
class LogicTester @inject.Inject() (engine:Engine) {

  @Test
  def testLibraryAccessRules():Unit = {
    val userIsNormalUser = Fact.GivenGenericStatement("user is normal type");
    val userIsSystemUser = Fact.GivenGenericStatement("user is system type");
    val userIsAdminUser = Fact.GivenGenericStatement("user is in admin group");
    val userIsInLibrary = Fact.GivenGenericStatement("user is in library");
    val userAllowedAccess = Fact.Derived("the user is allowed access")
    val userDeniedAccess = Fact.Derived("the user is denied access")
    val normalInteractiveAccess = Fact.Derived("a normal user is accessing interactive api")
    val assetHasPrivateSecurity = Fact.GivenGenericStatement("the asset has private security")
    val assetHasPublicSecurity = Fact.GivenGenericStatement("the asset has public security")
    val assetHasViewSecurity = Fact.GivenGenericStatement("the asset has view security")
    val aclAllowsUserAccess = Fact.GivenGenericStatement("the acl allows the user access")
    val aclDeniesUserAccess = Fact.GivenGenericStatement("the acl denies the user access")
    val endpointIsSyncApi = Fact.GivenGenericStatement("the endpoint is part of the sync api")
    val endpointIsInteractiveApi = Fact.GivenGenericStatement("the endpoint is part of the interactive api")
    val verbIsGet = Fact.GivenGenericStatement("the request is a http-get verb")

    val rules:Seq[Rule] = Seq(
      Rule.when(userIsAdminUser).and(endpointIsInteractiveApi).implies(userAllowedAccess),
      Rule.when(userIsNormalUser).and(endpointIsInteractiveApi).and(assetHasPrivateSecurity).and(aclAllowsUserAccess).implies(userAllowedAccess),
      Rule.when(userIsNormalUser).and(endpointIsInteractiveApi).and(assetHasViewSecurity).and(verbIsGet).implies(userAllowedAccess),
      Rule.when(userIsNormalUser).and(endpointIsInteractiveApi).and(assetHasPublicSecurity).and(aclDeniesUserAccess).implies(userDeniedAccess)
    )

    val tests = rules.map(
      rule => rule.conjunction -> QueryResult(rule.conjunction + rule.implies, Set(rule))
    )
    val rulesSet:Set[Rule] = Set() ++ rules
    tests.foreach(_ match {
      case (givenFacts, expectedResult) => {
        assertEquals(expectedResult, engine.query(givenFacts, rulesSet, Set()))
      }
    })

    // add a 2nd level rule
    val interactiveRule = Rule.when(userIsNormalUser).and(endpointIsInteractiveApi).implies(normalInteractiveAccess)
    val rules2Level:Seq[Rule] = Seq(
      Rule.when(normalInteractiveAccess).and(assetHasPrivateSecurity).and(aclAllowsUserAccess).implies(userAllowedAccess),
      Rule.when(normalInteractiveAccess).and(assetHasViewSecurity).and(verbIsGet).implies(userAllowedAccess),
      Rule.when(normalInteractiveAccess).and(assetHasPublicSecurity).and(aclDeniesUserAccess).implies(userDeniedAccess)
    )
    val tests2Level = rules2Level.map(
      rule => {
        val givenFacts = interactiveRule.conjunction ++ rule.conjunction - normalInteractiveAccess
        givenFacts -> QueryResult(givenFacts + rule.implies + normalInteractiveAccess, Set(interactiveRule, rule))
      }
    )
    val rules2LevelSet = Set() ++ rules2Level + interactiveRule
    tests2Level.foreach(_ match {
      case (givenFacts, expectedResult) => {
        assertEquals(expectedResult, engine.query(givenFacts, rules2LevelSet, Set()))
      }
    })
  }

}
