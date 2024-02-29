package io.github.nahkd123.inkingcraft.client.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.nahkd123.inking.api.tablet.TabletSpec;

public class PressureMapping {
	private List<PressureMappingPoint> points = new ArrayList<>();
	private boolean clamp;

	public PressureMapping(List<PressureMappingPoint> points, boolean clamp) {
		this.clamp = clamp;
		this.points.addAll(points);
		this.points.sort((a, b) -> a.sourcePressure() - b.sourcePressure());
	}

	public List<PressureMappingPoint> getPoints() { return Collections.unmodifiableList(points); }

	/**
	 * <p>
	 * Check if the output pressure value should be clamped to
	 * {@link TabletSpec#getMaxPressure()}.
	 * </p>
	 * 
	 * @return Clamp state.
	 */
	public boolean isClamp() { return clamp; }

	public void setClamp(boolean clamp) { this.clamp = clamp; }

	public void add(PressureMappingPoint point) {
		int search = Collections.binarySearch(points, point, (a, b) -> a.sourcePressure() - b.sourcePressure());

		if (search >= 0) {
			points.set(search, point);
		} else {
			int insertAt = -(search + 1);
			points.add(insertAt, point);
		}
	}

	public void remove(PressureMappingPoint point) {
		int idx = points.indexOf(point);
		if (idx >= 0) points.remove(idx);
	}

	public void replace(PressureMappingPoint oldPoint, PressureMappingPoint newPoint) {
		remove(oldPoint);
		add(newPoint);
	}

	public int map(int raw) {
		if (points.size() == 0) return raw;
		if (points.size() == 1) return PressureMappingPoint.interpolate(
			raw,
			new PressureMappingPoint(0, 0),
			points.get(0));

		int search = Collections.binarySearch( // TODO reduce allocation by reimplementing binary search
			points,
			new PressureMappingPoint(raw, raw),
			(a, b) -> a.sourcePressure() - b.sourcePressure());
		if (search >= 0) return points.get(search).targetPressure();

		int after = -(search + 1);
		int before = after - 1;

		PressureMappingPoint first = before >= 0
			? points.get(before)
			: new PressureMappingPoint(0, 0);
		PressureMappingPoint last = after < points.size()
			? points.get(after)
			: new PressureMappingPoint(raw, first.targetPressure());
		return PressureMappingPoint.interpolate(raw, first, last);
	}

	public static final Codec<PressureMapping> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		PressureMappingPoint.CODEC.listOf().fieldOf("points").forGetter(PressureMapping::getPoints),
		Codec.BOOL.fieldOf("clamping").forGetter(PressureMapping::isClamp))
		.apply(instance, PressureMapping::new));

	public static PressureMapping createDefault(TabletSpec spec) {
		return new PressureMapping(Arrays.asList(
			new PressureMappingPoint(0, 0),
			new PressureMappingPoint(spec.getMaxPressure(), spec.getMaxPressure())), false);
	}
}
