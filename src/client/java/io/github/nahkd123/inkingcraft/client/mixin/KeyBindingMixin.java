package io.github.nahkd123.inkingcraft.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import io.github.nahkd123.inkingcraft.client.bridge.KeyBindingBridge;
import net.minecraft.client.option.KeyBinding;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin implements KeyBindingBridge {
	@Shadow
	private int timesPressed;

	@Override
	public void increasePressCounter() {
		timesPressed++;
	}
}
