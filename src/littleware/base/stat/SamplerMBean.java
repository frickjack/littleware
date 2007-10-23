package littleware.base.stat;

import java.util.Date;

/**
 * MBean definition for read-only access to a random-variable sampler
 */
public interface SamplerMBean {
	/**
	 * Get the number of samples taken so far
	 */
	public int getNumSamples ();
	
	/** Get the value of the min sample so far */
	public float getSampleMin ();
	/** Get the max sample so far */
	public float getSampleMax ();	
	
	
	/**
	 * Get the time of the first sample
	 * after the sampler counters were cleared
	 */
	public Date getFirstSampleTime ();
	
	/**
	 * Get the time of the last sample
	 * after the sampler counters were cleared
	 */
	public Date getLastSampleTime ();
	
	/**
	 * Get the sample mean
	 *
	 * @return mean in whatever units sample was given data in
	 */
	public double getSampleMean ();
	
	/**
	 * Get the sample variance
	 *
	 * @return variance in sample-units squared
	 */
	public double getSampleVariance ();
	
	/**
	 * Get the length of the longest interrarival period in ms 
	 * starting after the first sample.
	 */
	public long getArrivalMaxMs ();
	
	/**
	 * Get the length of the shortest interrarival period in ms 
	 * starting after the first sample.
	 */
	public long getArrivalMinMs ();
	
	/**
	 * Get the interrarival mean after the first sample
	 *
	 * @return arrival mean in ms
	 */
	public double getArrivalMean ();
	
	/**
	 * Get arrival variance after the first sample
	 *
	 * @return arrival variance in ms2
	 */
	public double getArrivalVariance ();
	
	/**
	 * Get the time-weighted average value not-counting the last
	 * sample. So if we have N samples, eash sample i with value Ti
	 * arrivaing at time Ti, this function returns something like:
	 *        Sum( Vi * (Ti+1 - Ti) ) / N
	 */
	public double getTimeAverage ();
	
	/**
	 * Get the time-weighted variance of (Vi * (Ti+1 - Ti))
	 * not counting the last sample.
	 */
	public double getTimeVariance ();
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

