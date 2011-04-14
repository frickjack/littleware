/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.scala

import java.lang.reflect.InvocationTargetException
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
  implicit def emptyCheck( value:String ):Option[String] =
    if ( (null == value) || value.trim.equals( "" ) ) {
      None
    } else {
      Some(value)
    }

  implicit def callable[A]( func:()=>A ):Callable[A] = new Callable[A]() {
    override def call() = func()
  }


  implicit def runnable( func: () => Unit ):Runnable = new Runnable {
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

}

