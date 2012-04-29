/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message.model

trait Response {
  val progress:Int
  val feedback:Seq[String]
  val state:Response.State.Value
  val results:Seq[Object]
  
  def copy():Response.Builder
}

object Response {
  trait Builder {
    var progress:Int = 0
    def progress( value:Int ):this.type = {
      progress = value
      this
    }
    
    var state:State.Value = Response.State.RUNNING
    def state( value:State.Value ):this.type = {
      state = value
      this
    }
    
    def addFeedback( value:String ):this.type    
    def addResult( value:Object ):this.type
    def build():Response
  }
  
  object State extends Enumeration {
    val PENDING, RUNNING, COMPLETE, FAILED = Value
  }
}
