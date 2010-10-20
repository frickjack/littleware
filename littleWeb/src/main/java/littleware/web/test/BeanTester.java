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
import littleware.test.LittleTest;
import littleware.web.beans.DefaultsBean;


/**
 * TestFixture instantiates different littleware.web.beans beans,
 * and exercises them a bit.
 */
public class BeanTester extends LittleTest {

    private static Logger olog_generic = Logger.getLogger("littelware.web.test.BeanTester");

    /**
     * Do nothing constructor
     */
    public BeanTester() {
        setName( "testDefaultsBean" );
    }



    /**
     * Make sure the DefaultsBean got its data ok
     */
    public void testDefaultsBean() {
        final DefaultsBean bean_default = new DefaultsBean();
        assertTrue("Defaults bean has valid data",
                null != bean_default.getDefaults().get("contact_email"));
    }
}

