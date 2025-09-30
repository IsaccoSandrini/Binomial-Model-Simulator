package it.univr.trees.simplebinomialmodel;

/**
 * @author Isacco Sandrini
 */

public interface Option {
	public double[][] discountedPrice();
	public double[][] price();
}
