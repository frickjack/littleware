/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.aws.test;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.google.inject.Inject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.test.LittleTest;

/**
 * Test basic AWS SimpleDB connection, whatever
 */
public class AwsConnectTester extends LittleTest {
    private static final Logger log = Logger.getLogger( AwsConnectTester.class.getName() );
    private final AmazonSimpleDB db;
    
    @Inject
    public AwsConnectTester( AmazonSimpleDB db ) {
        setName( "testAwsConnect" );
        this.db = db;
    }
    
    public void testAwsConnect() {
        // Just make a call, and make sure an exception isn't thrown
        final List<String> domainList = db.listDomains().getDomainNames();
        for ( String name: domainList ) {
            log.log( Level.INFO, "SimpleDB domain: " + name );
        }
    }
    
}
