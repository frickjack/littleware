/*
 * Copyright 2009,2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.validate;

import java.util.Collection;
import java.util.Collections;
import littleware.asset.AssetBuilder;
import littleware.base.validate.AbstractValidator;
import littleware.base.validate.Validator;

/**
 * Validate an AssetBuilder's Data property
 */
public class AssetDataValidator {
    public boolean validate( String data ) {
        return data.length() < 1024;
    }

    public Validator build( final String data ) {
        return new AbstractValidator(){
            @Override
            public Collection<String> checkIfValid() {
                if( ! AssetDataValidator.this.validate( data ) ) {
                    return Collections.singletonList( "Asset data < 1024 characters" );
                } else {
                    return Collections.emptyList();
                }
            }
        };
    }

    /**
     * Convenience method validates against builder.getData
     */
    public Validator build( final AssetBuilder builder ) {
        return new AbstractValidator(){
            @Override
            public Collection<String> checkIfValid() {
                return build( builder.getData() ).checkIfValid();
            }
        };
    }

}
