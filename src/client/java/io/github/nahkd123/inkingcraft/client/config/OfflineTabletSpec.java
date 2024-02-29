package io.github.nahkd123.inkingcraft.client.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.nahkd123.inking.api.tablet.ButtonType;
import io.github.nahkd123.inking.api.tablet.TabletSpec;
import io.github.nahkd123.inking.api.util.ConstantVector2;
import io.github.nahkd123.inking.api.util.Vector2;
import io.github.nahkd123.inkingcraft.client.utils.InkingCodecs;

/**
 * <p>
 * A snapshot of {@link TabletSpec}. InkingCraft holds a snapshot of tablet
 * specifications so that the tablet can still be configured even if the tablet
 * is disconnected.
 * </p>
 */
public record OfflineTabletSpec(String tabletId, String displayName, int maxPressure, ConstantVector2 physicalSize, ConstantVector2 inputSize, int penButtons, int auxButtons) implements TabletSpec {
	/**
	 * <p>
	 * Take a specifications snapshot from another {@link TabletSpec}.
	 * </p>
	 * 
	 * @param tabletId The unique ID of the tablet.
	 * @param copyFrom The {@link TabletSpec} to copy.
	 */
	public OfflineTabletSpec(String tabletId, TabletSpec copyFrom) {
		// @formatter:off
		this(
			tabletId,
			copyFrom.getTabletName(),
			copyFrom.getMaxPressure(),
			new ConstantVector2(copyFrom.getPhysicalSize().x(), copyFrom.getPhysicalSize().y()),
			new ConstantVector2(copyFrom.getInputSize().x(), copyFrom.getInputSize().y()),
			copyFrom.getButtonsCount(ButtonType.PEN),
			copyFrom.getButtonsCount(ButtonType.AUXILIARY));
		// @formatter:on
	}

	@Override
	public String getTabletName() { return displayName; }

	@Override
	public int getMaxPressure() { return maxPressure; }

	@Override
	public Vector2 getPhysicalSize() { return physicalSize; }

	@Override
	public Vector2 getInputSize() { return inputSize; }

	@Override
	public int getButtonsCount(ButtonType type) {
		return switch (type) {
		case PEN -> penButtons;
		case AUXILIARY -> auxButtons;
		case null, default -> 0;
		};
	}

	public static final Codec<OfflineTabletSpec> CODEC = RecordCodecBuilder
		.create(instance -> instance.group(
			Codec.STRING.fieldOf("tabletId").forGetter(OfflineTabletSpec::tabletId),
			Codec.STRING.fieldOf("tabletName").forGetter(OfflineTabletSpec::displayName),
			Codec.INT.fieldOf("maxPressure").forGetter(OfflineTabletSpec::maxPressure),
			InkingCodecs.VECTOR2.fieldOf("physicalSize").forGetter(OfflineTabletSpec::physicalSize),
			InkingCodecs.VECTOR2.fieldOf("inputSize").forGetter(OfflineTabletSpec::inputSize),
			Codec.INT.fieldOf("penButtons").forGetter(OfflineTabletSpec::penButtons),
			Codec.INT.fieldOf("auxButtons").forGetter(OfflineTabletSpec::auxButtons))
			.apply(instance, OfflineTabletSpec::new));
}
