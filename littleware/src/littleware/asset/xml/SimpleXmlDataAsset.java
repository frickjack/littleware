/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.xml;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.base.ParseException;
import littleware.base.XmlSpecial;
import littleware.base.AssertionFailedException;

/**
 * Utility base class for asset-types that store formatted
 * XML data in the getData asset field at save time,
 * but automatically extract that data at asset load time
 * for access by specialized accessors.
 * Overrides getData() and setData() to base the XML data
 * on the XmlGetter and XmlSetter annotations of the implementation 
 * subclass.  The getContentHandler() parser from this class
 * uses the annotations to build up a set of XmlDataGetter and
 * XmlDataSetter handlers that map XML elements to getter and
 * setter methods on an asset instance object.
 */
public abstract class SimpleXmlDataAsset extends AbstractXmlDataAsset {

    private static final Logger olog_generic = Logger.getLogger(SimpleXmlDataAsset.class.getName());
    private HashMap<String, XmlDataSetter> ov_element_in = new HashMap<String, XmlDataSetter>();
    private HashMap<String, XmlDataSetter> ov_attribute_in = new HashMap<String, XmlDataSetter>();
    private HashMap<String, XmlDataGetter> ov_element_out = new HashMap<String, XmlDataGetter>();
    private HashMap<String, XmlDataGetter> ov_attribute_out = new HashMap<String, XmlDataGetter>();
    private String os_namespace = null;
    private String os_prefix = null;
    private String os_root = null;

