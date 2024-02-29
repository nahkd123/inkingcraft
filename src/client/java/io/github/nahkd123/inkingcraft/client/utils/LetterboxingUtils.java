package io.github.nahkd123.inkingcraft.client.utils;

public class LetterboxingUtils {
	public static void apply(double childRatio, double parentWidth, double parentHeight, XYConsumer childSizeConsumer) {
		double parentRatio = parentWidth / parentHeight;
		if (parentRatio > childRatio) childSizeConsumer.accept(parentHeight * childRatio, parentHeight);
		else childSizeConsumer.accept(parentWidth, parentWidth / childRatio);
	}

	public static double[] apply(double childRatio, double parentWidth, double parentHeight) {
		double[] xy = new double[2];
		apply(childRatio, parentWidth, parentHeight, XYConsumer.forArray(xy, 0));
		return xy;
	}
}
