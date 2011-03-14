/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base.validate;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate several validators
 */
public class CompoundValidator extends AbstractValidator implements Validator {
    private final ImmutableList<Validator> validatorSet;

    protected CompoundValidator( Validator ... validators ) {
        validatorSet = ImmutableList.copyOf( Arrays.asList( validators ) );
    }

    protected CompoundValidator( Iterable<? extends Validator> validators ) {
        validatorSet = ImmutableList.copyOf( validators );
    }

    @Override
    public final Collection<String> checkIfValid() {
        for( Validator check : validatorSet ) {
            final Collection<String> errors = check.checkIfValid();
            if( ! errors.isEmpty() ) {
                return errors;
            }
        }
        return Collections.emptyList();
    }


    public static Validator build( Validator ... validators ) {
        return new CompoundValidator( validators );
    }

    public static Validator build( Iterable<? extends Validator> validators ) {
        return new CompoundValidator( validators );
    }

}
