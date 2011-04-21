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
import java.util.Collection;
import littleware.asset.AssetBuilder;
import littleware.asset.LittleHome;
import littleware.asset.spi.AbstractAssetBuilder;
import littleware.base.validate.AbstractValidator;
import littleware.base.validate.CompoundValidator;
import littleware.base.validate.Validator;

public class SimpleABValidator implements AssetBuilderValidator {

    private final AssetNameValidator nameValidator;
    private final AssetDataValidator dataValidator;
    private final AssetAttrValidator attrValidator;

    @Inject
    public SimpleABValidator(AssetNameValidator nameValidator,
            AssetDataValidator dataValidator,
            AssetAttrValidator attrValidator) {
        this.nameValidator = nameValidator;
        this.dataValidator = dataValidator;
        this.attrValidator = attrValidator;
    }

    /**
     * Self injecting constructor
     */
    public SimpleABValidator() {
        this(new AssetNameValidator(), new AssetDataValidator(),
                new AssetAttrValidator());
    }

    @Override
    public Validator build(final AssetBuilder builderIn ) {
        final AbstractAssetBuilder builder = (AbstractAssetBuilder) builderIn;
        return new CompoundValidator(
                nameValidator.build(builderIn),
                dataValidator.build(builderIn),
                attrValidator.build(builderIn),
                new AbstractValidator() {

                    @Override
                    public Collection<String> checkIfValid() {
                        return buildErrorTracker().check((null != builder.getId()), "id must be set").check((null != builder.getHomeId()), "homeId must be set").check((LittleHome.HOME_TYPE.equals(builder.getAssetType())
                                ^ (null != builder.getFromId())), "exactly one of home-type asset and non-null fromId must be true").check((LittleHome.HOME_TYPE.equals(builder.getAssetType())
                                ^ (!builder.getId().equals(builder.getHomeId()))), "homeId == id for home-type asset, otherwise homeId != id").getErrors();
                    }
                }) {
        };
    }
}
