package littleware.base.stat;

import java.util.Date;

/**
 * Little random variable sampler interface
 * extends MBean interface withs etters.
 */
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

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

