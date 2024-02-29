package io.github.nahkd123.inkingcraft.client.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.nahkd123.inking.api.tablet.Tablet;
import io.github.nahkd123.inkingcraft.client.utils.InkingCodecs;

public class TabletConfiguration {
	private String tabletId;
	private boolean enable;
	private boolean interactUI;
	private AreaMapping mapping;
	private PressureMapping pressureMapping;
	private int pointerColor;

	public TabletConfiguration(String tabletId, boolean enable, boolean interactUI, AreaMapping areaMapping, PressureMapping pressureMapping, int pointerColor) {
		this.tabletId = tabletId;
		this.enable = enable;
		this.interactUI = interactUI;
		this.mapping = areaMapping;
		this.pressureMapping = pressureMapping;
		this.pointerColor = pointerColor;
	}

	public AreaMapping getAreaMapping() { return mapping; }

	public PressureMapping getPressureMapping() { return pressureMapping; }

	public boolean isEnabled() { return enable; }

	public void setEnable(boolean enable) { this.enable = enable; }

	public boolean isInteractUI() { return interactUI; }

	public void setInteractUI(boolean interactUI) { this.interactUI = interactUI; }

	public String getTabletId() { return tabletId; }

	public int getPointerColor() { return pointerColor; }

	public void setPointerColor(int pointerColor) { this.pointerColor = pointerColor; }

	public static final Codec<TabletConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("tabletId").forGetter(TabletConfiguration::getTabletId),
		Codec.BOOL.fieldOf("enabled").forGetter(TabletConfiguration::isEnabled),
		Codec.BOOL.fieldOf("interactUI").forGetter(TabletConfiguration::isInteractUI),
		AreaMapping.CODEC.fieldOf("areaMapping").forGetter(TabletConfiguration::getAreaMapping),
		PressureMapping.CODEC.fieldOf("pressureMapping").forGetter(TabletConfiguration::getPressureMapping),
		InkingCodecs.COLOR.optionalFieldOf("pointerColor", 0xFFFFFF).forGetter(TabletConfiguration::getPointerColor))
		.apply(instance, TabletConfiguration::new));

	public static TabletConfiguration createDefault(Tablet tablet) {
		// @formatter:off
		return new TabletConfiguration(
			tablet.getTabletId(), true, true,
			AreaMapping.createDefault(tablet.getSpec()),
			PressureMapping.createDefault(tablet.getSpec()),
			0xFFFFFF);
		// @formatter:on
	}
}
