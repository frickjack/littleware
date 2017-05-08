package littleware.scala

import java.lang.reflect.InvocationTargetException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.{ArrayList,Collection,List}
import java.util.concurrent.Callable

import scala.collection.JavaConversions._

/**
 * Just a few little java-scala grease routines -
 * extend scala.collections.JavaConversions with
 * more collection wrapping,
 * SwingUtilities.invokeLater sugar, etc.
 */
object LittleHelper {
  

  /**
   * Conversion of a null or s_check.trim().equals( "" ) string to None,
   * otherwise Some( s_check )
   */
  def emptyCheck( value:String ):Option[String] =
    if ( (null == value) || value.trim.equals( "" ) ) {
      None
    } else {
      Some(value)
    }

  def callable[A]( func:()=>A ):Callable[A] = new Callable[A]() {
    override def call() = func()
  }


  def runnable( func: () => Unit ):Runnable = new Runnable {
    override def run = func()
  }

  
  /**
   * Push everything from the iterable onto the given collection
   */
  def addAll[A,B <: java.util.Collection[A]]( addTo:B, pullFrom:Iterable[A] ):B = {
    pullFrom.foreach ( (x) => { addTo.add( x ) } )
    addTo
  }

  /**
   * Push everything from the iterable onto a list
   */
  def toJavaList[A]( pullFrom:Iterable[A] ):java.util.List[A] = addAll( new ArrayList[A](), pullFrom )



  /**
   * SwingUtilities.invokeLater sugar
   */
  def invokeLater( thunk:()=>Unit ):Unit = {
    javax.swing.SwingUtilities.invokeLater (
      new Runnable () {
        def run() = thunk()
      }
    )
  }

  /**
   * SwingUtilities.invokeAndwait sugar -
   * just runs inline if already on dispatch thread
   */
  def invokeAndWait( thunk:()=>Unit ):Unit = {
    if( javax.swing.SwingUtilities.isEventDispatchThread ) {
      thunk()
    } else {
      try {
        javax.swing.SwingUtilities.invokeAndWait (
          new Runnable () {
            def run() = thunk()
          }
        )
      } catch {
        case ex:InvocationTargetException => {
            throw ex.getCause()
          }
        case ex:Throwable => {
            throw ex
          }
      }
    }
  }
  
  /**
   * Simple 2-step pipeline overlaps stage-1 computation with stage-2 computation
   */
  class Pipeline[A]( input:Seq[A] ) {
    
    /**
     * Shared method - either processes stage2 in parallel or sequentially
     */
    private def plumber[B,C]( exec:ExecutorService, 
                      chunkSize:Int, stage1: (A) => B, 
                      stage2: (B) => C,
                      fb:littleware.base.feedback.Feedback,
                      parallelStage2:Boolean
    ):Seq[C] = {
      val chunks = input.grouped( chunkSize ).toSeq
      // prime the pipeline
      if ( chunks.isEmpty ) {
        fb.setProgress(100)
        Nil
      } else {
        class Stage1 (
          val chunkNumber:Int,
          val inProgress:Seq[Future[B]]
        ){
          def this( ready:Seq[A], chunkNumber:Int ) = this( chunkNumber, ready.map( (a) => exec.submit( callable( () => stage1(a) ) ) ) )
          
          def launchStage2():Seq[Future[C]] = inProgress.map( 
            (future) => {
              val nextStage:Future[C] = exec.submit( callable( () => stage2( future.get ) ) )
              if ( ! parallelStage2 ) {
                nextStage.get
              }
              nextStage
            } 
          )
        }

        val resultBuilder = Seq.newBuilder[C]
        // prime the pipeline
        fb.setProgress(0)
        val firstChunk = new Stage1( chunks.head, 0 )
        firstChunk.inProgress.foreach( (x) => x.get )  
        chunks.tail.foldLeft( firstChunk )(
          (lastStage1:Stage1,nextChunk:Seq[A]) => {
            // launch stage1 on next chunk
            val nextStage1 = new Stage1( nextChunk, lastStage1.chunkNumber + 1 )
            // process last chunk through stage2 - stage1 and stage2 should be running concurrently now
            lastStage1.launchStage2.foreach( (future) => resultBuilder += future.get )
            fb.setProgress( nextStage1.chunkNumber, chunks.size )
            nextStage1
          }
        ).launchStage2.foreach( (future) => resultBuilder += future.get )
        fb.setProgress( 100 )
        resultBuilder.result
      }
    }

    /**
     * Pipeline tasks off to the given ExecutorService so that chunkSize stage1 threads
     * overlap with chunkSize stage2 threads.
     * This works by forking off stage1 tasks, then forking off stage2 tasks to
     * execute already completed stage1 tasks
     * 
     * @param chunkSize number of concurrent threads to fork to execute each stage - 
     *            2 * chunkSize threads running once pipeline is primed
     * @param feedback to setProgress on as processing proceeds
     */
    def pipeline[B,C]( exec:ExecutorService, 
                      chunkSize:Int, stage1: (A) => B, 
                      stage2: (B) => C,
                      fb:littleware.base.feedback.Feedback = new littleware.base.feedback.NullFeedback
    ):Seq[C] = plumber( exec, chunkSize, stage1, stage2, fb, true )

    
    /**
     * Same as pipeline, but stage 2 is processed sequentially on a single thread
     */
    def forkJoin[B,C]( exec:ExecutorService, 
                 chunkSize:Int, stage1: (A) => B, 
                 stage2: (B) => C,
                 fb:littleware.base.feedback.Feedback = new littleware.base.feedback.NullFeedback
    ):Seq[C] = plumber( exec, chunkSize, stage1, stage2, fb, false )
  }

}

