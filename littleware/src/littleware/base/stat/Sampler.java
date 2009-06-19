/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base.stat;

import com.google.inject.ImplementedBy;

/**
 * Little random variable sampler interface
 * extends MBean interface withs etters.
 */
@ImplementedBy(SimpleSampler.class)
public interface Sampler extends SamplerMBean {
	/**
	 * Add a variable sample to the data
	 */
	public void sample ( float f_sample );
	
	/**
	 * Clear the internal counters
	 */
	public void clear ();
}

