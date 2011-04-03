/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.test;

import java.util.logging.Logger;
import java.util.logging.Level;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import java.io.*;

import java.util.UUID;
import junit.framework.*;

import littleware.asset.*;
import littleware.asset.xml.*;
import littleware.base.*;

/**
 * Tester for the SimpleXmlAssetBuilder Asset super class handling
 * of XML-based getData/setData via annotations.
 */
public class XmlAssetTester extends TestCase {
    private static final Logger log = Logger.getLogger(XmlAssetTester.class.getName());

    /**
     * Do nothing constructor - just pass the test-name through to super.
     */
    public XmlAssetTester(String s_test_name) {
        super(s_test_name);
    }

    /** Little enum-type to support TestBuilder below */
    public enum TestType {

        OK, FRICK;

        public static TestType parse(String s_item) throws ParseException {
            if (s_item.equals(OK.toString())) {
                return OK;
            }
            if (s_item.equals(FRICK.toString())) {
                return FRICK;
            }
            throw new ParseException("Invalid string: " + s_item);
        }
    }

    /**
     * Little test class
     */
    public static class TestBuilder extends SimpleXmlAssetBuilder {

        private String os_foo = "foo";
        private String os_bla = "bla";
        private int oi_100 = 100;
        private TestType on_type = TestType.OK;

        /** Custom XML setter for setNum100 */
        public static class Num100Setter extends SimpleXmlDataSetter {

            @Override
            public void setData(Object x_target, String s_data) {
                TestBuilder a_test = (TestBuilder) x_target;
                a_test.setNum100(Integer.parseInt(s_data));
            }
        }

        /** Custom XML setter for setNum100 */
        public static class TestTypeSetter extends SimpleXmlDataSetter {

            @Override
            public void setData(Object x_target, String s_data) throws ParseException {
                TestBuilder a_test = (TestBuilder) x_target;
                a_test.setTestType(TestType.parse(s_data));
            }
        }
        /** Namespace for getData/setData XML data */
        public final static String OS_NAMESPACE =
                "http://www.littleware.com/xml/namespace/2006/testxml";

        /**
         * Constructor just assigns AssetType.GENERIC,
         * an object-id, and a name.  Passes namespace and prefix
         * through to super.
         */
        public TestBuilder() {
            super( OS_NAMESPACE, "txml", "test_data", AssetType.GENERIC);
            setName("testcase");
        }

        @XmlGetter(element = "bla")
        public String getBla() {
            return os_bla;
        }

        @XmlSetter(element = "bla")
        public void setBla(String s_bla) {
            os_bla = s_bla;
        }

        @XmlGetter(element = "foo")
        public String getFoo() {
            return os_foo;
        }

        @XmlSetter(element = "foo")
        public void setFoo(String s_foo) {
            os_foo = s_foo;
        }

        @XmlGetter(element = "", attribute = "num100")
        public int getNum100() {
            return oi_100;
        }

        @XmlSetter(element = "", attribute = "num100", setter = Num100Setter.class)
        public void setNum100(int i_100) {
            oi_100 = i_100;
        }

        @XmlGetter(element = "ttype")
        public TestType getTestType() {
            return on_type;
        }

        @XmlSetter(element = "ttype", setter = TestTypeSetter.class)
        public void setTestType(TestType n_type) {
            on_type = n_type;
        }

        /** Clear all the getData() data elements to null to support testing */
        public void clearData() {
            os_bla = null;
            os_foo = null;
            oi_100 = 0;
            on_type = null;
        }
    }

    /**
     * Run the TestBuilder SimpleXmlAssetBuilder test subtype through some getData/setData
     * tests and XML verification.
     */
    public void testXmlAsset() {
        try {
            Asset a_xml = new TestBuilder().homeId( UUID.randomUUID() ).
                    fromId( UUID.randomUUID() ).build();
            String s_xml = a_xml.getData();

            log.log(Level.INFO, "TestBuilder getData got: " + s_xml);

            // Verify that the freakin data is properly formed XML
            {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                SAXParser sax_parser = factory.newSAXParser();
                DefaultHandler sax_handler = new DefaultHandler();

                sax_parser.parse(new InputSource(new StringReader(s_xml)),
                        sax_handler);
                // If no exception thrown, then the XML is properly formed
            }

            String s_xml2 = a_xml.copy().data(s_xml).build().getData();
            log.log(Level.INFO, "Data reset getData got: " + s_xml2);
            assertTrue("Data reset matches original data", s_xml2.equals(s_xml));
        } catch (Exception e) {
            log.log(Level.WARNING, "Caught: " + e +
                    ", " + BaseException.getStackTrace(e));
            assertTrue("Caught: " + e, false);
        }
    }
}
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

