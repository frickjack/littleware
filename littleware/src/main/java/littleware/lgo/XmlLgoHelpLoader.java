package littleware.lgo;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import littleware.base.Whatever;

import littleware.base.XmlResourceBundle;

/**
 * Implementation of LgoHelpLoader that loads
 * help info from Locale-specific (language_region,
 * language, non-specific) XML files stored
 * as resources in the classpath, and caches the result.
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
 */
public class XmlLgoHelpLoader implements LgoHelpLoader {
    private static final Logger log = Logger.getLogger( XmlLgoHelpLoader.class.getName () );
    public final static String  OS_XML_NAMESPACE = 
		"http://www.littleware.com/xml/namespace/2008/helpdoc";
    /**
     * Little enumeration of the XML tags in a legal document
     */
    public enum XmlTag {
        help, shortname, synopsis, description, example;
    }
    
    @Override
    public Optional<LgoHelp> loadHelp( String s_request, Locale locale) {
        final String        basename = s_request.replaceAll( "\\.", "/" ) + "Help";
        final List<String>  pathList = XmlResourceBundle.getResourcePaths( basename, locale );
        final ClassLoader   cloader = XmlLgoHelpLoader.class.getClassLoader();
        
        String        helpPath = "none found";
        InputStream   istream = null;
        for( String possiblePath : pathList ) {
            final String fullPath = possiblePath + ".xml";
            log.log( Level.FINE, "Searching for help file: {0}", fullPath);
            istream = cloader.getResourceAsStream( fullPath );
            if ( null != istream ) {
                log.log( Level.FINE, "Loading help file: {0}", fullPath);
                helpPath = fullPath;
                break;
            }
        }
                
        if ( null == istream ) {
            return Optional.empty();
        }
        final XmlDataHandler saxHandler = new XmlDataHandler ( helpPath );
        try {
            final SAXParserFactory saxFactory = SAXParserFactory.newInstance();
            saxFactory.setNamespaceAware(true);
            final SAXParser saxParser = saxFactory.newSAXParser();
            
            saxParser.parse(
                    new InputSource( new InputStreamReader( istream, Whatever.UTF8 ) ),
                    saxHandler
                    );
            return Optional.ofNullable( saxHandler.getHelp () );
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ex) {
            log.log( Level.WARNING, "Failure parsing resource: " + helpPath, ex );
        }
        return Optional.empty();
    }

    @Override
    public Optional<LgoHelp> loadHelp( String s_basename ) {
        return loadHelp( s_basename, Locale.getDefault () );
    }
    
    /**
     * SAX parser handler 
     */
    private class XmlDataHandler extends DefaultHandler {
        private final static String  os_not_loaded = "none loaded";
        private String             os_full_name = os_not_loaded;
        private final List<String> ov_short_names = new ArrayList<>();
        private String             os_synopsis = os_not_loaded;
        private String             os_description = os_not_loaded;
                
        private String                 os_example_title = os_not_loaded;
        private final List<LgoExample> ov_example = new ArrayList<> ();
        private final StringBuilder   osb_buffer = new StringBuilder();
        private final String filePath;

        /**
         * @param filePath path to XML file or whatever  - for error messages
         */
        public XmlDataHandler( String filePath ) {
            this.filePath = filePath;
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
                    final String s_full_name = v_attrs.getValue("fullname");
                    if ( null != s_full_name ) {
                        os_full_name = s_full_name;                        
                    } else {
                        os_full_name = os_not_loaded;
                        log.log(Level.WARNING, "XML help file does not have fullname attribute set on root help:help element: {0}", filePath);
                    }               
                } else if ( s_simple.equals( XmlTag.example.toString() ) ) {
                    String s_title = v_attrs.getValue( "", "title" );
                    if ( null != s_title ) {
                        os_example_title = s_title;
                    } else {
                        os_example_title = os_not_loaded;
                        log.log(Level.WARNING, "XML help file does not have title attribute set on help:example element: {0}", filePath);
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
