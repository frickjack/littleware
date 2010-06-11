/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.lgo;

import littleware.lgo.LgoHelp;
import littleware.lgo.LgoHelpLoader;
import littleware.lgo.LgoExample;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

import littleware.base.XmlResourceBundle;

/**
 * Implementation of LgoHelpLoader that loads
 * help info from Locale-specific (language_region,
 * language, non-specific) XML files stored
 * as resources in the classpath.
 * For example - given class
 *     foo.bar.Command
 * in locale en_US
 * this loader looks for resource
 *      /foo/bar/CommandHelp_en_US.xml first,
 * then /foo/bar/CommandHelp_en.xml, and
 * finally /foo/bar/CommandHelp.xml.
 * We assume everythign is UTF-8 encoded.
 * XML namespace is http://www.littleware.com/xml/namespace/2008/helpdoc.
 * Schema:
 *     &lt;help:help fullname="littleware.apps.lgo.XmlEncoder"&gt;
 *     &lt;help:shortname value="encode" /&gt;
 *     &lt;help:shortname value="xencode" /&gt;
 *     &lt;help:shortname value="xenc" /&gt; 
 *     &lt;help:synopsis&gt;xenc [-help]&lt;/help:synopsis&gt;
 *     &lt;help:description&gt;
 *           encode XmlSpecial characters within the string
 *     &gt;help:description;
 *     &lt;help:example title="example 1"&gt;
 *           pbcopy | lgo xenc
 *     &lt;/help:example&gt;
 *     &lt;/help:help&gt;
 *
 * @TODO implement test case
 */
public class XmlLgoHelpLoader implements LgoHelpLoader {
    private static final Logger olog = Logger.getLogger( XmlLgoHelpLoader.class.getName () );
    public final static String  OS_XML_NAMESPACE = 
		"http://www.littleware.com/xml/namespace/2008/helpdoc";
    /**
     * Little enumeration of the XML tags in a legal document
     */
    public enum XmlTag {
        help, shortname, synopsis, description, example;
    }
    
    public LgoHelp loadHelp( String s_request, Locale locale) {
        String        s_basename = s_request.replaceAll( "\\.", "/" ) + "Help";
        List<String>  v_paths = XmlResourceBundle.getResourcePaths( s_basename, locale );
        InputStream   istream_help = null;
        ClassLoader   cloader = XmlLgoHelpLoader.class.getClassLoader();
        String        s_path = "none found";
        for( String s_option : v_paths ) {
            String s_check = s_option + ".xml";
            olog.log( Level.FINE, "Searching for help file: " + s_check );
            istream_help = cloader.getResourceAsStream( s_check );
            if ( null != istream_help ) {
                olog.log( Level.FINE, "Loading help file: " + s_check );
                s_path = s_check;
                break;
            }
        }
                
        if ( null == istream_help ) {
            return null;
        }
        XmlDataHandler sax_handler = new XmlDataHandler ();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser sax_parser = factory.newSAXParser();
            
            sax_parser.parse(
                    new InputSource(
                        new InputStreamReader( istream_help, 
                                    Charset.forName( "UTF-8" )
                                    )
                        ),
                    sax_handler
                    );
            return sax_handler.getHelp ();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            olog.log( Level.WARNING, "Failure parsing resource: " + s_path + 
                    ", caught: " + e 
                    );
        }
        return null;
    }

    public LgoHelp loadHelp( String s_basename ) {
        return loadHelp( s_basename, Locale.getDefault () );
    }
    
    /**
     * SAX parser handler 
     */
    private class XmlDataHandler extends DefaultHandler {
        private final static String  os_not_loaded = "none loaded";
        String             os_full_name = os_not_loaded;
        final List<String> ov_short_names = new ArrayList<String>();
        String             os_synopsis = os_not_loaded;
        String             os_description = os_not_loaded;
                
        String                 os_example_title = os_not_loaded;
        final List<LgoExample> ov_example = new ArrayList<LgoExample> ();
        public StringBuilder   osb_buffer = new StringBuilder();

        public XmlDataHandler() {
        }

        /**
         * Assembly and LgoHelp instance based on the data
         * last parsed by this object.
         * 
         * @return
         */
        public LgoHelp  getHelp () {
            return new EzLgoHelp( os_full_name, ov_short_names, 
                    os_synopsis, os_description, ov_example
                    );
        }
        
        /**
         * Callback for XML start-tag
         */
        @Override
        public void startElement(String s_namespace,
                String s_simple, // simple name (localName)
                String s_qualified, // qualified name
                Attributes v_attrs)
                throws SAXException 
        {
            if (s_namespace.equals(OS_XML_NAMESPACE)) {
                osb_buffer.setLength(0);
                if ( s_simple.equals( XmlTag.help.toString() )) {
                    String s_full_name = v_attrs.getValue("", "fullname");
                    if ( null != s_full_name ) {
                        os_full_name = s_full_name;                        
                    } else {
                        os_full_name = os_not_loaded;
                        olog.log( Level.WARNING, "XML help file does not have fullname attribute set on root help:help element");
                    }               
                } else if ( s_simple.equals( XmlTag.example.toString() ) ) {
                    String s_title = v_attrs.getValue( "", "title" );
                    if ( null != s_title ) {
                        os_example_title = s_title;
                    } else {
                        os_example_title = os_not_loaded;
                        olog.log( Level.WARNING, "XML help file does not have title attribute set on help:example element");
                    }
                }
            }            
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
            if (s_namespace.equals(OS_XML_NAMESPACE)) {
                if ( s_simple.equals( XmlTag.description.toString () ) ) {
                    os_description = osb_buffer.toString ().trim ();
                } else if ( s_simple.equals( XmlTag.shortname.toString () ) ) {
                    ov_short_names.add( osb_buffer.toString() );
                } else if ( s_simple.equals( XmlTag.synopsis.toString () ) ) {
                    os_synopsis = osb_buffer.toString ();
                } else if ( s_simple.equals( XmlTag.example.toString() ) ) {
                    LgoExample example = new EzLgoExample( os_example_title,
                            osb_buffer.toString()
                            );
                    ov_example.add( example );
                }

                osb_buffer.setLength(0);
            }
        }

        @Override
        public void characters(char buf[], int offset, int len)
                throws SAXException {
            osb_buffer.append(buf, offset, len);
        }
    }
}
