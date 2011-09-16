/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.aws.test;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import littleware.asset.server.db.aws.AwsConfig;
import littleware.test.LittleTest;

/**
 * Test basic AWS SimpleDB connection, whatever
 */
public class AwsConnectTester extends LittleTest {

    private static final Logger log = Logger.getLogger(AwsConnectTester.class.getName());
    private final AmazonSimpleDB db;
    private final AwsConfig config;

    @Inject
    public AwsConnectTester(AmazonSimpleDB db, AwsConfig config) {
        setName("testAwsConnect");
        this.db = db;
        this.config = config;
    }

    
    public void testAwsConnect() {
        final String testDomain = config.getDbDomain();
        final List<String> deleteItemList = new ArrayList<String>();
        try {
            // Just make a call, and make sure an exception isn't thrown
            final Set<String> domainList = new HashSet<String>(db.listDomains().getDomainNames());
            for (String name : domainList) {
                log.log(Level.INFO, "SimpleDB domain: " + name);
            }
            Assert.assertTrue( "Domain list includes test domain: " + testDomain, domainList.contains( testDomain ) ); 
            // Ok - try to create an item with some null values, and see what the frick
            final String itemName = Long.toString((new Date()).getTime());
            this.db.putAttributes(new PutAttributesRequest(testDomain, itemName,
                    ImmutableList.of(
                    new ReplaceableAttribute("key", "value", true)
                    )));
            // Null keyes not allowed: new ReplaceableAttribute("nullKeyTest", null, true)
            deleteItemList.add(itemName);
            for( Attribute attr : db.getAttributes( new GetAttributesRequest( testDomain, itemName )).getAttributes() ) {
                log.log( Level.INFO, "Retrieved test item attr: " + attr.getName() + " -- " + attr.getValue() );
            }
            // Try to delete an attribute that does not exist
            db.deleteAttributes( new DeleteAttributesRequest( testDomain, itemName, Collections.singletonList( new Attribute( "bogus", "" ) ) ) );
            // Try to delete an item that does not exist
            db.deleteAttributes( new DeleteAttributesRequest( testDomain, "bogusItem"  ) );
        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed test", ex);
            fail("Caught exception: " + ex);
        } finally {
            for (String item : deleteItemList) {
                try {
                    db.deleteAttributes( new DeleteAttributesRequest( testDomain, item ) );
                } catch (Exception ex) {
                    log.log(Level.INFO, "Failed item cleanup", ex);
                }
            }
        }
    }
}
