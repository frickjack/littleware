/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.pickle.internal;

import org.xml.sax.*;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import littleware.base.*;
import littleware.asset.*;
import littleware.asset.pickle.AssetXmlPickler;
import littleware.asset.pickle.XmlTranslator;

/**
 * XmlTranslator for GenericAsset.GENERIC/AssetType.UNKNOWN
 * assets.  Has intelligence to chain off pickle/unpickle
 * handling to the appropriate specialized AssetType PickleMaker
 * when possible.
 */
public class PickleXml extends XmlTranslator<Asset> implements AssetXmlPickler {

    private static final Logger log = Logger.getLogger(PickleXml.class.getName());

    public static enum ParserState {

        /** Parser is scanning for an element with an asset:asset_type attribute */
        SCANNING,
        /**
         * An asset has been found, but its parsing has been delegated off to
         * an XmlTranslater (via PickleType.XML) specialized for the assets type.
         */
        DELEGATE,
        /**
         * An asset block has been found, no specialized XMLTranslater available,
         * so do the best we can
         */
        IN_ASSET,
        /** Within the asset:core block from which we can extract the AssetElement members */
        IN_CORE,
        /** Done retrieving IN_CORE data for this asset, ignore subsequent IN_CORE blocks */
        DONE
    }

    /**
     * Simple XML-type asset elements found within an asset:core block
     */
    public static enum AssetElement {

