package io.github.nahkd123.inkingcraft.client.config.binding.trigger;

import net.minecraft.text.Text;

public class InkingTriggers {
	// @formatter:off
	public static final SimpleTriggerFactory EMPTY = new SimpleTriggerFactory(
		Text.literal("Empty"),
		Text.literal("Do nothing."),
		holding -> {});
	// @formatter:on
}
