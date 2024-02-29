package io.github.nahkd123.inkingcraft.client.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * <p>
 * Global InkingCraft configuration.
 * </p>
 */
public class InkingConfiguration {
	private boolean showPointers = true;
	private boolean showIndicators = true;

	public InkingConfiguration(boolean showPointers, boolean showIndicators) {
		this.showPointers = showPointers;
		this.showIndicators = showIndicators;
	}

	public InkingConfiguration() {}

	/**
	 * <p>
	 * Check if Inking pointers should be rendered to the screen.
	 * </p>
	 * 
	 * @return Show pointers state.
	 */
	public boolean isShowPointers() { return showPointers; }

	public void setShowPointers(boolean showPointers) { this.showPointers = showPointers; }

	/**
	 * <p>
	 * Check if pointer indicators should be rendered to the screen. Indicator
	 * includes: current pressure and current tilting angles.
	 * </p>
	 * 
	 * @return Show indicators state.
	 */
	public boolean isShowIndicators() { return showIndicators; }

	public void setShowIndicators(boolean showIndicators) { this.showIndicators = showIndicators; }

	public static final Codec<InkingConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.BOOL.optionalFieldOf("showPointers", true).forGetter(InkingConfiguration::isShowPointers),
		Codec.BOOL.optionalFieldOf("showIndicators", true).forGetter(InkingConfiguration::isShowIndicators))
		.apply(instance, InkingConfiguration::new));
}
