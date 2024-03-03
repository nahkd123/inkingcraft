package io.github.nahkd123.inkingcraft.client.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import io.github.nahkd123.inkingcraft.client.bridge.ClientMouseBridge;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;

@Mixin(Mouse.class)
public abstract class MouseMixin implements ClientMouseBridge {
	@Shadow
	@Final
	private MinecraftClient client;

	@Shadow
	public abstract void onCursorPos(long window, double x, double y);

	@Shadow
	public abstract void onMouseButton(long window, int button, int action, int mods);

	@Override
	public void inkingOnMousePosition(long window, double x, double y) {
		onCursorPos(window, x, y);
	}

	@Override
	public void inkingOnMouseButton(long window, int button, int action, int mods) {
		client.execute(() -> onMouseButton(window, button, action, mods));
	}
}
