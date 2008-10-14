/*
 * Copyright (c) 2007,2008 Controlled Monitoring Marlborough NZ
 * All Rights Reserved
 */

package littleware.asset.xml;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import java.io.*;

import littleware.asset.SimpleAsset;
import littleware.base.ParseException;

/**
 *
 * @author pasquini
 */
public abstract class AbstractXmlDataAsset extends SimpleAsset implements XmlDataAsset {
    /**
     * Procedurally generate XML data.
     * 
     * @return XML formatted data to store in database
     */
    @Override
    public abstract String getData();
    
        /**
     * Parses the supplied s_xml data, and makes up-calls to 
     * subtype methods annotated with XmlGetter annotations.
     * Uses a ContentHandler retrieved from getContentHandler.
     */
    @Override
    public void setData(String s_xml) throws ParseException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser sax_parser = factory.newSAXParser();
            DefaultHandler sax_handler = getSaxDataHandler();

            sax_parser.parse(new InputSource(new StringReader(s_xml)),
                    sax_handler);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseException("Failed to parse: " + e, e);
        }
    }

    public abstract DefaultHandler getSaxDataHandler();
}