    /**
     * Avoid serializing the annotation information - that
     * stuff can be autoconstructed on the other end
     */
    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.writeObject(os_namespace);
        out.writeObject(os_prefix);
        out.writeObject(os_root);
    }

    /**
     * Avoid serializing the annotation information
     */
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        os_namespace = (String) in.readObject();
        os_prefix = (String) in.readObject();
        os_root = (String) in.readObject();

        ov_element_in = new HashMap<String, XmlDataSetter>();
        ov_attribute_in = new HashMap<String, XmlDataSetter>();
        ov_element_out = new HashMap<String, XmlDataGetter>();
        ov_attribute_out = new HashMap<String, XmlDataGetter>();
        try {
            scanForAnnotations();
        } catch (java.security.AccessControlException e) {
            olog_generic.log(Level.WARNING, "Security constrained environment, caught: " + e);
        }
    }

    /**
     * Little utility scans the implementation subclass for XmlGetter/XMLout annotations
     */
    private void scanForAnnotations() {
        Method[] v_methods = this.getClass().getDeclaredMethods();

        try {
            for (Method method_scan : v_methods) {
                XmlSetter ann_xmlin = method_scan.getAnnotation(XmlSetter.class);

                if (ann_xmlin != null) {
                    XmlDataSetter xml_reader = ann_xmlin.setter().newInstance();
                    xml_reader.init(method_scan);

                    if ((ann_xmlin.attribute() != null) && (!ann_xmlin.attribute().equals(""))) {
                        ov_attribute_in.put(ann_xmlin.attribute(), xml_reader);
                    }
                    if ((ann_xmlin.element() != null) && (!ann_xmlin.element().equals(""))) {
                        ov_element_in.put(ann_xmlin.element(), xml_reader);
                    }
                } else {
                    XmlGetter ann_xmlout = method_scan.getAnnotation(XmlGetter.class);
                    if (ann_xmlout != null) {
                        XmlDataGetter xml_writer = ann_xmlout.getter().newInstance();
                        xml_writer.init(method_scan);

                        if ((ann_xmlout.attribute() != null) && (!ann_xmlout.attribute().equals(""))) {
                            ov_attribute_out.put(ann_xmlout.attribute(), xml_writer);
                        }
                        if ((ann_xmlout.element() != null) && (!ann_xmlout.element().equals(""))) {
                            ov_element_out.put(ann_xmlout.element(), xml_writer);
                        }
                    }
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionFailedException("Failed to initialize XmlDataAsset from annotations, caught: " + e, e);
        }
    }

    /** 
     * No-arg constructor - only intended to support serialization 
     */
    protected SimpleXmlDataAsset() {
        scanForAnnotations();
    }

    /**
     * Preferred constructor - specify the namespace
     * for the data XML, and the XML-tag prefix for getData.
     *
     * @param s_namespace get/setData XML data is in
     * @param s_prefix to prefix XML tags with for getData()
     * @param s_root unqualified root-element name
     */
    protected SimpleXmlDataAsset(String s_namespace,
            String s_prefix,
            String s_root) {
        os_namespace = s_namespace;
        os_prefix = s_prefix;
        os_root = s_root;
        scanForAnnotations();
    }

    /**
     * Generate an attribute string: <br />
     *         bla="value" bla2="value2" ...  <br />
     * based on the XmlGetter annotations on the class.
     * Does not do any check to verify that the
     * attribute data is legal (no embedded " or \n or whatever)
     * as an XML attribute.
     */
    protected String getRootAttributesFromAnnotations() {
        StringBuilder s_result = new StringBuilder();

        for (Map.Entry<String, XmlDataGetter> map_entry : ov_attribute_out.entrySet()) {
            String s_data = map_entry.getValue().getData(SimpleXmlDataAsset.this);

            if (null != s_data) {
                s_result.append(map_entry.getKey()).append("=\"");
                s_result.append(s_data).append("\" ");
            }
        }
        return s_result.toString();
    }

    /**
     * Procedurally generates XML data in the constructor-supplied namespace
     * by scanning the implementation
     * class for methods with XMLout annotations.
     * The result in not properly formed XML since it is not
     * wrapped in the root XML element.
     */
    protected String getDataFromAnnotations() {
        StringBuilder s_result = new StringBuilder();

        for (Map.Entry<String, XmlDataGetter> map_entry : ov_element_out.entrySet()) {
            String s_data = map_entry.getValue().getData(SimpleXmlDataAsset.this);

            if (null != s_data) {
                s_result.append('<').append(os_prefix).append(':').
                        append(map_entry.getKey()).append('>');
                s_result.append(XmlSpecial.encode(s_data));
                s_result.append("</").append(os_prefix).append(':').
                        append(map_entry.getKey()).append(">\n");
            }
        }
        return s_result.toString();
    }

    /**
     * Procedurally generates XML data in the constructor-supplied namespace
     * by scanning the implementation
     * class for methods with XMLout annotations.
     * Calls getDataFromAnnotations, then just
     * wraps the result in the root element.
     * Subtype may override to include data that could not
     * easily be handled via the XmlGetter/XmlSetter annotations -
     * since those annotations work best with simple types of data.
     */
    @Override
    public String getData() {
        StringBuilder s_data = new StringBuilder();
        s_data.append('<').append(os_prefix).append(':').append(os_root).
                append(' ');
        s_data.append(getRootAttributesFromAnnotations()).
                append("xmlns:").append(os_prefix).append("=\"").
                append(os_namespace).append("\" >\n");
        s_data.append(getDataFromAnnotations());
        s_data.append("</").append(os_prefix).append(':').append(os_root).
                append(">\n");
        return s_data.toString();
    }


    /**
     * Return a newly allocated Handler that is aware of the XmlGetter/XmlSetter
     * annotations on this object&apos;s class.
     * Subtypes that want to handle some XML elements via annotations and
     * other XML elements via parser callbacks should override
     * this method, and return a Handler that handles the XML elements
     * that require custom handling, but call through to a
     * handler from super.getSaxDataHandler() to handle other XML elements
     * and attributes.
     */
    @Override
    public DefaultHandler getSaxDataHandler() {
        return new XmlDataHandler();
    }

    /**
     * SAX parser handler 
     */
    private class XmlDataHandler extends DefaultHandler {

        public StringBuilder os_buffer = new StringBuilder();

        public XmlDataHandler() {
        }

        /**
         * Callback for XML start-tag
         */
        @Override
        public void startElement(String s_namespace,
                String s_simple, // simple name (localName)
                String s_qualified, // qualified name
                Attributes v_attrs)
                throws SAXException {
            if (os_namespace.equals(s_namespace) && s_simple.equals(os_root)) {// then check the attributes
                try {
                    int i_attrs = v_attrs.getLength();
                    for (int i = 0; i < i_attrs; ++i) {
                        XmlDataSetter xml_setter = ov_attribute_in.get(v_attrs.getQName(i));
                        if (null != xml_setter) {
                            xml_setter.setData(SimpleXmlDataAsset.this, v_attrs.getValue(i));
                        }
                    }
                } catch (ParseException e) {
                    throw new SAXException("Failure handling root-node attributes", e);
                }
            }
            os_buffer.setLength(0);
        }

        /**
         * Callback for XML end-element
         *
         * @param s_simple name of element
         * @param s_qualified name of element
         */
        @Override
        public void endElement(String s_namespace,
                String s_simple,
                String s_qualified)
                throws SAXException {
            if (s_namespace.equals(os_namespace)) {
                XmlDataSetter xml_setter = ov_element_in.get(s_simple);

                if (null != xml_setter) {
                    try {
                        xml_setter.setData(SimpleXmlDataAsset.this, os_buffer.toString());
                    } catch (ParseException e) {
                        throw new SAXException("Bad XmlDataSetter data", e);
                    }
                }

                os_buffer.setLength(0);
            }
        }

        @Override
        public void characters(char buf[], int offset, int len)
                throws SAXException {
            os_buffer.append(buf, offset, len);
        }
    }

    /**
     * Return a simple copy of this object
     */
    @Override
    public SimpleXmlDataAsset clone() {
        SimpleXmlDataAsset a_xml = (SimpleXmlDataAsset) super.clone();
        a_xml.ov_element_in = (HashMap<String, XmlDataSetter>) ov_element_in.clone();
        a_xml.ov_attribute_in = (HashMap<String, XmlDataSetter>) ov_attribute_in.clone();
        a_xml.ov_element_out = (HashMap<String, XmlDataGetter>) ov_element_out.clone();
        a_xml.ov_attribute_out = (HashMap<String, XmlDataGetter>) ov_attribute_out.clone();
        return a_xml;
    }
}
