package io.github.nahkd123.inkingcraft.client.config.binding.trigger;

import io.github.nahkd123.inkingcraft.InkingCraft;
import io.github.nahkd123.inkingcraft.client.bridge.KeyBindingBridge;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;

public class KeybindsTriggers {
	public static SimpleTriggerFactory of(KeyBinding keybinding) {
		// @formatter:off
		return new SimpleTriggerFactory(
			Text.literal("Keybind: ")
				.append(Text.translatable(keybinding.getTranslationKey())),
			Text.literal("From vanilla keybinding list."),
			holding -> {
				keybinding.setPressed(holding);
				if (holding) ((KeyBindingBridge) keybinding).increasePressCounter();
			});
		// @formatter:on
	}

	@SuppressWarnings("resource")
	public static void registerAll() {
		for (KeyBinding kb : MinecraftClient.getInstance().options.allKeys)
			of(kb).register(InkingCraft.id("keybinds/" + kb.getTranslationKey().toLowerCase().replace('.', '_')));
	}
}
