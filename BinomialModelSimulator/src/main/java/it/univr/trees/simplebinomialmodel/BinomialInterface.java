package it.univr.trees.simplebinomialmodel;

import java.util.function.DoubleUnaryOperator;

/**
 * @author Isacco Sandrini
 */

public interface BinomialInterface {

	private void generateValues() {}
	
	private void generateValuesProbabilities() {}
	
	public double[] getValuesAtGivenTimeIndex(int timeIndex);
	
	public double[] getTransformedValuesAtGivenTimeIndex(int timeIndex, DoubleUnaryOperator transformFunction);
	
	public double[] getValuesProbabilitiesAtGivenTimeIndex(int timeIndex);
	
	public double[][] getUpAndDownProbabilities();
	
	public double[] getConditionalExpectation(double[] values, int timeIndex);
	
	public double getFinalTime();
	
	public int getNumberOfTimes();
	
	public double getRiskFreeRate(int timeIndex);
	
	public double[] discount(double[] values, int timeIndex);
	
}
