/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.pickle;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.base.BaseException;
import littleware.base.Whatever;
import littleware.security.LittleGroup;
import littleware.security.SecurityAssetType;

/**
 * Simple human-readable text rep of an asset.
 */
public class SimpleHumanPickler implements AssetHumanPickler {

    private final boolean obShowData;

    /**
     * Default constructor configures pickler to show data property values
     */
    public SimpleHumanPickler() {
        obShowData = true;
    }

    /**
     * @param bShowData set true if pickle() should show the asset Data property value
     */
    public SimpleHumanPickler(boolean bShowData) {
        obShowData = bShowData;
    }

    /** @throws UnsupportedOperationException */
    @Override
    public Asset unpickle(Reader reader) throws AssetException, BaseException, GeneralSecurityException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private final DateFormat oformatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    /**
     * Assumes its called at the beginning of a new line
     * 
     * @param sb builder to append to
     * @param sName property name - alphanumeric
     * @param xValue checks for null, handles String and formats Date, otherwise just xValue.toString
     */
    protected void appendProperty(StringBuilder sb, String sName, Object xValue) {
        sb.append("    property: ").
                append(sName).append(": ");
        if (null != xValue) {
            if (xValue instanceof String) {
                sb.append((String) xValue);
            } else if ( xValue instanceof Date ) {
                sb.append( oformatDate.format( (Date) xValue ) );
            } else {
                sb.append(xValue.toString());
            }
        }
        sb.append(Whatever.NEWLINE);
    }

    /**
     * Allow subtypes to add custom properties within normal Asset pickle string.
     * Default implementation just does
     *   if ( obShowData ) { appendProperty( sb, "data", x.getData() ); }
     *
     * @param sb
     * @param x asset being pickled
     */
    protected void pickleData( StringBuilder sb, Asset aIn ) {
        if (obShowData) {
            appendProperty(sb, "data", aIn.getData());
        }
    }


    /** @TODO internationalize this with a ResourceBundle */
    @Override
    public void pickle(Asset aIn, Writer writer) throws AssetException, BaseException, GeneralSecurityException, IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(Whatever.NEWLINE).append("AssetBegin: ").append(Whatever.NEWLINE);
        appendProperty(sb, "name", aIn.getName());
        appendProperty(sb, "type", aIn.getAssetType());
        appendProperty(sb, "id", aIn.getId());
        appendProperty(sb, "home", aIn.getHomeId());
        appendProperty(sb, "acl", aIn.getAclId());
        appendProperty(sb, "from", aIn.getFromId());
        appendProperty(sb, "to", aIn.getToId());
        appendProperty(sb, "owner", aIn.getOwnerId());
        appendProperty(sb, "creator", aIn.getCreatorId());
        appendProperty(sb, "createDate", aIn.getCreateDate());
        appendProperty(sb, "updater", aIn.getLastUpdaterId());
        appendProperty(sb, "updateDate", aIn.getLastUpdateDate());
        appendProperty(sb, "startDate", aIn.getStartDate());
        appendProperty(sb, "endDate", aIn.getEndDate());
        appendProperty(sb, "transaction", Long.toString(aIn.getTransaction()));
        appendProperty( sb, "comment", aIn.getComment() );
        appendProperty( sb, "value", aIn.getValue() );
        // Go ahead and add some special handling of GROUP type assets here.
        // Can move out to separate registered handler later if we want
        if ( aIn.getAssetType().isA( SecurityAssetType.GROUP ) ) {
            final LittleGroup group = aIn.narrow( LittleGroup.class );
            for( Enumeration<? extends Principal> member = group.members();
                member.hasMoreElements();
            ) {
                appendProperty( sb, "groupmember", member.nextElement().getName() );
            }
        }
        pickleData( sb, aIn );
        sb.append( "AssetEnd:" ).append( Whatever.NEWLINE );
        writer.write( sb.toString() );
    }
}
