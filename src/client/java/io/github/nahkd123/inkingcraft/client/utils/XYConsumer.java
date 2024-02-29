package io.github.nahkd123.inkingcraft.client.utils;

@FunctionalInterface
public interface XYConsumer {
	public void accept(double x, double y);

	public static XYConsumer forArray(double[] arr, int offset) {
		if (arr == null) throw new NullPointerException("array is null");
		if (arr.length < (offset + 2))
			throw new IllegalArgumentException("array length is less than " + offset + " + 2");
		return (x, y) -> {
			arr[offset] = x;
			arr[offset + 1] = y;
		};
	}
}
