package it.univr.trees.simplebinomialmodel;
import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

import it.univr.usefulmethodsarrays.UsefulMethodsForArrays;
import net.finmath.functions.AnalyticFormulas;
import net.finmath.marketdata.model.curves.DiscountCurveInterpolation;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.CurveInterpolation;
import net.finmath.marketdata.model.curves.CurveInterpolation.ExtrapolationMethod;
import net.finmath.marketdata.model.curves.CurveInterpolation.InterpolationEntity;
import net.finmath.marketdata.model.curves.CurveInterpolation.InterpolationMethod;

/**
 * @author Isacco Sandrini
 */

public class BinomialModelWithTimeDependentParameters implements BinomialInterface{
	//caratteristiche dell'asset
	private double initialValue;
	private double sigma;

	//internal storage
	private double upFactor;
	private double downFactor;
	private double [] riskNeutralProbabilityUp;
	private double [] riskNeutralProbabilityDown;

	//discretizzazione del tempo
	private int numberOfTimes;
	private double finalTime;
	private double[][] valuesProbabilities;
	private double[][] values;
	private double [] riskFreeRates;
	private double[] timeInstants;
	public BinomialModelWithTimeDependentParameters(double initialValue, double sigma, int numberOfTimes,double finalTime,
			double[] maturities, double[] ZCBPrices) {
		//remember the use of this in order to solve the conflict between name of input variables and of fields
		this.initialValue = initialValue;
		this.finalTime = finalTime;
		this.sigma = sigma;
		this.upFactor = Math.exp(this.sigma*Math.sqrt(finalTime/(double)(numberOfTimes)));
		this.downFactor = 1.0 / this.upFactor;
		this.numberOfTimes = numberOfTimes;

		DiscountCurveInterpolation myDiscountCurveFromDiscountFactor =
				DiscountCurveInterpolation.createDiscountCurveFromDiscountFactors(
						"discountCurveFromDiscountFactors" ,
						maturities /* maturities */,
						ZCBPrices /* ZCBs prices */,
						InterpolationMethod.LINEAR,
						ExtrapolationMethod.CONSTANT,
						InterpolationEntity.LOG_OF_VALUE);

		timeInstants=getTimeInstants(numberOfTimes,finalTime);
		
		riskFreeRates = myDiscountCurveFromDiscountFactor.getZeroRates(timeInstants);
		riskNeutralProbabilityUp = new double[numberOfTimes];
		riskNeutralProbabilityDown = new double[numberOfTimes];
		for(int i=0;i<numberOfTimes  ;i++) {
			riskNeutralProbabilityUp[i] = (Math.exp(riskFreeRates[i] * finalTime/(double)(numberOfTimes))- downFactor)
					/ (upFactor - downFactor);
			riskNeutralProbabilityDown[i]= 1.0-riskNeutralProbabilityUp[i];
		}

	}

	public double[] getTimeInstants(int numberOfTimes,double finalTime) {
		double [] timeInstants = new double [numberOfTimes+1];
		double delta = finalTime/((double)(numberOfTimes));
		for( int i=0;i<=numberOfTimes;i++)
		{
			timeInstants[i] = delta*i;
		}
		return timeInstants;
	}

	private void generateValues() {
		values = new double[numberOfTimes][numberOfTimes];
		values[0][0] = initialValue;
		int numberOfDowns;//it will be updated in the for loop
		for (int numberOfMovements = 1; numberOfMovements < numberOfTimes; numberOfMovements++) {
			for (int numberOfUps = 0; numberOfUps <= numberOfMovements; numberOfUps++) {
				numberOfDowns=numberOfMovements-numberOfUps;
				/*
				 * Value of the binomial model when it went numberOfUps times up and numberOfDowns times down.
				 * Note that this is stored in position numberOfDowns! So the first position has all ups and so on
				 */
				values[numberOfMovements][numberOfDowns] = values[0][0] * Math.pow(upFactor, numberOfUps)*
						Math.pow(downFactor, numberOfDowns);
			}
		}
	}

