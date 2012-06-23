/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message
package test

import com.google.inject
import java.util.logging.{Level,Logger}
import junit.{framework => jtest}


class HttpMessageTester @inject.Inject()(
  client:remote.WebMessageRemote,
  credsFactory:model.Credentials.Factory
) extends littleware.test.LittleTest {
  putName( "testWebRemote" )
  val log = Logger.getLogger( getClass.getName )
  
  
  def testWebRemote():Unit = try {
    val session = client.login( credsFactory.namePasswordCreds("test", "password" ))
    (0 to 1).foreach( (j) => {
        val handle = client.postMessage( session, MessageProcessTester.TestMessage )
        log.log( Level.INFO, "-" + j + " Waiting for test message response" )
        var progress = 0
        // Try at most 10 times to collect response
        (0 until 10).iterator.filter( (_) => progress < 100 ).foreach( (i) => {
            Thread.sleep( 1000 )
            log.log( Level.INFO, "-" + j + " Checking for response" )
            val responseSeq = if ( j == 0 ) {
              client.checkResponse( session, handle )
            } else {
              client.checkResponse( session ).get( handle.id ).flatten
            }
            responseSeq.foreach( 
              (envelope) => {
                jtest.Assert.assertTrue( 
                  "-" + j + " Progress advances: " + envelope.response.progress,
                  envelope.response.progress >= progress
                )
                log.log( Level.INFO, "-" + j + " Got response: " + envelope )
                progress = envelope.response.progress
              }
            )
          }
        )
        jtest.Assert.assertTrue( "-" + j + " progress 100 after processing responses: " + progress,
                                progress == 100
        )
      }
    )
  } catch { 
    case ex:Exception => {
        log.log( Level.WARNING, "Test failed", ex )
        jtest.Assert.fail( "Caught: " + ex )
      }
  }
}
