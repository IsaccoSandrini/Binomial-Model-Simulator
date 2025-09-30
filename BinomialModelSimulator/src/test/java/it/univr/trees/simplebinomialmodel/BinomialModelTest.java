package it.univr.trees.simplebinomialmodel;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

import it.univr.usefulmethodsarrays.UsefulMethodsForArrays;
import net.finmath.functions.AnalyticFormulas;

/**
 * This class tests the implementation of BinomialModel
 * 
 * @author Andrea Mazzon
 * @author Alessandro Gnoatto
 * 
 * @author Isacco Sandrini
 * 
 * This design is clearly problematic: 
 * - what if the maturity of the option does not coincide with the time horizon of the binomial tree?
 * - what if we want to price two options with two different maturities or more generally a whole portfolio of claims on the asset?
 * - what about non-uniform grids in time?
 * - There is no class for the financial product, this is a bad design (but it works!). A missing layer of abstraction.
 * - I can not test what would happen if I wanted to price the same product with a different model e.g. trinomial tree. A missing layer of abstraction.
 * - There is no model for dividends
 * 
 * In summary: There is a lot of work to do to improve this.
 *
 */
public class BinomialModelTest {	
	public static void main(String[] strings) {
		double initialValue = 100;
		double strike = 90;
		double riskFreeRate = 0.04;
		double sigma = .25;
		int assetNumberOfTimes = 50;
		double assetFinalTime = 5;
		double optionMaturity = 1;

		System.out.println("Calcoliamo i prezzi di una call americana ed europea con S0="+initialValue+", strike="+strike+", r="+riskFreeRate*100.0+"% e sigma="+sigma*100.0+"%");
		DoubleUnaryOperator payoffFunction = x -> Math.max(x - strike, 0);

		BinomialModel model = new BinomialModel(initialValue, riskFreeRate, sigma, assetNumberOfTimes, assetFinalTime);

		EuropeanOption euro_call = new EuropeanOption(payoffFunction, optionMaturity , model);
		double[][] euro_prices = euro_call.discountedPrice();
		System.out.println("Prezzo call europea: " + euro_prices[0][0]);

		AmericanOption us_call = new AmericanOption(payoffFunction, optionMaturity, model);		
		double[][] us_prices = us_call.discountedPrice();
		System.out.println("Prezzo call americana: " + us_prices[0][0]);
		
		double priceBlackScholes = AnalyticFormulas.blackScholesOptionValue(initialValue, riskFreeRate, sigma, optionMaturity, strike);
		System.out.println("Il prezzo con Black-Scholes è: " + priceBlackScholes);
				
		//prezzi e maturities di zcb con tasso riskFreeRate
		double[] maturities = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
		DoubleUnaryOperator discountFunction = (x) -> Math.exp(-riskFreeRate * x);
		double[] ZCBPrices = UsefulMethodsForArrays.applyFunctionToArray(maturities	, discountFunction);
		
		BinomialModelWithTimeDependentParameters variableModel = new BinomialModelWithTimeDependentParameters(initialValue,
				sigma, assetNumberOfTimes, assetFinalTime, maturities, ZCBPrices);
		
		euro_call = new EuropeanOption(payoffFunction, optionMaturity, variableModel);
		euro_prices = euro_call.discountedPrice();
		System.out.println("Prezzo europea tassi variabili: " + euro_prices[0][0]);
		
		us_call = new AmericanOption(payoffFunction, optionMaturity, variableModel);
		us_prices = us_call.discountedPrice();
		System.out.println("Prezzo americana tassi variabili: " + us_prices[0][0]);
		
	}
}
