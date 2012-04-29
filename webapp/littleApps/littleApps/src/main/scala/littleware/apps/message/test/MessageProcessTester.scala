/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message
package test

import com.google.gson
import com.google.inject
import java.util.logging.{Level,Logger}

class MessageProcessTester @inject.Inject()(
  processor:controller.MessageProcessor
) extends littleware.test.LittleTest {

  private val log = Logger.getLogger( getClass.getName )
  
  def testMessageProcess():Unit = try {
    val client = processor.client
  } catch {
    case ex:Exception => {
        
    }
  }
}
