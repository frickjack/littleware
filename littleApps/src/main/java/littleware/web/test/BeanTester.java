/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.web.test;


import java.util.logging.Logger;

import junit.framework.*;

import littleware.web.*;
import littleware.web.beans.*;

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
    @Override
    public void setUp() {
    }

    /** Call setUp() to clear out test data */
    @Override
    public void tearDown() {
        setUp();
    }

    /**...
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
     *
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
     *
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
    */

    /**
     * Make sure the DefaultsBean got its data ok
     */
    public void testDefaultsBean() {
        DefaultsBean bean_default = new DefaultsBean();
        assertTrue("Defaults bean has valid data",
                null != bean_default.getDefaults().get("contact_email"));
    }
}