	private void generateValuesProbabilities() {
		valuesProbabilities = new double[numberOfTimes][numberOfTimes];
		valuesProbabilities[0][0]=1;//the first value is deterministic
		int numberOfDowns;//it will be updated in the for loop
		for (int numberOfMovements = 1; numberOfMovements < numberOfTimes; numberOfMovements++) {
			/*
			 * Here we have to take care of the computation of the binomial coefficients. 
			 * We are at time n and start the "internal" for loop with the case when we have k=0 ups.
			 * So we first have binomialCoefficient(n,k)=n!/(k!(n-k)!)=1 
			 */
			double binomialCoefficient = 1;
			for (int numberOfUps = 0; numberOfUps <= numberOfMovements; numberOfUps++) {
				numberOfDowns=numberOfMovements-numberOfUps;
				/*
				 * Probability of having B(0)u^numberOfUps*d^numberOfDowns.
				 * Note that this is stored in position numberOfDowns! So the first position has all ups and so on
				 */
				valuesProbabilities[numberOfMovements][numberOfDowns]
						= binomialCoefficient*Math.pow(riskNeutralProbabilityUp[numberOfTimes], numberOfUps)
						* Math.pow(riskNeutralProbabilityDown[numberOfTimes], numberOfDowns);
				/*
				 * Here we update the value of the binomial coeffeicient computing the one
				 * that we will use next, i.e., when we will have one more up: so, if k is
				 * the actual number of ups, we have to compute 
				 * binomialCoefficient(n,k+1)=n!/((k+1)!(n-k-1)!)=n!/(k!(n-k)!)*(n-k)/(k+1).
				 * Since n!/(k!(n-k)!) is the last computed value, we multiply by (n-k) 
				 * (so, by numberOfDowns) and divide by k+1, so, by the current number of ups plus 1.
				 */
				binomialCoefficient=binomialCoefficient * (numberOfDowns)/(numberOfUps+1);
			}
		}
	}

	public double[] getValuesAtGivenTimeIndex(int timeIndex) {
		/*
		 * Pay attention: the method generateValues() initializes the array values and sets it. This is
		 * of course needed if we want to get those values. However, we want to do that only once!
		 * So we check if values is null (this means "not yet initialized") and call the method only
		 * in this case. 
		 */
		if (values == null) {
			generateValues();
		}	
		/*
		 * We only return the first timeIndex entries! The others are zero, because the process can take
		 * only timeIndex values at time index timeIndex.
		 */
		return Arrays.copyOfRange(values[timeIndex], 0, timeIndex+1);
	}

	public double[] getTransformedValuesAtGivenTimeIndex(int timeIndex, DoubleUnaryOperator transformFunction) {
		//the possible values of the binomial model
		double[] valuesAtGivenTimeIndex = getValuesAtGivenTimeIndex(timeIndex);
		//we return the function applied to this array
		return UsefulMethodsForArrays.applyFunctionToArray(valuesAtGivenTimeIndex, transformFunction);
	}

	public double[] getValuesProbabilitiesAtGivenTimeIndex(int timeIndex) {
		/*
		 * Pay attention: the method generateValues() initializes the array valuesProbabilities and sets it.
		 * This is of course needed if we want to get those values. However, we want to do that only once!
		 * So we check if valuesProbabilities is null (this means "not yet initialized") and call the method only
		 * in this case. 
		 */
		if (valuesProbabilities == null) {
			generateValuesProbabilities();
		}
		/*
		 * We only return the first timeIndex entries! The others are zero, because the process can take
		 * only timeIndex values at time index timeIndex.
		 */
		return Arrays.copyOfRange(valuesProbabilities[timeIndex], 0, timeIndex + 1);
	}

	public double[][] getUpAndDownProbabilities() {
		double[][] probabilities = new double [numberOfTimes][2];
		for(int i=0;i<numberOfTimes;i++) {
			probabilities[i][0]=riskNeutralProbabilityUp[i];
			probabilities[i][1]=riskNeutralProbabilityDown[i];
		}
		return probabilities;
	}

	public double[] getConditionalExpectation(double[] values, int timeIndex) {
		//at timeIndex we have timeIndex + 1 values
		double[] conditionalExpectation = new double[timeIndex+1];
		for (int i = 0; i <= timeIndex; i++) {
			/*
			 * computation of the conditional probability at the state with i down. Note that the i-th element
			 * of binomialValues has gone up, because the number of down is still i. 
			 */
			conditionalExpectation[i] = (values[i]*riskNeutralProbabilityUp[timeIndex] + values[i + 1]*riskNeutralProbabilityDown[timeIndex]);
		}
		return conditionalExpectation;
	}

	public double getFinalTime() {
		return this.finalTime;
	}

	@Override
	public int getNumberOfTimes() {
		// TODO Auto-generated method stub
		return numberOfTimes;
	}

	@Override
	public double getRiskFreeRate(int timeIndex) {
		// TODO Auto-generated method stub
		return riskFreeRates[timeIndex];
	}

	@Override
	public double[] discount(double[] values, int timeIndex) {
		// TODO Auto-generated method stub
		double[] discountedValues = new double[values.length];
		for(int i=0; i < values.length; i++) {
			discountedValues[i] = values[i]/Math.exp((double)(timeIndex)*finalTime*riskFreeRates[timeIndex]/(double)(numberOfTimes));
		}
		return discountedValues;
	}
}
