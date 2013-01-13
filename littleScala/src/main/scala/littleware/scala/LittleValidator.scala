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

import com.google.common.collect.ImmutableList
import littleware.base.validate._

object LittleValidator {

  /**
   * Goofy little helper checks a condition, and returns
   * a copy of itself with/without the message added to an error list.
   */
  trait Helper {
    val errors:List[String]
    /**
     * Return new Helper with message to the error sring if test fails (is false),
     * otherwise return this
     */    
    def check( test:Boolean, message:String ):Helper
  }

  private class SimpleHelper( override val errors:List[String] ) extends Helper {
    override def check( test:Boolean, message:String ):Helper = test match {
      case true => this
      case _ => new SimpleHelper( errors :+ message )
    }
  }

  def helper:Helper = new SimpleHelper( Nil )
}


/**
 * Extends Validator with support for some scala types
 */
trait LittleValidator extends Validator {
  @throws(classOf[ValidationException])
  override def validate():Unit = {
    val errors = checkSanity
    if ( ! errors.isEmpty ) {
      throw new ValidationException(
        ((new StringBuilder) /: errors)( (sb,error) => { sb.append( error ).append( littleware.base.Whatever.NEWLINE ) } ).toString
      )
    }
  }

  override def checkIfValid():ImmutableList[String] = (ImmutableList.builder[String]() /: checkSanity())( (builder,error) => builder.add( error ) ).build

  /**
   * Same as checkIfValid, just scala-friendly return type
   */
  def checkSanity():Iterable[String]
}
