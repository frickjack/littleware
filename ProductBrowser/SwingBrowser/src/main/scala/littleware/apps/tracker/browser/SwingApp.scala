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

import edu.auburn.library.util.JclWrapper._
import java.net.URL
import java.util.Properties
import javax.swing.JFrame
import javax.swing.JPanel
import littleware.apps.swingbase.SwingBaseModule
import littleware.apps.swingbase.view.BaseView
import littleware.bootstrap.client.ClientBootstrap

object SwingApp {

  class Main @Inject() (
    viewBuilder:BaseView.ViewBuilder
  ) extends Runnable {
    private val jcontentPanel = new JPanel

    override def run = {
      val jframe = new JFrame( "ProductBrowser" )
      viewBuilder.container( jframe ).basicContent( jcontentPanel ).build
      jframe.setVisible( true )
    }
  }

  def main( args:Array[String] ):Unit = invokeLater( () => {
      val swingBase = (new SwingBaseModule.Factory).appName( "ProductBrowser"
      ).version( "2.1"
      ).helpUrl( new URL( "http://code.google.com/p/littleware/" )
      ).properties( new Properties
      )
      val boot = ClientBootstrap.clientProvider.get.addModuleFactory(
        swingBase
      ).build
    }
  )
}
