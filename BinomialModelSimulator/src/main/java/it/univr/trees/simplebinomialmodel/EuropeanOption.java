package it.univr.trees.simplebinomialmodel;
import it.univr.usefulmethodsarrays.UsefulMethodsForArrays;
import java.util.function.DoubleUnaryOperator; 

/**
 * @author Isacco Sandrini
 */

public class EuropeanOption implements Option{
	private  double maturity; 	
	private  DoubleUnaryOperator payoffFunction;
	private int numberOfTimes;
	private BinomialInterface model;

	public EuropeanOption(DoubleUnaryOperator payoffFunction, double maturity, BinomialInterface model) {
		double deltaTime = model.getFinalTime()/(double)(model.getNumberOfTimes());		
		numberOfTimes = (int)(maturity/deltaTime);
		this.maturity = maturity;		
		if (this.maturity > model.getFinalTime()) {	    		
			throw new IllegalArgumentException("ERRORE la maturità dell'opzione è maggiore dell'orizzonte temporeale dell'asset");    
		}
		this.payoffFunction = payoffFunction;
		this.model = model;
	}
	
	public double[][] price() {
		double[][] prices = new double [numberOfTimes][numberOfTimes];

		for (int col=numberOfTimes-1; col>=0; col--) {			
			if(col==numberOfTimes-1) {
				prices[col]= model.getTransformedValuesAtGivenTimeIndex(col, payoffFunction);
			}
			else {
				prices[col] = model.getConditionalExpectation(prices[col+1], col);
			}			
		}
		return prices;
	}

	public double[][] discountedPrice() {
		double[][] prices = this.price();
		
		for (int col=0; col < prices.length; col++) {
			prices[col] = model.discount(prices[col], col);
		}		
		return prices;
	}
}