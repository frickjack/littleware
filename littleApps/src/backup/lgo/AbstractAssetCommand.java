/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.lgo;

import littleware.lgo.AbstractLgoCommand;
import littleware.lgo.LgoException;
import java.io.StringWriter;
import littleware.asset.Asset;
import littleware.asset.pickle.HumanPicklerProvider;
import littleware.base.feedback.Feedback;

/**
 * Extends AbstractLgoCommand with a runCommandLine implementation
 * that runs an asset result through a HumanPickler
 */
public abstract class AbstractAssetCommand<Tin,Tout extends Asset>
        extends AbstractLgoCommand<Tin,Tout>
{
    private final HumanPicklerProvider providePickler;

    protected AbstractAssetCommand( String sName, HumanPicklerProvider providePickler, Tin input ) {
        super(sName, input);
        this.providePickler = providePickler;
    }

    /**
     * Just run the result of runDynamic(feedback,sIn) through pickler
     */
    @Override
    public String runCommandLine( Feedback feedback ) throws Exception {
        try {
            final StringWriter writer = new StringWriter();
            providePickler.get().pickle( runCommand(feedback ), writer);
            return writer.toString();
        } catch ( LgoException ex ) {
            throw ex;
        } catch( Exception ex ) {
            throw new LgoException( "Failed result pickle", ex );
        }
    }

}
