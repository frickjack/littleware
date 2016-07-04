package littleware.asset.xml;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import java.io.*;

import littleware.asset.AssetType;
import littleware.asset.spi.AbstractAssetBuilder;
import littleware.base.validate.ValidationException;

public abstract class AbstractXmlAssetBuilder<T extends XmlAssetBuilder> extends AbstractAssetBuilder<T> implements XmlAssetBuilder {

    public AbstractXmlAssetBuilder(AssetType assetType) {
        super(assetType);
    }

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
    public T data(String s_xml) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser sax_parser = factory.newSAXParser();
            DefaultHandler sax_handler = getSaxDataHandler();

            sax_parser.parse(new InputSource(new StringReader(s_xml)),
                    sax_handler);
            return (T) this;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException("Failed to parse: " + e, e);
        }
    }

    @Override
    public abstract DefaultHandler getSaxDataHandler();
}
