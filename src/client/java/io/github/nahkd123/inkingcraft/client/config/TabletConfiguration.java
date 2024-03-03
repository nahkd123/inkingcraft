package io.github.nahkd123.inkingcraft.client.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.nahkd123.inking.api.tablet.Tablet;
import io.github.nahkd123.inkingcraft.client.config.binding.BindingConfiguration;
import io.github.nahkd123.inkingcraft.client.utils.InkingCodecs;

public class TabletConfiguration {
	private String tabletId;
	private boolean enable;
	private int pointerColor;
	private AreaMapping mapping;
	private PressureMapping pressureMapping;
	private BindingConfiguration binding;

	public TabletConfiguration(String tabletId, boolean enable, int pointerColor, AreaMapping areaMapping, PressureMapping pressureMapping, BindingConfiguration binding) {
		this.tabletId = tabletId;
		this.enable = enable;
		this.mapping = areaMapping;
		this.pressureMapping = pressureMapping;
		this.pointerColor = pointerColor;
		this.binding = binding;
	}

	public AreaMapping getAreaMapping() { return mapping; }

	public PressureMapping getPressureMapping() { return pressureMapping; }

	public BindingConfiguration getBinding() { return binding; }

	public boolean isEnabled() { return enable; }

	public void setEnable(boolean enable) { this.enable = enable; }

	public String getTabletId() { return tabletId; }

	public int getPointerColor() { return pointerColor; }

	public void setPointerColor(int pointerColor) { this.pointerColor = pointerColor; }

	public static final Codec<TabletConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("tabletId").forGetter(TabletConfiguration::getTabletId),
		Codec.BOOL.optionalFieldOf("enabled", true).forGetter(TabletConfiguration::isEnabled),
		InkingCodecs.COLOR.optionalFieldOf("pointerColor", 0xFFFFFF).forGetter(TabletConfiguration::getPointerColor),
		AreaMapping.CODEC.fieldOf("areaMapping").forGetter(TabletConfiguration::getAreaMapping),
		PressureMapping.CODEC.fieldOf("pressureMapping").forGetter(TabletConfiguration::getPressureMapping),
		BindingConfiguration.CODEC.fieldOf("binding").forGetter(TabletConfiguration::getBinding))
		.apply(instance, TabletConfiguration::new));

	public static TabletConfiguration createDefault(Tablet tablet) {
		// @formatter:off
		return new TabletConfiguration(
			tablet.getTabletId(), true,
			0xFFFFFF,
			AreaMapping.createDefault(tablet.getSpec()),
			PressureMapping.createDefault(tablet.getSpec()),
			BindingConfiguration.createDefault());
		// @formatter:on
	}
}
