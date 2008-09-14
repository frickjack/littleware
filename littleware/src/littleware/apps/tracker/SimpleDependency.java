
package littleware.apps.tracker;

import java.security.GeneralSecurityException;
import java.rmi.RemoteException;
import java.io.StringReader;
import java.io.IOException;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import littleware.base.BaseException;
import littleware.base.ParseException;
import littleware.asset.*;
import littleware.asset.xml.*;
import littleware.apps.filebucket.*;


/**
* Simple implementation of Dependency interface.
 */
public class SimpleDependency extends SimpleAsset implements Dependency {
    
    /** Do nothing constructor */
    public SimpleDependency () {
        setAssetType ( TrackerAssetType.DEPENDENCY );
    }
        
}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

