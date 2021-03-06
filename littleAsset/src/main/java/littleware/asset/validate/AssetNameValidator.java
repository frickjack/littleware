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

import java.util.Collection;
import java.util.Collections;
import littleware.asset.AssetBuilder;
import littleware.base.validate.AbstractValidator;
import littleware.base.validate.Validator;

/**
 * Validate whether a given name is valid or not
 */
public class AssetNameValidator {

    public boolean validate(String name) {
        return name.matches("^\\w[\\w-#:@\\.]*$");
    }

    public Validator build(final String assetName) {
        return new AbstractValidator() {

            @Override
            public Collection<String> checkIfValid() {
                if (!AssetNameValidator.this.validate(assetName)) {
                    return Collections.singletonList("asset name is invalid: " + assetName);
                }
                return Collections.emptyList();
            }
        };
    }

    public Validator build(final AssetBuilder builder) {
        return new AbstractValidator() {

            @Override
            public Collection<String> checkIfValid() {
                return build(builder.getName()).checkIfValid();
            }
        };
    }
}