        NAME {

            @Override
            public void writeElement(Asset a_data, Writer io_out, String s_prefix) throws IOException {
                if (null != a_data.getName()) {
                    writeElement(io_out, s_prefix, a_data.getName());
                }
            }

            @Override
            public void assignElement(String s_data, AssetBuilder builder) {
                builder.setName(s_data);
            }
        },
        OBJECT_ID {

            @Override
            public void writeElement(Asset a_data, Writer io_out, String s_prefix) throws IOException {
                writeElement(io_out, s_prefix, UUIDFactory.makeCleanString(a_data.getId()));
            }

            @Override
            public void assignElement(String s_data, AssetBuilder builder) {
                builder.setId(UUIDFactory.parseUUID(s_data));
            }
        },
        HOME_ID {

            @Override
            public void writeElement(Asset a_data, Writer io_out, String s_prefix) throws IOException {
                writeElement(io_out, s_prefix, UUIDFactory.makeCleanString(a_data.getHomeId()));
            }

            @Override
            public void assignElement(String s_data, AssetBuilder builder) {
                builder.setHomeId(UUIDFactory.parseUUID(s_data));
            }
        },
        /*..
        TO_ID {

            @Override
            public void writeElement(Asset a_data, Writer io_out, String s_prefix) throws IOException {
                if (null != a_data.getToId()) {
                    writeElement(io_out, s_prefix, UUIDFactory.makeCleanString(a_data.getToId()));
                }
            }

            @Override
            public void assignElement(String s_data, AssetBuilder builder) {
                builder.setToId(UUIDFactory.parseUUID(s_data));
            }
        },
        FROM_ID {

            @Override
            public void writeElement(Asset a_data, Writer io_out, String s_prefix) throws IOException {
                if (null != a_data.getFromId()) {
                    writeElement(io_out, s_prefix, UUIDFactory.makeCleanString(a_data.getFromId()));
                }
            }

            @Override
            public void assignElement(String s_data, AssetBuilder builder) {
                builder.setFromId(UUIDFactory.parseUUID(s_data));
            }
        },
         *
         */
        ACL_ID {

            @Override
            public void writeElement(Asset a_data, Writer io_out, String s_prefix) throws IOException {
                if (null != a_data.getAclId()) {
                    writeElement(io_out, s_prefix, UUIDFactory.makeCleanString(a_data.getAclId()));
                }
            }

            @Override
            public void assignElement(String s_data, AssetBuilder builder) {
                builder.setAclId(UUIDFactory.parseUUID(s_data));
            }
        },
        CREATOR_ID {

            @Override
            public void writeElement(Asset a_data, Writer io_out, String s_prefix) throws IOException {
                writeElement(io_out, s_prefix, UUIDFactory.makeCleanString(a_data.getCreatorId()));
            }

            @Override
            public void assignElement(String s_data, AssetBuilder builder) {
                builder.setCreatorId(UUIDFactory.parseUUID(s_data));
            }
        },
        UPDATER_ID {

            @Override
            public void writeElement(Asset a_data, Writer io_out, String s_prefix) throws IOException {
                writeElement(io_out, s_prefix, UUIDFactory.makeCleanString(a_data.getLastUpdaterId()));
            }

            @Override
            public void assignElement(String s_data, AssetBuilder builder) {
                builder.setLastUpdaterId(UUIDFactory.parseUUID(s_data));
            }
        },
        CREATE_DATE {

            @Override
            public void writeElement(Asset a_data, Writer io_out, String s_prefix) throws IOException {
                writeElement(io_out, s_prefix, oformat_date.format(a_data.getCreateDate()));
            }

            @Override
            public void assignElement(String s_data, AssetBuilder builder) {
            }
        },
        UPDATE_DATE {

            @Override
            public void writeElement(Asset a_data, Writer io_out, String s_prefix) throws IOException {
                writeElement(io_out, s_prefix, oformat_date.format(a_data.getLastUpdateDate()));
            }

            @Override
            public void assignElement(String s_data, AssetBuilder builder) {
            }
        },
        /*..
        START_DATE {

            @Override
            public void writeElement(Asset a_data, Writer io_out, String s_prefix) throws IOException {
                Date t_start = a_data.getStartDate();
                if (null != t_start) {
                    writeElement(io_out, s_prefix, oformat_date.format(t_start));
                }
            }

            @Override
            public void assignElement(String s_data, AssetBuilder builder) {
            }
        },
        END_DATE {

            @Override
            public void writeElement(Asset a_data, Writer io_out, String s_prefix) throws IOException {
                Date t_end = a_data.getEndDate();
                if (null != t_end) {
                    writeElement(io_out, s_prefix, oformat_date.format(t_end));
                }
            }

            @Override
            public void assignElement(String s_data, AssetBuilder builder) {
            }
        },
         *
         */
        ASSET_TYPE {

            @Override
            public void writeElement(Asset a_data, Writer io_out, String s_prefix) throws IOException {
                writeElement(io_out, s_prefix, a_data.getAssetType().toString());
            }

            @Override
            public void assignElement(String s_data, AssetBuilder builder) {
                // should not be necessary when parsing properly formatted data
            }
        },
        COMMENT {

            @Override
            public void writeElement(Asset a_data, Writer io_out, String s_prefix) throws IOException {
                String s_comment = a_data.getComment();
                if (null != s_comment) {
                    writeElement(io_out, s_prefix, XmlSpecial.encode(s_comment));
                }
            }

            @Override
            public void assignElement(String s_data, AssetBuilder builder) {
                builder.setComment(s_data);
            }
        },
        UPDATE_COMMENT {

            @Override
            public void writeElement(Asset a_data, Writer io_out, String s_prefix) throws IOException {
                String s_comment = a_data.getLastUpdate();
                if (null != s_comment) {
                    writeElement(io_out, s_prefix, XmlSpecial.encode(s_comment));
                }
            }

            @Override
            public void assignElement(String s_data, AssetBuilder builder) {
                builder.setLastUpdate(s_data);
            }
        },
        /*...
        VALUE {

            @Override
            public void writeElement(Asset a_data, Writer io_out, String s_prefix) throws IOException {
                writeElement(io_out, s_prefix, Float.toString(a_data.getValue()));
            }

            @Override
            public void assignElement(String s_data, AssetBuilder builder) {
                builder.setValue(Float.parseFloat(s_data));
            }
        },
         *
         */
        TRANSACTION {

            @Override
            public void writeElement(Asset a_data, Writer io_out, String s_prefix) throws IOException {
                writeElement(io_out, s_prefix, Long.toString(a_data.getTimestamp()));
            }

            @Override
            public void assignElement(String s_data, AssetBuilder builder) {
                builder.setTimestamp(Long.parseLong(s_data));
            }
        };
        protected final DateFormat oformat_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        /**
         * Write the element value from a_data to io_out using the
         * given XML namespace prefix
         *
         * @param a_data to pull info from
         * @param io_out to write to
         * @param s_prefix namespace prefix
         */
        public abstract void writeElement(Asset a_data, Writer io_out, String s_prefix) throws IOException;

        /**
         * Little utility for shared code
         *
         * @param io_out to write XML data to
         * @param s_prefix namespace prefix
         * @param s_raw_data to run through XML filter
         */
        protected void writeElement(Writer io_out, String s_prefix, String s_raw_data) throws IOException {
            String s_tag = this.toString().toLowerCase();
            io_out.write("<");
            io_out.write(s_prefix);
            io_out.write(":");
            io_out.write(s_tag);
            io_out.write(">");
            io_out.write(XmlSpecial.encode(s_raw_data));
            io_out.write("</");
            io_out.write(s_prefix);
            io_out.write(":");
            io_out.write(s_tag);
            io_out.write(">\n");
        }

        /**
         * Assign the element value specified by s_value to a_blank
         * after performing appropriate type conversion.
         *
         * @param s_data element contents extracted from XML parse
         * @param builder to assign data to
         */
        public abstract void assignElement(String data, AssetBuilder builder);
    }
    private ParserState on_state = ParserState.SCANNING;
    // Handler to delegate to when in DELEGATE ParseMode
    private XmlTranslator<? extends Asset> opickle_chain = null;
    private AssetBuilder resultBuilder = null;

