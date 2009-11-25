/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security;

import java.rmi.RemoteException;
import java.util.UUID;
import java.security.GeneralSecurityException;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import java.io.StringReader;

import littleware.asset.*;
import littleware.base.*;

/**
 * Quota asset gets attached to a user to restrict
 * the user's access to the littleware database in some way.
 */
public class QuotaBuilder extends SimpleAssetBuilder implements Quota.Builder {

    private final static String xmlNamespace =
            "http://www.littleware.com/xml/namespace/2006/quota";
    private static final long serialVersionUID = -502948639955038504L;
    private int limit = -1;

    public QuotaBuilder() {
        super(SecurityAssetType.QUOTA);
    }

    @Override
    public Quota build() {
        return new QuotaAsset( this, limit );
    }

    @Override
    public int getQuotaCount() {
        return (int) getValue().floatValue();
    }

    @Override
    public void setQuotaCount(int value) {
        setValue((float) value);
    }

    @Override
    public Quota.Builder quotaCount( int value ) {
        setQuotaCount( value );
        return this;
    }

    @Override
    public int getQuotaLimit() {
        return limit;
    }

    @Override
    public void setQuotaLimit(int value) {
        limit = value;
    }
    @Override
    public Quota.Builder quotaLimit( int value ) {
        setQuotaLimit( value );
        return this;
    }

    @Override
    public Quota.Builder incrementQuotaCount() {
        this.setQuotaCount(this.getQuotaCount() + 1);
        return this;
    }

    /**
     * SAX parser handler
     */
    private static class XmlDataHandler extends DefaultHandler {

        private int oi_parse_limit = 0;

        /**
         * Callback for XML start-tag.
         * Pulls the limit attribute out of the quotaspec tag.
         */
        @Override
        public void startElement(String s_namespace,
                String s_simple, // simple name (localName)
                String s_qualified, // qualified name
                Attributes v_attrs)
                throws SAXException {
            // Clear the data
			/*..
            olog_generic.log ( Level.FINE, "Starting element: " + s_simple +
            ", " + s_qualified
            );
            ..*/
            if (s_simple.equals("quotaspec")) {
                oi_parse_limit = Integer.parseInt(v_attrs.getValue("limit"));
            }
        }

        /** Return the quota-limit parsed out of the XML file */
        public int getParseLimit() {
            return oi_parse_limit;
        }
    }

    /**
     * Parse XML data to determine quota limit and supporting info
     */
    @Override
    public void setData(String value) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser sax_parser = factory.newSAXParser();
            XmlDataHandler sax_handler = new XmlDataHandler();

            sax_parser.parse(new InputSource(new StringReader(value)),
                    sax_handler);
            limit = sax_handler.getParseLimit();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse: " + e, e);
        }
    }

    /**
     * Procedurally generate XML data
     */
    @Override
    public String getData() {
        String s_data = "<quota:quotaset xmlns:quota=\"http://www.littleware.com/xml/namespace/2006/quota\">\n";
        s_data += "<quota:quotaspec type=\"update\" limit=\"";
        s_data += Integer.toString(limit);
        s_data += "\" />\n</quota:quotaset>";
        return s_data;
    }

    private static class QuotaAsset extends SimpleAssetBuilder.SimpleAsset implements Quota {
        private int limit;

        /**
         * Constructor for serializable
         */
        private QuotaAsset(){}

        public QuotaAsset( QuotaBuilder builder, int limit ) {
            super( builder );
            this.limit = limit;
        }
        
        @Override
        public UUID getNextInChainId() {
            return getToId();
        }

        @Override
        public Quota getNextInChain(AssetRetriever retriever) throws BaseException, AssetException,
                GeneralSecurityException, RemoteException {
            return (Quota) retriever.getAsset(getToId()).getOr(null);
        }

        @Override
        public UUID getUserId() {
            return getFromId();
        }

        @Override
        public LittleUser getUser(AssetRetriever retriever) throws BaseException, AssetException,
                GeneralSecurityException, RemoteException {
            return (LittleUser) retriever.getAsset(getFromId()).getOr(null);
        }

        @Override
        public int getQuotaCount() {
            return (int) getValue().floatValue();
        }

        @Override
        public int getQuotaLimit() {
            return limit;
        }

        @Override
        public Quota.Builder copy() {
            return (Builder) super.copy();
        }
    }
}

