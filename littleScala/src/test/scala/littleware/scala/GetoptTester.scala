/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.scala
package test

import junit.framework.Assert._


/**
 * Little tester of GetoptHelper
 */
class GetoptTester extends littleware.scala.test.LittleTest {
    setName( "testGetopt" )
    
  def testGetopt():Unit = try {
    val args = Array( "-alpha", "-beta", "a", "-gamma", "a", "b" )
    val argMap = GetoptHelper.extract( args )
    Seq( "alpha", "beta", "gamma" ).zipWithIndex.map( _ match {
        case (arg,count) => assertTrue( arg + " handled ok: " + argMap.get(arg).getOrElse( "ugh!" ), 
                                       argMap.contains(arg) && argMap(arg).size == count
          )
      })
    val blaMap = GetoptHelper.compress( argMap, Map( "alpha" -> Seq( "beta", "gamma") ) )
    assertTrue( "Compress merges everything", blaMap.size == 1 && blaMap.contains( "alpha" ) && blaMap( "alpha" ).size == 3 )
  } catch basicHandler 
}