    @Override
    public Asset getResult() throws IllegalStateException {
        if (on_state.equals(ParserState.DELEGATE)) {
            return opickle_chain.getResult();
        } else if (on_state.equals(ParserState.DONE)) {
            return resultBuilder.build();
        } else {
            throw new IllegalStateException("Parser result not yet ready, in state: " + on_state);
        }
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
        if (on_state.equals(ParserState.DELEGATE)) {
            opickle_chain.startElement(s_namespace, s_simple, s_qualified, v_attrs);
        } else if (on_state.equals(ParserState.SCANNING)) {
            final String typeName = v_attrs.getValue(XmlTranslator.OS_ASSET_NAMESPACE,
                    "asset_type");
            if (null != typeName) {
                try {
                    final AssetType assetType = AssetType.getMember(typeName);
                    throw new UnsupportedOperationException( "Need to setup an asset allocator framework!" );
                    /*...
                    if ((!assetType.equals(GenericAsset.GENERIC))
                            && (!assetType.equals(AssetType.UNKNOWN))
                            && PickleType.XML.hasPickleMaker(assetType)) {
                        opickle_chain = (XmlTranslator<? extends Asset>) PickleType.XML.createPickleMaker(assetType);
                        on_state = ParserState.DELEGATE;
                        opickle_chain.startElement(s_namespace, s_simple,
                                s_qualified, v_attrs);
                    } else {
                        // No delegate handler available
                        resultBuilder = null; // FRICKJACK!!! n_type.create();
                        on_state = ParserState.IN_ASSET;
                        // recurse on this data now that we're in the IN_ASSET state
                        startElement(s_namespace, s_simple, s_qualified, v_attrs);
                    }
                     *
                     */
                } catch (RuntimeException e) {
                    throw e;
                    /*
                } catch (SAXException e) {
                    throw e;
                     * 
                     */
                } catch (Exception e) {
                    throw new SAXException("Failure to resolve pickle handler for asset type: "
                            + typeName);
                }
            }
        } else if (on_state.equals(ParserState.IN_ASSET)) {
            // Watch for the asset:core to drop into the IN_CORE state
            if (s_simple.equals("core")
                    && s_namespace.equals(XmlTranslator.OS_ASSET_NAMESPACE)) {
                on_state = ParserState.IN_CORE;
            }
        }
    }
    private static Map<String, AssetElement> ov_name2element = new HashMap<String, AssetElement>();

    static {
        for (AssetElement n_element : AssetElement.values()) {
            ov_name2element.put(n_element.toString(), n_element);
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
        if (on_state.equals(ParserState.DELEGATE)) {
            opickle_chain.endElement(s_namespace, s_simple, s_qualified);
        } else if (on_state.equals(ParserState.IN_CORE)) {
            if (s_namespace.equals(XmlTranslator.OS_ASSET_NAMESPACE)) {
                if (s_simple.equals("core")
                        && s_namespace.equals(XmlTranslator.OS_ASSET_NAMESPACE)) {
                    on_state = ParserState.DONE;
                } else {
                    AssetElement n_element = ov_name2element.get(s_simple);
                    if (null != n_element) {
                        n_element.assignElement(os_buffer.toString(), resultBuilder);
                    }
                }
            }
            os_buffer.setLength(0);
        }
    }
    public StringBuilder os_buffer = new StringBuilder();

    @Override
    public void characters(char buf[], int offset, int len)
            throws SAXException {
        if (on_state.equals(ParserState.DELEGATE)) {
            opickle_chain.characters(buf, offset, len);
        } else if (on_state.equals(ParserState.IN_CORE)) {
            os_buffer.append(buf, offset, len);
        }
    }

    @Override
    public Asset unpickle(Reader io_data) throws AssetException, BaseException,
            GeneralSecurityException, IOException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser sax_parser = factory.newSAXParser();

            sax_parser.parse(new InputSource(io_data),
                    this);
            return getResult();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseException("Failed to parse: " + e, e);
        }
    }

    @Override
    public void pickle(Asset a_in, Writer io_data) throws AssetException, BaseException,
            GeneralSecurityException, IOException {
        io_data.write("<asset:core asset:asset_type=\"");
        io_data.write(a_in.getAssetType().toString());
        io_data.write("\" xmlns:asset=\"");
        io_data.write(XmlTranslator.OS_ASSET_NAMESPACE);
        io_data.write("\">\n");

        for (AssetElement n_element : AssetElement.values()) {
            n_element.writeElement(a_in, io_data, "asset");
        }

        io_data.write("<asset:data>");
        /*..
        if (a_in instanceof XmlAssetBuilder) {
            io_data.write(a_in.getData());
        } else {
            io_data.write(XmlSpecial.encode(a_in.getData()));
        }
         * 
         */
        io_data.write("</asset:data>\n");
        io_data.write("</asset:core>\n");
    }
}
