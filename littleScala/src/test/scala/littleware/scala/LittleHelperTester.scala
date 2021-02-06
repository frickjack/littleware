package littleware.scala

import com.google.inject
import java.util.concurrent.ExecutorService
import org.junit.Assert._
import org.junit.Test
import org.junit.runner.RunWith
import littleware.base.feedback.Feedback
import littleware.scala.LittleHelper._

@RunWith( classOf[littleware.test.LittleTestRunner] )
class LittleHelperTester @inject.Inject() ( exec:ExecutorService, fbFactory:inject.Provider[Feedback] ) extends test.LittleTest {

  @Test
  def testLittleHelper():Unit = try {
    assertTrue( "Empty check handled string of spaces", ! emptyCheck( "    " ).isDefined )
    assertTrue( "ToJavaList has right size", 3 == toJavaList( List( "A", "B", "C" ) ).size )
  } catch basicHandler
  
  @Test
  def testPipeline():Unit = try {
    val pipeResult = new Pipeline( (0 until 100).toSeq ).pipeline( exec, 4, (x:Int) => x + x, (y:Int) => y.toString + "*", fbFactory.get )
    val fjResult = new Pipeline( (0 until 100).toSeq ).forkJoin( exec, 4, (x:Int) => x + x, (y:Int) => y.toString + "*", fbFactory.get )
    val check = (0 until 100).map( (x) => (x+x).toString + "*" ).toSeq
    assertTrue( "pipeline works: " + pipeResult, pipeResult == check )
    assertTrue( "forkJoin works: " + fjResult, fjResult == check )
  } catch basicHandler

}
