/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker.browser

import java.net.URL
import java.util.Arrays
import java.util.logging.Level
import java.util.logging.Logger
import javax.security.auth.login.LoginException
import littleware.apps.lgo.LgoCommandLine
import littleware.bootstrap.client.ClientBootstrap
import littleware.security.auth.client.ClientLoginModule

object CliApp {
  val log = Logger.getLogger( getClass.getName )
  def main( args:Array[String] ):Unit = {
    val loginBuilder = ClientLoginModule.newBuilder
    val cleanArgs = if ((args.length > 1) && args(0).matches("^-+[uU][rR][lL]")) {
      // Currently only support -url argument
      val sUrl = args(1);
      val url = new URL(sUrl);
      loginBuilder.host(url.getHost());
      if (args.length > 2) {
        Arrays.copyOfRange(args, 2, args.length);
      } else {
        new Array[String](0);
      }
    } else {
      args
    }
    try {
      val boot = ClientBootstrap.clientProvider.get.build.automatic(
        loginBuilder.build
      )
      val cl = boot.bootstrap(classOf[LgoCommandLine])
      //val cl = boot.;
      val exitCode = cl.run(cleanArgs);
      boot.shutdown();
      System.exit( exitCode )
    } catch {
      case ex:LoginException => {
          log.log(Level.SEVERE, "Failed login", ex)
          System.exit(1)
        }
    }
      
  }
}
