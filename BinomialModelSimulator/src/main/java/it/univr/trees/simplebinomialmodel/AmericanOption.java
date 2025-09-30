package it.univr.trees.simplebinomialmodel;

import java.util.function.DoubleUnaryOperator;

import it.univr.usefulmethodsarrays.UsefulMethodsForArrays;

/**
 *  @author Isacco Sandrini
 */

public class AmericanOption implements Option{
	private double maturity; // Indice temporale della maturità nell'albero binomiale
	private DoubleUnaryOperator payoffFunction;
	private int numberOfTimes;
	private BinomialInterface model;

	public AmericanOption(DoubleUnaryOperator payoffFunction, double maturity, BinomialInterface model) {
		double deltaTime = model.getFinalTime()/(double)(model.getNumberOfTimes());		
		numberOfTimes = (int)(maturity/deltaTime);
		this.maturity = maturity;
		if (maturity > model.getFinalTime()) {
			throw new IllegalArgumentException("ERRORE la maturità dell'opzione è maggiore dell'orizzonte temporeale dell'asset");
		}
		this.payoffFunction = payoffFunction;
		this.model = model;
	}

	// Questo metodo calcola l'albero dei prezzi non scontati dell'opzione, ovvero l'inviluppo di Sneil non scontato
	public double[][] price() {
		double[][] prices = new double [numberOfTimes][numberOfTimes];
		double [] expectedPrice;
		double [] payoff;
		
		for (int col=numberOfTimes-1; col >=0; col--) {			
			expectedPrice = new double[col+1];
			payoff = new double[col+1];			
			if(col==numberOfTimes-1) {
				prices[col]= model.getTransformedValuesAtGivenTimeIndex(col, payoffFunction);
			}
			else {
				expectedPrice = model.getConditionalExpectation(prices[col+1], col);
				payoff = model.getTransformedValuesAtGivenTimeIndex(col, payoffFunction);				
				prices[col] = UsefulMethodsForArrays.getMaxValuesBetweenTwoArrays(expectedPrice, payoff);				
			}
		}
		return prices;
	}

	// Questo metodo calcola l'albero dei prezzi scontati dell'opzione, ovvero l'inviluppo di Sneil scontato
	public double[][] discountedPrice() {
		double[][] prices = this.price();
		
		for (int col=0; col < prices.length; col++) {
			prices[col] = model.discount(prices[col], col);
		}		
		return prices;
	}
}