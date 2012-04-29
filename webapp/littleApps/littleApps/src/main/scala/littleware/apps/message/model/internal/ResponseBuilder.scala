/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */ 


package littleware.apps.message
package model
package internal

class ResponseBuilder () extends Response.Builder {
  private val fbBuilder = Seq.newBuilder[String]
  def addFeedback( value:String ):this.type = {
    fbBuilder += value
    this
  }
  def addFeedbacks( value:Iterable[String] ):this.type = {
    fbBuilder ++= value
    this
  }
  
    
  private val rsBuilder = Seq.newBuilder[java.lang.Object]
  def addResult( value:java.lang.Object ):this.type = {
    rsBuilder += value
    this
  }
  def addResults( value:Iterable[java.lang.Object] ):this.type = {
    rsBuilder ++= value
    this
  }
    
  def build():Response = ResponseBuilder.SimpleResponse(
    progress, fbBuilder.result, state, rsBuilder.result
  )
  
}

object ResponseBuilder {
  case class SimpleResponse(
    progress:Int,
    feedback:Seq[String],
    state:Response.State.Value,
    results:Seq[Object]
  ) extends Response {
    
    def copy():Response.Builder = new ResponseBuilder().progress( progress
    ).state( state ).addFeedbacks(feedback).addResults( results )
  }
}
