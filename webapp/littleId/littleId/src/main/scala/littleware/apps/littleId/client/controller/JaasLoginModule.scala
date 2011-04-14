/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.littleId.client.controller

import com.google.inject.Inject
import java.net.URL
import java.util.logging.Level
import javax.security.auth
import javax.security.auth.callback
import javax.security.auth.spi.LoginModule
import littleware.apps.littleId
import littleware.scala.LazyLogger
import scala.collection.JavaConversions._

object JaasLoginModule {
  /**
   * JAAS configuration key to override the default littleId verify URL
   */
  val VerifyUrlOptionKey = "verify_url"
  val VerifyToolOptionKey = "verify_tool"

  /**
   * Simple login.Configuration for easy in-app LoginContext setup -
   * just has a single global application configuration entry that requires
   * authentication against the JaasLoginModule.  Client uses it like this:
   *    (new LoginContext( "ignore", new Subject(), callbackHandler, config )).login
   */
  class Config @Inject()( tool:VerifyTool ) extends auth.login.Configuration {
    val entryArray = Array(
      new auth.login.AppConfigurationEntry( classOf[JaasLoginModule].getName,
                                      auth.login.AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                                      Map( VerifyToolOptionKey -> tool )
      )
    )

    override def getAppConfigurationEntry(name:String):Array[auth.login.AppConfigurationEntry] = entryArray
  }

}

/**
 * Login module for littleware clients.
 * Assumes e-mail as username and verify-secret as password,
 * then verifies against the littleId verify service.
 *
 * @param tool to use for verification - or null if to be set dynamically based
 *               on initialization parameters
 */
class JaasLoginModule @Inject() ( private var tool:VerifyTool ) extends LoginModule {
  private val log = LazyLogger( getClass )
  private var handlerInit:Option[callback.CallbackHandler] = None
  private var subjectInit:Option[auth.Subject] = None

  /**
   * No arg constructor sets a null VerifyTool
   */
  def this() = this( null )

  /**
   * Initialize the module with data from underlying
   * login context
   *
   * @param subject to manage
   * @param handler to invoke for user-supplied data
   * @param sharedState map shared with other login modules
   * @param optionsMap login options - currently only look for verify_url
   */
  override def initialize(
    subject:auth.Subject,
    handler:callback.CallbackHandler,
    sharedState:java.util.Map[String,_],
    optionsMap:java.util.Map[String,_]
  ) {
    this.subjectInit = Option( subject )
    this.handlerInit = Option( handler )
    if ( null == tool ) {
      tool = optionsMap.get( JaasLoginModule.VerifyToolOptionKey ).asInstanceOf[VerifyTool]
    }
    if ( null == tool ) {
      tool = new internal.HttpVerifyTool(
        new URL( Option( optionsMap.get( JaasLoginModule.VerifyUrlOptionKey ).asInstanceOf[String]
          ).getOrElse( "http://beta.frickjack.com:8080/openId/openId/services/verify/" )
        )
      )
    }
  }


  /**
   * Attempt phase-1 login using cached CallbackHandler to get user info
   *
   * @return true if authentication succeeds, false to ignore this module
   * @exception LoginException if authentication fails
   */
  @throws( classOf[auth.login.LoginException] )
  override def login():Boolean  = {
    (for( handler <- handlerInit;
        subject <- subjectInit
    ) yield {
      val nameCallback = new callback.NameCallback("Enter username" )
      val passwordCallback = new callback.PasswordCallback("Enter password", false)
      // Collect username and password via callbacks
      handler.handle(
        Array[callback.Callback](
          nameCallback,
          passwordCallback
        ))

      val email = nameCallback.getName()
      val secret = new String(passwordCallback.getPassword());
      if ( tool.verify( secret, Map( "email" -> email ))) {
        // decorate the authenticated Subject

        //subject.getPrincipals().add(
        //user);
        //subject.getPrivateCredentials().add(helper);

        log.log(Level.INFO, "User authenticated: " + email )
        subject.getPrincipals().add( littleId.common.model.UserCreds(email) )
        true
      } else {
        false
      }
    }).getOrElse( {
        log.log( Level.WARNING, "Subject or Handler not initialized for JaasLoginModule")
        false
      }
    )
  }

  /**
   * Phase 2 commit of login.
   * Idea is that multiple modules may go through a phase 1 login,
   * then phase 2 comes through once all is ok.
   *
   * @exception LoginException if commit fails
   */
  override def commit():Boolean = true

  /**
   * Abort the login process - always returns true for now -
   * should cancel out the LittleSession later.
   *
   * @exception LoginException if abort fails
   */
  override def abort():Boolean = true

  /**
   * Logout the subject associated with this module's context.
   * Does nothing for now - should cancel out the LittleSession later.
   *
   * @return true if logout ok, false to ignore this module
   * @exception LoginException if logout fails
   */
  override def logout():Boolean = true


}
