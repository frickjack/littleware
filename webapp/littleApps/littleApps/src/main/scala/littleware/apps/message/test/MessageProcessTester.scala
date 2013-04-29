/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message
package test

import com.google.common.{base => gbase}
import com.google.inject
import java.util.logging.{Level,Logger}
import org.joda.{time => jtime}
import junit.{framework => jtest}

class MessageProcessTester @inject.Inject()(
  processor:server.MessageProcessor
) extends littleware.test.LittleTest {
  setName( "testMessageProcess" )
  
  private val log = Logger.getLogger( getClass.getName )
  
  def responseIterator( session:model.ClientSession ):Iterator[model.ResponseEnvelope] = 
    new Iterator[model.ResponseEnvelope]() {
      var it:Iterator[model.ResponseEnvelope] = processor.client.checkResponse( session ).values.flatten.iterator
      override def hasNext():Boolean = {
        val timer = new gbase.Stopwatch().start()
        while( (! it.hasNext) && timer.elapsedMillis < 10000 ) {
          it = processor.client.checkResponse( session ).values.flatten.iterator
        }
        it.hasNext
      }
      override def next():model.ResponseEnvelope = it.next
    }

  def testMessageProcess():Unit = try {
    val now = jtime.DateTime.now
    val client = processor.client
    val session = client.login( model.Credentials.NamePassword("littleware.test_user", "test123" ) )
    val handle:model.MessageHandle = client.postMessage( session, MessageProcessTester.TestMessage )
    val responseSeq:Seq[model.ResponseEnvelope] = responseIterator( session 
    ).take(20).toSeq
    val messages = responseSeq.flatMap( _.response.feedback ).mkString( "," )
    jtest.Assert.assertTrue( "Got expected feedback: " + messages,
                            messages == (1 to 10).mkString( "," )
    )
    responseSeq.map( _.response.progress ).reduceLeft( (a,b) => {
        jtest.Assert.assertTrue( "Progress increases through test response stream: " + a + " ?<>= " + b, a <= b )
        b
      }
    )
  } catch {
    case ex:Exception => {
        log.log( Level.WARNING, "Failed test", ex )
        jtest.Assert.fail( "Caught ex: " + ex )
      }
  }
}

object MessageProcessTester {
  private val log = Logger.getLogger( getClass.getName )
  
  object TestMessage extends model.Message {
    val messageType = "littleware.TestMessage"
    val payload:TestPayload = new TestPayload( "Hello, World!" )
  }
  
  object TestListener extends server.MessageListener {
    val id:java.util.UUID = java.util.UUID.randomUUID

    def messageArrival( event:model.MessageEvent, fb:littleware.base.feedback.Feedback ):Unit = {
      (1 to 10) map {
        (i) => fb.info( i.toString ); fb.setProgress( 10*i )
      }
    }    
  }
  
}
