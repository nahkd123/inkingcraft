package io.github.nahkd123.inkingcraft.client.config;

import java.util.Arrays;

import com.mojang.serialization.Codec;

public record PressureMappingPoint(int sourcePressure, int targetPressure) {
	public static final Codec<PressureMappingPoint> CODEC = Codec.INT.listOf().xmap(
		list -> new PressureMappingPoint(list.get(0), list.get(1)),
		p -> Arrays.asList(p.sourcePressure, p.targetPressure));

	/**
	 * <p>
	 * Interpolate between 2 pressure mapping points.
	 * </p>
	 * 
	 * @param source The raw source value that sits between
	 *               {@link #sourcePressure()} of {@code from} and {@code to}.
	 * @param from   The mapping point whose source pressure is less than or equals
	 *               to source from argument.
	 * @param to     The mapping point whose source pressure is greater than or
	 *               equals to source from argument.
	 * @return Linear interpolated raw pressure.
	 */
	public static int interpolate(int source, PressureMappingPoint from, PressureMappingPoint to) {
		if (source <= from.sourcePressure) return from.targetPressure;
		if (source >= to.sourcePressure) return to.targetPressure;

		int sourceCurrent = source - from.sourcePressure;
		int sourceMax = to.sourcePressure - from.sourcePressure;
		int targetMax = to.targetPressure - from.targetPressure;
		return from.targetPressure + (sourceCurrent * targetMax / sourceMax);
	}
}
