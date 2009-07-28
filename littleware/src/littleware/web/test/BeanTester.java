package littleware.web.test;

import java.util.*;
import java.sql.*;
import java.security.*;
import java.security.acl.*;
import javax.security.auth.login.*;
import javax.mail.*;

import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.*;

import littleware.base.*;
import littleware.web.*;
import littleware.web.beans.*;
import littleware.apps.addressbook.*;

/**
 * TestFixture instantiates different littleware.web.beans beans,
 * and exercises them a bit.
 */
public class BeanTester extends TestCase {

    private static Logger olog_generic = Logger.getLogger("littelware.web.test.BeanTester");

    /**
     * Do nothing constructor
     */
    public BeanTester(String s_name) {
        super(s_name);
    }

    /**
     * Erase the test users/groups out of the database,
     */
    public void setUp() {
    }

    /** Call setUp() to clear out test data */
    public void tearDown() {
        setUp();
    }

    /**
     * Test the SessionBean
     */
    public void testLoginBean() {
        LoginBean bean_test = new LoginBean ();
        
        bean_test.setSessionBean( new SessionBean() );

        bean_test.setName("littleware.test_user");
        bean_test.setPassword("bogus");
        String s_result = bean_test.authenticateAction();

        assertTrue("Bad password login should have failed, but got result: " + s_result,
                ! bean_test.getLastResult().equals( ActionResult.Ok )
                );
    }

    public void testUpdateContactBean () {
        try {
            UpdateContactBean bean_test = new UpdateContactBean();
            bean_test.setSessionBean( new SessionBean () );
	    // setting up the session should have
	    // automatically set the update user
	    String s_test_user = bean_test.getSessionBean ().getUser ().getName ();
	    assertTrue( "UpdateBean's user set by session: " +
			bean_test.getUser () + " == " + s_test_user,
			bean_test.getUser ().equals ( s_test_user ) 
			);
	} catch ( Exception e ) {
            assertTrue("Caught unexepcted: " + e, false);
        }
    }

    /**
     * Test the SessionBean
     */
    public void testNewUserBean() {
        NewUserBean bean_test = new NewUserBean();

        bean_test.setSessionBean( new SessionBean () );
        bean_test.setName("littleware.test_user");
        bean_test.setEmail("bogus@bla.com");
        bean_test.setUsaState(UsaState.AL);
        bean_test.setCity("Auburn");
        String s_result = bean_test.newUserAction();

        assertTrue("Illegal user create should have failed, but got result: " + s_result,
                ! bean_test.getLastResult().equals( ActionResult.Ok )
                );
    }

    /**
     * Make sure that the NewUserBean can send an e-mail to us
     */
    public void testNewUserEmail() {
        try {
            NewUserBean bean_test = new NewUserBean ();
            
            bean_test.setSessionBean( new SessionBean () );
            
            String s_name = "bogus";
            String s_password = "password";
            String s_to = "pasquinir@bellsouth.net";

            bean_test.sendWelcomeMessage(s_name, s_to,
                    s_password);
            olog_generic.log(Level.INFO, "Test user " + s_to + " should have just received test e-mail");
        } catch (MessagingException e) {
            assertTrue("Caught unexepcted: " + e, false);
        }
    }

    /**
     * Make sure the SessionBean can log us in, and get our contact information
     */
    public void testBasicSession() {
        try {
            LoginBean bean_test = new LoginBean();
            SessionBean bean_session = new SessionBean();
            bean_test.setSessionBean( bean_session );
            
            bean_test.setName("littleware.test_user");
            bean_test.setPassword("test123");
            String s_result = bean_test.authenticateAction();

            assertTrue("Login ok: " + s_result,
                    bean_test.getLastResult().equals( ActionResult.Ok ) 
                    );

            /*.. disable for now ..
            Contact contact_user = bean_session.getContact();
            assertTrue("Got a contact", contact_user != null);
            olog_generic.log(Level.INFO, "Got contact with data: " + contact_user.getData());
             * */
        } catch (Exception e) {
            olog_generic.log(Level.WARNING, "Caught unexpected: " + e + ", " +
                    BaseException.getStackTrace(e));
            assertTrue("Caught unexpected: " + e, false);
        }
    }

    /**
     * Make sure the DefaultsBean got its data ok
     */
    public void testDefaultsBean() {
        DefaultsBean bean_default = new DefaultsBean();
        assertTrue("Defaults bean has valid data",
                null != bean_default.getDefaults().get("contact_email"));
    }
}
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

