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

import java.util.Date;


/**
 * Simple implementation of the sampler interface.
 * Does not try to guard against overflow in accumulators.
 * Assumes we're taking samples of an independent random variable.
 */
public class SimpleSampler implements Sampler, java.io.Serializable {
    private static final long serialVersionUID = 7088383371790722679L;
	private Date   ot_start       =null;
	private Date   ot_last_sample = null;
	private float  of_last_sample = 0.0F;
	
	private float  of_max = 0.0F;
	private float  of_min = 0.0F;
	
	private long   ol_arrival_max_ms = 0;
	private long   ol_arrival_min_ms = 0;
	
	private double od_accumulator = 0;
	private double od_accumulator2 = 0;  // variance accumulator
	
	private double od_time_accumulator = 0;  // time-weighted
	private double od_time_accumulator2 = 0;
	
	private long ol_arrival_accumulator = 0;
	private long ol_arrival_accumulator2 = 0;
	
	private  int   oi_sample_count = 0;
	
	/** Constructor just clears internal counters */
	public SimpleSampler () {
		clear ();
	}
	
	public float getSampleMin () { return of_min; }
	public float getSampleMax () { return of_max; }
	
	

	public synchronized void sample ( float f_sample ) {
		++oi_sample_count;
		od_accumulator += f_sample;
		od_accumulator2 += f_sample * f_sample;
		
		Date t_now = new Date ();

		if ( null != ot_last_sample ) {
			long l_arrival_delay = t_now.getTime () - ot_last_sample.getTime ();
			ol_arrival_accumulator += l_arrival_delay;
			ol_arrival_accumulator2 += (l_arrival_delay * l_arrival_delay);

			od_time_accumulator += l_arrival_delay * of_last_sample;
			od_time_accumulator2 += (l_arrival_delay * of_last_sample * l_arrival_delay * of_last_sample);
			
			if ( 2 == oi_sample_count ) { // 1st measure
				ol_arrival_max_ms = l_arrival_delay;
				ol_arrival_min_ms = l_arrival_delay;
			} else if ( l_arrival_delay > ol_arrival_max_ms ) {
				ol_arrival_max_ms = l_arrival_delay;
			} else if ( l_arrival_delay < ol_arrival_min_ms ) {
				ol_arrival_min_ms = l_arrival_delay;
			}
		} else {
			ot_start = t_now;
		}
		of_last_sample = f_sample;
		ot_last_sample = t_now;
		
		if ( f_sample > of_max ) {
			of_max = f_sample;
		} 
		if ( f_sample < of_min ) {
			of_min = f_sample;
		}
	}
	

	public synchronized void clear () {
		ot_start =null;
		ot_last_sample = null;
		of_last_sample = 0.0F;
		of_max = 0.0F;
		of_min = 0.0F;
		od_accumulator = 0;
		od_accumulator2 = 0;
		od_time_accumulator = 0;
		od_time_accumulator2 = 0;
		oi_sample_count = 0;
		ol_arrival_max_ms = 0;
		ol_arrival_min_ms = 0;
	}


	public int getNumSamples () {
		return oi_sample_count;
	}
	

	public Date getFirstSampleTime () {
		return ot_start;
	}
	

	public Date getLastSampleTime () {
		return ot_last_sample;
	}
	

	public synchronized double getSampleMean () {
		if ( oi_sample_count < 1 ) {
			return 0.0;
		}
		return od_accumulator / oi_sample_count;
	}
	

	public synchronized double getSampleVariance () {
		if ( oi_sample_count < 1 ) {
			return 0.0;
		}
		double d_mean = getSampleMean ();
		return (od_accumulator2/oi_sample_count) - (d_mean*d_mean);
	}
	

	public synchronized double getArrivalMean () {
		if ( oi_sample_count < 2 ) {
			return 0.0;
		}
		return ((double) ol_arrival_accumulator) / (oi_sample_count - 1);
	}
	
	public synchronized double getArrivalVariance () {
		if ( oi_sample_count < 2 ) {
			return 0.0;
		}
		double d_mean = getArrivalMean ();
		return ( ((double) ol_arrival_accumulator2) / (oi_sample_count - 1)) - (d_mean * d_mean);
	}
		
	
	public synchronized double getTimeAverage () {
		if ( oi_sample_count < 2 ) {
			return 0.0;
		}
		return od_time_accumulator / (oi_sample_count - 1);
	}
	

	public synchronized double getTimeVariance () {
		if ( oi_sample_count < 2 ) {
			return 0.0;
		}
		double d_mean = getTimeAverage ();
		return (od_time_accumulator2 / (oi_sample_count - 1)) - (d_mean * d_mean);
	}
	
	public float getMin () { return of_min; }
	public float getMax () { return of_max; }
	
	public long getArrivalMaxMs () { return ol_arrival_max_ms; }
	public long getArrivalMinMs () { return ol_arrival_min_ms; }

}
