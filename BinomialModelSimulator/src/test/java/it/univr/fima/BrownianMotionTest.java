package it.univr.fima;


import net.finmath.montecarlo.*;
import net.finmath.plots.*;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.*;

/**
 *  @author Isacco Sandrini
 */

public class BrownianMotionTest {

	public static void main(String[] args) {
		
		TimeDiscretizationFromArray td = new TimeDiscretizationFromArray(0.0, 100, 0.1);
		
		
		System.out.println(td.toString());
		
		
		 
		var bm = new BrownianMotionFromMersenneRandomNumbers(td, 1, 10000, 3213);
		
		
		RandomVariable x = bm.getBrownianIncrement(1,0);
		
		System.out.println(x);
					
		
		var plot = Plots.createPlotOfHistogram(x, 100, 5.0);
		plot.setTitle("Histogram").setXAxisLabel("value").setYAxisLabel("frequency");
		plot.show();				

	}

}
