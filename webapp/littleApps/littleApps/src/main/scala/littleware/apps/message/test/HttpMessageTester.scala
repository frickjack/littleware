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
    val handle = client.postMessage( session, MessageProcessTester.TestMessage )
    log.log( Level.INFO, "Waiting for test message response" )
    var progress = 0
    // Try at most 10 times to collect response
    (0 until 10).iterator.filter( (_) => progress < 100 ).foreach( (i) => {
        Thread.sleep( 1000 )
        log.log( Level.INFO, "Checking for response" )
        client.checkResponse( session, handle ).foreach( 
          (envelope) => {
            jtest.Assert.assertTrue( 
              "Progress advances: " + envelope.response.progress,
              envelope.response.progress >= progress
            )
            log.log( Level.INFO, "Got response: " + envelope )
            progress = envelope.response.progress
          }
        )
      }
    )
    jtest.Assert.assertTrue( "progress 100 after processing responses: " + progress,
                            progress == 100
    )
  } catch { 
    case ex:Exception => {
        log.log( Level.WARNING, "Test failed", ex )
        jtest.Assert.fail( "Caught: " + ex )
      }
  }
}
