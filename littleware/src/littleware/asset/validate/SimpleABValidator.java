/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.validate;

import com.google.inject.Inject;
import littleware.asset.AssetBuilder;
import littleware.asset.AssetType;
import littleware.base.AbstractValidator;
import littleware.base.CompoundValidator;
import littleware.base.Validator;

public class SimpleABValidator implements AssetBuilderValidator {
    private final AssetNameValidator nameValidator;
    private final AssetDataValidator dataValidator;
    private final AssetAttrValidator attrValidator;

    @Inject
    public SimpleABValidator( AssetNameValidator nameValidator,
            AssetDataValidator dataValidator,
            AssetAttrValidator attrValidator
            ) {
        this.nameValidator = nameValidator;
        this.dataValidator = dataValidator;
        this.attrValidator = attrValidator;
    }

    /**
     * Self injecting constructor
     */
    public SimpleABValidator() {
        this( new AssetNameValidator(), new AssetDataValidator(),
                new AssetAttrValidator()
                );
    }


    @Override
    public Validator build( final AssetBuilder builder ) {
        return new CompoundValidator(
                nameValidator.build(builder),
                dataValidator.build( builder ),
                attrValidator.build(builder),
                new AbstractValidator(){

            @Override
            public void validate() {
                assume( (null != builder.getId())
                        && (null != builder.getHomeId())
                        && (
                            AssetType.HOME.equals( builder.getAssetType() )
                            ^ (null != builder.getFromId())
                            )
                        && (
                            AssetType.HOME.equals( builder.getAssetType() )
                            ^ (! builder.getId().equals( builder.getHomeId() ))
                            ),
                            "Home-id, From-id check passed"
                            );
            }
        }
        ) {};
    }

}


